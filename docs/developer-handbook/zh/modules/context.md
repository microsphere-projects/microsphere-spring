[← 模块开发指南](./README.md)

# microsphere-spring-context

核心模块：其他所有模块都依赖它。基础包 `io.microsphere.spring`，构件
`io.github.microsphere-projects:microsphere-spring-context`。

## 1. 增强属性源

### `@ResourcePropertySource`

在 Spring 的 `@PropertySource` 之上扩展出通配符定位、显式排序、跨配置类继承以及热重载能力。
`@Repeatable(@ResourcePropertySources)`、`@Import(ResourcePropertySourceLoader)`，并且可以用作元注解。

| 属性 | 默认值 | 含义 |
|-----------|---------|---------|
| `value()` / `name()` | `{}` / `""` | 资源定位路径（支持 `classpath*:` 通配符）/ 属性源名称 |
| `autoRefreshed()` | `false` | 匹配到的文件发生变更时重新加载该属性源 |
| `first()` | `false` | 插入到 `PropertySources` 列表的头部 |
| `before()` / `after()` | `""` | 紧挨在指定名称的属性源之前/之后插入 |
| `resourceComparator()` | `DefaultResourceComparator.class` | 为通配符匹配结果提供确定的排序 |
| `ignoreResourceNotFound()` | `false` | 容忍资源缺失 |
| `encoding()` | `${file.encoding:UTF-8}` | 资源编码 |
| `factory()` | `DefaultPropertySourceFactory.class` | 可插拔的 `PropertySourceFactory` |

```java
@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/META-INF/config/*.properties",
        autoRefreshed = true,
        first = true
)
public class AppConfig { }
```

### `@YamlPropertySource` / `@JsonPropertySource`

便捷注解——属性集合完全相同，各自通过元注解
`@ResourcePropertySource(factory = YamlPropertySourceFactory.class / JsonPropertySourceFactory.class)` 实现。

```java
@Configuration
@YamlPropertySource("classpath:/config/application.yaml")
@JsonPropertySource("classpath:/config/feature-flags.json")
public class AppConfig { }
```

其底层支撑为 `ImmutableMapPropertySource`（不可修改）、`ResourceYamlProcessor`（将多个 YAML 资源合并为一个只读
Map），以及 `DefaultResourceComparator`。

### `@DefaultPropertiesPropertySource`

将内联的 `properties()`（`key=value` 键值对）和/或 `locations()` 合并进 `defaultProperties` 属性源——它是优先级最
低的属性源，非常适合承载库自带的默认值。可通过 `@DefaultPropertiesPropertySources` 重复使用。
属性包括：`properties()`、`value()/locations()`（`@AliasFor` 成对属性）、`resourceComparator()`、
`ignoreResourceNotFound()`、`encoding()`（`UTF-8`）、`factory()`。

```java
@Configuration
@DefaultPropertiesPropertySource(properties = {
        "microsphere.webmvc.enabled=true",
        "app.banner.mode=off"
})
public class DefaultsConfig { }
```

### 构建你自己的属性源注解

用 `@PropertySourceExtension`（一个 `@Target(ANNOTATION_TYPE)` 模板，携带与 `@ResourcePropertySource`
完全相同的属性）对自定义注解做元注解，并继承 `PropertySourceExtensionLoader<A, EA>` 与
`PropertySourceExtensionAttributes<A>`。如果你不需要扩展属性，`AnnotatedPropertySourceLoader<A>` 是更简单的基类。

### 变更事件

- `PropertySourceChangedEvent`（一个 `ApplicationContextEvent`）——在新增/替换/移除单个属性源时触发；
  静态工厂方法 `added(...)`、`replaced(...)`、`removed(...)`；`getKind()`、`getNewPropertySource()`、
  `getOldPropertySource()`。
- `PropertySourcesChangedEvent`——聚合事件；`getChangedProperties()`、`getAddedProperties()`、`getRemovedProperties()`。

## 2. 可监听的 `Environment`

对应用 `Environment` 进行包装，使属性解析与 Profile 访问变为可观察的行为。

- 由 `ListenableConfigurableEnvironmentInitializer` 自动安装（在 `META-INF/spring.factories`
  中注册为 `ApplicationContextInitializer`）。它将上下文 environment 替换为
  `ListenableConfigurableEnvironment`，这是一个委托式的 `ConfigurableEnvironment`，会触发前置/后置回调。
- 监听器 SPI（全部方法均带有 `default` 空实现——按需实现即可）：
  - `PropertyResolverListener`——针对 `getProperty`、必需属性访问、`resolvePlaceholders`、
    转换服务访问、占位符前缀/后缀/转义字符变更、值分隔符、必需属性校验等的前置/后置回调。
  - `ProfileListener`——针对获取与设置 active 及 default profile 的前置/后置回调。
  - `EnvironmentListener`——合并上述两者，并额外提供 `getPropertySources`、`getSystemProperties`、
    `getSystemEnvironment`、`merge`。
- `LoggingEnvironmentListener`——追踪日志实现，适合用于诊断某个取值究竟来自哪里。
- 相关能力：`ListenableAutowireCandidateResolver` 及其初始化器，通过
  `microsphere.spring.listenable-autowire-candidate-resolver.enabled=true` 开启；
  `AutowireCandidateResolvingListener` 可观察推荐值（suggested value）与懒加载代理解析。
- 工具类：`EnvironmentUtils`、`PropertySourcesUtils`（`getPropertySource`、`getSubProperties(prefix)`、
  `findPropertyNamesByPrefix`、`DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME`）、`PropertyResolverUtils`。

```java
@Component
public class PropertyAudit implements EnvironmentListener {
    @Override
    public void afterGetProperty(Environment environment, String name, Object value) {
        // 记录应用实际读取了哪些 key
    }
}
```

## 3. 配置属性收集

- `ConfigurationPropertyRepository`——已解析 `ConfigurationProperty` 实例的有界缓存
  （`microsphere.spring.configuration-property.repository.max-size`）。
- `CollectingConfigurationPropertyListener`——同时实现 `PropertyResolverListener` 与
  `AutowireCandidateResolvingListener`，为上述仓库提供数据。配合可监听 environment，你可以得到应用在运行期实际消费
  的配置清单——这对配置审计与迁移工具非常有用。

## 4. 配置 Bean 绑定

`@EnableConfigurationBeanBinding(prefix = "...", type = MyProperties.class)` 依据某个前缀下的属性绑定出一个
POJO，并将其注册为 Bean——相较 Boot 的 `@ConfigurationProperties`，这是一种更轻量、风格更早期的方案。

| 属性 | 默认值 | 含义 |
|-----------|---------|---------|
| `prefix()` | 必填 | 用于绑定的属性前缀 |
| `type()` | 必填 | 需要绑定并注册的 POJO 类 |
| `multiple()` | `false` | 绑定多个 Bean（每个子前缀一个），而不是只绑定单个 |
| `ignoreUnknownFields()` | `true` | 容忍未知的属性名 |
| `ignoreInvalidFields()` | `true` | 容忍无法转换的值 |

可通过 `@EnableConfigurationBeanBindings` 重复使用。启用链路：
`@Import(ConfigurationBeanBindingRegistrar)` → 注册 Bean 定义以及基础设施 Bean
`configurationBeanBindingPostProcessor`（`ConfigurationBeanBindingPostProcessor`），后者在初始化之前依据
`configurationProperties` 属性执行绑定。

扩展点：

- `ConfigurationBeanBinder` / `DefaultConfigurationBeanBinder`——可替换绑定引擎（默认使用 Spring 的 `Binder`）。
- `ConfigurationBeanCustomizer extends Ordered`——`customize(beanName, configurationBean)` 后置处理钩子；
  上下文中所有此类 Bean 都会被依次应用。
- `ConfigurationBeanAliasGenerator` 以及 `Default…`/`Join…`/`Hyphen…`/`UnderScoreJoin…` 实现——为绑定后的
  Bean 提供别名命名策略。

## 5. TTL 缓存

为任意由 Spring 管理的缓存增加条目级存活时间（time-to-live）。

- `@EnableTTLCaching`——即 `@EnableCaching` 加上 `@Import(TTLCachingConfiguration)`，后者注册
  `ttlCacheResolver` Bean（`TTLCacheResolver`）。属性与 `@EnableCaching` 一致：`proxyTargetClass()=false`、
  `mode()=PROXY`、`order()=LOWEST_PRECEDENCE`。
- `@TTLCacheable`——`@Cacheable` 的变体，**TTL 为必填项**，通过元注解
  `@Cacheable(cacheResolver = "ttlCacheResolver")` 实现。属性包括：`value()/cacheNames()`、`key()`、`keyGenerator()`、
  `condition()`、`unless()`、`sync()`、`cacheManagers()`（目标 `CacheManager` 的 Bean 名称）、**`expire()`
  （必填 `long`）**、`timeUnit()`（默认 `MILLISECONDS`）。
  > 注意：该属性名是 `expire`，而不是 `ttl`——根 README 中的 `ttl = 300` 示例已经过时。
- `@TTLCachePut`——对应 `@CachePut` 的同形态注解（没有 `sync`）。
- `TTLContext`——在解析缓存期间使用的线程本地 TTL 载体：静态方法
  `doWithTTL(Consumer|Function, defaultTTL)`、`setTTL`、`getTTL`、`clearTTL`。编程式设置 TTL：

```java
TTLContext.doWithTTL(ttl -> cache.put(key, value), 60_000L);
```

- Redis 支持（`cache.redis.TTLRedisCacheWriterWrapper`、`TTLRedisConfiguration`）在源码中存在，但
  **整体被注释掉了**——请视为当前不可用。该包名还拼写错误为
  `cache.intereptor`，预期后续会重命名。

## 6. Bean 依赖图、注入与并行实例化

- `BeanDependencyResolver`——`resolve(ConfigurableListableBeanFactory): Map<String, Set<String>>`，以及针对单个
  Bean 的 `resolve(beanName, mergedBD, beanFactory)`；默认实现为 `DefaultBeanDependencyResolver`。
- `InjectionPointDependencyResolver`——从 `Field`/`Method`/`Constructor`/`Parameter` 注入点收集依赖的 SPI；
  实现类注册在 `META-INF/spring.factories` 中：
  `ConstructionInjectionPointDependencyResolver`、`AutowiredInjectionPointDependencyResolver`、
  `ResourceInjectionPointDependencyResolver`，此外还有用于自定义注解的 `AnnotatedInjectionPointDependencyResolver`。
- `Dependency` / `DependencyTreeWalker`——用于启动分析的图模型与遍历。
- `AnnotatedInjectionBeanPostProcessor`——为**你自己的**注解类型提供通用的 `@Inject` 风格注入；将其声明为一个
  `@Bean` 并传入需要支持的注解类即可生效（`@EnableGuice` 正是这样实现的）。
- `ParallelPreInstantiationSingletonsBeanFactoryListener`——一个 `BeanFactoryListener`，在 Bean 工厂冻结之后并发
  地预实例化彼此独立的单例。需要在配置类中将其注册为 Bean；可通过
  `microsphere.spring.pre-instantiation.singletons.threads` 与 `.thread.name-prefix` 调优。
- `DelegatingFactoryBean`——把一个已构建好的实例以完整 Spring Bean 的形式暴露出来（具备生命周期与 Aware 回调）。
- `AutoRegistrationBean` + `@EnableAutoRegistrationBean`——自描述 Bean（`isAutoRegistered(env)`、
  `getBeanName()`、`getBeanType()`、`getScope()`、`customize(BeanDefinitionBuilder)`、`getOrder()`、
  `getDescription()`），从 `spring.factories` 加载并按需注册。总开关：
  `microsphere.spring.beans.auto-registered=false` 控制全局，`microsphere.spring.beans.<beanName>.auto-registered`
  控制单个 Bean。
- 支撑工具：`BeanRegistrar`（静态注册辅助方法）、`BeanDefinitionUtils`、
  `GenericBeanPostProcessorAdapter<T>`、`GenericBeanNameGenerator`、`NamedBeanHolderComparator`、
  `ResolvableDependencyTypeFilter`、`BeanUtils`、`PropertyValuesUtils`、`BeanFactoryUtils`。

## 7. 事件与监听器扩展

`@EnableEventExtension`（`@OverrideAnnotationAttributes` + `@Import(EventExtensionRegistrar)`）：

| 属性 | 默认值 | 含义 |
|-----------|---------|---------|
| `intercepted()` | `true` | 用 `InterceptingApplicationEventMulticaster` 替换 `applicationEventMulticaster` |
| `executorForListener()` | `"N/E"`（无） | 用于异步调用监听器的 `Executor` Bean 名称 |
| `sources()` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 拦截器的收集来源 |

- 拦截 SPI：`ApplicationEventInterceptor` + `ApplicationEventInterceptorChain`（环绕事件多播），
  `ApplicationListenerInterceptor` + `ApplicationListenerInterceptorChain`（环绕每一次监听器调用）；
  支持 `Ordered`，并提供了默认的 `Default…Chain` 实现。
- Bean 生命周期观察：`BeanListener`——`supports(beanName)`、`onBeanDefinitionReady`、
  `onBeforeBeanInstantiate`（通用 / 构造器 / 工厂方法等多种重载）、`onAfterBeanInstantiated`、
  `onBeanPropertyValuesReady`、`onBefore/onAfterBeanInitialize`、`onBeanReady`、`onBefore/onAfterBeanDestroy`；
  `BeanListenerAdapter` 提供空实现默认值。`BeanFactoryListener`——
  `onBeanDefinitionRegistryReady`、`onBeanFactoryReady`、`onBeanFactoryConfigurationFrozen`
  （另有 `BeanFactoryListenerAdapter`）。这些事件由 `EventPublishingBeanInitializer`（spring.factories）发布。
- 现成监听器：`BeanTimeStatistics`（借助 `StopWatch` 统计每个 Bean 的实例化/初始化耗时）、
  `LoggingBeanFactoryListener`、`LoggingBeanListener`、`DependencyAnalysisBeanFactoryListener`、
  `OnceApplicationContextEventListener<T>`（每类上下文事件只触发一次——Web 侧织入器所用的基类）。
- `GenericApplicationListenerAdapter`——将 `GenericApplicationListener` 与 `SmartApplicationListener` 合并并提供
  默认实现。JavaBeans 桥接：`BeanPropertyChangedEvent`、`JavaBeansPropertyChangeListenerAdapter`。
- 基类：`AbstractSmartLifecycle` / `LoggingSmartLifecycle`、
  `ConfigurableApplicationContextInitializer`（为每个初始化器提供开关
  `microsphere.spring.context-initializer.<beanName>.enabled`）。

## 8. Annotation-Import 框架

本代码库中每一个 `@Enable*` 背后的工具箱：

- `BeanCapableImportCandidate`——抽象的 `ImportSelector`/`ImportBeanDefinitionRegistrar`，内置 `BeanFactory`、
  `Environment` 与 Aware 支持，并提供属性覆盖钩子。
- `AnnotatedBeanCapableImportCandidate<A>`、`AnnotatedBeanCapableImportSelector<A>`、
  `AnnotatedBeanCapableImportBeanDefinitionRegistrar<A>`——带注解类型参数的基类；通过
  `microsphere.spring.<class>@<annotation>.enabled` 或 `microsphere.spring.<annotation>.enabled` 启用/禁用。
- `ImportOptional`（`@Import(ImportOptionalSelector)`）——按**类名**导入，自动静默跳过不在 classpath 上的类。
- `OverrideAnnotationAttributes` + `OverrideAnnotationAttributesStrategy`
  （默认 `ConfigurationPropertyOverrideAnnotationAttributesStrategy`）——依据 Environment 中的
  `@ConfigurationProperty` 名称覆盖注解属性；这使派生的 `@Enable*` 注解具备可组合性。
- `AnnotationUtils`（跨元注解进行查找/获取/合并，并支持属性覆盖）、
  `GenericAnnotationAttributes<A>`（类型化属性）、`ResolvablePlaceholderAnnotationAttributes<A>`
  （针对 Environment 解析 `${...}`）。
- `EnvironmentEnabled`——通过属性选择加入/退出；`isEnabled(Environment)`、`getEnabledPropertyName()`
  （默认 `microsphere.spring.<class>.enabled`）、`getDefaultEnabled()`（默认 `true`）。
- 扫描支持：`AnnotatedBeanDefinitionRegistryUtils`、`ExposingClassPathBeanDefinitionScanner`。

## 9. 转换器与 `spring:` URL 协议

- `SpringConverterAdapter`——一个 `ConditionalGenericConverter`，把 Microsphere 的
  `io.microsphere.convert.Converter` 暴露给 Spring 的 `ConversionService`；通过
  `@EnableSpringConverterAdapter` 启用。配套组件：
  `ConversionServiceResolver`（Bean `conversionService` / `resolved-conversionService`）、`ConversionServiceUtils`。
- `SpringProtocolURLStreamHandler` 注册了 `spring:` URL 协议（作为基础设施 Bean 初始化）：

| URL 形式 | 工厂 | 用途 |
|----------|---------|---------|
| `spring:resource:...` | `SpringResourceURLConnectionFactory` | 通过 URL 寻址任意 Spring `Resource` |
| `spring:env:profiles://{type}` | `SpringEnvironmentURLConnectionFactory`（`SpringProfilesURLConnectionAdapter`） | 以资源形式读取 active/default profile |
| `spring:env:property-sources://{prefix}/{media-type}` | `SpringPropertySourcesURLConnectionAdapter` | 将匹配的属性以 JSON/Properties 内容导出 |
| 委托子协议 | `SpringSubProtocolURLConnectionFactory`、`SpringDelegatingBeanProtocolURLConnectionFactory` | 以 Bean 形式贡献处理器（`microsphere-spring-jdbc` 的 `p6spy://` 即基于此） |

## 10. 其他杂项

`core.io.ResourceLoaderUtils`、`core.io.ResourceUtils`、`core.io.support.PropertiesUtils`、
`core.io.support.SpringFactoriesLoaderUtils`（整个代码库共用的带缓存 SPI 加载器）、
`core.MethodParameterUtils`、`core.SpringVersion`（枚举已发布的 Spring 版本 ≥ 6.0 并支持比较）、
`util.SpringVersionUtils`（运行期版本检查）、`util.MimeTypeUtils`、`util.FilterMode`
（`SEQUENTIAL`/`CONDITIONAL`，通过 `microsphere.spring.filter-mode` 配置）、`beans.BeanSource`
（`BEAN_FACTORY`/`SPRING_FACTORIES`/`JAVA_SERVICE_PROVIDER` + `registerBeans(...)`）、
`constants.PropertyConstants`。

## 11. `META-INF/spring.factories`（模块资源）

```properties
# InjectionPointDependencyResolver
io.microsphere.spring.beans.factory.InjectionPointDependencyResolver=\
io.microsphere.spring.beans.factory.ConstructionInjectionPointDependencyResolver,\
io.microsphere.spring.beans.factory.annotation.AutowiredInjectionPointDependencyResolver,\
io.microsphere.spring.beans.factory.annotation.ResourceInjectionPointDependencyResolver

# ApplicationContextInitializer
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.spring.context.annotation.AutoRegistrationBeanInitializer,\
io.microsphere.spring.context.event.EventPublishingBeanInitializer,\
io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer,\
io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer
```

## 12. 注意事项

- 大多数 `*Registrar`/`*Loader` 类是刻意设为包内私有的——只做注解，不要直接导入它们。
- `@TTLCacheable` 要求提供 `expire`；不存在全局默认 TTL。
- TTL 的 Redis 支持处于禁用状态（已被注释掉）。
- 并行预实例化并没有对应的 `@Enable*`——你必须自行注册该监听器 Bean，并且在开启之前，应当审查那些排序假设未被满足
  的 Bean。

---
[← 模块开发指南](./README.md)
