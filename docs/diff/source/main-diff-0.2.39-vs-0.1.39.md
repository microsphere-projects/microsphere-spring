# Source Diff — `0.2.39` vs `0.1.39` (`src/main`)

**English** · [简体中文](./zh/main-diff-0.2.39-vs-0.1.39.md)

Implementation diff of the `src/main` sources of the two latest released tags of
`microsphere-spring`, module by module. The `src/test` diff is kept in a separate document:
[test-diff-0.2.39-vs-0.1.39.md](./test-diff-0.2.39-vs-0.1.39.md).

## 1. Scope and method

| Item | Value |
|---|---|
| Tag A (left side, `-` lines) | `0.2.39` — branch `release`, version line 0.2.x |
| Tag B (right side, `+` lines) | `0.1.39` — branch `release-1.x`, version line 0.1.x |
| Diff command shape | `git diff 0.2.39 0.1.39 -- <module>/src/main` |
| Modules compared | Only Maven modules present in **both** tags (9) |
| Modules ignored | `microsphere-spring-compatible` (exists only in `0.1.39`) |
| Path filter | `src/main/java`, `src/main/resources` (incl. `META-INF/services`), module `pom.xml` |

Status letters used in the tables:

| Letter | Meaning |
|---|---|
| `A` | File exists only in `0.1.39` (added on the 0.1.x line) |
| `D` | File exists only in `0.2.39` (dropped on the 0.1.x line) |
| `M` | File present in both tags, with content differences |
| `R` | File moved/renamed (percentage = similarity) |

## 2. Version baselines

The two lines are pinned to different platforms, which explains most of the surviving differences.

| Dimension | `0.2.39` | `0.1.39` |
|---|---|---|
| Java | 17 (`<java.version>17</java.version>`) | 8 (no `java.version` override) |
| Java EE / Jakarta EE namespace | `jakarta.*` | `javax.*` |
| Jakarta EE BOM | `jakarta.platform:jakarta.jakartaee-bom:11.0.0` (imported) | not imported |
| Servlet container | Servlet 6.0 descriptors / `web.xml` 6.0 | `tomcat-embed-core:9.0.119`, Servlet 3.1 descriptors |
| Spring Framework profiles | `6.0` (6.0.23) / `6.1` (6.1.21) / `6.2` (6.2.18) / `7.0` (7.0.9, default) | `4.3` (4.3.30.RELEASE) / `5.0` (5.0.20.RELEASE) / `5.1` (5.1.20.RELEASE) / `5.2` (5.2.25.RELEASE) / `5.3` (5.3.39, `activeByDefault`) |
| Spring Reactor BOM | `2022.0.22` … `2025.0.7` | `Bismuth-SR17` … `2020.0.47` |
| Test framework | JUnit 5 (`junit-jupiter`) | JUnit 4 (`junit`) |
| `<revision>` | `0.2.39-SNAPSHOT` | `0.1.39-SNAPSHOT` |

Identical on both tags: Jackson `2.22.2`, SnakeYAML `2.6`, P6Spy `3.9.1`, ZooKeeper `3.9.5`,
Curator `5.9.0`, `microsphere-java` `0.3.19`, `microsphere-logging` `0.2.2`, build parent
`microsphere-build` `0.3.14`. `microsphere-spring-dependencies/pom.xml` (the BOM) is byte-identical
on both tags — and it never lists `microsphere-spring-compatible`, even though the `0.1.39` root POM
declares it as a module (root `pom.xml:61`).

## 3. Cause categories

Each row in the module tables is tagged with the dominant reason for the difference:

| Tag | Meaning |
|---|---|
| **API compat** | The 0.1.x line must run on Spring 4.3–5.3, so a newer Spring API is replaced by a `MethodHandle`/reflection probe plus a fallback (or by a locally re-declared shim). |
| **Namespace** | Pure `javax.*` ↔ `jakarta.*` substitution — listed for completeness, not counted as an implementation difference. |
| **JDK syntax** | Java 17 pattern matching / `var` / `toList()` vs Java 8 equivalents — excluded from the analysis by request. |
| **Feature** | Behaviour deliberately present on one line only (e.g. a capability that needs Spring 6/7 APIs). |
| **Dead API** | Code present but with no caller on its own tag. |
| **Defect** | A behavioural bug found while diffing; noted rather than silently described. |
| **Javadoc/comment** | Prose-only change, no code semantics. |

## 4. `src/main` diff summary

| Maven module | Added (`0.1.39`) | Deleted (`0.2.39` only) | Modified | Lines (`+` = 0.1.39 / `-` = 0.2.39) | Character of the diff |
|---|---|---|---|---|---|
| `microsphere-spring-context` | 4 | 0 | 33 | `+1768 / -270` | Compatibility bridges (largest concentration) |
| `microsphere-spring-dependencies` | 0 | 0 | 0 | — | Byte-identical BOM |
| `microsphere-spring-guice` | 0 | 0 | 0 | — | No main-source diff |
| `microsphere-spring-jdbc` | 0 | 0 | 0 | — | No main-source diff |
| `microsphere-spring-parent` | 0 | 0 | 0 | — | No main sources exist |
| `microsphere-spring-test` | 0 | 2 | 15 | `+90 / -219` | Test scaffolding: Servlet namespace + removed WebFlux/WebMvc fixtures |
| `microsphere-spring-web` | 1 | 1 | 14 | `+190 / -115` | One unused visitor API, one Spring 7 workaround, rest namespace |
| `microsphere-spring-webflux` | 0 | 0 | 1 | `+69 / -27` | Single `RequestPredicates.Visitor` bridge |
| `microsphere-spring-webmvc` | 1 | 3 | 20 (+1 `R`) | `+308 / -445` | `WebMvcConfigurerAdapter` shim vs ported router-function machinery |

## 5. `microsphere-spring-context`

### 5.1 Classes that exist only in `0.1.39`

| File (path under `src/main/java`) | Status | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/beans/factory/ObjectProviderUtils.java` | `A` (333 lines) | absent; zero references anywhere in the module | Static facade over `ObjectProvider`: `getIfAvailable`×2, `ifAvailable`, `getIfUnique`×2, `ifUnique`, `iterator`, `stream`, `orderedStream`. `iterator()` does `if (objectProvider instanceof Iterable)` then casts, else `throw new UnsupportedOperationException(...)` | **API compat** — Spring 4.3's `ObjectProvider` implements neither `Iterable` nor the `stream()`/`orderedStream()` methods that arrived in 5.x |
| `io/microsphere/spring/beans/factory/config/InstantiationAwareBeanPostProcessorAdapter.java` | `A` (132 lines) | absent (Spring removed this adapter in 6.0) | `implements SmartInstantiationAwareBeanPostProcessor` with no-op defaults for every hook, incl. `determineBeanType` (`@since Spring Framework 6.0`) and `postProcessProperties` (5.1) | **API compat** — provides the base class the Spring 4.3/5.x extension points expect |
| `io/microsphere/spring/beans/factory/support/BeanDefinitionBuilderUtils.java` | `A` (87 lines) | absent | `genericBeanDefinitionBuilder(Class<T>, Supplier<T>)` tries `setInstanceSupplier(...)` and returns a `boolean spring5AndAbove`; otherwise registers a `DelegatingFactoryBean` with `addConstructorArgValue(bean)` | **API compat** — `AbstractBeanDefinition#setInstanceSupplier` does not exist in Spring 4.3 |
| `io/microsphere/spring/context/event/DeferredApplicationEventPublisher.java` | `A` (198 lines) | absent | Buffers `ApplicationEvent`s in a `ConcurrentLinkedQueue` and replays them on `ContextRefreshedEvent`; invokes `ApplicationEventPublisher#publishEvent(Object)` reflectively (Spring 4.2+). Already marked **`@Deprecated`** at `:55` on `0.1.39` | **Dead API** — no main-code caller on either tag; only its own test uses it |

### 5.2 Inheritance and signature changes driven by the adapter

| File | Status | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/beans/factory/config/AnnotatedInjectionBeanPostProcessor.java` | `M` | `implements SmartInstantiationAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, PriorityOrdered, …` | `extends InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor, …` | **API compat** |
| `io/microsphere/spring/beans/factory/config/EventPublishingBeanBeforeProcessor.java`<br/>`…/EventPublishingBeanAfterProcessor.java` | `M` | `implements InstantiationAwareBeanPostProcessor` | `extends InstantiationAwareBeanPostProcessorAdapter` | **API compat** |
| `…/EventPublishingBeanAfterProcessor.java` (`requiresDestruction`) | `M` | keeps `@Override` | drops `@Override`, keeps the `@since Spring Framework 4.3` Javadoc; body still `return true;` | **API compat** (the hook's signature differs across 4.3/5.x) |
| `io/microsphere/spring/context/annotation/AnnotatedBeanCapableImportBeanDefinitionRegistrar.java` | `M` | uses `FullyQualifiedAnnotationBeanNameGenerator.INSTANCE` (Spring 5.2.3+) | declares its own `public static final AnnotationBeanNameGenerator INSTANCE`, comments out `@Override` | **API compat** |
| `io/microsphere/spring/core/io/ResourceUtils.java` | `M` | `resource instanceof FileUrlResource` | `FILE_URL_RESOURCE_CLASS = resolveClass("org.springframework.core.io.FileUrlResource")` + `isInstance(...)`; `@since Spring Framework 5.0.2` | **API compat** — `FileUrlResource` only exists from 5.0.2 |

### 5.3 Method-handle bridging inside modified classes

| File | Δ | What `0.1.39` adds | What `0.2.39` keeps | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/beans/factory/config/BeanDefinitionUtils.java` | `+307` | Handles `GET_RESOLVABLE_TYPE_`, `SET_INSTANCE_SUPPLIER_`, `GET_INSTANCE_SUPPLIER_` + `isXxxMethodPresent()` probes; `getResolvableType(...)`×2, `set/getInstanceSupplier(...)`, and a `doGetResolvableType(...)` fallback (`getResolvedFactoryMethod() != null → forMethodReturnType(...)`, else `hasBeanClass() ? forClass : NONE`) | Calls Spring getters directly | **API compat** |
| `…/BeanDefinitionUtils#resolveBeanType` | signature | Parameter narrowed to `AbstractBeanDefinition` | Parameter is `BeanDefinition` | **API compat** — the bridge can only read `resolvableType`/`instanceSupplier` from `AbstractBeanDefinition`; ripples into `BeanFactoryUtils.getBeanClass` (adds `if (beanDefinition instanceof AbstractBeanDefinition)`), `GenericBeanNameGenerator` (extra cast), `DefaultBeanDependencyResolver` and `DependencyAnalysisBeanFactoryListener` (call the bridged static facades instead of the Spring getters) |
| `io/microsphere/spring/beans/BeanUtils.java` | small | `FIND_PRIMARY_CONSTRUCTOR_METHOD_HANDLE = findStatic(org.springframework.beans.BeanUtils.class, "findPrimaryConstructor", Class.class)` and a `findPrimaryConstructor(...)` that returns `null` when the handle is missing | Calls `org.springframework.beans.BeanUtils.findPrimaryConstructor` (Spring 5.2) | **API compat** |
| `io/microsphere/spring/beans/BeanUtils.java` (`invokeAwareInterfaces`) | visibility | package-private | **`public`** | **Feature** — 0.2.x exposes it as API |
| `io/microsphere/spring/beans/factory/support/BeanRegistrar.java` | `M` | `if (setInstanceSupplier(beanDefinition, () -> bean)) { … } else { registerFactoryBean(registry, beanName, bean, primary); }`; the `registerBeanDefinition(..., Class, boolean)` overload builds `genericBeanDefinition` + `setPrimary` by hand | `builder.setPrimary(primary)` overload | **API compat** |
| `io/microsphere/spring/beans/factory/support/ListenableAutowireCandidateResolver.java` | `+65` | Bridges `AutowireCandidateResolver#isRequired` (fallback `descriptor.isRequired()`), `#hasQualifier` (fallback `return false`), `#cloneIfNecessary` (fallback `instantiateClass(delegate.getClass())`); removes `@Override` with the comment "…commented for compatibility with older versions" | Straight overrides | **API compat** |
| `io/microsphere/spring/core/env/ListenableConfigurableEnvironment.java` | `M` | Bridges `Environment#acceptsProfiles(Profiles)` (5.1; fallback throws `"...not supported before Spring Framework 5.1"`), `matchesProfiles(String[])` (fallback `acceptsProfiles(profileExpressions)`), `PropertyResolver#getPropertyAsClass`; adds `PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-environment."` and `ENABLED_PROPERTY_NAME` annotated `@ConfigurationProperty(type = boolean.class, defaultValue = "false", …)` | `setEscapeCharacter(Character)` bridging `ConfigurablePropertyResolver#setEscapeCharacter` (6.2); `getPropertyAsClass` simply `throw new UnsupportedOperationException();` | **API compat** + see defect below |
| `io/microsphere/spring/context/event/InterceptingApplicationEventMulticasterProxy.java` | `M` | Probes `removeApplicationListeners(Predicate)` / `removeApplicationListenerBeans(Predicate)` (Spring 5.3.5+) and returns silently when absent; `@Override` removed ×2 | Calls them directly | **API compat** |
| `io/microsphere/spring/util/MimeTypeUtils.java` | `-34` | — | Nested `public static class SpecificityComparator<T extends MimeType> implements Comparator<T>` | **API compat (inverse)** — 0.1.x inherits `MediaType.SPECIFICITY_COMPARATOR`, which Spring 7 deletes, so 0.2.x must own the comparator; `GenericMediaTypeExpression.java:26` on 0.1.39 still does `import static org.springframework.http.MediaType.SPECIFICITY_COMPARATOR` |
| `io/microsphere/spring/core/SpringVersion.java` | `M` | Enum range `SPRING_3_2` … `SPRING_5_3_39` (199 constants); stray `CURRENT(of(...));;` | Enum range `SPRING_6_0` … `SPRING_7_0_8` (78 constants) | **API compat** — each line enumerates only its supported window |

### 5.4 Files whose only diff is JDK syntax or namespace (excluded from the analysis)

`DelegatingFactoryBean`, `AnnotationBeanDefinitionRegistryPostProcessor`,
`ResolvableDependencyTypeFilter`, `GenericAnnotationAttributes`,
`EnableSpringConverterAdapterRegistrar`, `EnvironmentUtils`, `PropertySourcesUtils`,
`ResourceLoaderUtils`, `DefaultPropertiesPropertySourceLoader`,
`InterceptingApplicationListener` — pattern-matching `instanceof`, `var`, `List.of` vs
`Arrays.asList`, `.toList()` vs `.collect(toList())`, and `javax`↔`jakarta` imports only.

Comment-only: `DefaultPropertiesPropertySources`, `ResourcePropertySource`,
`ResourcePropertySources` ("Example Usage" → "Examples Usage"), `EventPublishingBeanInitializer`,
`AnnotationUtils`.

### 5.5 Defect observed on `0.2.39`

`ListenableConfigurableEnvironment#setEscapeCharacter` (lines 309-321) places
`throw new UnsupportedOperationException(...)` **outside** the
`if (SET_ESCAPE_CHARACTER_METHOD_HANDLE != null)` guard, so it throws even after a successful
bridged invocation. The 0.1.x line does not carry this method at all, so the diff surfaces the bug
rather than a divergence of intent.

## 6. `microsphere-spring-web`

| File | Status | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/web/metadata/WebEndpointMappingVisitor.java` | `A` (+114) | absent | Visitor interface over the endpoint-mapping tree: 12 callbacks (`method`, `path`, `header`, `queryParam`, `startAnd`/`and`/`endAnd`, `startOr`/`or`/`endOr`, `startNegate`/`endNegate`) | **Dead API** — `git grep` on `0.1.39` matches only its own declaration; nothing wires it |
| `io/microsphere/spring/web/util/MediaTypeUtils.java` | `D` (53 lines) | Owns `SPECIFICITY_COMPARATOR` as `new SpecificityComparator<>() { … }` built on `io.microsphere.spring.util.MimeTypeUtils.SpecificityComparator` | absent | **API compat** — exists only because Spring 7 removes `MediaType.SPECIFICITY_COMPARATOR` |
| `io/microsphere/spring/web/rule/WebRequestProducesRule.java` | `M` (+18 / -0) | Plain `resolveMediaTypes(...)` result | Adds `BEFORE_SPRING_505 = CURRENT.isLessThan(SPRING_5_0_5)` and a `MEDIA_TYPE_ALL_LIST = ofList(ALL)` fallback used when `resolveMediaTypes` returns empty | **API compat** — pre-5.0.5 produces no media types |
| `io/microsphere/spring/web/metadata/WebEndpointMapping.java` | `M` (16/16) | — | — | **Namespace** only: Javadoc `javax.servlet`/`javax.portlet` ↔ `jakarta.*`; no code change |
| `io/microsphere/spring/web/metadata/WebRequestPattensRule.java` | `M` | — | — | **Javadoc** only |
| `io/microsphere/spring/web/helper/SpringWebHelper.java`, `io/microsphere/spring/web/util/WebRequestUtils.java` | `M` | After namespace normalisation, no signature difference | same | **Namespace** only |
| `io/microsphere/spring/web/registry/CompositeWebEndpointMappingRegistry.java` | `M` | `.toList()` | `.collect(toList())` | **JDK syntax** (excluded) |

Remaining modified files in this module are namespace/JDK-syntax rewrites.

## 7. `microsphere-spring-webflux`

Single modified main file, `+69 / -27`:

| File | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|
| `io/microsphere/spring/webflux/function/server/RequestPredicateVisitorAdapter.java` | `interface … extends RequestPredicates.Visitor` with 13 `default @Override` methods plus `version(String)` (`@since Spring Framework 7.0`) | `interface … extends InvocationHandler`; resolves `VISITOR_CLASS = resolveClass("org.springframework.web.reactive.function.server.RequestPredicates$Visitor")` and `ACCEPT_METHOD = findMethod(RequestPredicate.class, "accept", VISITOR_CLASS)`, exposes `isVisitorSupported()`, `getProxy()`, `invoke(...)`; `visit(p)` forks to `RequestPredicateKind.acceptVisitor(p, this)` when the visitor is unsupported | **API compat** — `RequestPredicates.Visitor` exists only from Spring 5.1; the 0.1.x line has to support 4.3 |

`io/microsphere/spring/webflux/function/server/RequestPredicateKind.java` is **byte-identical** on
both tags, which confirms the fork lives entirely in the adapter.

## 8. `microsphere-spring-webmvc`

### 8.1 Added / removed files

| File | Status | Note | Reason |
|---|---|---|---|
| `io/microsphere/spring/webmvc/config/WebMvcConfigurerAdapter.java` | `A` (195 lines) | `interface … extends WebMvcConfigurer` with 19 empty default methods; wired into `WebMvcExtensionConfiguration`, `ConfigurableContentNegotiationManagerWebMvcConfigurer` and `InterceptingHandlerMethodProcessor` (which on `0.2.39` simply `implements WebMvcConfigurer`) | **API compat** — Spring 6 deleted the concrete `WebMvcConfigurerAdapter` class, 0.1.x needs an adapter shape that is optional-method-free on Spring 4.3 |
| `io/microsphere/spring/web/servlet/function/ConsumingWebEndpointMappingAdapter.java` | `D` (193 lines) | 0.2.x-only port of the webflux visitor machinery to Servlet functional endpoints; keeps a `ThreadLocal<LinkedList<…Builder>>` stack | **Feature** — Servlet functional-endpoint introspection (5.2+ API) is a 0.2.x capability |
| `io/microsphere/spring/web/servlet/function/RequestPredicateVisitorAdapter.java` | `D` (105 lines) | Declares `version(String)` marked `@since Spring Framework 7.0` | **Feature** |
| `io/microsphere/spring/web/servlet/function/RouterFunctionVisitorAdapter.java` | `D` (70 lines) | Marks `attributes(Map)` for 5.3.x compatibility | **Feature** |
| `META-INF/services/…ServletContainerInitializer` | `R100` | Content identical; only the key changes from `jakarta.servlet.ServletContainerInitializer` to `javax.servlet.…` | **Namespace** |

### 8.2 Modified files with substance

| File | Δ | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/webmvc/metadata/RequestMappingMetadataWebEndpointMappingFactory.java` | `+36` | `RequestMappingInfo#getPatternValues()` (5.3+) | Same call behind a handle, fallback `getPatternsCondition().getPatterns()` | **API compat** |
| `io/microsphere/spring/webmvc/metadata/HandlerMappingWebEndpointMappingResolver.java` | `-13` | Has `resolveFromRouterFunctionMapping()` | Absent | **Feature** — 0.2.x resolves router functions in MVC |
| `io/microsphere/spring/webmvc/interceptor/AbstractPageRenderContextHandlerInterceptor.java` | `M` | `implements HandlerInterceptor` (`:20`) | `extends HandlerInterceptorAdapter` (`:20`) | **API compat** — default methods on `HandlerInterceptor` only from 5.0 |
| `io/microsphere/spring/webmvc/util/ViewUtils.java#getResponseStatus` | `M` | Returns `HttpStatusCode` (`:50`) | Returns `HttpStatus` (`:50`) | **API compat** — `HttpStatusCode` is a Spring 6 abstraction |
| `io/microsphere/spring/webmvc/handler/ReversedProxyHandlerMapping.java` | `M` | Reuses the `isAbstractHandlerMapping()` gate when filling the cache | Inlines the source-type check instead | **Feature/refactor** — 0.2.x tightened the gate to also require `Kind.WEB_FLUX`/`WEB_MVC` |

The other modified files in this module differ only by namespace, Javadoc or JDK syntax.

## 9. `microsphere-spring-test`

| Area | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|
| Deleted fixtures `io/microsphere/spring/test/webmvc/PersonHandler.java`, `…/webmvc/RouterFunctionTestConfig.java` | Present | Absent | **Feature** — 0.1.x ships no WebFlux/router-function test scaffolding |
| `io/microsphere/spring/test/webmvc/AbstractWebMvcTest.java` | `@SpringJUnitConfig({TestController, RouterFunctionTestConfig})`, `protected` hooks, `testUpdatePerson()` exercising `PUT /test/person/{id}` | `@RunWith(SpringRunner.class)` + `public` methods, no `testUpdatePerson` | **Test-framework / Feature** |
| `io/microsphere/spring/test/webflux/AbstractWebFluxTest.java` | Aggregate `testWebEndpoints()` at `:77` | No aggregate; has an extra `testView()` | **Feature** |
| `io/microsphere/spring/test/webmvc/RouterFunctionTestConfig.java` | Imports the constants from `WebTestUtils` | Re-declares them | **API compat** — keeps the fixture self-contained on the older line |
| Embedded-Tomcat deployment logic | Unchanged | Unchanged | 10 of 14 modified main files are pure `jakarta.servlet` ↔ `javax.servlet`; the deployment lines (`37/59/101/179/203/213/223/227`) are identical, and neither tag uses Servlet 6-only APIs (`getAllTlds`, `SessionCookieConfig` are absent on both) |

`WebTestUtils` has zero diff.

## 10. `microsphere-spring-guice`, `microsphere-spring-jdbc`, `microsphere-spring-dependencies`, `microsphere-spring-parent`

| Module | `src/main` result |
|---|---|
| `microsphere-spring-guice` | **No diff.** The Guice bridge is identical; only the test sources and the JUnit artifact id in the POM differ (`junit-jupiter` at `pom.xml:58-59` on 0.2.39 ↔ `junit` at `:51-52` on 0.1.39) |
| `microsphere-spring-jdbc` | **No diff.** Same P6Spy `DataSource` decoration, P6Spy `3.9.1` on both |
| `microsphere-spring-dependencies` | **Byte-identical BOM.** `microsphere-spring-compatible` is absent from it on both tags even though `0.1.39` builds that module |
| `microsphere-spring-parent` | No `src/main`; differs only in `<java.version>`, namespace BOM import and the Spring/Reactor profile matrix (§2) |

## 11. Reading the diff

Three mechanisms account for nearly all substantive 0.1.x main-source code:

1. **Capability probing with fallback.** `MethodHandle`/`reflect` look-ups
   (`findVirtual`, `findStatic`, `findDeclaredMethod`, `resolveClass`, `findMethod`) decide at class
   initialisation whether a newer Spring API exists, and every call site branches on that result.
   `@since Spring Framework x.y` Javadoc markers record which version each bridge needs.
2. **Re-declared Spring shims.** Where a base class or constant disappeared (or had not yet been
   added) — `InstantiationAwareBeanPostProcessorAdapter`, `WebMvcConfigurerAdapter`,
   `MediaType.SPECIFICITY_COMPARATOR`, `ObjectProvider` stream/iterator — the library owns its copy.
3. **Removal of `@Override`.** Overriding a method that may not exist in the oldest supported
   Spring breaks compilation, so 0.1.x deletes the annotation (sometimes leaving the explanatory
   comment in place).

Conversely, the 0.2.x-only additions (`web/servlet/function/*`,
`HandlerMappingWebEndpointMappingResolver#resolveFromRouterFunctionMapping`, `MediaTypeUtils`) are
features that require Spring 5.2+/6/7 APIs and were therefore never back-ported.

## 12. Related documents

- [test-diff-0.2.39-vs-0.1.39.md](./test-diff-0.2.39-vs-0.1.39.md) — the `src/test` counterpart
- [简体中文 version](./zh/main-diff-0.2.39-vs-0.1.39.md)
- [Documentation index](../../README.md)
