[← Module guides](./README.md)

# microsphere-spring-web

Transport-agnostic web abstractions shared by `microsphere-spring-webmvc` and `microsphere-spring-webflux`. Base
package `io.microsphere.spring.web`. Depends on `microsphere-spring-context`; Spring Web and the Servlet API are
optional. All types `@since 1.0.0`.

## 1. `@EnableWebExtension`

`io.microsphere.spring.web.annotation.EnableWebExtension` — the only annotation in this module.
`@Target({TYPE, ANNOTATION_TYPE}) @Inherited @Documented @Import(WebExtensionBeanDefinitionRegistrar)`.
Normally you use `@EnableWebMvcExtension` / `@EnableWebFluxExtension` instead, which re-expose these attributes via
`@AliasFor`.

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `registerWebEndpointMappings()` | `true` | initialize resolver/registry/factory/filter beans so `WebEndpointMapping`s from MVC, WebFlux and classic Servlet get registered |
| `interceptHandlerMethods()` | `true` | initialize `HandlerMethodArgumentInterceptor` / `HandlerMethodInterceptor` beans invoked around `HandlerMethod` execution |
| `publishEvents()` | `true` | publish `HandlerMethodArgumentsResolvedEvent` and `WebEndpointMappingsReadyEvent` |
| `sources()` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | where extension components are collected from |
| `requestContextStrategy()` | `DEFAULT` | where `RequestAttributes` is stored (`DEFAULT` / `THREAD_LOCAL` / `INHERITABLE_THREAD_LOCAL`) |

Enablement chain: `@Import(WebExtensionBeanDefinitionRegistrar)` → registers the four `WebEndpointMapping` SPI roles,
`WebEndpointMappingRegistrar`, `DelegatingHandlerMethodAdvice` (bean `delegatingHandlerMethodAdvice`) + interceptors,
and `WebEventPublisher` — each gated by the flags above. Registry selection: if no user-provided
`WebEndpointMappingRegistry` bean exists, `SimpleWebEndpointMappingRegistry` becomes primary; otherwise all
candidates are demoted to non-primary and wrapped by `CompositeWebEndpointMappingRegistry`.

## 2. Endpoint Metadata Model — `WebEndpointMapping<E>`

A transport-neutral description of one mapped endpoint: patterns, HTTP methods, params, headers, consumes, produces,
negation, id, `kind`, source, plus arbitrary attributes.

- `enum Kind { SERVLET, FILTER, WEB_MVC, WEB_FLUX, CUSTOMIZED }`
- Static factories: `servlet()`, `filter()`, `webmvc()`, `webflux()`, `customized()`, `of(Kind)`; fluent
  `WebEndpointMapping.Builder` (`endpoint`, `pattern(s)`, `method(s)`, `param(s)`, `header(s)`, `consume(s)`,
  `produce(s)`, `negate`, `source`, `nest*`, `build`).
- Accessors: `getKind/getEndpoint/getId/isNegated/getSource/getPatterns/getMethods/getParams/getHeaders/getConsumes/getProduces`,
  `setAttribute/getAttribute`.
- Serialization: `toJSON()` (Jackson), `toExpression()`; constant `ID_HEADER_NAME = "microsphere_wem_id"` — the header
  that carries an endpoint id through a reverse proxy (consumed by `ReversedProxyHandlerMapping` in both web stacks).

### SPI roles (all extend-friendly, discovered from the configured `sources`)

| Interface | Key methods | Role |
|-----------|-------------|------|
| `WebEndpointMappingResolver` | `Collection<WebEndpointMapping> resolve(ApplicationContext)` | find endpoints from a context |
| `WebEndpointMappingRegistry` | `boolean register(mapping)`, `int register(mapping, others...)`, `int register(Iterable)`, `Collection<WebEndpointMapping> getWebEndpointMappings()` | store endpoints |
| `WebEndpointMappingFactory<E>` | `Optional<WebEndpointMapping<E>> create(E endpoint)`, `boolean supports(E)`, `Class<E> getSourceType()` | build one endpoint from a source object |
| `WebEndpointMappingFilter` | `boolean accept(WebEndpointMapping)` | decide whether a mapping is registered |

Implementations worth knowing:

- `AbstractWebEndpointMappingFactory<E>` — template wrapping `doCreate(E)` in try/catch → `Optional`.
- `ServletRegistrationWebEndpointMappingFactory` / `FilterRegistrationWebEndpointMappingFactory` — from
  `jakarta.servlet.ServletRegistration` / `FilterRegistration` (HTTP methods derived from overridden `doXxx` on
  `HttpServlet`).
- `Jackson2WebEndpointMappingFactory` — deserializes a JSON descriptor into a mapping; registered by default in
  `spring.factories`.
- `SmartWebEndpointMappingFactory` — dispatches to other factories grouped by `getSourceType()`.
- `ServletWebEndpointMappingResolver` — resolves all Servlet `Filter`/`Servlet` registrations from the
  `ServletContext` (Servlet 3.0+).
- `SimpleWebEndpointMappingRegistry` (in-memory map), `FilteringWebEndpointMappingRegistry` (abstract, applies
  filters composed by a `FilterOperator`, default `OR`), `CompositeWebEndpointMappingRegistry` (fans out to all
  registry beans).
- `WebEndpointMappingRegistrar` (`AbstractSmartLifecycle`, phase = `WebEventPublisher.DEFAULT_PHASE - 10`) — pulls
  from every resolver into the registry before the ready event fires.
- `HandlerMetadata<H, M>` / `HandlerMethodMetadata<M>` — handler + metadata pairs used by the stack-specific factories.

## 3. Handler-Method Interception SPI

| Interface | Callbacks |
|-----------|-----------|
| `HandlerMethodInterceptor` | `beforeExecute(HandlerMethod, Object[] args, NativeWebRequest)`, `afterExecute(HandlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest)` |
| `HandlerMethodArgumentInterceptor` | `beforeResolveArgument(MethodParameter, HandlerMethod, NativeWebRequest)`, `afterResolveArgument(MethodParameter, Object resolvedArgument, HandlerMethod, NativeWebRequest)` |
| `HandlerMethodAdvice` | facade combining both (all four methods have no-op defaults) |

`DelegatingHandlerMethodAdvice` collects the sorted interceptor beans on `ContextRefreshedEvent` and delegates to
them — the stack weavers (`InterceptingHandlerMethodProcessor` in webmvc/webflux) call into this single advice bean.

```java
@Component
public class AuditInterceptor implements HandlerMethodInterceptor {
    @Override
    public void beforeExecute(HandlerMethod hm, Object[] args, NativeWebRequest request) { ... }
    @Override
    public void afterExecute(HandlerMethod hm, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) { ... }
}
```

## 4. Request Rules and Expressions

Modeled directly on `@RequestMapping` conditions, but reusable outside a handler mapping (authorization, gating,
gray routing):

- `WebRequestRule` — `boolean matches(NativeWebRequest)`; bases `AbstractWebRequestRule<T>` (`isEmpty()`,
  `getContent()`, `getToStringInfix()`), `CompositeWebRequestRule` (AND-composition).
- Concrete rules: `WebRequestMethodsRule`, `WebRequestPattensRule` (sic — Ant-style path matching with
  suffix/trailing-slash options), `WebRequestParamsRule`, `WebRequestHeadersRule`, `WebRequestConsumesRule`,
  `WebRequestProducesRule` (supports a `ContentNegotiationManager`).
- Expressions: `NameValueExpression<T>` + `AbstractNameValueExpression<T>` (parses `!name=value`),
  `WebRequestHeaderExpression`, `WebRequestParamExpression` (each with static `parseExpressions(String...)`);
  `MediaTypeExpression` + `GenericMediaTypeExpression` (`of(String)`, specificity `compareTo`),
  `ConsumeMediaTypeExpression`, `ProduceMediaTypeExpression`.

## 5. Events

- `HandlerMethodArgumentsResolvedEvent` (`ApplicationEvent`) — after a `HandlerMethod`'s arguments are resolved;
  `getHandlerMethod()`, `getMethod()`, `getArguments()`, `getWebRequest()`.
- `WebEndpointMappingsReadyEvent` (`ApplicationContextEvent`) — once all mappings are registered; `getMappings()`.
  This is the trigger most components in this family listen for.
- `WebEventPublisher` (`AbstractSmartLifecycle` + `HandlerMethodInterceptor`, `DEFAULT_PHASE = EARLIEST_PHASE + 100`)
  — publishes both.

## 6. Cross-Stack Abstraction

- `SpringWebHelper` — the SPI that hides MVC vs WebFlux differences: `getMethod`, `get/set/addHeader`,
  `getHeaderValues`, `getCookieValue`, `addCookie`, `getRequestBody(request, Class<T>)`,
  `writeResponseBody(...)`, `getBestMatchingHandler`, `getPathWithinHandlerMapping`, `getBestMatchingPattern`,
  `getUriTemplateVariables`, `getMatrixVariables`, `getProducibleMediaTypes`, `SpringWebType getType()`.
  Implementations: `SpringWebMvcHelper` (webmvc), `SpringWebFluxHelper` (webflux), `UnknownSpringWebHelper`
  (no-op fallback in this module) — resolved through `spring.factories`.
- `WebRequestUtils` — the facade you actually call; dispatches to the helper. `WebUtils` — `isHandlerMethod`,
  `isNoArgumentHandlerMethod`, `resolveHandlerMethod`. `HttpUtils` — `ALL_HTTP_METHODS`, `supportsMethod(...)`.
  `MediaTypeUtils` — `SPECIFICITY_COMPARATOR`.
- `RequestAttributesUtils` — stores/retrieves handler-method arguments, the `@RequestBody` argument and the return
  value in `RequestAttributes`; attribute prefixes `HM.ARGS:`, `HM.RB.ARG:`, `HM.RV:`. This is what makes body and
  return value available to interceptors.
- Enums: `RequestContextStrategy`, `SpringWebType` (`WEB_MVC`/`WEB_FLUX`/`UNKNOWN` + `valueOf(NativeWebRequest)`),
  `WebType` (`SERVLET`/`REACTIVE`/`NONE`), `WebScope` (`REQUEST`/`SESSION` typed attribute access),
  `WebSource` (`REQUEST_ATTRIBUTE`, `SESSION_ATTRIBUTE`, `REQUEST_PARAMETER`, `REQUEST_HEADER`, `REQUEST_COOKIE`,
  `REQUEST_BODY`, `PATH_VARIABLE`, `MATRIX_VARIABLE`), `WebTarget` (`RESPONSE_BODY`, `RESPONSE_HEADER`,
  `RESPONSE_COOKIE`).
- `constants.PropertyConstants` — `MICROSPHERE_SPRING_WEB_PROPERTY_NAME_PREFIX = "microsphere.spring.web."`.

## 7. `META-INF/spring.factories`

```properties
io.microsphere.spring.web.metadata.WebEndpointMappingFactory=\
io.microsphere.spring.web.metadata.Jackson2WebEndpointMappingFactory

io.microsphere.spring.web.util.SpringWebHelper=\
io.microsphere.spring.web.util.UnknownSpringWebHelper
```

## 8. Gotchas

- This module alone cannot intercept anything — you need webmvc or webflux on the classpath for the weaver.
- Register new web concepts **here first**, then implement per stack; that keeps MVC and WebFlux symmetric, which
  reviewers expect.
- `WebEndpointMapping` ids are stable only within a JVM start; do not persist them across deploys expecting
  `ReversedProxyHandlerMapping` lookups to survive a rolling release without cache warm-up.

---
[← Module guides](./README.md)
