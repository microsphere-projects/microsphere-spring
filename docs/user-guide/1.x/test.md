# microsphere-spring-test · 1.x

[← 1.x overview](./README.md) · [简体中文](./zh/test.md) · [Developer guide](../../developer-guide/1.x/test.md) · [0.2.x docs](../current/test.md)

## Version information

| Item | Value |
|------|-------|
| artifactId | `microsphere-spring-test` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version in development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility range | **4.3.x – 5.3.x** |
| Servlet namespace | **`javax.servlet`** |
| Embedded container | `tomcat-embed-core` **9.0.119** (`optional`) |
| Test frameworks | JUnit 4 (`junit`, `optional`) and JUnit 5 (`junit-jupiter`, `optional`) **coexist** |

> The key point: almost every dependency of this module is `optional`. After adding it, you must **supply yourself** the extra dependencies listed below, according to the features you actually use:

| Feature | Extra dependencies to add |
|---------|---------------------------|
| Any capability at all | `microsphere-java-test`, `microsphere-logging-test`, `microsphere-logback` (these three are `compile` and do propagate) |
| Embedded database | `org.xerial:sqlite-jdbc` or `com.h2database:h2`, plus `spring-jdbc` |
| Embedded Tomcat | `org.apache.tomcat.embed:tomcat-embed-core`, `javax.servlet-api`, `spring-webmvc`, `spring-test` |
| WebFlux testing | `spring-webflux` (→ **Spring 5.0+**), `reactor-test` (your own), `junit-jupiter` |
| Embedded ZooKeeper | `org.apache.zookeeper:zookeeper`, `org.apache.curator:curator-test`, `curator-recipes` |
| JSON assertions | `org.skyscreamer:jsonassert` |

## 1. `@EnableEmbeddedDatabase`: embedded SQLite / H2

`io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase`:

```java
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(EmbeddedDataBaseBeanDefinitionRegistrar.class)
@Repeatable(EnableEmbeddedDatabases.class)
public @interface EnableEmbeddedDatabase {

    EmbeddedDatabaseType type() default SQLITE;

    int port() default -1;

    String dataSource();            // required

    boolean primary() default false;

    String[] properties() default {};
}
```

| Attribute | Default | Description |
|-----------|---------|-------------|
| `type` | `SQLITE` | the enum `EmbeddedDatabaseType { SQLITE, H2 }` |
| `dataSource` | **required** | the **name** (not the type) of the `DataSource` bean to create |
| `primary` | `false` | whether to mark it as primary |
| `properties` | `{}` | additional `key=value` properties |
| `port` | `-1` | ⚠️ **never read in the source** — `git grep` shows no reference outside the declaration. Setting it has no effect whatsoever |

```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JdbcTestConfig.class)
@EnableEmbeddedDatabase(dataSource = "testDataSource")     // in-memory SQLite database
public class JdbcTest {

    @Autowired @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    public void testConnection() throws SQLException {
        assertNotNull(dataSource.getConnection());
    }
}
```

Switching to H2:

```java
@EnableEmbeddedDatabase(
        type = EmbeddedDatabaseType.H2,
        dataSource = "h2DataSource",
        primary = true,
        properties = {"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"}
)
```

When you need several databases, use repeatability via the `@EnableEmbeddedDatabases` container annotation (note the `DataBase` spelling in the class names: `EmbeddedDataBaseBeanDefinitionRegistrar` / `EmbeddedDataBaseBeanDefinitionsRegistrar`).

## 2. `@EmbeddedTomcatConfiguration`: real Tomcat + Spring MVC

This is a **class-level composed annotation** that already carries the JUnit 4 runner and context configuration itself:

```java
@Target(TYPE) @Retention(RUNTIME) @Documented @Inherited
@RunWith(SpringRunner.class)
@BootstrapWith(EmbeddedTomcatTestContextBootstrapper.class)
@ContextConfiguration
@WebAppConfiguration
public @interface EmbeddedTomcatConfiguration {

    int port() default 8080;
    String contextPath() default "";
    String basedir() default "${java.io.tmpdir}";
    @AliasFor(annotation = WebAppConfiguration.class, attribute = "value")
    String docBase() default "classpath:/webapp";
    String alternativeWebXml() default "";
    Feature[] features() default {};
    @AliasFor(annotation = ContextConfiguration.class) Class<?>[] classes() default {};
    @AliasFor(annotation = ContextConfiguration.class) String[] locations() default {};
    @AliasFor(annotation = ContextConfiguration.class)
    Class<? extends ApplicationContextInitializer<?>>[] initializers() default {};
    @AliasFor(annotation = ContextConfiguration.class) boolean inheritLocations() default true;
    @AliasFor(annotation = ContextConfiguration.class) boolean inheritInitializers() default true;

    enum Feature { NAMING, DEFAULT_WEB_XML, WEB_APP_DEFAULTS, USE_TEST_CLASSPATH, SILENT; }
}
```

| Attribute | Default | Notes |
|-----------|---------|-------|
| `port` | `8080` | the port actually listened on |
| `contextPath` | `""` | deployed at the root path |
| `basedir` | `"${java.io.tmpdir}"` | a **placeholder string**, resolved at runtime from the environment |
| `docBase` | `"classpath:/webapp"` | `@AliasFor` with `@WebAppConfiguration`'s `value` — setting either side is enough |
| `alternativeWebXml` | `""` | the alternative web.xml passed to Tomcat |
| `features` | `{}` | the set of `Feature` switches, see the table below |
| `classes` / `locations` / `initializers` / `inheritLocations` / `inheritInitializers` | same as `@ContextConfiguration` | all `@AliasFor(ContextConfiguration.class)` |

Each `Feature` maps onto a Tomcat builder option:

| Value | Effect |
|-------|--------|
| `NAMING` | `tomcat.enableNaming()`, enabling JNDI/Naming support |
| `DEFAULT_WEB_XML` | `tomcat.setAddDefaultWebXmlToWebapp(true)` |
| `WEB_APP_DEFAULTS` | `initWebappDefaults(context)` — registers the default servlet/JSP and related components for the webapp |
| `USE_TEST_CLASSPATH` | applies the test class's ClassLoader via `context.setParentClassLoader(...)`, so the webapp can load classes from the test classpath |
| `SILENT` | turns off Tomcat's own log output |

```java
@EmbeddedTomcatConfiguration(
        port = 8999,
        contextPath = "/api",
        features = {Feature.NAMING, Feature.USE_TEST_CLASSPATH},
        classes = {WebAppConfig.class, TestController.class}
)
public class EmbeddedTomcatIT {

    @Autowired
    private ApplicationContext context;

    @Test
    public void hello() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(
                    new HttpGet("http://localhost:8999/api/test/helloworld"));
            assertThat(resp.getStatusLine().getStatusCode(), is(200));
        }
    }
}
```

> ⚠️ Visibility trap: among the companions, **`EmbeddedTomcatContextLoader` is a package-private class** (`class EmbeddedTomcatContextLoader extends AbstractGenericContextLoader`), while `EmbeddedTomcatTestContextBootstrapper` is not. So never try to reference it in `@ContextConfiguration(loader = EmbeddedTomcatContextLoader.class)` — always use `@EmbeddedTomcatConfiguration`, which performs the wiring through `@BootstrapWith`.

## 3. `@EmbeddedZookeeperServer`

```java
@Target(TYPE) @Retention(RUNTIME) @Documented @Inherited
@RunWith(SpringRunner.class)
@TestExecutionListeners(
        listeners = EmbeddedZookeeperServerTestExecutionListener.class,
        mergeMode = MERGE_WITH_DEFAULTS)
public @interface EmbeddedZookeeperServer {

    int port() default 2181;

    String dataDir() default "${java.io.tmpdir}/test-zookeeper-${port}";

    long tickTime() default 2000L;
}
```

- `dataDir` supports nested `${port}` placeholders (both the `-` and the `${port}` are part of the default value).
- `mergeMode = MERGE_WITH_DEFAULTS` keeps Spring's default listener chain, so `@Autowired` injection keeps working as usual.
- Requires `zookeeper` + `curator-test`/`curator-recipes` (all optional).

## 4. Abstract test base classes: JUnit 4 and JUnit 5 mixed

This is where 1.x and 0.2.x differ most easily:

| Base class | Test framework | Meta-annotations | Notes |
|------------|----------------|------------------|-------|
| `test.webmvc.AbstractWebMvcTest` | **JUnit 4** | `@Ignore @RunWith(SpringRunner.class) @WebAppConfiguration @ContextConfiguration(classes = TestController.class) @EnableWebMvc` | because `@Ignore` sits on the class, **subclasses must explicitly override/call** its test methods for them to run |
| `test.webflux.AbstractWebFluxTest` | **JUnit 5** | `@Disabled @SpringJUnitConfig @ContextConfiguration(classes = {TestController.class, RouterFunctionTestConfig.class}) @EnableWebFlux` | `@Disabled` behaves the same way; `init()` is declared `final` (`@BeforeEach`) and cannot be overridden |

Fields and methods provided by `AbstractWebMvcTest`:

```java
@Autowired protected ConfigurableWebApplicationContext context;
@Autowired protected TestController testController;
protected MockMvc mockMvc;

@Before public void setUp() { this.mockMvc = webAppContextSetup(this.context).build(); }

public void testWebEndpoints()      // calls the 5 methods below in turn
public void testHelloWorld()        // GET /test/helloworld
public void testGreeting()          // GET /test/greeting/{message}
public void testUser()
public void testError()
public void testResponseEntity()
```

Subclass usage (JUnit 4):

```java
public class MyMvcTest extends AbstractWebMvcTest {

    @Test
    @Override
    public void testWebEndpoints() throws Exception {
        super.testWebEndpoints();
    }

    @Test
    public void customEndpoint() throws Exception {
        mockMvc.perform(get("/my/endpoint")).andExpect(status().isOk());
    }
}
```

`AbstractWebFluxTest` exposes `webTestClient` (built in `@BeforeEach` by `buildWebTestClient(context)`) and a set of `protected void testXxx()` assertion methods, covering both annotated controllers and `RouterFunction`s (`RouterFunctionTestConfig`, `PersonHandler`).

## 5. Test support classes

| Class | Purpose |
|-------|---------|
| `test.web.controller.TestController` | provides the endpoints asserted by the base classes, such as `/test/helloworld` and `/test/greeting/{message}` |
| `test.webflux.RouterFunctionTestConfig` / `PersonHandler` / `SimpleUrlHandlerMappingTestConfig` | WebFlux-side functional and handler-mapping configuration samples (`webmvc` has its own class with the same name, `SimpleUrlHandlerMappingTestConfig`) |
| `test.web.servlet.TestServlet` / `TestFilter` / `TestServletRegistration` / `TestFilterRegistration` / `TestServletContext` / `TestServletContextListener` | test Servlets/Filters fed directly to `ServletContext` registration, plus a programmatic `ServletContext` implementation |
| `test.web.context.request.MockServletWebRequest` | a Mock implementation of `NativeWebRequest`, convenient for unit-testing the interceptors in [web.md](./web.md) |
| `test.domain.User` | general-purpose test entity |

Utilities:

| Class | Key members |
|-------|-------------|
| `test.util.SpringTestUtils` | `testInSpringContainer(ThrowableConsumer<ConfigurableApplicationContext>, Class<?>...)` and an overload taking a `ConfigurableEnvironment` — anonymous creation, `refresh()` and `close()` all handled for you |
| `test.util.SpringTestWebUtils` | `createWebRequest()`, `createWebRequest(Consumer<MockHttpServletRequest>)`, `createWebRequest(String requestURI)`, `createWebRequestWithParams(Object...)`, `createWebRequestWithHeaders(Object...)` / `(Map)`, `createPreFightRequest()`, `clearAttributes(NativeWebRequest[, scope])`; constant `PATH_ATTRIBUTE` |
| `test.util.ServletTestUtils` | `addTestServlet(ServletContext)`, `addTestFilter(ServletContext)`, returning `*Registration.Dynamic` |
| `test.web.WebTestUtils` | agreed test constants: `TEST_ROOT_PATH = "/test"`, `PERSON_PATH = "/person"`, `ATTRIBUTE_NAME`/`NOT_FOUND_ATTRIBUTE_NAME`/`HEADER_NAME`/`HEADER_NAME_2` (multi-valued), `PARAM_NAME`/`PARAM_NAME_2`, and so on |
| `test.junit.jupiter.SpringLoggingTest` | JUnit 5 extension annotation that adjusts log levels at runtime (pairs with `microsphere-logging-test`) |

```java
// test NativeWebRequest-related logic without starting a context
NativeWebRequest request = SpringTestWebUtils.createWebRequestWithHeaders(
        mapOf("X-Tenant", "acme", "X-Trace-Id", "abc"));

// throwaway container to check whether a configuration can load
SpringTestUtils.testInSpringContainer(ctx ->
        assertNotNull(ctx.getBean(MyService.class)), MyConfig.class);
```

## 6. Summary of known limitations

1. `@EnableEmbeddedDatabase(port = ...)` is **completely ineffective** (never read in the source).
2. `EmbeddedTomcatContextLoader` is package-private and cannot be specified manually as a `loader`.
3. `AbstractWebMvcTest` uses JUnit 4 while `AbstractWebFluxTest` uses JUnit 5 — do not expect the two styles to be interchangeable within one test class.
4. The WebFlux test base class requires Spring **5.0+** (`spring-webflux` does not exist in 4.3).
5. The vast majority of dependencies are `optional`; when one is missing you get a `NoClassDefFoundError` instead of a clear error message.
