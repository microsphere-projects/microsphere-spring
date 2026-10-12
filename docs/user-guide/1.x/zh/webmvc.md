# microsphere-spring-webmvc (1.x)

[← 1.x 版本总览](./README.md) · [English](../webmvc.md) · [开发者文档](../../../developer-guide/1.x/zh/webmvc.md) · [0.2.x 文档](../../current/zh/webmvc.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-webmvc` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | **4.3.x – 5.3.x** |
| Servlet 命名空间 | **`javax.servlet`**（`javax.servlet-api` 为 provided 作用域） |
| Servlet 容器（测试） | `tomcat-embed-core` `9.0.119` |

依赖：`microsphere-spring-web`（必需，内部依赖）；`spring-web` / `spring-webmvc` / `jackson-*` 为 `optional`；`javax.servlet-api` 为 `provided`；`microsphere-annotation-processor` 是编译期可选依赖。

> 与 0.2.x 的核心差异：本分支上所有 Servlet/request API 都是 `javax.*`；此外还有 1.x 专属的 `WebEndpointMappingVisitor` 协作类，以及 `META-INF/services/javax.servlet.ServletContainerInitializer` 装配入口。

---

## 1. `@EnableWebMvcExtension`

`io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension`：

```java
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@EnableWebExtension
@OverrideAnnotationAttributes
@Import(WebMvcExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebMvcExtension { ... }
```

它通过 `@EnableWebExtension`（用 `@AliasFor` 桥接）继承通用属性，然后额外增加 5 个 MVC 专属属性：

| 属性 | 默认值 | 桥接 | 语义 |
|------|--------|------|------|
| `registerWebEndpointMappings` | `true` | `@AliasFor(EnableWebExtension)` | 注册端点元数据组件 |
| `interceptHandlerMethods` | `true` | 同上 | 启用控制器方法拦截 |
| `publishEvents` | `true` | 同上 | 注册 `WebEventPublisher` |
| `sources` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 同上 | SPI 发现来源 |
| `requestContextStrategy` | `DEFAULT` | 同上 | 请求上下文的线程继承策略（见第 2 节） |
| `registerHandlerInterceptors` | `false` | **MVC 专属** | 是否自动注册 `HandlerInterceptor` |
| `handlerInterceptors` | `{}` | **MVC 专属** | 显式注册的拦截器类型 |
| `storeRequestBodyArgument` | `false` | **MVC 专属** | 注册 `StoringRequestBodyArgumentAdvice` |
| `storeResponseBodyReturnValue` | `false` | **MVC 专属** | 注册 `StoringResponseBodyReturnValueAdvice` |
| `reversedProxyHandlerMapping` | `false` | **MVC 专属** | 注册 `ReversedProxyHandlerMapping` |

> 由于该注解标注了 `@OverrideAnnotationAttributes`，上面所有属性都可以从外部配置覆盖（见 [context.md](./context.md) 第 9 节），因此无需改代码就能开关特性。

`WebMvcExtensionBeanDefinitionRegistrar` 实际注册的内容（逐条与源码核对）：

| 条件 | 注册的 Bean |
|------|-------------|
| 无条件 | `WebMvcExtensionConfiguration` |
| `registerWebEndpointMappings = true` | `ServletWebEndpointMappingResolver`、`HandlerMappingWebEndpointMappingResolver` |
| `interceptHandlerMethods = true` | `InterceptingHandlerMethodProcessor`（bean 名常量 `BEAN_NAME`） |
| `registerHandlerInterceptors = true` **或** `handlerInterceptors` 非空 | `LazyCompositeHandlerInterceptor`（拦截器类型数组作为构造参数传入）；显式列出类型时，每个类型单独注册 |
| `storeRequestBodyArgument = true` | `StoringRequestBodyArgumentAdvice` |
| `storeResponseBodyReturnValue = true` | `StoringResponseBodyReturnValueAdvice` |
| `reversedProxyHandlerMapping = true` | `ReversedProxyHandlerMapping` |

拦截器解析逻辑里有一个很容易被忽略的细节：

```java
Class<? extends HandlerInterceptor>[] handlerInterceptorClasses =
        registerHandlerInterceptors ? ALL_HANDLER_INTERCEPTOR_CLASSES : handlerInterceptors;
```

也就是说 **`registerHandlerInterceptors = true` 优先**：此时使用“全部 `HandlerInterceptor`”这个哨兵数组，`handlerInterceptors` 列表被忽略；只有当 `registerHandlerInterceptors = false` 时，才会把 `handlerInterceptors` 里列出的类型逐个注册（见源码中的 `registerInterceptors`：命中哨兵数组会立即返回）。

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

## 2. `EnableWebMvcExtensionListener`：ServletContainerInitializer 自动装配

本模块打包了：

```
# META-INF/services/javax.servlet.ServletContainerInitializer
io.microsphere.spring.web.servlet.listener.EnableWebMvcExtensionListener
```

```java
@HandlesTypes(EnableWebMvcExtension.class)
public class EnableWebMvcExtensionListener implements ServletContainerInitializer { ... }
```

Servlet 容器启动时，容器会把所有标注了 `@EnableWebMvcExtension` 的类交给这个监听器，监听器据此应用 `requestContextStrategy`：

| 策略 | 动作 |
|------|------|
| `DEFAULT` | 什么都不做，立即返回 |
| `THREAD_LOCAL` | 为每个 `FrameworkServlet`（含 `DispatcherServlet`）的注册设置 init-param `threadContextInheritable=false`；若尚不存在则注册 `RequestContextFilter` |
| `INHERITABLE_THREAD_LOCAL` | 同上，但 `threadContextInheritable=true` |

> 这是 1.x 上“到底谁读取 `requestContextStrategy`”这个问题的部分答案：web 模块的注册器并不读取它，MVC 依赖这个 `ServletContainerInitializer`，WebFlux 则依赖 `WebFluxExtensionBeanDefinitionRegistrar`（见 [webflux.md](./webflux.md)）。
>
> 前提：`ServletContainerInitializer` 需要由 Servlet 3.0+ 容器通过 SPI 发现；在嵌入式测试或非容器部署路径下不会触发。

## 3. 控制器方法拦截：`InterceptingHandlerMethodProcessor`

当 `interceptHandlerMethods = true` 时，注册的 `InterceptingHandlerMethodProcessor` 同时扮演多个角色（委托给 Spring 自己的 `HandlerMethodArgumentResolver` / `ReturnValueHandler`），并把 [web.md](./web.md) 中的两类拦截器织入调用链：

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

需要参数级别的观察时使用 `HandlerMethodArgumentInterceptor`；两类都可以通过 `sources` 的三个来源被发现。

## 4. `MethodHandlerInterceptor`：把 `Object handler` 收窄为 `HandlerMethod`

Spring 的 `HandlerInterceptor` 三个方法接收的都是 `Object handler`，因此你得自己写 `instanceof` 判断，还得自行处理静态资源处理器。`io.microsphere.spring.webmvc.interceptor.MethodHandlerInterceptor` 把这三个方法设为 `final` 并收窄参数：

```java
public abstract class MethodHandlerInterceptor implements HandlerInterceptor {

    @Override public final boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception;
    @Override public final void postHandle(HttpServletRequest req, HttpServletResponse resp, Object handler,
                                           @Nullable ModelAndView mav) throws Exception;
    @Override public final void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler,
                                                @Nullable Exception ex) throws Exception;

    // 子类实现这三个（均为 protected abstract）
    protected abstract boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                                        HandlerMethod handlerMethod) throws Exception;
    protected abstract void postHandle(HttpServletRequest req, HttpServletResponse resp,
                                       HandlerMethod handlerMethod, ModelAndView mav) throws Exception;
    protected abstract void afterCompletion(HttpServletRequest req, HttpServletResponse resp,
                                            HandlerMethod handlerMethod, Exception ex) throws Exception;

    // 可选覆写，默认为 true
    protected boolean supports(HttpServletRequest req, HttpServletResponse resp,
                               HandlerMethod handlerMethod) throws Exception { return true; }
}
```

这些 `final` 包装方法的固定语义：`handler` 不是 `HandlerMethod` → 直接放行（`preHandle` 返回 `true`，另外两个静默跳过）；是 `HandlerMethod` **且** `supports(...)` 返回 `true` → 才会调用收窄后的抽象方法。三个抽象方法都 `throw Exception`，因此 `preHandle` 必须返回布尔值。

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

`AnnotatedMethodHandlerInterceptor<A extends Annotation>` 在此基础上构建，按泛型注解类型匹配；它的回调多出一个注解参数：

```java
public abstract class AnnotatedMethodHandlerInterceptor<A extends Annotation> extends MethodHandlerInterceptor {

    protected boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                                HandlerMethod hm, A annotation) throws Exception { return true; }
    protected void postHandle(..., HandlerMethod hm, @Nullable ModelAndView mav, A annotation) throws Exception {}
    protected void afterCompletion(..., HandlerMethod hm, @Nullable Exception ex, A annotation) throws Exception {}

    public final Class<A> getAnnotationType();
    protected A getAnnotation(HandlerMethod handlerMethod);   // 默认为 handlerMethod.getMethodAnnotation(...)
}
```

`HandlerMethod` 版本的 `preHandle`/`postHandle`/`afterCompletion` 被声明为 `final`：注解不存在时请求直接放行，只有命中时才会调用带 `A` 参数的重载——所以你**只需要实现带注解参数的那些方法**。注解类型通过 `ResolvableType` 从泛型实参解析，因此必须写成具体子类（`extends AnnotatedMethodHandlerInterceptor<RequiresAudit>`）；裸类型或匿名泛型都不起作用。

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

> ⚠️ 缓存副作用：在 1.x 上，`getMethodAnnotation(...)` 会把注解结果写入 **`ServletContext` 属性**，键为 `annotationType@handlerMethod.toString()`，以避免重复反射。由于 `ServletContext` 与应用同生命周期、键的数量随处理方法数量增长，端点数量极大时这些条目会全程常驻。若在灰度/热部署场景下频繁重建 `HandlerMethod`，请把这一点考虑在内。

## 5. 请求/响应体保留

| 类 | 触发方式 | 实际行为（与源码核对） |
|----|----------|----------------------|
| `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` | 需要你自己注册（`OncePerRequestFilter` 的子类） | **只包装响应**：把 `HttpServletResponse` 包进 `ContentCachingResponseWrapper`，并在 `finally` 块中调用 `copyBodyToResponse()`；静态方法 `getResponseContentAsString(ServletRequest, ServletResponse)` 在响应不是 `ContentCachingResponseWrapper` 时返回 `null`，否则读取内容并缓存到请求属性 `_ContentCachingFilter_`（常量 `RESPONSE_CONTENT_REQUEST_ATTRIBUTE_NAME`）以避免重复计算 |
| `webmvc.advice.StoringRequestBodyArgumentAdvice` | `storeRequestBodyArgument = true` | `final` 的 `@RestControllerAdvice`，继承 `RequestBodyAdviceAdapter`；`supports(...)` 仅在转换器 ∈ `WebMvcUtils.SUPPORTED_CONVERTER_TYPES` 时成立；`afterBodyRead` 调用 `WebMvcUtils.setHandlerMethodRequestBodyArgument(method, body)` |
| `webmvc.advice.StoringResponseBodyReturnValueAdvice` | `storeResponseBodyReturnValue = true` | `@RestControllerAdvice`，继承 `ResponseBodyAdviceAdapter<Object>`；`beforeBodyWrite` 中只有在 `request instanceof ServletServerHttpRequest` 时才取得 `HttpServletRequest`，并调用 `WebMvcUtils.setHandlerMethodReturnValue(httpServletRequest, method, body)` |
| `webmvc.advice.RequestBodyAdviceAdapter` / `ResponseBodyAdviceAdapter` | 由你自己继承 | 对 Spring 原生 advice 接口提供空实现的基类 |

两个 advice 的 `supports(...)` 都限定在 `WebMvcUtils.SUPPORTED_CONVERTER_TYPES`，其取值为 `{MappingJackson2HttpMessageConverter, StringHttpMessageConverter}` —— **使用其他任何 `HttpMessageConverter`（XML、Protobuf 等）时都不会保留请求/响应体**。

存储位置是**请求属性**，其键由 `io.microsphere.spring.web.util.RequestAttributesUtils` 的前缀常量加上方法签名拼成：

| 常量 | 值 | 含义 |
|------|-----|------|
| `HANDLER_METHOD_REQUEST_BODY_ARGUMENT_ATTRIBUTE_NAME_PREFIX` | `HM.RB.ARG:` | `@RequestBody` 参数 |
| `HANDLER_METHOD_RETURN_VALUE_ATTRIBUTE_NAME_PREFIX` | `HM.RV:` | `@ResponseBody` 返回值 |
| `HANDLER_METHOD_ARGUMENTS_ATTRIBUTE_NAME_PREFIX` | `HM.ARGS:` | 全部已解析参数 |

通过 `io.microsphere.spring.webmvc.util.WebMvcUtils` 的公开访问方法读取（每组都有一个“隐式当前请求”重载和一个“显式 `HttpServletRequest`”重载）：

```java
WebMvcUtils.getHandlerMethodRequestBodyArgument(HandlerMethod)
WebMvcUtils.getHandlerMethodRequestBodyArgument(Method)
WebMvcUtils.getHandlerMethodReturnValue(HandlerMethod)
WebMvcUtils.getHandlerMethodArguments(HandlerMethod)   // Object[]
```

在拦截器里做审计/重放的完整示例：

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

> 前提与坑：`getHandlerMethodRequestBodyArgument(...)` 依赖绑定到当前线程的 `RequestAttributes`。如果在异步线程（`@Async`、自建线程池）中调用，需要 `requestContextStrategy = INHERITABLE_THREAD_LOCAL`（见第 2 节），或者使用带显式 `HttpServletRequest` 的重载。

## 6. `ReversedProxyHandlerMapping`：面向网关的反向代理

`io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping` 继承 `AbstractHandlerMapping`，并监听 `WebEndpointMappingsReadyEvent`：

- `DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1`，因此它几乎排在队首；
- `getHandlerInternal` 读取请求头 `microsphere_wem_id`（常量 `WebEndpointMapping.ID_HEADER_NAME`）；取值为 `-1`（不存在）时返回 `null`，交由后续 HandlerMapping 接管；
- 命中时从 `Map<Integer, WebEndpointMapping>` 缓存取出映射，并在 `source` 是 `AbstractHandlerMapping` 时，通过 `MethodHandle` 反射调用其 **protected** 的 `getHandlerExecutionChain(Object, HttpServletRequest)` 来构造 `HandlerExecutionChain`；
- 若 `source` 类型不匹配，则返回 `null`（不抛异常）。

换句话说：网关只需带上端点 ID，业务服务就能**绕过 URL 匹配**，直接定位并执行目标控制器方法。

```java
@Configuration
@EnableWebMvcExtension(reversedProxyHandlerMapping = true)
public class GatewayFacingConfig {
}
```

网关侧只需在转发时加上：

```
microsphere_wem_id: <WebEndpointMapping.getId()>
```

> 前提：必须先存在端点元数据（`registerWebEndpointMappings = true`，即默认值），缓存才会由 `WebEndpointMappingsReadyEvent` 填充；两者都关闭时，这个 HandlerMapping 始终返回 `null`。

## 7. `WebMvcConfigurerAdapter`（1.x 上的关键兼容类）

`io.microsphere.spring.webmvc.config.WebMvcConfigurerAdapter` 是一个**接口**（不是 Spring 那个同名的已过时类）：

```java
public interface WebMvcConfigurerAdapter extends WebMvcConfigurer {
    @Override default void configurePathMatch(PathMatchConfigurer configurer) {}
    @Override default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {}
    @Override default void configureAsyncSupport(AsyncSupportConfigurer configurer) {}
    @Override default void addInterceptors(InterceptorRegistry registry) {}
    // …对 WebMvcConfigurer 的每个方法都提供空的默认实现
}
```

它存在的原因：**Spring 4.3 的 `WebMvcConfigurer` 所有方法都是抽象的**，实现类必须把每一个都覆写一遍。这个接口提前提供了空默认实现，这意味着：

- 在 Spring 4.3 / 5.0 上你可以只实现关心的钩子；
- 在 Spring 5.0+ 上它退化为等同于直接实现 `WebMvcConfigurer`（因为原生接口已带默认方法）。

```java
@Configuration
public class WebConfig implements WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuditInterceptor()).addPathPatterns("/admin/**");
    }
}
```

## 8. 其余组件

| 类 | 说明 |
|----|------|
| `webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer` | 通过配置属性定制 `ContentNegotiationManager` |
| `webmvc.context.ExclusiveViewResolverApplicationListener` | 控制视图解析器的筛选/优先级；受 `microsphere.spring.webmvc.view-resolver.*` 属性影响 |
| `webmvc.interceptor.LazyCompositeHandlerInterceptor` | 惰性解析并按序执行一组拦截器（由注册器注册，bean 名常量 `BEAN_NAME`） |
| `webmvc.interceptor.LoggingMethodHandlerInterceptor` | 现成的请求日志拦截器 |
| `webmvc.interceptor.AbstractPageRenderContextHandlerInterceptor` / `LoggingPageRenderContextHandlerInterceptor` | 服务端渲染场景：把 model/view 信息浓缩为“页面渲染上下文” |
| `webmvc.metadata.HandlerMappingWebEndpointMappingFactory` / `HandlerMetadataWebEndpointMappingFactory` / `RequestMappingMetadataWebEndpointMappingFactory` | 分别从 `HandlerMapping`、`HandlerMetadata`、`@RequestMapping` 元数据生成 `WebEndpointMapping` |
| `webmvc.metadata.RequestMappingMetadata` | `@RequestMapping` 的结构化描述 |
| `webmvc.method.support.HandlerMethodArgumentResolverAdvice` / `LoggingHandlerMethodArgumentResolverAdvice` | 参数解析器级别的 advice |
| `webmvc.util.WebMvcUtils` / `ViewUtils` / `ViewResolverUtils` / `SpringWebMvcHelper` | MVC 工具类以及 `SpringWebHelper` 的 MVC 实现 |
| `web.servlet.util.WebUtils` | Servlet 层工具类（`javax.servlet`） |

属性前缀（`io.microsphere.spring.webmvc.constants.PropertyConstants`）：

```java
String MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX = "microsphere.spring.webmvc.";
String MICROSPHERE_SPRING_WEBMVC_VIEW_RESOLVER_PROPERTY_NAME_PREFIX = "microsphere.spring.webmvc.view-resolver.";
```

`META-INF/spring.factories` 只声明了 `SpringWebHelper` 的实现映射：

```
io.microsphere.spring.web.util.SpringWebHelper=\
io.microsphere.spring.webmvc.util.SpringWebMvcHelper
```
