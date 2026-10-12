# microsphere-spring-webflux (1.x)

[← 1.x overview](./README.md) · [简体中文](./zh/webflux.md) · [Developer guide](../../developer-guide/1.x/webflux.md) · [0.2.x docs](../current/webflux.md)

## Version information

| Item | Value |
|------|-----|
| artifactId | `microsphere-spring-webflux` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version under development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility | **5.0.x – 5.3.x** (WebFlux does not exist in 4.3.x) |
| Reactor | pulled in transitively by `spring-webflux`, version depends on the active profile |

The Reactor BOM is decided by the parent POM profile (an imported `reactor-bom`):

| Profile | Spring Framework | Reactor BOM |
|---------|------------------|-------------|
| `spring-framework-5.0` | `5.0.20.RELEASE` | `Bismuth-SR17` |
| `spring-framework-5.1` | `5.1.20.RELEASE` | `Californium-SR23` |
| `spring-framework-5.2` | `5.2.25.RELEASE` | `Dysprosium-SR25` |
| `spring-framework-5.3` (default) | `5.3.39` | `2020.0.47` |

> ⚠️ **Not available on 4.3**: the `spring-webflux` artifact only exists for Spring 5.0+. When the whole
> reactor is built with `-P spring-framework-4.3`, this module cannot resolve its dependencies; if you
> pin 4.3, exclude this module from `<modules>`.

> ⚠️ **Version floor for functional endpoint metadata**: `RequestPredicateVisitorAdapter` looks up
> `org.springframework.web.reactive.function.server.RequestPredicates$Visitor` reflectively (**present only
> since Spring 5.1**) and works only when `RequestPredicate.accept(Visitor)` exists. Both static fields in
> the class are annotated `@Nullable`: on 5.0 they are `null` and the visitor chain degrades silently
> (no exception is thrown, but the condition details of functional endpoints are lost).

Dependencies: `microsphere-spring-web` (required); `spring-context` / `spring-webflux` / Jackson are
`optional`; `microsphere-annotation-processor` is optional at compile time. Tests use JUnit 5
(`junit-jupiter`).

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

| Attribute | Default | Bridged to | Semantics |
|-----------|---------|------------|-----------|
| `registerWebEndpointMappings` | `true` | `EnableWebExtension` | registers `HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods` | `true` | same | registers `InterceptingHandlerMethodProcessor` |
| `publishEvents` | `true` | same | registers `RequestHandledEventPublishingWebFilter` |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | same | SPI discovery sources (consumed by the web module's registrar) |
| `requestContextStrategy` | `DEFAULT` | same | **actually read by this module**, see section 3 |
| `storeRequestBodyArgument` | `false` | WebFlux-only | registers `StoringRequestBodyArgumentInterceptor` |
| `storeResponseBodyReturnValue` | `false` | WebFlux-only | registers `StoringResponseBodyReturnValueInterceptor` |
| `reversedProxyHandlerMapping` | `false` | WebFlux-only | registers `ReversedProxyHandlerMapping` |

> Key differences from `@EnableWebMvcExtension`: the WebFlux side has **no** `registerHandlerInterceptors` /
> `handlerInterceptors` (reactive programming has no `HandlerInterceptor` concept), and it does **not**
> need a `ServletContainerInitializer` — `requestContextStrategy` is applied directly by this annotation's
> registrar.

Overview of what `WebFluxExtensionBeanDefinitionRegistrar` registers:

| Condition | Bean name | Class |
|-----------|-----------|-------|
| `registerWebEndpointMappings` | default (by type) | `webflux.metadata.HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods` | `interceptingHandlerMethodProcessor` (constant `BEAN_NAME`) | `webflux.method.InterceptingHandlerMethodProcessor` |
| `publishEvents` | default | `webflux.server.filter.RequestHandledEventPublishingWebFilter` |
| `requestContextStrategy != DEFAULT` | `requestContextWebFilter` (`uncapitalize(SimpleName)`) | `webflux.server.filter.RequestContextWebFilter`, with the `threadContextInheritable` property injected |
| `storeRequestBodyArgument` | default | `webflux.method.StoringRequestBodyArgumentInterceptor` |
| `storeResponseBodyReturnValue` | default | `webflux.method.StoringResponseBodyReturnValueInterceptor` |
| `reversedProxyHandlerMapping` | default | `webflux.handler.ReversedProxyHandlerMapping` |

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

## 2. `InterceptingHandlerMethodProcessor`: one bean, five roles

```java
public class InterceptingHandlerMethodProcessor
        extends OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>
        implements WebFilter, HandlerAdapter, HandlerMethodArgumentResolver,
                   HandlerResultHandler, WebExceptionHandler, Ordered {

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";
}
```

It runs through the entire reactive request-handling chain, which is what makes the
`HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` SPIs from
[web.md](./web.md) work in WebFlux as well:

| Implemented interface | Responsibility | Key methods |
|-----------------------|----------------|-------------|
| `WebFilter` | puts the `ServerWebExchange` into the request context (delegates to an inner static `RequestContextWebFilter`) | `filter(ServerWebExchange, WebFilterChain)` |
| `HandlerAdapter` | decides whether / executes a `HandlerMethod` | `supports(Object)`, `handle(ServerWebExchange, Object)` |
| `HandlerMethodArgumentResolver` | callbacks before and after argument resolution | `supportsParameter(MethodParameter)`, `resolveArgument(...)` |
| `HandlerResultHandler` | callbacks before and after return-value handling | `supports(HandlerResult)`, `handleResult(...)` |
| `WebExceptionHandler` | callbacks on the exception path | `handle(ServerWebExchange, Throwable)` |
| `Ordered` | controls its position in the chain | `getOrder()` |

Internally it maintains two `MethodParameter` caches (`HashMap` with initial capacity 256) plus the
`HandlerMethodAdvice` / `HandlerResultHandler` lists. The base class `OnceApplicationContextEventListener`
(lives in **microsphere-spring-context**, package `io.microsphere.spring.context.event`) makes
`onApplicationEvent` `final`, first filters out any `ApplicationContextEvent` whose `source` is not this
context, and then calls the abstract method `onApplicationContextEvent`. The override here runs
`initHandlerMethodAdvices` / `initHandlerResultHandlers` / `initRequestMappingHandlerAdapters` in that
order, i.e. **assembly happens after `WebEndpointMappingsReadyEvent` arrives** — `interceptHandlerMethods`
and `registerWebEndpointMappings` must both be enabled to get the complete interception chain.

```java
@Component
public class TimingInterceptor implements HandlerMethodInterceptor {

    @Override
    public void beforeExecute(HandlerMethod hm, Object[] args, NativeWebRequest request) {}

    @Override
    public void afterExecute(HandlerMethod hm, Object[] args, @Nullable Object ret,
                             @Nullable Throwable error, NativeWebRequest request) {
        // under WebFlux, request is actually a ServerWebRequest
        ServerWebExchange exchange = request.getNativeRequest(ServerWebExchange.class);
        ...
    }
}
```

## 3. `ServerWebRequest`: bridging reactive into `NativeWebRequest`

`io.microsphere.spring.webflux.context.request.ServerWebRequest` implements `NativeWebRequest` and takes a
`ServerWebExchange` as its constructor argument:

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

| Method | Behaviour |
|--------|-----------|
| `getNativeRequest()` / `getNativeResponse()` | return `ServerHttpRequest` / `ServerHttpResponse`, **never null** |
| `getNativeRequest(Class<T>)` / `getNativeResponse(Class<T>)` | return the object only when the type is assignable, otherwise return **`null`** (no exception is thrown) — therefore `request.getNativeRequest(HttpServletRequest.class)` is always `null` under WebFlux |
| `getHeader(String)` | read from `request.getHeaders()` |
| attribute read/write | backed by the `ServerWebExchange` attribute map |

The companion `io.microsphere.spring.web.util.WebServerUtils` offers `getSession(exchange)`,
`getPrincipal(exchange)`, `getSessionId(exchange)`, `getUserName(exchange)`;
`io.microsphere.spring.web.util.MonoUtils` offers `getValue(Mono<T>)` — it reads the value synchronously
from a `Mono` that **has already completed subscription**, and must never be called on the main reactive
chain (it would block the event loop).

## 4. Request context and events

| Class | Description |
|-------|-------------|
| `server.filter.RequestContextWebFilter` | a `WebFilter` that binds the exchange to the thread context; property `threadContextInheritable` (`Boolean`, may be `null`) |
| `server.filter.DelegatingWebFilter` | delegating `WebFilter` base class, convenient for selective overriding |
| `server.filter.CompositeWebFilter` | composes several `WebFilter` instances and executes them by `Ordered` |
| `server.filter.RequestHandledEventPublishingWebFilter` | registered when `publishEvents = true`; publishes `ServerRequestHandledEvent` |
| `webflux.context.event.ServerRequestHandledEvent` | `extends RequestHandledEvent` (the one Spring designed for the Servlet stack); provides `getWebHandler()`, `getExchange()`; two constructor overloads of the form `(WebHandler, ServerWebExchange, ...)` |

```java
@EventListener
public void onHandled(ServerRequestHandledEvent event) {
    log.info("status = {}, time = {}ms",
            event.getExchange().getResponse().getStatusCode(),
            event.getTimeMillis());
}
```

## 5. Endpoint metadata: annotation-based and functional

Three factories plus one resolver under `webflux.metadata`, mirroring the MVC-side names:

| Class | Input | Output |
|-------|-------|--------|
| `HandlerMappingWebEndpointMappingFactory` | `HandlerMapping` (`@RequestMappingHandlerMapping`) | a mapping of `WebEndpointMapping.Kind.WEB_FLUX` |
| `HandlerMetadataWebEndpointMappingFactory` | `HandlerMetadata` | same as above |
| `RequestMappingMetadataWebEndpointMappingFactory` | structured metadata of `@RequestMapping` | same as above |
| `HandlerMappingWebEndpointMappingResolver` | a request | the target endpoint (conditionally registered by the registrar) |

Functional routing (`RouterFunction` / `RequestPredicate`) is handled by `webflux.function.server`:

| Class | Shape | Purpose |
|-------|-------|---------|
| `RequestPredicateVisitorAdapter` | `interface extends InvocationHandler` | adapts Spring's `RequestPredicates.Visitor`; provides default callbacks such as `method(Set<HttpMethod>)`, `path(String)`, `pathExtension(String)`, `header(String, String)`, `queryParam(String, String)`; driven through `Proxy`/reflection, **requires Spring 5.1+** |
| `RouterFunctionVisitorAdapter` | `interface extends RouterFunctions.Visitor` | walks the `RouterFunction` structure (`routes`, `nest`, `andOtherResources`, ...) |
| `ConsumingWebEndpointMappingAdapter` | `class implements` both adapters above | receives predicate and route notifications at the same time and accumulates them into a `WebEndpointMapping` |
| `RequestPredicateKind` | `enum` | predicate kind enum with static helpers: `valueOf(RequestPredicate)`, `parseRequestPredicate(String)`, `toExpression(RequestPredicate)` |

```java
// describe a functional route as an expression, useful for double-checking what was collected
RequestPredicate predicate = RequestPredicates.GET("/products/{id}")
        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON));

RequestPredicateKind kind = RequestPredicateKind.valueOf(predicate);
log.info("kind = {}, expression = {}", kind, RequestPredicateKind.toExpression(predicate));
```

> These three classes are **a 1.x artifact of working together with `WebEndpointMappingVisitor`**: the
> 0.2.x branch has neither `WebEndpointMappingVisitor` nor the `webflux.function.server` package. When
> migrating to 0.2.x, the way functional endpoints are collected has to be re-evaluated.

## 6. Reverse-proxy HandlerMapping

`io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping` uses the same name and the same
strategy as its MVC counterpart:

```java
public class ReversedProxyHandlerMapping extends AbstractHandlerMapping
        implements ApplicationListener<WebEndpointMappingsReadyEvent> {

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;   // setOrder in the constructor
}
```

- it reads the same request header `microsphere_wem_id` (`WebEndpointMapping.ID_HEADER_NAME`);
- it builds the `id -> mapping` cache at `WebEndpointMappingsReadyEvent` as well;
- the difference is that the resolved handler goes through the reactive execution chain instead of a
  `HandlerExecutionChain`.

```java
@Configuration
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
public class ReactiveGatewayConfig {
}
```

## 7. `SpringWebHelper` binding

This module's `META-INF/spring.factories` declares a single line, used by the web module to decide
"are we on MVC or on WebFlux":

```
io.microsphere.spring.web.util.SpringWebHelper=\
io.microsphere.spring.webflux.util.SpringWebFluxHelper
```
