[← Module guides](./README.md)

# microsphere-spring-webflux

Reactive (Spring WebFlux) implementation of the `microsphere-spring-web` contracts — symmetric with
`microsphere-spring-webmvc`. Packages `io.microsphere.spring.webflux.*` plus `io.microsphere.spring.web.util`.
`spring-webflux` is an optional dependency; all types `@since 1.0.0`.

## 1. `@EnableWebFluxExtension`

`io.microsphere.spring.webflux.annotation.EnableWebFluxExtension` —
`@Target(TYPE) @Retention(RUNTIME) @Documented`, meta-annotated `@EnableWebExtension` +
`@OverrideAnnotationAttributes` + `@Import(WebFluxExtensionBeanDefinitionRegistrar)`.

| Attribute | Default | Aliased to web? | Registers |
|-----------|---------|-----------------|-----------|
| `registerWebEndpointMappings()` | `true` | yes | `HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods()` | `true` | yes | `InterceptingHandlerMethodProcessor` (bean `interceptingHandlerMethodProcessor`) |
| `publishEvents()` | `true` | yes | `RequestHandledEventPublishingWebFilter` |
| `sources()` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | yes | component discovery scope |
| `requestContextStrategy()` | `DEFAULT` | yes | `RequestContextWebFilter` (`THREAD_LOCAL` → `threadContextInheritable=false`, `INHERITABLE_THREAD_LOCAL` → `true`, `DEFAULT` → nothing) |
| `storeRequestBodyArgument()` | `false` | no | `StoringRequestBodyArgumentInterceptor` |
| `storeResponseBodyReturnValue()` | `false` | no | `StoringResponseBodyReturnValueInterceptor` |
| `reversedProxyHandlerMapping()` | `false` | no | `ReversedProxyHandlerMapping` |

Each registration decision is logged at TRACE as `@EnableWebFluxExtension.<attr> = <value>` — turn on trace logging
for the registrar to see what got wired.

```java
@Configuration
@EnableWebFluxExtension(interceptHandlerMethods = true, publishEvents = true)
public class ReactiveWebConfig { }
```

## 2. The Reactive Weaver

`InterceptingHandlerMethodProcessor` — extends `OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>`
and implements `WebFilter`, `HandlerAdapter`, `HandlerMethodArgumentResolver`, `HandlerResultHandler`,
`WebExceptionHandler`, `Ordered` (`HIGHEST_PRECEDENCE`). On the ready event it collects sorted `HandlerMethodAdvice`
beans, reflectively inserts itself at index 0 of `RequestMappingHandlerAdapter`'s argument resolvers and of
`DispatcherHandler`'s result handlers, caches per-`MethodParameter`/return-type contexts, and then fires
`beforeResolveArgument` / `afterResolveArgument` / `beforeExecuteMethod` / `afterExecuteMethod`. It delegates request
context binding to an internal `RequestContextWebFilter` so the context is always available.

Your interception code uses the stack-agnostic SPI from `-web`
(`HandlerMethodInterceptor`, `HandlerMethodArgumentInterceptor`, `HandlerMethodAdvice`) — see
[web.md §3](./web.md#3-handler-method-interception-spi).

## 3. Body / Return-Value Capture

- `StoringRequestBodyArgumentInterceptor implements HandlerMethodArgumentInterceptor` — stores resolved
  `@RequestBody` arguments into `RequestAttributes` after resolution.
- `StoringResponseBodyReturnValueInterceptor implements HandlerMethodInterceptor` — stores return values of
  `@ResponseBody` handlers into `RequestAttributes` after execution.
- Read them back with `RequestAttributesUtils.getHandlerMethodRequestBodyArgument(...)` /
  `getHandlerMethodReturnValue(...)`.

## 4. Web Filters

| Class | Order | Role |
|-------|-------|------|
| `RequestContextWebFilter` | `HIGHEST_PRECEDENCE + 1` | WebFlux analogue of `RequestContextFilter`: binds `ServerWebRequest` + `LocaleContext` to the thread (optionally inheritable) for the request duration and resets on terminate; `setThreadContextInheritable(boolean)` |
| `RequestHandledEventPublishingWebFilter` | `LOWEST_PRECEDENCE` | measures exchange processing time and publishes `ServerRequestHandledEvent` when the chain terminates |
| `CompositeWebFilter` | — | composes an ordered filter list: `addFilter` (rejects null/self/duplicate), `removeFilter`, `addFilters(one, others...)` (fluent), `getWebFilters()` unmodifiable |
| `DelegatingWebFilter` | — | on `ContextRefreshedEvent`, collects all sorted `WebFilter` beans (excluding itself) into a `CompositeWebFilter` and delegates to them |

## 5. Request Context Adaptation

- `ServerWebRequest implements NativeWebRequest` — exposes a `ServerWebExchange` through the servlet-era
  `NativeWebRequest` API (headers, query params as attributes, locale, session id/mutex, `checkNotModified`,
  attribute get/set via `WebServerScope`). Accessors `getExchange()`, `getRequest()`, `getResponse()`,
  `getRequestHeaders()`, `getQueryParams()`, `getSession()`. Constants `REMOTE_USER_ATTRIBUTE_NAME`,
  `SESSION_MUTEX_ATTRIBUTE_NAME`, reference keys `REFERENCE_KEY_REQUEST/RESPONSE/SESSION`.
  Known limitations: `registerDestructionCallback` is unsupported (logs a warning); `isUserInRole` and `isSecure`
  return `false`. **This adapter is what lets the shared `-web` interception code run unchanged on the reactive
  stack.**
- `event.ServerRequestHandledEvent extends RequestHandledEvent` — the WebFlux counterpart of
  `ServletRequestHandledEvent`; carries the `WebHandler`, `ServerWebExchange`, processing time and optional failure
  cause; `getWebHandler()`, `getExchange()`.
- `WebServerScope` (enum `REQUEST`/`SESSION`) — maps scope ints to `ServerWebExchange` / `WebSession` attribute maps:
  `value()`, `getAttribute/setAttribute/removeAttribute/getRequiredAttribute/getAttributeOrDefault/getAttributeNames`,
  static `valueOf(int)` and static exchange+scope variants.
- `WebServerUtils` — `getSession`, `getPrincipal`, `getSessionId`, `getUserName` over an exchange.
- `MonoUtils` — `static <T> T getValue(Mono<T>)` blocks for a single value (future-get on non-blocking threads,
  `block()` otherwise). Test/debug utility, not for production request paths.
- `SpringWebFluxHelper` — the `SpringWebHelper` implementation registered via `spring.factories`;
  `getType() → SpringWebType.WEB_FLUX`; header/cookie writes (secure + httpOnly `ResponseCookie`), best-matching
  handler/pattern, URI-template and matrix variables, producible media types.

## 6. Functional Endpoints

- `RequestPredicateKind` — enum of the 11 `RequestPredicates` shapes: `AND`, `OR`, `NEGATE`, `METHOD`, `PATH`,
  `PATH_EXTENSION`, `QUERY_PARAM`, `ACCEPT`, `CONTENT_TYPE`, `HEADERS`, `UNKNOWN`. Each constant implements
  `matches(RequestPredicate)`, `matches(String)`, `predicate(String)`, `expression(RequestPredicate)`. Static DSL:

```java
RequestPredicate p = RequestPredicateKind.parseRequestPredicate("(GET && /users)");
String e = RequestPredicateKind.toExpression(p);   // round-trips
RequestPredicateKind k = RequestPredicateKind.valueOf(somePredicate);
```

  Accepted expression forms: `"GET"`, `"/users"`, `"*.json"`, `"?name=value"`, `"Accept: application/json"`,
  `"!GET"`, `"(GET || POST)"`, `"&&"`/`"||"` nesting with parentheses.
- `RequestPredicateVisitorAdapter extends RequestPredicates.Visitor` — all-no-op defaults: `method(Set<HttpMethod>)`,
  `path`, `pathExtension`, `header`, `queryParam`, `version` (Spring 7.0 compat), `startAnd/and/endAnd`,
  `startOr/or/endOr`, `startNegate/endNegate`, `unknown`, plus concrete `visit(RequestPredicate)`.
- `RouterFunctionVisitorAdapter extends RouterFunctions.Visitor` — `startNested/endNested`,
  `route(RequestPredicate, HandlerFunction<?>)`, `resources(Function<ServerRequest, Mono<Resource>>)`,
  `attributes(Map)` (Spring 5.3.x compat), `unknown`.
- `ConsumingWebEndpointMappingAdapter` — implements both visitors; walks a `RouterFunction` with a ThreadLocal stack
  of `RequestPredicate`→`WebEndpointMapping.Builder` and emits complete endpoint metadata to a
  `Consumer<WebEndpointMapping<?>>`. Constructors `(Consumer)` and `(Consumer, Object source)`.

## 7. Endpoint Metadata

`HandlerMappingWebEndpointMappingResolver` resolves mappings from every `HandlerMapping` bean —
`AbstractUrlHandlerMapping` via `HandlerMetadataWebEndpointMappingFactory`, `RequestMappingInfoHandlerMapping` via
`RequestMappingMetadataWebEndpointMappingFactory`, `RouterFunctionMapping` via `ConsumingWebEndpointMappingAdapter`.
`HandlerMappingWebEndpointMappingFactory<H, M>` is the template base (kind `WEB_FLUX`, endpoint = handler,
source = handler mapping; abstract `getMethods`/`getPatterns`, overridable `getHandler`/`contribute`).

`ReversedProxyHandlerMapping` mirrors the MVC one: caches `WEB_FLUX` mappings by id at
`WebEndpointMappingsReadyEvent`, resolves via the `microsphere_wem_id` header, `DEFAULT_ORDER =
HIGHEST_PRECEDENCE + 1`, currently `RequestMappingHandlerMapping`-sourced endpoints only.

## 8. Module Resources

`META-INF/spring.factories`:

```properties
io.microsphere.spring.web.util.SpringWebHelper=\
io.microsphere.spring.webflux.util.SpringWebFluxHelper
```

So having the jar on the classpath makes the helper discoverable regardless of `@EnableWebFluxExtension`.

## 9. Gotchas

- `requestContextStrategy` must be non-`DEFAULT` if you expect `RequestAttributes`-based features (body capture,
  interceptor context reads) to see a bound request on arbitrary threads; child-thread visibility requires
  `INHERITABLE_THREAD_LOCAL`.
- Blocking helpers (`MonoUtils.getValue`) and the reflective weaver are for tooling/tests; do not add blocking calls
  in your own reactive interception code.
- Some Spring-version-sensitive visitor methods exist purely for compatibility (`version(...)`, `attributes(Map)`) —
  implement them only if you consume the whole predicate surface.

---
[← Module guides](./README.md)
