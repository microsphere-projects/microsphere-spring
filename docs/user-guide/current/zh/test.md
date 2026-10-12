# microsphere-spring-test · 0.2.x

[← 当前版本总览](./README.md) · [English](../test.md) · [开发者文档](../../../developer-guide/current/zh/test.md) · [1.x 文档](../../1.x/zh/test.md)

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-test` |
| 分支 | `release`（0.2.x 线） |
| 版本 | `0.2.39` / 开发版 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x**（`spring-test` 需与所选 Spring 版本一致） |
| 命名空间 | `jakarta.servlet.*`（Jakarta EE 11）、Tomcat 10.1+ 内嵌容器 |
| 必需依赖 | `microsphere-java-test`、`microsphere-logging-test`、`microsphere-logback` |
| 可选依赖 | `spring-context`、`spring-jdbc`、`spring-web`、`spring-webmvc`、`spring-webflux`、`spring-test`、`jakarta.servlet-api`、`tomcat-embed-core`、`sqlite-jdbc`、`h2`、`zookeeper` 3.9.5、`curator-recipes`/`curator-test` 5.9.0、`junit-jupiter`、`mockito-core`、`jackson-*`、`jsonassert`、`hamcrest`、`jakarta.websocket-api`/`-client-api` |

本模块是库的测试工具箱。**所有**测试框架与容器依赖（`spring-test`、`spring-web*`、`jakarta.servlet-api`、`tomcat-embed-core`、`sqlite-jdbc`、`h2`、ZooKeeper/Curator、JUnit Jupiter、Mockito、Jackson、JSONAssert、Hamcrest）都是 optional，compile 必需项只有 `microsphere-java-test`、`microsphere-logging-test` 与 `microsphere-logback`，请按实际使用精确引入——这也是它能被其他模块在 `test` 作用域复用的原因。JUnit Jupiter / Mockito 版本由 `microsphere-java-dependencies`（0.3.19）统一管理。

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- 按特性补充 -->
<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.apache.tomcat.embed</groupId><artifactId>tomcat-embed-core</artifactId><scope>test</scope></dependency>
```

---

## 1. `@EnableEmbeddedDatabase`：内嵌数据库

`io.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase` 的声明为 `@Target({TYPE, ANNOTATION_TYPE}) @Import(EmbeddedDataBaseBeanDefinitionRegistrar.class) @Repeatable`（容器注解 `@EnableEmbeddedDatabases`）。它按你指定的 Bean 名注册一个 `DriverManagerDataSource`：`SQLITE` → `jdbc:sqlite::memory:`，`H2` → `jdbc:h2:mem:`。

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `dataSource` | `String` | **必填** | DataSource Bean 名；重名抛 `BeanCreationException` |
| `type` | `EmbeddedDatabaseType` | `SQLITE` | 枚举 `{ SQLITE, H2 }` |
| `primary` | `boolean` | `false` | 是否标记 `@Primary` |
| `properties` | `String[]` | `{}` | 追加 `key=value`，支持 `${}` 占位符 |
| `port` | `int` | `-1` | 源码中已声明但**注册器未读取**，请勿依赖 |

```java
@SpringBootTest
@EnableEmbeddedDatabase(dataSource = "testDataSource", type = EmbeddedDatabaseType.H2)
class RepositoryTest {
    @Autowired private JdbcTemplate jdbcTemplate;
}
```

依赖测试 classpath 上的 `org.xerial:sqlite-jdbc` 或 `com.h2database:h2`（在本模块中均为 optional）。

---

## 2. `@EmbeddedTomcatConfiguration`：真实内嵌 Tomcat

`io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration` 是组合注解：`@ExtendWith(SpringExtension)` + `@BootstrapWith(EmbeddedTomcatTestContextBootstrapper)` + `@ContextConfiguration` + `@WebAppConfiguration`。启动器内部构建 `EmbeddedTomcatMergedContextConfiguration`，用 `tomcat.addWebapp(...)` 部署 `docBase`、必要时 `setAltDDName(alternativeWebXml)`，然后 `tomcat.start()`，并把可解析的 `ApplicationContext`/`WebApplicationContext` 暴露给容器。

> ⚠️ `EmbeddedTomcatContextLoader` 是 **package-private**，**不能**写 `@ContextConfiguration(loader = EmbeddedTomcatContextLoader.class)`；唯一受支持的入口是本注解。

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `port` | `int` | `8080` | `0` 表示随机端口 |
| `contextPath` | `String` | `""` | 上下文路径 |
| `basedir` | `String` | `"${java.io.tmpdir}"` | Tomcat 基础目录 |
| `docBase` | `String` | `"classpath:/webapp"` | webapp 文档根，`@AliasFor` 到 `@WebAppConfiguration.value` |
| `alternativeWebXml` | `String` | `""` | 替代 `web.xml` 路径 |
| `features` | `Feature[]` | `{}` | `enum Feature { NAMING, DEFAULT_WEB_XML, WEB_APP_DEFAULTS, USE_TEST_CLASSPATH, SILENT }` |
| `classes` / `locations` / `initializers` / `inheritLocations` / `inheritInitializers` | — | `{}` / `{}` / `{}` / `true` / `true` | `@AliasFor` 到 `@ContextConfiguration` |

```java
@EmbeddedTomcatConfiguration(
        port = 0,
        contextPath = "/demo",
        docBase = "classpath:/webapp",
        features = { NAMING, WEB_APP_DEFAULTS, SILENT },
        classes = WebAppConfig.class
)
class WebAppIT {
    // 实际端口可从上下文解析
}
```

依赖 `tomcat-embed-core`、`jakarta.servlet-api`（在本模块中均为 optional）。

---

## 3. Web 测试基类

两个基类都已装配好 Web 环境并**标注 `@Disabled`**（避免自身作为测试执行），继承后按需调用其预置用例方法：

| 基类 | 注解 | 受保护字段 | 预置用例 |
|---|---|---|---|
| `test.webmvc.AbstractWebMvcTest` | `@Disabled @WebAppConfiguration @SpringJUnitConfig(classes = {TestController.class, RouterFunctionTestConfig.class}) @EnableWebMvc` | `ConfigurableWebApplicationContext context`、`TestController testController`、`MockMvc mockMvc` | `testWebEndpoints()`、`testHelloWorld()`、`testGreeting()`、`testUser()`、`testError()`、`testResponseEntity()`、`testView()`、`testUpdatePerson()` |
| `test.webflux.AbstractWebFluxTest` | `@Disabled @SpringJUnitConfig(classes = {TestController.class, RouterFunctionTestConfig.class}) @EnableWebFlux` | `ConfigurableApplicationContext context`、`TestController testController`、`WebTestClient webTestClient`（`@BeforeEach` 构建） | 同上（无 `testView()`） |

```java
class MyMvcIT extends AbstractWebMvcTest {
    @Test
    void helloWorldShouldWork() throws Exception {
        testHelloWorld();                 // 复用基类断言
    }

    @Test
    void customEndpoint() throws Exception {
        mockMvc.perform(get("/products/1")).andExpect(status().isOk());
    }
}
```

配套 fixture：`test.web.controller.TestController`、`test.webmvc.PersonHandler` / `RouterFunctionTestConfig` / `SimpleUrlHandlerMappingTestConfig`（webflux 有同名对应类）、`test.domain.User`。

---

## 4. Servlet / 请求构造工具

| 工具 | 说明 |
|---|---|
| `test.web.servlet.TestServletContext` | 继承 `MockServletContext`，`addServlet/addFilter/addListener` 真正生效 |
| `TestServlet` / `TestFilter` / `TestServletRegistration` / `TestFilterRegistration` / `TestServletContextListener` | 默认名 `testServlet`/`testFilter`，URL 模式 `"/testServlet"`/`"/testFilter"` |
| `test.util.ServletTestUtils.addTestServlet/addTestFilter(ServletContext)` | 一行注册测试组件 |
| `test.util.SpringTestWebUtils` | `createWebRequest()`、`createWebRequest(Consumer<MockHttpServletRequest>)`、`createWebRequest(String)`、`createWebRequestWithParams(Object...)`、`createWebRequestWithHeaders(...)`、`createPreFightRequest()`、`clearAttributes(...)` — 构造 `ServletWebRequest`，用于测试 `HandlerMethodInterceptor` 这类需要 `NativeWebRequest` 的代码 |
| `test.web.context.request.MockServletWebRequest` | 可取回底层 `MockHttpServletRequest`/`MockHttpServletResponse` |
| `test.util.SpringTestUtils.testInSpringContainer(consumer, configClasses...)` | 不依赖 JUnit 扩展，临时启动容器执行断言 |
| `test.web.WebTestUtils` | `mockServerWebExchange()`、`getValue(Mono<T>)`（同步取响应式值） |
| `test.context.annotation.AnnotatedTypeMetadataTestFactory` | `createMethodAnnotatedTypeMetadata()`，为自定义 `Condition`/注解解析提供 mock |
| `test.context.annotation.TestConditionContext` | mock 版 `ConditionContext` |
| `test.zookeeper.embedded.EmbeddedZookeeperServer` | 注解，内嵌 ZooKeeper：`port()` 默认 `2181`、`dataDir()` 默认 `"${java.io.tmpdir}/test-zookeeper-${port}"`、`tickTime()` 默认 `2000L`；配套 `EmbeddedZookeeperServerTestExecutionListener`；需 `curator-test` |
| `test.junit.jupiter.SpringLoggingTest` | 测试注解，内部组合 `@LoggingLevelsClass(levels = {"TRACE", "INFO", "ERROR"})`（由 microsphere-logging 提供）动态调整日志级别 |
