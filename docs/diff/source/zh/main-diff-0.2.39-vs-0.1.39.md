# 源码差异 —— `0.2.39` vs `0.1.39`（`src/main`）

[English](../main-diff-0.2.39-vs-0.1.39.md) · **简体中文**

本文按 Maven 模块逐个对比 `microsphere-spring` 最新两个已发布 tag 之间 `src/main` 源码的实现差异。
`src/test` 的差异见独立文档：[test-diff-0.2.39-vs-0.1.39.md](./test-diff-0.2.39-vs-0.1.39.md)。

## 1. 对比范围与方法

| 项目 | 取值 |
|---|---|
| Tag A（左侧，diff 中的 `-` 行） | `0.2.39` —— `release` 分支，0.2.x 版本线 |
| Tag B（右侧，diff 中的 `+` 行） | `0.1.39` —— `release-1.x` 分支，0.1.x 版本线 |
| diff 命令形态 | `git diff 0.2.39 0.1.39 -- <module>/src/main` |
| 参与对比的模块 | 两个 tag 中**同时存在**的 Maven 模块（9 个） |
| 忽略的模块 | `microsphere-spring-compatible`（仅存在于 `0.1.39`） |
| 路径过滤 | `src/main/java`、`src/main/resources`（含 `META-INF/services`）、模块 `pom.xml` |

表格中的状态字母：

| 字母 | 含义 |
|---|---|
| `A` | 文件仅存在于 `0.1.39`（0.1.x 线新增） |
| `D` | 文件仅存在于 `0.2.39`（0.1.x 线删除） |
| `M` | 两个 tag 都存在且内容有差异 |
| `R` | 文件被移动/重命名（百分号为相似度） |

## 2. 版本基线

两条版本线锁定的平台不同，这解释了绝大多数残留差异。

| 维度 | `0.2.39` | `0.1.39` |
|---|---|---|
| Java | 17（`<java.version>17</java.version>`） | 8（无 `java.version` 覆盖） |
| Java EE / Jakarta EE 命名空间 | `jakarta.*` | `javax.*` |
| Jakarta EE BOM | `jakarta.platform:jakarta.jakartaee-bom:11.0.0`（import） | 未 import |
| Servlet 容器 | Servlet 6.0 描述符 / `web.xml` 6.0 | `tomcat-embed-core:9.0.119`，Servlet 3.1 描述符 |
| Spring Framework Profile | `6.0`（6.0.23）/ `6.1`（6.1.21）/ `6.2`（6.2.18）/ `7.0`（7.0.9，默认） | `4.3`（4.3.30.RELEASE）/ `5.0`（5.0.20.RELEASE）/ `5.1`（5.1.20.RELEASE）/ `5.2`（5.2.25.RELEASE）/ `5.3`（5.3.39，`activeByDefault`） |
| Spring Reactor BOM | `2022.0.22` … `2025.0.7` | `Bismuth-SR17` … `2020.0.47` |
| 测试框架 | JUnit 5（`junit-jupiter`） | JUnit 4（`junit`） |
| `<revision>` | `0.2.39-SNAPSHOT` | `0.1.39-SNAPSHOT` |

两个 tag 完全一致的第三方版本：Jackson `2.22.2`、SnakeYAML `2.6`、P6Spy `3.9.1`、ZooKeeper `3.9.5`、
Curator `5.9.0`、`microsphere-java` `0.3.19`、`microsphere-logging` `0.2.2`、构建父工程
`microsphere-build` `0.3.14`。`microsphere-spring-dependencies/pom.xml`（BOM）在两个 tag 上字节级相同
—— 而它从未收录 `microsphere-spring-compatible`，尽管 `0.1.39` 的根 POM 把它声明为模块（根
`pom.xml:61`）。

## 3. 差异原因分类

每个条目都标注了造成该差异的主要原因：

| 标记 | 含义 |
|---|---|
| **API 兼容** | 0.1.x 线必须能跑在 Spring 4.3–5.3 上，因此较新的 Spring API 被替换成 `MethodHandle`/反射探测 + 回退分支（或在本地重新声明一个垫片）。 |
| **命名空间** | 纯粹的 `javax.*` ↔ `jakarta.*` 替换 —— 为完整性列出，不计入实现差异。 |
| **JDK 语法** | Java 17 的模式匹配 / `var` / `toList()` 与 Java 8 写法的等价替换 —— 按需求排除在分析之外。 |
| **功能需要** | 只在其中一条线上刻意提供的行为（例如依赖 Spring 6/7 API 的能力）。 |
| **未接线 API** | 代码存在，但在该 tag 上没有任何调用方。 |
| **缺陷** | 对比过程中发现的行为性 bug —— 如实记录，不做美化描述。 |
| **注释/Javadoc** | 仅文案变更，无语义变化。 |

## 4. `src/main` 差异汇总

| Maven 模块 | 新增（`0.1.39`） | 删除（仅 `0.2.39`） | 修改 | 行数（`+` = 0.1.39 / `-` = 0.2.39） | 差异性质 |
|---|---|---|---|---|---|
| `microsphere-spring-context` | 4 | 0 | 33 | `+1768 / -270` | 兼容垫片（最集中） |
| `microsphere-spring-dependencies` | 0 | 0 | 0 | — | BOM 字节级相同 |
| `microsphere-spring-guice` | 0 | 0 | 0 | — | main 无差异 |
| `microsphere-spring-jdbc` | 0 | 0 | 0 | — | main 无差异 |
| `microsphere-spring-parent` | 0 | 0 | 0 | — | 不存在 main 源码 |
| `microsphere-spring-test` | 0 | 2 | 15 | `+90 / -219` | 测试支撑：Servlet 命名空间 + 移除的 WebFlux/WebMvc 夹具 |
| `microsphere-spring-web` | 1 | 1 | 14 | `+190 / -115` | 一个未接线的 visitor API、一个 Spring 7 规避，其余为命名空间 |
| `microsphere-spring-webflux` | 0 | 0 | 1 | `+69 / -27` | 仅一个 `RequestPredicates.Visitor` 垫片 |
| `microsphere-spring-webmvc` | 1 | 3 | 20（另 1 个 `R`） | `+308 / -445` | `WebMvcConfigurerAdapter` 垫片 vs 移植的 router function 机制 |

## 5. `microsphere-spring-context`

### 5.1 仅存在于 `0.1.39` 的类

| 文件（`src/main/java` 下路径） | 状态 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/beans/factory/ObjectProviderUtils.java` | `A`（333 行） | 不存在，且模块内零引用 | `ObjectProvider` 的空安全静态门面：`getIfAvailable`×2、`ifAvailable`、`getIfUnique`×2、`ifUnique`、`iterator`、`stream`、`orderedStream`。`iterator()` 先 `if (objectProvider instanceof Iterable)` 再强转，否则 `throw new UnsupportedOperationException(...)` | **API 兼容** —— Spring 4.3 的 `ObjectProvider` 既未实现 `Iterable`，也没有 5.x 才引入的 `stream()`/`orderedStream()` |
| `io/microsphere/spring/beans/factory/config/InstantiationAwareBeanPostProcessorAdapter.java` | `A`（132 行） | 不存在（Spring 6.0 已删除该适配器） | `implements SmartInstantiationAwareBeanPostProcessor`，为所有钩子提供空实现默认值，包含 `determineBeanType`（`@since Spring Framework 6.0`）与 `postProcessProperties`（5.1） | **API 兼容** —— 为 Spring 4.3/5.x 的扩展点补上基类 |
| `io/microsphere/spring/beans/factory/support/BeanDefinitionBuilderUtils.java` | `A`（87 行） | 不存在 | `genericBeanDefinitionBuilder(Class<T>, Supplier<T>)` 先尝试 `setInstanceSupplier(...)` 并返回 `boolean spring5AndAbove`；失败则注册 `DelegatingFactoryBean` 并 `addConstructorArgValue(bean)` | **API 兼容** —— Spring 4.3 没有 `AbstractBeanDefinition#setInstanceSupplier` |
| `io/microsphere/spring/context/event/DeferredApplicationEventPublisher.java` | `A`（198 行） | 不存在 | 用 `ConcurrentLinkedQueue` 缓冲 `ApplicationEvent`，在 `ContextRefreshedEvent` 时重放；通过反射调用 `ApplicationEventPublisher#publishEvent(Object)`（Spring 4.2+）。在 `0.1.39` 的 `:55` 处已标注 **`@Deprecated`** | **未接线 API** —— 两个 tag 的 main 代码都没有调用方，只有它自己的测试用到 |

### 5.2 由适配器引发的继承关系与签名变化

| 文件 | 状态 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/beans/factory/config/AnnotatedInjectionBeanPostProcessor.java` | `M` | `implements SmartInstantiationAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, PriorityOrdered, …` | `extends InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor, …` | **API 兼容** |
| `io/microsphere/spring/beans/factory/config/EventPublishingBeanBeforeProcessor.java`<br/>`…/EventPublishingBeanAfterProcessor.java` | `M` | `implements InstantiationAwareBeanPostProcessor` | `extends InstantiationAwareBeanPostProcessorAdapter` | **API 兼容** |
| `…/EventPublishingBeanAfterProcessor.java`（`requiresDestruction`） | `M` | 保留 `@Override` | 去掉 `@Override`，保留 `@since Spring Framework 4.3` Javadoc；方法体仍是 `return true;` | **API 兼容**（该钩子签名在 4.3/5.x 间有差异） |
| `io/microsphere/spring/context/annotation/AnnotatedBeanCapableImportBeanDefinitionRegistrar.java` | `M` | 使用 `FullyQualifiedAnnotationBeanNameGenerator.INSTANCE`（5.2.3+） | 自行声明 `public static final AnnotationBeanNameGenerator INSTANCE`，并把 `@Override` 注释掉 | **API 兼容** |
| `io/microsphere/spring/core/io/ResourceUtils.java` | `M` | `resource instanceof FileUrlResource` | `FILE_URL_RESOURCE_CLASS = resolveClass("org.springframework.core.io.FileUrlResource")` + `isInstance(...)`；`@since Spring Framework 5.0.2` | **API 兼容** —— `FileUrlResource` 从 5.0.2 才存在 |

### 5.3 被修改类内部的 MethodHandle 垫片

| 文件 | Δ | `0.1.39` 新增内容 | `0.2.39` 保留写法 | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/beans/factory/config/BeanDefinitionUtils.java` | `+307` | 句柄 `GET_RESOLVABLE_TYPE_`、`SET_INSTANCE_SUPPLIER_`、`GET_INSTANCE_SUPPLIER_` 及 `isXxxMethodPresent()` 探测；`getResolvableType(...)`×2、`set/getInstanceSupplier(...)`，以及 `doGetResolvableType(...)` 回退（`getResolvedFactoryMethod() != null → forMethodReturnType(...)`，否则 `hasBeanClass() ? forClass : NONE`） | 直接调用 Spring getter | **API 兼容** |
| `…/BeanDefinitionUtils#resolveBeanType` | 签名 | 参数收窄为 `AbstractBeanDefinition` | 参数为 `BeanDefinition` | **API 兼容** —— 垫片只能从 `AbstractBeanDefinition` 读取 `resolvableType`/`instanceSupplier`；连带影响 `BeanFactoryUtils.getBeanClass`（新增 `if (beanDefinition instanceof AbstractBeanDefinition)`）、`GenericBeanNameGenerator`（多一次强转）、`DefaultBeanDependencyResolver` 与 `DependencyAnalysisBeanFactoryListener`（改为调用垫片静态门面而非 Spring getter） |
| `io/microsphere/spring/beans/BeanUtils.java` | 少量 | `FIND_PRIMARY_CONSTRUCTOR_METHOD_HANDLE = findStatic(org.springframework.beans.BeanUtils.class, "findPrimaryConstructor", Class.class)`，并提供句柄缺失时返回 `null` 的 `findPrimaryConstructor(...)` | 直接调用 `org.springframework.beans.BeanUtils.findPrimaryConstructor`（Spring 5.2） | **API 兼容** |
| `io/microsphere/spring/beans/BeanUtils.java`（`invokeAwareInterfaces`） | 可见性 | 包级私有 | **`public`** | **功能需要** —— 0.2.x 把它作为公开 API |
| `io/microsphere/spring/beans/factory/support/BeanRegistrar.java` | `M` | `if (setInstanceSupplier(beanDefinition, () -> bean)) { … } else { registerFactoryBean(registry, beanName, bean, primary); }`；`registerBeanDefinition(..., Class, boolean)` 手工 `genericBeanDefinition` + `setPrimary` | `builder.setPrimary(primary)` 重载 | **API 兼容** |
| `io/microsphere/spring/beans/factory/support/ListenableAutowireCandidateResolver.java` | `+65` | 垫片化 `AutowireCandidateResolver#isRequired`（回退 `descriptor.isRequired()`）、`#hasQualifier`（回退 `return false`）、`#cloneIfNecessary`（回退 `instantiateClass(delegate.getClass())`）；移除 `@Override` 并留注释"…commented for compatibility with older versions" | 直接 `@Override` | **API 兼容** |
| `io/microsphere/spring/core/env/ListenableConfigurableEnvironment.java` | `M` | 垫片化 `Environment#acceptsProfiles(Profiles)`（5.1；回退抛出 `"...not supported before Spring Framework 5.1"`）、`matchesProfiles(String[])`（回退 `acceptsProfiles(profileExpressions)`）、`PropertyResolver#getPropertyAsClass`；新增 `PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-environment."` 与 `ENABLED_PROPERTY_NAME`，并带 `@ConfigurationProperty(type = boolean.class, defaultValue = "false", …)` | `setEscapeCharacter(Character)` 垫片化 `ConfigurablePropertyResolver#setEscapeCharacter`（6.2）；`getPropertyAsClass` 直接 `throw new UnsupportedOperationException();` | **API 兼容** + 见下方缺陷 |
| `io/microsphere/spring/context/event/InterceptingApplicationEventMulticasterProxy.java` | `M` | 反射探测 `removeApplicationListeners(Predicate)` / `removeApplicationListenerBeans(Predicate)`（Spring 5.3.5+），缺失时静默返回；`@Override` 移除 ×2 | 直接调用 | **API 兼容** |
| `io/microsphere/spring/util/MimeTypeUtils.java` | `-34` | —— | 内含嵌套类 `public static class SpecificityComparator<T extends MimeType> implements Comparator<T>` | **API 兼容（反向）** —— Spring 7 删除了 `MediaType.SPECIFICITY_COMPARATOR`，故 0.2.x 必须自己持有比较器；而 `0.1.39` 的 `GenericMediaTypeExpression.java:26` 仍是 `import static org.springframework.http.MediaType.SPECIFICITY_COMPARATOR` |
| `io/microsphere/spring/core/SpringVersion.java` | `M` | 枚举区间 `SPRING_3_2` … `SPRING_5_3_39`（199 个常量）；存在多余的 `CURRENT(of(...));;` | 枚举区间 `SPRING_6_0` … `SPRING_7_0_8`（78 个常量） | **API 兼容** —— 每条线只枚举自己支持的版本窗口 |

### 5.4 仅由 JDK 语法或命名空间造成差异的文件（排除在分析之外）

`DelegatingFactoryBean`、`AnnotationBeanDefinitionRegistryPostProcessor`、
`ResolvableDependencyTypeFilter`、`GenericAnnotationAttributes`、
`EnableSpringConverterAdapterRegistrar`、`EnvironmentUtils`、`PropertySourcesUtils`、
`ResourceLoaderUtils`、`DefaultPropertiesPropertySourceLoader`、
`InterceptingApplicationListener` —— 只有模式匹配 `instanceof`、`var`、`List.of` 与
`Arrays.asList`、`.toList()` 与 `.collect(toList())`，以及 `javax`↔`jakarta` 的 import 差异。

仅注释差异：`DefaultPropertiesPropertySources`、`ResourcePropertySource`、
`ResourcePropertySources`（"Example Usage" → "Examples Usage"）、`EventPublishingBeanInitializer`、
`AnnotationUtils`。

### 5.5 在 `0.2.39` 上发现的缺陷

`ListenableConfigurableEnvironment#setEscapeCharacter`（309-321 行）把
`throw new UnsupportedOperationException(...)` 放在了
`if (SET_ESCAPE_CHARACTER_METHOD_HANDLE != null)` 判断**之外**，因此即使垫片调用成功也照样抛异常。
0.1.x 线上根本没有这个方法，所以这个 bug 是通过差异对比暴露出来的，而非两条线的设计分歧。

## 6. `microsphere-spring-web`

| 文件 | 状态 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/web/metadata/WebEndpointMappingVisitor.java` | `A`（+114） | 不存在 | 端点映射树的 visitor 接口：12 个回调（`method`、`path`、`header`、`queryParam`、`startAnd`/`and`/`endAnd`、`startOr`/`or`/`endOr`、`startNegate`/`endNegate`） | **未接线 API** —— 在 `0.1.39` 上 `git grep` 只命中它自身的声明，无任何装配 |
| `io/microsphere/spring/web/util/MediaTypeUtils.java` | `D`（53 行） | 持有 `SPECIFICITY_COMPARATOR`，实现为 `new SpecificityComparator<>() { … }`，基于 `io.microsphere.spring.util.MimeTypeUtils.SpecificityComparator` | 不存在 | **API 兼容** —— 只因 Spring 7 删除 `MediaType.SPECIFICITY_COMPARATOR` 才存在 |
| `io/microsphere/spring/web/rule/WebRequestProducesRule.java` | `M`（+18 / -0） | 直接使用 `resolveMediaTypes(...)` 结果 | 新增 `BEFORE_SPRING_505 = CURRENT.isLessThan(SPRING_5_0_5)`，以及 `resolveMediaTypes` 返回空时使用的 `MEDIA_TYPE_ALL_LIST = ofList(ALL)` 回退 | **API 兼容** —— 5.0.5 之前的 produces 解析不到媒体类型 |
| `io/microsphere/spring/web/metadata/WebEndpointMapping.java` | `M`（16/16） | —— | —— | 仅**命名空间**：Javadoc 里的 `javax.servlet`/`javax.portlet` ↔ `jakarta.*`，代码无变化 |
| `io/microsphere/spring/web/metadata/WebRequestPattensRule.java` | `M` | —— | —— | 仅 **Javadoc** |
| `io/microsphere/spring/web/helper/SpringWebHelper.java`、`io/microsphere/spring/web/util/WebRequestUtils.java` | `M` | 归一化命名空间后签名完全一致 | 同左 | 仅**命名空间** |
| `io/microsphere/spring/web/registry/CompositeWebEndpointMappingRegistry.java` | `M` | `.toList()` | `.collect(toList())` | **JDK 语法**（已排除） |

该模块其余被修改文件都只是命名空间/JDK 语法的改写。

## 7. `microsphere-spring-webflux`

main 侧只有一个被修改文件，`+69 / -27`：

| 文件 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|
| `io/microsphere/spring/webflux/function/server/RequestPredicateVisitorAdapter.java` | `interface … extends RequestPredicates.Visitor`，含 13 个 `default @Override` 方法以及 `version(String)`（`@since Spring Framework 7.0`） | `interface … extends InvocationHandler`；解析 `VISITOR_CLASS = resolveClass("org.springframework.web.reactive.function.server.RequestPredicates$Visitor")` 与 `ACCEPT_METHOD = findMethod(RequestPredicate.class, "accept", VISITOR_CLASS)`，暴露 `isVisitorSupported()`、`getProxy()`、`invoke(...)`；`visit(p)` 在不支持 visitor 时改走 `RequestPredicateKind.acceptVisitor(p, this)` | **API 兼容** —— `RequestPredicates.Visitor` 从 Spring 5.1 才有，而 0.1.x 线必须支持 4.3 |

`io/microsphere/spring/webflux/function/server/RequestPredicateKind.java` 在两个 tag 上**字节级相同**，
说明分叉完全落在适配器内。

## 8. `microsphere-spring-webmvc`

### 8.1 新增 / 删除文件

| 文件 | 状态 | 说明 | 原因 |
|---|---|---|---|
| `io/microsphere/spring/webmvc/config/WebMvcConfigurerAdapter.java` | `A`（195 行） | `interface … extends WebMvcConfigurer`，含 19 个空的 default 方法；被装配进 `WebMvcExtensionConfiguration`、`ConfigurableContentNegotiationManagerWebMvcConfigurer` 与 `InterceptingHandlerMethodProcessor`（后者在 `0.2.39` 上只是 `implements WebMvcConfigurer`） | **API 兼容** —— Spring 6 删除了具体的 `WebMvcConfigurerAdapter` 类，而 0.1.x 需要一个在 Spring 4.3 上"所有方法都可选"的适配器形态 |
| `io/microsphere/spring/web/servlet/function/ConsumingWebEndpointMappingAdapter.java` | `D`（193 行） | 0.2.x 独有的、把 webflux visitor 机制移植到 Servlet 函数式端点的实现；内部用 `ThreadLocal<LinkedList<…Builder>>` 栈 | **功能需要** —— Servlet 函数式端点自省依赖 5.2+ API |
| `io/microsphere/spring/web/servlet/function/RequestPredicateVisitorAdapter.java` | `D`（105 行） | 其中 `version(String)` 标注 `@since Spring Framework 7.0` | **功能需要** |
| `io/microsphere/spring/web/servlet/function/RouterFunctionVisitorAdapter.java` | `D`（70 行） | 为 5.3.x 兼容标注了 `attributes(Map)` | **功能需要** |
| `META-INF/services/…ServletContainerInitializer` | `R100` | 内容完全一致，仅键名由 `jakarta.servlet.ServletContainerInitializer` 变为 `javax.servlet.…` | **命名空间** |

### 8.2 有实质内容的修改

| 文件 | Δ | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|---|
| `io/microsphere/spring/webmvc/metadata/RequestMappingMetadataWebEndpointMappingFactory.java` | `+36` | `RequestMappingInfo#getPatternValues()`（5.3+） | 同一调用置于句柄之后，回退 `getPatternsCondition().getPatterns()` | **API 兼容** |
| `io/microsphere/spring/webmvc/metadata/HandlerMappingWebEndpointMappingResolver.java` | `-13` | 有 `resolveFromRouterFunctionMapping()` | 不存在 | **功能需要** —— 0.2.x 才在 MVC 中解析 router function |
| `io/microsphere/spring/webmvc/interceptor/AbstractPageRenderContextHandlerInterceptor.java` | `M` | `implements HandlerInterceptor`（`:20`） | `extends HandlerInterceptorAdapter`（`:20`） | **API 兼容** —— `HandlerInterceptor` 的 default 方法从 5.0 才有 |
| `io/microsphere/spring/webmvc/util/ViewUtils.java#getResponseStatus` | `M` | 返回 `HttpStatusCode`（`:50`） | 返回 `HttpStatus`（`:50`） | **API 兼容** —— `HttpStatusCode` 是 Spring 6 引入的抽象 |
| `io/microsphere/spring/webmvc/handler/ReversedProxyHandlerMapping.java` | `M` | 填充缓存时复用 `isAbstractHandlerMapping()` 闸门 | 把来源类型判断内联展开 | **功能需要/重构** —— 0.2.x 收紧了闸门，额外要求 `Kind.WEB_FLUX`/`WEB_MVC` |

该模块其余被修改文件只是命名空间、Javadoc 或 JDK 语法差异。

## 9. `microsphere-spring-test`

| 区域 | `0.2.39` | `0.1.39` | 原因 |
|---|---|---|---|
| 删除的夹具 `io/microsphere/spring/test/webmvc/PersonHandler.java`、`…/webmvc/RouterFunctionTestConfig.java` | 存在 | 不存在 | **功能需要** —— 0.1.x 不提供 WebFlux/router function 测试脚手架 |
| `io/microsphere/spring/test/webmvc/AbstractWebMvcTest.java` | `@SpringJUnitConfig({TestController, RouterFunctionTestConfig})`、`protected` 钩子、含 `testUpdatePerson()` 走 `PUT /test/person/{id}` | `@RunWith(SpringRunner.class)` + `public` 方法，无 `testUpdatePerson` | **测试框架 / 功能需要** |
| `io/microsphere/spring/test/webflux/AbstractWebFluxTest.java` | 有聚合入口 `testWebEndpoints()`（`:77`） | 无聚合入口，但多一个 `testView()` | **功能需要** |
| `io/microsphere/spring/test/webmvc/RouterFunctionTestConfig.java` | 从 `WebTestUtils` import 常量 | 重新声明常量 | **API 兼容** —— 让夹具在老版本线上自包含 |
| 内嵌 Tomcat 部署逻辑 | 未变 | 未变 | 14 个被修改 main 文件中有 10 个纯属 `jakarta.servlet` ↔ `javax.servlet`；部署相关行号（`37/59/101/179/203/213/223/227`）完全一致，且两个 tag 都没有使用 Servlet 6 独有 API（`getAllTlds`、`SessionCookieConfig` 在两边均不存在） |

`WebTestUtils` 零差异。

## 10. `microsphere-spring-guice`、`microsphere-spring-jdbc`、`microsphere-spring-dependencies`、`microsphere-spring-parent`

| 模块 | `src/main` 结论 |
|---|---|
| `microsphere-spring-guice` | **无差异。** Guice 桥接完全一致；只有测试源码与 POM 里的 JUnit 坐标不同（0.2.39 的 `pom.xml:58-59` 是 `junit-jupiter` ↔ 0.1.39 的 `:51-52` 是 `junit`） |
| `microsphere-spring-jdbc` | **无差异。** 同样的 P6Spy `DataSource` 装饰实现，两边都是 P6Spy `3.9.1` |
| `microsphere-spring-dependencies` | **BOM 字节级相同。** 两个 tag 的 BOM 都不含 `microsphere-spring-compatible`，尽管 `0.1.39` 会构建该模块 |
| `microsphere-spring-parent` | 无 `src/main`；差异仅在 `<java.version>`、命名空间 BOM import 以及 Spring/Reactor profile 矩阵（见 §2） |

## 11. 如何解读这份差异

三类机制几乎解释了 0.1.x main 侧全部实质性代码：

1. **能力探测 + 回退。** 在类初始化阶段用 `MethodHandle`/反射查表
   （`findVirtual`、`findStatic`、`findDeclaredMethod`、`resolveClass`、`findMethod`）判断较新的
   Spring API 是否存在，每个调用点都依据该结果分支。`@since Spring Framework x.y` 的 Javadoc 标记
   记录了每段垫片对应的最低版本。
2. **重新声明 Spring 垫片。** 当某个基类或常量已消失（或尚未出现）时 ——
   `InstantiationAwareBeanPostProcessorAdapter`、`WebMvcConfigurerAdapter`、
   `MediaType.SPECIFICITY_COMPARATOR`、`ObjectProvider` 的 stream/iterator —— 库自己持有一份。
3. **删除 `@Override`。** 覆盖一个在最老支持版本中可能不存在的方法会导致编译失败，因此 0.1.x 去掉注解
   （有时保留解释性注释）。

反过来，0.2.x 独有的新增内容（`web/servlet/function/*`、
`HandlerMappingWebEndpointMappingResolver#resolveFromRouterFunctionMapping`、`MediaTypeUtils`）
都是依赖 Spring 5.2+/6/7 API 的功能，因此从未被回移到 0.1.x。

## 12. 相关文档

- [test-diff-0.2.39-vs-0.1.39.md](./test-diff-0.2.39-vs-0.1.39.md) —— `src/test` 差异（简体中文）
- [English version](../main-diff-0.2.39-vs-0.1.39.md)
- [文档总索引](../../../zh/README.md)
