# microsphere-spring-context（1.x）

[← 1.x 版本总览](./README.md) · [English](../context.md) · [开发者文档](../../../developer-guide/1.x/zh/context.md) · [0.2.x 文档](../../current/zh/context.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-spring-context` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8**（`maven.compiler.source/target = 8`） |
| Spring Framework 兼容范围 | **4.3.x – 5.3.x** |
| Servlet 命名空间 | `javax.*`（本模块不涉及 Servlet） |

Spring / Reactor 版本由父 POM（`microsphere-spring-parent`）的 Maven profile 决定，模块自身的 profile 只负责追加可选依赖；`spring-framework-5.3` 为默认激活：

| Profile | Spring Framework | Reactor BOM | 本模块追加的可选依赖 |
|---------|------------------|-------------|--------------|
| `spring-framework-4.3` | `4.3.30.RELEASE` | `Bismuth-SR17` | `microsphere-spring-compatible` |
| `spring-framework-5.0` | `5.0.20.RELEASE` | `Bismuth-SR17` | `microsphere-spring-compatible`、`spring-context-indexer` |
| `spring-framework-5.1` | `5.1.20.RELEASE` | `Californium-SR23` | `spring-context-indexer` |
| `spring-framework-5.2` | `5.2.25.RELEASE` | `Dysprosium-SR25` | `spring-context-indexer` |
| `spring-framework-5.3`（默认） | `5.3.39` | `2020.0.47` | `spring-context-indexer` |

> 说明：4.3 / 5.0 两条 profile 需要 `microsphere-spring-compatible` 提供 Spring 5.1+ 才有的工具类与注解（见 [compatible.md](./compatible.md)）。升级到低版本 Spring 构建时请确保该模块在 classpath 上。

依赖姿态（`microsphere-spring-context/pom.xml`）：

| 依赖 | 范围 | 是否 optional |
|------|------|---------------|
| `microsphere-java-core` | compile | 否（必需） |
| `spring-context` / `spring-context-support` / `spring-beans` / `spring-aop` | provided | 是 |
| `javax.annotation-api` | provided | 是 |
| `jackson-annotations` / `jackson-databind` | provided | 是（仅 JSON 属性源需要） |
| `snakeyaml` | provided | 是（仅 YAML 属性源需要） |
| `microsphere-annotation-processor` | provided | 是（编译期，生成配置元数据） |

> 由于 Spring 依赖是 `optional` + `provided`，本模块**不会**把 Spring 传递给你的应用；请使用 `microsphere-spring-dependencies` BOM 或 `spring-boot-starter` 自行管理 Spring 版本。

引入方式：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-dependencies</artifactId>
            <version>0.1.39</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-context</artifactId>
    </dependency>
    <!-- 按功能补齐 optional 依赖 -->
    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
    </dependency>
</dependencies>
```

---

## 1. `@ResourcePropertySource`：可通配、可排序、可自动刷新

Spring 原生 `@PropertySource` 不支持 `classpath*:` 通配、不支持声明插入位置、也不会在文件变化后重新加载。`@ResourcePropertySource`（包 `io.microsphere.spring.config.context.annotation`）解决这三件事。

注解声明（`release-1.x` 源码）：

```java
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@PropertySourceExtension
@Repeatable(ResourcePropertySources.class)
@Import(ResourcePropertySourceLoader.class)
public @interface ResourcePropertySource { ... }
```

全部属性均通过 `@AliasFor(annotation = PropertySourceExtension.class)` 映射到元注解 `@PropertySourceExtension`，因此可以自定义派生注解。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `name` | `String` | `""` | 属性源名；为空时由资源路径推导 |
| `value` | `String[]` | `{}` | 资源位置，支持 `classpath*:` 通配 |
| `autoRefreshed` | `boolean` | `false` | 文件变更时重新加载并发布 `PropertySourcesChangedEvent` |
| `first` | `boolean` | `false` | 插到属性源链表**最前面**（优先级最高） |
| `before` | `String` | `""` | 插在指定属性源之前 |
| `after` | `String` | `""` | 插在指定属性源之后 |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator.class` | 通配命中多个资源时的排序策略 |
| `ignoreResourceNotFound` | `boolean` | `false` | 未命中任何资源时是否忽略 |
| `encoding` | `String` | `"${file.encoding:UTF-8}"` | 支持占位符，运行期从环境解析 |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory.class` | 属性源创建工厂 |

> 注意 `encoding` 默认值是**占位符字符串** `"${file.encoding:UTF-8}"`，与 `@YamlPropertySource` 的 `"UTF-8"` 字面量不同；若要固定编码请显式声明。

```java
@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/META-INF/config/*.properties",
        autoRefreshed = true,   // 文件变更后自动重载
        after = "systemProperties"
)
public class AppConfig {

    @Autowired
    private Environment environment;

    @Value("${app.feature.enabled:false}")
    private boolean featureEnabled;
}
```

监听变更事件（`io.microsphere.spring.config.env.event`）：

```java
@Component
public class ConfigReloadLogger implements ApplicationListener<PropertySourcesChangedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ConfigReloadLogger.class);

    @Override
    public void onApplicationEvent(PropertySourcesChangedEvent event) {
        log.info("PropertySource [{}] changed, new size = {}",
                event.getPropertySourceName(), event.getPropertySource().size());
    }
}
```

同名多条声明可使用 `@Repeatable` 的容器注解 `@ResourcePropertySources`：

```java
@Configuration
@ResourcePropertySource(name = "a", value = "classpath:/a.properties")
@ResourcePropertySource(name = "b", value = "classpath:/b.properties", first = true)
public class MultiSourceConfig {
}
```

## 2. `@YamlPropertySource` / `@JsonPropertySource`

两者都定义在 `io.microsphere.spring.config.env.annotation`，本质是 `@ResourcePropertySource` 的派生注解，只是把 `factory` 固定在元注解上：

```java
@Target(TYPE) @Retention(RUNTIME) @Inherited @Documented
@ResourcePropertySource(factory = YamlPropertySourceFactory.class)
public @interface YamlPropertySource { ... }
```

| 属性 | `@YamlPropertySource` | `@JsonPropertySource` |
|------|-----------------------|------------------------|
| 属性个数 | 9 | 9 |
| 相对 `@ResourcePropertySource` 缺少 | `factory`（已在元注解固定） | `factory`（已在元注解固定） |
| `encoding` 默认值 | `"UTF-8"` | `"UTF-8"` |
| 其余属性 | `name`/`value`/`autoRefreshed`/`first`/`before`/`after`/`resourceComparator`/`ignoreResourceNotFound` 与上表一致 | 同左 |
| 底层依赖 | `org.yaml:snakeyaml` | `com.fasterxml.jackson.core:jackson-databind` |

```java
@Configuration
@YamlPropertySource("classpath:/config/application.yaml")
@JsonPropertySource(name = "flags", value = "classpath:/config/feature-flags.json")
public class YamlJsonConfig {
}
```

多文档 YAML 由 `io.microsphere.spring.config.env.config.ResourceYamlProcessor` 处理；如只需读取首个文档，行为与 Spring 的 `YamlProcessor` 派生用法一致。

## 3. `@DefaultPropertiesPropertySource`

用于提供"兜底"默认值，放在属性源链表的**末尾**，因此不会覆盖外部配置。定义于 `io.microsphere.spring.config.context.annotation`。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `properties` | `String[]` | `{}` | 内联 `key=value` 对 |
| `value` / `locations` | `String[]` | `{}` | 互为 `@AliasFor` 的资源位置 |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator.class` | 资源排序 |
| `ignoreResourceNotFound` | `boolean` | `false` | 未命中是否忽略 |
| `encoding` | `String` | `"UTF-8"` | 字面量，非占位符 |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory.class` | 工厂 |

> 相比 `@ResourcePropertySource`，本注解**没有** `name`/`first`/`before`/`after`/`autoRefreshed`——它的定位就是"末尾兜底"。

```java
@Configuration
@DefaultPropertiesPropertySource(
        properties = {
                "app.thread-pool.core-size=4",
                "app.thread-pool.max-size=16"
        },
        locations = "classpath:/META-INF/default-config/*.properties"
)
public class DefaultConfig {
}
```

## 4. TTL 缓存：`@EnableTTLCaching` + `@TTLCacheable` / `@TTLCachePut`

Spring 的 `CacheManager` 只能整体设置过期策略；`io.microsphere.spring.cache.annotation` 提供**按条目**设置 TTL。

```java
@Target(TYPE) @Retention(RUNTIME) @Documented
@EnableCaching
@Import(TTLCachingConfiguration.class)
public @interface EnableTTLCaching {
    // 三个属性均 @AliasFor(EnableCaching.class)
    boolean proxyTargetClass() default false;
    AdviceMode mode() default PROXY;
    int order() default LOWEST_PRECEDENCE;
}
```

`@TTLCacheable`（`@Target({TYPE, METHOD})`）在 `@Cacheable(cacheResolver = BEAN_NAME)` 之上扩展：

| 属性 | 类型 | 默认值 | 来源 |
|------|------|--------|------|
| `expire` | `long` | **必填（无默认值）** | 本注解 |
| `timeUnit` | `TimeUnit` | `MILLISECONDS` | 本注解 |
| `cacheManagers` | `String[]` | `{}` | 本注解（限定生效的 CacheManager Bean 名） |
| `value` / `cacheNames` | `String[]` | `{}` | `@AliasFor(Cacheable.class)` |
| `key` / `keyGenerator` | `String` | `""` | `@AliasFor(Cacheable.class)` |
| `condition` / `unless` | `String` | `""` | `@AliasFor(Cacheable.class)` |
| `sync` | `boolean` | `false` | `@AliasFor(Cacheable.class)` |

`@TTLCachePut` 属性集相同，仅**没有 `sync`**（`@CachePut` 无此语义）。

```java
@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @TTLCacheable(cacheNames = "products", key = "#id", expire = 30, timeUnit = TimeUnit.MINUTES)
    public Product find(Long id) {
        return repository.findById(id).orElse(null);
    }

    @TTLCachePut(cacheNames = "products", key = "#product.id", expire = 30, timeUnit = TimeUnit.MINUTES)
    public Product update(Product product) {
        return repository.save(product);
    }
}

@Configuration
@EnableTTLCaching
public class CachingConfig {
}
```

要点与限制：

- `expire` **没有默认值**，漏写会直接编译失败——这是刻意设计，避免"以为设了 TTL 其实没设"。
- TTL 上下文由 `io.microsphere.spring.cache.TTLContext`（ThreadLocal）在一次调用内传递，`io.microsphere.spring.cache.intereptor.TTLCacheResolver`（注意包名拼写为 `intereptor`）据此为条目计算过期时间；该 Resolver 的 Bean 名为 `ttlCacheResolver`，通过 `@Cacheable(cacheResolver = ...)` 固定注入。
- ⚠️ **Redis TTL 在 1.x 同样不可用**：`io.microsphere.spring.cache.redis.TTLRedisConfiguration` / `TTLRedisCacheWriterWrapper` 的源码整段被注释，模块 pom 也没有 `spring-data-redis` 依赖。不要按 0.2.x README 的描述假定 Redis 支持。
- 实际可用的过期能力取决于你注入的 `Cache` 实现；Spring 的 `ConcurrentMapCacheManager` 本身不驱逐条目，`TTLCacheResolver` 通过在读取时判断 `TTLContext` 时间戳实现"逻辑过期"。
- `mode = AdviceMode.ASPECTJ` 需自行引入 AspectJ 编织。

## 5. 宽松绑定：`@EnableConfigurationBeanBinding`

把松散格式的配置键（`server-port` / `serverPort` / `SERVER_PORT`）绑定到 POJO，并注册为 Bean。定义于 `io.microsphere.spring.beans.factory.annotation`：

```java
@Target({TYPE, ANNOTATION_TYPE}) @Retention(RUNTIME) @Documented
@Import(ConfigurationBeanBindingRegistrar.class)
@Repeatable(EnableConfigurationBeanBindings.class)
public @interface EnableConfigurationBeanBinding {
    boolean DEFAULT_MULTIPLE = false;
    boolean DEFAULT_IGNORE_UNKNOWN_FIELDS = true;
    boolean DEFAULT_IGNORE_INVALID_FIELDS = true;

    String prefix();       // 必填
    Class<?> type();       // 必填
    boolean multiple() default DEFAULT_MULTIPLE;
    boolean ignoreUnknownFields() default DEFAULT_IGNORE_UNKNOWN_FIELDS;
    boolean ignoreInvalidFields() default DEFAULT_IGNORE_INVALID_FIELDS;
}
```

```java
public class ServerConfig {
    private String host = "0.0.0.0";
    private int port;
    // getters / setters
}

@Configuration
@EnableConfigurationBeanBinding(prefix = "server", type = ServerConfig.class)
public class BindingConfig {

    @Autowired
    private ServerConfig serverConfig;   // 已绑定并可注入
}
```

`application.properties`：

```properties
server.host=127.0.0.1
server.port=9090
```

`multiple = true` 时前缀下可绑定集合，Bean 别名由 `io.microsphere.spring.beans.factory.support` 下的 `ConfigurationBeanAliasGenerator` 生成；`DefaultConfigurationBeanAliasGenerator`、`HyphenAliasGenerator`、`UnderScoreJoinAliasGenerator`、`JoinAliasGenerator` 提供不同命名策略（1.x 测试 `EnableConfigurationBeanBindingTestForAlias` 断言了 `users-a` 与 `users_a` 两种别名形态）。

## 6. 可监听 `Environment`

`io.microsphere.spring.core.env` 提供 `PropertyResolverListener`，可观察属性解析过程，适合排查"这个值到底来自哪个属性源"。真实回调签名（`release-1.x` 源码）：

```java
public interface PropertyResolverListener {

    default void beforeGetProperty(ConfigurablePropertyResolver resolver, String name,
                                  @Nullable Class<?> targetType, @Nullable Object defaultValue) {}

    default void afterGetProperty(ConfigurablePropertyResolver resolver, String name,
                                  @Nullable Class<?> targetType,
                                  @Nullable Object value, @Nullable Object defaultValue) {}
    // 另含 placeholder 解析、ConversionService、值分隔符等钩子
}
```

该能力**默认关闭**，由 `META-INF/spring.factories` 中的 `ApplicationContextInitializer` 按属性开关：

```properties
# 开启可监听 Environment
microsphere.spring.listenable-environment.enabled=true

# 开启可监听 AutowireCandidateResolver
microsphere.spring.listenable-autowire-candidate-resolver.enabled=true
```

`spring.factories` 实际注册（1.x）：

```
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.spring.context.annotation.AutoRegistrationBeanInitializer,\
io.microsphere.spring.context.event.EventPublishingBeanInitializer,\
io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer,\
io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer
```

> 类名差异：1.x 为 `ListenableConfigurableEnvironmentInitializer`；0.2.x 的对应实现另有其名。以所属分支源码为准。

## 7. 并行预实例化单例

`io.microsphere.spring.context.event.ParallelPreInstantiationSingletonsBeanFactoryListener` 实现 `BeanFactoryListener`：在 `preInstantiateSingletons()` 阶段解析 Bean 依赖图，把互不依赖的 Singleton 放到线程池并发创建，从而缩短启动时间。

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `microsphere.spring.pre-instantiation.singletons.threads` | `Math.max(1, availableProcessors)` | 线程数；设为 `< 1` 关闭并行 |
| `microsphere.spring.pre-instantiation.singletons.thread.name-prefix` | `Parallel-Pre-Instantiation-Singletons-Thread-` | 线程名前缀 |

监听器不会被自动注册，需显式声明为 Bean：

```java
@Bean
public ParallelPreInstantiationSingletonsBeanFactoryListener parallelPreInstantiationListener() {
    return new ParallelPreInstantiationSingletonsBeanFactoryListener();
}
```

```properties
microsphere.spring.pre-instantiation.singletons.threads=4
```

> 副作用：Bean 创建顺序不再确定，任何依赖初始化顺序的逻辑（静态持有、`@PostConstruct` 里读别的 Bean）都可能出问题。建议先在非生产环境验证。

## 8. 事件拦截：`@EnableEventExtension`

定义于 `io.microsphere.spring.context.event`：

```java
@Target({TYPE, ANNOTATION_TYPE}) @Retention(RUNTIME) @Documented
@OverrideAnnotationAttributes
@Import(EventExtensionRegistrar.class)
public @interface EnableEventExtension {

    String NO_EXECUTOR = "N/E";

    boolean intercepted() default true;

    String executorForListener() default NO_EXECUTOR;

    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER};
}
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `intercepted` | `true` | 是否启用 `ApplicationEventInterceptor` / `ApplicationListenerInterceptor` 链 |
| `executorForListener` | `"N/E"`（不异步） | 监听器异步执行所用 `Executor` 的 **Bean 名** |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 拦截器发现来源，取值 `io.microsphere.spring.beans.BeanSource` |

拦截器 SPI 为责任链式（两者均 `extends Ordered`，默认 `LOWEST_PRECEDENCE`）：

```java
@Component
public class SlowEventInterceptor implements ApplicationListenerInterceptor {

    @Override
    public void intercept(ApplicationListener<?> listener, ApplicationEvent event,
                         ApplicationListenerInterceptorChain chain) {
        long start = System.currentTimeMillis();
        chain.intercept(listener, event);
        long cost = System.currentTimeMillis() - start;
        if (cost > 100) {
            log.warn("Listener {} took {} ms for {}", listener, cost, event.getClass());
        }
    }
}
```

```java
@Configuration
@EnableEventExtension(intercepted = true, executorForListener = "taskExecutor")
public class EventConfig {
}
```

`ApplicationEventInterceptor` 面向事件本身：

```java
void intercept(ApplicationEvent event, ResolvableType eventType,
               ApplicationEventInterceptorChain chain);
```

## 9. 其余注解与开关

| 注解 / 接口 | 位置 | 关键点 |
|-------------|------|--------|
| `@ImportOptional` | `io.microsphere.spring.context.annotation` | `String[] value()`（必填）；配合 `ImportOptionalSelector` 按条件 `@Import`，可被属性 `microsphere.spring.@ImportOptional.value=` 覆盖 |
| `@OverrideAnnotationAttributes` | 同上 | 仅 `@Target(ANNOTATION_TYPE)`；允许外部配置覆盖注解属性，`strategy()` 默认 `ConfigurationPropertyOverrideAnnotationAttributesStrategy.class` |
| `@EnableAutoRegistrationBean` | 同上 | **无属性**；由 `AutoRegistrationBeanInitializer` 批量注册 `@Bean` 之外的 Bean，可用 `microsphere.spring.beans.{bean-name}.auto-registered=false` 逐个关闭（常量名拼写为 `BEANS_AUTO_REGISTERED_PROEPRTY_NAME`） |
| `@EnableSpringConverterAdapter` | `io.microsphere.spring.core.convert.annotation` | 把 `microsphere-java-core` 的 `Converter` 接入 Spring `ConversionService` |
| `EnvironmentEnabled` | `io.microsphere.spring.config.env` | 接口约定：属性名 `microsphere.spring.<类全名>.enabled`，`getDefaultEnabled()` 默认 **true** |
| `@PropertySourceExtension` | `io.microsphere.spring.config.context.annotation` | 上述属性源注解的元注解，可用来做自己的派生注解 |

工具类速查：

| 类 | 用途 |
|----|------|
| `io.microsphere.spring.context.ApplicationContextUtils` | 上下文/属性源查询与操作 |
| `io.microsphere.spring.beans.factory.BeanFactoryUtils` | Bean 定义/名称遍历 |
| `io.microsphere.spring.beans.factory.support.BeanDefinitionBuilderUtils` | Fluent 构造 `BeanDefinition` |
| `io.microsphere.spring.beans.factory.ObjectProviderUtils` | `ObjectProvider` 便捷解包（1.x 独有） |
| `io.microsphere.spring.beans.factory.DependencyTreeWalker` | 依赖图遍历（并行预实例化底层） |
| `io.microsphere.spring.context.event.DeferredApplicationEventPublisher` | 延迟到上下文就绪后再发布事件 |
| `io.microsphere.spring.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter` | 兼容低版本的 BPP 适配基类（1.x 独有） |
