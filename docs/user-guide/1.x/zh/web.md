# microsphere-spring-web（1.x）

[← 1.x 版本总览](./README.md) · [English](../web.md) · [开发者文档](../../../developer-guide/1.x/zh/web.md) · [0.2.x 文档](../../current/zh/web.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-web` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | **4.3.x – 5.3.x** |
| Servlet API | `javax.servlet:javax.servlet-api`（`optional` / provided） |
| 响应式 | `reactor-core` 仅 test 作用域，本模块不依赖 WebFlux |

依赖姿态：

| 依赖 | 说明 |
|------|------|
| `microsphere-java-core` | 必需（compile） |
| `microsphere-spring-context` | 必需（本库内部依赖，非 optional） |
| `spring-context` / `spring-web` | `optional`，由应用提供 |
| `javax.servlet-api` | `optional`，Servlet 3+ 由容器提供 |
| `jackson-annotations` / `jackson-databind` | `optional`，仅 `Jackson2WebEndpointMappingFactory` 需要 |
| `com.google.code.findbugs:jsr305` | `optional`，编译期注解 |

本模块是 **MVC 与 WebFlux 共用的抽象层**：端点元数据模型、注册/解析 SPI、控制器方法拦截 SPI。它自己不做路由，需要与 [webmvc.md](./webmvc.md) 或 [webflux.md](./webflux.md) 搭配使用。

> 1.x 与 0.2.x 的本模块 API 基本一致，`@interface` 属性集相同；差异集中在 Servlet 命名空间（`javax.servlet` 而非 `jakarta.servlet`）以及 1.x 独有的 `WebEndpointMappingVisitor`。

---

## 1. `@EnableWebExtension`

`io.microsphere.spring.web.annotation.EnableWebExtension`：

```java
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Documented
@Inherited
@Import(WebExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebExtension {

    boolean registerWebEndpointMappings() default true;

    boolean interceptHandlerMethods() default true;

    boolean publishEvents() default true;

    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER};

    RequestContextStrategy requestContextStrategy() default DEFAULT;
}
```

| 属性 | 默认值 | 语义 |
|------|--------|------|
| `registerWebEndpointMappings` | `true` | 注册端点元数据相关的 Resolver / Registry / Factory / Filter / Registrar |
| `interceptHandlerMethods` | `true` | 注册 `HandlerMethodAdvice` 与两类拦截器，把控制器方法包进 AOP |
| `publishEvents` | `true` | 注册 `WebEventPublisher`，发布端点就绪 / 参数解析事件 |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 从哪些来源发现 SPI 实现（`io.microsphere.spring.beans.BeanSource`） |
| `requestContextStrategy` | `DEFAULT` | 请求上下文保存策略，见下方"重要提示" |

`WebExtensionBeanDefinitionRegistrar` 实际动作（源码逐条核对）：

1. `registerWebEndpointMappings = true` 时：
   - 按 `sources` 注册所有 `WebEndpointMappingResolver`、`WebEndpointMappingFactory`、`WebEndpointMappingFilter` 实现；
   - Registry 处理有特殊逻辑：一个都没注册时，注册默认的 `SimpleWebEndpointMappingRegistry`；否则把已注册的每个 Registry 的 `primary` 置为 `false`，再注册 `CompositeWebEndpointMappingRegistry` 作为主 Bean；
   - 注册 `WebEndpointMappingRegistrar`（在上下文刷新时收集端点元数据）。
2. `interceptHandlerMethods = true` 时：注册 `HandlerMethodAdvice` 实现、`HandlerMethodArgumentInterceptor`、`HandlerMethodInterceptor`，并以固定 Bean 名注册 `DelegatingHandlerMethodAdvice`。
3. `publishEvents = true` 时：注册 `WebEventPublisher`。

> ⚠️ 1.x 中，`@EnableWebExtension` 自身的 registrar **并不读取** `requestContextStrategy`——它只在注解上声明。真正消费该属性的是 `EnableWebMvcExtensionListener`（webmvc）和 `WebFluxExtensionBeanDefinitionRegistrar`（webflux）。因此若你只启用 `@EnableWebExtension`，请改用模块级注解（`@EnableWebMvcExtension` / `@EnableWebFluxExtension`）来设置请求上下文策略。

`RequestContextStrategy` 取值：

```java
public enum RequestContextStrategy {
    DEFAULT,                     // 沿用 Spring 默认行为
    THREAD_LOCAL,                // 普通 ThreadLocal
    INHERITABLE_THREAD_LOCAL;    // InheritableThreadLocal，子线程可见
}
```

用法：

```java
@Configuration
@EnableWebMvcExtension(          // 推荐：模块级注解会继承本注解的属性
        registerWebEndpointMappings = true,
        interceptHandlerMethods = true,
        requestContextStrategy = RequestContextStrategy.INHERITABLE_THREAD_LOCAL
)
public class WebConfig {
}
```

## 2. `WebEndpointMapping<E>`：统一的端点描述

`io.microsphere.spring.web.metadata.WebEndpointMapping` 是一个不可变泛型值对象，`E` 是被描述的端点类型（`ServletRegistrationBean`、`FilterRegistrationBean`、`HandlerMethod`、`RequestMappingInfo` 等）。

关键字段与常量：

| 成员 | 说明 |
|------|------|
| `Kind kind` | `SERVLET` / `FILTER` / `WEB_MVC` / `WEB_FLUX` / `CUSTOMIZED` |
| `E endpoint` | 端点对象本身（`transient`） |
| `int id` | ⚠ `Objects.hashCode(endpoint)`——该 ID 是**端点对象**的哈希码（`WebEndpointMapping.java:1275`），并非由模式或条件摘要计算而来；网关透传匹配使用的就是它 |
| `String[] patterns / methods` | 路径模式与 HTTP 方法 |
| `String[] params / headers / consumes / produces` | 条件（可为 `null`） |
| `boolean negated` | 取反 |
| `Object source` | 来源对象（`UNKNOWN_SOURCE` 为占位） |
| `ID_HEADER_NAME = "microsphere_wem_id"` | 反向代理透传的 header 名 |
| `ALL_METHODS` | 全部 `HttpMethod` 名称集合 |

Builder 为**私有构造 + 静态入口**，按 `Kind` 选择：

```java
public static <E> Builder<E> servlet();
public static <E> Builder<E> filter();
public static <E> Builder<E> webmvc();
public static <E> Builder<E> webflux();
public static <E> Builder<E> customized();
public static <E> Builder<E> of(Kind kind);
```

链式方法（节选，均已核对签名）：`endpoint(E)`、`pattern(String)`、`patterns(String...)` / `patterns(Collection)` / `patterns(Collection, Function)`、`method(HttpMethod)` / `method(String)`、`methods(HttpMethod...)` / `methods(String...)` / `methods(Collection)`、`param(name, value)` / `param(nameAndValue)`、`header(name, value)` / `header(nameAndValue)`、`consume(MediaType|String)`、`produce(...)`、`negate()`、`build()`。

> `header(name, value)` 有便捷分支：`name` 为 `Content-Type` 时转发到 `consume(value)`，为 `Accept` 时转发到 `produce(value)`——不必再单独调用。

```java
WebEndpointMapping<HandlerMethod> mapping = WebEndpointMapping.<HandlerMethod>webmvc()
        .endpoint(handlerMethod)
        .patterns("/products/{id}", "/items/{id}")
        .methods("GET")
        .param("version", "v2")
        .header("X-Tenant", "acme")
        .produce(MediaType.APPLICATION_JSON)
        .build();

log.info("id = {}, expression = {}", mapping.getId(), mapping.toExpression());
```

常用读取方法：`getKind()`、`getId()`、`getSource()`、`toExpression()`（渲染成人类可读的组合表达式，用于日志与网关规则）。`negate()` 在 Builder 上调用，产出的 mapping 语义为"不匹配这些条件"。

## 3. SPI 一览

| 接口 | 职责 | 发现来源 |
|------|------|----------|
| `WebEndpointMappingFactory<E, T>` | 把某个源对象（`HandlerMethod`、`ServletRegistration`…）转成 `WebEndpointMapping` | `sources` |
| `SmartWebEndpointMappingFactory` | 能自行判断是否支持该源对象的工厂 | `sources` |
| `AbstractWebEndpointMappingFactory` / `RegistrationWebEndpointMappingFactory` / `ServletRegistrationWebEndpointMappingFactory` / `FilterRegistrationWebEndpointMappingFactory` | 逐级细化的抽象基类与内置实现 | 内置 |
| `Jackson2WebEndpointMappingFactory` | 用 Jackson 把外部（如 JSON）描述反序列化为 mapping | `sources`（需 jackson-databind） |
| `WebEndpointMappingRegistry` | 保存 mapping 的注册表；默认 `SimpleWebEndpointMappingRegistry`，多实现时由 `CompositeWebEndpointMappingRegistry` 聚合 | `sources` |
| `FilteringWebEndpointMappingRegistry` | 带过滤逻辑的注册表基类 | 内置 |
| `WebEndpointMappingResolver` | 从请求/上下文解析目标端点；`ServletWebEndpointMappingResolver` 面向 Servlet | `sources` |
| `WebEndpointMappingFilter` | 收集阶段的谓词 | `sources` |
| `WebEndpointMappingRegistrar` | 在 `WebEndpointMappingsReadyEvent` 之前把工厂产出写入注册表 | 由 registrar 固定注册 |
| `WebEndpointMappingVisitor` | **1.x 独有**：以访问者形式遍历 mapping 的条件树 | 自定义 |

> ⚠️ `WebEndpointMappingVisitor` 在 1.x 中**只有接口定义**：`git grep` 显示它没有任何库内实现，也没有调用点（0.2.x 分支完全不存在该接口）。因此它是一个纯扩展点——需要你自行编写遍历 `WebEndpointMapping` 的驱动代码。

```java
void method(String[] methods);
void path(String pattern);
void header(String name, String value);
void queryParam(String name, String value);
void startAnd();  void and();      void endAnd();
void startOr();   void or();       void endOr();
void startNegate(); void endNegate();
```

适合把 mapping 编译成外部网关 DSL（Envoy / Nginx / 自研路由规则）：

```java
public class DslPrinter implements WebEndpointMappingVisitor {

    private final StringBuilder out = new StringBuilder();

    @Override public void path(String pattern)   { out.append("path(").append(pattern).append(")"); }
    @Override public void method(String[] ms)    { out.append("method[").append(String.join("|", ms)).append("]"); }
    @Override public void startAnd()             { out.append(" and("); }
    @Override public void and()                  { out.append(","); }
    @Override public void endAnd()               { out.append(")"); }
    @Override public void startOr()              { out.append(" or("); }
    @Override public void or()                   { out.append(","); }
    @Override public void endOr()                { out.append(")"); }
    @Override public void startNegate()          { out.append(" not("); }
    @Override public void endNegate()            { out.append(")"); }
    @Override public void header(String n, String v) { out.append("header(").append(n).append("=").append(v).append(")"); }
    @Override public void queryParam(String n, String v) { out.append("param(").append(n).append("=").append(v).append(")"); }
}
```

## 4. 控制器方法拦截 SPI

两个接口都定义在 `io.microsphere.spring.web.method.support`，参数中的 `HandlerMethod` / `MethodParameter` 来自 `spring-web`，`NativeWebRequest` 使它们同时适用于 MVC 与 WebFlux（WebFlux 侧由 `ServerWebRequest` 适配）。

```java
public interface HandlerMethodInterceptor {

    default void beforeExecute(HandlerMethod handlerMethod, Object[] args,
                               NativeWebRequest request) throws Exception {}

    default void afterExecute(HandlerMethod handlerMethod, Object[] args,
                              @Nullable Object returnValue, @Nullable Throwable error,
                              NativeWebRequest request) throws Exception {}
}

public interface HandlerMethodArgumentInterceptor {

    default void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod,
                                       NativeWebRequest webRequest) throws Exception {}

    default void afterResolveArgument(MethodParameter parameter, Object resolvedArgument,
                                      HandlerMethod handlerMethod,
                                      NativeWebRequest webRequest) throws Exception {}
}
```

全部方法都是 `default`，只实现你关心的那个即可。`afterExecute` 同时给出返回值和异常，二者都可能为 `null`——不要假定只会有一个非空。

```java
@Component
public class ApiMetricsInterceptor implements HandlerMethodInterceptor {

    private static final ThreadLocal<Long> START = new ThreadLocal<>();

    @Override
    public void beforeExecute(HandlerMethod hm, Object[] args, NativeWebRequest request) {
        START.set(System.nanoTime());
    }

    @Override
    public void afterExecute(HandlerMethod hm, Object[] args, @Nullable Object returnValue,
                             @Nullable Throwable error, NativeWebRequest request) {
        long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - START.get());
        START.remove();
        log.info("{}#{} cost={}ms error={}",
                hm.getBeanType().getSimpleName(), hm.getMethod().getName(), cost,
                error == null ? "-" : error.getClass().getSimpleName());
    }
}
```

`HandlerMethodAdvice` / `DelegatingHandlerMethodAdvice`（Bean 名固定）是把这些拦截器织入控制器的内部桥梁；一般无需直接实现——除非你要自定义通知语义。

## 5. 事件

| 事件 | 触发时机 | 载荷 |
|------|----------|------|
| `WebEndpointMappingsReadyEvent` | 端点元数据收集完成（上下文已就绪） | 全部 `WebEndpointMapping` |
| `HandlerMethodArgumentsResolvedEvent` | 一次控制器调用的参数全部解析完毕 | `HandlerMethod` + 已解析参数 |

```java
@EventListener
public void onMappingsReady(WebEndpointMappingsReadyEvent event) {
    event.getMappings().forEach(m ->
            log.info("[{}] {} -> id={}", m.getKind(), m.toExpression(), m.getId()));
}
```

`WebEventPublisher` 负责发布上述事件，`publishEvents = false` 时不会被注册。

事件访问器（源码核对）：

| 事件 | 可访问方法 |
|------|------------|
| `WebEndpointMappingsReadyEvent` | `extends ApplicationContextEvent`；`getMappings()` → `Collection<WebEndpointMapping>` |
| `HandlerMethodArgumentsResolvedEvent` | `getHandlerMethod()`、`getMethod()`、`getArguments()`、`getWebRequest()`；构造器为 `(WebRequest, HandlerMethod, Object...)` |

## 6. 工具类

| 类 | 用途 |
|----|------|
| `io.microsphere.spring.web.util.WebRequestUtils` | 从 `NativeWebRequest` 读写属性、取 `HttpServletRequest/Response` |
| `io.microsphere.spring.web.util.RequestAttributesUtils` | `RequestAttributes` 层次与属性操作 |
| `io.microsphere.spring.web.util.HttpUtils` | HTTP 方法/状态、header 解析 |
| `io.microsphere.spring.web.util.WebUtils` | 请求参数、cookie 等通用读取 |
| `io.microsphere.spring.web.util.SpringWebHelper` / `SpringWebType` / `WebType` / `WebSource` / `WebTarget` | 判断当前是 MVC 还是 WebFlux、归一化 Web 类型 |
| `io.microsphere.spring.web.rule.*` | 条件表达式实现（`WebRequestPatternsRule`、`WebRequestMethodsRule`、`WebRequestParamsRule`、`WebRequestHeadersRule`、`WebRequestConsumesRule`、`WebRequestProducesRule`、`CompositeWebRequestRule` 等） |

> `WebRequestPatternsRule` 类名有拼写偏差（`Pattens`），使用时按实际类名引用。

`io.microsphere.spring.web.constants.PropertyConstants` 提供前缀常量：

```java
String MICROSPHERE_SPRING_WEB_PROPERTY_NAME_PREFIX = "microsphere.spring.web.";
```
