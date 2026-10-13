# microsphere-spring-webflux · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/webflux.md) · [Developer guide](../../developer-guide/current/webflux.md) · [1.x docs](../1.x/webflux.md)

`microsphere-spring-webflux` is the reactive counterpart of the Web MVC extensions: `@EnableWebFluxExtension` installs the same endpoint-registry and handler-method-interception features into a WebFlux application, and the shared SPI written for MVC keeps working unchanged because the exchange is adapted through `ServerWebRequest`.

---

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-webflux` |
| Branch | `release` (the 0.2.x line) |
| Version | `0.2.39` / development version `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework compatibility | **6.0.x – 7.0.x** (matching Reactor BOM: 2022.0.22 / 2023.0.19 / 2024.0.17 / 2025.0.7) |
| Required dependency | `microsphere-spring-web` (which transitively brings in `microsphere-spring-context`) |
| Optional dependencies | `spring-context`, `spring-webflux`, `jackson-annotations`, `jackson-databind` |

The Reactor line follows the selected Spring profile through the parent POM's `reactor-bom`, so when you use this module keep Spring Framework and the Reactor BOM in step (see the table above).

---

## 1. `@EnableWebFluxExtension`

`io.microsphere.spring.webflux.annotation.EnableWebFluxExtension` (`@Target(TYPE)`) is the reactive specialization of `@EnableWebExtension`: it is meta-annotated with `@EnableWebExtension` + `@OverrideAnnotationAttributes` + `@Import(WebFluxExtensionBeanDefinitionRegistrar.class)`.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `registerWebEndpointMappings` | `boolean` | `true` | Aliased to `@EnableWebExtension` |
| `interceptHandlerMethods` | `boolean` | `true` | Aliased |
| `publishEvents` | `boolean` | `true` | Aliased |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Aliased |
| `requestContextStrategy` | `RequestContextStrategy` | `DEFAULT` | Aliased; in reactive mode it applies to `RequestContextWebFilter` |
| `storeRequestBodyArgument` | `boolean` | `false` | Registers the `StoringRequestBodyArgumentInterceptor` (request-body storing) |
| `storeResponseBodyReturnValue` | `boolean` | `false` | Registers the `StoringResponseBodyReturnValueInterceptor` (response-body storing) |
| `reversedProxyHandlerMapping` | `boolean` | `false` | Registers the WebFlux `ReversedProxyHandlerMapping` (see section 4) |

**Note**: this annotation deliberately exposes **no** `registerHandlerInterceptors` / `handlerInterceptors` attributes — WebFlux has no `HandlerInterceptor`; the analogous extension point is `WebFilter`.

Every one of the seven registrations is behind its own attribute guard (`WebFluxExtensionBeanDefinitionRegistrar.java:61-73`): `registerWebEndpointMappings` → `HandlerMappingWebEndpointMappingResolver`, `interceptHandlerMethods` → `InterceptingHandlerMethodProcessor`, `publishEvents` → `RequestHandledEventPublishingWebFilter`, `storeRequestBodyArgument` / `storeResponseBodyReturnValue` → the two storing interceptors, `reversedProxyHandlerMapping` → `ReversedProxyHandlerMapping`.

⚠ `RequestContextWebFilter` is the exception that proves the rule: it is registered **only** for `requestContextStrategy = THREAD_LOCAL` or `INHERITABLE_THREAD_LOCAL` (`:101-122`). With the default `DEFAULT`, no request-context filter bean is created at all — the thread-local request context stays off unless you opt in.

```java
@Configuration
@EnableWebFluxExtension(storeRequestBodyArgument = true)
public class ReactiveWebConfig {
}
```

---

## 2. Reactive handler-method interception

The interception SPI is shared with MVC in full (see [web.md, section 4](./web.md)): implement `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` and register the implementation as a bean; `InterceptingHandlerMethodProcessor` invokes those beans along the reactive pipeline.

```java
@Component
public class ReactiveAccessInterceptor implements HandlerMethodInterceptor {
    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) {
        log.info("-> {}", handlerMethod.getMethod().getName());
    }
}
```

`InterceptingHandlerMethodProcessor` (`io.microsphere.spring.webflux.method`, bean name `interceptingHandlerMethodProcessor`) extends `OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>` and, with a single object, implements `WebFilter`, `HandlerAdapter`, `HandlerMethodArgumentResolver`, `HandlerResultHandler`, `WebExceptionHandler` and `Ordered` (`InterceptingHandlerMethodProcessor.java:81-83`). It caches the resolution context of `MethodParameter`. `RequestContextWebFilter`'s `threadContextInheritable` is derived from `requestContextStrategy`, and `ServerWebRequest` adapts `ServerWebExchange` into a `NativeWebRequest` — that combination is what lets the same `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` beans written for MVC work unchanged in a reactive application.

---

## 3. Endpoint metadata

The resolver/factory set — `HandlerMappingWebEndpointMappingResolver` together with `HandlerMappingWebEndpointMappingFactory`, `HandlerMetadataWebEndpointMappingFactory` and `RequestMappingMetadataWebEndpointMappingFactory` — extracts mappings from `@RequestMapping` handlers and from functional endpoints, recorded with `Kind` `WEB_FLUX`. Functional endpoints are discovered by traversing the `RouterFunction` with `RouterFunctionVisitorAdapter`, `RequestPredicateVisitorAdapter`, `RequestPredicateKind` and `ConsumingWebEndpointMappingAdapter`. Reading the collected metadata is identical to MVC: inject `WebEndpointMappingRegistry` and listen for `WebEndpointMappingsReadyEvent`.

---

## 4. `ReversedProxyHandlerMapping`

`io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping` runs with `order = HIGHEST_PRECEDENCE + 1`, and `getHandlerInternal(ServerWebExchange)` returns `Mono<?>`. It keeps only mappings with `Kind.WEB_FLUX` whose `source instanceof AbstractHandlerMapping` and, based on the `microsphere_wem_id` request header, routes straight to the already-selected endpoint. Usage is the same as in MVC:

```java
@Configuration
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
public class ReactiveBackendConfig {
}
```

The design matches the MVC counterpart (order `HIGHEST_PRECEDENCE + 1`) and dispatches on the `microsphere_wem_id` header, so a gateway-forwarded reactive request executes exactly the endpoint the gateway matched.

---

## 5. Other reactive components

| Component | Description |
|---|---|
| `CompositeWebFilter` / `DelegatingWebFilter` | Composition and delegation of `WebFilter` instances |
| `RequestHandledEventPublishingWebFilter` | Publishes `ServerRequestHandledEvent`; corresponds to `publishEvents` |
| `ServerRequestHandledEvent` | Request-handled completion event |
| `MonoUtils` / `WebServerUtils` / `WebServerScope` | Common `Mono` and server utilities, plus the scope constant |
| `SpringWebFluxHelper` | Module registration hook, registered via `META-INF/spring.factories` |
| `StoringRequestBodyArgumentInterceptor` / `StoringResponseBodyReturnValueInterceptor` | Request-body / response-body storing, enabled by the `store*` attributes |
