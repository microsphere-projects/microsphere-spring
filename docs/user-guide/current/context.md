# microsphere-spring-context · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/context.md) · [Developer guide](../../developer-guide/current/context.md) · [1.x docs](../1.x/context.md)

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-context` |
| Branch | `release` (the 0.2.x line) |
| Version | `0.2.39` released / `0.2.40-SNAPSHOT` under development |
| Java | 17 |
| Spring Framework range | **6.0.x – 7.0.x** (regression-tested on 6.0.23 / 6.1.21 / 6.2.18 / 7.0.9) |
| Required dependency | `microsphere-java-core` 0.3.19 |
| Optional dependencies | `spring-context`, `spring-context-support`, `spring-beans`, `spring-aop`, `spring-context-indexer`, `javax.annotation-api`, `jackson-annotations`, `jackson-databind` 2.22.2, `snakeyaml` 2.6 |

Because every Spring artifact here is `optional`, one binary works across the whole 6.0–7.0 range. Jackson is needed only by `@JsonPropertySource`, SnakeYAML only by `@YamlPropertySource`.

Features activate in two ways: an annotation carries its own `@Import(...Loader/Registrar)` (explicit opt-in), or an `ApplicationContextInitializer` registered in `META-INF/spring.factories` swaps in behaviour gated by a config property (most default to `false`).

```properties
# META-INF/spring.factories (actual content of this module)
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.spring.context.annotation.AutoRegistrationBeanInitializer,\
io.microsphere.spring.context.event.EventPublishingBeanInitializer,\
io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer,\
io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer
```

---

## 1. `@ResourcePropertySource`: enhanced property sources

Spring's `@PropertySource` cannot expand wildcard patterns, cannot position a source precisely, and never reloads on file changes. `@ResourcePropertySource` (package `io.microsphere.spring.config.context.annotation`) imports `ResourcePropertySourceLoader`, which resolves the resources at startup and inserts the resulting `PropertySource` into `ConfigurableEnvironment#getPropertySources()`; `autoRefreshed = true` registers a file watcher that re-reads changed files. The annotation is `@Target(TYPE) @Inherited @Repeatable` (container annotation `@ResourcePropertySources`).

| Attribute | Type | Default | Description |
|---|---|---|---|
| `value` | `String[]` | `{}` | Resource locations; `classpath*:` wildcards supported |
| `name` | `String` | `""` | Property source name; auto-generated when empty |
| `autoRefreshed` | `boolean` | `false` | Reload when matched files change |
| `first` | `boolean` | `false` | Insert at the front |
| `before` | `String` | `""` | Insert before the named source |
| `after` | `String` | `""` | Insert after the named source |
| `resourceComparator` | `Class<? extends Comparator<Resource>>` | `DefaultResourceComparator.class` | Ordering when a pattern matches many resources |
| `ignoreResourceNotFound` | `boolean` | `false` | Skip missing resources instead of failing |
| `encoding` | `String` | `"${file.encoding:UTF-8}"` | Encoding; placeholder-supported |
| `factory` | `Class<? extends PropertySourceFactory>` | `DefaultPropertySourceFactory.class` | Custom parsing factory |

All attributes live on the meta-annotation `@PropertySourceExtension` (`@Target(ANNOTATION_TYPE)`), so custom annotations reuse the same semantics via `@AliasFor(annotation = PropertySourceExtension.class)`.

```java
@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/META-INF/config/*.properties",
        autoRefreshed = true
)
public class AppConfig {
}

// Outrank systemEnvironment
@Configuration
@ResourcePropertySource(value = "classpath:/config/override.properties", before = "systemEnvironment")
public class OverrideConfig {
}

// Aggregation across jars, missing files tolerated
@Configuration
@ResourcePropertySource(value = "classpath*:/optional/*.properties", ignoreResourceNotFound = true)
public class LenientConfig {
}
```

---

## 2. `@YamlPropertySource` / `@JsonPropertySource`

In `io.microsphere.spring.config.env.annotation`, these are derived from `@ResourcePropertySource` with `factory` pinned to `YamlPropertySourceFactory` / `JsonPropertySourceFactory`. They therefore expose every attribute except `factory` (`autoRefreshed`, `first`, `before`, `after`, `resourceComparator`, `ignoreResourceNotFound`) and pin `encoding` to `"UTF-8"`. YAML becomes an `ImmutableMapPropertySource` whose nested keys are flattened to `a.b.c`; JSON parsing needs Jackson on the classpath.

```java
@Configuration
@YamlPropertySource(value = "classpath:/config/application.yaml", autoRefreshed = true)
@JsonPropertySource("classpath:/config/feature-flags.json")
public class AppConfig {
}
```

```yaml
# classpath:/config/application.yaml → properties server.port / datasource.url
server:
  port: 9090
datasource:
  url: jdbc:h2:mem:demo
```

---

## 3. `@DefaultPropertiesPropertySource`

Writes into the lowest-precedence `defaultProperties` source, so a library can ship defaults without overriding user configuration. Attributes: `properties` (`String[]` of `key=value`), `value`/`locations` (aliased to each other, resource locations), `resourceComparator`, `ignoreResourceNotFound`, `encoding` (`"UTF-8"`), `factory`. Repeatable via `DefaultPropertiesPropertySources`.

```java
@Configuration
@DefaultPropertiesPropertySource(properties = "microsphere.web.enabled=true")
@DefaultPropertiesPropertySource(value = "classpath*:/META-INF/defaults/*.properties")
public class LibDefaultsConfig {
}
```

---

## 4. TTL caching

`@EnableTTLCaching` (`io.microsphere.spring.cache.annotation`) equals `@EnableCaching` plus a `TTLCacheResolver` bean named `ttlCacheResolver`. `@TTLCacheable` / `@TTLCachePut` derive from `@Cacheable` / `@CachePut` with that resolver pinned; at execution the aspect pushes the annotation's `expire`/`timeUnit` into the `TTLContext` thread-local, where a TTL-aware cache implementation is expected to read it.

> ⚠️ On 0.2.x **the TTL is never consumed by the library itself**. The only code that reads `TTLContext` — `io.microsphere.spring.cache.redis.TTLRedisCacheWriterWrapper` and `TTLRedisConfiguration` — is entirely commented out in source, so `TTLCacheResolver` only writes the thread-local (`TTLCacheResolver.java:121`). With the stock Spring cache managers, `@TTLCacheable` therefore behaves exactly like `@Cacheable`: `expire` is silently ignored. To make it work you must plug in your own `Cache`/`CacheWriter` that calls `TTLContext.getTTL()` (or `doWithTTL`) while writing. The attribute is also named `expire`, not `ttl`.

`@EnableTTLCaching` attributes (all `@AliasFor` to `@EnableCaching`): `proxyTargetClass` (`boolean`, `false`), `mode` (`AdviceMode`, `PROXY`), `order` (`int`, `LOWEST_PRECEDENCE`).

`@TTLCacheable` (`@Target({TYPE, METHOD}) @Inherited`):

| Attribute | Type | Default | Description |
|---|---|---|---|
| `expire` | `long` | **required** | Time-to-live length |
| `timeUnit` | `TimeUnit` | `MILLISECONDS` | Unit of `expire` |
| `cacheManagers` | `String[]` | `{}` | Microsphere-specific: CacheManager bean names; empty means all |
| `cacheNames` / `value` | `String[]` | `{}` | Cache names |
| `key` | `String` | `""` | SpEL |
| `keyGenerator` | `String` | `""` | Bean name |
| `condition` / `unless` | `String` | `""` | SpEL |
| `sync` | `boolean` | `false` | As `@Cacheable` |

`@TTLCachePut` has the same attributes but **no** `sync`; there is no TTL variant of `@CacheEvict` — use the native annotation. Note the resolver package is spelled `io.microsphere.spring.cache.intereptor` (historical typo; import the real package name).

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

Programmatic control: `TTLContext.doWithTTL(duration -> cache.put(key, value), Duration.ofSeconds(30));` (also `setTTL` / `getTTL` / `clearTTL`). Proxy semantics match Spring Cache — self-invocation bypasses caching.

---

## 5. `@EnableConfigurationBeanBinding`: relaxed binding

`@Import(ConfigurationBeanBindingRegistrar.class)` registers `configurationBeanBindingPostProcessor`, which binds all properties under `prefix` onto the target type and exposes it as a bean. Alias strategies are loaded with `SpringFactoriesLoader` (`ConfigurationBeanBindingRegistrar.java:140`), and implementations exist for `Default` / `Hyphen` / `UnderScoreJoin`, making `user-name`, `userName` and `user_name` equivalent. `ConfigurationBeanCustomizer` allows post-binding customization of the registered bean. Repeatable via `@EnableConfigurationBeanBindings`.

> ⚠️ This module ships **no** `META-INF/spring.factories` entry for `ConfigurationBeanAliasGenerator` in `src/main/resources` — the four generators are declared only under `src/test`. Out of the box, therefore, no alias generator is active and only exact property names bind. To get hyphen/underscore equivalence, declare the generators in your own `META-INF/spring.factories`:
>
> ```properties
> io.microsphere.spring.beans.factory.support.ConfigurationBeanAliasGenerator=\
> io.microsphere.spring.beans.factory.support.DefaultConfigurationBeanAliasGenerator,\
> io.microsphere.spring.beans.factory.support.HyphenAliasGenerator,\
> io.microsphere.spring.beans.factory.support.UnderScoreJoinAliasGenerator
> ```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `prefix` | `String` | **required** | Property prefix |
| `type` | `Class<?>` | **required** | Binding target type |
| `multiple` | `boolean` | `false` | Bind a group of beans (`prefix.<name>.*`) |
| `ignoreUnknownFields` | `boolean` | `true` | Tolerate unknown fields |
| `ignoreInvalidFields` | `boolean` | `true` | Tolerate type mismatches |

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

This produces `User` beans named `a` and `b`, injectable via `Map<String, User>` or `@Qualifier("a")`. For a single object use `multiple = false` with `user.name` keys.

---

## 6. Listenable `Environment`

Intercepts property reads, placeholder resolution, profile activation and property-source changes for configuration auditing and dynamic-refresh troubleshooting. `io.microsphere.spring.core.env.EnvironmentListener` extends `ProfileListener` and `PropertyResolverListener`; every callback is a no-op `default` method whose first argument is the `ConfigurablePropertyResolver`:

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
    // plus before/after callbacks for placeholder prefixes/suffixes, value separators,
    // ConversionService and required-property checks
}
```

**There is no `@EnableListenableEnvironment` annotation** — `ListenableConfigurableEnvironmentInitializer` (registered in `spring.factories`) swaps the `ConfigurableEnvironment` for a `ListenableConfigurableEnvironment` early in startup, but only when enabled by property. Listeners are collected from Spring beans and from the spring.factories keys `io.microsphere.spring.core.env.EnvironmentListener` (also `.ProfileListener`, `.PropertyResolverListener`).

```properties
microsphere.spring.listenable-environment.enabled=true      # default: false
```

⚠ If you register the initializer yourself instead of relying on `spring.factories`, use the real class name
`io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer`. The Javadoc example inside that class
prints `io.microsphere.spring.beans.factory.support.ListenableConfigurableEnvironmentInitializer`, which does not exist
in the reactor — copying it produces a `ClassNotFoundException` at startup.

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

The same pattern applies to autowire-candidate resolution: `ListenableAutowireCandidateResolverInitializer`, property `microsphere.spring.listenable-autowire-candidate-resolver.enabled` (default `false`), listener type `AutowireCandidateResolvingListener`.

---

## 7. Parallel pre-instantiation of singleton beans

`ParallelPreInstantiationSingletonsBeanFactoryListener` (`io.microsphere.spring.context.event`) hooks `onBeanFactoryConfigurationFrozen`, resolves the dependency graph with `DefaultBeanDependencyResolver`, then pre-instantiates independent non-lazy singletons concurrently to shorten cold start. **There is no enabling annotation** — register the listener as a bean; `BeanFactoryListeners` collects all `BeanFactoryListener` beans and spring.factories implementations.

```java
@Configuration
public class StartupConfig {
    @Bean
    public ParallelPreInstantiationSingletonsBeanFactoryListener parallelPreInstantiationListener() {
        return new ParallelPreInstantiationSingletonsBeanFactoryListener();
    }
}
```

| Property | Type | Default | Description |
|---|---|---|---|
| `microsphere.spring.pre-instantiation.singletons.threads` | `int` | `max(1, availableProcessors)` | Thread count; values `< 1` disable the feature |
| `microsphere.spring.pre-instantiation.singletons.thread.name-prefix` | `String` | `Parallel-Pre-Instantiation-Singletons-Thread-` | Thread name prefix |

Caveats: bean creation becomes concurrent, so beans that depend on initialization ordering or read thread-locals (`SecurityContext`, `RequestAttributes`) inside `@PostConstruct` are not good candidates. Startup logs a two-phase `StopWatch` report to help you measure the win.

---

## 8. `@EnableEventExtension`: event interception

Imports `EventExtensionRegistrar`, which wraps event publication and listener invocation in chain-of-responsibility interceptors. Attributes: `intercepted` (`boolean`, `true`), `executorForListener` (`String`, sentinel `"N/E"` means no async executor; otherwise an `Executor` bean name), `sources` (`BeanSource[]`, default `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}`).

```java
public interface ApplicationEventInterceptor extends Ordered {          // default order = LOWEST_PRECEDENCE
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
        chain.intercept(event, eventType);      // must continue the chain
        log.info("event {} dispatched in {} ns", eventType, System.nanoTime() - start);
    }
}
```

---

## 9. Other features and utilities

| Name | Description |
|---|---|
| `@EnableAutoRegistrationBean` | No attributes. Registers classes implementing `AutoRegistrationBean`; global toggle `microsphere.spring.beans.auto-registered` (default `true`), per bean via `microsphere.spring.beans.<beanName>.auto-registered=false` |
| `@ImportOptional` | Imports configuration classes by class-name string; silently skipped when the class is absent |
| `@OverrideAnnotationAttributes` | Overrides attribute values inherited from a meta-annotation (how `@EnableWebMvcExtension` writes back to `@EnableWebExtension`) |
| `@EnableSpringConverterAdapter` | No attributes. Adapts Spring's `ConversionService` to the Microsphere conversion layer |
| `EnvironmentEnabled` | Interface; implementors gain a `microsphere.spring.<FQCN>.enabled` self-disable switch (default `true`) |
| `spring:` URL | `SpringProtocolURLStreamHandler` supports `spring:env:property-sources://...`, used by the JDBC module to read environment properties |
| `EventPublishingBean*` | Bean lifecycle events; toggle `microsphere.spring.event-publishing-bean.enabled` (default `false`) |
| Utilities | `BeanFactoryUtils`, `BeanDefinitionUtils`, `AnnotatedBeanDefinitionRegistryUtils`, `ApplicationContextUtils`, `EnvironmentUtils`, `PropertySourcesUtils`, `PropertyResolverUtils`, `ResourceLoaderUtils`, `ResourceUtils`, `PropertiesUtils`, `AnnotationUtils`, `ConversionServiceUtils`, `BeanRegistrar`, `DependencyTreeWalker`, `SpringVersion`/`SpringVersionUtils` |
| `FilterMode` | Extension filtering mode; property `microsphere.spring.filter-mode` (default `SEQUENTIAL`). ⚠ Inert in 0.2.x: no production code reads the property, so setting it changes nothing |

---

## Summary

`microsphere-spring-context` (0.2.39 released / `0.2.40-SNAPSHOT` in development, Java 17, Spring Framework **6.0.x–7.0.x**) is the foundation module. Its only compile dependency is `microsphere-java-core` 0.3.19; every Spring artifact plus Jackson and SnakeYAML are optional, which is exactly why one binary works across the whole 6.0–7.0 range.

Explicit opt-in annotations import their own registrars: the property-source family (`@ResourcePropertySource`, `@YamlPropertySource`, `@JsonPropertySource`, `@DefaultPropertiesPropertySource`), the TTL caching family (`@EnableTTLCaching` + `@TTLCacheable`/`@TTLCachePut`, where `expire` is a **required** `long`), `@EnableConfigurationBeanBinding`, `@EnableEventExtension`, and the infrastructure annotations (`@EnableAutoRegistrationBean`, `@ImportOptional`, `@OverrideAnnotationAttributes`, `@EnableSpringConverterAdapter`). Implicit, property-gated activation comes from the four `ApplicationContextInitializer`s in `META-INF/spring.factories`. Parallel startup is opt-in by bean registration rather than by annotation.

Two limitations on this line are worth repeating: Redis-backed TTL is commented out in source, and `@Idempotent` / `RestControlMapping` do not exist here.
