[← Module guides](./README.md)

# microsphere-spring-context

The core module: everything else depends on it. Base package `io.microsphere.spring`, artifact
`io.github.microsphere-projects:microsphere-spring-context`.

## 1. Enhanced Property Sources

### `@ResourcePropertySource`

Extends Spring's `@PropertySource` with wildcard locations, explicit ordering, inheritance across configuration
classes, and live reload. `@Repeatable(@ResourcePropertySources)`, `@Import(ResourcePropertySourceLoader)`, and it is
meta-annotation-friendly.

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `value()` / `name()` | `{}` / `""` | resource locations (supports `classpath*:` wildcards) / property-source name |
| `autoRefreshed()` | `false` | reload the source whenever a matched file changes |
| `first()` | `false` | insert at the head of the `PropertySources` list |
| `before()` / `after()` | `""` | insert immediately before/after the named property source |
| `resourceComparator()` | `DefaultResourceComparator.class` | deterministic ordering for wildcard matches |
| `ignoreResourceNotFound()` | `false` | tolerate missing resources |
| `encoding()` | `${file.encoding:UTF-8}` | resource encoding |
| `factory()` | `DefaultPropertySourceFactory.class` | pluggable `PropertySourceFactory` |

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

Convenience annotations — the same attribute set, each meta-annotated
`@ResourcePropertySource(factory = YamlPropertySourceFactory.class / JsonPropertySourceFactory.class)`.

```java
@Configuration
@YamlPropertySource("classpath:/config/application.yaml")
@JsonPropertySource("classpath:/config/feature-flags.json")
public class AppConfig { }
```

Backed by `ImmutableMapPropertySource` (unmodifiable), `ResourceYamlProcessor` (merges multiple YAML resources into
one read-only map), and `DefaultResourceComparator`.

### `@DefaultPropertiesPropertySource`

Merges inline `properties()` (`key=value` pairs) and/or `locations()` into the `defaultProperties` source — the
lowest-precedence source, ideal for library-supplied defaults. Repeatable via `@DefaultPropertiesPropertySources`.
Attributes: `properties()`, `value()/locations()` (`@AliasFor` pair), `resourceComparator()`,
`ignoreResourceNotFound()`, `encoding()` (`UTF-8`), `factory()`.

```java
@Configuration
@DefaultPropertiesPropertySource(properties = {
        "microsphere.webmvc.enabled=true",
        "app.banner.mode=off"
})
public class DefaultsConfig { }
```

### Building your own property-source annotation

Meta-annotate a custom annotation with `@PropertySourceExtension` (a `@Target(ANNOTATION_TYPE)` template carrying the
same attributes as `@ResourcePropertySource`) and extend `PropertySourceExtensionLoader<A, EA>` +
`PropertySourceExtensionAttributes<A>`. `AnnotatedPropertySourceLoader<A>` is the simpler base when you do not need
extension attributes.

### Change events

- `PropertySourceChangedEvent` (an `ApplicationContextEvent`) — fired on add/replace/remove of one source;
  static factories `added(...)`, `replaced(...)`, `removed(...)`; `getKind()`, `getNewPropertySource()`,
  `getOldPropertySource()`.
- `PropertySourcesChangedEvent` — aggregate; `getChangedProperties()`, `getAddedProperties()`, `getRemovedProperties()`.

## 2. Listenable `Environment`

Wraps the application `Environment` so property resolution and profile access become observable.

- Installed automatically by `ListenableConfigurableEnvironmentInitializer` (registered as an
  `ApplicationContextInitializer` in `META-INF/spring.factories`). It replaces the context environment with
  `ListenableConfigurableEnvironment`, a delegating `ConfigurableEnvironment` that fires before/after hooks.
- Listener SPIs (all with `default` no-op methods — implement what you need):
  - `PropertyResolverListener` — before/after for `getProperty`, required-property access, `resolvePlaceholders`,
    conversion service access, placeholder prefix/suffix/escape changes, value separator, required-properties
    validation.
  - `ProfileListener` — before/after for get/set of active and default profiles.
  - `EnvironmentListener` — combines the two above and adds `getPropertySources`, `getSystemProperties`,
    `getSystemEnvironment`, `merge`.
- `LoggingEnvironmentListener` — trace-logging implementation, useful for diagnosing where a value comes from.
- Related: `ListenableAutowireCandidateResolver` + its initializer, enabled with
  `microsphere.spring.listenable-autowire-candidate-resolver.enabled=true`;
  `AutowireCandidateResolvingListener` observes suggested values and lazy-proxy resolution.
- Utilities: `EnvironmentUtils`, `PropertySourcesUtils` (`getPropertySource`, `getSubProperties(prefix)`,
  `findPropertyNamesByPrefix`, `DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME`), `PropertyResolverUtils`.

```java
@Component
public class PropertyAudit implements EnvironmentListener {
    @Override
    public void afterGetProperty(Environment environment, String name, Object value) {
        // record which keys the application actually reads
    }
}
```

## 3. Configuration Property Collection

- `ConfigurationPropertyRepository` — bounded cache of resolved `ConfigurationProperty` instances
  (`microsphere.spring.configuration-property.repository.max-size`).
- `CollectingConfigurationPropertyListener` — implements both `PropertyResolverListener` and
  `AutowireCandidateResolvingListener`, feeding the repository. Together with the listenable environment this gives
  you a runtime inventory of the configuration an application consumes — useful for config auditing and migration
  tooling.

## 4. Configuration Bean Binding

`@EnableConfigurationBeanBinding(prefix = "...", type = MyProperties.class)` binds a POJO from properties under a
prefix and registers it as a bean — a lighter-weight, earlier-style alternative to Boot's `@ConfigurationProperties`.

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `prefix()` | required | property prefix to bind |
| `type()` | required | POJO class to bind and register |
| `multiple()` | `false` | bind multiple beans (one per sub-prefix) instead of a single one |
| `ignoreUnknownFields()` | `true` | tolerate unknown property names |
| `ignoreInvalidFields()` | `true` | tolerate unconvertible values |

Repeatable via `@EnableConfigurationBeanBindings`. Enablement chain:
`@Import(ConfigurationBeanBindingRegistrar)` → registers the bean definition plus the infrastructure bean
`configurationBeanBindingPostProcessor` (`ConfigurationBeanBindingPostProcessor`), which performs binding from the
`configurationProperties` attribute before initialization.

Extension points:

- `ConfigurationBeanBinder` / `DefaultConfigurationBeanBinder` — swap the binding engine (default uses Spring `Binder`).
- `ConfigurationBeanCustomizer extends Ordered` — `customize(beanName, configurationBean)` post-processing hook; all
  such beans in the context are applied.
- `ConfigurationBeanAliasGenerator` + `Default…`/`Join…`/`Hyphen…`/`UnderScoreJoin…` implementations — alias naming
  strategies for bound beans.

## 5. TTL Caching

Adds per-entry time-to-live to any Spring-managed cache.

- `@EnableTTLCaching` — `@EnableCaching` plus `@Import(TTLCachingConfiguration)`, which registers the
  `ttlCacheResolver` bean (`TTLCacheResolver`). Attributes mirror `@EnableCaching`: `proxyTargetClass()=false`,
  `mode()=PROXY`, `order()=LOWEST_PRECEDENCE`.
- `@TTLCacheable` — a `@Cacheable` variant **with a mandatory TTL**, meta-annotated
  `@Cacheable(cacheResolver = "ttlCacheResolver")`. Attributes: `value()/cacheNames()`, `key()`, `keyGenerator()`,
  `condition()`, `unless()`, `sync()`, `cacheManagers()` (bean names of the target `CacheManager`s), **`expire()`
  (required `long`)**, `timeUnit()` (default `MILLISECONDS`).
  > Note: the attribute is `expire`, not `ttl` — the root README's `ttl = 300` snippet is out of date.
- `@TTLCachePut` — same shape for `@CachePut` (minus `sync`).
- `TTLContext` — thread-local TTL carrier used while resolving caches: static
  `doWithTTL(Consumer|Function, defaultTTL)`, `setTTL`, `getTTL`, `clearTTL`. Programmatic TTL:

```java
TTLContext.doWithTTL(ttl -> cache.put(key, value), 60_000L);
```

- Redis support (`cache.redis.TTLRedisCacheWriterWrapper`, `TTLRedisConfiguration`) exists in source but is
  **entirely commented out** — treat as not currently available. The package is also misspelled
  `cache.intereptor`; expect a rename.

## 6. Bean Dependency Graph, Injection and Parallel Instantiation

- `BeanDependencyResolver` — `resolve(ConfigurableListableBeanFactory): Map<String, Set<String>>` and per-bean
  `resolve(beanName, mergedBD, beanFactory)`; default `DefaultBeanDependencyResolver`.
- `InjectionPointDependencyResolver` — SPI collecting dependencies from `Field`/`Method`/`Constructor`/`Parameter`
  injection points; implementations registered in `META-INF/spring.factories`:
  `ConstructionInjectionPointDependencyResolver`, `AutowiredInjectionPointDependencyResolver`,
  `ResourceInjectionPointDependencyResolver`, plus `AnnotatedInjectionPointDependencyResolver` for custom annotations.
- `Dependency` / `DependencyTreeWalker` — graph model and traversal for startup analysis.
- `AnnotatedInjectionBeanPostProcessor` — generic `@Inject`-style injection for **your own** annotation types; declare
  it as a `@Bean` with the annotation classes to activate (this is exactly how `@EnableGuice` works).
- `ParallelPreInstantiationSingletonsBeanFactoryListener` — a `BeanFactoryListener` that pre-instantiates independent
  singletons concurrently once the bean factory is frozen. Register it as a bean in a configuration class; tune with
  `microsphere.spring.pre-instantiation.singletons.threads` and `.thread.name-prefix`.
- `DelegatingFactoryBean` — expose a pre-built instance as a full Spring bean (lifecycle + Aware callbacks).
- `AutoRegistrationBean` + `@EnableAutoRegistrationBean` — self-describing beans (`isAutoRegistered(env)`,
  `getBeanName()`, `getBeanType()`, `getScope()`, `customize(BeanDefinitionBuilder)`, `getOrder()`,
  `getDescription()`) loaded from `spring.factories` and registered on demand. Kill-switches:
  `microsphere.spring.beans.auto-registered=false` globally, `microsphere.spring.beans.<beanName>.auto-registered`
  per bean.
- Support utilities: `BeanRegistrar` (static register helpers), `BeanDefinitionUtils`,
  `GenericBeanPostProcessorAdapter<T>`, `GenericBeanNameGenerator`, `NamedBeanHolderComparator`,
  `ResolvableDependencyTypeFilter`, `BeanUtils`, `PropertyValuesUtils`, `BeanFactoryUtils`.

## 7. Event & Listener Extension

`@EnableEventExtension` (`@OverrideAnnotationAttributes` + `@Import(EventExtensionRegistrar)`):

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `intercepted()` | `true` | replace `applicationEventMulticaster` with `InterceptingApplicationEventMulticaster` |
| `executorForListener()` | `"N/E"` (none) | bean name of an `Executor` for asynchronous listener invocation |
| `sources()` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | where interceptors are collected from |

- Interception SPIs: `ApplicationEventInterceptor` + `ApplicationEventInterceptorChain` (around event multicasting),
  `ApplicationListenerInterceptor` + `ApplicationListenerInterceptorChain` (around each listener call);
  `Ordered` supported, defaults `Default…Chain` implementations provided.
- Bean lifecycle observation: `BeanListener` — `supports(beanName)`, `onBeanDefinitionReady`,
  `onBeforeBeanInstantiate` (general / constructor / factory-method overloads), `onAfterBeanInstantiated`,
  `onBeanPropertyValuesReady`, `onBefore/onAfterBeanInitialize`, `onBeanReady`, `onBefore/onAfterBeanDestroy`;
  `BeanListenerAdapter` gives no-op defaults. `BeanFactoryListener` —
  `onBeanDefinitionRegistryReady`, `onBeanFactoryReady`, `onBeanFactoryConfigurationFrozen`
  (+ `BeanFactoryListenerAdapter`). These are published by `EventPublishingBeanInitializer` (spring.factories).
- Ready-made listeners: `BeanTimeStatistics` (per-bean instantiation/initialization timing via `StopWatch`),
  `LoggingBeanFactoryListener`, `LoggingBeanListener`, `DependencyAnalysisBeanFactoryListener`,
  `OnceApplicationContextEventListener<T>` (fires once per context event — the base used by the web weavers).
- `GenericApplicationListenerAdapter` — combines `GenericApplicationListener` + `SmartApplicationListener` with
  defaults. JavaBeans bridge: `BeanPropertyChangedEvent`, `JavaBeansPropertyChangeListenerAdapter`.
- Base classes: `AbstractSmartLifecycle` / `LoggingSmartLifecycle`,
  `ConfigurableApplicationContextInitializer` (per-initializer switch
  `microsphere.spring.context-initializer.<beanName>.enabled`).

## 8. Annotation-Import Framework

The toolkit behind every `@Enable*` in this codebase:

- `BeanCapableImportCandidate` — abstract `ImportSelector`/`ImportBeanDefinitionRegistrar` with `BeanFactory`,
  `Environment` and Aware support plus attribute-override hooks.
- `AnnotatedBeanCapableImportCandidate<A>`, `AnnotatedBeanCapableImportSelector<A>`,
  `AnnotatedBeanCapableImportBeanDefinitionRegistrar<A>` — annotation-typed bases; enable/disable via
  `microsphere.spring.<class>@<annotation>.enabled` or `microsphere.spring.<annotation>.enabled`.
- `ImportOptional` (`@Import(ImportOptionalSelector)`) — import classes by **name**, silently skipping those absent
  from the classpath.
- `OverrideAnnotationAttributes` + `OverrideAnnotationAttributesStrategy`
  (`ConfigurationPropertyOverrideAnnotationAttributesStrategy` default) — override an annotation's attributes from
  `@ConfigurationProperty` names in the Environment; this makes derived `@Enable*` annotations composable.
- `AnnotationUtils` (find/get/merge across meta-annotations with attribute overriding),
  `GenericAnnotationAttributes<A>` (typed attributes), `ResolvablePlaceholderAnnotationAttributes<A>`
  (resolves `${...}` against the Environment).
- `EnvironmentEnabled` — opt-in/opt-out by property; `isEnabled(Environment)`, `getEnabledPropertyName()`
  (default `microsphere.spring.<class>.enabled`), `getDefaultEnabled()` (default `true`).
- Scanning support: `AnnotatedBeanDefinitionRegistryUtils`, `ExposingClassPathBeanDefinitionScanner`.

## 9. Converters and the `spring:` URL Protocol

- `SpringConverterAdapter` — a `ConditionalGenericConverter` exposing Microsphere `io.microsphere.convert.Converter`s
  to Spring's `ConversionService`; enable with `@EnableSpringConverterAdapter`. Supporting:
  `ConversionServiceResolver` (bean `conversionService` / `resolved-conversionService`), `ConversionServiceUtils`.
- `SpringProtocolURLStreamHandler` registers the `spring:` URL protocol (initialized as an infrastructure bean):

| URL form | Factory | Purpose |
|----------|---------|---------|
| `spring:resource:...` | `SpringResourceURLConnectionFactory` | address any Spring `Resource` by URL |
| `spring:env:profiles://{type}` | `SpringEnvironmentURLConnectionFactory` (`SpringProfilesURLConnectionAdapter`) | read active/default profiles as a resource |
| `spring:env:property-sources://{prefix}/{media-type}` | `SpringPropertySourcesURLConnectionAdapter` | export matching properties as JSON/Properties content |
| delegated sub-protocols | `SpringSubProtocolURLConnectionFactory`, `SpringDelegatingBeanProtocolURLConnectionFactory` | contribute handlers as beans (used by `microsphere-spring-jdbc` for `p6spy://`) |

## 10. Miscellaneous

`core.io.ResourceLoaderUtils`, `core.io.ResourceUtils`, `core.io.support.PropertiesUtils`,
`core.io.support.SpringFactoriesLoaderUtils` (cached SPI loading used across the codebase),
`core.MethodParameterUtils`, `core.SpringVersion` (enum of released Spring versions ≥ 6.0 with comparison),
`util.SpringVersionUtils` (runtime version checks), `util.MimeTypeUtils`, `util.FilterMode`
(`SEQUENTIAL`/`CONDITIONAL`, via `microsphere.spring.filter-mode`), `beans.BeanSource`
(`BEAN_FACTORY`/`SPRING_FACTORIES`/`JAVA_SERVICE_PROVIDER` + `registerBeans(...)`),
`constants.PropertyConstants`.

## 11. `META-INF/spring.factories` (module resources)

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

## 12. Gotchas

- Most `*Registrar`/`*Loader` classes are intentionally package-private — annotate, never import them directly.
- `@TTLCacheable` requires `expire`; there is no global default TTL.
- TTL Redis support is disabled (commented out).
- Parallel pre-instantiation is not enabled by a `@Enable*` — you must register the listener bean yourself, and beans
  with unsatisfied ordering assumptions should be audited before turning it on.

---
[← Module guides](./README.md)
