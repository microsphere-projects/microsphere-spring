# microsphere-spring-webmvc · 0.2.x

[← 当前版本总览](./README.md) · [English](../webmvc.md) · [开发者文档](../../../developer-guide/current/zh/webmvc.md) · [1.x 文档](../../1.x/zh/webmvc.md)

`microsphere-spring-webmvc` 是 MicroSphere Web 扩展中的 Spring Web MVC 层：复用 `microsphere-spring-web` 的端点注册表与控制器方法拦截内核，并在此之上提供 MVC 专属能力——`HandlerInterceptor` 自动注册、参数解析器观测、请求/响应体暂存，以及基于 `ReversedProxyHandlerMapping` 的网关转发直达调度。

---

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-webmvc` |
| 分支 | `release`（0.2.x 线） |
| 版本 | `0.2.39` / 开发版 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x**（6.0.23 / 6.1.21 / 6.2.18 / 7.0.9） |
| Servlet 命名空间 | `jakarta.servlet.*`（`jakarta.servlet-api` 为 **provided**） |
| 必需依赖 | `microsphere-spring-web`（进而传递 `microsphere-spring-context`） |
| 可选依赖 | `spring-web`、`spring-webmvc`、`jackson-annotations`、`jackson-databind` |

---

## 1. `@EnableWebMvcExtension`

`io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension`，`@Target(TYPE)`，本质是 `@EnableWebExtension` 的 MVC 特化：它标注了 `@EnableWebExtension` + `@OverrideAnnotationAttributes`，因此子注解上以 `@AliasFor(annotation = EnableWebExtension.class)` 声明的属性会回写到父注解语义，也就是覆盖父注解的取值。

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `registerWebEndpointMappings` | `boolean` | `true` | 别名到 `@EnableWebExtension` |
| `interceptHandlerMethods` | `boolean` | `true` | 别名 |
| `publishEvents` | `boolean` | `true` | 别名。⚠ MVC 注册器从不读取它；`HandlerMethodArgumentsResolvedEvent` 由内核的 `WebEventPublisher` 以 `HandlerMethodInterceptor` 身份发布，因此只有在控制器方法拦截生效时才会触发 |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 别名 |
| `requestContextStrategy` | `RequestContextStrategy` | `DEFAULT` | 别名。⚠ MVC 侧只有 `EnableWebMvcExtensionListener`（一个 `ServletContainerInitializer`）会读取它，并为 `THREAD_LOCAL`/`INHERITABLE_THREAD_LOCAL` 注册 `RequestContextFilter`；内嵌（Spring Boot）启动不会执行 SCI，所以打成 WAR 部署之外这个属性不起作用（见[开发者文档第 4 节](../../../developer-guide/current/zh/webmvc.md)） |
| `registerHandlerInterceptors` | `boolean` | `false` | **MVC 专属**：自动把容器中全部 Spring `HandlerInterceptor` Bean 注册进 `InterceptorRegistry`；开启后 `handlerInterceptors()` 失效 |
| `handlerInterceptors` | `Class<? extends HandlerInterceptor>[]` | `{}` | **MVC 专属**：显式指定要注册为 Bean 并加入 MVC 拦截链的拦截器类 |
| `storeRequestBodyArgument` | `boolean` | `false` | 注册 `StoringRequestBodyArgumentAdvice`，暂存请求体以便重复读取 |
| `storeResponseBodyReturnValue` | `boolean` | `false` | 注册 `StoringResponseBodyReturnValueAdvice`，暂存响应体 |
| `reversedProxyHandlerMapping` | `boolean` | `false` | 注册 `ReversedProxyHandlerMapping`（见第 4 节） |

`WebMvcExtensionBeanDefinitionRegistrar` 真正**无条件**装配的只有一个 Bean：`WebMvcExtensionConfiguration`（一个 `WebMvcConfigurer`，把 `LazyCompositeHandlerInterceptor[]` 挂入 `addInterceptors`，`:86-88`）。其余全部由属性开关（`:71-79`）：`registerWebEndpointMappings` 一次性注册 `ServletWebEndpointMappingResolver` **与** `HandlerMappingWebEndpointMappingResolver`（`:90-98`）；`interceptHandlerMethods` 注册 `InterceptingHandlerMethodProcessor`（Bean 名 `interceptingHandlerMethodProcessor`，`:99-106`）；`registerHandlerInterceptors` / `handlerInterceptors` 决定拦截器 Bean 与懒合并拦截器的注册（`:108-113`）；两个 store-* 标志和 `reversedProxyHandlerMapping` 各管各的 Bean。

因为这些开关的默认值都是 `true`，裸写 `@EnableWebMvcExtension` 时两个 resolver 和 processor 实际都会被注册；但把任一开关关掉，它们确实会消失。

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

## 2. 控制器方法拦截

控制器横切能力有三个层面：web 模块共用的 `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` SPI（本节）；Spring MVC 形态的 `MethodHandlerInterceptor` 基类（第 3 节，其收窄后的 `preHandle`/`postHandle`/`afterCompletion` 为抽象方法，`supports(...)` 默认返回 `true`，另有按注解匹配的 `AnnotatedMethodHandlerInterceptor<A>` 与开箱即用的 `LoggingMethodHandlerInterceptor`）；以及用于观测参数解析器的 `HandlerMethodArgumentResolverAdvice`。

实现 web 模块的共用 SPI（`HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor`，方法签名见 [web.md 第 4 节](./web.md)），注册为 Bean 即被 `InterceptingHandlerMethodProcessor` 调用。

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

参数级拦截则实现 `HandlerMethodArgumentInterceptor`；MVC 侧还提供 `HandlerMethodArgumentResolverAdvice`（含内置的 `LoggingHandlerMethodArgumentResolverAdvice`），用于观测 `HandlerMethodArgumentResolver` 的行为。

---

## 3. `MethodHandlerInterceptor` / `AnnotatedMethodHandlerInterceptor`

二者位于 `io.microsphere.spring.webmvc.interceptor`。`MethodHandlerInterceptor` 是 Spring `HandlerInterceptor` 的抽象实现：把 `preHandle/postHandle/afterCompletion` 置为 `final`，内部完成 `handler instanceof HandlerMethod` 判断与 `supports(...)` 过滤后，转而调用**收窄为 `HandlerMethod` 参数的同名抽象方法**（必须实现）。

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
        return true;   // 覆写以限定生效范围
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

`AnnotatedMethodHandlerInterceptor<A extends Annotation>` 在此之上再按泛型注解匹配：仅当目标方法（或其类）携带注解 `A` 时才回调。`LoggingMethodHandlerInterceptor`、`LoggingPageRenderContextHandlerInterceptor`、`AbstractPageRenderContextHandlerInterceptor` 为内置可用实现。

注册途径二选一：

```java
// 1) 声明为 Bean，并让全部 HandlerInterceptor 自动进入 MVC 拦截链
@EnableWebMvcExtension(registerHandlerInterceptors = true)

// 2) 显式列出拦截器类
@EnableWebMvcExtension(handlerInterceptors = AdminAuditInterceptor.class)
```

---

## 4. `ReversedProxyHandlerMapping`：网关转发直达

`io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping` 继承 `AbstractHandlerMapping` 并监听 `WebEndpointMappingsReadyEvent`，把 `WebEndpointMapping` 缓存为 `id -> mapping`（`order = HIGHEST_PRECEDENCE + 1`）。当网关按已匹配的端点转发请求时，会在请求头带上 `microsphere_wem_id`；后端服务据此**直接定位 `HandlerExecutionChain`，跳过常规 URL 重匹配**，从而保证“网关选中的端点 == 后端实际执行的端点”。

```java
@Configuration
@EnableWebMvcExtension(reversedProxyHandlerMapping = true)
public class BackendWebConfig {
}
```

前提：`registerWebEndpointMappings` 必须为 `true`（默认即是），否则注册表为空、映射无法命中。

---

## 5. 其他 MVC 组件

| 组件 | 说明 |
|---|---|
| `RequestBodyAdviceAdapter` / `ResponseBodyAdviceAdapter<T>` | Spring `RequestBodyAdvice`/`ResponseBodyAdvice` 的空实现基类，按需覆写 |
| `StoringRequestBodyArgumentAdvice` / `StoringResponseBodyReturnValueAdvice` | 由 `storeRequestBodyArgument` / `storeResponseBodyReturnValue` 开启 |
| `ContentCachingFilter`（`io.microsphere.spring.web.servlet.filter`） | 基于 `ContentCaching*Wrapper` 缓存请求/响应体 |
| `ConfigurableContentNegotiationManagerWebMvcConfigurer` | 可配置内容协商 |
| `ExclusiveViewResolverApplicationListener` | 单一 `ViewResolver` 场景下的自动选择 |
| `EnableWebMvcExtensionListener` | `ServletContainerInitializer`：传统 war 部署下按 `requestContextStrategy` 给 `FrameworkServlet` 设置 `threadContextInheritable`，并在缺少 `RequestContextFilter` 时自动注册（`/*`） |
| `HandlerMappingWebEndpointMappingResolver` / `HandlerMappingWebEndpointMappingFactory` / `RequestMappingMetadata*` | 从 `RequestMappingHandlerMapping` 提取端点元数据 |
| `RouterFunctionVisitorAdapter` / `RequestPredicateVisitorAdapter` / `ConsumingWebEndpointMappingAdapter` | 函数式端点（`RouterFunction`）遍历适配 |
| 工具 | `WebMvcUtils`、`ViewUtils`、`ViewResolverUtils`、`SpringWebMvcHelper`、`WebUtils`、`PropertyConstants` |
