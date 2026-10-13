# microsphere-spring-webmvc · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/webmvc.md) · [Developer guide](../../developer-guide/current/webmvc.md) · [1.x docs](../1.x/webmvc.md)

`microsphere-spring-webmvc` is the Spring Web MVC layer of the MicroSphere web extensions: it reuses the endpoint registry and the handler-method interception kernel of `microsphere-spring-web` and adds the MVC-only capabilities on top — automatic `HandlerInterceptor` registration, observation of argument resolvers, request/response body storing and gateway-forwarded dispatch through `ReversedProxyHandlerMapping`.

---

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-webmvc` |
| Branch | `release` (the 0.2.x line) |
| Version | `0.2.39` / development version `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework compatibility range | **6.0.x – 7.0.x** (6.0.23 / 6.1.21 / 6.2.18 / 7.0.9) |
| Servlet namespace | `jakarta.servlet.*` (`jakarta.servlet-api` is **provided**) |
| Required dependency | `microsphere-spring-web` (which transitively brings in `microsphere-spring-context`) |
| Optional dependencies | `spring-web`, `spring-webmvc`, `jackson-annotations`, `jackson-databind` |

---

## 1. `@EnableWebMvcExtension`

`io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension` (`@Target(TYPE)`) is the MVC specialization of `@EnableWebExtension`: it is meta-annotated with `@EnableWebExtension` plus `@OverrideAnnotationAttributes`, so attributes declared with `@AliasFor(annotation = EnableWebExtension.class)` on the child annotation are written back onto the parent annotation's semantics — i.e. they override the parent's values.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `registerWebEndpointMappings` | `boolean` | `true` | Aliased to `@EnableWebExtension` |
| `interceptHandlerMethods` | `boolean` | `true` | Aliased |
| `publishEvents` | `boolean` | `true` | Aliased. ⚠ The MVC registrar never reads it; `HandlerMethodArgumentsResolvedEvent` is published by the kernel's `WebEventPublisher` acting as a `HandlerMethodInterceptor`, so it only fires while handler-method interception is active |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Aliased |
| `requestContextStrategy` | `RequestContextStrategy` | `DEFAULT` | Aliased. ⚠ On MVC it is honoured only by `EnableWebMvcExtensionListener`, a `ServletContainerInitializer`, which registers a `RequestContextFilter` for `THREAD_LOCAL`/`INHERITABLE_THREAD_LOCAL` — an embedded (Spring Boot) deployment never runs it, so this attribute has no effect in MVC unless you deploy a WAR ([developer guide, section 4](../../developer-guide/current/webmvc.md)) |
| `registerHandlerInterceptors` | `boolean` | `false` | **MVC-only**: auto-registers every Spring `HandlerInterceptor` bean found in the context into the `InterceptorRegistry`; once enabled, `handlerInterceptors()` becomes ineffective |
| `handlerInterceptors` | `Class<? extends HandlerInterceptor>[]` | `{}` | **MVC-only**: the interceptor classes to register as beans explicitly and add to the MVC interceptor chain |
| `storeRequestBodyArgument` | `boolean` | `false` | Registers `StoringRequestBodyArgumentAdvice`, which stores the request body so it can be read repeatedly |
| `storeResponseBodyReturnValue` | `boolean` | `false` | Registers `StoringResponseBodyReturnValueAdvice`, which stores the response body |
| `reversedProxyHandlerMapping` | `boolean` | `false` | Registers `ReversedProxyHandlerMapping` (see section 4) |

`WebMvcExtensionBeanDefinitionRegistrar` installs **only one** bean unconditionally: `WebMvcExtensionConfiguration`, a `WebMvcConfigurer` that wires `LazyCompositeHandlerInterceptor[]` into `addInterceptors` (`WebMvcExtensionBeanDefinitionRegistrar.java:86-88`). Everything else is attribute-gated (`:71-79`): `registerWebEndpointMappings` registers `ServletWebEndpointMappingResolver` **and** `HandlerMappingWebEndpointMappingResolver` together (`:90-98`); `interceptHandlerMethods` registers `InterceptingHandlerMethodProcessor` under the bean name `interceptingHandlerMethodProcessor` (`:99-106`); `handlerInterceptors`/`registerHandlerInterceptors` build the interceptor beans and the lazy composite (`:108-113`); the two store-* flags and `reversedProxyHandlerMapping` each guard their own bean.

Because the defaults are `true`, the practical effect with a bare `@EnableWebMvcExtension` is that both the resolvers and the processor *are* registered — but switching either attribute off really does remove them.

```java
@Configuration
@EnableWebMvcExtension(
        interceptHandlerMethods = true,
        registerWebEndpointMappings = true,
        storeRequestBodyArgument = true,
        storeResponseBodyReturnValue = true
)
public class WebConfig {
}
```

---

## 2. Intercepting controller methods

Controller cross-cutting is available at three levels: the shared `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` SPI (this section), the Spring MVC-shaped `MethodHandlerInterceptor` base class (section 3, whose narrowed `preHandle`/`postHandle`/`afterCompletion` are abstract and whose `supports(...)` defaults to `true`, plus the annotation-driven `AnnotatedMethodHandlerInterceptor<A>` and the ready-made `LoggingMethodHandlerInterceptor`), and `HandlerMethodArgumentResolverAdvice` for observing argument resolvers.

Implement the shared SPI of the web module (`HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor`; for the method signatures see [web.md, section 4](./web.md)) and register the implementation as a bean: `InterceptingHandlerMethodProcessor` will then invoke it.

```java
@Component
public class TimingInterceptor implements HandlerMethodInterceptor {
    private static final ThreadLocal<Long> START = new ThreadLocal<>();

    @Override
    public void beforeExecute(HandlerMethod hm, Object[] args, NativeWebRequest request) {
        START.set(System.nanoTime());
    }

    @Override
    public void afterExecute(HandlerMethod hm, Object[] args, Object returnValue,
                             Throwable error, NativeWebRequest request) {
        long cost = System.nanoTime() - START.get();
        START.remove();
        log.info("{} cost {} ns", hm.getMethodDescription(), cost);
    }
}
```

Argument-level interception implements `HandlerMethodArgumentInterceptor`; on the MVC side `HandlerMethodArgumentResolverAdvice` (with the built-in `LoggingHandlerMethodArgumentResolverAdvice`) is available to observe the behaviour of `HandlerMethodArgumentResolver` instances.

---

## 3. `MethodHandlerInterceptor` / `AnnotatedMethodHandlerInterceptor`

Both live in `io.microsphere.spring.webmvc.interceptor`. `MethodHandlerInterceptor` is an abstract implementation of Spring's `HandlerInterceptor`: it makes `preHandle/postHandle/afterCompletion` `final`, performs the `handler instanceof HandlerMethod` test and the `supports(...)` filter internally, and then delegates to same-named **abstract methods whose parameter is narrowed to `HandlerMethod`** (you must implement them).

```java
public abstract class MethodHandlerInterceptor implements HandlerInterceptor {
    protected abstract boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                         HandlerMethod handlerMethod) throws Exception;
    protected abstract void postHandle(HttpServletRequest request, HttpServletResponse response,
                                       HandlerMethod handlerMethod,
                                       @Nullable ModelAndView modelAndView) throws Exception;
    protected abstract void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                            HandlerMethod handlerMethod,
                                            @Nullable Exception ex) throws Exception;
    protected boolean supports(HttpServletRequest request, HttpServletResponse response,
                               HandlerMethod handlerMethod) throws Exception {
        return true;   // override to scope the interceptor
    }
}
```

```java
@Component
public class AdminAuditInterceptor extends MethodHandlerInterceptor {
    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                HandlerMethod handlerMethod) {
        return true;
    }
    @Override
    protected void postHandle(HttpServletRequest request, HttpServletResponse response,
                              HandlerMethod handlerMethod, @Nullable ModelAndView mav) {
        log.info("[ADMIN] {} -> {}", request.getRequestURI(), handlerMethod.getMethod());
    }
    @Override
    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   HandlerMethod handlerMethod, @Nullable Exception ex) {
        if (ex != null) {
            log.error("[ADMIN] {} failed", request.getRequestURI(), ex);
        }
    }
}
```

`AnnotatedMethodHandlerInterceptor<A extends Annotation>` matches on a generic annotation on top of that: it only calls back when the target method (or its class) carries annotation `A`. `LoggingMethodHandlerInterceptor`, `LoggingPageRenderContextHandlerInterceptor` and `AbstractPageRenderContextHandlerInterceptor` are ready-made implementations you can use directly.

Register it in one of two ways:

```java
// 1) declare it as a bean and let every HandlerInterceptor enter the MVC interceptor chain
@EnableWebMvcExtension(registerHandlerInterceptors = true)

// 2) list the interceptor classes explicitly
@EnableWebMvcExtension(handlerInterceptors = AdminAuditInterceptor.class)
```

---

## 4. `ReversedProxyHandlerMapping`: gateway-forwarded dispatch

`io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping` extends `AbstractHandlerMapping` and listens for `WebEndpointMappingsReadyEvent`, caching the `WebEndpointMapping`s as `id -> mapping` (`order = HIGHEST_PRECEDENCE + 1`). When a gateway forwards a request for an endpoint it has already matched, it puts the `microsphere_wem_id` header on it; the backend service then **locates the `HandlerExecutionChain` directly from that id, skipping the ordinary URL re-matching**, which guarantees that "the endpoint chosen by the gateway" == "the endpoint actually executed by the backend".

```java
@Configuration
@EnableWebMvcExtension(reversedProxyHandlerMapping = true)
public class BackendWebConfig {
}
```

Precondition: `registerWebEndpointMappings` must stay `true` (which is the default), otherwise the registry is empty and no mapping can be matched.

---

## 5. Other MVC components

| Component | Description |
|---|---|
| `RequestBodyAdviceAdapter` / `ResponseBodyAdviceAdapter<T>` | No-op base implementations of Spring's `RequestBodyAdvice`/`ResponseBodyAdvice`, override selectively |
| `StoringRequestBodyArgumentAdvice` / `StoringResponseBodyReturnValueAdvice` | Enabled by the `storeRequestBodyArgument` / `storeResponseBodyReturnValue` attributes |
| `ContentCachingFilter` (`io.microsphere.spring.web.servlet.filter`) | Caches the request and response bodies on top of `ContentCaching*Wrapper` |
| `ConfigurableContentNegotiationManagerWebMvcConfigurer` | Configurable content negotiation |
| `ExclusiveViewResolverApplicationListener` | Automatic selection in the single-`ViewResolver` scenario |
| `EnableWebMvcExtensionListener` | `ServletContainerInitializer`: for classic WAR deployments it applies `requestContextStrategy` to the `FrameworkServlet` registrations via `threadContextInheritable`, and registers `RequestContextFilter` (mapped to `/*`) when it is missing |
| `HandlerMappingWebEndpointMappingResolver` / `HandlerMappingWebEndpointMappingFactory` / `RequestMappingMetadata*` | Extract endpoint metadata from `RequestMappingHandlerMapping` |
| `RouterFunctionVisitorAdapter` / `RequestPredicateVisitorAdapter` / `ConsumingWebEndpointMappingAdapter` | Functional endpoint (`RouterFunction`) traversal |
| Utilities | `WebMvcUtils`, `ViewUtils`, `ViewResolverUtils`, `SpringWebMvcHelper`, `WebUtils`, `PropertyConstants` |
