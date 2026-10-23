# microsphere-spring-web (1.x)

[← 1.x overview](./README.md) · [简体中文](./zh/web.md) · [Developer guide](../../developer-guide/1.x/web.md) · [0.2.x docs](../current/web.md)

## Version information

| Item | Value |
|------|-----|
| artifactId | `microsphere-spring-web` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version in development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility range | **4.3.x – 5.3.x** |
| Servlet API | `javax.servlet:javax.servlet-api` (`optional` / provided) |
| Reactive | `reactor-core` at test scope only; this module does not depend on WebFlux |

Dependency posture:

| Dependency | Notes |
|------|------|
| `microsphere-java-core` | Required (compile) |
| `microsphere-spring-context` | Required (internal dependency of this library, not optional) |
| `spring-context` / `spring-web` | `optional`, provided by the application |
| `javax.servlet-api` | `optional`, Servlet 3+ provided by the container |
| `jackson-annotations` / `jackson-databind` | `optional`, required only by `Jackson2WebEndpointMappingFactory` |
| `com.google.code.findbugs:jsr305` | `optional`, compile-time annotations |

This module is **the abstraction layer shared by MVC and WebFlux**: the endpoint metadata model, the registration/resolution SPI, and the controller-method interception SPI. It performs no routing itself and must be used together with [webmvc.md](./webmvc.md) or [webflux.md](./webflux.md).

> The API of this module is essentially the same on 1.x and 0.2.x, and the `@interface` attribute sets are identical; the differences are concentrated in the Servlet namespace (`javax.servlet` rather than `jakarta.servlet`) and in `WebEndpointMappingVisitor`, which exists only on 1.x.

---

## 1. `@EnableWebExtension`

`io.microsphere.spring.web.annotation.EnableWebExtension`:

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

| Attribute | Default | Semantics |
|------|--------|------|
| `registerWebEndpointMappings` | `true` | Registers the Resolver / Registry / Factory / Filter / Registrar components related to endpoint metadata |
| `interceptHandlerMethods` | `true` | Registers `HandlerMethodAdvice` and the two interceptor types, wrapping controller methods with AOP |
| `publishEvents` | `true` | Registers `WebEventPublisher`, which publishes the endpoint-ready / argument-resolution events |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Which sources SPI implementations are discovered from (`io.microsphere.spring.beans.BeanSource`) |
| `requestContextStrategy` | `DEFAULT` | The request-context storage strategy; see the "important note" below |

What `WebExtensionBeanDefinitionRegistrar` actually does (verified against the source, item by item):

1. When `registerWebEndpointMappings = true`:
   - Registers all `WebEndpointMappingResolver`, `WebEndpointMappingFactory` and `WebEndpointMappingFilter` implementations found according to `sources`;
   - Registry handling has special logic: when no registry is registered at all, the default `SimpleWebEndpointMappingRegistry` is registered; otherwise every already-registered Registry has its `primary` flag set to `false`, and `CompositeWebEndpointMappingRegistry` is registered as the primary bean;
   - Registers `WebEndpointMappingRegistrar` (which collects endpoint metadata during context refresh).
2. When `interceptHandlerMethods = true`: registers the `HandlerMethodAdvice` implementations, `HandlerMethodArgumentInterceptor` and `HandlerMethodInterceptor`, and registers `DelegatingHandlerMethodAdvice` under a fixed bean name.
3. When `publishEvents = true`: registers `WebEventPublisher`.

> ⚠️ On 1.x, the registrar of `@EnableWebExtension` itself **does not read** `requestContextStrategy` — the attribute is only declared on the annotation. The actual consumers are `EnableWebMvcExtensionListener` (webmvc) and `WebFluxExtensionBeanDefinitionRegistrar` (webflux). Therefore, if you only enable `@EnableWebExtension`, use the module-level annotation (`@EnableWebMvcExtension` / `@EnableWebFluxExtension`) to set the request-context strategy instead.

`RequestContextStrategy` values:

```java
public enum RequestContextStrategy {
    DEFAULT,                     // Keep Spring's default behaviour
    THREAD_LOCAL,                // Plain ThreadLocal
    INHERITABLE_THREAD_LOCAL;    // InheritableThreadLocal, visible to child threads
}
```

Usage:

```java
@Configuration
@EnableWebMvcExtension(          // Recommended: the module-level annotation inherits this annotation's attributes
        registerWebEndpointMappings = true,
        interceptHandlerMethods = true,
        requestContextStrategy = RequestContextStrategy.INHERITABLE_THREAD_LOCAL
)
public class WebConfig {
}
```

## 2. `WebEndpointMapping<E>`: the unified endpoint description

`io.microsphere.spring.web.metadata.WebEndpointMapping` is an immutable generic value object, where `E` is the described endpoint type (`ServletRegistrationBean`, `FilterRegistrationBean`, `HandlerMethod`, `RequestMappingInfo`, and so on).

Key fields and constants:

| Member | Description |
|------|------|
| `Kind kind` | `SERVLET` / `FILTER` / `WEB_MVC` / `WEB_FLUX` / `CUSTOMIZED` |
| `E endpoint` | The endpoint object itself (`transient`) |
| `int id` | ⚠ `Objects.hashCode(endpoint)` — the ID is the **endpoint object's** hash code (`WebEndpointMapping.java:1275`), not a digest of the pattern or conditions; it is what gateway pass-through matching keys on |
| `String[] patterns / methods` | The path patterns and HTTP methods |
| `String[] params / headers / consumes / produces` | The conditions (may be `null`) |
| `boolean negated` | Whether the match is negated |
| `Object source` | The source object (`UNKNOWN_SOURCE` acts as a placeholder) |
| `ID_HEADER_NAME = "microsphere_wem_id"` | The header name used for reverse-proxy pass-through |
| `ALL_METHODS` | The set of all `HttpMethod` names |

The Builder uses a **private constructor plus static entry points**, selected by `Kind`:

```java
public static <E> Builder<E> servlet();
public static <E> Builder<E> filter();
public static <E> Builder<E> webmvc();
public static <E> Builder<E> webflux();
public static <E> Builder<E> customized();
public static <E> Builder<E> of(Kind kind);
```

Chaining methods (an excerpt; all signatures were verified): `endpoint(E)`, `pattern(String)`, `patterns(String...)` / `patterns(Collection)` / `patterns(Collection, Function)`, `method(HttpMethod)` / `method(String)`, `methods(HttpMethod...)` / `methods(String...)` / `methods(Collection)`, `param(name, value)` / `param(nameAndValue)`, `header(name, value)` / `header(nameAndValue)`, `consume(MediaType|String)`, `produce(...)`, `negate()`, `build()`.

> `header(name, value)` has convenience branches: when `name` is `Content-Type` it delegates to `consume(value)`, and when it is `Accept` it delegates to `produce(value)` — there is no need to call those separately.

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

Frequently used accessors: `getKind()`, `getId()`, `getSource()`, `toExpression()` (renders a human-readable combined expression, for logs and gateway rules). `negate()` is called on the Builder, and the mapping it produces means "does not match these conditions".

## 3. SPI overview

| Interface | Responsibility | Discovery source |
|------|------|----------|
| `WebEndpointMappingFactory<E, T>` | Turns a source object (`HandlerMethod`, `ServletRegistration`, …) into a `WebEndpointMapping` | `sources` |
| `SmartWebEndpointMappingFactory` | A factory that can decide for itself whether it supports the given source object | `sources` |
| `AbstractWebEndpointMappingFactory` / `RegistrationWebEndpointMappingFactory` / `ServletRegistrationWebEndpointMappingFactory` / `FilterRegistrationWebEndpointMappingFactory` | Abstract base classes and built-in implementations, refined level by level | Built in |
| `Jackson2WebEndpointMappingFactory` | Uses Jackson to deserialize an external (e.g. JSON) description into a mapping | `sources` (requires jackson-databind) |
| `WebEndpointMappingRegistry` | The registry that stores mappings; `SimpleWebEndpointMappingRegistry` by default, and `CompositeWebEndpointMappingRegistry` aggregates the implementations when there are several | `sources` |
| `FilteringWebEndpointMappingRegistry` | A registry base class with filtering logic | Built in |
| `WebEndpointMappingResolver` | Resolves the target endpoint from the request/context; `ServletWebEndpointMappingResolver` targets Servlet | `sources` |
| `WebEndpointMappingFilter` | A predicate applied during the collection phase | `sources` |
| `WebEndpointMappingRegistrar` | Writes the factory output into the registry before `WebEndpointMappingsReadyEvent` | Registered unconditionally by the registrar |
| `WebEndpointMappingVisitor` | **1.x only**: traverses the mapping's condition tree in visitor form | Custom |

> ⚠️ On 1.x, `WebEndpointMappingVisitor` **is an interface definition only**: `git grep` shows it has no implementation inside the library and no call site (the interface does not exist at all on the 0.2.x branch). It is therefore a pure extension point — you must write the driver code that traverses a `WebEndpointMapping` yourself.

```java
void method(String[] methods);
void path(String pattern);
void header(String name, String value);
void queryParam(String name, String value);
void startAnd();  void and();      void endAnd();
void startOr();   void or();       void endOr();
void startNegate(); void endNegate();
```

It fits compiling a mapping into an external gateway DSL (Envoy / Nginx / a home-grown routing rule language):

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

## 4. Controller-method interception SPI

Both interfaces are defined in `io.microsphere.spring.web.method.support`. The `HandlerMethod` / `MethodParameter` types in their signatures come from `spring-web`, and `NativeWebRequest` makes them applicable to both MVC and WebFlux (on the WebFlux side they are adapted by `ServerWebRequest`).

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

Every method is `default`, so you only implement the one you care about. `afterExecute` receives both the return value and the error, and both may be `null` — do not assume that exactly one of them is non-null.

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

`HandlerMethodAdvice` / `DelegatingHandlerMethodAdvice` (fixed bean name) are the internal bridge that weaves these interceptors into controllers. There is normally no need to implement them directly — unless you want custom advice semantics.

## 5. Events

| Event | When it fires | Payload |
|------|----------|------|
| `WebEndpointMappingsReadyEvent` | Endpoint metadata collection is complete (context is ready) | All `WebEndpointMapping` instances |
| `HandlerMethodArgumentsResolvedEvent` | All arguments of one controller invocation have been resolved | `HandlerMethod` + the resolved arguments |

```java
@EventListener
public void onMappingsReady(WebEndpointMappingsReadyEvent event) {
    event.getMappings().forEach(m ->
            log.info("[{}] {} -> id={}", m.getKind(), m.toExpression(), m.getId()));
}
```

`WebEventPublisher` publishes the events above; it is not registered when `publishEvents = false`.

Event accessors (verified against the source):

| Event | Accessible methods |
|------|------------|
| `WebEndpointMappingsReadyEvent` | `extends ApplicationContextEvent`; `getMappings()` → `Collection<WebEndpointMapping>` |
| `HandlerMethodArgumentsResolvedEvent` | `getHandlerMethod()`, `getMethod()`, `getArguments()`, `getWebRequest()`; the constructor is `(WebRequest, HandlerMethod, Object...)` |

## 6. Utilities

| Class | Purpose |
|----|------|
| `io.microsphere.spring.web.util.WebRequestUtils` | Reads and writes attributes on a `NativeWebRequest`, obtains the `HttpServletRequest`/`HttpServletResponse` |
| `io.microsphere.spring.web.util.RequestAttributesUtils` | `RequestAttributes` hierarchy and attribute operations |
| `io.microsphere.spring.web.util.HttpUtils` | HTTP methods/status codes, header parsing |
| `io.microsphere.spring.web.util.WebUtils` | Generic reads of request parameters, cookies and so on |
| `io.microsphere.spring.web.util.SpringWebHelper` / `SpringWebType` / `WebType` / `WebSource` / `WebTarget` | Detects whether the current environment is MVC or WebFlux, normalizes the web type |
| `io.microsphere.spring.web.rule.*` | Condition-expression implementations (`WebRequestPatternsRule`, `WebRequestMethodsRule`, `WebRequestParamsRule`, `WebRequestHeadersRule`, `WebRequestConsumesRule`, `WebRequestProducesRule`, `CompositeWebRequestRule`, …) |

> The class name `WebRequestPatternsRule` contains a spelling deviation (`Pattens`); refer to it by its actual name.

`io.microsphere.spring.web.constants.PropertyConstants` provides the prefix constant:

```java
String MICROSPHERE_SPRING_WEB_PROPERTY_NAME_PREFIX = "microsphere.spring.web.";
```
