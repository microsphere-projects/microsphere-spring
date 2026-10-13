# microsphere-spring-webflux · 0.2.x

[← 当前版本总览](./README.md) · [English](../webflux.md) · [开发者文档](../../../developer-guide/current/zh/webflux.md) · [1.x 文档](../../1.x/zh/webflux.md)

`microsphere-spring-webflux` 是 Web MVC 扩展的响应式对应物：`@EnableWebFluxExtension` 把同一套端点注册表与控制器方法拦截能力装配进 WebFlux 应用；由于 `ServerWebExchange` 会经 `ServerWebRequest` 适配，为 MVC 编写的共享 SPI 无需改动即可继续生效。

---

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-webflux` |
| 分支 | `release`（0.2.x 线） |
| 版本 | `0.2.39` / 开发版 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x**（对应 Reactor BOM：2022.0.22 / 2023.0.19 / 2024.0.17 / 2025.0.7） |
| 必需依赖 | `microsphere-spring-web`（进而传递 `microsphere-spring-context`） |
| 可选依赖 | `spring-context`、`spring-webflux`、`jackson-annotations`、`jackson-databind` |

Reactor 版本由父 POM 的 `reactor-bom` 随所选 Spring profile 一起切换，因此使用本模块时请保持 Spring Framework 与 Reactor BOM 的对应关系（见上表）。

---

## 1. `@EnableWebFluxExtension`

`io.microsphere.spring.webflux.annotation.EnableWebFluxExtension`，`@Target(TYPE)`，是 `@EnableWebExtension` 的响应式特化（`@EnableWebExtension` + `@OverrideAnnotationAttributes` + `@Import(WebFluxExtensionBeanDefinitionRegistrar.class)`）。

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `registerWebEndpointMappings` | `boolean` | `true` | 别名到 `@EnableWebExtension` |
| `interceptHandlerMethods` | `boolean` | `true` | 别名 |
| `publishEvents` | `boolean` | `true` | 别名 |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 别名 |
| `requestContextStrategy` | `RequestContextStrategy` | `DEFAULT` | 别名（响应式下作用于 `RequestContextWebFilter`） |
| `storeRequestBodyArgument` | `boolean` | `false` | 注册 `StoringRequestBodyArgumentInterceptor`（请求体暂存） |
| `storeResponseBodyReturnValue` | `boolean` | `false` | 注册 `StoringResponseBodyReturnValueInterceptor`（响应体暂存） |
| `reversedProxyHandlerMapping` | `boolean` | `false` | 注册 WebFlux 版 `ReversedProxyHandlerMapping`（见第 4 节） |

**注意**：本注解**没有** `registerHandlerInterceptors` / `handlerInterceptors` 属性——WebFlux 模型中不存在 `HandlerInterceptor`，对应扩展点是 `WebFilter`。

七项注册各自都有独立的属性开关（`WebFluxExtensionBeanDefinitionRegistrar.java:61-73`）：`registerWebEndpointMappings` → `HandlerMappingWebEndpointMappingResolver`，`interceptHandlerMethods` → `InterceptingHandlerMethodProcessor`，`publishEvents` → `RequestHandledEventPublishingWebFilter`，`storeRequestBodyArgument` / `storeResponseBodyReturnValue` → 两个暂存拦截器，`reversedProxyHandlerMapping` → `ReversedProxyHandlerMapping`。

⚠ `RequestContextWebFilter` 是最容易被误读的一项：它**只在** `requestContextStrategy = THREAD_LOCAL` 或 `INHERITABLE_THREAD_LOCAL` 时才注册（`:101-122`）。默认的 `DEFAULT` 下不会创建任何请求上下文过滤器 Bean——不显式开启就没有 ThreadLocal 请求上下文。

```java
@Configuration
@EnableWebFluxExtension(storeRequestBodyArgument = true)
public class ReactiveWebConfig {
}
```

---

## 2. 控制器方法拦截（响应式）

拦截 SPI 与 MVC 完全共用（见 [web.md 第 4 节](./web.md)）：实现 `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` 并注册为 Bean 即可，由 `InterceptingHandlerMethodProcessor` 在响应式链上回调这些 Bean。

```java
@Component
public class ReactiveAccessInterceptor implements HandlerMethodInterceptor {
    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) {
        log.info("-> {}", handlerMethod.getMethod().getName());
    }
}
```

`InterceptingHandlerMethodProcessor`（`io.microsphere.spring.webflux.method`，Bean 名 `interceptingHandlerMethodProcessor`）继承 `OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>`，一个对象同时实现 `WebFilter`、`HandlerAdapter`、`HandlerMethodArgumentResolver`、`HandlerResultHandler`、`WebExceptionHandler` 与 `Ordered`（`InterceptingHandlerMethodProcessor.java:81-83`），并缓存 `MethodParameter` 的解析上下文；`RequestContextWebFilter` 的 `threadContextInheritable` 由 `requestContextStrategy` 推导，`ServerWebRequest` 负责把 `ServerWebExchange` 适配成 `NativeWebRequest`。正是这套组合，让为 MVC 编写的 `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` Bean 在响应式应用中无需修改即可直接使用。

---

## 3. 端点元数据

`HandlerMappingWebEndpointMappingResolver` 与 `HandlerMappingWebEndpointMappingFactory` / `HandlerMetadataWebEndpointMappingFactory` / `RequestMappingMetadataWebEndpointMappingFactory` 从 `@RequestMapping` 与函数式端点提取映射，`Kind` 为 `WEB_FLUX`；函数式端点由 `RouterFunctionVisitorAdapter`、`RequestPredicateVisitorAdapter`、`RequestPredicateKind`、`ConsumingWebEndpointMappingAdapter` 遍历 `RouterFunction` 得到。读取方式与 MVC 相同：注入 `WebEndpointMappingRegistry` 并监听 `WebEndpointMappingsReadyEvent`。

---

## 4. `ReversedProxyHandlerMapping`

`io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping`，`order = HIGHEST_PRECEDENCE + 1`，`getHandlerInternal(ServerWebExchange)` 返回 `Mono<?>`；只筛选 `Kind.WEB_FLUX` 且 `source instanceof AbstractHandlerMapping` 的映射，依据请求头 `microsphere_wem_id` 直达已选端点。用法与 MVC 一致：

```java
@Configuration
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
public class ReactiveBackendConfig {
}
```

其设计与 MVC 版本一致（order 为 `HIGHEST_PRECEDENCE + 1`），仅接受来源为 `AbstractHandlerMapping` 的响应式映射，并按 `microsphere_wem_id` 分发，从而让网关转发的响应式请求精确执行网关已匹配到的那个端点。

---

## 5. 其他响应式组件

| 组件 | 说明 |
|---|---|
| `CompositeWebFilter` / `DelegatingWebFilter` | `WebFilter` 的组合与委派 |
| `RequestHandledEventPublishingWebFilter` | 发布 `ServerRequestHandledEvent`，对应 `publishEvents` |
| `ServerRequestHandledEvent` | 请求处理完成事件 |
| `MonoUtils` / `WebServerUtils` / `WebServerScope` | 常用 `Mono` 与服务器工具、作用域常量 |
| `SpringWebFluxHelper` | 经 `META-INF/spring.factories` 注册的模块装配入口 |
| `StoringRequestBodyArgumentInterceptor` / `StoringResponseBodyReturnValueInterceptor` | 请求体/响应体暂存（由 `store*` 属性开启） |
