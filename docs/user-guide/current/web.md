# microsphere-spring-web · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/web.md) · [Developer guide](../../developer-guide/current/web.md) · [1.x docs](../1.x/web.md)

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-web` |
| Branch | `release` (the 0.2.x line) |
| Version | `0.2.39` released / `0.2.40-SNAPSHOT` under development |
| Java | 17 |
| Spring Framework compatibility range | **6.0.x – 7.0.x** (regression-tested on 6.0.23 / 6.1.21 / 6.2.18 / 7.0.9) |
| Servlet namespace | `jakarta.servlet.*` (`jakarta.servlet-api` is optional) |
| Required dependency | `microsphere-spring-context` |
| Optional dependencies | `spring-context`, `spring-web`, `jakarta.servlet-api`, `jackson-annotations`, `jackson-databind`, `jsr305` |

This module is the shared kernel of `webmvc` and `webflux`: the web-endpoint metadata registry, the handler-method interception SPI and the request-context strategy. Depending on `microsphere-spring-webmvc` or `microsphere-spring-webflux` brings it in transitively, and only `microsphere-spring-context` is mandatory — `spring-web`, `jakarta.servlet-api` and Jackson stay optional.

---

## 1. `@EnableWebExtension`

`io.microsphere.spring.web.annotation.EnableWebExtension`, declared as `@Target({TYPE, ANNOTATION_TYPE}) @Inherited @Documented`, takes effect through `@Import(WebExtensionBeanDefinitionRegistrar.class)`.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `registerWebEndpointMappings` | `boolean` | `true` | Collects and registers endpoint metadata at startup |
| `interceptHandlerMethods` | `boolean` | `true` | Wires the handler-method interception chain |
| `publishEvents` | `boolean` | `true` | Publishes web events (registers `WebEventPublisher`) |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Where extension components are discovered |
| `requestContextStrategy` | `RequestContextStrategy` | `DEFAULT` | `DEFAULT` / `THREAD_LOCAL` / `INHERITABLE_THREAD_LOCAL` |

Registrar behaviour:

- If several `WebEndpointMappingRegistry` implementations already exist in the container, they are demoted to non-primary and aggregated by a `CompositeWebEndpointMappingRegistry`, so the context keeps a single logical registry; otherwise `SimpleWebEndpointMappingRegistry` is used.
- `WebEndpointMappingRegistrar` (a `SmartLifecycle`) is always registered: during `doStart()` it calls every `WebEndpointMappingResolver` in turn, applies the `WebEndpointMappingFilter` and `WebEndpointMappingFactory` beans, writes the results into the registry and finally publishes `WebEndpointMappingsReadyEvent`.
- When `interceptHandlerMethods = true`, `DelegatingHandlerMethodAdvice` is registered under the bean name `delegatingHandlerMethodAdvice` and fans every call out to all interceptor beans.

```java
@Configuration
@EnableWebExtension(
        registerWebEndpointMappings = true,
        interceptHandlerMethods = true,
        publishEvents = true,
        requestContextStrategy = RequestContextStrategy.INHERITABLE_THREAD_LOCAL
)
public class WebConfig {
}
```

---

## 2. `WebEndpointMapping<E>`: endpoint metadata

`io.microsphere.spring.web.metadata.WebEndpointMapping` is an immutable endpoint description model, classified by the `Kind` enum `{ SERVLET, FILTER, WEB_MVC, WEB_FLUX, CUSTOMIZED }`. It is built from a static entry point plus a fluent builder:

```java
WebEndpointMapping<HandlerMethod> mapping = WebEndpointMapping.<HandlerMethod>webmvc()
        .endpoint(handlerMethod)
        .patterns("/products/{id}")        // String... / Collection<String> overloads
        .methods("GET")                    // String... / HttpMethod... overloads
        .params("page", "size")
        .headers("X-Tenant")
        .produces("application/json")
        .source("order-service")
        .negate()                          // invert matching
        .build();

mapping.getKind();        // WEB_MVC
mapping.getId();          // unique id, matched against the microsphere_wem_id forwarding header
mapping.getPatterns();    // getMethods()/getParams()/getHeaders()/getConsumes()/getProduces()
mapping.toExpression();   // human-readable expression, e.g. "(GET && /products/{id})"
mapping.toJSON();
mapping.setAttribute("owner", "team-a");   // getAttribute(name)
```

The builder exposes `endpoint`, `patterns`, `methods`, `params`, `headers`, `consume(s)`, `produce(s)`, `negate`, `source`, the `nest*` combinators and `build`; the resulting object offers read-only accessors, `setAttribute`/`getAttribute`, and `toJSON()` plus `toExpression()` for diagnostics.

Defaults: `build()` falls back to `ALL_METHODS` (the set of all HTTP method names) when no `methods` were specified, and `patterns` must not be empty. A `Content-Type` header is normalized into consumes and an `Accept` header into produces. `nestPatterns/nestMethods/nestParams/nestHeaders/nestConsumes/nestProduces(Builder)` merge the conditions of another mapping into this one. Constants: `ID_HEADER_NAME = "microsphere_wem_id"`, `UNKNOWN_SOURCE`.

---

## 3. Registry and SPI

| Interface | Key signature | Purpose |
|---|---|---|
| `WebEndpointMappingRegistry` | `boolean register(WebEndpointMapping)`; the varargs/`Iterable` overloads return the number of entries written; `Collection<WebEndpointMapping> getWebEndpointMappings()` | Read all collected endpoint metadata |
| `WebEndpointMappingResolver` | `Collection<WebEndpointMapping> resolve(ApplicationContext)` | Resolve mappings from the context |
| `WebEndpointMappingFactory<E>` | `Optional<WebEndpointMapping<E>> create(E)`; `boolean supports(E)`; `Class<E> getSourceType()` | Create a mapping from an endpoint object |
| `WebEndpointMappingFilter` | `extends Filter<WebEndpointMapping>` | Exclude mappings that should not be registered |

Consumption example:

```java
@Component
public class EndpointExporter {
    @Autowired
    private WebEndpointMappingRegistry registry;

    @EventListener
    public void onReady(WebEndpointMappingsReadyEvent event) {
        registry.getWebEndpointMappings()
                .forEach(m -> log.info("{} {}", m.getKind(), m.toExpression()));
    }
}
```

`SimpleWebEndpointMappingRegistry`, `CompositeWebEndpointMappingRegistry` and `FilteringWebEndpointMappingRegistry` are the built-in implementations; `ServletWebEndpointMappingResolver` and `HandlerMappingWebEndpointMappingResolver` are contributed by the webmvc/webflux modules. The factory hierarchy — `AbstractWebEndpointMappingFactory`, `RegistrationWebEndpointMappingFactory`, `ServletRegistrationWebEndpointMappingFactory`, `FilterRegistrationWebEndpointMappingFactory`, `Jackson2WebEndpointMappingFactory`, `SmartWebEndpointMappingFactory` — covers Servlet/Filter registrations and JSON rendering.

---

## 4. Handler-method interception SPI

Two interfaces live in `io.microsphere.spring.web.method.support`. Every method is a `default` no-op, so you override only what you need:

```java
public interface HandlerMethodInterceptor {
    default void beforeExecute(HandlerMethod handlerMethod, Object[] args,
                               NativeWebRequest request) throws Exception { }
    default void afterExecute(HandlerMethod handlerMethod, Object[] args,
                              @Nullable Object returnValue, @Nullable Throwable error,
                              NativeWebRequest request) throws Exception { }
}

public interface HandlerMethodArgumentInterceptor {
    default void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod,
                                       NativeWebRequest webRequest) throws Exception { }
    default void afterResolveArgument(MethodParameter parameter, @Nullable Object resolvedArgument,
                                      HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception { }
}
```

`HandlerMethodAdvice` is the equivalent façade for the same semantics (its methods are named `beforeExecuteMethod`/`afterExecuteMethod`), and `DelegatingHandlerMethodAdvice` is what dispatches each call to every interceptor bean in the context.

```java
@Component
public class AccessLoggingInterceptor implements HandlerMethodInterceptor {
    @Override
    public void beforeExecute(HandlerMethod hm, Object[] args, NativeWebRequest request) {
        log.info("-> {} {}", request.getMethod(), hm.getBeanType().getSimpleName());
    }
    @Override
    public void afterExecute(HandlerMethod hm, Object[] args, Object returnValue,
                             Throwable error, NativeWebRequest request) {
        if (error != null) {
            log.error("<- {} failed", hm.getMethod().getName(), error);
        }
    }
}
```

Note: this SPI is shared by webmvc and webflux, so one interceptor implementation serves both the blocking and the reactive stack. On WebFlux the `NativeWebRequest` role is filled by the module's `ServerWebRequest` adapter.

---

## 5. Events and other components

| Component | Description |
|---|---|
| `WebEndpointMappingsReadyEvent` | Published once endpoint metadata collection finishes; `ReversedProxyHandlerMapping` and others initialize on it |
| `HandlerMethodArgumentsResolvedEvent` | Published after the controller-method arguments have resolved |
| `HandlerMetadata` / `HandlerMethodMetadata` | Handler metadata models |
| `WebRequestRule` + the `rules` package | Matching rules composable by pattern/method/param/header/consume/produce |
| `WebUtils`, `RequestAttributesUtils`, `RequestContextStrategy` | Request and context utilities |
| `SpringWebHelper` | The generic assembly entry point each web module registers in `META-INF/spring.factories` |
