# microsphere-spring-context — Developer Guide (0.1.x)

[← Developer guide overview](./README.md) · [简体中文](./zh/context.md) · [User guide](../../user-guide/1.x/context.md) · [0.2.x](../current/context.md)

This page documents the **0.1.x** line, which exists only on `origin/release-1.x`. Every line number below was read from that branch's object store (`git show origin/release-1.x:<path>`, `git grep -n <symbol> origin/release-1.x -- microsphere-spring-context`); the working tree is the 0.2.x line, so opening these files in the checkout shows different content.

## 1. Module identity, baselines and dependency posture

| Attribute | Value | Evidence |
|---|---|---|
| Maven coordinate | `io.github.microsphere-projects:microsphere-spring-context:${revision}` | `origin/release-1.x:microsphere-spring-context/pom.xml:13-16` |
| Reactor version | `revision` = `0.1.40-SNAPSHOT`; last published release of the line `0.1.39` | `origin/release-1.x:pom.xml:54-56`, tag `0.1.39` |
| Build parent | `microsphere-build:0.3.14` (external, installed POM) | `origin/release-1.x:pom.xml:5-9` |
| Java baseline | `8` — `java.version=8`, `maven.compiler.source/target=${java.version}` | `microsphere-build-0.3.14.pom:58,89,94` |
| Spring baseline | profile-selected `4.3.30.RELEASE` / `5.0.20.RELEASE` / `5.1.20.RELEASE` / `5.2.25.RELEASE` / `5.3.39`; `spring-framework-5.3` is `activeByDefault` | `origin/release-1.x:microsphere-spring-parent/pom.xml:136-179`, `:169-178` |
| Reactor BOM per profile | `Bismuth-SR17` (4.3, 5.0), `Californium-SR23` (5.1), `Dysprosium-SR25` (5.2), `2020.0.47` (5.3) | same file `:141,:149,:157,:165,:176` |
| Managed microsphere libs | `microsphere-java` `0.3.19`, `microsphere-logging` `0.2.2`, Jackson `2.22.2`, SnakeYAML `2.6` | same file `:22-25` |
| Annotation namespace | `javax.annotation:javax.annotation-api`, **optional** | `...:microsphere-spring-context/pom.xml:61-66`; the only `javax.*` import in main sources is `ResourceInjectionPointDependencyResolver.java:21` |
| Reactor modules | 10, including the **0.1.x-only** `microsphere-spring-compatible` | `origin/release-1.x:pom.xml:58-69`, `:61` |
| Main / test entries | 165 main classes + 1 `spring.factories`; 150 test entries (incl. SPI fixtures under `META-INF/services`, `META-INF/spring.factories` and YAML/JSON fixtures under `META-INF/test/`) | `git ls-tree -r --name-only origin/release-1.x -- microsphere-spring-context` |
| Auto-configuration entries | 2 keys, 4 classes in `META-INF/spring.factories` | quoted verbatim in §4.5 |

**Dependency posture.** The only mandatory compile-scope edge is `microsphere-java-core` (`origin/release-1.x:microsphere-spring-context/pom.xml:31-34`); everything else the code compiles against is `optional` and therefore non-transitive: `spring-context` (`:37-41`), `spring-context-support` (`:43-47`), `spring-beans` (`:49-53`), `spring-aop` (`:55-59`), `javax.annotation-api` (`:61-66`), `jackson-annotations`/`jackson-databind` (`:68-79`) and `snakeyaml` (`:81-86`). That is deliberate — this is a *library of extensions to Spring* and must not pin a consumer's Spring version — but the consequence is that declaring `microsphere-spring-context` alone yields a classpath on which almost every public type throws `NoClassDefFoundError` on first use. The Jackson/SnakeYAML optionality is precisely what makes `@JsonPropertySource` and `@YamlPropertySource` opt-in rather than guaranteed features (§4.2). Unlike 0.2.x, `spring-context-indexer` is not a plain dependency either: it appears only inside the `5.0`/`5.1`/`5.2`/`5.3` profiles (`:143-146`, `:160-164`, `:171-175`, `:182-186`) and is absent from `4.3`, where the artifact does not exist.

## 2. Dependency graph

```mermaid
flowchart LR
    CORE["microsphere-java-core 0.3.19<br/>mandatory compile edge"] -->|compile| CTX["microsphere-spring-context"]
    SF["spring-context / -beans / -aop / -context-support<br/>jackson-databind · snakeyaml · javax.annotation-api"] -.optional.-> CTX
    CMP["microsphere-spring-compatible<br/>org.springframework.* shims, never deployed"] -. "optional, ONLY spring-framework-4.3 (:130-135)<br/>and spring-framework-5.0 (:148-153)" .-> CTX
    IDX["spring-context-indexer"] -. "optional, profiles 5.0-5.3 only" .-> CTX
    WEB["microsphere-spring-web"] -->|compile| CTX
    JDBC["microsphere-spring-jdbc"] -->|compile| CTX
    GUICE["microsphere-spring-guice"] -->|compile| CTX
    MVC["microsphere-spring-webmvc / -webflux"] -->|compile, transitively via web| CTX
    CTX -. "test scope — a genuine cycle at test level" .-> TEST["microsphere-spring-test"]
```

Inbound edges: `origin/release-1.x:microsphere-spring-web/pom.xml:33-35`, `microsphere-spring-jdbc/pom.xml:33-35` and `microsphere-spring-guice/pom.xml:37-39` each declare `microsphere-spring-context` at compile scope with `<version>${revision}</version>`; `webmvc` and `webflux` reach it transitively through `microsphere-spring-web` (`microsphere-spring-webmvc/pom.xml:33`, `microsphere-spring-webflux/pom.xml:33`). ⚠ `microsphere-spring-test` declares **no** dependency on this module at all; the edge runs the other way, as a `test`-scope dependency *of* `microsphere-spring-context` (`microsphere-spring-context/pom.xml:109-115`), so the two modules form a test-scoped cycle.

The **`microsphere-spring-compatible`** edge is the most 0.1.x-specific fact on this page. It is declared exactly twice, both `<optional>true</optional>`, and only in the two lowest profiles: `spring-framework-4.3` (`microsphere-spring-context/pom.xml:127-137`, dependency block `:130-135`) and `spring-framework-5.0` (`:139-155`, block `:148-153`); profiles `5.1`, `5.2` and `5.3` (`:157-188`) do **not** declare it. It supplies `org.springframework.*` types Spring 4.3 / early 5.0 lack — 15 classes such as `core/env/Profiles`, `core/env/ProfilesParser`, `lang/NonNull`, `lang/Nullable` and ten `util/*` classes. The module is excluded from deployment by `maven-deploy-plugin <skip>true</skip>` (`microsphere-spring-compatible/pom.xml:34-41`), so it is **not** on Maven Central: `-P spring-framework-4.3` only resolves from inside the reactor. See [compatible.md](./compatible.md).

## 3. Package map

| Package (root `io.microsphere.spring`) | Representative types | Responsibility |
|---|---|---|
| `config.context.annotation` | `PropertySourceExtension`, `PropertySourceExtensionAttributes`, `PropertySourceExtensionLoader`, `AnnotatedPropertySourceLoader`, `ResourcePropertySource(Loader)`, `ResourcePropertySources(Loader)`, `DefaultPropertiesPropertySource(Loader)` | the property-source subsystem (§4.2) |
| `config.env.annotation`, `config.env.support`, `config.env.config` | `YamlPropertySource`, `JsonPropertySource`, `YamlPropertySourceFactory`, `JsonPropertySourceFactory`, `DefaultResourceComparator`, `ResourceYamlProcessor` | format meta-annotations over `@ResourcePropertySource`, their `PropertySourceFactory` implementations, the SnakeYAML bridge |
| `config.env`, `config.env.event`, `config` | `EnvironmentEnabled`, `ImmutableMapPropertySource`, `PropertySourceChangedEvent`, `PropertySourcesChangedEvent`, `ConfigurationPropertyRepository` | Environment-driven enablement, immutable source, auto-refresh events, runtime `@ConfigurationProperty` catalogue |
| `cache`, `cache.annotation` | `TTLContext`, `EnableTTLCaching`, `TTLCacheable`, `TTLCachePut`, `TTLCachingConfiguration` | thread-local TTL carrier and TTL cache activation (§4.3) |
| `cache.intereptor` ⚠, `cache.redis` | `TTLCacheResolver`; `TTLRedisCacheWriterWrapper`, `TTLRedisConfiguration` | the resolver — **package name is misspelled on this branch**; the redis layer is **100 % commented out** (§8) |
| `beans`, `beans.factory` | `BeanUtils`, `BeanSource`, `PropertyValuesUtils`, `BeanFactoryUtils`, `BeanDependencyResolver`, `DefaultBeanDependencyResolver`, `DependencyTreeWalker`, `InjectionPointDependencyResolver(s)`, `DelegatingFactoryBean`, `ObjectProviderUtils` | bean lookup / naming and dependency-graph introspection |
| `beans.factory.annotation` | `AnnotatedInjectionBeanPostProcessor`, `ConfigurationBeanBinding{Registrar,PostProcessor}`, `EnableConfigurationBeanBinding(s)`, `AnnotationBeanDefinitionRegistryPostProcessor` | injection engine + relaxed-binding registration (§4.4) |
| `beans.factory.support`, `.config`, `.filter` | `ListenableAutowireCandidateResolver(Initializer)`, the four `ConfigurationBeanAliasGenerator` types, `BeanRegistrar`, `GenericBeanNameGenerator`, `BeanDefinitionUtils`, `InstantiationAwareBeanPostProcessorAdapter`, `ResolvableDependencyTypeFilter` | candidate resolution, aliasing, registration primitives and definition helpers |
| `context`, `context.annotation` | `ApplicationContextUtils`, `ConfigurableApplicationContextInitializer`, `BeanCapableImportCandidate`, `AnnotatedBeanCapableImportCandidate`/`Selector`/`BeanDefinitionRegistrar`, `ImportOptional(Selector)`, `OverrideAnnotationAttributes(Strategy)`, `AutoRegistrationBean*` | the annotation → registrar/loader model (§4.1) |
| `context.config` | `AutoRegistrationBean`, `ConfigurationBeanBinder`, `DefaultConfigurationBeanBinder`, `ConfigurationBeanCustomizer` | binding and self-registering beans |
| `context.event`, `context.lifecycle` | `BeanFactoryListener(s)`, `BeanListener(s)`, `EventPublishingBean*`, `ParallelPreInstantiationSingletonsBeanFactoryListener`, `DependencyAnalysisBeanFactoryListener`, `EnableEventExtension`, `EventExtensionRegistrar`, `InterceptingApplicationEvent*`, `DeferredApplicationEventPublisher`, `BeanTimeStatistics`, `AbstractSmartLifecycle` | lifecycle events, parallel startup, event interception (§4.6, §4.7) |
| `core.env` | `ListenableConfigurableEnvironment(Initializer)`, `EnvironmentUtils`, `PropertySourcesUtils`, `PropertyResolverUtils`, `EnvironmentListener`/`ProfileListener`/`PropertyResolverListener`/`LoggingEnvironmentListener` | listenable `Environment` (§4.5) |
| `core`, `core.annotation`, `core.convert`, `core.io` | `AnnotationUtils`, `GenericAnnotationAttributes`, `ResolvablePlaceholderAnnotationAttributes`, `ConversionServiceUtils`, `SpringConverterAdapter`, `ResourceLoaderUtils`, `ResourceUtils`, `SpringFactoriesLoaderUtils`, `MethodParameterUtils`, `SpringVersion` | primitives everything else stands on |
| `net`, `constants`, `util` | `SpringProtocolURLStreamHandler`, `Spring*URLConnectionFactory`/`Adapter`, `PropertyConstants`, `MimeTypeUtils`, `FilterMode`, `SpringVersionUtils` | the `spring:` URL protocol (§4.8), property-name constants, utilities |

## 4. Architecture design

### 4.1 The annotation → candidate model

Every `@Enable*` / `@*PropertySource` / `@ImportOptional` entry point resolves to one of three Spring extension interfaces, all funnelled through a single abstract base class:

```
BeanCapableImportCandidate                  (aware: BeanClassLoader, BeanFactory, Environment, ResourceLoader) — :140-141
   ├── AnnotatedBeanCapableImportCandidate<A>   binds a generic annotation type A, adds isEnabled()      (:97-117)
   │      ├── AnnotatedBeanCapableImportSelector<A>            implements ImportSelector                  (:69-77)
   │      │      └── AnnotatedPropertySourceLoader<A>          property-source loaders                    (:67-88)
   │      └── AnnotatedBeanCapableImportBeanDefinitionRegistrar<A>  implements ImportBeanDefinitionRegistrar (:63-114)
```

`selectImports(AnnotationMetadata)` is `final` (`context/annotation/AnnotatedBeanCapableImportSelector.java:73-77`) and delegates to the protected three-arg overload; `registerBeanDefinitions` is likewise `final` and gated by `isEnabled(metadata)` before handing the subclass a `ResolvablePlaceholderAnnotationAttributes<A>` (`AnnotatedBeanCapableImportBeanDefinitionRegistrar.java:70-81,114`). The gate is property-driven (`AnnotatedBeanCapableImportCandidate.java:180-187`), which is why attribute-free `@Enable*` annotations here remain switchable: per importing class `microsphere.spring.<importing-class>@<annotation-FQCN>.enabled` (`:204-207`) or globally `microsphere.spring.<annotation-FQCN>.enabled` (`:222-224`). Annotation *attributes* — not just the on/off switch — can be overridden from the Environment via `@OverrideAnnotationAttributes` + `ConfigurationPropertyOverrideAnnotationAttributesStrategy` (`context/annotation/OverrideAnnotationAttributes.java:75-`, `...Strategy.java:108,129,189,212,245`), whose prefixes are `microsphere.spring.prefix.<annotation-FQCN>` and the default `microsphere.spring.@<SimpleName>.` (`:268,289`; constants in `constants/PropertyConstants.java:41`), and placeholders inside attributes resolve because every attribute map is wrapped in `ResolvablePlaceholderAnnotationAttributes`.

### 4.2 Property-source subsystem

`@PropertySourceExtension` (`config/context/annotation/PropertySourceExtension.java:144-245`) is a `@Target(ANNOTATION_TYPE)` meta-annotation with ten attributes: `name`, `autoRefreshed`, `first`, `before`, `after`, `value`, `resourceComparator`, `ignoreResourceNotFound`, `encoding`, `factory`. It is never used directly: it is meta-annotated onto `@ResourcePropertySource` (`ResourcePropertySource.java:127-134`), which carries `@Import(ResourcePropertySourceLoader.class)` (`:133`). `@YamlPropertySource` and `@JsonPropertySource` are third-level annotations that only pin the factory — `@ResourcePropertySource(factory = YamlPropertySourceFactory.class)` (`config/env/annotation/YamlPropertySource.java:111`) and `@ResourcePropertySource(factory = JsonPropertySourceFactory.class)` (`.../JsonPropertySource.java:116`) — and neither declares an `@Import` of its own; the import arrives through the `@ResourcePropertySource` meta-annotation, which is the path Spring's `ConfigurationClassParser` follows.

Loading happens inside `selectImports`, not in a registrar: `AnnotatedPropertySourceLoader.selectImports` (`:73-88`) resolves the property-source name (`:98-104`, falling back to `<importing-class>@<annotation-FQCN>` at `:129-133`), pulls `getEnvironment().getPropertySources()` and calls the abstract `loadPropertySource` (`:145-146`), wrapping any `Throwable` in `BeanCreationException` (`:82-86`). `PropertySourceExtensionLoader` implements that hook as a `final` template method (`:104-124`): build `PropertySourceExtensionAttributes` reflectively via the `(Map, Class, PropertyResolver)` constructor (`:164-167`), load one `CompositePropertySource` (`:169-201`), and on a `null` result either log a warning (`ignoreResourceNotFound`, `:114-117`) or throw `IllegalArgumentException` (`:119`).

Insertion order is decided by `addPropertySource` (`:133-152`) with strict precedence: `first` → `addFirst`; else `after` → `addAfter`; else `before` → `addBefore`; else `addLast`. The javadoc claim in `PropertySourceExtension#first()` that `before`/`after` are ignored when `first` is set matches the code exactly — they are never consulted in that branch. Multiple `value` locations of one annotation are resolved per location (`:392-423`), wrapped in a comparable `PropertySourceResource` (`:525-551`) and sorted with the user-supplied `resourceComparator` (`:452-454`; the default `DefaultResourceComparator` is a filename comparison, `config/env/support/DefaultResourceComparator.java:45-48`), each leaf source named `<propertySourceName>#<resourceValue>@<resource.toString()>` (`:460-463`).

Auto-refresh: when `autoRefreshed() == true` the loader builds a `ResourcePropertySourcesRefresher` lambda guarded by `synchronized (this)` (`:203-226`) and hands it to the overridable `configureResourcePropertySourcesRefresher`, whose base implementation is literally `// DO NOTHING` (`:499-503`). Only `ResourcePropertySourceLoader` does work: it starts a `StandardFileWatchService` (from `microsphere-java-core`) and registers only *file-based* resources (`ResourcePropertySourceLoader.java:147-169`, filtered by `isFileBasedResource` at `:160`), so a classpath resource inside a JAR is silently unwatched. A refresh rebuilds and `propertySources.replace(name, newComposite)` (`:297-308`), emitting `PropertySourceChangedEvent`s (`added`/`replaced`/`removed`, `:279-339`) grouped into one `PropertySourcesChangedEvent` published on the context (`:228-232`). `@DefaultPropertiesPropertySource` (`DefaultPropertiesPropertySourceLoader.java:46,78,87-91`) is a different animal: it merges into Spring's `defaultProperties` source (`PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME`, `core/env/PropertySourcesUtils.java:53`) instead of adding one, so it always sits at the very bottom.

YAML and JSON differ in the produced `PropertySource`: `YamlPropertySourceFactory` returns an **`ImmutableMapPropertySource`** built through `ResourceYamlProcessor` (`config/env/support/YamlPropertySourceFactory.java:54-56`, `config/env/config/ResourceYamlProcessor.java:74,89-92`), while `JsonPropertySourceFactory` returns a plain `MapPropertySource` over a `LinkedHashMap` (`.../JsonPropertySourceFactory.java:72-76`). `ResourceYamlProcessor.createYaml` (`:96-104`) configures SnakeYAML 2.x `LoaderOptions` with `setAllowDuplicateKeys(false)` and `setMaxAliasesForCollections(Integer.MAX_VALUE)` — the latter an anchor-expansion DoS guard the library chooses to disable.

### 4.3 TTL cache subsystem

`TTLContext` (`cache/TTLContext.java:53-164`) is a plain `ThreadLocal<Duration>` holder (`:55`) with `setTTL`/`getTTL`/`clearTTL` and two `doWithTTL` overloads whose `Function` variant clears the thread-local in a `finally` (`:98-105`); the effective TTL is `defaultIfNull(getTTL(), defaultTTL)` (`:107-110`), i.e. an explicitly set TTL always beats the annotation's. `@EnableTTLCaching` (`cache/annotation/EnableTTLCaching.java:68-73`) is `@EnableCaching` + `@Import(TTLCachingConfiguration.class)` with three `@AliasFor`-aliased attributes (`proxyTargetClass`, `mode`, `order` at `:86,99,107`); `TTLCachingConfiguration` is a **package-private class without `@Configuration`** (`:30`) whose single `@Bean(name = "ttlCacheResolver")` method registers the resolver in "lite" mode (`:32-35`). `@TTLCacheable`/`@TTLCachePut` are `@Cacheable`/`@CachePut` meta-annotations hard-wiring `cacheResolver = TTLCacheResolver.BEAN_NAME` (`TTLCacheable.java:64`, `TTLCachePut.java:66`).

`TTLCacheResolver` (`cache/intereptor/TTLCacheResolver.java:58`) is simultaneously a `CacheResolver` and an `OnceApplicationContextEventListener<ContextRefreshedEvent>`, and only obtains its `Environment` and `Map<String, CacheManager>` inside the event callback (`:152-157`). `resolveCaches` (`:72-117`) maps `CacheOperation` class → TTL annotation type through a two-entry static table (`:62-65`), reads `expire` + `timeUnit`, publishes the resulting `Duration` into `TTLContext` (`:119-122`), then fans the Cartesian product of `cacheNames × cacheManagers` into `Cache` instances; the `cacheManagers` attribute **is** read (`:97-106`) and empty means "all `CacheManager` beans". ⚠ Nothing on this branch *consumes* that TTL: `TTLContext.setTTL` is written at `TTLCacheResolver.java:121` while the only reader, `TTLRedisCacheWriterWrapper.doWithTTL`, lives in a fully commented-out file (§8), and `pom.xml` has no `spring-data-redis` dependency at all — so on 0.1.x `@TTLCacheable` behaves as an ordinary `@Cacheable` unless the application wires its own TTL-aware `CacheWriter`.

### 4.4 Relaxed binding and alias generators

`@EnableConfigurationBeanBinding` (`beans/factory/annotation/EnableConfigurationBeanBinding.java:70-138`) carries `prefix` (required), `type` (required), `multiple`, `ignoreUnknownFields`, `ignoreInvalidFields`, and is `@Repeatable(EnableConfigurationBeanBindings.class)` + `@Import(ConfigurationBeanBindingRegistrar.class)`. The registrar (`ConfigurationBeanBindingRegistrar.java:94`) resolves the sub-property map with `PropertySourcesUtils.getSubProperties(environment.getPropertySources(), environment, prefix)` (`:124`), whose algorithm *is* the whole "relaxed binding" story on this branch (`core/env/PropertySourcesUtils.java:246-273`): normalise the prefix to end in `.` (`:217-219`), iterate `PropertySources` **in Environment order**, and for each name starting with the prefix strip it and `if (!subProperties.containsKey(subName))` — an explicit *take first one* rule (`:259`) — then resolve `${...}` placeholders on `String` values (`:261-265`). There is **no** camel-case ↔ kebab-case ↔ upper-snake equivalence table anywhere in that method: `my.camelCase` and `my.camel-case` are different keys, so "relaxed" here means only "prefix-relative, first-source-wins, placeholder-resolved", far weaker than Spring Boot's `CanonicalPropertyName`. The registrar then registers one bean definition per name (single, or `multiple` with a second `getSubProperties(normalizePrefix(beanName))` pass, `:168-179`), stashes the metadata as bean-definition *attributes* (`ConfigurationBeanBindingPostProcessor.java:233-237`) and registers the `PriorityOrdered` post-processor as an infrastructure bean (`:185-187`, `BEAN_NAME` at `ConfigurationBeanBindingPostProcessor.java:90`).

Alias generation is an SPI: for **every** registered bean name the registrar calls `loadFactories(beanFactory, ConfigurationBeanAliasGenerator.class)` (`:140`) and then `registry.registerAlias(beanName, alias)` unconditionally, with no `null` guard (`ConfigurationBeanBindingRegistrar.java:133,139-146`). Implementations: `DefaultConfigurationBeanAliasGenerator` returns `getSimpleName(configClass) + capitalize(beanName)` — a delimiter-less concatenation (`beans/factory/support/DefaultConfigurationBeanAliasGenerator.java:21-23`); `JoinAliasGenerator` splits `prefix` on `.`, capitalises every segment after the first and appends `delimiter() + beanName` (`:23-30`, `delimiter()` abstract at `:32`), with `HyphenAliasGenerator` (`-`, `:25-28`) and `UnderScoreJoinAliasGenerator` (`_`, `:18-21`) as its two concrete variants. Because discovery is `SpringFactoriesLoaderUtils.loadFactories` and this module's own `META-INF/spring.factories` (§4.5) has **no** `ConfigurationBeanAliasGenerator` key, out of the box the factory list is empty and no alias is registered at all: consumers opt in by contributing a `spring.factories` entry, not by configuring the annotation — and a generator returning `null` is not skipped, it fails context startup.

### 4.5 Listenable `Environment` / `AutowireCandidateResolver` and their property gates

`src/main/resources/META-INF/spring.factories` — verbatim, the complete file:

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

⚠ The file has **no** `EnableAutoConfiguration`, no `AutoConfiguration.imports` equivalent and no `EnvironmentPostProcessor` key: the four initializers are picked up by Spring Boot's initializer chain *only if a Boot application is present*, and in a plain `ClassPathXmlApplicationContext` / `AnnotationConfigApplicationContext` nothing runs automatically. All four extend `ConfigurableApplicationContextInitializer` (`context/ConfigurableApplicationContextInitializer.java:62`), whose `initialize` is `final` (`:70-82`) and does three things — consult the property gate, check `isRegistered(context)` (`:146-148`), run the subclass hook and `registerSelf(context)` (`:155-158`), turning the initializer into a singleton under its generated bean name. The gate comes from `EnvironmentEnabled` (`config/env/EnvironmentEnabled.java:38-130`): `environment.getProperty(getEnabledPropertyName(), boolean.class, getDefaultEnabled())` (`:67-69`).

| Initializer | Property gate | Default | What it does |
|---|---|---|---|
| `AutoRegistrationBeanInitializer` | `microsphere.spring.beans.auto-registered` (`EnableAutoRegistrationBean.java:62`, constant misspelled `..._PROEPRTY_NAME`) | `true` (`PropertyConstants.java:64,69`) | registers a package-private `@EnableAutoRegistrationBean` holder class (`AutoRegistrationBeanInitializer.java:40-47`) |
| `EventPublishingBeanInitializer` | `microsphere.spring.event-publishing-bean.enabled` (`context/event/EventPublishingBeanInitializer.java:84,96`) | **`false`** (`:86,101`) | `context.addBeanFactoryPostProcessor(new EventPublishingBeanBeforeProcessor())`; order `HIGHEST_PRECEDENCE` (`:112-115`) |
| `ListenableAutowireCandidateResolverInitializer` | `microsphere.spring.listenable-autowire-candidate-resolver.enabled` (`beans/factory/support/ListenableAutowireCandidateResolverInitializer.java:77,89`) | **`false`** (`:79,94`) | `registerBeanDefinition(beanFactory, ListenableAutowireCandidateResolver.class)` (`:96-100`) |
| `ListenableConfigurableEnvironmentInitializer` | `microsphere.spring.listenable-environment.enabled` (`core/env/ListenableConfigurableEnvironmentInitializer.java:73,85`) | **`false`** (`:75,90`) | `context.setEnvironment(new ListenableConfigurableEnvironment(context))` (`:92-95`) |

Three of the four are therefore **dead by default**: out of the box only `AutoRegistrationBeanInitializer` runs, because the base default is `true` (`ConfigurableApplicationContextInitializer.java:121-123`) while `EventPublishingBeanInitializer`, `ListenableAutowireCandidateResolverInitializer` and `ListenableConfigurableEnvironmentInitializer` each override `getDefaultEnabled()` to `false`.

`ListenableConfigurableEnvironment` (`core/env/ListenableConfigurableEnvironment.java:59`) implements the whole `ConfigurableEnvironment` interface by delegation, holds three listener lists (`:105-109`) loaded from Spring SPI plus beans, and — because one artifact must serve Spring 4.3 through 5.3 — reaches version-specific methods through `MethodHandle`s resolved once at class-initialisation time: `PropertyResolver.getPropertyAsClass` (`:89`), `Environment.acceptsProfiles(Profiles)` (`:95`, the Spring 5.1 signature that deprecates the varargs form) and `Environment.matchesProfiles` (`:101`, Spring 5.2.7+), each call site null-checking the handle and falling back to the older API. `ListenableAutowireCandidateResolver` (`beans/factory/support/ListenableAutowireCandidateResolver.java:73`) uses the identical technique — `IS_REQUIRED_METHOD_HANDLE` (`:83`, "Spring Framework 5.0"), `HAS_QUALIFIER_METHOD_HANDLE` (`:90`, "5.1"), `CLONE_IF_NECESSARY_METHOD_HANDLE` (`:97`, "5.2.7") — with `descriptor.isRequired()`, `false` and `instantiateClass(delegate.getClass())` as fallbacks (`:185-298`). It is *both* a bean definition and a `BeanFactoryPostProcessor` (`:318-320` → `wrap(beanFactory)`, `:333-344`): `wrap` reads the factory's current resolver, stores it as `delegate`, installs a `CompositeAutowireCandidateResolvingListener` built from `AutowireCandidateResolvingListener.loadListeners(beanFactory)` (`:77-86`, Spring SPI **plus** sorted beans) and calls `dbf.setAutowireCandidateResolver(this)`. Only `getSuggestedValue` (`:241-245`) and `getLazyResolutionProxyIfNecessary` (`:270-274`) notify listeners; `isAutowireCandidate` (`:176-178`) is a silent pass-through.

### 4.6 Bean-factory listeners and parallel pre-instantiation

`BeanFactoryListener` (`context/event/BeanFactoryListener.java:61`) adds three callbacks to the plain `ApplicationListener` contract — `onBeanDefinitionRegistryReady` (`:68`), `onBeanFactoryReady` (`:75`), `onBeanFactoryConfigurationFrozen` (`:84`) — with `BeanFactoryListenerAdapter` (`:36-47`) supplying no-op defaults. `BeanListener` (`BeanListener.java:68`) is far richer: 11 hooks from `onBeanDefinitionReady` through the three `onBeforeBeanInstantiate` overloads to `onAfterBeanDestroy` (`:84-170`), gated by `supports(String beanName)`. Package-private composites `BeanFactoryListeners` (`:50`) and `BeanListeners` (`:54`) fan out to all matching beans (`:78-88` / `:97`) and register themselves as infrastructure beans via `registerBean` (`:110` / `:183`). The publishers are `EventPublishingBeanBeforeProcessor` (installed by the initializer above) and `EventPublishingBeanAfterProcessor`, which attach the `InstantiationAwareBeanPostProcessorAdapter`/`GenericBeanPostProcessorAdapter` hooks that raise the `BeanListener` events; `DependencyAnalysisBeanFactoryListener` and `LoggingBeanFactoryListener` are the two shipped `BeanFactoryListenerAdapter` consumers, and `BeanTimeStatistics` the per-bean timing record.

`ParallelPreInstantiationSingletonsBeanFactoryListener` (`context/event/ParallelPreInstantiationSingletonsBeanFactoryListener.java:109`) hooks `onBeanFactoryConfigurationFrozen` (`:148-173`) — the last point at which Spring's own `preInstantiateSingletons()` has not yet run — builds a dependency map with `DefaultBeanDependencyResolver` (`:185-194`) and instantiates in dependency-safe waves on a fixed thread pool (`:196+`). Configuration is Environment-driven and read per startup (`:175-183`): `microsphere.spring.pre-instantiation.singletons.threads` (default = available processors; **any value `< 1` disables the feature entirely**, because `newExecutorService()` returns `null` and the caller checks for it at `:161`) and `microsphere.spring.pre-instantiation.singletons.thread.name-prefix` (default `Parallel-Pre-Instantiation-Singletons-Thread-`; `:115,125,130,140`). It is registered as an ordinary bean — nothing in `spring.factories` wires it.

### 4.7 Event interception

`@EnableEventExtension` (`context/event/EnableEventExtension.java:57-62`) is `@OverrideAnnotationAttributes` + `@Import(EventExtensionRegistrar.class)` with three attributes: `intercepted` (default `true`, `:79`), `executorForListener` (default `"N/E"`, the sentinel `NO_EXECUTOR` at `:67`) and `sources()`, the bean-discovery set for interceptors, default `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` (`:102`). The package-private `EventExtensionRegistrar` (`:55`) replaces Spring's `applicationEventMulticaster` bean definition (`:69-118`) in three evidenced cases: `!intercepted && !associatedExecutorBean` → log and **return without registering** (`:81-90`); no existing definition → build a `RootBeanDefinition` of `InterceptingApplicationEventMulticaster` (or `SimpleApplicationEventMulticaster` when only an executor is wanted) and stamp the `intercepted` / `executorForListener` / `@className` attributes on it (`:151-158`); an existing definition → if the attributes match, skip (`:100-105,120-128`), otherwise **remove** it, re-register under a reset bean name derived from the Environment (`getResetBeanName(super.environment)`, `:139-141`) and substitute `InterceptingApplicationEventMulticasterProxy` (`:143`).

`InterceptingApplicationEventMulticaster` (`:42`) makes `multicastEvent` (`:48-55`) and `invokeListener` (`:65-69`) `final`, routing them through `DefaultApplicationEventInterceptorChain` / `DefaultApplicationListenerInterceptorChain` whose terminal nodes are the `protected` `doMulticastEvent`/`doInvokeListener` (`:71-77`); interceptor lists are collected once in `setBeanFactory` via `getSortedBeans` (`:79-85`). `getTaskExecutor()` is overridden to fall back to `Runnable::run` (`:87-94`), so the whole multicast runs *inline* on the publishing thread unless an executor bean is named. `ApplicationEventInterceptor` (`:52`) and `ApplicationListenerInterceptor` extend `Ordered` with a `getOrder()` default (`:90`); the chain contract is `intercept(event, type, chain)` (`:87`).

### 4.8 The `spring:` URL protocol

`SpringProtocolURLStreamHandler` (`net/SpringProtocolURLStreamHandler.java:60`) extends `microsphere-java-core`'s `ExtendableProtocolURLStreamHandler` with `PROTOCOL = "spring"` (`:62`) and registers itself as a **JVM-wide** handler in `afterPropertiesSet()` → `super.init()` (`:76-80`). Its grammar is `spring:{sub-protocol}:{ext-1}:...://{authority}{path}`, and a missing sub-protocol is a hard `MalformedURLException` (`:95-103`). Three factories are installed in a fixed order (`:88-93`):

| Sub-protocol | Factory | Dispatch |
|---|---|---|
| `resource` | `SpringResourceURLConnectionFactory` (`net/SpringResourceURLConnectionFactory.java:40`) | second segment becomes the Spring resource scheme (`:72-77`), e.g. `spring:resource:classpath://…` |
| `env` | `SpringEnvironmentURLConnectionFactory` (`:44`) | type at index 1: `property-sources` (`:46`) or `profiles` (`:48`), `TYPE_INDEX = 1` (`:50`) |
| *any* | `SpringDelegatingBeanProtocolURLConnectionFactory` (`:36`) | looks up all `SubProtocolURLConnectionFactory` **beans** and delegates (`:45,67`) — how downstream modules such as `microsphere-spring-jdbc`'s `p6spy:` join |

`supports(...)` on the shared base is `final` and compares only the *first* resolved sub-protocol with `getSubProtocol()` (`net/SpringSubProtocolURLConnectionFactory.java:37-42`). The handler is a bean, not a static initialiser: nothing registers `spring:` unless `@EnableEventExtension`, `@EnableP6DataSource` or an equivalent `@Import` pulls it in.

## 5. Two complex flows

### 5.1 `@ResourcePropertySource` registration and auto-refresh

```mermaid
flowchart TD
    A["@Configuration class annotated '@ResourcePropertySource'"] --> B["ConfigurationClassParser processes the<br/>meta-annotated '@Import(ResourcePropertySourceLoader.class)'"]
    B --> C["AnnotatedPropertySourceLoader.selectImports (:73-88)"]
    C --> D["resolvePropertySourceName: 'name' attribute,<br/>else class@annotation (:98-133)"]
    D --> E["PropertySourceExtensionLoader.loadPropertySource — final (:104-124)"]
    E --> F["buildExtensionAttributes, reflective ctor (:164-167)"]
    F --> G["resolveResources → PathMatchingResourcePatternResolver<br/>+ resourceComparator (:452-454, :102-105)"]
    G --> J{"any resource found?"}
    J -- "no" --> K{"ignoreResourceNotFound?"}
    K -- true --> L["logger.warn, continue (:114-117)"]
    K -- false --> M["IllegalArgumentException (:119)"]
    J -- "yes" --> N["createPropertySourceFactory (:438-440) →<br/>one source per resource (:460-486) → Composite (:183-190)"]
    N --> Q{"autoRefreshed?"}
    Q -- false --> R["base refresher = DO NOTHING (:499-503)"]
    Q -- true --> S["StandardFileWatchService.start(), file-based only (:147-169)"]
    R --> T["addPropertySource: first→addFirst · after→addAfter<br/>· before→addBefore · else addLast (:133-152)"]
    S --> T
    S -. "file change" .-> AA["refresher lambda, synchronized(this) (:203-226)"]
    AA --> AB["rebuild + replace (:297-339)"]
    AB --> AC["publish PropertySourcesChangedEvent (:228-232)"]
```

### 5.2 Initializer and startup ordering

```mermaid
flowchart TD
    A["SpringApplication.run()"] --> B["ApplicationContextInitializer chain<br/>from META-INF/spring.factories (no declared order<br/>except EventPublishingBeanInitializer, :112-115)"]
    B --> C{"event-publishing-bean.enabled? default false"}
    C -- true --> C2["addBeanFactoryPostProcessor(EventPublishingBeanBeforeProcessor)"]
    B --> D{"beans.auto-registered? default true"}
    D -- true --> D2["register @EnableAutoRegistrationBean holder (:40-47)"]
    B --> E["listenable-autowire-candidate-resolver.enabled? default false<br/>→ registerBeanDefinition(ListenableAutowireCandidateResolver)"]
    B --> F["listenable-environment.enabled? default false<br/>→ context.setEnvironment(...) replaces the Environment<br/>AFTER earlier initializers already read it (:92-95)"]
    C2 --> G["refresh(): bean-definition phase"]
    D2 --> G
    E --> G
    G --> H["ImportSelectors: property-source loaders mutate MutablePropertySources (:73-88)"]
    G --> I["ImportBeanDefinitionRegistrars: ConfigurationBeanBindingRegistrar, EventExtensionRegistrar"]
    I --> I1["applicationEventMulticaster replaced / proxied"]
    H --> J["BeanFactoryPostProcessors"]
    J --> K["ListenableAutowireCandidateResolver.postProcessBeanFactory (:318-320) → wrap (:333-344)"]
    J --> L["EventPublishingBeanBeforeProcessor publishes<br/>onBeanDefinitionRegistryReady / onBeanFactoryReady"]
    K --> M["finishBeanFactoryInitialization → freezeConfiguration()"]
    L --> M
    M --> N["BeanFactoryListeners fan-out onBeanFactoryConfigurationFrozen (:83-88)"]
    N --> O["ParallelPreInstantiationSingletonsBeanFactoryListener (:148-173)"]
    O --> P["DefaultBeanDependencyResolver → dependency waves on a fixed pool (:185-194)"]
    P --> Q["remaining singletons via Spring's own preInstantiateSingletons()"]
    Q --> R["ContextRefreshedEvent"]
    R --> S["TTLCacheResolver.onApplicationContextEvent (:152-157)"]
```

⚠ That ordering is *not* declared: only `EventPublishingBeanInitializer` implements `Ordered` (`:79,112-115`). The other three rely on `spring.factories` listing order and Spring Boot's default sorting, so `ListenableConfigurableEnvironmentInitializer`'s `context.setEnvironment(...)` (`ListenableConfigurableEnvironmentInitializer.java:94`) can run after initializers that already cached the old `Environment`.

## 6. Design patterns evidenced

| Pattern | Class(es) | Evidence |
|---|---|---|
| Template Method (frozen steps) | `AnnotatedPropertySourceLoader` → `PropertySourceExtensionLoader` → `ResourcePropertySourceLoader` | `selectImports` final `:73`; `loadPropertySource` final `:104`; abstract `resolveResources` `:515`, `isResourcePattern` `:92`, overridable `configureResourcePropertySourcesRefresher` `:499` |
| Template Method | `ConfigurableApplicationContextInitializer.initialize` final + abstract 2-arg overload | `:70-82` / `:90` |
| Strategy | `OverrideAnnotationAttributesStrategy` + `ConfigurationPropertyOverrideAnnotationAttributesStrategy`; `InjectionPointDependencyResolver` + three implementations; `ConfigurationBeanAliasGenerator` + four implementations | selection at `BeanCapableImportCandidate#getOverriddenAnnotationAttributes`; `spring.factories` §4.5; `ConfigurationBeanBindingRegistrar.java:139-146` |
| Registry | `BeanDefinitionRegistry` wrappers in `BeanRegistrar`; `ConfigurationPropertyRepository` (`config/ConfigurationPropertyRepository.java:78`, `BEAN_NAME` `:80`, bounded by `…config-property-repository.max-size`, default `99999`, `:84,100`) | — |
| Decorator / Proxy | `ListenableConfigurableEnvironment` (wraps `ConfigurableEnvironment`, `getDelegate()` `:390`), `ListenableAutowireCandidateResolver` (`wrap` `:333-344`), `InterceptingApplicationEventMulticasterProxy`, `DelegatingFactoryBean` | — |
| Chain of Responsibility | `ApplicationEventInterceptorChain` / `DefaultApplicationEventInterceptorChain`, `ApplicationListenerInterceptorChain` / `DefaultApplicationListenerInterceptorChain` | terminal nodes `InterceptingApplicationEventMulticaster.java:52,67` |
| Composite | `CompositeAutowireCandidateResolvingListener`, `BeanFactoryListeners`, `BeanListeners`, `InjectionPointDependencyResolvers`, and the `CompositePropertySource` every loader produces | `InjectionPointDependencyResolvers.java:59-85`; `PropertySourceExtensionLoader.java:183` |
| Observer | `AutowireCandidateResolvingListener`, `EnvironmentListener`/`ProfileListener`/`PropertyResolverListener`, `BeanListener`, `BeanFactoryListener`, `FileChangedListener` (`ResourcePropertySourceLoader.ListenerAdapter`) | `AutowireCandidateResolvingListener.java:94,105` |
| Null Object | base `configureResourcePropertySourcesRefresher` (`// DO NOTHING`), `BeanFactoryListenerAdapter` / `BeanListenerAdapter` no-op defaults | `PropertySourceExtensionLoader.java:499-503`; `BeanFactoryListenerAdapter.java:39-47`; `BeanListenerAdapter.java:36,39` |
| Thread-local context | `TTLContext` static `ThreadLocal` | `cache/TTLContext.java:55` |
| Adapter (cross-version) | `MethodHandle` guards in `ListenableConfigurableEnvironment` and `ListenableAutowireCandidateResolver`; `SpringConverterAdapter`; `GenericApplicationListenerAdapter`; `SpringProfilesURLConnectionAdapter` | `:89,95,101` and `:83,90,97` |
| Marker / enable annotation | `EnableTTLCaching`, `EnableEventExtension`, `EnableConfigurationBeanBinding`, `EnableAutoRegistrationBean`, `@ImportOptional` | each `@Import`s exactly one candidate |

## 7. 0.1.x vs 0.2.x divergence for this module

The file **sets** are almost identical — 165 main classes on `origin/release-1.x` versus 161 in the working tree, 4 files only on 0.1.x and none only on 0.2.x (`comm -23/-13` over `git ls-tree -r --name-only` of each side), so the divergence is in *content*, not inventory.

| Aspect | 0.1.x (`origin/release-1.x`) | 0.2.x (working tree) | Evidence |
|---|---|---|---|
| Java baseline | `8` | `17` (property declared in the reactor root) | `microsphere-build-0.3.14.pom:89` vs `pom.xml:56` |
| Reactor version | `0.1.40-SNAPSHOT`, last release `0.1.39` | `0.2.40-SNAPSHOT` | `origin/release-1.x:pom.xml:55` vs `pom.xml:55` |
| Build parent | `microsphere-build:0.3.14` | `microsphere-build:0.3.17` | `origin/release-1.x:pom.xml:5-9` vs `pom.xml:5-9` |
| Spring matrix | 4.3.30 / 5.0.20 / 5.1.20 / 5.2.25 / 5.3.39, default `5.3` | 6.0 / 6.1 / 6.2 / 7.0, default `7.0` | `origin/release-1.x:microsphere-spring-parent/pom.xml:136-179` vs `microsphere-spring-parent/pom.xml:138-173` |
| Extra reactor module | `microsphere-spring-compatible` (15 `org.springframework.*` shims, never deployed) | module deleted from the reactor | `origin/release-1.x:pom.xml:61`; absent from `pom.xml:58-69` |
| `spring-context-indexer` | profile-scoped, absent under `4.3` | unconditional optional dependency | `origin/release-1.x:microsphere-spring-context/pom.xml:143-186` vs `microsphere-spring-context/pom.xml:61-65` |
| Test framework | JUnit **4** (`junit:junit`) | JUnit 5 (`org.junit.jupiter:junit-jupiter`) | `origin/release-1.x:microsphere-spring-context/pom.xml:89-93` vs `microsphere-spring-context/pom.xml:94-99` |
| Cross-version API shims | `MethodHandle` guards for `isRequired`/`hasQualifier`/`cloneIfNecessary`/`acceptsProfiles(Profiles)`/`matchesProfiles`/`getPropertyAsClass` | all replaced by direct `@Override` delegation (`delegate.isRequired(descriptor)`, `delegate.hasQualifier(...)`, `delegate.matchesProfiles(...)`); 0.2.x instead adds a `setEscapeCharacter` handle | `git diff origin/release-1.x HEAD -- .../ListenableAutowireCandidateResolver.java` (−72 lines), `.../ListenableConfigurableEnvironment.java` (±100) |
| `ENABLED_PROPERTY_NAME` constants | declared on `ListenableConfigurableEnvironment` itself (`core/env/ListenableConfigurableEnvironment.java:66-83`) | removed from that class | same diff |
| Types deleted on 0.2.x | `beans/factory/ObjectProviderUtils` (333 lines), `beans/factory/config/InstantiationAwareBeanPostProcessorAdapter` (132), `beans/factory/support/BeanDefinitionBuilderUtils` (87), `context/event/DeferredApplicationEventPublisher` (198) | absent | `git ls-tree` set difference; `git diff --stat origin/release-1.x HEAD -- microsphere-spring-context/src/main` |
| Heavily rewritten on 0.2.x | `core/SpringVersion.java` (enum of every Spring release since 3.2, 402-line diff), `beans/factory/config/BeanDefinitionUtils.java` (−312), `beans/BeanUtils.java` (±98), `context/event/InterceptingApplicationEventMulticasterProxy.java` (±39) | slimmed | same diff stat |
| `javax.*` vs `jakarta.*` | ⚠ **No difference in this module**: both branches import `javax.annotation.Resource` at `ResourceInjectionPointDependencyResolver.java:21` and both declare `javax.annotation:javax.annotation-api` optional. The `jakarta.jakartaee-bom 11.0.0` import is new on 0.2.x (`microsphere-spring-parent/pom.xml:24,51-58`) but this module does not consume it. | — | `git grep -n "^import javax\." origin/release-1.x -- microsphere-spring-context/src/main/java` and the same against `HEAD` |
| `META-INF/spring.factories` | byte-identical content (2 keys, 4 initializers) | identical | both quoted in §4.5 |
| TTL redis layer | present but fully commented out | same, still commented | `cache/redis/*.java` on both sides |

## 8. Gotchas, dead code and unwired attributes

| # | Observation | Evidence |
|---|---|---|
| 1 | **`DefaultConfigurationBeanBinder.bind` swaps its two flags**: `dataBinder.setIgnoreInvalidFields(ignoreUnknownFields)` then `dataBinder.setIgnoreUnknownFields(ignoreInvalidFields)`. Setting `ignoreUnknownFields = false` therefore actually relaxes *invalid* fields, and vice-versa. The post-processor's attribute plumbing is correct, so the bug stays invisible until binding fails. | `context/config/DefaultConfigurationBeanBinder.java:42-55` (lines `:46-47`); `ConfigurationBeanBindingPostProcessor.java:94-96,233-237` |
| 2 | `io.microsphere.spring.cache.intereptor` is misspelled in main sources, while the test tree uses **both** spellings: `cache/intereptor/TTLCacheResolverTest.java:17` matches production, `cache/interceptor/TTLCacheableOperation.java` sits in a correctly spelled package. Renaming either side breaks anything referencing the misspelled package by name. | `git ls-tree` listing; `cache/intereptor/TTLCacheResolver.java:17` |
| 3 | `cache/redis/TTLRedisCacheWriterWrapper.java` and `cache/redis/TTLRedisConfiguration.java` are **100 % commented out** — every line from the `package` declaration down is a `//` comment. They compile as empty compilation units, contribute nothing, and are the only designed consumers of `TTLContext`; consequently `@TTLCacheable`/`@TTLCachePut` never apply a TTL by themselves, and there is no `spring-data-redis` dependency to un-comment them against. | `cache/redis/TTLRedisCacheWriterWrapper.java:17-99`, `cache/redis/TTLRedisConfiguration.java:17-71`; `microsphere-spring-context/pom.xml:21-123` |
| 4 | `TTLCacheResolver.getTTL` does `(Long) attributes.get("expire")` with no `null` guard, and `getTTLAnnotationAttributes` returns `null` for any `CacheOperation` other than `CacheableOperation`/`CachePutOperation` **and** whenever the TTL annotation is absent from the method or target class. Since `@TTLCacheable`'s `cacheResolver` is fixed at the *annotation* level, mixing it with `@CacheEvict`/`@Caching` on the same method reaches `setTTL(null)` → NPE. `resolveCaches` also NPEs on `namedCacheManagersMap.size()` (`:82`) if invoked before `ContextRefreshedEvent` — which is normal, because Spring builds operation metadata during proxy creation. | `cache/intereptor/TTLCacheResolver.java:81-85,91-93,119-122,124-143,145-150` |
| 5 | `TTLCachingConfiguration` is package-private **and** carries no `@Configuration`. It works (`@Bean` in "lite" mode) but the bean name is the method name and the class cannot be imported by user code; `@EnableTTLCaching` is the only entry point. | `cache/annotation/TTLCachingConfiguration.java:30-35` |
| 6 | Constant name typo: `BEANS_AUTO_REGISTERED_PROEPRTY_NAME` (value `"microsphere.spring.beans.auto-registered"`) is referenced from `AutoRegistrationBeanInitializer.java:113`. Only the Java identifier is wrong, so a rename is source-compatible but binary-breaking. | `context/annotation/EnableAutoRegistrationBean.java:62` |
| 7 | Three of the four `spring.factories` initializers default to `false` **and** are listed: out of the box only `AutoRegistrationBeanInitializer` runs, and `ListenableConfigurableEnvironmentInitializer` replaces the `Environment` late in the chain with no declared order (see the warning under §5.2). | `EventPublishingBeanInitializer.java:86,101`; `ListenableAutowireCandidateResolverInitializer.java:79,94`; `ListenableConfigurableEnvironmentInitializer.java:75,90` |
| 8 | `ListenableAutowireCandidateResolver` never notifies listeners for `isAutowireCandidate` (`:176-178`), `isRequired` (`:185-198`) or `hasQualifier` (`:205-217`), although `AutowireCandidateResolvingListener` is documented as covering "the resolving processing" — only two of five seams fire events. | `AutowireCandidateResolvingListener.java:35-40,94,105` |
| 9 | `PropertySourceExtensionLoader.handleSourceName` calls `substring(0, lastIndexOf("@"))` unconditionally, so a resource-property-source name without an `@` (`lastIndexOf` = `-1`) yields truncated or exception-prone comparisons during refresh; the `@` is guaranteed only because `createResourcePropertySourceName` (`:460-463`) injects one. | `PropertySourceExtensionLoader.java:341-344,460-463` |
| 10 | Auto-refresh watches **files only**: `isFileBasedResource` filters out `ClasspathResource`s backed by a JAR (`:160`), and a *new* `StandardFileWatchService` is created per loader instance (`:152`) — one watcher thread group per annotated source, no dedup. `ignoreResourceNotFound = true` plus a wildcard matching nothing produces only a warning. | `ResourcePropertySourceLoader.java:147-169`; `PropertySourceExtensionLoader.java:114-117` |
| 11 | Encoding defaults are inconsistent: `@YamlPropertySource.encoding()` is the literal `"UTF-8"` (`:207`) and `@JsonPropertySource.encoding()` likewise (`:211`), whereas `@PropertySourceExtension.encoding()` is the placeholder `"${file.encoding:UTF-8}"` (`PropertySourceExtension.java:235`) — so the two format annotations **do not** honour `file.encoding`, unlike the base annotation. | `config/env/annotation/YamlPropertySource.java:207`, `.../JsonPropertySource.java:211` |
| 12 | `YamlPropertySourceFactory` and `JsonPropertySourceFactory` construct `new ImmutableMapPropertySource(name, …)` / `new MapPropertySource(name, …)` with the passed-in `name`, which is `null` when either factory is used through plain Spring `@PropertySource` instead of through these annotations; nothing rejects `null`. | `config/env/support/YamlPropertySourceFactory.java:54-56`, `.../JsonPropertySourceFactory.java:72-76` |
| 13 | "Relaxed binding" is only prefix-relative first-wins extraction (§4.4) — no camel/kebab/underscore equivalence, so `getSubProperties` cannot see a property whose *name* differs in case style from the field. Placeholder resolution inside sub-values does happen (`:261-265`). | `core/env/PropertySourcesUtils.java:246-273` |
| 14 | `InterceptingApplicationEventMulticaster.getTaskExecutor()` returns `Runnable::run` when no executor is configured (`:87-94`), making delivery synchronous *including the interceptor chain*; `@EnableEventExtension(executorForListener = "N/E")` is the default, so the "asynchronous listening" the attribute advertises (`:81-87`) is off unless you name a bean. Relatedly, `EventExtensionRegistrar` mutates the `applicationEventMulticaster` definition in place and on a second, differently-attributed `@EnableEventExtension` *removes and re-registers* the original under a reset name (`:130-149`) — two conflicting annotations in one context silently produce a proxy chain. | `InterceptingApplicationEventMulticaster.java:87-94`; `EnableEventExtension.java:67,81-87`; `EventExtensionRegistrar.java:130-149` |
| 15 | The `spring:` URL protocol is registered **JVM-globally** from `afterPropertiesSet` (`net/SpringProtocolURLStreamHandler.java:76-80`) and `URL.setURLStreamHandlerFactory` may only be called once per JVM: a second context importing the handler, or an application that already installed its own factory, fails here. Nothing registers it by default — it arrives only through `@Import`. | as cited in §4.8 |
| 16 | `microsphere-spring-compatible` is `<skip>true</skip>` for deployment (`microsphere-spring-compatible/pom.xml:34-41`) yet is a reactor module (`pom.xml:61`) and an *optional compile* dependency of this module under two profiles: consumers on Spring 4.3/5.0 cannot resolve it from a repository and must re-add the shim classes themselves. | as cited in §2 |
| 17 | The alias SPI has no fallback and no caching: `registerConfigurationBeanAlias` runs *inside* the bean-name loop (`ConfigurationBeanBindingRegistrar.java:133`), re-invoking `loadFactories` per bean and passing the result straight to `registry.registerAlias` (`:143`). Combined with §4.4, a `null`-returning generator aborts context startup while the shipped defaults contribute no aliases at all. | `ConfigurationBeanBindingRegistrar.java:133,139-146`; `beans/factory/support/DefaultConfigurationBeanAliasGenerator.java:21-23` |
| 18 | `@since 1.0.0` appears on essentially every type of this module (`PropertySourceExtension.java:142`, `TTLContext.java:51`, …) while the analysed reactor version is `0.1.40-SNAPSHOT`. Do not use `@since` for release mapping on this branch. | `origin/release-1.x:pom.xml:55` |

---

**Cross-links** — [Developer guide overview](./README.md) · [简体中文](./zh/context.md) · [User guide — context (1.x)](../../user-guide/1.x/context.md) · [0.2.x developer guide](../current/context.md) · [1.x parent POM](./parent.md) · [1.x dependency management](./dependencies.md) · [1.x compatible shims](./compatible.md) · [0.2.x jdbc](../current/jdbc.md) · [0.2.x guice](../current/guice.md)
