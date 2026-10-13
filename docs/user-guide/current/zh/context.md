# microsphere-spring-context · 0.2.x

[← 当前版本总览](./README.md) · [English](../context.md) · [开发者文档](../../../developer-guide/current/zh/context.md) · [1.x 文档](../../1.x/zh/context.md)

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-context` |
| 分支 | `release`（0.2.x 线） |
| 版本 | 已发布 `0.2.39`；开发版 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x**（回归验证于 6.0.23 / 6.1.21 / 6.2.18 / 7.0.9） |
| 必需依赖 | `microsphere-java-core` 0.3.19 |
| 可选依赖 | `spring-context`、`spring-context-support`、`spring-beans`、`spring-aop`、`spring-context-indexer`、`javax.annotation-api`、`jackson-annotations`、`jackson-databind` 2.22.2、`snakeyaml` 2.6 |

> 说明：Spring 依赖为 optional，因此本模块可与 6.0–7.0 任意版本搭配；Jackson 仅 `@JsonPropertySource` 需要，SnakeYAML 仅 `@YamlPropertySource` 需要。

本模块的特性通过两种方式生效：一是注解自带 `@Import(...Loader/Registrar)`（显式开启），二是 `META-INF/spring.factories` 注册的 `ApplicationContextInitializer`（由配置属性开关，默认多为 `false`）。

```properties
# META-INF/spring.factories（本模块实际内容）
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.spring.context.annotation.AutoRegistrationBeanInitializer,\
io.microsphere.spring.context.event.EventPublishingBeanInitializer,\
io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer,\
io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer
```

---

## 1. `@ResourcePropertySource`：增强属性源

Spring 原生 `@PropertySource` 不支持通配符批量加载、不能精确指定插入位置、也无法在文件变更后重载。`@ResourcePropertySource` 位于 `io.microsphere.spring.config.context.annotation`，通过 `@Import(ResourcePropertySourceLoader.class)` 在容器启动期解析资源并写入 `ConfigurableEnvironment#getPropertySources()`；`autoRefreshed = true` 时注册文件监听器，文件变更即重新加载。注解为 `@Target(TYPE) @Inherited @Repeatable`（聚合注解 `@ResourcePropertySources`）。

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `value` | `String[]` | `{}` | 资源位置，支持 `classpath*:` 通配符 |
| `name` | `String` | `""` | 属性源名，缺省自动生成 |
| `autoRefreshed` | `boolean` | `false` | 文件变更自动重载 |
| `first` | `boolean` | `false` | 插入到首位 |
| `before` | `String` | `""` | 插入到指定属性源之前 |
| `after` | `String` | `""` | 插入到指定属性源之后 |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator.class` | 多命中时的排序 |
| `ignoreResourceNotFound` | `boolean` | `false` | 缺失资源忽略而非抛异常 |
| `encoding` | `String` | `"${file.encoding:UTF-8}"` | 支持占位符 |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory.class` | 自定义解析工厂 |

上述属性由元注解 `@PropertySourceExtension`（`@Target(ANNOTATION_TYPE)`）承载，自定义注解可用 `@AliasFor(annotation = PropertySourceExtension.class)` 复用同一套语义。

```java
@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/META-INF/config/*.properties",
        autoRefreshed = true
)
public class AppConfig {
}

// 与 systemEnvironment 竞争优先级
@Configuration
@ResourcePropertySource(value = "classpath:/config/override.properties", before = "systemEnvironment")
public class OverrideConfig {
}

// 多模块 jar 聚合，允许缺失
@Configuration
@ResourcePropertySource(value = "classpath*:/optional/*.properties", ignoreResourceNotFound = true)
public class LenientConfig {
}
```

---

## 2. `@YamlPropertySource` / `@JsonPropertySource`

位于 `io.microsphere.spring.config.env.annotation`，是 `@ResourcePropertySource` 的派生注解，分别固定 `factory` 为 `YamlPropertySourceFactory` / `JsonPropertySourceFactory`。因此除 `factory` 外拥有全部属性（含 `autoRefreshed`、`first`、`before`、`after`、`resourceComparator`、`ignoreResourceNotFound`），且 `encoding` 默认值固定为 `"UTF-8"`。YAML 结果封装为 `ImmutableMapPropertySource`，嵌套结构展平为 `a.b.c`；JSON 解析依赖 Jackson。

```java
@Configuration
@YamlPropertySource(value = "classpath:/config/application.yaml", autoRefreshed = true)
@JsonPropertySource("classpath:/config/feature-flags.json")
public class AppConfig {
}
```

```yaml
# classpath:/config/application.yaml → 属性 server.port / datasource.url
server:
  port: 9090
datasource:
  url: jdbc:h2:mem:demo
```

---

## 3. `@DefaultPropertiesPropertySource`

把属性写入优先级最低的兜底属性源 `defaultProperties`，适合类库提供默认值而不覆盖用户配置。属性：`properties`（`String[]`，`key=value`）、`value`/`locations`（互为 `@AliasFor`，资源位置）、`resourceComparator`、`ignoreResourceNotFound`、`encoding`（默认 `"UTF-8"`）、`factory`。`@Repeatable`（容器注解 `DefaultPropertiesPropertySources`）。

```java
@Configuration
@DefaultPropertiesPropertySource(properties = "microsphere.web.enabled=true")
@DefaultPropertiesPropertySource(value = "classpath*:/META-INF/defaults/*.properties")
public class LibDefaultsConfig {
}
```

---

## 4. TTL 缓存

`@EnableTTLCaching`（`io.microsphere.spring.cache.annotation`）= `@EnableCaching` + 注册名为 `ttlCacheResolver` 的 `TTLCacheResolver`。`@TTLCacheable` / `@TTLCachePut` 是 `@Cacheable` / `@CachePut` 的派生注解（`cacheResolver` 已固定）；切面执行时把注解上的 `expire`/`timeUnit` 写入 `TTLContext`（ThreadLocal），期待由支持 TTL 的缓存实现去读取，从而实现**每次调用独立过期**。

> ⚠️ 0.2.x 中 **本库自身从不消费这个 TTL**：唯一读取 `TTLContext` 的 `io.microsphere.spring.cache.redis.TTLRedisCacheWriterWrapper` 与 `TTLRedisConfiguration` 在源码中整体处于注释状态，因此 `TTLCacheResolver` 只写不读（`TTLCacheResolver.java:121`）。配合 Spring 原生 CacheManager 使用时，`@TTLCacheable` 的表现与 `@Cacheable` 完全一致，`expire` 会被静默忽略。要让它真正生效，必须自行接入一个在写入时调用 `TTLContext.getTTL()`（或 `doWithTTL`）的 `Cache`/`CacheWriter`。另注意属性名是 `expire`，不是 README 里的 `ttl`。

`@EnableTTLCaching` 属性（均 `@AliasFor` 到 `@EnableCaching`）：`proxyTargetClass`（`boolean`，`false`）、`mode`（`AdviceMode`，`PROXY`）、`order`（`int`，`LOWEST_PRECEDENCE`）。

`@TTLCacheable`（`@Target({TYPE, METHOD}) @Inherited`）：

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `expire` | `long` | **必填** | 过期时长 |
| `timeUnit` | `TimeUnit` | `MILLISECONDS` | `expire` 的时间单位 |
| `cacheManagers` | `String[]` | `{}` | Microsphere 扩展：按 Bean 名选 CacheManager，空为全部 |
| `cacheNames` / `value` | `String[]` | `{}` | 缓存名 |
| `key` | `String` | `""` | SpEL |
| `keyGenerator` | `String` | `""` | Bean 名 |
| `condition` / `unless` | `String` | `""` | SpEL |
| `sync` | `boolean` | `false` | 同 `@Cacheable` |

`@TTLCachePut` 属性相同但**无** `sync`；不存在 TTL 版 `@CacheEvict`（直接用原生 `@CacheEvict`）。注意解析器包名拼写为 `io.microsphere.spring.cache.intereptor`（历史遗留，导入时需按真实包名）。

```java
@Configuration
@EnableTTLCaching(proxyTargetClass = true)
public class CachingConfig {
}

@Service
public class ProductService {

    @TTLCacheable(cacheNames = "products", key = "#id", expire = 1, timeUnit = TimeUnit.MINUTES)
    public Product findById(Long id) { return repository.load(id); }

    @TTLCachePut(cacheNames = "products", key = "#product.id", expire = 5, timeUnit = TimeUnit.MINUTES)
    public Product update(Product product) { return repository.save(product); }

    @CacheEvict(cacheNames = "products", key = "#id")
    public void delete(Long id) { repository.delete(id); }
}
```

程序化控制：`TTLContext.doWithTTL(duration -> cache.put(key, value), Duration.ofSeconds(30));`（另有 `setTTL`/`getTTL`/`clearTTL`）。代理机制与 Spring Cache 一致——同类自调用不触发缓存。

---

## 5. `@EnableConfigurationBeanBinding`：宽松绑定

`@Import(ConfigurationBeanBindingRegistrar.class)` 注册 `configurationBeanBindingPostProcessor`，把 `prefix` 下的属性绑定到目标类型并注册为 Bean；别名策略通过 `SpringFactoriesLoader` 加载（`ConfigurationBeanBindingRegistrar.java:140`），`DefaultConfigurationBeanAliasGenerator`、`HyphenAliasGenerator`、`UnderScoreJoinAliasGenerator` 三个实现都在 `src/main/java` 中，启用后 `user-name`、`userName`、`user_name` 等价。可用 `ConfigurationBeanCustomizer` 定制绑定的 Bean。`@Repeatable`（容器注解 `@EnableConfigurationBeanBindings`）。

> ⚠️ 本模块 **没有在 `src/main/resources/META-INF/spring.factories` 中注册任何 `ConfigurationBeanAliasGenerator`**，三个实现只在 `src/test` 的 spring.factories 里声明。因此默认没有任何别名生成器生效，只有属性名完全一致时才能绑定。要获得中划线/下划线的宽松匹配，需要应用方在自己的 `META-INF/spring.factories` 中补上：
>
> ```properties
> io.microsphere.spring.beans.factory.support.ConfigurationBeanAliasGenerator=\
> io.microsphere.spring.beans.factory.support.DefaultConfigurationBeanAliasGenerator,\
> io.microsphere.spring.beans.factory.support.HyphenAliasGenerator,\
> io.microsphere.spring.beans.factory.support.UnderScoreJoinAliasGenerator
> ```

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `prefix` | `String` | **必填** | 属性前缀 |
| `type` | `Class<?>` | **必填** | 绑定目标 |
| `multiple` | `boolean` | `false` | 绑定一组 Bean（`prefix.<name>.*`） |
| `ignoreUnknownFields` | `boolean` | `true` | 忽略未知属性 |
| `ignoreInvalidFields` | `boolean` | `true` | 忽略类型不符字段 |

```java
public class User { private String name; private int age; /* getters/setters */ }

@Configuration
@EnableConfigurationBeanBinding(prefix = "users", type = User.class, multiple = true,
        ignoreUnknownFields = false, ignoreInvalidFields = false)
public class UsersBindingConfig {
}
```

```properties
users.a.name = name-a
users.a.age  = 18
users.b.name = name-b
```

绑定结果为 `a`、`b` 两个 `User` Bean，可用 `Map<String, User>` 或 `@Qualifier("a")` 注入。单对象场景写 `multiple = false` 并用 `user.name` 形式。

---

## 6. 可监听的 Environment

可拦截属性读取、占位符解析、Profile 激活、属性源变更等事件，用于配置审计与动态刷新排查。接口 `io.microsphere.spring.core.env.EnvironmentListener` 继承 `ProfileListener` 与 `PropertyResolverListener`，全部方法为 `default` 空实现，首个参数一律是 `ConfigurablePropertyResolver`：

```java
public interface PropertyResolverListener {
    default void beforeGetProperty(ConfigurablePropertyResolver propertyResolver, String name,
                                   Class<?> targetType, @Nullable Object defaultValue) { }
    default void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name,
                                  Class<?> targetType, @Nullable Object value,
                                  @Nullable Object defaultValue) { }
    default void beforeGetRequiredProperty(ConfigurablePropertyResolver pr, String name, Class<?> targetType) { }
    default void beforeResolvePlaceholders(ConfigurablePropertyResolver pr, String text) { }
    default void afterResolvePlaceholders(ConfigurablePropertyResolver pr, String text, String result) { }
    // 另有占位符前后缀、值分隔符、ConversionService、required properties 校验等 before/after 回调
}
```

**没有 `@EnableListenableEnvironment` 注解**：由 spring.factories 中的 `ListenableConfigurableEnvironmentInitializer` 在启动早期把 `ConfigurableEnvironment` 替换为 `ListenableConfigurableEnvironment`，默认关闭，需用属性开启。监听器来自 Spring Bean 及 spring.factories 键 `io.microsphere.spring.core.env.EnvironmentListener`（另支持 `.ProfileListener`、`.PropertyResolverListener`）。

```properties
microsphere.spring.listenable-environment.enabled=true      # 默认 false
```

⚠ 如果你不走 `spring.factories` 而是自己注册这个 initializer，请使用真实类名
`io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer`。该类自身的 Javadoc 示例里写的是
`io.microsphere.spring.beans.factory.support.ListenableConfigurableEnvironmentInitializer`，reactor 中并不存在这个类——
照抄会在启动时抛 `ClassNotFoundException`。

```java
@Component
public class AuditEnvironmentListener implements EnvironmentListener {
    @Override
    public void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name,
                                 Class<?> targetType, @Nullable Object value,
                                 @Nullable Object defaultValue) {
        if (name.startsWith("feature.")) {
            log.info("property [{}] resolved to [{}]", name, value);
        }
    }
}
```

同一模式的还有可监听的自动装配候选解析：`ListenableAutowireCandidateResolverInitializer` + 属性 `microsphere.spring.listenable-autowire-candidate-resolver.enabled`（默认 `false`），监听器类型 `AutowireCandidateResolvingListener`。

---

## 7. 并行预实例化单例 Bean

`ParallelPreInstantiationSingletonsBeanFactoryListener`（`io.microsphere.spring.context.event`）在 `onBeanFactoryConfigurationFrozen` 回调中，用 `DefaultBeanDependencyResolver` 解析依赖图，按依赖路径分组并发预实例化非懒加载单例，以缩短冷启动。**没有开关注解**——需把监听器注册为 Bean（`BeanFactoryListeners` 会自动收集所有 `BeanFactoryListener` Bean 与 spring.factories 中的实现）。

```java
@Configuration
public class StartupConfig {
    @Bean
    public ParallelPreInstantiationSingletonsBeanFactoryListener parallelPreInstantiationListener() {
        return new ParallelPreInstantiationSingletonsBeanFactoryListener();
    }
}
```

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `microsphere.spring.pre-instantiation.singletons.threads` | `int` | `max(1, CPU 核数)` | 并行线程数，`< 1` 关闭 |
| `microsphere.spring.pre-instantiation.singletons.thread.name-prefix` | `String` | `Parallel-Pre-Instantiation-Singletons-Thread-` | 线程名前缀 |

注意：Bean 创建变为并发，依赖初始化副作用顺序、或在 `@PostConstruct` 中读取线程本地上下文（`SecurityContext`、`RequestAttributes`）的 Bean 不适合开启；启动日志会输出 `StopWatch` 两段耗时便于评估收益。

---

## 8. `@EnableEventExtension`：事件拦截

`@Import(EventExtensionRegistrar.class)`，为事件发布与监听器执行提供责任链式拦截。属性：`intercepted`（`boolean`，`true`）、`executorForListener`（`String`，`"N/E"` 表示不异步，否则为 `Executor` 的 Bean 名）、`sources`（`BeanSource[]`，默认 `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}`）。

```java
public interface ApplicationEventInterceptor extends Ordered {          // 默认 order = LOWEST_PRECEDENCE
    void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain);
}

public interface ApplicationListenerInterceptor extends Ordered {
    void intercept(ApplicationListener<?> listener, ApplicationEvent event, ApplicationListenerInterceptorChain chain);
}
```

```java
@Configuration
@EnableEventExtension(executorForListener = "eventExecutor")
public class EventConfig {
    @Bean public Executor eventExecutor() { return new ThreadPoolExecutor(4, 8, 60L, SECONDS, new LinkedBlockingQueue<>()); }
}

@Component
public class EventAuditInterceptor implements ApplicationEventInterceptor {
    @Override
    public void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain) {
        long start = System.nanoTime();
        chain.intercept(event, eventType);      // 必须继续责任链
        log.info("event {} dispatched in {} ns", eventType, System.nanoTime() - start);
    }
}
```

---

## 9. 其他特性与工具

| 名称 | 说明 |
|---|---|
| `@EnableAutoRegistrationBean` | 无属性。注册实现 `AutoRegistrationBean` 的类；总开关 `microsphere.spring.beans.auto-registered`（默认 `true`），单个可用 `microsphere.spring.beans.<beanName>.auto-registered=false` |
| `@ImportOptional` | 按类名字符串可选导入，类缺失时静默跳过 |
| `@OverrideAnnotationAttributes` | 覆盖元注解属性值（`@EnableWebMvcExtension` 等借此回写 `@EnableWebExtension`） |
| `@EnableSpringConverterAdapter` | 无属性。把 Spring `ConversionService` 适配给 Microsphere 转换层 |
| `EnvironmentEnabled` | 接口。实现者获得 `microsphere.spring.<FQCN>.enabled` 开关（默认 `true`） |
| `spring:` URL | `SpringProtocolURLStreamHandler` 支持 `spring:env:property-sources://...`，P6Spy 模块借此读配置 |
| `EventPublishingBean*` | Bean 生命周期事件发布，开关 `microsphere.spring.event-publishing-bean.enabled`（默认 `false`） |
| 工具类 | `BeanFactoryUtils`、`BeanDefinitionUtils`、`AnnotatedBeanDefinitionRegistryUtils`、`ApplicationContextUtils`、`EnvironmentUtils`、`PropertySourcesUtils`、`PropertyResolverUtils`、`ResourceLoaderUtils`、`ResourceUtils`、`PropertiesUtils`、`AnnotationUtils`、`ConversionServiceUtils`、`BeanRegistrar`、`DependencyTreeWalker`、`SpringVersion`/`SpringVersionUtils` |
| `FilterMode` | 扩展组件过滤模式，属性 `microsphere.spring.filter-mode`（默认 `SEQUENTIAL`）。⚠ 0.2.x 中该开关是**空转的**：没有任何生产代码读取它，设置与否都不改变行为 |

---

## 小结

`microsphere-spring-context`（已发布 0.2.39 / 开发版 `0.2.40-SNAPSHOT`，Java 17，Spring Framework **6.0.x–7.0.x**）是基础模块。唯一的 compile 依赖是 `microsphere-java-core` 0.3.19，Spring、Jackson、SnakeYAML 全为 optional——这正是同一个二进制能覆盖 6.0–7.0 全部版本的原因。

显式开启由注解自带 `@Import` 完成：属性源家族（`@ResourcePropertySource` 及 YAML/JSON/默认值派生注解，支持通配符、`first`/`before`/`after` 定位与文件变更自动刷新）、TTL 缓存（`@EnableTTLCaching` + `@TTLCacheable`/`@TTLCachePut`，`expire` 为**必填** `long`，`timeUnit` 默认 `MILLISECONDS`，另有 Microsphere 专属的 `cacheManagers` 选择器）、宽松绑定 `@EnableConfigurationBeanBinding`、事件拦截 `@EnableEventExtension`，以及 `@EnableAutoRegistrationBean` / `@ImportOptional` / `@OverrideAnnotationAttributes` / `@EnableSpringConverterAdapter` 等基础能力。隐式开启由 spring.factories 中四个 `ApplicationContextInitializer` 完成，分别对应可监听 Environment（默认 `false`）、可监听自动装配候选解析（默认 `false`）、Bean 生命周期事件（默认 `false`）与自动注册 Bean（默认 `true`）。并行预实例化靠注册监听器 Bean 开启，线程数由 `microsphere.spring.pre-instantiation.singletons.threads` 控制（默认 `max(1, availableProcessors)`，`< 1` 关闭）。

本版本线两点限制值得重申：Redis TTL 支持在源码中整体被注释；`@Idempotent` 与 `RestControlMapping` 在 0.2.x 中不存在。
