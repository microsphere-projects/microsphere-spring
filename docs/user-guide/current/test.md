# microsphere-spring-test · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/test.md) · [Developer guide](../../developer-guide/current/test.md) · [1.x docs](../1.x/test.md)

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-test` |
| Branch | `release` (0.2.x line) |
| Version | `0.2.39` / development `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework compatibility range | **6.0.x – 7.0.x** (`spring-test` must match the Spring version you choose) |
| Namespace | `jakarta.servlet.*` (Jakarta EE 11), Tomcat 10.1+ embedded container |
| Required dependencies | `microsphere-java-test`, `microsphere-logging-test`, `microsphere-logback` |
| Optional dependencies | `spring-context`, `spring-jdbc`, `spring-web`, `spring-webmvc`, `spring-webflux`, `spring-test`, `jakarta.servlet-api`, `tomcat-embed-core`, `sqlite-jdbc`, `h2`, `zookeeper` 3.9.5, `curator-recipes`/`curator-test` 5.9.0, `junit-jupiter`, `mockito-core`, `jackson-*`, `jsonassert`, `hamcrest`, `jakarta.websocket-api`/`-client-api` |

This module is the library's testing toolbox. **Every** test-framework and container dependency (`spring-test`, `spring-web*`, `jakarta.servlet-api`, `tomcat-embed-core`, `sqlite-jdbc`, `h2`, ZooKeeper/Curator, JUnit Jupiter, Mockito, Jackson, JSONAssert, Hamcrest) is `optional`, so add precisely the ones your tests use — only `microsphere-java-test`, `microsphere-logging-test` and `microsphere-logback` are compile-required. This is also why the other modules depend on it in `test` scope. JUnit Jupiter and Mockito versions are managed centrally by `microsphere-java-dependencies` (0.3.19).

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- add per feature -->
<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.apache.tomcat.embed</groupId><artifactId>tomcat-embed-core</artifactId><scope>test</scope></dependency>
```

---

## 1. `@EnableEmbeddedDatabase`: embedded database

`io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase` is declared as `@Target({TYPE, ANNOTATION_TYPE}) @Import(EmbeddedDataBaseBeanDefinitionRegistrar.class) @Repeatable` (container annotation `@EnableEmbeddedDatabases`). It registers a `DriverManagerDataSource` bean under the name you choose: `SQLITE` → `jdbc:sqlite::memory:`, `H2` → `jdbc:h2:mem:`.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `dataSource` | `String` | **required** | the DataSource bean name; a duplicate name throws `BeanCreationException` |
| `type` | `EmbeddedDatabaseType` | `SQLITE` | the enum `{ SQLITE, H2 }` |
| `primary` | `boolean` | `false` | whether to mark the bean `@Primary` |
| `properties` | `String[]` | `{}` | extra `key=value` pairs, `${}` placeholders supported |
| `port` | `int` | `-1` | declared in the source but **never read** by the registrar — do not rely on it |

```java
@SpringBootTest
@EnableEmbeddedDatabase(dataSource = "testDataSource", type = EmbeddedDatabaseType.H2)
class RepositoryTest {
    @Autowired private JdbcTemplate jdbcTemplate;
}
```

Requires `org.xerial:sqlite-jdbc` or `com.h2database:h2` on the test classpath (both are optional here).

---

## 2. `@EmbeddedTomcatConfiguration`: real embedded Tomcat

`io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration` is a composed annotation: `@ExtendWith(SpringExtension)` + `@BootstrapWith(EmbeddedTomcatTestContextBootstrapper)` + `@ContextConfiguration` + `@WebAppConfiguration`. The bootstrapper builds an `EmbeddedTomcatMergedContextConfiguration` internally, deploys `docBase` with `tomcat.addWebapp(...)`, calls `setAltDDName(alternativeWebXml)` when needed, then `tomcat.start()`, and exposes the resolvable `ApplicationContext`/`WebApplicationContext` to the context.

> ⚠️ `EmbeddedTomcatContextLoader` is **package-private**, so `@ContextConfiguration(loader = EmbeddedTomcatContextLoader.class)` will not compile; this annotation is the only supported entry point.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `port` | `int` | `8080` | `0` means a random port |
| `contextPath` | `String` | `""` | the context path |
| `basedir` | `String` | `"${java.io.tmpdir}"` | the Tomcat base directory |
| `docBase` | `String` | `"classpath:/webapp"` | the webapp document root, `@AliasFor` to `@WebAppConfiguration.value` |
| `alternativeWebXml` | `String` | `""` | path of the alternative deployment descriptor |
| `features` | `Feature[]` | `{}` | `enum Feature { NAMING, DEFAULT_WEB_XML, WEB_APP_DEFAULTS, USE_TEST_CLASSPATH, SILENT }` |
| `classes` / `locations` / `initializers` / `inheritLocations` / `inheritInitializers` | — | `{}` / `{}` / `{}` / `true` / `true` | `@AliasFor` to `@ContextConfiguration` |

```java
@EmbeddedTomcatConfiguration(
        port = 0,
        contextPath = "/demo",
        docBase = "classpath:/webapp",
        features = { NAMING, WEB_APP_DEFAULTS, SILENT },
        classes = WebAppConfig.class
)
class WebAppIT {
    // resolve the actually bound port from the context
}
```

Requires `tomcat-embed-core` and `jakarta.servlet-api` (both are optional here).

---

## 3. Web test base classes

Both bases are pre-wired with a web environment and annotated `@Disabled` so they never run on their own; extend them and call the inherited smoke-test methods as needed:

| Base class | Annotations | Protected fields | Prebuilt smoke tests |
|---|---|---|---|
| `test.webmvc.AbstractWebMvcTest` | `@Disabled @WebAppConfiguration @SpringJUnitConfig(classes = {TestController.class, RouterFunctionTestConfig.class}) @EnableWebMvc` | `ConfigurableWebApplicationContext context`, `TestController testController`, `MockMvc mockMvc` | `testWebEndpoints()`, `testHelloWorld()`, `testGreeting()`, `testUser()`, `testError()`, `testResponseEntity()`, `testView()`, `testUpdatePerson()` |
| `test.webflux.AbstractWebFluxTest` | `@Disabled @SpringJUnitConfig(classes = {TestController.class, RouterFunctionTestConfig.class}) @EnableWebFlux` | `ConfigurableApplicationContext context`, `TestController testController`, `WebTestClient webTestClient` (built in `@BeforeEach`) | same as above, except no `testView()` |

```java
class MyMvcIT extends AbstractWebMvcTest {
    @Test
    void helloWorldShouldWork() throws Exception {
        testHelloWorld();                 // reuse the base assertion
    }

    @Test
    void customEndpoint() throws Exception {
        mockMvc.perform(get("/products/1")).andExpect(status().isOk());
    }
}
```

Supporting fixtures: `test.web.controller.TestController`, `test.webmvc.PersonHandler` / `RouterFunctionTestConfig` / `SimpleUrlHandlerMappingTestConfig` (with same-named WebFlux counterparts), `test.domain.User`.

---

## 4. Servlet and request utilities

| Utility | Description |
|---|---|
| `test.web.servlet.TestServletContext` | extends `MockServletContext` where `addServlet/addFilter/addListener` really take effect |
| `TestServlet` / `TestFilter` / `TestServletRegistration` / `TestFilterRegistration` / `TestServletContextListener` | default names `testServlet`/`testFilter`, URL patterns `"/testServlet"`/`"/testFilter"` |
| `test.util.ServletTestUtils.addTestServlet/addTestFilter(ServletContext)` | one-line registration of the test components |
| `test.util.SpringTestWebUtils` | `createWebRequest()`, `createWebRequest(Consumer<MockHttpServletRequest>)`, `createWebRequest(String)`, `createWebRequestWithParams(Object...)`, `createWebRequestWithHeaders(...)`, `createPreFightRequest()`, `clearAttributes(...)` — builds `ServletWebRequest` instances to test code such as `HandlerMethodInterceptor` implementations that need a `NativeWebRequest` |
| `test.web.context.request.MockServletWebRequest` | gives access to the underlying `MockHttpServletRequest`/`MockHttpServletResponse` |
| `test.util.SpringTestUtils.testInSpringContainer(consumer, configClasses...)` | starts an ad-hoc container and runs assertions without any JUnit extension |
| `test.web.WebTestUtils` | `mockServerWebExchange()`, `getValue(Mono<T>)` (blocking read of a reactive value) |
| `test.context.annotation.AnnotatedTypeMetadataTestFactory` | `createMethodAnnotatedTypeMetadata()`, a mock for custom `Condition`/annotation parsing |
| `test.context.annotation.TestConditionContext` | a mock `ConditionContext` |
| `test.zookeeper.embedded.EmbeddedZookeeperServer` | annotation for an embedded ZooKeeper: `port()` defaults to `2181`, `dataDir()` to `"${java.io.tmpdir}/test-zookeeper-${port}"`, `tickTime()` to `2000L`; paired with `EmbeddedZookeeperServerTestExecutionListener`; requires `curator-test` |
| `test.junit.jupiter.SpringLoggingTest` | test annotation composing `@LoggingLevelsClass(levels = {"TRACE", "INFO", "ERROR"})` (provided by microsphere-logging) to adjust log levels per test |
