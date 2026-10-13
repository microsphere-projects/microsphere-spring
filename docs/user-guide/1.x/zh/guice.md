# microsphere-spring-guice · 1.x

[← 1.x 版本总览](./README.md) · [English](../guice.md) · [开发者文档](../../../developer-guide/1.x/zh/guice.md) · [0.2.x 文档](../../current/zh/guice.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-guice` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | **4.3.x – 5.3.x** |
| Guice | `com.google.inject:guice` **7.0.0**（`optional`） |

> ⚠️ **重要冲突**：Guice **7.0.0 需要 Java 11+**，而本分支编译目标是 Java 8。直接依赖本模块并在 JDK 8 上运行会失败。可行做法：
> 1. 在自己的 `<dependencyManagement>` 里把 Guice 降到 **5.x / 4.2.x**（仍使用 `com.google.inject.Inject`，本模块代码不受影响）；
> 2. 或者在 JDK 11+ 上运行应用（库字节码仍是 8）。

依赖：

| 依赖 | 说明 |
|------|------|
| `microsphere-spring-context` | 必需（提供 `AnnotatedInjectionBeanPostProcessor`） |
| `spring-context` | `optional` |
| `guice` | `optional` —— **不会传递给你**，需自行声明 |

## 1. 支持范围：只桥接 `@Inject`

本模块只有 3 个类，职责非常有限：

| 类 | 可见性 | 作用 |
|----|--------|------|
| `guice.annotation.EnableGuice` | public | 入口注解，`@Import(GuiceConfiguration.class)`，**无属性** |
| `guice.annotation.GuiceConfiguration` | **包私有** | 仅 `@Import(GuiceInjectAnnotationBeanPostProcessor.class)` |
| `guice.annotation.GuiceInjectAnnotationBeanPostProcessor` | **包私有** | 继承 `AnnotatedInjectionBeanPostProcessor`，注解类型固定为 `com.google.inject.Inject` |

它**不做**的事（务必确认后再引入）：

- 不创建、不暴露 Guice `Injector`；
- 不读取 `com.google.inject.Module`，也不把 Spring Bean 绑定进 Guice；
- 不支持 Guice 的 `@Provides`、`Provider<T>` 语义、`Scope`、`multibinding`、AOP；
- 不支持 `javax.inject.Inject`（那是 Spring 自己早已处理的标准注解）；
- 不支持 Guice 的构造器注入解析（注入仍由 Spring 的 Bean 生命周期完成）。

因此它的定位是：**让已经写了 Guice `@Inject` 的存量代码，在不改注解的前提下由 Spring 完成注入**——一个迁移辅助桥，而不是 Guice 容器集成。

## 2. `required` 语义映射

Guice 的 `@Inject` 用 `optional` 表达"可缺失"，Spring 用 `required`。`determineRequiredStatus` 完成转换：

```java
private static final String OPTIONAL_ATTRIBUTE_NAME = "optional";

@Override
protected boolean determineRequiredStatus(AnnotationAttributes attributes) {
    if (!attributes.containsKey(OPTIONAL_ATTRIBUTE_NAME)) {
        return false;                        // 无该属性 → 非必需
    }
    return Boolean.FALSE.equals(attributes.getBoolean(OPTIONAL_ATTRIBUTE_NAME));
}
```

对照表：

| 写法 | 解析出的 `required` | 找不到匹配 Bean 时 |
|------|---------------------|--------------------|
| `@Inject private Foo foo;`（无 `optional` 元素） | `false` | **注入 `null`，不报错** |
| `@Inject(optional = true) private Foo foo;` | `false` | 注入 `null` |
| `@Inject(optional = false) private Foo foo;` | ⚠ 同样是 `false` | 注入 `null`，不抛异常 |

> ⚠ **在本模块下 Guice 的 `@Inject` 永远是可选的，且无法关闭。** processor 构造时调用了 `setIgnoreDefaultValue(true)`
> （`microsphere-spring-context/.../AnnotatedInjectionBeanPostProcessor.java`，构造函数），会移除所有取值等于注解默认值的属性。
> Guice 声明 `boolean optional() default false`，所以 `optional = false` 在进入
> `GuiceInjectAnnotationBeanPostProcessor.determineRequiredStatus` 前就被丢弃，走 `!containsKey("optional")` 分支返回 `false`；
> `optional = true` 也因覆写实现返回 `Boolean.FALSE.equals(value)` 而得到 `false`。再看第一行：与 Spring
> `@Autowired(required = true)` 的默认相反，**裸 `@Inject` 在这里是可选的**。若需要「缺失即失败」，请改用 Spring 的
> `@Autowired`（默认必需）或自行校验，而不是写 `optional = false`。

## 3. 用法

```java
@Configuration
@EnableGuice
public class IntegrationConfig {
}
```

```java
@Service
public class OrderService {

    @Inject                                   // 由 Spring 满足
    private OrderRepository repository;

    @Inject(optional = true)                  // 缺失则为 null
    private AuditClient auditClient;

    @Inject(optional = false)                 // ⚠ 与上面的裸写法完全等价，依旧是可选
    private PaymentGateway paymentGateway;
}
```

1.x 自带测试即最小组合（JUnit 4 + `SpringRunner`）：

```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EnableGuiceTest.class})
@EnableGuice
public class EnableGuiceTest {

    @Inject
    private ApplicationContext context;                 // 普通 Spring Bean 可满足

    @Inject
    private GuiceConfiguration guiceConfiguration;      // @Import 进来的配置类也可注入

    @Inject(optional = true)
    private GuiceInjectAnnotationBeanPostProcessor postProcessor;

    @Inject(optional = true)
    private String notExists;                           // 断言为 null

    @Test
    public void testInject() {
        assertNotNull(context);
        assertNull(notExists);
    }
}
```

## 4. 接线关系

```
@EnableGuice
   └── @Import GuiceConfiguration            （包私有）
          └── @Import GuiceInjectAnnotationBeanPostProcessor   （包私有）
                 └── extends AnnotatedInjectionBeanPostProcessor
                        （microsphere-spring-context，注解类型 = com.google.inject.Inject）
```

`AnnotatedInjectionBeanPostProcessor` 来自 [context.md](./context.md) 描述的注入基础设施：它扫描字段/方法上指定的注解，按类型+限定符从 Spring 容器解析依赖，并用 `determineRequiredStatus` 决定是否强制。

## 5. 迁移建议

如果你原本依赖 Guice 的以下能力，本模块**不能**替代：

| Guice 能力 | 迁移到 Spring 的对应做法 |
|------------|--------------------------|
| `Injector` / `Module` / `bind()` | `@Configuration` + `@Bean` |
| `@Provides` | `@Bean` 方法 |
| `Provider<T>`（延迟提供） | `ObjectProvider<T>`（Spring 4.3+） |
| 自定义 `Scope` | `@Scope` + `Scope` 实现 |
| `@Named` 限定 | `@Qualifier` |
| multibinding | `List<T>` / `Map<String, T>` 注入 |
| Guice AOP | Spring AOP |
