[← Module guides](./README.md)

# microsphere-spring-test

Testing utilities used by this project's own tests and recommended for consumer tests. Base package
`io.microsphere.spring.test`. No public interfaces — the stable API is the annotations, the `Abstract*` bases, and the
`*Utils` helpers. `src/main/resources` exists but is empty; everything is annotation-driven. Heavy engines
(tomcat-embed, zookeeper/curator, sqlite/h2) are optional dependencies.

## 1. Context Booting

```java
// Boot an AnnotationConfigApplicationContext and assert inside it
SpringTestUtils.testInSpringContainer(context -> {
    assertNotNull(context.getBean(MyService.class));
}, MyConfig.class);

// Or with the Environment available too
SpringTestUtils.testInSpringContainer((context, env) -> {
    assertEquals("on", env.getProperty("feature.x"));
}, MyConfig.class);
```

`SpringTestUtils` (`testInSpringContainer(ThrowableConsumer<Ctx>, Class<?>...)` and the `ThrowableBiConsumer`
overload) is the fastest way to write a focused container test without JUnit wiring.

`@SpringLoggingTest` (`junit.jupiter`) turns on Spring logging at TRACE/INFO/ERROR for a test class (meta-annotated
`@LoggingLevelsClass` from `microsphere-logging-test`).

## 2. Embedded Database — `@EnableEmbeddedDatabase`

`io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase` —
`@Target({TYPE, ANNOTATION_TYPE})`, `@Repeatable(@EnableEmbeddedDatabases)`,
`@Import(EmbeddedDataBaseBeanDefinitionRegistrar)`.

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `dataSource()` | **required** | bean name for the registered `DataSource` |
| `type()` | `EmbeddedDatabaseType.SQLITE` | `SQLITE` or `H2` |
| `port()` | `-1` | reserved for networked modes |
| `primary()` | `false` | mark the bean primary |
| `properties()` | `{}` | driver/datasource properties |

Registers a `DriverManagerDataSource` with `jdbc:sqlite::memory:` or `jdbc:h2:mem:`.

```java
@SpringBootTest
@EnableEmbeddedDatabase(dataSource = "testDataSource", type = EmbeddedDatabaseType.H2, primary = true)
class OrderRepositoryTest { }
```

Add `org.xerial:sqlite-jdbc` or `com.h2database:h2` to your test classpath.

## 3. Embedded Tomcat — `@EmbeddedTomcatConfiguration`

`io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration` — a JUnit 5 test annotation:
`@Inherited @ExtendWith(SpringExtension) @BootstrapWith(EmbeddedTomcatTestContextBootstrapper)
@ContextConfiguration @WebAppConfiguration`. It starts a **real embedded Tomcat** and grafts the resulting
`WebApplicationContext` in as the test context's parent.

| Attribute | Default | Notes |
|-----------|---------|-------|
| `port()` | `8080` | HTTP port |
| `contextPath()` | `""` | servlet context path |
| `basedir()` | `${java.io.tmpdir}` | Tomcat base dir |
| `docBase()` | `classpath:/webapp` | `@AliasFor(WebAppConfiguration.value)` |
| `alternativeWebXml()` | `""` | explicit `web.xml` to deploy |
| `features()` | `{}` | `Feature[]`: `NAMING`, `DEFAULT_WEB_XML`, `WEB_APP_DEFAULTS`, `USE_TEST_CLASSPATH`, `SILENT` |
| `classes()` / `locations()` / `initializers()` / `inheritLocations()` / `inheritInitializers()` | `{}` / `{}` / `{}` / `true` / `true` | `@AliasFor(ContextConfiguration)` |

```java
@EmbeddedTomcatConfiguration(port = 9090, docBase = "classpath:/webapp",
        features = {EmbeddedTomcatConfiguration.Feature.NAMING, EmbeddedTomcatConfiguration.Feature.SILENT})
class CheckoutEndToEndTest { /* hits http://localhost:9090/... */ }
```

Internals (package-private, do not reference): `EmbeddedTomcatContextLoader` (extends
`AbstractGenericContextLoader`, `XmlBeanDefinitionReader`, resource suffix `-context.xml`, registers a shutdown hook),
`EmbeddedTomcatMergedContextConfiguration`, `EmbeddedTomcatTestContextBootstrapper`. Requires
`org.apache.tomcat.embed:tomcat-embed-core` on the test classpath.

## 4. Embedded ZooKeeper — `@EmbeddedZookeeperServer`

`io.microsphere.spring.test.zookeeper.embedded.EmbeddedZookeeperServer` —
`@Inherited @ExtendWith(SpringExtension) @TestExecutionListeners(listeners =
EmbeddedZookeeperServerTestExecutionListener.class, mergeMode = MERGE_WITH_DEFAULTS)`.

| Attribute | Default |
|-----------|---------|
| `port()` | `2181` |
| `dataDir()` | `${java.io.tmpdir}/test-zookeeper-${port}` |
| `tickTime()` | `2000L` |

The listener (order `HIGHEST_PRECEDENCE + 5`) starts a Curator `TestingServer` in `beforeTestClass` and stops it in
`afterTestClass`, exposing it under the test attribute `microsphere:zookeeper-server`. Requires `zookeeper` (3.9.6)
and `curator-test`/`curator-recipes` (5.9.0).

## 5. Endpoint Test Bases

| Base | Annotations | Gives you |
|------|-------------|-----------|
| `test.webmvc.AbstractWebMvcTest` | `@Disabled @WebAppConfiguration @SpringJUnitConfig(TestController, RouterFunctionTestConfig) @EnableWebMvc` | `ConfigurableWebApplicationContext`, `TestController`, `MockMvc` |
| `test.webflux.AbstractWebFluxTest` | `@Disabled @SpringJUnitConfig(TestController, RouterFunctionTestConfig) @EnableWebFlux` | `ConfigurableApplicationContext`, `TestController`, `WebTestClient` (bind-to-application-context) |

Both are `@Disabled` (so Surefire skips them) and carry ready-made assertions you can call or override:
`testHelloWorld`, `testGreeting`, `testUser`, `testError`, `testResponseEntity`, `testView`, `testUpdatePerson`,
`testWebEndpoints`.

```java
class MyControllerTest extends AbstractWebMvcTest {

    @Test
    void helloWorldWorks() throws Exception {
        mockMvc.perform(get("/test/helloworld"))
               .andExpect(status().isOk());
        testWebEndpoints();   // inherited endpoint-registry assertions
    }
}
```

Fixtures that ship with the module: `TestController` (`/test/helloworld`, `/test/greeting/{message}`, `/test/user`
POST, `/test/error`, `/test/response-entity` PUT, `/test/view`), `webmvc.RouterFunctionTestConfig` +
`webmvc.PersonHandler` and their `webflux` mirrors (`personRouterFunction`,
`nestedPersonRouterFunction`, five CRUD operations), `SimpleUrlHandlerMappingTestConfig` (`BASE_PATH = "/simple"`,
`FIRST_PATH = "/simple/1"`), domain POJO `test.domain.User`.

## 6. Request Fixtures

| Helper | Methods |
|--------|---------|
| `test.util.SpringTestWebUtils` | `createWebRequest()`, `(Consumer<MockHttpServletRequest>)`, `(String uri)`, `createWebRequestWithParams(Object...)`, `createWebRequestWithHeaders(...)` (varargs and `Map` overloads), `createPreFightRequest()` (CORS OPTIONS), `clearAttributes(NativeWebRequest[, scope])`; const `PATH_ATTRIBUTE` |
| `test.web.context.request.MockServletWebRequest` | ready `ServletWebRequest` over `MockHttpServletRequest`/`Response` + `MockServletContext`; `getMockHttpServletRequest()`, `getMockHttpServletResponse()`; ctors `()` and `(ServletContext)` |
| `test.web.WebTestUtils` | `mockServerWebExchange()`, `getValue(Mono)`; path/header/param constants `TEST_ROOT_PATH`, `PERSON_PATH`, `GET_PERSON_PATH`, `HEADER_*`, `PARAM_*`, `ATTRIBUTE_*`, `AUTH_NAME/_VALUE`, `REMOTE_ADDRESS` |
| `test.util.ServletTestUtils` | `addTestServlet(ServletContext)`, `addTestFilter(ServletContext)` |
| `test.web.servlet.*` | `TestServlet` ("Hello World!", `DEFAULT_SERVLET_NAME="testServlet"`, `DEFAULT_SERVLET_URL_PATTERN="/testServlet"`), `TestFilter` (`"testFilter"`, `/testFilter`), `TestServletContextListener`, `TestServletRegistration` / `TestFilterRegistration` (in-memory `...Dynamic` records with getters), `TestServletContext` (extends `MockServletContext`, captures registrations/listeners) |

`TestServletContext` is what you need to exercise the `WebEndpointMapping` servlet/filters factories or
`ServletContainerInitializer` behavior without a container.

## 7. Testing `@Conditional` Code

- `AnnotatedTypeMetadataTestFactory` (`BeanClassLoaderAware`) — `createMethodAnnotatedTypeMetadata()` builds a
  `StandardMethodMetadata` for the calling method, so you can feed a real annotated method into a `Condition`.
- `TestConditionContext` (`ApplicationContextAware`) — a `ConditionContext` backed by a
  `ConfigurableApplicationContext` (registry, bean factory, environment, resource loader, class loader).

## 8. Choosing a Strategy

| What you are testing | Use |
|----------------------|-----|
| A bean wiring rule, a registrar, an interceptor | `SpringTestUtils.testInSpringContainer(...)` — no JUnit context cache, fastest |
| Annotation-driven behavior with a full web context | `AbstractWebMvcTest` / `AbstractWebFluxTest` |
| HTTP-visible behavior of a war-style app | `@EmbeddedTomcatConfiguration` |
| DAO/SQL | `@EnableEmbeddedDatabase` (SQLite in memory by default, H2 available) |
| Registry/coordination code | `@EmbeddedZookeeperServer` |
| Servlet-container metadata (registrations, SCIs) | `TestServletContext` + `ServletTestUtils` |
| `Condition` implementations | `AnnotatedTypeMetadataTestFactory` + `TestConditionContext` |

## 9. Gotchas

- Optional engines must be on the test classpath: tomcat-embed-core, zookeeper + curator-test, sqlite-jdbc / h2.
- `Abstract*` bases are `@Disabled` by design — remove that annotation on your subclass only by extending, never by
  editing the base.
- Curator's `TestingServer` and embedded Tomcat bind real ports; set an explicit `port` (or leave defaults in CI
  where they are known-free) rather than randomizing inside assertions.
- Several engine classes are package-private: reference the annotations, not the loaders/bootstrappers.

---
[← Module guides](./README.md)
