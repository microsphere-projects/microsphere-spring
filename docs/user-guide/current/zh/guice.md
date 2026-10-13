# microsphere-spring-guice · 0.2.x

[← 当前版本总览](./README.md) · [English](../guice.md) · [开发者文档](../../../developer-guide/current/zh/guice.md) · [1.x 文档](../../1.x/zh/guice.md)

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-guice` |
| 分支 | `release`（0.2.x 线） |
| 版本 | `0.2.39` / 开发版 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x** |
| Guice 版本 | `com.google.inject:guice` **7.0.0**（optional，需自备） |
| 必需依赖 | `microsphere-spring-context` |
| 可选依赖 | `spring-context`、`guice` |

```xml
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>7.0.0</version>
</dependency>
```

Guice 7 面向 Java 11+ 且使用 `com.google.inject` 注解包，与本版本线（Java 17）兼容。

本模块只有三个类，唯一对外暴露的入口是 `@EnableGuice`。`microsphere-spring-context` 为必需依赖，`spring-context` 与 `com.google.inject:guice` **7.0.0** 均为 optional——请自行引入 Guice，否则注解类型 `com.google.inject.Inject` 将不存在。

---

## 1. 能力边界（务必先读）

`@EnableGuice`（`io.microsphere.spring.guice.annotation.EnableGuice`，`@Target(TYPE)`）**没有任何属性**，也**不创建 Guice `Injector`、不加载 Guice `Module`**。它唯一的职责是注册 `GuiceInjectAnnotationBeanPostProcessor`——一个继承自 context 模块 `AnnotatedInjectionBeanPostProcessor` 的 package-private 处理器，绑定注解 `com.google.inject.Inject`。也就是说：**Spring 管理的 Bean 上出现的 Guice `@Inject` 注入点（字段、方法、构造器）会由 Spring 容器来满足**，仅此而已。

需求性规则（`determineRequiredStatus` 把 Guice 的 `optional` 属性映射为 Spring 的 required 标记）：

| Guice 声明 | Spring 侧行为 |
|---|---|
| `@Inject`（无 `optional` 键） | `required = false`（走 `determineRequiredStatus` 的缺省分支，视为非必需） |
| `@Inject(optional = true)` | `required = false`，缺 Bean 时保持 `null` 且不报错 |
| `@Inject(optional = false)` | ⚠ 同样是 `required = false`，见下方说明 |

因此裸写的 `@Inject` 被视为**非必需**：找不到候选 Bean 时得到 `null`，而不是抛异常。

⚠ **`optional = false` 并不会让注入变成必需，`@Inject` 的任意写法都做不到。** 该 BeanPostProcessor 在构造时调用了
`setIgnoreDefaultValue(true)`（`AnnotatedInjectionBeanPostProcessor.java:249`），会丢弃所有「取值等于注解默认值」的属性；
Guice 声明的是 `boolean optional() default false`，于是 `optional = false` 在进入 `determineRequiredStatus` 之前就被过滤掉
（`GuiceInjectAnnotationBeanPostProcessor.java:42-47` 走 `!containsKey` 分支返回 `false`）。`optional = true` 同样返回
`false`，因为覆写实现返回的是 `Boolean.FALSE.equals(value)`。结论：**所有** Guice `@Inject` 位点都是可选的，无法通过该属性
恢复 fail-fast。若需要「缺失即报错」，请改用 Spring 的 `@Autowired`（默认必需）。

本模块**不提供**：`Injector`、`Module` 消费、`Provider`/`Key`/`Scope` 桥接，`@EnableGuice` 上也没有传入模块的属性。它适合**从 Guice 迁移到 Spring** 的过渡场景（保留旧类的 `@Inject` 写法），不适合需要完整 Guice 容器语义（`Module`、`Key`、`Provider`、`Scope`）的场景——若需要 Guice 自身的对象图语义，请保留真正的 Guice 容器。

---

## 2. 用法

```java
import com.google.inject.Inject;
import io.microsphere.spring.guice.annotation.EnableGuice;

@Configuration
@EnableGuice
public class IntegrationConfig {
}

@Service
public class LegacyService {          // 从 Guice 工程迁移而来的类
    @Inject
    private OrderRepository repository;        // 由 Spring 注入

    @Inject(optional = true)
    private MetricsReporter metrics;           // 无对应 Bean 时为 null
}
```

模块测试中的真实用法（`EnableGuiceTest`）：

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EnableGuiceTest.class})
@EnableGuice
class EnableGuiceTest {
    @Inject private ApplicationContext context;
    @Inject(optional = true) private String notExists;   // 断言保持 null
}
```

---

## 3. 装配链路

| 类 | 可见性 | 说明 |
|---|---|---|
| `guice.annotation.EnableGuice` | public | `@Import(GuiceConfiguration.class)`，无属性 |
| `guice.annotation.GuiceConfiguration` | package-private | `@Import(GuiceInjectAnnotationBeanPostProcessor.class)` |
| `guice.annotation.GuiceInjectAnnotationBeanPostProcessor` | package-private | `extends AnnotatedInjectionBeanPostProcessor`，`ANNOTATION_TYPE = Inject.class`，`OPTIONAL_ATTRIBUTE_NAME = "optional"`；覆写 `determineRequiredStatus(AnnotationAttributes)` |

处理器来自 context 模块，因此注入解析能力（`@Inject` 的字段/方法/构造器、按类型与名称匹配、`BeanFactory` 层次遍历等）与 Spring 原生 `@Autowired`/`@Resource` 走同一套基础设施。
