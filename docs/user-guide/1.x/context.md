# microsphere-spring-context (1.x)

[← 1.x overview](./README.md) · [简体中文](./zh/context.md) · [Developer guide](../../developer-guide/1.x/context.md) · [0.2.x docs](../current/context.md)

## Version Information

| Item | Value |
|------|-----|
| groupId | `io.github.microsphere-projects` |
| artifactId | `microsphere-spring-context` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version under development | `0.1.40-SNAPSHOT` |
| Java version | **8** (`maven.compiler.source/target = 8`) |
| Spring Framework compatibility range | **4.3.x – 5.3.x** |
| Servlet namespace | `javax.*` (this module does not involve Servlet) |

The Spring / Reactor versions are decided by the Maven profiles of the parent POM (`microsphere-spring-parent`); the profiles of the module itself only add optional dependencies, and `spring-framework-5.3` is active by default:

| Profile | Spring Framework | Reactor BOM | Optional dependencies added by this module |
|---------|------------------|-------------|--------------|
| `spring-framework-4.3` | `4.3.30.RELEASE` | `Bismuth-SR17` | `microsphere-spring-compatible` |
| `spring-framework-5.0` | `5.0.20.RELEASE` | `Bismuth-SR17` | `microsphere-spring-compatible`, `spring-context-indexer` |
| `spring-framework-5.1` | `5.1.20.RELEASE` | `Californium-SR23` | `spring-context-indexer` |
| `spring-framework-5.2` | `5.2.25.RELEASE` | `Dysprosium-SR25` | `spring-context-indexer` |
| `spring-framework-5.3` (default) | `5.3.39` | `2020.0.47` | `spring-context-indexer` |

> Note: the `spring-framework-4.3` and `spring-framework-5.0` profiles need `microsphere-spring-compatible` to supply the utility classes and annotations that only exist from Spring 5.1 onwards (see [compatible.md](./compatible.md)). When building against these lower Spring versions, make sure that module is on the classpath.

Dependency posture (`microsphere-spring-context/pom.xml`):

| Dependency | Scope | Optional? |
|------------|-------|-----------|
| `microsphere-java-core` | compile | No (required) |
| `spring-context` / `spring-context-support` / `spring-beans` / `spring-aop` | provided | Yes |
| `javax.annotation-api` | provided | Yes |
| `jackson-annotations` / `jackson-databind` | provided | Yes (only the JSON property source needs it) |
| `snakeyaml` | provided | Yes (only the YAML property source needs it) |
| `microsphere-annotation-processor` | provided | Yes (compile time, generates configuration metadata) |

> Because the Spring dependencies are `optional` + `provided`, this module **does not** transitively bring Spring into your application; manage the Spring versions yourself with the `microsphere-spring-dependencies` BOM or `spring-boot-starter`.

How to declare it:

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
    <!-- complete the optional dependencies your features need -->
    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
    </dependency>
</dependencies>
```

---

## 1. `@ResourcePropertySource`: Wildcards, Ordering and Auto-Refresh

Spring's native `@PropertySource` supports no `classpath*:` wildcards, cannot declare the insertion position, and does not reload after the file changes. `@ResourcePropertySource` (package `io.microsphere.spring.config.context.annotation`) solves these three things.

The annotation declaration (`release-1.x` sources):

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

Every attribute is mapped onto the `@PropertySourceExtension` meta-annotation through `@AliasFor(annotation = PropertySourceExtension.class)`, so you can define your own derived annotation.

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `name` | `String` | `""` | Property source name; derived from the resource path when empty |
| `value` | `String[]` | `{}` | Resource locations, `classpath*:` wildcards supported |
| `autoRefreshed` | `boolean` | `false` | Reload when the file changes and publish `PropertySourcesChangedEvent` |
| `first` | `boolean` | `false` | Insert at the **head** of the property-source chain (highest precedence) |
| `before` | `String` | `""` | Insert before the given property source |
| `after` | `String` | `""` | Insert after the given property source |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator.class` | Ordering strategy when the wildcard matches several resources |
| `ignoreResourceNotFound` | `boolean` | `false` | Whether to ignore the case where no resource is matched |
| `encoding` | `String` | `"${file.encoding:UTF-8}"` | Placeholder supported, resolved from the environment at runtime |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory.class` | Factory that creates the property source |

> Note that the default value of `encoding` is a **placeholder string**, `"${file.encoding:UTF-8}"`, which differs from the `"UTF-8"` literal of `@YamlPropertySource`; declare the encoding explicitly if you want it pinned.

```java
@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/META-INF/config/*.properties",
        autoRefreshed = true,   // reload automatically after file changes
        after = "systemProperties"
)
public class AppConfig {

    @Autowired
    private Environment environment;

    @Value("${app.feature.enabled:false}")
    private boolean featureEnabled;
}
```

Listening for the change event (`io.microsphere.spring.config.env.event`):

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

Several declarations with the same name use `@ResourcePropertySources`, the container annotation of `@Repeatable`:

```java
@Configuration
@ResourcePropertySource(name = "a", value = "classpath:/a.properties")
@ResourcePropertySource(name = "b", value = "classpath:/b.properties", first = true)
public class MultiSourceConfig {
}
```

## 2. `@YamlPropertySource` / `@JsonPropertySource`

Both are defined in `io.microsphere.spring.config.env.annotation` and are essentially derived annotations of `@ResourcePropertySource`; they only pin `factory` on the meta-annotation:

```java
@Target(TYPE) @Retention(RUNTIME) @Inherited @Documented
@ResourcePropertySource(factory = YamlPropertySourceFactory.class)
public @interface YamlPropertySource { ... }
```

| Attribute | `@YamlPropertySource` | `@JsonPropertySource` |
|-----------|-----------------------|------------------------|
| Number of attributes | 9 | 9 |
| Missing relative to `@ResourcePropertySource` | `factory` (already pinned on the meta-annotation) | `factory` (already pinned on the meta-annotation) |
| Default value of `encoding` | `"UTF-8"` | `"UTF-8"` |
| Remaining attributes | `name`/`value`/`autoRefreshed`/`first`/`before`/`after`/`resourceComparator`/`ignoreResourceNotFound`, same as the table above | Same as the left column |
| Underlying dependency | `org.yaml:snakeyaml` | `com.fasterxml.jackson.core:jackson-databind` |

```java
@Configuration
@YamlPropertySource("classpath:/config/application.yaml")
@JsonPropertySource(name = "flags", value = "classpath:/config/feature-flags.json")
public class YamlJsonConfig {
}
```

Multi-document YAML is handled by `io.microsphere.spring.config.env.config.ResourceYamlProcessor`; when only the first document has to be read, the behaviour matches the derived usage of Spring's `YamlProcessor`.

## 3. `@DefaultPropertiesPropertySource`

Provides "fallback" default values, placed at the **end** of the property-source chain, so it never overrides external configuration. Defined in `io.microsphere.spring.config.context.annotation`.

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `properties` | `String[]` | `{}` | Inline `key=value` pairs |
| `value` / `locations` | `String[]` | `{}` | Resource locations, `@AliasFor` each other |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator.class` | Ordering of resources |
| `ignoreResourceNotFound` | `boolean` | `false` | Whether to ignore a miss |
| `encoding` | `String` | `"UTF-8"` | A literal, not a placeholder |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory.class` | Factory |

> Compared with `@ResourcePropertySource`, this annotation has **no** `name`/`first`/`before`/`after`/`autoRefreshed` — its role is precisely "fallback at the end of the chain".

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

## 4. TTL Caching: `@EnableTTLCaching` + `@TTLCacheable` / `@TTLCachePut`

Spring's `CacheManager` can only set an expiry policy as a whole; `io.microsphere.spring.cache.annotation` offers TTL **per entry**.

```java
@Target(TYPE) @Retention(RUNTIME) @Documented
@EnableCaching
@Import(TTLCachingConfiguration.class)
public @interface EnableTTLCaching {
    // all three attributes are @AliasFor(EnableCaching.class)
    boolean proxyTargetClass() default false;
    AdviceMode mode() default PROXY;
    int order() default LOWEST_PRECEDENCE;
}
```

`@TTLCacheable` (`@Target({TYPE, METHOD})`) extends `@Cacheable(cacheResolver = BEAN_NAME)`:

| Attribute | Type | Default | Origin |
|-----------|------|---------|--------|
| `expire` | `long` | **Mandatory (no default)** | This annotation |
| `timeUnit` | `TimeUnit` | `MILLISECONDS` | This annotation |
| `cacheManagers` | `String[]` | `{}` | This annotation (restricts the CacheManager beans it applies to, by bean name) |
| `value` / `cacheNames` | `String[]` | `{}` | `@AliasFor(Cacheable.class)` |
| `key` / `keyGenerator` | `String` | `""` | `@AliasFor(Cacheable.class)` |
| `condition` / `unless` | `String` | `""` | `@AliasFor(Cacheable.class)` |
| `sync` | `boolean` | `false` | `@AliasFor(Cacheable.class)` |

`@TTLCachePut` has the same attribute set, only **without `sync`** (`@CachePut` has no such semantics).

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

Key points and limitations:

- `expire` **has no default value**; omitting it fails compilation outright — this is deliberate, to avoid "believing a TTL was set when it was not".
- The TTL context is carried within a single call by `io.microsphere.spring.cache.TTLContext` (a ThreadLocal), and `io.microsphere.spring.cache.intereptor.TTLCacheResolver` (note the package name is spelled `intereptor`) computes the expiry time of an entry from it; the bean name of this resolver is `ttlCacheResolver`, injected in a fixed way through `@Cacheable(cacheResolver = ...)`.
- ⚠️ **Redis TTL is unavailable in 1.x as well**: the sources of `io.microsphere.spring.cache.redis.TTLRedisConfiguration` / `TTLRedisCacheWriterWrapper` are commented out in their entirety, and the module pom has no `spring-data-redis` dependency. Do not assume Redis support from the description in the 0.2.x README.
- The expiry capability you actually get depends on the `Cache` implementation you inject; Spring's `ConcurrentMapCacheManager` does not evict entries itself, and `TTLCacheResolver` achieves "logical expiry" by checking the `TTLContext` timestamp when reading.
- `mode = AdviceMode.ASPECTJ` requires you to bring in AspectJ weaving yourself.

## 5. Relaxed Binding: `@EnableConfigurationBeanBinding`

Binds loosely formatted configuration keys (`server-port` / `serverPort` / `SERVER_PORT`) onto a POJO and registers it as a bean. Defined in `io.microsphere.spring.beans.factory.annotation`:

```java
@Target({TYPE, ANNOTATION_TYPE}) @Retention(RUNTIME) @Documented
@Import(ConfigurationBeanBindingRegistrar.class)
@Repeatable(EnableConfigurationBeanBindings.class)
public @interface EnableConfigurationBeanBinding {
    boolean DEFAULT_MULTIPLE = false;
    boolean DEFAULT_IGNORE_UNKNOWN_FIELDS = true;
    boolean DEFAULT_IGNORE_INVALID_FIELDS = true;

    String prefix();       // required
    Class<?> type();       // required
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
    private ServerConfig serverConfig;   // already bound and injectable
}
```

`application.properties`:

```properties
server.host=127.0.0.1
server.port=9090
```

With `multiple = true`, a collection can be bound under the prefix, and the bean aliases are generated by `ConfigurationBeanAliasGenerator` under `io.microsphere.spring.beans.factory.support`; `DefaultConfigurationBeanAliasGenerator`, `HyphenAliasGenerator`, `UnderScoreJoinAliasGenerator` and `JoinAliasGenerator` provide different naming strategies (the 1.x test `EnableConfigurationBeanBindingTestForAlias` asserts the two alias forms `users-a` and `users_a`).

## 6. Listenable `Environment`

`io.microsphere.spring.core.env` provides `PropertyResolverListener`, which observes the property resolution process and suits the investigation of "which property source did this value actually come from". The real callback signatures (`release-1.x` sources):

```java
public interface PropertyResolverListener {

    default void beforeGetProperty(ConfigurablePropertyResolver resolver, String name,
                                  @Nullable Class<?> targetType, @Nullable Object defaultValue) {}

    default void afterGetProperty(ConfigurablePropertyResolver resolver, String name,
                                  @Nullable Class<?> targetType,
                                  @Nullable Object value, @Nullable Object defaultValue) {}
    // also includes hooks for placeholder resolution, ConversionService, value separators, etc.
}
```

This capability is **disabled by default** and switched on by property through the `ApplicationContextInitializer` entries in `META-INF/spring.factories`:

```properties
# enable the listenable Environment
microsphere.spring.listenable-environment.enabled=true

# enable the listenable AutowireCandidateResolver
microsphere.spring.listenable-autowire-candidate-resolver.enabled=true
```

What `spring.factories` actually registers (1.x):

```
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.spring.context.annotation.AutoRegistrationBeanInitializer,\
io.microsphere.spring.context.event.EventPublishingBeanInitializer,\
io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer,\
io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer
```

> Class-name difference: 1.x uses `ListenableConfigurableEnvironmentInitializer`; the corresponding implementation in 0.2.x goes by another name. Always follow the sources of the branch you are on.

## 7. Parallel Pre-Instantiation of Singletons

`io.microsphere.spring.context.event.ParallelPreInstantiationSingletonsBeanFactoryListener` implements `BeanFactoryListener`: during the `preInstantiateSingletons()` phase it resolves the bean dependency graph and creates mutually independent singletons concurrently in a thread pool, thereby shortening startup time.

| Property | Default | Description |
|----------|---------|-------------|
| `microsphere.spring.pre-instantiation.singletons.threads` | `Math.max(1, availableProcessors)` | Number of threads; set `< 1` to disable parallelism |
| `microsphere.spring.pre-instantiation.singletons.thread.name-prefix` | `Parallel-Pre-Instantiation-Singletons-Thread-` | Thread name prefix |

The listener is not registered automatically; declare it as a bean explicitly:

```java
@Bean
public ParallelPreInstantiationSingletonsBeanFactoryListener parallelPreInstantiationListener() {
    return new ParallelPreInstantiationSingletonsBeanFactoryListener();
}
```

```properties
microsphere.spring.pre-instantiation.singletons.threads=4
```

> Side effect: bean creation order is no longer deterministic, so any logic that depends on initialization order (static holders, reading another bean inside `@PostConstruct`) may break. Verify in a non-production environment first.

## 8. Event Interception: `@EnableEventExtension`

Defined in `io.microsphere.spring.context.event`:

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

| Attribute | Default | Description |
|-----------|---------|-------------|
| `intercepted` | `true` | Whether the `ApplicationEventInterceptor` / `ApplicationListenerInterceptor` chains are enabled |
| `executorForListener` | `"N/E"` (not asynchronous) | The **bean name** of the `Executor` used to run listeners asynchronously |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Where interceptors are discovered; values come from `io.microsphere.spring.beans.BeanSource` |

The interceptor SPI is a chain of responsibility (both `extends Ordered`, defaulting to `LOWEST_PRECEDENCE`):

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

`ApplicationEventInterceptor` targets the event itself:

```java
void intercept(ApplicationEvent event, ResolvableType eventType,
               ApplicationEventInterceptorChain chain);
```

## 9. Remaining Annotations and Switches

| Annotation / Interface | Location | Key points |
|------------------------|----------|------------|
| `@ImportOptional` | `io.microsphere.spring.context.annotation` | `String[] value()` (required); together with `ImportOptionalSelector` it performs a conditional `@Import`, and it can be overridden by the property `microsphere.spring.@ImportOptional.value=` |
| `@OverrideAnnotationAttributes` | Same as above | `@Target(ANNOTATION_TYPE)` only; lets external configuration override annotation attributes, `strategy()` defaults to `ConfigurationPropertyOverrideAnnotationAttributesStrategy.class` |
| `@EnableAutoRegistrationBean` | Same as above | **No attributes**; `AutoRegistrationBeanInitializer` batch-registers the beans declared outside `@Bean`, and each one can be switched off individually with `microsphere.spring.beans.{bean-name}.auto-registered=false` (the constant name is spelled `BEANS_AUTO_REGISTERED_PROEPRTY_NAME`) |
| `@EnableSpringConverterAdapter` | `io.microsphere.spring.core.convert.annotation` | Plugs the `Converter` implementations of `microsphere-java-core` into Spring's `ConversionService` |
| `EnvironmentEnabled` | `io.microsphere.spring.config.env` | Interface contract: property name `microsphere.spring.<fqcn>.enabled`, `getDefaultEnabled()` defaults to **true** |
| `@PropertySourceExtension` | `io.microsphere.spring.config.context.annotation` | The meta-annotation behind the property-source annotations above; use it to build your own derived annotations |

Utility class quick reference:

| Class | Purpose |
|-------|---------|
| `io.microsphere.spring.context.ApplicationContextUtils` | Querying and manipulating the context and its property sources |
| `io.microsphere.spring.beans.factory.BeanFactoryUtils` | Traversing bean definitions / names |
| `io.microsphere.spring.beans.factory.support.BeanDefinitionBuilderUtils` | Fluent construction of `BeanDefinition`s |
| `io.microsphere.spring.beans.factory.ObjectProviderUtils` | Convenient unwrapping of `ObjectProvider` (1.x only) |
| `io.microsphere.spring.beans.factory.DependencyTreeWalker` | Dependency-graph traversal (the machinery behind parallel pre-instantiation) |
| `io.microsphere.spring.context.event.DeferredApplicationEventPublisher` | Publishes events only after the context is ready |
| `io.microsphere.spring.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter` | BPP adapter base class for lower versions (1.x only) |
