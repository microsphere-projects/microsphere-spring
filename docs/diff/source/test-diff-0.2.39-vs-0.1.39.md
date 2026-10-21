# Source Diff — `0.2.39` vs `0.1.39` (`src/test`)

**English** · [简体中文](./zh/test-diff-0.2.39-vs-0.1.39.md)

Implementation diff of the `src/test` sources of the two latest released tags of
`microsphere-spring`, module by module. The `src/main` diff lives in the companion document
[main-diff-0.2.39-vs-0.1.39.md](./main-diff-0.2.39-vs-0.1.39.md), and the platform baselines
(Java, namespace, Spring profile matrix) are recorded in its §2 — they are not repeated here.

## 1. Scope and method

| Item | Value |
|---|---|
| Tag A (left side, `-` lines) | `0.2.39` — branch `release`, version line 0.2.x, JUnit 5 |
| Tag B (right side, `+` lines) | `0.1.39` — branch `release-1.x`, version line 0.1.x, JUnit 4 |
| Diff command shape | `git diff 0.2.39 0.1.39 -- <module>/src/test` |
| Modules compared | Only Maven modules present in **both** tags (9) |
| Modules ignored | `microsphere-spring-compatible` (exists only in `0.1.39`) |
| Path filter | `src/test/java`, `src/test/resources` |

Status letters: `A` = only in `0.1.39`, `D` = only in `0.2.39`, `M` = modified,
`R###` = renamed with that similarity percentage.

Two classes of difference are excluded from the analysis by request and summarised once in §3:

- Java 17 ↔ Java 8 syntax and API differences.
- Java EE ↔ Jakarta EE (`javax.*` ↔ `jakarta.*`) differences.

What is *not* excluded, and dominates this diff, is the **test-framework downgrade** (JUnit 5 → JUnit 4),
because it changes which assertions and extensions can express a case at all.

## 2. `src/test` diff summary

| Maven module | `A` | `D` | `M` | Rename | Lines (`+` = 0.1.39 / `-` = 0.2.39) | Character |
|---|---|---|---|---|---|---|
| `microsphere-spring-context` | 3 | 2 | 121 | — | `+2412 / -2190` | 14 substantive, 107 mechanical |
| `microsphere-spring-dependencies` | 0 | 0 | 0 | — | — | No test sources |
| `microsphere-spring-guice` | 0 | 0 | 1 | — | `+8 / -8` | Pure JUnit 4 swap |
| `microsphere-spring-jdbc` | 0 | 0 | 2 | — | `+21 / -23` | JUnit 4 swap + lost logging-level scoping |
| `microsphere-spring-parent` | 0 | 0 | 0 | — | — | No test sources |
| `microsphere-spring-test` | 0 | 4 | 29 | `R100` | `+352 / -542` | Removed reactive fixtures, framework swap |
| `microsphere-spring-web` | 0 | 0 | 56 | `R051` | `+950 / -904` | 48 mechanical, 8 substantive |
| `microsphere-spring-webflux` | 0 | 0 | 9 | — | `+81 / -11` | Every change is substantive |
| `microsphere-spring-webmvc` | 2 | 3 | 34 | — | `+619 / -769` | Adapter test + removed servlet-function tests |

## 3. Mechanical transformations (recorded once)

### 3.1 JUnit 5 → JUnit 4

| `0.2.39` | `0.1.39` | Why |
|---|---|---|
| `org.junit.jupiter.api.*` | `org.junit.*` | JUnit 4 artifact (`junit` ↔ `junit-jupiter`) |
| `Assertions.assertXxx` | `Assert.assertXxx` | No `Assertions` class in JUnit 4 |
| `@BeforeEach` / `@AfterEach` | `@Before` / `@After` | Naming |
| `@ExtendWith(SpringExtension.class)` + `@SpringJUnitConfig` | `@RunWith(SpringRunner.class)` + `@ContextConfiguration` | Spring Test 4.3 has no `SpringExtension` |
| `@Disabled` | `@Ignore` | Naming |
| `@Nested`, `@ParameterizedTest` | Flattened / looped inside one method | Not available |
| `@SpringLoggingTest` / `@LoggingLevelsClass(...)` | `@ClassRule LoggingLevelsRule levels("TRACE","INFO","ERROR")` | The scoped logging-level extension is a `microsphere-logging` 0.2.x JUnit 5 feature |
| package-private test methods | `public` methods | JUnit 4 requires `public` |

The last row is the reason a JUnit-4 test file usually shows added `public` modifiers on
every test method and `@Test`-annotated helper.

### 3.2 JDK and namespace

`instanceof T t` pattern matching → explicit cast; `var` → declared type; text blocks → concatenation;
`.toList()` → `.collect(Collectors.toList())`; `List.of` → `Arrays.asList`; and `jakarta.servlet.*`
imports → `javax.servlet.*`.

## 4. `microsphere-spring-context`

Of 121 modified test files, **14 carry substance**; the other 107 are §3 transformations only.

### 4.1 Added / deleted tests

| File | Status | Content | Reason |
|---|---|---|---|
| `io/microsphere/spring/beans/factory/ObjectProviderUtilsTest.java` | `A` | 16 cases; guards with `private static final boolean isBelowSpring51 = SPRING_5_1.gt(CURRENT);` | Covers the `0.1.39`-only bridge (§5.1 of the main diff) |
| `io/microsphere/spring/beans/factory/support/BeanDefinitionBuilderUtilsTest.java` | `A` | Covers the `spring5AndAbove` fork and the `DelegatingFactoryBean` fallback | API compat |
| `io/microsphere/spring/context/event/DeferredApplicationEventPublisherTest.java` | `A` | Nulls `PUBLISH_EVENT_METHOD` to simulate Spring < 4.2 and asserts the degraded path | API compat |
| `io/microsphere/spring/beans/factory/support/DefaultBeanDependencyResolverTest.java` | `D` | Added on 0.2.x in 2025-05 | Coverage never back-ported |
| `io/microsphere/spring/context/event/ParallelPreInstantiationSingletonsBeanFactoryListenerTest.java` | `D` | Same 2025-05 0.2.x coverage wave | Never back-ported |

### 4.2 Substantive modifications

| File | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|
| `io/microsphere/spring/core/SpringVersionTest.java` | Enum table for `SPRING_6_0` … `SPRING_7_0_8` | Enum table for `SPRING_3_2` … `SPRING_5_3_39` | API compat — each line asserts its own supported window |
| `io/microsphere/spring/beans/factory/config/BeanDefinitionUtilsTest.java` | Direct getter assertions | `+8` bridge cases, incl. `assertEquals(isGESpring5, isSetInstanceSupplierMethodPresent())` | API compat |
| `io/microsphere/spring/beans/factory/BeanFactoryUtilsTest.java` | 7 separate `testAsXxx` methods | Collapsed into one `testAsMethods` | Test-framework (flattening) |
| `io/microsphere/spring/beans/BeanUtilsTest.java` | — | Extra `testFindPrimaryConstructorOnFailed` | API compat — asserts the `null` fallback of the new bridge |
| `io/microsphere/spring/util/MimeTypeUtilsTest.java` | `testSpecificityComparator` | Absent | API compat — 0.1.x has no nested `SpecificityComparator` to test |
| `io/microsphere/spring/core/annotation/AnnotationUtilsTest.java` | reads `bean.autowireCandidate()` | reads `bean.autowire()` | API compat — Spring 6 removed `@AliasFor`-paired `autowire()` from `@Bean` |
| `io/microsphere/spring/context/annotation/AnnotatedBeanDefinitionRegistryUtilsTest.java` | `testScanBasePackages` expects 2 beans | Expects 1 | Feature — 0.2.x registers an extra infra bean |
| `io/microsphere/spring/context/annotation/EnableAutoRegistrationBeanTest.java` | `testDisabledTest2Config` has **no `@Test`** → never executes | Present as-is | **Defect** on 0.2.39 (a disabled-path assertion that silently does not run) |
| `io/microsphere/spring/context/event/InterceptingApplicationListenerTest.java` | Wraps the listener directly | Wraps it in `GenericApplicationListenerAdapter` | API compat — 4.3 needs the adapter to match `ApplicationListener` semantics |
| `io/microsphere/spring/beans/factory/support/ListenableAutowireCandidateResolverTest.java` | Unconditional assertions | Version-conditional assertions on the bridged methods | API compat |
| `io/microsphere/spring/core/env/ListenableConfigurableEnvironmentTest.java` | Covers `setEscapeCharacter` | Covers `acceptsProfiles(Profiles)` / `matchesProfiles` | API compat |
| `io/microsphere/spring/core/io/ResourceUtilsTest.java` | Direct `FileUrlResource` assertions | Gated on `resolveClass(...)` availability | API compat |
| `io/microsphere/spring/beans/factory/support/BeanRegistrarTest.java` | Calls the `registerBeanDefinition(..., Class, boolean)` builder overload | Calls the hand-built variant | API compat |

## 5. `microsphere-spring-web`

56 modified test files: **48 mechanical, 8 substantive**, plus one rename that is the most
interesting single artefact in the whole diff.

| File | Status | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/web/util/MediaTypeUtilsTest.java` → `org/springframework/http/server/reactive/ServerHttpRequest.java` | `R051` | Real test keeping `testSPECIFICITY_COMPARATOR`, asserting `assertEquals(-1, SPECIFICITY_COMPARATOR.compare(audioType07, audioType03))` | An **empty placeholder class** commented "Dump class for {ServerHttpRequest} in the Spring 5.0+ WebFlux", compiled into `src/test/java` under the Spring package name | **API compat** — `spring-web 4.3.30` ships no `org.springframework.http.server.reactive` package (`0` entries vs `116` in `5.3.39`), so `mock(ServerHttpRequest.class)` would not even compile under the `4.3` profile |
| `io/microsphere/spring/web/helper/SpringWebMvcHelperTest.java` | `M` (+75/-40) | `andExpectAll(...)`, `.characterEncoding(UTF_8)`, `fromPath(t).build(map)`, `removeApplicationListener(listener)`; has `testGetServletWebRequest` | Chained `andExpect(...)`, `.characterEncoding("UTF_8")`, `buildAndExpand(map).toUri()`, dedupe via a `processedListeners` `ServletContext` attribute; `testGetServletWebRequest` deleted (`@Test` count 432 → 430) | API compat + test-framework |
| `io/microsphere/spring/web/helper/SpringWebMvcHelperTest.java` (`testGetMatrixVariables`, `testGetProducibleMediaTypes`) | `M` | Asserts `null` | Asserts empty | **API compat** — older Spring returns empty collections rather than `null` |
| `io/microsphere/spring/web/extension/EnableWebExtensionTest.java` | `M` | Declares `sources = {BEAN_FACTORY, SPRING_FACTORIES}` explicitly | Omits it | API compat — 4.3's default `@ContextConfiguration` locations already cover it |
| `io/microsphere/spring/web/context/RequestAttributesUtilsTest.java` | `M` | Uses `org.springframework.core.MethodParameter.forExecutable` (5.3+) | Uses `io.microsphere.spring.core.MethodParameterUtils.forExecutable` | API compat |

The remaining 51 files differ only by JUnit 4 translation, JDK syntax or the Servlet namespace.

## 6. `microsphere-spring-webflux`

All 9 modified test files are substantive — this module's tests encode the version gap directly.

| File | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|
| `io/microsphere/spring/webflux/AbstractEnableWebFluxExtensionTest.java` | Inherits endpoint cases from `AbstractWebFluxTest#testWebEndpoints()` (`:77`) | Inlines the six endpoint tests | Feature — the aggregate hook exists only on 0.2.x |
| `io/microsphere/spring/webflux/function/server/RequestPredicateKindTest.java` | Builds predicates only | Adds `when(request.pathContainer())…` stubs | API compat — the fallback path needs an exchange stub |
| `io/microsphere/spring/webflux/function/server/RequestPredicateVisitorAdapterTest.java` | Tests the 13 default methods | `+5` cases incl. `testVisit(boolean isVisitorSupported)` | API compat — covers both branches of the fork |
| `io/microsphere/spring/webflux/filter/CompositeWebFilterTest.java` | `new DefaultWebFilterChain(webHandler, ofList(...))` | Array overload | API compat — Spring 7 dropped the varargs/List constructor shape used by 0.2.x |
| `io/microsphere/spring/webflux/helper/SpringWebFluxHelperTest.java` | `.bodyValue(user)`; two `expectCookie().valueEquals` assertions active | `.body(just(user), User.class)`; those cookie assertions commented out; `if (CURRENT.lt(SPRING_5_3)) assertNull(path)` | API compat |
| `io/microsphere/spring/webflux/mapping/ConsumingWebEndpointMappingAdapterTest.java` | No extra annotation | Adds `@EnableWebFluxExtension` | Feature — wiring is implicit on 0.2.x |
| `io/microsphere/spring/webflux/filter/RequestContextWebFilterTest.java`<br/>`io/microsphere/spring/webflux/filter/RequestHandledEventPublishingWebFilterTest.java` | Rely on the extension | Add `@EnableWebFlux` / `@EnableWebFluxExtension` | Feature |

## 7. `microsphere-spring-webmvc`

| File | Status | Note | Reason |
|---|---|---|---|
| `io/microsphere/spring/webmvc/config/WebMvcConfigurerAdapterTest.java` | `A` (122 lines, 18 `@Test`) | Calls `WebMvcConfigurerAdapter.super.configurePathMatch(null)` and the other defaults | Covers the `0.1.39`-only shim |
| `src/test/resources/webapp/WEB-INF/dispatcherServlet-servlet.xml` | `A` | Added with commit `26225dc13`, which also changed the paired assertion | Feature — 0.1.x needs its own servlet context descriptor to prove parent/current context resolution |
| `io/microsphere/spring/webmvc/context/WebApplicationContextTest.java` (paired assertion) | `M` | `0.2.39` asserts `assertSame(wac, getRequiredWebApplicationContext(servletContext))` (`:73`); `0.1.39` asserts `assertSame(wac.getParent(), getRequiredWebApplicationContext(servletContext))` | Feature — on the older line the child context resolution lands one level up |
| `io/microsphere/spring/webmvc/handler/…ServletFunction*Test` (3 files, 22 cases) | `D` | Tests for the `web/servlet/function` machinery | Feature — that machinery is 0.2.x-only (main diff §8.1) |
| `io/microsphere/spring/webmvc/config/ConfigurableContentNegotiationManagerWebMvcConfigurerTest.java` | `M` | `0.2.39`: `assertTrue(strategies.size() > 1)`; `0.1.39`: `assertEquals(4, strategies.size())` incl. `ServletPathExtensionContentNegotiationStrategy` | API compat — Spring 6 stopped auto-registering that strategy |
| `src/test/resources/webapp/WEB-INF/web.xml` | `M` | `0.2.39`: `version="6.0"`, `jakarta.ee` schema, `metadata-complete="false"`; `0.1.39`: `version="3.1"`, `xmlns.jcp.org` schema | Namespace |
| 26 further modified files | `M` | — | JUnit 4 translation only (136 removed `jupiter` imports, 20 added `@RunWith`) |

## 8. `microsphere-spring-test`

| File | Status | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| `io/microsphere/spring/test/webflux/PersonHandlerTest.java`<br/>`io/microsphere/spring/test/webflux/WebFluxTest.java`<br/>`io/microsphere/spring/test/webflux/SimpleUrlHandlerMappingTestConfigTest.java`<br/>`io/microsphere/spring/test/webmvc/PersonHandlerTest.java` | `D` ×4 | Present | Absent | Feature — depends on the deleted `PersonHandler` / `RouterFunctionTestConfig` fixtures |
| `io/microsphere/spring/test/webmvc/AbstractWebMvcTest.java` | `M` (22/31) | `@SpringJUnitConfig({TestController, RouterFunctionTestConfig})`, `protected` hooks, `testUpdatePerson()` on `PUT /test/person/{id}` | `@RunWith(SpringRunner.class)`, `public` methods, no `testUpdatePerson` | Test-framework + Feature |
| `io/microsphere/spring/test/webflux/AbstractWebFluxTest.java` | `M` | Aggregate `testWebEndpoints()` at `:77` | No aggregate; extra `testView()` | Feature |
| `io/microsphere/spring/test/util/WebTestUtils.java` | no diff | — | — | Shared helper |
| 10 of the 14 modified main-scope test-support files | `M` | — | — | Pure `jakarta.servlet` ↔ `javax.servlet`; embedded-Tomcat deployment lines (`37/59/101/179/203/213/223/227`) are identical on both tags |
| `META-INF/services/…` | `R100` | `jakarta.servlet.ServletContainerInitializer` key | `javax.servlet.…` key | Namespace |

Neither tag uses Servlet 6-only APIs (`getAllTlds`, `SessionCookieConfig` appear in neither).

## 9. `microsphere-spring-guice`, `microsphere-spring-jdbc`

| Module | Files | `0.2.39` | `0.1.39` | Reason |
|---|---|---|---|---|
| guice | `io/microsphere/spring/beangogo/guice/extension/EnableGuiceTest.java` (±8) | JUnit 5 imports/annotations | JUnit 4 equivalents — assertions unchanged | Test-framework |
| guice | `pom.xml` | `junit-jupiter` (`:58-59`) | `junit` (`:51-52`) | Test-framework |
| jdbc | 2 tests (±21 / −23) | `@SpringLoggingTest` + `@LoggingLevelsClass(loggingClasses = P6DataSourceBeanPostProcessor.class, …)` scopes log levels to one class | Falls back to a global `@ClassRule LoggingLevelsRule` | Test-framework — a **capability regression in coverage precision**, not in production behaviour (P6Spy `3.9.1` on both) |

## 10. Reading the diff

1. **Most test volume is translation, not design.** 107/121 in `context`, 48/56 in `web`, 26/34 in
   `webmvc` and 8/9 module-changed files in `guice`/`jdbc` are the JUnit 4 translation plus the
   excluded JDK/namespace rewrites.
2. **Substantive test changes mirror main-source bridges.** Every `0.1.39`-only bridge
   (`ObjectProviderUtils`, `BeanDefinitionBuilderUtils`, `DeferredApplicationEventPublisher`,
   `WebMvcConfigurerAdapter`) arrives with a matching test, and each test either probes the
   capability (`isBelowSpring51`, `isSetInstanceSupplierMethodPresent()`) or forces the fallback
   (nulling `PUBLISH_EVENT_METHOD`).
3. **Coverage gaps are one-directional.** The two deleted `context` tests are 2025-05 additions on
   the 0.2.x line that were never back-ported, and the three deleted `webmvc` servlet-function tests
   simply track the 0.2.x-only feature — so 0.1.x never has *more* coverage than 0.2.x.
4. **Two real defects surfaced.** `EnableAutoRegistrationBeanTest#testDisabledTest2Config` lacks
   `@Test` on `0.2.39` and therefore never runs; and the empty `org.springframework.http.server.reactive.ServerHttpRequest`
   placeholder in `0.1.39` is a compile-time shim that must be removed if the `4.3` profile is dropped.

## 11. Related documents

- [main-diff-0.2.39-vs-0.1.39.md](./main-diff-0.2.39-vs-0.1.39.md) — the `src/main` counterpart and the platform baselines
- [简体中文 version](./zh/test-diff-0.2.39-vs-0.1.39.md)
- [Documentation index](../../README.md)
