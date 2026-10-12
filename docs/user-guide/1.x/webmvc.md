# microsphere-spring-webmvc (1.x)

[← 1.x overview](./README.md) · [简体中文](./zh/webmvc.md) · [Developer guide](../../developer-guide/1.x/webmvc.md) · [0.2.x docs](../current/webmvc.md)

## Version information

| Item | Value |
|------|-----|
| artifactId | `microsphere-spring-webmvc` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version in development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility range | **4.3.x – 5.3.x** |
| Servlet namespace | **`javax.servlet`** (`javax.servlet-api` at provided scope) |
| Servlet container (tests) | `tomcat-embed-core` `9.0.119` |

Dependencies: `microsphere-spring-web` (required, internal dependency); `spring-web` / `spring-webmvc` / `jackson-*` are `optional`; `javax.servlet-api` is `provided`; `microsphere-annotation-processor` is an optional compile-time dependency.

> The core differences from 0.2.x: on this branch every Servlet/request API is `javax.*`; in addition there are `WebEndpointMappingVisitor` collaborators specific to 1.x and the `META-INF/services/javax.servlet.ServletContainerInitializer` auto-configuration entry point.

---

## 1. `@EnableWebMvcExtension`

`io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension`:

```java
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@EnableWebExtension
@OverrideAnnotationAttributes
@Import(WebMvcExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebMvcExtension { ... }
```

It inherits the generic attributes through `@EnableWebExtension` (bridged with `@AliasFor`) and then adds 5 MVC-specific attributes:

| Attribute | Default | Bridged | Semantics |
|------|--------|------|------|
| `registerWebEndpointMappings` | `true` | `@AliasFor(EnableWebExtension)` | Registers the endpoint-metadata components |
| `interceptHandlerMethods` | `true` | Same as above | Enables controller-method interception |
| `publishEvents` | `true` | Same as above | Registers `WebEventPublisher` |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Same as above | The SPI discovery sources |
| `requestContextStrategy` | `DEFAULT` | Same as above | The request-context thread-inheritance strategy (see section 2) |
| `registerHandlerInterceptors` | `false` | **MVC-specific** | Whether `HandlerInterceptor`s are registered automatically |
| `handlerInterceptors` | `{}` | **MVC-specific** | The interceptor types to register explicitly |
| `storeRequestBodyArgument` | `false` | **MVC-specific** | Registers `StoringRequestBodyArgumentAdvice` |
| `storeResponseBodyReturnValue` | `false` | **MVC-specific** | Registers `StoringResponseBodyReturnValueAdvice` |
| `reversedProxyHandlerMapping` | `false` | **MVC-specific** | Registers `ReversedProxyHandlerMapping` |

> Because the annotation is marked with `@OverrideAnnotationAttributes`, all of the attributes above can be overridden from external configuration (see section 9 of [context.md](./context.md)), so features can be switched on and off without changing code.

What `WebMvcExtensionBeanDefinitionRegistrar` actually registers (verified against the source, item by item):

| Condition | Beans registered |
|------|-------------|
| Unconditionally | `WebMvcExtensionConfiguration` |
| `registerWebEndpointMappings = true` | `ServletWebEndpointMappingResolver`, `HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods = true` | `InterceptingHandlerMethodProcessor` (bean-name constant `BEAN_NAME`) |
| `registerHandlerInterceptors = true` **or** `handlerInterceptors` is non-empty | `LazyCompositeHandlerInterceptor` (the interceptor type array is passed as constructor argument); when types are listed explicitly, each is registered individually |
| `storeRequestBodyArgument = true` | `StoringRequestBodyArgumentAdvice` |
| `storeResponseBodyReturnValue = true` | `StoringResponseBodyReturnValueAdvice` |
| `reversedProxyHandlerMapping = true` | `ReversedProxyHandlerMapping` |

There is an easily-missed detail in the interceptor resolution logic:

```java
Class<? extends HandlerInterceptor>[] handlerInterceptorClasses =
        registerHandlerInterceptors ? ALL_HANDLER_INTERCEPTOR_CLASSES : handlerInterceptors;
```

That is, **`registerHandlerInterceptors = true` takes precedence**: in that case the sentinel "all `HandlerInterceptor`" array is used and the `handlerInterceptors` list is ignored; only when `registerHandlerInterceptors = false` are the types listed in `handlerInterceptors` registered one by one (see `registerInterceptors` in the source: hitting the sentinel array returns immediately).

```java
@Configuration
@EnableWebMvcExtension(
        registerWebEndpointMappings = true,
        interceptHandlerMethods = true,
        storeRequestBodyArgument = true,
        storeResponseBodyReturnValue = true,
        handlerInterceptors = {AdminAuditInterceptor.class}
)
public class WebMvcConfig {
}
```

## 2. `EnableWebMvcExtensionListener`: ServletContainerInitializer auto-configuration

This module packages:

```
# META-INF/services/javax.servlet.ServletContainerInitializer
io.microsphere.spring.web.servlet.listener.EnableWebMvcExtensionListener
```

```java
@HandlesTypes(EnableWebMvcExtension.class)
public class EnableWebMvcExtensionListener implements ServletContainerInitializer { ... }
```

When the Servlet container starts, the container hands all classes annotated with `@EnableWebMvcExtension` to this listener, and the listener applies `requestContextStrategy` accordingly:

| Strategy | Action |
|------|------|
| `DEFAULT` | Does nothing and returns immediately |
| `THREAD_LOCAL` | Sets the init-param `threadContextInheritable=false` on every `FrameworkServlet` (including `DispatcherServlet`) registration; registers `RequestContextFilter` if none is present yet |
| `INHERITABLE_THREAD_LOCAL` | Same as above, but with `threadContextInheritable=true` |

> This is part of the answer to "who actually reads `requestContextStrategy`" on 1.x: the web module's registrar does not read it, MVC relies on this `ServletContainerInitializer`, and WebFlux relies on `WebFluxExtensionBeanDefinitionRegistrar` (see [webflux.md](./webflux.md)).
>
> Prerequisite: `ServletContainerInitializer` is discovered via SPI by a Servlet 3.0+ container; it is not triggered on embedded-test or non-container deployment paths.

## 3. Controller-method interception: `InterceptingHandlerMethodProcessor`

When `interceptHandlerMethods = true`, the registered `InterceptingHandlerMethodProcessor` plays several roles at once (delegating to Spring's own `HandlerMethodArgumentResolver` / `ReturnValueHandler`) and weaves the two interceptor types from [web.md](./web.md) into the call chain:

```java
@Component
public class TraceIdInterceptor implements HandlerMethodInterceptor {

    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args,
                              NativeWebRequest request) throws Exception {
        String traceId = request.getHeader("X-Trace-Id");
        MDC.put("traceId", traceId);
    }

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args,
                             @Nullable Object returnValue, @Nullable Throwable error,
                             NativeWebRequest request) throws Exception {
        MDC.remove("traceId");
    }
}
```

Use `HandlerMethodArgumentInterceptor` for argument-level observation; both types can be discovered through the three sources of `sources`.

## 4. `MethodHandlerInterceptor`: narrowing `Object handler` to `HandlerMethod`

All three methods of Spring's `HandlerInterceptor` receive `Object handler`, so you have to write the `instanceof` check yourself and deal with static-resource handlers on your own. `io.microsphere.spring.webmvc.interceptor.MethodHandlerInterceptor` makes those three methods `final` and narrows the parameter:

```java
public abstract class MethodHandlerInterceptor implements HandlerInterceptor {

    @Override public final boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception;
    @Override public final void postHandle(HttpServletRequest req, HttpServletResponse resp, Object handler,
                                           @Nullable ModelAndView mav) throws Exception;
    @Override public final void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler,
                                                @Nullable Exception ex) throws Exception;

    // Subclasses implement these three (all protected abstract)
    protected abstract boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                                        HandlerMethod handlerMethod) throws Exception;
    protected abstract void postHandle(HttpServletRequest req, HttpServletResponse resp,
                                       HandlerMethod handlerMethod, ModelAndView mav) throws Exception;
    protected abstract void afterCompletion(HttpServletRequest req, HttpServletResponse resp,
                                            HandlerMethod handlerMethod, Exception ex) throws Exception;

    // Optionally overridden, defaults to true
    protected boolean supports(HttpServletRequest req, HttpServletResponse resp,
                               HandlerMethod handlerMethod) throws Exception { return true; }
}
```

The fixed semantics of the `final` wrapper methods: if `handler` is not a `HandlerMethod` → pass through directly (`preHandle` returns `true`, the other two are silently skipped); if it is a `HandlerMethod` **and** `supports(...)` returns `true` → only then are the narrowed abstract methods invoked. All three abstract methods `throw Exception`, so `preHandle` must return a boolean.

```java
@Component
public class AdminAuditInterceptor extends MethodHandlerInterceptor {

    @Override
    protected boolean supports(HttpServletRequest request, HttpServletResponse response,
                               HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getName().startsWith("com.acme.admin");
    }

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                HandlerMethod handlerMethod) throws Exception {
        log.info("[ADMIN] {} -> {}", request.getRequestURI(), handlerMethod.getMethod());
        return true;
    }

    @Override
    protected void postHandle(HttpServletRequest request, HttpServletResponse response,
                              HandlerMethod handlerMethod, ModelAndView mav) throws Exception {
        if (mav != null) {
            mav.addObject("audited", Boolean.TRUE);
        }
    }

    @Override
    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   HandlerMethod handlerMethod, Exception ex) throws Exception {
        if (ex != null) {
            log.error("[ADMIN] {} failed", request.getRequestURI(), ex);
        }
    }
}
```

`AnnotatedMethodHandlerInterceptor<A extends Annotation>` builds on this and matches by generic annotation type; its callbacks gain an extra annotation parameter:

```java
public abstract class AnnotatedMethodHandlerInterceptor<A extends Annotation> extends MethodHandlerInterceptor {

    protected boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                                HandlerMethod hm, A annotation) throws Exception { return true; }
    protected void postHandle(..., HandlerMethod hm, @Nullable ModelAndView mav, A annotation) throws Exception {}
    protected void afterCompletion(..., HandlerMethod hm, @Nullable Exception ex, A annotation) throws Exception {}

    public final Class<A> getAnnotationType();
    protected A getAnnotation(HandlerMethod handlerMethod);   // defaults to handlerMethod.getMethodAnnotation(...)
}
```

The `HandlerMethod` versions of `preHandle`/`postHandle`/`afterCompletion` are made `final`: when the annotation is absent the request passes straight through, and the overload that takes the `A` parameter is invoked only on a hit — so you **only need to implement the annotation-parameter methods**. The annotation type is resolved from the generic argument via `ResolvableType`, so you must write a concrete subclass (`extends AnnotatedMethodHandlerInterceptor<RequiresAudit>`); raw types or anonymous generics will not work.

```java
@Target(METHOD) @Retention(RUNTIME)
public @interface RequiresAudit {
    String value() default "";
}

@Component
public class AuditInterceptor extends AnnotatedMethodHandlerInterceptor<RequiresAudit> {

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                HandlerMethod handlerMethod, RequiresAudit annotation) throws Exception {
        log.info("audit reason = {} for {}", annotation.value(), handlerMethod.getMethod());
        return true;
    }
}
```

> ⚠️ Caching side effect: on 1.x, `getMethodAnnotation(...)` writes the annotation result into a **`ServletContext` attribute**, keyed by `annotationType@handlerMethod.toString()`, to avoid repeated reflection. Since the `ServletContext` lives as long as the application and the number of keys grows with the number of handler methods, very large endpoint counts mean these entries stay resident for the whole lifetime. If you rebuild `HandlerMethod`s frequently in a canary/hot-deployment scenario, take this into account.

## 5. Request/response body retention

| Class | How it is triggered | Actual behaviour (verified against the source) |
|----|----------|----------------------|
| `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` | You must register it yourself (a subclass of `OncePerRequestFilter`) | **Wraps the response only**: wraps `HttpServletResponse` in a `ContentCachingResponseWrapper` and calls `copyBodyToResponse()` in a `finally` block; the static method `getResponseContentAsString(ServletRequest, ServletResponse)` returns `null` when the response is not a `ContentCachingResponseWrapper`, otherwise it reads the content and caches it in the request attribute `_ContentCachingFilter_` (constant `RESPONSE_CONTENT_REQUEST_ATTRIBUTE_NAME`) to avoid recomputation |
| `webmvc.advice.StoringRequestBodyArgumentAdvice` | `storeRequestBodyArgument = true` | A `final` `@RestControllerAdvice` extending `RequestBodyAdviceAdapter`; `supports(...)` holds only when the converter ∈ `WebMvcUtils.SUPPORTED_CONVERTER_TYPES`; `afterBodyRead` calls `WebMvcUtils.setHandlerMethodRequestBodyArgument(method, body)` |
| `webmvc.advice.StoringResponseBodyReturnValueAdvice` | `storeResponseBodyReturnValue = true` | A `@RestControllerAdvice` extending `ResponseBodyAdviceAdapter<Object>`; in `beforeBodyWrite` it obtains the `HttpServletRequest` only when `request instanceof ServletServerHttpRequest` and calls `WebMvcUtils.setHandlerMethodReturnValue(httpServletRequest, method, body)` |
| `webmvc.advice.RequestBodyAdviceAdapter` / `ResponseBodyAdviceAdapter` | Extend them yourself | Base classes with empty implementations of Spring's native advice interfaces |

`supports(...)` of both advices is restricted to `WebMvcUtils.SUPPORTED_CONVERTER_TYPES`, whose value is `{MappingJackson2HttpMessageConverter, StringHttpMessageConverter}` — **with any other `HttpMessageConverter` (XML, Protobuf, and so on) the body is not retained**.

The storage location is a **request attribute**, whose key is built from the prefix constants of `io.microsphere.spring.web.util.RequestAttributesUtils` plus the method signature:

| Constant | Value | Meaning |
|------|-----|------|
| `HANDLER_METHOD_REQUEST_BODY_ARGUMENT_ATTRIBUTE_NAME_PREFIX` | `HM.RB.ARG:` | The `@RequestBody` argument |
| `HANDLER_METHOD_RETURN_VALUE_ATTRIBUTE_NAME_PREFIX` | `HM.RV:` | The `@ResponseBody` return value |
| `HANDLER_METHOD_ARGUMENTS_ATTRIBUTE_NAME_PREFIX` | `HM.ARGS:` | All resolved arguments |

Read them through the public accessors of `io.microsphere.spring.webmvc.util.WebMvcUtils` (each group has an "implicit current request" overload and an "explicit `HttpServletRequest`" overload):

```java
WebMvcUtils.getHandlerMethodRequestBodyArgument(HandlerMethod)
WebMvcUtils.getHandlerMethodRequestBodyArgument(Method)
WebMvcUtils.getHandlerMethodReturnValue(HandlerMethod)
WebMvcUtils.getHandlerMethodArguments(HandlerMethod)   // Object[]
```

A complete example of auditing/replaying from an interceptor:

```java
@Component
public class AuditInterceptor implements HandlerMethodInterceptor {

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args,
                             @Nullable Object returnValue, @Nullable Throwable error,
                             NativeWebRequest request) throws Exception {
        Object body = WebMvcUtils.getHandlerMethodRequestBodyArgument(handlerMethod);
        log.info("handler = {}, request body = {}, return = {}",
                handlerMethod.getMethod(), body, returnValue);
    }
}
```

> Prerequisite and pitfall: `getHandlerMethodRequestBodyArgument(...)` relies on the `RequestAttributes` bound to the current thread. If you call it from an asynchronous thread (`@Async`, your own thread pool), you need `requestContextStrategy = INHERITABLE_THREAD_LOCAL` (see section 2) or the overload that takes an explicit `HttpServletRequest`.

## 6. `ReversedProxyHandlerMapping`: reverse proxying for gateways

`io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping` extends `AbstractHandlerMapping` and listens for `WebEndpointMappingsReadyEvent`:

- `DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1`, so it sits almost first in line;
- `getHandlerInternal` reads the request header `microsphere_wem_id` (constant `WebEndpointMapping.ID_HEADER_NAME`); when the value is `-1` (absent) it returns `null` so the following HandlerMappings take over;
- On a hit it fetches the mapping from the `Map<Integer, WebEndpointMapping>` cache and, when `source` is an `AbstractHandlerMapping`, builds the `HandlerExecutionChain` by reflectively invoking its **protected** `getHandlerExecutionChain(Object, HttpServletRequest)` through a `MethodHandle`;
- If the `source` type does not match, it returns `null` (no exception is thrown).

In other words: the gateway only has to carry the endpoint ID, and the business service can **bypass URL matching** and locate and execute the target controller method directly.

```java
@Configuration
@EnableWebMvcExtension(reversedProxyHandlerMapping = true)
public class GatewayFacingConfig {
}
```

On the gateway side, all you need is to add this while forwarding:

```
microsphere_wem_id: <WebEndpointMapping.getId()>
```

> Prerequisite: endpoint metadata must exist first (`registerWebEndpointMappings = true`, which is the default) so the cache gets populated by `WebEndpointMappingsReadyEvent`; if both are disabled, this HandlerMapping always returns `null`.

## 7. `WebMvcConfigurerAdapter` (a key compatibility class on 1.x)

`io.microsphere.spring.webmvc.config.WebMvcConfigurerAdapter` is an **interface** (not the deprecated Spring class of the same name):

```java
public interface WebMvcConfigurerAdapter extends WebMvcConfigurer {
    @Override default void configurePathMatch(PathMatchConfigurer configurer) {}
    @Override default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {}
    @Override default void configureAsyncSupport(AsyncSupportConfigurer configurer) {}
    @Override default void addInterceptors(InterceptorRegistry registry) {}
    // …an empty default implementation for every method of WebMvcConfigurer
}
```

The reason it exists: **all methods of Spring 4.3's `WebMvcConfigurer` are abstract**, so an implementing class has to override every single one. This interface supplies empty defaults up front, which means:

- on Spring 4.3 / 5.0 you can implement only the hooks you care about;
- on Spring 5.0+ it degrades to being equivalent to implementing `WebMvcConfigurer` directly (because the native interface already has defaults).

```java
@Configuration
public class WebConfig implements WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuditInterceptor()).addPathPatterns("/admin/**");
    }
}
```

## 8. Remaining components

| Class | Description |
|----|------|
| `webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer` | Customizes `ContentNegotiationManager` through configuration properties |
| `webmvc.context.ExclusiveViewResolverApplicationListener` | Controls view-resolver singling/priority; affected by the `microsphere.spring.webmvc.view-resolver.*` properties |
| `webmvc.interceptor.LazyCompositeHandlerInterceptor` | Lazily resolves and executes a group of interceptors in order (registered by the registrar, bean-name constant `BEAN_NAME`) |
| `webmvc.interceptor.LoggingMethodHandlerInterceptor` | A ready-made request-logging interceptor |
| `webmvc.interceptor.AbstractPageRenderContextHandlerInterceptor` / `LoggingPageRenderContextHandlerInterceptor` | Server-side rendering scenarios: condense model/view information into a "page render context" |
| `webmvc.metadata.HandlerMappingWebEndpointMappingFactory` / `HandlerMetadataWebEndpointMappingFactory` / `RequestMappingMetadataWebEndpointMappingFactory` | Generate `WebEndpointMapping`s from `HandlerMapping`, `HandlerMetadata`, and `@RequestMapping` metadata |
| `webmvc.metadata.RequestMappingMetadata` | A structured description of `@RequestMapping` |
| `webmvc.method.support.HandlerMethodArgumentResolverAdvice` / `LoggingHandlerMethodArgumentResolverAdvice` | Argument-resolver-level advice |
| `webmvc.util.WebMvcUtils` / `ViewUtils` / `ViewResolverUtils` / `SpringWebMvcHelper` | MVC utilities and the MVC implementation of `SpringWebHelper` |
| `web.servlet.util.WebUtils` | Servlet-layer utilities (`javax.servlet`) |

Property prefixes (`io.microsphere.spring.webmvc.constants.PropertyConstants`):

```java
String MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX = "microsphere.spring.webmvc.";
String MICROSPHERE_SPRING_WEBMVC_VIEW_RESOLVER_PROPERTY_NAME_PREFIX = "microsphere.spring.webmvc.view-resolver.";
```

`META-INF/spring.factories` declares only the `SpringWebHelper` implementation mapping:

```
io.microsphere.spring.web.util.SpringWebHelper=\
io.microsphere.spring.webmvc.util.SpringWebMvcHelper
```
