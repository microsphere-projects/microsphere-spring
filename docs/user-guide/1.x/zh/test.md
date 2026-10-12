# microsphere-spring-test · 1.x

[← 1.x 版本总览](./README.md) · [English](../test.md) · [开发者文档](../../../developer-guide/1.x/zh/test.md) · [0.2.x 文档](../../current/zh/test.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-test` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | **4.3.x – 5.3.x** |
| Servlet 命名空间 | **`javax.servlet`** |
| 内嵌容器 | `tomcat-embed-core` **9.0.119**（`optional`） |
| 测试框架 | JUnit 4（`junit`，`optional`）与 JUnit 5（`junit-jupiter`，`optional`）**并存** |

> 关键在于：本模块几乎每一个依赖都是 `optional`。引入之后，你必须按自己实际用到的功能，**自行补齐**下面列出的额外依赖：

| 功能 | 需要额外添加的依赖 |
|------|--------------------|
| 任何能力（最小要求） | `microsphere-java-test`、`microsphere-logging-test`、`microsphere-logback`（这三个是 `compile` 且会传递） |
| 内嵌数据库 | `org.xerial:sqlite-jdbc` 或 `com.h2database:h2`，再加 `spring-jdbc` |
| 内嵌 Tomcat | `org.apache.tomcat.embed:tomcat-embed-core`、`javax.servlet-api`、`spring-webmvc`、`spring-test` |
| WebFlux 测试 | `spring-webflux`（→ **Spring 5.0+**）、`reactor-test`（自备）、`junit-jupiter` |
| 内嵌 ZooKeeper | `org.apache.zookeeper:zookeeper`、`org.apache.curator:curator-test`、`curator-recipes` |
| JSON 断言 | `org.skyscreamer:jsonassert` |

## 1. `@EnableEmbeddedDatabase`：内嵌 SQLite / H2

`io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase`：

```java
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(EmbeddedDataBaseBeanDefinitionRegistrar.class)
@Repeatable(EnableEmbeddedDatabases.class)
public @interface EnableEmbeddedDatabase {

    EmbeddedDatabaseType type() default SQLITE;

    int port() default -1;

    String dataSource();            // 必填

    boolean primary() default false;

    String[] properties() default {};
}
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `type` | `SQLITE` | 枚举 `EmbeddedDatabaseType { SQLITE, H2 }` |
| `dataSource` | **必填** | 要创建的 `DataSource` Bean 的**名称**（不是类型） |
| `primary` | `false` | 是否将其标记为 primary |
| `properties` | `{}` | 额外的 `key=value` 属性 |
| `port` | `-1` | ⚠️ **源码中从未被读取** —— `git grep` 显示除声明之外没有任何引用。设置它不会产生任何效果 |

```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JdbcTestConfig.class)
@EnableEmbeddedDatabase(dataSource = "testDataSource")     // 内存 SQLite 数据库
public class JdbcTest {

    @Autowired @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    public void testConnection() throws SQLException {
        assertNotNull(dataSource.getConnection());
    }
}
```

切换为 H2：

```java
@EnableEmbeddedDatabase(
        type = EmbeddedDatabaseType.H2,
        dataSource = "h2DataSource",
        primary = true,
        properties = {"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"}
)
```

需要多个数据库时，通过容器注解 `@EnableEmbeddedDatabases` 实现可重复（注意类名中 `DataBase` 的拼写：`EmbeddedDataBaseBeanDefinitionRegistrar` / `EmbeddedDataBaseBeanDefinitionsRegistrar`）。

## 2. `@EmbeddedTomcatConfiguration`：真实 Tomcat + Spring MVC

这是一个**类级组合注解**，它自身已经带上了 JUnit 4 runner 与上下文配置：

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

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `port` | `8080` | 实际监听的端口 |
| `contextPath` | `""` | 部署在根路径 |
| `basedir` | `"${java.io.tmpdir}"` | 一个**占位符字符串**，运行时从环境中解析 |
| `docBase` | `"classpath:/webapp"` | 与 `@WebAppConfiguration` 的 `value` 通过 `@AliasFor` 关联——只设置其中任意一边即可 |
| `alternativeWebXml` | `""` | 传给 Tomcat 的替代 web.xml |
| `features` | `{}` | `Feature` 开关集合，见下方表格 |
| `classes` / `locations` / `initializers` / `inheritLocations` / `inheritInitializers` | 与 `@ContextConfiguration` 相同 | 全部是 `@AliasFor(ContextConfiguration.class)` |

每个 `Feature` 对应一个 Tomcat builder 选项：

| 取值 | 效果 |
|------|------|
| `NAMING` | `tomcat.enableNaming()`，启用 JNDI/Naming 支持 |
| `DEFAULT_WEB_XML` | `tomcat.setAddDefaultWebXmlToWebapp(true)` |
| `WEB_APP_DEFAULTS` | `initWebappDefaults(context)`——为 webapp 注册默认的 servlet/JSP 及相关组件 |
| `USE_TEST_CLASSPATH` | 通过 `context.setParentClassLoader(...)` 应用测试类的 ClassLoader，使 webapp 能加载测试 classpath 上的类 |
| `SILENT` | 关闭 Tomcat 自身的日志输出 |

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

> ⚠️ 可见性陷阱：在配套类中，**`EmbeddedTomcatContextLoader` 是包私有类**（`class EmbeddedTomcatContextLoader extends AbstractGenericContextLoader`），而 `EmbeddedTomcatTestContextBootstrapper` 不是。因此不要试图在 `@ContextConfiguration(loader = EmbeddedTomcatContextLoader.class)` 中引用它——始终使用 `@EmbeddedTomcatConfiguration`，它通过 `@BootstrapWith` 完成装配。

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

- `dataDir` 支持嵌套的 `${port}` 占位符（其中的 `-` 和 `${port}` 都是默认值的一部分）。
- `mergeMode = MERGE_WITH_DEFAULTS` 保留了 Spring 默认的监听器链，因此 `@Autowired` 注入照常生效。
- 需要 `zookeeper` + `curator-test`/`curator-recipes`（全部 optional）。

## 4. 抽象测试基类：JUnit 4 与 JUnit 5 混用

这是 1.x 与 0.2.x 之间最容易出问题的差异：

| 基类 | 测试框架 | 元注解 | 说明 |
|------|----------|--------|------|
| `test.webmvc.AbstractWebMvcTest` | **JUnit 4** | `@Ignore @RunWith(SpringRunner.class) @WebAppConfiguration @ContextConfiguration(classes = TestController.class) @EnableWebMvc` | 因为 `@Ignore` 标在类上，**子类必须显式覆盖/调用**它的测试方法，这些方法才会执行 |
| `test.webflux.AbstractWebFluxTest` | **JUnit 5** | `@Disabled @SpringJUnitConfig @ContextConfiguration(classes = {TestController.class, RouterFunctionTestConfig.class}) @EnableWebFlux` | `@Disabled` 的行为相同；`init()` 被声明为 `final`（`@BeforeEach`），无法覆盖 |

`AbstractWebMvcTest` 提供的字段与方法：

```java
@Autowired protected ConfigurableWebApplicationContext context;
@Autowired protected TestController testController;
protected MockMvc mockMvc;

@Before public void setUp() { this.mockMvc = webAppContextSetup(this.context).build(); }

public void testWebEndpoints()      // 依次调用下面的 5 个方法
public void testHelloWorld()        // GET /test/helloworld
public void testGreeting()          // GET /test/greeting/{message}
public void testUser()
public void testError()
public void testResponseEntity()
```

子类用法（JUnit 4）：

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

`AbstractWebFluxTest` 暴露 `webTestClient`（在 `@BeforeEach` 中由 `buildWebTestClient(context)` 构建）以及一组 `protected void testXxx()` 断言方法，同时覆盖注解式控制器与 `RouterFunction`（`RouterFunctionTestConfig`、`PersonHandler`）。

## 5. 测试支持类

| 类 | 用途 |
|----|------|
| `test.web.controller.TestController` | 提供基类所断言的端点，例如 `/test/helloworld`、`/test/greeting/{message}` |
| `test.webflux.RouterFunctionTestConfig` / `PersonHandler` / `SimpleUrlHandlerMappingTestConfig` | WebFlux 侧的函数式与处理器映射配置示例（`webmvc` 也有同名类 `SimpleUrlHandlerMappingTestConfig`） |
| `test.web.servlet.TestServlet` / `TestFilter` / `TestServletRegistration` / `TestFilterRegistration` / `TestServletContext` / `TestServletContextListener` | 直接交给 `ServletContext` 注册的测试 Servlet/Filter，以及一个编程式的 `ServletContext` 实现 |
| `test.web.context.request.MockServletWebRequest` | `NativeWebRequest` 的 Mock 实现，便于单元测试 [web.md](./web.md) 中的拦截器 |
| `test.domain.User` | 通用测试实体 |

工具类：

| 类 | 关键成员 |
|----|----------|
| `test.util.SpringTestUtils` | `testInSpringContainer(ThrowableConsumer<ConfigurableApplicationContext>, Class<?>...)`，以及一个接收 `ConfigurableEnvironment` 的重载——匿名创建、`refresh()` 与 `close()` 全部替你处理 |
| `test.util.SpringTestWebUtils` | `createWebRequest()`、`createWebRequest(Consumer<MockHttpServletRequest>)`、`createWebRequest(String requestURI)`、`createWebRequestWithParams(Object...)`、`createWebRequestWithHeaders(Object...)` / `(Map)`、`createPreFightRequest()`、`clearAttributes(NativeWebRequest[, scope])`；常量 `PATH_ATTRIBUTE` |
| `test.util.ServletTestUtils` | `addTestServlet(ServletContext)`、`addTestFilter(ServletContext)`，返回 `*Registration.Dynamic` |
| `test.web.WebTestUtils` | 约定的测试常量：`TEST_ROOT_PATH = "/test"`、`PERSON_PATH = "/person"`、`ATTRIBUTE_NAME`/`NOT_FOUND_ATTRIBUTE_NAME`/`HEADER_NAME`/`HEADER_NAME_2`（多值）、`PARAM_NAME`/`PARAM_NAME_2` 等 |
| `test.junit.jupiter.SpringLoggingTest` | 运行时调整日志级别的 JUnit 5 扩展注解（与 `microsphere-logging-test` 配合） |

```java
// 不启动上下文，直接测试与 NativeWebRequest 相关的逻辑
NativeWebRequest request = SpringTestWebUtils.createWebRequestWithHeaders(
        mapOf("X-Tenant", "acme", "X-Trace-Id", "abc"));

// 用一次性容器检查某个配置能否加载
SpringTestUtils.testInSpringContainer(ctx ->
        assertNotNull(ctx.getBean(MyService.class)), MyConfig.class);
```

## 6. 已知限制汇总

1. `@EnableEmbeddedDatabase(port = ...)` **完全无效**（源码中从未被读取）。
2. `EmbeddedTomcatContextLoader` 是包私有的，无法手动指定为 `loader`。
3. `AbstractWebMvcTest` 使用 JUnit 4，而 `AbstractWebFluxTest` 使用 JUnit 5——不要指望两种风格在同一个测试类里互换使用。
4. WebFlux 测试基类要求 Spring **5.0+**（4.3 中不存在 `spring-webflux`）。
5. 绝大多数依赖都是 `optional`；缺少某个依赖时，你得到的是 `NoClassDefFoundError`，而不是一条清晰的错误提示。
