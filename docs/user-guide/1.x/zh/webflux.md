# microsphere-spring-webflux（1.x）

[← 1.x 版本总览](./README.md) · [English](../webflux.md) · [开发者文档](../../../developer-guide/1.x/zh/webflux.md) · [0.2.x 文档](../../current/zh/webflux.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-webflux` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | **5.0.x – 5.3.x**（WebFlux 不存在于 4.3.x） |
| Reactor | 由 `spring-webflux` 传递引入，版本随 profile 而变 |

Reactor BOM 由父 POM profile 决定（`reactor-bom` 导入）：

| Profile | Spring Framework | Reactor BOM |
|---------|------------------|-------------|
| `spring-framework-5.0` | `5.0.20.RELEASE` | `Bismuth-SR17` |
| `spring-framework-5.1` | `5.1.20.RELEASE` | `Californium-SR23` |
| `spring-framework-5.2` | `5.2.25.RELEASE` | `Dysprosium-SR25` |
| `spring-framework-5.3`（默认） | `5.3.39` | `2020.0.47` |

> ⚠️ **4.3 不可用**：`spring-webflux` 这个 artifact 只在 Spring 5.0+ 存在。用 `-P spring-framework-4.3` 构建整个 reactor 时，本模块无法解析依赖；若锁定 4.3，请在 `<modules>` 中排除本模块。

> ⚠️ **函数式端点元数据的版本门槛**：`RequestPredicateVisitorAdapter` 通过反射查找
> `org.springframework.web.reactive.function.server.RequestPredicates$Visitor`（**Spring 5.1 起才有**），
> 并在 `RequestPredicate.accept(Visitor)` 存在时才工作。类中这两个静态字段都标注 `@Nullable`：在 5.0 上它们为
> `null`，访问者链路静默降级（不抛异常，但拿不到函数式端点的条件细节）。

依赖：`microsphere-spring-web`（必需）；`spring-context` / `spring-webflux` / Jackson 为 `optional`；`microsphere-annotation-processor` 编译期可选。测试使用 JUnit 5（`junit-jupiter`）。

---

## 1. `@EnableWebFluxExtension`

```java
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@EnableWebExtension
@OverrideAnnotationAttributes
@Import(WebFluxExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebFluxExtension {

    @AliasFor(annotation = EnableWebExtension.class) boolean registerWebEndpointMappings() default true;
    @AliasFor(annotation = EnableWebExtension.class) boolean interceptHandlerMethods() default true;
    @AliasFor(annotation = EnableWebExtension.class) boolean publishEvents() default true;
    @AliasFor(annotation = EnableWebExtension.class) BeanSource[] sources() default {...};
    @AliasFor(annotation = EnableWebExtension.class) RequestContextStrategy requestContextStrategy() default DEFAULT;

    boolean storeRequestBodyArgument() default false;
    boolean storeResponseBodyReturnValue() default false;
    boolean reversedProxyHandlerMapping() default false;
}
```

| 属性 | 默认值 | 桥接 | 语义 |
|------|--------|------|------|
| `registerWebEndpointMappings` | `true` | `EnableWebExtension` | 注册 `HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods` | `true` | 同上 | 注册 `InterceptingHandlerMethodProcessor` |
| `publishEvents` | `true` | 同上 | 注册 `RequestHandledEventPublishingWebFilter` |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 同上 | SPI 发现来源（由 web 模块 registrar 消费） |
| `requestContextStrategy` | `DEFAULT` | 同上 | **本模块真正读取**，见第 3 节 |
| `storeRequestBodyArgument` | `false` | WebFlux 专属 | 注册 `StoringRequestBodyArgumentInterceptor` |
| `storeResponseBodyReturnValue` | `false` | WebFlux 专属 | 注册 `StoringResponseBodyReturnValueInterceptor` |
| `reversedProxyHandlerMapping` | `false` | WebFlux 专属 | 注册 `ReversedProxyHandlerMapping` |

> 与 `@EnableWebMvcExtension` 的关键差异：WebFlux 侧**没有** `registerHandlerInterceptors` / `handlerInterceptors`（响应式没有 `HandlerInterceptor` 概念），也**不需要** `ServletContainerInitializer`——`requestContextStrategy` 由本注解的 registrar 直接落实。

`WebFluxExtensionBeanDefinitionRegistrar` 注册总览：

| 条件 | Bean 名 | 类 |
|------|---------|-----|
| `registerWebEndpointMappings` | 默认（按类型） | `webflux.metadata.HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods` | `interceptingHandlerMethodProcessor`（常量 `BEAN_NAME`） | `webflux.method.InterceptingHandlerMethodProcessor` |
| `publishEvents` | 默认 | `webflux.server.filter.RequestHandledEventPublishingWebFilter` |
| `requestContextStrategy != DEFAULT` | `requestContextWebFilter`（`uncapitalize(SimpleName)`） | `webflux.server.filter.RequestContextWebFilter`，并注入 `threadContextInheritable` 属性 |
| `storeRequestBodyArgument` | 默认 | `webflux.method.StoringRequestBodyArgumentInterceptor` |
| `storeResponseBodyReturnValue` | 默认 | `webflux.method.StoringResponseBodyReturnValueInterceptor` |
| `reversedProxyHandlerMapping` | 默认 | `webflux.handler.ReversedProxyHandlerMapping` |

```java
@Configuration
@EnableWebFluxExtension(
        registerWebEndpointMappings = true,
        interceptHandlerMethods = true,
        storeRequestBodyArgument = true,
        requestContextStrategy = RequestContextStrategy.INHERITABLE_THREAD_LOCAL
)
public class WebFluxConfig {
}
```

## 2. `InterceptingHandlerMethodProcessor`：一个 Bean，五种角色

```java
public class InterceptingHandlerMethodProcessor
        extends OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>
        implements WebFilter, HandlerAdapter, HandlerMethodArgumentResolver,
                   HandlerResultHandler, WebExceptionHandler, Ordered {

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";
}
```

它把响应式请求处理的整条链路都过一遍，从而让 [web.md](./web.md) 里的 `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` 在 WebFlux 中同样生效：

| 实现的接口 | 承担的职责 | 关键方法 |
|------------|------------|----------|
| `WebFilter` | 把 `ServerWebExchange` 放入请求上下文（委托给内部静态 `RequestContextWebFilter`） | `filter(ServerWebExchange, WebFilterChain)` |
| `HandlerAdapter` | 判断/执行 `HandlerMethod` | `supports(Object)`、`handle(ServerWebExchange, Object)` |
| `HandlerMethodArgumentResolver` | 参数解析前后回调 | `supportsParameter(MethodParameter)`、`resolveArgument(...)` |
| `HandlerResultHandler` | 返回值处理前后回调 | `supports(HandlerResult)`、`handleResult(...)` |
| `WebExceptionHandler` | 异常路径回调 | `handle(ServerWebExchange, Throwable)` |
| `Ordered` | 控制它在链中的位置 | `getOrder()` |

内部维护两个 `MethodParameter` 缓存（初始容量 256 的 `HashMap`）以及 `HandlerMethodAdvice` / `HandlerResultHandler` 列表。基类 `OnceApplicationContextEventListener`（位于 **microsphere-spring-context**，包 `io.microsphere.spring.context.event`）把 `onApplicationEvent` 置为 `final`，先过滤掉 `source` 不是本上下文的 `ApplicationContextEvent`，再回调抽象方法 `onApplicationContextEvent`。本类的覆写依次执行 `initHandlerMethodAdvices` / `initHandlerResultHandlers` / `initRequestMappingHandlerAdapters`，也就是说**装配发生在 `WebEndpointMappingsReadyEvent` 到达之后**——`interceptHandlerMethods` 与 `registerWebEndpointMappings` 需要同时开启才能拿到完整拦截链。

```java
@Component
public class TimingInterceptor implements HandlerMethodInterceptor {

    @Override
    public void beforeExecute(HandlerMethod hm, Object[] args, NativeWebRequest request) {}

    @Override
    public void afterExecute(HandlerMethod hm, Object[] args, @Nullable Object ret,
                             @Nullable Throwable error, NativeWebRequest request) {
        // WebFlux 下 request 实际是 ServerWebRequest
        ServerWebExchange exchange = request.getNativeRequest(ServerWebExchange.class);
        ...
    }
}
```

## 3. `ServerWebRequest`：把响应式桥接进 `NativeWebRequest`

`io.microsphere.spring.webflux.context.request.ServerWebRequest` 实现 `NativeWebRequest`，构造入参为 `ServerWebExchange`：

```java
public class ServerWebRequest implements NativeWebRequest {
    public static final String REMOTE_USER_ATTRIBUTE_NAME = "REMOTE_USER";
    public static final String SESSION_MUTEX_ATTRIBUTE_NAME = "org.springframework.web.util.WebUtils.MUTEX";
    public static final String REFERENCE_KEY_REQUEST  = "request";
    public static final String REFERENCE_KEY_RESPONSE = "response";
    public static final String REFERENCE_KEY_SESSION  = "session";

    public ServerWebRequest(ServerWebExchange exchange) { ... }
}
```

| 方法 | 行为 |
|------|------|
| `getNativeRequest()` / `getNativeResponse()` | 返回 `ServerHttpRequest` / `ServerHttpResponse`，**非空** |
| `getNativeRequest(Class<T>)` / `getNativeResponse(Class<T>)` | 仅在类型可赋值时返回，否则返回 **`null`**（不抛异常）——因此 `request.getNativeRequest(HttpServletRequest.class)` 在 WebFlux 下恒为 `null` |
| `getHeader(String)` | 从 `request.getHeaders()` 读取 |
| 属性读写 | 落在 `ServerWebExchange` 的属性图上 |

配套的 `io.microsphere.spring.web.util.WebServerUtils` 提供 `getSession(exchange)`、`getPrincipal(exchange)`、`getSessionId(exchange)`、`getUserName(exchange)`；`io.microsphere.spring.web.util.MonoUtils` 提供 `getValue(Mono<T>)`——在**已经订阅完成**的 `Mono` 上同步取值，切勿在响应式主链路上调用（会阻塞事件循环）。

## 4. 请求上下文与事件

| 类 | 说明 |
|----|------|
| `server.filter.RequestContextWebFilter` | `WebFilter`，把 exchange 绑定到线程上下文；属性 `threadContextInheritable`（`Boolean`，可为 `null`） |
| `server.filter.DelegatingWebFilter` | 委托型 `WebFilter` 基类，便于按需覆写 |
| `server.filter.CompositeWebFilter` | 组合多个 `WebFilter` 并按 `Ordered` 执行 |
| `server.filter.RequestHandledEventPublishingWebFilter` | `publishEvents = true` 时注册；发布 `ServerRequestHandledEvent` |
| `webflux.context.event.ServerRequestHandledEvent` | `extends RequestHandledEvent`（Spring 为 Servlet 设计的那个）；提供 `getWebHandler()`、`getExchange()`；构造器为 `(WebHandler, ServerWebExchange, ...)` 两个重载 |

```java
@EventListener
public void onHandled(ServerRequestHandledEvent event) {
    log.info("status = {}, time = {}ms",
            event.getExchange().getResponse().getStatusCode(),
            event.getTimeMillis());
}
```

## 5. 端点元数据：注解式与函数式

`webflux.metadata` 下三个工厂 + 一个解析器，与 MVC 侧同名对应：

| 类 | 输入 | 产出 |
|----|------|------|
| `HandlerMappingWebEndpointMappingFactory` | `HandlerMapping`（`@RequestMappingHandlerMapping`） | `WebEndpointMapping.Kind.WEB_FLUX` 的 mapping |
| `HandlerMetadataWebEndpointMappingFactory` | `HandlerMetadata` | 同上 |
| `RequestMappingMetadataWebEndpointMappingFactory` | `@RequestMapping` 结构化元数据 | 同上 |
| `HandlerMappingWebEndpointMappingResolver` | 请求 | 目标端点（由 registrar 条件注册） |

函数式路由（`RouterFunction` / `RequestPredicate`）由 `webflux.function.server` 处理：

| 类 | 形态 | 作用 |
|----|------|------|
| `RequestPredicateVisitorAdapter` | `interface extends InvocationHandler` | 适配 Spring 的 `RequestPredicates.Visitor`；提供 `method(Set<HttpMethod>)`、`path(String)`、`pathExtension(String)`、`header(String, String)`、`queryParam(String, String)` 等 default 回调；通过 `Proxy`/反射驱动，**需 Spring 5.1+** |
| `RouterFunctionVisitorAdapter` | `interface extends RouterFunctions.Visitor` | 遍历 `RouterFunction` 结构（`routes`、`nest`、`andOtherResources` 等） |
| `ConsumingWebEndpointMappingAdapter` | `class implements` 上述两个适配器 | 同时接收谓词与路由通知，累积成 `WebEndpointMapping` |
| `RequestPredicateKind` | `enum` | 谓词类型枚举，附静态工具：`valueOf(RequestPredicate)`、`parseRequestPredicate(String)`、`toExpression(RequestPredicate)` |

```java
// 把一个函数式路由描述成表达式，便于核对采集结果
RequestPredicate predicate = RequestPredicates.GET("/products/{id}")
        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON));

RequestPredicateKind kind = RequestPredicateKind.valueOf(predicate);
log.info("kind = {}, expression = {}", kind, RequestPredicateKind.toExpression(predicate));
```

> 这三者是 **1.x 与 `WebEndpointMappingVisitor` 配合的产物**：0.2.x 分支既没有 `WebEndpointMappingVisitor`，也没有 `webflux.function.server` 包。迁移到 0.2.x 时函数式端点的采集方式需要重新评估。

## 6. 反向代理 HandlerMapping

`io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping` 与 MVC 侧同名同策略：

```java
public class ReversedProxyHandlerMapping extends AbstractHandlerMapping
        implements ApplicationListener<WebEndpointMappingsReadyEvent> {

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;   // 构造器里 setOrder
}
```

- 同样读取请求头 `microsphere_wem_id`（`WebEndpointMapping.ID_HEADER_NAME`）；
- 同样在 `WebEndpointMappingsReadyEvent` 时建立 `id -> mapping` 缓存；
- 区别在于解析出的 handler 走响应式执行链，而非 `HandlerExecutionChain`。

```java
@Configuration
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
public class ReactiveGatewayConfig {
}
```

## 7. `SpringWebHelper` 绑定

本模块的 `META-INF/spring.factories` 只声明一行，供 web 模块判定"当前是 MVC 还是 WebFlux"：

```
io.microsphere.spring.web.util.SpringWebHelper=\
io.microsphere.spring.webflux.util.SpringWebFluxHelper
```
