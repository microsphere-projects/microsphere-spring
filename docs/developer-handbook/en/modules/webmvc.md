[← Module guides](./README.md)

# microsphere-spring-webmvc

Spring MVC (servlet-stack) implementation of the `microsphere-spring-web` contracts. Packages
`io.microsphere.spring.webmvc.*` and `io.microsphere.spring.web.servlet.*`. Uses `jakarta.servlet` (Servlet 6 /
Boot 3+ line).

## 1. `@EnableWebMvcExtension`

`io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension` —
`@Target(TYPE) @Retention(RUNTIME) @Documented`, meta-annotated `@EnableWebExtension` +
`@OverrideAnnotationAttributes` + `@Import(WebMvcExtensionBeanDefinitionRegistrar)`. (Not itself `@Inherited`, though
the parent annotation is.)

| Attribute | Default | Aliased to web? | Registers |
|-----------|---------|-----------------|-----------|
| `registerWebEndpointMappings()` | `true` | yes | `ServletWebEndpointMappingResolver` + `HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods()` | `true` | yes | `InterceptingHandlerMethodProcessor` (bean `interceptingHandlerMethodProcessor`) |
| `publishEvents()` | `true` | yes | event publishing via `WebEventPublisher` |
| `sources()` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | yes | component discovery scope |
| `requestContextStrategy()` | `DEFAULT` | yes | handled by the `ServletContainerInitializer` path (§6) |
| `registerHandlerInterceptors()` | `false` | no | `LazyCompositeHandlerInterceptor` wiring **all** `HandlerInterceptor` beans |
| `handlerInterceptors()` | `{}` | no | specific `HandlerInterceptor` types registered as beans and wired (ignored when `registerHandlerInterceptors=true`) |
| `storeRequestBodyArgument()` | `false` | no | `StoringRequestBodyArgumentAdvice` |
| `storeResponseBodyReturnValue()` | `false` | no | `StoringResponseBodyReturnValueAdvice` |
| `reversedProxyHandlerMapping()` | `false` | no | `ReversedProxyHandlerMapping` |

`WebMvcExtensionConfiguration` is always registered; it contributes interceptors to the `InterceptorRegistry`.

```java
@Configuration
@EnableWebMvcExtension(
        interceptHandlerMethods = true,
        registerWebEndpointMappings = true,
        storeRequestBodyArgument = true,
        reversedProxyHandlerMapping = true
)
public class WebConfig { }
```

## 2. Handler-Method Interception (the weaver)

`InterceptingHandlerMethodProcessor` — the central class. It extends
`OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>` and implements
`HandlerMethodArgumentResolver` + `HandlerMethodReturnValueHandler` + `HandlerInterceptor` + `WebMvcConfigurer`.
On the ready event it collects sorted `HandlerMethodAdvice` and `HandlerMethodArgumentResolverAdvice` beans,
pre-builds per-`MethodParameter`/return-type caches for every endpoint, then **prepends itself** to each
`RequestMappingHandlerAdapter`'s resolver/handler lists so it can fire `beforeResolveArgument` /
`afterResolveArgument` / `beforeExecuteMethod` / `afterExecuteMethod`. No-arg methods are covered via `preHandle`,
error paths via `afterCompletion`.

Two interception levels are available to your code:

| You implement | Callbacks | When |
|---------------|-----------|------|
| `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` / `HandlerMethodAdvice` (from `-web`) | see [web guide](./web.md#3-handler-method-interception-spi) | generic, stack-agnostic |
| `webmvc.method.support.HandlerMethodArgumentResolverAdvice` | `beforeResolveArgument(MethodParameter, ModelAndViewContainer, NativeWebRequest, WebDataBinderFactory)`, `afterResolveArgument(..., Object resolvedArgument, ...)` | MVC-specific, sees MVC-only types |

`LoggingHandlerMethodArgumentResolverAdvice` is a trace-logging reference implementation.

## 3. `HandlerInterceptor` Bases

- `MethodHandlerInterceptor` (abstract) — narrows `preHandle`/`postHandle`/`afterCompletion` to `HandlerMethod`
  handlers, with a `supports(request, response, handlerMethod)` gate.
- `AnnotatedMethodHandlerInterceptor<A extends Annotation>` (abstract) — fires only when the handler method carries
  annotation `A`; the annotation instance is passed to your hooks and lookup results are cached in `ServletContext`
  attributes. The idiomatic base for feature annotations (rate limits, auditing, permissions).
- `LazyCompositeHandlerInterceptor` (bean `lazyCompositeHandlerInterceptor`) — on context refresh gathers all
  `HandlerInterceptor` beans of configured types (excluding itself and the processor), sorts with
  `AnnotationAwareOrderComparator`, delegates; `preHandle` short-circuits on `false`.
- `LoggingMethodHandlerInterceptor` — trace logging.
- `AbstractPageRenderContextHandlerInterceptor` (abstract) — calls `postHandleOnPageRenderContext(...)` only for real
  page renders (a `ModelAndView` with a view name); `LoggingPageRenderContextHandlerInterceptor` implements it.

## 4. Request/Response Body Capture

- `StoringRequestBodyArgumentAdvice` (`@RestControllerAdvice`) — stores the resolved `@RequestBody` argument into
  `RequestAttributes`; only for `WebMvcUtils.SUPPORTED_CONVERTER_TYPES`
  (`MappingJackson2HttpMessageConverter`, `StringHttpMessageConverter`).
- `StoringResponseBodyReturnValueAdvice` (`@RestControllerAdvice`) — stores the return value before body writing.
- Read them back anywhere in the request:

```java
Object body = WebMvcUtils.getHandlerMethodRequestBodyArgument(request);
Object result = WebMvcUtils.getHandlerMethodReturnValue(request);
```

- Adapter bases you can extend without implementing everything: `RequestBodyAdviceAdapter`,
  `ResponseBodyAdviceAdapter<T>`.

## 5. Endpoint Metadata Collection

`HandlerMappingWebEndpointMappingResolver` implements the `-web` resolver over every `HandlerMapping` bean
(including ancestors) and dispatches by type:

| HandlerMapping kind | Factory | Notes |
|---------------------|---------|-------|
| `AbstractUrlHandlerMapping` | `HandlerMetadataWebEndpointMappingFactory` | handler→URL maps; all HTTP methods, pattern = key |
| `RequestMappingInfoHandlerMapping` | `RequestMappingMetadataWebEndpointMappingFactory` | extracts methods/patterns and contributes params/headers/consumes/produces; handler stored as `HandlerMethod.createWithResolvedBean()`; pairs carried by `RequestMappingMetadata` |
| `RouterFunctionMapping` | `ConsumingWebEndpointMappingAdapter` | visitor over `RouterFunction`/`RequestPredicate` rebuilding nested endpoint metadata |

Bases: `HandlerMappingWebEndpointMappingFactory<H, M>` (template: `getMethods`, `getPatterns`, `contribute`).

### `ReversedProxyHandlerMapping`

Performance optimization for traffic forwarded by a reverse proxy (Zuul, Spring Cloud Gateway). It listens for
`WebEndpointMappingsReadyEvent`, builds an id→`WebEndpointMapping` cache (WEB_MVC mappings sourced from
`AbstractHandlerMapping`), then at request time reads the `microsphere_wem_id` header and resolves the handler
directly through a `MethodHandle` on `AbstractHandlerMapping#getHandlerExecutionChain` — skipping pattern matching
entirely. `DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1`. Currently supports only `RequestMappingHandlerMapping` endpoints.

## 6. Servlet-Layer Integration

- `io.microsphere.spring.web.servlet.listener.EnableWebMvcExtensionListener` — a
  `ServletContainerInitializer` (`@HandlesTypes(EnableWebMvcExtension.class)`, declared in
  `META-INF/services/jakarta.servlet.ServletContainerInitializer`). It acts only on `requestContextStrategy()`:
  - `DEFAULT` → no-op.
  - `THREAD_LOCAL` → sets `threadContextInheritable=false` on every `FrameworkServlet` registration and registers
    `RequestContextFilter` (name `requestContextFilter`, `/*`, REQUEST dispatch) if absent.
  - `INHERITABLE_THREAD_LOCAL` → same with `threadContextInheritable=true` (context visible to child threads).
  This is the web.xml-less hook for war / `SpringBootServletInitializer` deployments; no `web-fragment.xml` ships.
- `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` — `OncePerRequestFilter` wrapping every response in
  a `ContentCachingResponseWrapper`; static `getResponseContentAsString(request, response)` reads it back
  (attribute `_ContentCachingFilter_`). **Not auto-registered** — declare the filter yourself.
- `web.servlet.util.WebUtils` — `isRunningBelowServlet3Container`, `getServletContext(request)`,
  `findFilterRegistrations`/`findServletRegistrations(ServletContext, Class)`.

## 7. Standalone Components (register them yourself)

- `ConfigurableContentNegotiationManagerWebMvcConfigurer` — `WebMvcConfigurer` + `EnvironmentAware`; binds every
  `microsphere.spring.webmvc.content-negotiation.*` property onto the internal
  `ContentNegotiationManagerFactoryBean` (field reflection + `DataBinder`, with
  `contentNegotiationManager`/`servletContext` disallowed). Its inner `MediaTypesMapPropertyEditor` parses a
  JSON-style `{...}` `mediaTypes` value via Jackson.
- `ExclusiveViewResolverApplicationListener` — on `ContextRefreshedEvent`, restricts
  `ContentNegotiatingViewResolver` (or the `mvcViewResolver` `ViewResolverComposite`) to the single `ViewResolver`
  bean named by `microsphere.spring.webmvc.view-resolver.exclusive-bean-name`. Useful when you want to force a
  specific view technology (e.g. pin Thymeleaf during migration).

## 8. Utilities and Constants

- `WebMvcUtils` — `getHttpServletRequest()` (+ `RequestAttributes` overload), `getWebApplicationContext(...)`,
  body/argument/return-value storage and retrieval, `isControllerAdviceBeanType(Class)`,
  `isPageRenderRequest(ModelAndView)`, servlet init-param helpers `setInitParameters`,
  `setGlobalInitializerClassInitParameter`, `setContextInitializerClassInitParameter`,
  `setFrameworkServletContextInitializerClassInitParameter`; constants `SUPPORTED_CONVERTER_TYPES`,
  `INIT_PARAM_DELIMITERS`.
- `ViewResolverUtils` — well-known resolver bean names: `beanNameViewResolver`, `defaultViewResolver`,
  `velocityViewResolver`, `thymeleafViewResolver`, `freeMarkerViewResolver`, `groovyMarkupViewResolver`,
  `mustacheViewResolver`, `mvcViewResolver`, `viewResolver`.
- `ViewUtils` — `getResponseStatus()`, `getPathVariables()`, `getSelectedContentType()` from view request attributes.
- `SpringWebMvcHelper` — the `SpringWebHelper` implementation (registered in `spring.factories`): method-override
  header honored, cookie/header writes (secure + httpOnly), and handler/pattern/URI-template/matrix/producible
  lookups via MVC request attributes.
- `webmvc.constants.PropertyConstants` — `microsphere.spring.webmvc.` and
  `microsphere.spring.webmvc.view-resolver.` prefixes.

## 9. Module Resources

- `META-INF/spring.factories` — `io.microsphere.spring.web.util.SpringWebHelper=io.microsphere.spring.webmvc.util.SpringWebMvcHelper`
- `META-INF/services/jakarta.servlet.ServletContainerInitializer` — `io.microsphere.spring.web.servlet.listener.EnableWebMvcExtensionListener`

## 10. Gotchas

- `registerHandlerInterceptors=true` and `handlerInterceptors={...}` are mutually exclusive — the former wins and the
  latter is ignored.
- The explicitly-listed interceptor beans are registered as beans too, so the composite intentionally skips them to
  avoid double invocation; don't also register them manually in your own `WebMvcConfigurer`.
- `ContentCachingFilter`, `ConfigurableContentNegotiationManagerWebMvcConfigurer`, and
  `ExclusiveViewResolverApplicationListener` require manual bean/filter registration.
- Body-capture advices only work for Jackson and String converters (`SUPPORTED_CONVERTER_TYPES`).

---
[← Module guides](./README.md)
