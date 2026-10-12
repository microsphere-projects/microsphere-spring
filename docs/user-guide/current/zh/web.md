# microsphere-spring-web · 0.2.x

[← 当前版本总览](./README.md) · [English](../web.md) · [开发者文档](../../../developer-guide/current/zh/web.md) · [1.x 文档](../../1.x/zh/web.md)

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-web` |
| 分支 | `release`（0.2.x 线） |
| 版本 | 已发布 `0.2.39` / 开发中 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x**（6.0.23 / 6.1.21 / 6.2.18 / 7.0.9 回归验证） |
| Servlet 命名空间 | `jakarta.servlet.*`（`jakarta.servlet-api` 为 optional） |
| 必需依赖 | `microsphere-spring-context` |
| 可选依赖 | `spring-context`、`spring-web`、`jakarta.servlet-api`、`jackson-annotations`、`jackson-databind`、`jsr305` |

本模块是 `webmvc` / `webflux` 的公共内核，提供端点元数据注册表、控制器方法拦截 SPI 与请求上下文策略；引入 `microsphere-spring-webmvc` 或 `microsphere-spring-webflux` 时会自动传递引入本模块。唯一必需依赖只有 `microsphere-spring-context`，`spring-web`、`jakarta.servlet-api` 与 Jackson 均为可选。

---

## 1. `@EnableWebExtension`

`io.microsphere.spring.web.annotation.EnableWebExtension`，`@Target({TYPE, ANNOTATION_TYPE}) @Inherited @Documented`，通过 `@Import(WebExtensionBeanDefinitionRegistrar.class)` 生效。

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `registerWebEndpointMappings` | `boolean` | `true` | 启动时收集并注册端点元数据 |
| `interceptHandlerMethods` | `boolean` | `true` | 装配控制器方法拦截链 |
| `publishEvents` | `boolean` | `true` | 发布 Web 事件（注册 `WebEventPublisher`） |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | 扩展组件发现来源 |
| `requestContextStrategy` | `RequestContextStrategy` | `DEFAULT` | `DEFAULT` / `THREAD_LOCAL` / `INHERITABLE_THREAD_LOCAL` |

注册器行为要点：

- 若容器中已存在多个 `WebEndpointMappingRegistry` 实现，会把它们降级为 non-primary，并由 `CompositeWebEndpointMappingRegistry` 聚合，从而保证容器内只有一个逻辑注册表；否则直接使用 `SimpleWebEndpointMappingRegistry`。
- 始终注册 `WebEndpointMappingRegistrar`（一个 `SmartLifecycle`）：在 `doStart()` 阶段依次调用所有 `WebEndpointMappingResolver`，应用 `WebEndpointMappingFilter` 与 `WebEndpointMappingFactory`，写入注册表后发布 `WebEndpointMappingsReadyEvent`。
- `interceptHandlerMethods = true` 时注册 `DelegatingHandlerMethodAdvice`（Bean 名 `delegatingHandlerMethodAdvice`），把调用扇出到所有拦截器 Bean。

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

## 2. `WebEndpointMapping<E>`：端点元数据

`io.microsphere.spring.web.metadata.WebEndpointMapping` 是不可变的端点描述模型，`Kind` 枚举为 `{ SERVLET, FILTER, WEB_MVC, WEB_FLUX, CUSTOMIZED }`。通过静态入口 + Builder 构造：

```java
WebEndpointMapping<HandlerMethod> mapping = WebEndpointMapping.<HandlerMethod>webmvc()
        .endpoint(handlerMethod)
        .patterns("/products/{id}")        // String... / Collection<String> 重载
        .methods("GET")                    // String... / HttpMethod... 重载
        .params("page", "size")
        .headers("X-Tenant")
        .produces("application/json")
        .source("order-service")
        .negate()                          // 取反匹配
        .build();

mapping.getKind();        // WEB_MVC
mapping.getId();          // 唯一 ID，对应转发头 microsphere_wem_id
mapping.getPatterns();    // getMethods()/getParams()/getHeaders()/getConsumes()/getProduces()
mapping.toExpression();   // 可读表达式，形如 "(GET && /products/{id})"
mapping.toJSON();
mapping.setAttribute("owner", "team-a");   // getAttribute(name)
```

Builder 提供 `endpoint`、`patterns`、`methods`、`params`、`headers`、`consume(s)`、`produce(s)`、`negate`、`source`、`nest*` 组合方法与 `build`；构建出的对象提供只读访问器、`setAttribute`/`getAttribute`，以及用于诊断的 `toJSON()` 与 `toExpression()`。

约定：`build()` 时未指定 `methods` 则取 `ALL_METHODS`（全部 HTTP 方法名集合），`patterns` 不可为空；header 中 `Content-Type` 归一为 consumes、`Accept` 归一为 produces；`nestPatterns/nestMethods/nestParams/nestHeaders/nestConsumes/nestProduces(Builder)` 用于合并另一映射的条件。常量 `ID_HEADER_NAME = "microsphere_wem_id"`、`UNKNOWN_SOURCE`。

---

## 3. 注册表与 SPI

| 接口 | 关键方法 | 用途 |
|---|---|---|
| `WebEndpointMappingRegistry` | `boolean register(WebEndpointMapping)`；可变参数/`Iterable` 重载返回写入条数；`Collection<WebEndpointMapping> getWebEndpointMappings()` | 读取全部端点元数据 |
| `WebEndpointMappingResolver` | `Collection<WebEndpointMapping> resolve(ApplicationContext)` | 从容器解析映射 |
| `WebEndpointMappingFactory<E>` | `Optional<WebEndpointMapping<E>> create(E)`；`boolean supports(E)`；`Class<E> getSourceType()` | 由对象生成映射 |
| `WebEndpointMappingFilter` | `extends Filter<WebEndpointMapping>` | 过滤不需要登记的映射 |

消费示例：

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

`SimpleWebEndpointMappingRegistry`、`CompositeWebEndpointMappingRegistry`、`FilteringWebEndpointMappingRegistry` 为内置实现；`ServletWebEndpointMappingResolver`、`HandlerMappingWebEndpointMappingResolver` 由 webmvc/webflux 模块提供。工厂体系 `AbstractWebEndpointMappingFactory`、`RegistrationWebEndpointMappingFactory`、`ServletRegistrationWebEndpointMappingFactory`、`FilterRegistrationWebEndpointMappingFactory`、`Jackson2WebEndpointMappingFactory`、`SmartWebEndpointMappingFactory` 覆盖 Servlet/Filter 注册与 JSON 输出场景。

---

## 4. 控制器方法拦截 SPI

`io.microsphere.spring.web.method.support` 下的两个接口，全部方法为 `default` 空实现，按需覆写：

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

`HandlerMethodAdvice` 是同语义的门面（方法名为 `beforeExecuteMethod`/`afterExecuteMethod`），`DelegatingHandlerMethodAdvice` 负责把调用分发给容器内所有拦截器 Bean。

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

注意：这两个接口是 **webmvc 与 webflux 共用**的 SPI，因此同一拦截器可同时服务于阻塞与响应式应用；但 WebFlux 侧的 `NativeWebRequest` 由模块内的 `ServerWebRequest` 适配。

---

## 5. 事件与其他组件

| 组件 | 说明 |
|---|---|
| `WebEndpointMappingsReadyEvent` | 端点元数据收集完成时发布，`ReversedProxyHandlerMapping` 等据此初始化 |
| `HandlerMethodArgumentsResolvedEvent` | 控制器方法参数解析完成后发布 |
| `HandlerMetadata` / `HandlerMethodMetadata` | 处理器元信息模型 |
| `WebRequestRule` + `rules` 包 | 按 pattern/method/param/header/consume/produce 组合匹配规则 |
| `WebUtils`、`RequestAttributesUtils`、`RequestContextStrategy` | 请求与上下文工具 |
| `SpringWebHelper` | 各 Web 模块经 `META-INF/spring.factories` 注册的通用装配入口 |
