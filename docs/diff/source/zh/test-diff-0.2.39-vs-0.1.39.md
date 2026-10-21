# 源码差异 —— `0.2.39` vs `0.1.39`（`src/test`）

[English](../test-diff-0.2.39-vs-0.1.39.md) · **简体中文**

本文按 Maven 模块逐个对比 `microsphere-spring` 最新两个已发布 tag 之间 `src/test` 源码的实现差异。
`src/main` 的差异见配套文档 [main-diff-0.2.39-vs-0.1.39.md](./main-diff-0.2.39-vs-0.1.39.md)，
平台基线（Java、命名空间、Spring profile 矩阵）记录在该文档的 §2，此处不再重复。

## 1. 对比范围与方法

| 项目 | 取值 |
|---|---|
| Tag A（左侧，diff 中的 `-` 行） | `0.2.39` —— `release` 分支，0.2.x 版本线，JUnit 5 |
| Tag B（右侧，diff 中的 `+` 行） | `0.1.39` —— `release-1.x` 分支，0.1.x 版本线，JUnit 4 |
| diff 命令形态 | `git diff 0.2.39 0.1.39 -- <module>/src/test` |
| 参与对比的模块 | 两个 tag 中**同时存在**的 Maven 模块（9 个） |
| 忽略的模块 | `microsphere-spring-compatible`（仅存在于 `0.1.39`） |
| 路径过滤 | `src/test/java`、`src/test/resources` |

状态字母：`A` = 仅存在于 `0.1.39`，`D` = 仅存在于 `0.2.39`，`M` = 内容有修改，
`R###` = 重命名（百分号为相似度）。

按需求排除在分析之外的两类差异，统一在 §3 汇总一次：

- Java 17 ↔ Java 8 的语法与 API 差异。
- Java EE ↔ Jakarta EE（`javax.*` ↔ `jakarta.*`）差异。

**未被排除**、且主导本差异的是**测试框架降级**（JUnit 5 → JUnit 4），因为它直接决定了某个用例能否被表达出来。

## 2. `src/test` 差异汇总

| Maven 模块 | `A` | `D` | `M` | 重命名 | 行数（`+` = 0.1.39 / `-` = 0.2.39） | 差异性质 |
|---|---|---|---|---|---|---|
| `microsphere-spring-context` | 3 | 2 | 121 | — | `+2412 / -2190` | 14 个实质差异，107 个机械差异 |
| `microsphere-spring-dependencies` | 0 | 0 | 0 | — | — | 无测试源码 |
| `microsphere-spring-guice` | 0 | 0 | 1 | — | `+8 / -8` | 纯 JUnit 4 换写法 |
| `microsphere-spring-jdbc` | 0 | 0 | 2 | — | `+21 / -23` | JUnit 4 换写法 + 日志级别作用域退化 |
| `microsphere-spring-parent` | 0 | 0 | 0 | — | — | 无测试源码 |
| `microsphere-spring-test` | 0 | 4 | 29 | `R100` | `+352 / -542` | 删除响应式夹具 + 框架换写法 |
| `microsphere-spring-web` | 0 | 0 | 56 | `R051` | `+950 / -904` | 48 个机械差异，8 个实质差异 |
| `microsphere-spring-webflux` | 0 | 0 | 9 | — | `+81 / -11` | 每个改动都有实质内容 |
| `microsphere-spring-webmvc` | 2 | 3 | 34 | — | `+619 / -769` | 新增适配器测试 + 删除 servlet 函数式测试 |

## 3. 机械性转换（统一记录一次）

### 3.1 JUnit 5 → JUnit 4

| `0.2.39` | `0.1.39` | 原因 |
|---|---|---|
| `org.junit.jupiter.api.*` | `org.junit.*` | JUnit 坐标不同（`junit-jupiter` ↔ `junit`） |
| `Assertions.assertXxx` | `Assert.assertXxx` | JUnit 4 没有 `Assertions` 类 |
| `@BeforeEach` / `@AfterEach` | `@Before` / `@After` | 命名差异 |
| `@ExtendWith(SpringExtension.class)` + `@SpringJUnitConfig` | `@RunWith(SpringRunner.class)` + `@ContextConfiguration` | Spring Test 4.3 无 `SpringExtension` |
| `@Disabled` | `@Ignore` | 命名差异 |
| `@Nested`、`@ParameterizedTest` | 拍平为单方法内的循环 | JUnit 4 不支持 |
| `@SpringLoggingTest` / `@LoggingLevelsClass(...)` | `@ClassRule LoggingLevelsRule levels("TRACE","INFO","ERROR")` | 按类作用域的日志级别能力是 `microsphere-logging` 0.2.x 的 JUnit 5 特性 |
| 包级私有的测试方法 | `public` 测试方法 | JUnit 4 要求 `public` |

最后一行解释了为什么 JUnit 4 的测试文件通常会在每个测试方法上多出 `public` 修饰符。

### 3.2 JDK 与命名空间

`instanceof T t` 模式匹配 → 显式强转；`var` → 显式类型声明；文本块 → 字符串拼接；
`.toList()` → `.collect(Collectors.toList())`；`List.of` → `Arrays.asList`；
`jakarta.servlet.*` import → `javax.servlet.*`。

## 4. `microsphere-spring-context`

121 个被修改的测试文件中，**14 个有实质内容**，其余 107 个只是 §3 的机械转换。

### 4.1 新增 / 删除的测试

| 文件 | 状态 | 内容 | 原因 |
|---|---|---|---|
| `io/microsphere/spring/beans/factory/ObjectProviderUtilsTest.java` | `A` | 16 个用例；用 `private static final boolean isBelowSpring51 = SPRING_5_1.gt(CURRENT);` 做门槛 | 覆盖仅存在于 `0.1.39` 的垫片（main 差异文档 §5.1） |
| `io/microsphere/spring/beans/factory/support/BeanDefinitionBuilderUtilsTest.java` | `A` | 覆盖 `spring5AndAbove` 分叉与 `DelegatingFactoryBean` 回退 | API 兼容 |
| `io/microsphere/spring/context/event/DeferredApplicationEventPublisherTest.java` | `A` | 把 `PUBLISH_EVENT_METHOD` 置为 `null` 以模拟 Spring < 4.2，断言降级路径 | API 兼容 |
| `io/microsphere/spring/beans/factory/support/DefaultBeanDependencyResolverTest.java` | `D` | 2025-05 在 0.2.x 线上补充的覆盖率测试 | 未回移 |
| `io/microsphere/spring/context/event/ParallelPreInstantiationSingletonsBeanFactoryListenerTest.java` | `D` | 同一批 2025-05 覆盖率补充 | 未回移 |

### 4.2 有实质内容的修改

| 文件 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|
| `io/microsphere/spring/core/SpringVersionTest.java` | 断言 `SPRING_6_0` … `SPRING_7_0_8` 枚举表 | 断言 `SPRING_3_2` … `SPRING_5_3_39` 枚举表 | API 兼容 —— 每条线只校验自己的版本窗口 |
| `io/microsphere/spring/beans/factory/config/BeanDefinitionUtilsTest.java` | 直接校验 getter 结果 | `+8` 个垫片用例，含 `assertEquals(isGESpring5, isSetInstanceSupplierMethodPresent())` | API 兼容 |
| `io/microsphere/spring/beans/factory/BeanFactoryUtilsTest.java` | 7 个独立的 `testAsXxx` 方法 | 合并为单个 `testAsMethods` | 测试框架（拍平） |
| `io/microsphere/spring/beans/BeanUtilsTest.java` | —— | 多出 `testFindPrimaryConstructorOnFailed` | API 兼容 —— 断言新垫片的 `null` 回退 |
| `io/microsphere/spring/util/MimeTypeUtilsTest.java` | 有 `testSpecificityComparator` | 不存在 | API 兼容 —— 0.1.x 没有可测试的嵌套 `SpecificityComparator` |
| `io/microsphere/spring/core/annotation/AnnotationUtilsTest.java` | 读取 `bean.autowireCandidate()` | 读取 `bean.autowire()` | API 兼容 —— Spring 6 移除了 `@Bean` 上的 `autowire()` |
| `io/microsphere/spring/context/annotation/AnnotatedBeanDefinitionRegistryUtilsTest.java` | `testScanBasePackages` 期望 2 个 bean | 期望 1 个 | 功能需要 —— 0.2.x 多注册了一个基础设施 bean |
| `io/microsphere/spring/context/annotation/EnableAutoRegistrationBeanTest.java` | `testDisabledTest2Config` **没有 `@Test`** → 从不执行 | 同样保留 | **缺陷**（0.2.39 上一条关闭路径断言其实从未运行） |
| `io/microsphere/spring/context/event/InterceptingApplicationListenerTest.java` | 直接包装 listener | 用 `GenericApplicationListenerAdapter` 包装 | API 兼容 —— 4.3 需要适配器才能获得一致的 `ApplicationListener` 语义 |
| `io/microsphere/spring/beans/factory/support/ListenableAutowireCandidateResolverTest.java` | 无条件断言 | 对被垫片的方法做版本条件断言 | API 兼容 |
| `io/microsphere/spring/core/env/ListenableConfigurableEnvironmentTest.java` | 覆盖 `setEscapeCharacter` | 覆盖 `acceptsProfiles(Profiles)` / `matchesProfiles` | API 兼容 |
| `io/microsphere/spring/core/io/ResourceUtilsTest.java` | 直接断言 `FileUrlResource` | 以 `resolveClass(...)` 是否可用作为前提 | API 兼容 |
| `io/microsphere/spring/beans/factory/support/BeanRegistrarTest.java` | 调用 `registerBeanDefinition(..., Class, boolean)` 的 builder 重载 | 调用手工构建的变体 | API 兼容 |

## 5. `microsphere-spring-web`

56 个被修改的测试文件：**48 个机械差异，8 个实质差异**，外加整个差异中最值得注意的一个重命名。

| 文件 | 状态 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/web/util/MediaTypeUtilsTest.java` → `org/springframework/http/server/reactive/ServerHttpRequest.java` | `R051` | 保留真实测试 `testSPECIFICITY_COMPARATOR`，断言 `assertEquals(-1, SPECIFICITY_COMPARATOR.compare(audioType07, audioType03))` | 变成一个**空占位类**，注释写着 "Dump class for {ServerHttpRequest} in the Spring 5.0+ WebFlux"，编译产物落在 `src/test/java` 的 Spring 包名下 | **API 兼容** —— `spring-web 4.3.30` 根本没有 `org.springframework.http.server.reactive` 包（该 jar 中 `0` 个条目，而 `5.3.39` 有 `116` 个），在 `4.3` profile 下连 `mock(ServerHttpRequest.class)` 都无法编译 |
| `io/microsphere/spring/web/helper/SpringWebMvcHelperTest.java` | `M`（+75/-40） | `andExpectAll(...)`、`.characterEncoding(UTF_8)`、`fromPath(t).build(map)`、`removeApplicationListener(listener)`；含 `testGetServletWebRequest` | 链式 `andExpect(...)`、`.characterEncoding("UTF_8")`、`buildAndExpand(map).toUri()`、改用 `ServletContext` 属性 `processedListeners` 去重；删除 `testGetServletWebRequest`（`@Test` 数 432 → 430） | API 兼容 + 测试框架 |
| `…/SpringWebMvcHelperTest.java`（`testGetMatrixVariables`、`testGetProducibleMediaTypes`） | `M` | 断言 `null` | 断言空集合 | **API 兼容** —— 老版本 Spring 返回空集合而非 `null` |
| `io/microsphere/spring/web/extension/EnableWebExtensionTest.java` | `M` | 显式声明 `sources = {BEAN_FACTORY, SPRING_FACTORIES}` | 省略 | API 兼容 —— 4.3 的 `@ContextConfiguration` 默认位置已覆盖 |
| `io/microsphere/spring/web/context/RequestAttributesUtilsTest.java` | `M` | 使用 `org.springframework.core.MethodParameter.forExecutable`（5.3+） | 使用 `io.microsphere.spring.core.MethodParameterUtils.forExecutable` | API 兼容 |

其余 51 个文件只是 JUnit 4 换写法、JDK 语法或 Servlet 命名空间的差异。

## 6. `microsphere-spring-webflux`

9 个被修改的测试文件全部有实质内容 —— 该模块的测试直接编码了版本落差。

| 文件 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|
| `io/microsphere/spring/webflux/AbstractEnableWebFluxExtensionTest.java` | 从 `AbstractWebFluxTest#testWebEndpoints()`（`:77`）继承端点用例 | 把六个端点测试内联展开 | 功能需要 —— 聚合钩子只存在于 0.2.x |
| `io/microsphere/spring/webflux/function/server/RequestPredicateKindTest.java` | 只构造 predicate | 增加 `when(request.pathContainer())…` 打桩 | API 兼容 —— 回退路径需要一个 exchange 桩 |
| `io/microsphere/spring/webflux/function/server/RequestPredicateVisitorAdapterTest.java` | 测试 13 个 default 方法 | `+5` 个用例，含 `testVisit(boolean isVisitorSupported)` | API 兼容 —— 覆盖分叉的两个分支 |
| `io/microsphere/spring/webflux/filter/CompositeWebFilterTest.java` | `new DefaultWebFilterChain(webHandler, ofList(...))` | 数组重载 | API 兼容 —— Spring 7 去掉了 0.2.x 使用的构造形态 |
| `io/microsphere/spring/webflux/helper/SpringWebFluxHelperTest.java` | `.bodyValue(user)`；两个 `expectCookie().valueEquals` 断言生效 | `.body(just(user), User.class)`；那两条 cookie 断言被注释掉；新增 `if (CURRENT.lt(SPRING_5_3)) assertNull(path)` | API 兼容 |
| `io/microsphere/spring/webflux/mapping/ConsumingWebEndpointMappingAdapterTest.java` | 无额外注解 | 加上 `@EnableWebFluxExtension` | 功能需要 —— 0.2.x 的装配是隐式的 |
| `io/microsphere/spring/webflux/filter/RequestContextWebFilterTest.java`<br/>`io/microsphere/spring/webflux/filter/RequestHandledEventPublishingWebFilterTest.java` | 依赖扩展自动装配 | 加上 `@EnableWebFlux` / `@EnableWebFluxExtension` | 功能需要 |

## 7. `microsphere-spring-webmvc`

| 文件 | 状态 | 说明 | 原因 |
|---|---|---|---|
| `io/microsphere/spring/webmvc/config/WebMvcConfigurerAdapterTest.java` | `A`（122 行，18 个 `@Test`） | 逐个调用 `WebMvcConfigurerAdapter.super.configurePathMatch(null)` 等默认实现 | 覆盖仅存在于 `0.1.39` 的垫片 |
| `src/test/resources/webapp/WEB-INF/dispatcherServlet-servlet.xml` | `A` | 随提交 `26225dc13` 一起加入，该提交同时修改了配套断言 | 功能需要 —— 0.1.x 需要自己的 servlet 上下文描述符来验证父子上下文解析 |
| `io/microsphere/spring/webmvc/context/WebApplicationContextTest.java`（配套断言） | `M` | `0.2.39` 断言 `assertSame(wac, getRequiredWebApplicationContext(servletContext))`（`:73`）；`0.1.39` 断言 `assertSame(wac.getParent(), getRequiredWebApplicationContext(servletContext))` | 功能需要 —— 在老版本线上解析到的是上一级上下文 |
| `io/microsphere/spring/webmvc/handler/…ServletFunction*Test`（3 个文件，22 个用例） | `D` | 针对 `web/servlet/function` 机制的测试 | 功能需要 —— 该机制是 0.2.x 独有（main 差异 §8.1） |
| `io/microsphere/spring/webmvc/config/ConfigurableContentNegotiationManagerWebMvcConfigurerTest.java` | `M` | `0.2.39`：`assertTrue(strategies.size() > 1)`；`0.1.39`：`assertEquals(4, strategies.size())`，且包含 `ServletPathExtensionContentNegotiationStrategy` | API 兼容 —— Spring 6 不再自动注册该策略 |
| `src/test/resources/webapp/WEB-INF/web.xml` | `M` | `0.2.39`：`version="6.0"`、`jakarta.ee` schema、`metadata-complete="false"`；`0.1.39`：`version="3.1"`、`xmlns.jcp.org` schema | 命名空间 |
| 另外 26 个被修改文件 | `M` | —— | 仅 JUnit 4 换写法（删除 136 行 `jupiter` 相关，新增 20 个 `@RunWith`） |

## 8. `microsphere-spring-test`

| 文件 | 状态 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/test/webflux/PersonHandlerTest.java`<br/>`io/microsphere/spring/test/webflux/WebFluxTest.java`<br/>`io/microsphere/spring/test/webflux/SimpleUrlHandlerMappingTestConfigTest.java`<br/>`io/microsphere/spring/test/webmvc/PersonHandlerTest.java` | `D` ×4 | 存在 | 不存在 | 功能需要 —— 依赖被删除的 `PersonHandler` / `RouterFunctionTestConfig` 夹具 |
| `io/microsphere/spring/test/webmvc/AbstractWebMvcTest.java` | `M`（22/31） | `@SpringJUnitConfig({TestController, RouterFunctionTestConfig})`、`protected` 钩子、含 `testUpdatePerson()` 走 `PUT /test/person/{id}` | `@RunWith(SpringRunner.class)`、`public` 方法、无 `testUpdatePerson` | 测试框架 + 功能需要 |
| `io/microsphere/spring/test/webflux/AbstractWebFluxTest.java` | `M` | 有聚合入口 `testWebEndpoints()`（`:77`） | 无聚合入口，但多一个 `testView()` | 功能需要 |
| `io/microsphere/spring/test/util/WebTestUtils.java` | 无差异 | —— | —— | 公共工具 |
| 14 个被修改的测试支撑 main 文件中有 10 个 | `M` | —— | —— | 纯属 `jakarta.servlet` ↔ `javax.servlet`；内嵌 Tomcat 部署相关行（`37/59/101/179/203/213/223/227`）两边完全一致 |
| `META-INF/services/…` | `R100` | 键为 `jakarta.servlet.ServletContainerInitializer` | 键为 `javax.servlet.…` | 命名空间 |

两个 tag 都没有使用 Servlet 6 独有 API（`getAllTlds`、`SessionCookieConfig` 在两边均不出现）。

## 9. `microsphere-spring-guice`、`microsphere-spring-jdbc`

| 模块 | 文件 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| guice | `io/microsphere/spring/beangogo/guice/extension/EnableGuiceTest.java`（±8） | JUnit 5 的 import 与注解 | JUnit 4 等价写法 —— 断言完全未变 | 测试框架 |
| guice | `pom.xml` | `junit-jupiter`（`:58-59`） | `junit`（`:51-52`） | 测试框架 |
| jdbc | 2 个测试（±21 / −23） | `@SpringLoggingTest` + `@LoggingLevelsClass(loggingClasses = P6DataSourceBeanPostProcessor.class, …)` 把日志级别限定到单个类 | 退化为全局 `@ClassRule LoggingLevelsRule` | 测试框架 —— 这是**覆盖精度的能力退化**，不是生产行为差异（两边 P6Spy 都是 `3.9.1`） |

## 10. 如何解读这份差异

1. **测试差异的主体是翻译而非设计。** `context` 的 107/121、`web` 的 48/56、`webmvc` 的 26/34，
   以及 `guice`/`jdbc` 中被改动文件的绝大多数，都只是 JUnit 4 翻译加上被排除的 JDK/命名空间改写。
2. **实质性测试改动与 main 侧垫片一一对应。** 每个仅存在于 `0.1.39` 的垫片
   （`ObjectProviderUtils`、`BeanDefinitionBuilderUtils`、`DeferredApplicationEventPublisher`、
   `WebMvcConfigurerAdapter`）都带着配套测试，而这些测试要么探测能力
   （`isBelowSpring51`、`isSetInstanceSupplierMethodPresent()`），要么强制走回退分支
   （把 `PUBLISH_EVENT_METHOD` 置空）。
3. **覆盖缺口是单向的。** 两个被删除的 `context` 测试是 2025-05 在 0.2.x 线新增、从未回移的覆盖率补充；
   三个被删除的 `webmvc` servlet 函数式测试只是跟随 0.2.x 独有的特性 —— 因此 0.1.x 的覆盖永远不会比
   0.2.x 更多。
4. **发现两个真实缺陷。** `0.2.39` 上 `EnableAutoRegistrationBeanTest#testDisabledTest2Config` 缺少
   `@Test`，因此从不执行；`0.1.39` 上空壳的
   `org.springframework.http.server.reactive.ServerHttpRequest` 占位类是一个编译期垫片，一旦不再支持
   `4.3` profile 就必须移除。

## 11. 相关文档

- [main-diff-0.2.39-vs-0.1.39.md](./main-diff-0.2.39-vs-0.1.39.md) —— `src/main` 差异与平台基线（简体中文）
- [English version](../test-diff-0.2.39-vs-0.1.39.md)
- [文档总索引](../../../zh/README.md)
