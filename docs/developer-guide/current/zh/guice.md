# microsphere-spring-guice — 开发者指南

[← 开发者文档总览](./README.md) · [English](../guice.md) · [用户文档](../../../user-guide/current/zh/guice.md) · [1.x](../1.x/zh/guice.md)

## 1. 模块标识、版本基线与依赖姿态

| 属性 | 值 | 证据 |
|---|---|---|
| Maven 坐标 | `io.github.microsphere-projects:microsphere-spring-guice:${revision}` | `microsphere-spring-guice/pom.xml:13-16` |
| 本文分析的聚合版本 | `0.2.40-SNAPSHOT`（`revision` 属性） | `pom.xml:55` |
| 工作树中最后一次构建产物 | `0.2.39` jar 与 sources jar | `microsphere-spring-guice/target/`、`microsphere-spring-guice/.flattened-pom.xml:13` |
| 父 POM | `microsphere-spring-parent:${revision}` | `microsphere-spring-guice/pom.xml:5-10` |
| Java 基线 | `17`（`java.version`） | `pom.xml:56` |
| Spring 基线 | 按 profile 选择 `6.0.23` / `6.1.21` / `6.2.18` / `7.0.9`；默认激活 `spring-framework-7.0` | `microsphere-spring-parent/pom.xml:138-173`，BOM 导入 `:99-106` |
| Jakarta EE | 父 POM 导入 `jakarta.jakartaee-bom` `11.0.0`，但本模块**未**声明任何 `jakarta.*` 构件 | `microsphere-spring-parent/pom.xml:24,51-58` |
| Guice | `com.google.inject:guice` `7.0.0`，**optional**，版本在本模块 POM 内钉死 | `microsphere-spring-guice/pom.xml:21-23,48-54` |
| `microsphere-spring-context` | compile 且**非** optional —— 聚合内唯一的强制依赖边 | `microsphere-spring-guice/pom.xml:41-46` |
| `org.springframework:spring-context` | optional | `microsphere-spring-guice/pom.xml:34-39` |
| `microsphere-annotation-processor` | optional，无版本声明（由 `microsphere-java-dependencies` 提供 `0.3.19`） | `microsphere-spring-guice/pom.xml:27-32`、`microsphere-spring-parent/pom.xml:22,117-124` |
| 测试依赖 | `junit-jupiter`、`spring-test`、`logback-classic` —— 本模块**没有** `microsphere-spring-test` 这条边 | `microsphere-spring-guice/pom.xml:56-73` |
| 包名根 | `io.microsphere.spring.guice.annotation` | `microsphere-spring-guice/src/main/java` |
| 主/测试类型数 | 3 个主类、1 个测试类 | `microsphere-spring-guice/src/**` |
| `src/main/resources` | **不存在** —— 无 `spring.factories`、无 `AutoConfiguration.imports`、无 `module-info.java` | `microsphere-spring-guice/src/main/` 目录清单 |

依赖姿态（这是该模块最常见的破坏点，因此精确表述）：**Guice 是 `optional`**（`microsphere-spring-guice/pom.xml:53`），所以 `com.google.inject:guice` **不会**传递给消费者；已发布的 POM 保留了该标记（`.flattened-pom.xml:66-72`），消费者必须自行声明 Guice。`spring-context` 与注解处理器同样是 optional（`:28-39`）；聚合内唯一强制边是 `microsphere-spring-context`（`:41-46`），它自身又以强制方式带入 `microsphere-java-core`（`microsphere-spring-context/pom.xml:30-34`）。

⚠ Guice 并未在父 POM 或 BOM 中被管理：`microsphere-spring-parent/pom.xml:32-136` 没有 `com.google.inject` 条目，`microsphere-spring-dependencies/pom.xml:30-34` 只管理本模块自身的构件。`docs/developer-guide/current/zh/README.md:21` 把 Guice 列入"受管第三方版本"；[parent.md](../parent.md) 第 140-141 行的说法才与源码一致 —— 本模块是把版本钉在本地的那个例外。

```mermaid
flowchart LR
    subgraph IN["入边（谁依赖 microsphere-spring-guice）"]
        BOM["microsphere-spring-dependencies —— 仅 BOM 版本管理"]
    end
    GU["microsphere-spring-guice"] --> BOM
    subgraph OUT["出边 —— microsphere-spring-guice/pom.xml"]
        GU -->|compile，强制| CTX["microsphere-spring-context"]
        CTX -->|compile，强制| JCO["microsphere-java-core"]
        GU -.optional.-> SC["org.springframework:spring-context"]
        GU -.optional.-> GUI["com.google.inject:guice 7.0.0"]
        GU -.optional.-> AP["microsphere-annotation-processor"]
        GU -.test.-> JJ["org.junit.jupiter:junit-jupiter"]
        GU -.test.-> ST["org.springframework:spring-test"]
        GU -.test.-> LB["ch.qos.logback:logback-classic"]
    end
    APP["消费者应用"] -.->|"必须自行声明 guice，否则桥接类无法链接"| GUI
```

聚合内没有任何模块依赖 `microsphere-spring-guice`；模块之外的源码级引用只有 `pom.xml:67`（module 列表）与 `microsphere-spring-dependencies/pom.xml:32`（BOM）。

## 2. 包结构地图

| 包 | 类型（可见性） | 职责 |
|---|---|---|
| `io.microsphere.spring.guice.annotation` | `EnableGuice`（public）、`GuiceConfiguration`（包级私有，`GuiceConfiguration.java:30`）、`GuiceInjectAnnotationBeanPostProcessor`（包级私有，`GuiceInjectAnnotationBeanPostProcessor.java:31`） | 启用注解 + 本模块绑定的注解类型 + required 判定覆写 |

本模块构建所依赖的外部类型：

| 类型 | 所属 | 本模块为何需要 |
|---|---|---|
| `io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionBeanPostProcessor` | **`microsphere-spring-context`** | 父类（`GuiceInjectAnnotationBeanPostProcessor.java:31`）；扫描、解析、缓存全部由它承载 |
| `io.microsphere.spring.core.annotation.AnnotationUtils` | `microsphere-spring-context` | 父类提取注解属性时使用（`AnnotatedInjectionBeanPostProcessor.java:515-517` → `AnnotationUtils.java:463-485`） |
| `AnnotatedInjectionBeanPostProcessor.AnnotationInjectedElement` | `microsphere-spring-context` | public 的注入点模型（`:739-762`），供解析钩子使用 |
| `org.springframework.context.annotation.Import` | Spring Framework | 唯一的激活机制（`EnableGuice.java:38-40`、`GuiceConfiguration.java:29`） |
| `com.google.inject.Inject` | Guice 7.0.0 | 桥接所绑定的注解类型（`GuiceInjectAnnotationBeanPostProcessor.java:19,33`） |
| `SmartInstantiationAwareBeanPostProcessor`、`MergedBeanDefinitionPostProcessor`、`PriorityOrdered`、`BeanFactoryAware`、`BeanClassLoaderAware`、`EnvironmentAware`、`InitializingBean`、`DisposableBean` | Spring Framework | 父类实现的 Spring 缝隙（`AnnotatedInjectionBeanPostProcessor.java:160-162`） |

## 3. 架构

⚠ 本模块**没有 `ImportBeanDefinitionRegistrar`、没有 `ApplicationContextInitializer`、没有 `Injector`、没有 `Module`、也没有 `Provider`**。`docs/developer-guide/current/zh/README.md:83` 把本页概括为"Spring ↔ Guice 注入桥接"，`:92-103` 又描述了聚合统一的"注解 → 注册器 → 缝隙"形态；`microsphere-spring-guice` 两者都不严格符合 —— 方向是单向的（*Spring* bean 上被 Guice 注解标注的成员由 *Spring* 满足），激活是两跳 `@Import`，中间不存在注册器。以下即证据。

```mermaid
flowchart TD
    CFG["@Configuration 类上标注 '@EnableGuice'"] --> META["从注解类型解析出元注解 '@Import' —— EnableGuice.java:38-40"]
    META --> GC["GuiceConfiguration —— 包级私有、无 '@Configuration'，'lite' 模式 —— GuiceConfiguration.java:29-30"]
    GC --> IMP["第二跳 '@Import'"]
    IMP --> PP["GuiceInjectAnnotationBeanPostProcessor bean —— GuiceInjectAnnotationBeanPostProcessor.java:31-39"]
    PP --> BASE["AnnotatedInjectionBeanPostProcessor —— microsphere-spring-context:160-162"]
    BASE --> SCAN["逐 bean 类型：doWithFields :430 + doWithMethods :469 + determineCandidateConstructors :278，按 com.google.inject.Inject 过滤"]
    SCAN --> DD["DependencyDescriptor（字段或方法参数）+ required 标记 :799 :890"]
    DD --> BF["ConfigurableListableBeanFactory.resolveDependency :671-680"]
    BF --> WRITE["setFieldValue :791 / invokeMethod :862"]
    G["com.google.inject:guice —— optional，仅触及它的注解类型"] -.-> SCAN
```

`@EnableGuice`（`EnableGuice.java:41`）没有声明任何属性，因此既无可配置项，也无 Environment 级开关。中间类 `GuiceConfiguration` 未标注 `@Configuration`，也没有 `@Bean` 方法（`GuiceConfiguration.java:30`）；它只是承载第二个 `@Import` 的容器，副作用是两个被导入类都成为普通 bean 定义 —— 模块自带的测试通过注入它们证明了这一点（`EnableGuiceTest.java:45-46,48-49`，在 `:57-58` 断言非空）。

## 4. 源码分析

### 4.1 桥接类

```java
class GuiceInjectAnnotationBeanPostProcessor extends AnnotatedInjectionBeanPostProcessor {  // :31
    private static final Class<? extends Annotation> ANNOTATION_TYPE = Inject.class;        // :33
    private static final String OPTIONAL_ATTRIBUTE_NAME = "optional";                       // :35
    public GuiceInjectAnnotationBeanPostProcessor() { super(ANNOTATION_TYPE); }             // :37-39
    @Override
    protected boolean determineRequiredStatus(AnnotationAttributes attributes) {            // :41-47
        if (!attributes.containsKey(OPTIONAL_ATTRIBUTE_NAME)) {
            return false;
        }
        return Boolean.FALSE.equals(attributes.getBoolean(OPTIONAL_ATTRIBUTE_NAME));
    }
}
```

整个模块就是一个常量（`Inject.class`）加一个钩子覆写。把注解类型固化在 `static final` 字段里，也正是缺失 Guice 时失败发生在链接期的原因：初始化该类需要 `com.google.inject.Inject`（`:19,33`），因此在没有该 optional 依赖时，创建这个后处理器 bean 会抛 `NoClassDefFoundError`。

### 4.2 继承而来的机制及其不可变点

父类是一台自包含的注入引擎：`postProcessProperties`、`postProcessPropertyValues`、`postProcessMergedBeanDefinition`、`determineCandidateConstructors`、`setBeanFactory`、`setEnvironment`、`setBeanClassLoader`、`getOrder`、`getAnnotationTypes` 全部是 `final`（`AnnotatedInjectionBeanPostProcessor.java:278,387,408,553,561,566,571,713,717`），子类无法改变它*何时*、*被执行几次*。可覆写面恰好是三个 protected 钩子：`determineRequiredStatus`（`:455-457`，默认返回 `true`）、`resolveInjectedFieldValue`（`:651-654`，返回 `null`）、`resolveInjectedMethodArguments`（`:666-669`，返回 `null`）。Guice 子类只覆写其中之一。

父类构造器设定了两个决定下文语义的默认值：`setOrder(LOWEST_PRECEDENCE - 3)`（`:246`，context 模块测试 `AnnotatedInjectionBeanPostProcessorTest.java:96` 对其做了断言）以及 `setIgnoreDefaultValue(true)` + `setTryMergedAnnotation(true)`（`:249-250`）。⚠ `:201-203` 的字段注释声称该 order 是为了"比 `AutowiredAnnotationBeanPostProcessor` 优先级更高"，但 `LOWEST_PRECEDENCE - 3` 位于 `Ordered` 刻度的最末端；仅凭这个数值并不能确立与 Spring 自身注解处理器的相对顺序。

### 4.3 必需 vs 可选 —— 精确的解析过程

`determineRequiredStatus` 只读 `optional` 键，但这个键*是否存在*由上游决定。完整链路如下：

1. `doGetAnnotationAttributes(member, Inject.class)` 传入 `ignoreDefaultValue = true` 与 `tryMergedAnnotation = true`（`AnnotatedInjectionBeanPostProcessor.java:515-517`）。
2. `AnnotationUtils.getAnnotationAttributes(...)` 的两个分支（`AnnotationUtils.java:463-485`：合并注解分支 `:474-477`、普通分支 `:479-482`）都会汇入 `filterAnnotationAttributes`（`:643-671`）。
3. `filterAnnotationAttributes` 会**移除**取值等于注解声明默认值的属性：`if (ignoreDefaultValue && nullSafeEquals(attributeValue, getDefaultValue(annotation, attributeName))) continue;`（`AnnotationUtils.java:657-659`），其中 `getDefaultValue` 来自 Spring，在 `AnnotationUtils.java:34` 静态导入。
4. Guice 声明的是 `boolean optional() default false`。Java 注解代理对未书写的元素返回默认值，所以合成出的属性映射在"裸写"与"显式写 `false`"两种情况下都含 `optional=false` —— 然后被第 3 步删除。

因此结论是算术推导而非观点：`containsKey("optional")` 为 `true` 的唯一可能是取值不同于 `false`、即为 `true`，此时 `Boolean.FALSE.equals(true)` 等于 `false`。**经由 `@EnableGuice`，`determineRequiredStatus` 永远不可能返回 `true`**——该线上所有 Guice 注入点都是非必需的。

| Guice 声明 | `optional` 取值 | 是否通过默认值过滤 | `determineRequiredStatus` | 运行时效果（有据可查） |
|---|---|---|---|---|
| `@Inject` | `false`（默认值） | 否 | `false`（`GuiceInjectAnnotationBeanPostProcessor.java:43-45`） | 找不到候选 → 字段被写入 `null`，不抛异常（`AnnotatedFieldElement.resolveFieldValue:795-806`） |
| `@Inject(optional = false)` | `false`（默认值） | **否 —— 与上一行完全相同** | `false` | ⚠ `docs/user-guide/current/zh/guice.md:42` 与 `docs/user-guide/1.x/zh/guice.md:71` 记为 `required = true`，但源码无法到达该结果 |
| `@Inject(optional = true)` | `true` | 是 | `false`（`:46`） | 找不到候选 → `null`；由 `EnableGuiceTest.java:51-52,59` 断言 |

`required` 只会作为 `DependencyDescriptor` 的第二个参数传给 Spring（字段路径 `:799`、方法参数路径 `:890`、构造器路径 `:323`）。它容忍"缺失"，但**不容忍"歧义"**：`resolveDependency` 捕获所有 `BeansException` 并重新抛出 `UnsatisfiedDependencyException`（`:671-680`），所以两个候选 bean 一样会让 `@Inject(optional = true)` 字段失败。唯一能恢复 Guice 语义的开关是 `setIgnoreDefaultValue(false)`（`:606-608`），但通过 `@EnableGuice` 无法触达：子类从未调用它，且子类本身是包级私有（`:31`），用户既不能继承也不能配置它。

### 4.4 注入点边界情况（全部继承而来）

- **静态字段**被丢弃并打 `warn`（`:434-439`）；Guice 本身禁止静态注入，所以遗留的静态 `@Inject` 在这里静默无效。
- **零参数方法**只是 `warn`（`:483-487`）却仍被登记（`:490`）；注入时 `resolveMethodArguments` 得到空参数数组（`:884-886`），随后方法被以无参方式调用（`:861-863`）—— 桥接把它们变成了初始化式回调。
- **覆写方法与桥接方法**会去重：先解析桥接方法对（`:470-473`），再只保留最具体声明（`:476`）。
- **构造器**：带 `@Inject` 的构造器会成为候选（`:304`），但由于 `required` 恒为 `false`，`requiredConstructor` 分支永不触发（`:316-333`）；若类同时声明了无参构造器，后者会被追加为回退（`:338-351`）；而"只有一个带参构造器且未加注解"的情形仍会被选中（`:353-354`）。
- **显式属性值优先于 setter**：`checkPropertySkipping(pvs)` 提前返回（`:846-848`），因此由 XML 或 `PropertyValues` 显式赋值的 `@Inject` setter 会被跳过。字段没有这个保护 —— `inject` 永远写入，包括 `null`（`:778-792`）。

## 5. 一次注入请求的完整路径

```mermaid
sequenceDiagram
    participant BF as DefaultListableBeanFactory
    participant PP as Guice 桥接层
    participant BASE as AnnotatedInjectionBeanPostProcessor
    participant AU as AnnotationUtils
    BF->>BASE: postProcessMergedBeanDefinition beanType beanName :553
    BASE->>AU: doGetAnnotationAttributes 字段 com.google.inject.Inject env ignoreDefaultValue=true :515
    AU-->>BASE: AnnotationAttributes，optional 等于默认值时为空 :657
    BASE->>PP: determineRequiredStatus attributes :440
    PP-->>BASE: false，恒成立 :43-46
    BF->>BASE: postProcessProperties，populateBean 阶段 :408
    BASE->>BASE: AnnotatedFieldElement.inject :778
    BASE->>BASE: resolveInjectedFieldValue 钩子，默认返回 null :651 :796
    BASE->>BF: resolveDependency DependencyDescriptor(字段, false) :671
    alt 找到 bean
        BF-->>BASE: 实例
        BASE->>BASE: 缓存为 ShortcutDependencyDescriptor :819 然后 setFieldValue :791
    else 无候选
        BF-->>BASE: null（因为 required 为 false）
        BASE->>BASE: setFieldValue 写入 null，覆盖字段初始值 :791
    else 多个候选
        BF-->>BASE: BeansException
        BASE->>BASE: 包装为 UnsatisfiedDependencyException :676-678，optional 并不宽容
    end
```

这三个分支就是该模块契约的全部：*命中 → 注入，缺失 → `null`，歧义 → 失败*。不存在第四步去咨询 Guice；你在自己代码里创建的 `Injector` 永远不会被触达。

## 6. 设计模式

| 模式 | 类 | 证据 | 说明 |
|---|---|---|---|
| 配置启用（Enable*） | `EnableGuice` | `EnableGuice.java:35-41` | 零属性标记，载荷是元注解 `@Import` |
| 两跳 Import 间接层 | `GuiceConfiguration` | `GuiceConfiguration.java:29-30` | 包级私有的 "lite" 类承载第二跳 `@Import`，自身也可被注入（`EnableGuiceTest.java:45-46`） |
| 模板方法 | `AnnotatedInjectionBeanPostProcessor` + `GuiceInjectAnnotationBeanPostProcessor` | 固定步骤 `:278,408,553`，钩子 `:455,651,666`；覆写位于 `GuiceInjectAnnotationBeanPostProcessor.java:41-47` | 算法冻结，只有必需性判定可变 |
| 桥接 / 适配器 | `GuiceInjectAnnotationBeanPostProcessor` | `:33,35` 把 `com.google.inject.Inject.optional` 映射为 Spring 的 `required` | 模块描述本身就是 "Microsphere Spring Bridge Guice"（`microsphere-spring-guice/pom.xml:19`） |
| 工厂（实例化感知） | `determineCandidateConstructors`、`ShortcutDependencyDescriptor.resolveShortcut` | `AnnotatedInjectionBeanPostProcessor.java:277-368`、`:933-949` | 挑选候选构造器；快捷描述符复用已绑定的 bean 名 |
| 注册表 / 缓存 | `candidateConstructorsCache`、`injectionMetadataCache` | `:191-193`、`:627-630`，写入点 `:363,542` | 以 bean 类型 / bean 名为键；仅在 `destroy()` 中清空（`:633-639`） |
| 空对象 | `resolveInjectedFieldValue`、`resolveInjectedMethodArguments` | `:651-654`、`:666-669` | 返回 `null` 即"我没有自定义值，交给 Spring" |
| 策略 | `annotationTypes` 集合 + `order` | `:189,236-238,713-715`、`:580,717-720` | 注解类型即策略参数，此处固定为单值 |

无论本模块还是继承链上都没有出现观察者、装饰器或组合模式 —— 与 `microsphere-spring-jdbc` 不同，这里既不包装对象也不做事件扇出。

## 7. 扩展指南

由于内置的后处理器是包级私有，扩展必须针对 `microsphere-spring-context` 中的 **public** 父类进行。

### 7.1 用真实 `Injector` 为 Guice 注入点供值

```java
public class GuiceBackedInjectBeanPostProcessor extends AnnotatedInjectionBeanPostProcessor {

    private final Injector injector;   // 由应用创建，例如 Guice.createInjector(new AppModule())

    public GuiceBackedInjectBeanPostProcessor(Injector injector) {
        super(com.google.inject.Inject.class);      // 也可用可变参数追加 jakarta/javax 的 Inject（:236）
        this.injector = injector;
        setIgnoreDefaultValue(false);               // :606-608 —— 否则 optional=false 会被过滤掉
    }

    @Override
    protected boolean determineRequiredStatus(AnnotationAttributes attributes) {   // 等价于 :41-47，但此时可达
        return !Boolean.TRUE.equals(attributes.getBoolean("optional"));
    }

    @Override
    protected Object resolveInjectedFieldValue(Object bean, String beanName, PropertyValues pvs,
                                               AnnotationInjectedElement<Field> fieldElement) throws Throwable {
        return injector.getInstance(fieldElement.getInjectionPoint().getType());   // 在 Spring 之前被咨询（:796）
    }
}
```

注册方式是 `@Bean`（返回 `BeanPostProcessor` 的 `@Bean` 方法应声明为 `static`，以免出现基础设施过早初始化告警），或用 `@Import` 导入声明它的类。钩子返回非 `null` 值会**完全短路** Spring 查找。

### 7.2 决定"谁获胜"的规则

| 规则 | 证据 |
|---|---|
| 解析钩子先于 bean 工厂执行：字段路径 `:796-804`、方法路径 `:881-893`；返回 `null` 才表示"交给 Spring" | `AnnotatedInjectionBeanPostProcessor.java:796,881` |
| 钩子返回值**不做**类型转换 —— `setFieldValue` / `invokeMethod` 收到的是原始对象；只有 Spring 路径会传入 `TypeConverter` | `:791,862` 对比 `:672-675` |
| 每个已注册的处理器都会对每个 bean 独立跑一遍；若两个处理器认领同一字段，后执行者写入最终值 | `:778-792`（无条件写入） |
| 相对顺序由 `setOrder` 控制，继承来的默认值是 `LOWEST_PRECEDENCE - 3` | `:580-582,246` |
| 缓存按处理器实例隔离，因此自定义处理器会重新扫描 Guice 处理器已扫描过的类 | `:191-193,627-630` |
| 想既保留 `@EnableGuice` 又增加绑定来源，就把你的处理器**额外**注册进去；无法向 `@EnableGuice` 传入模块 —— 它没有属性 | `EnableGuice.java:41` |
| "把 Spring bean 暴露给 Guice"不是本模块的扩展点：仓库里没有 `Binder`/`AbstractModule`。要么手写一个 provider 读取 `ApplicationContext` 的 Guice 模块，要么在 Spring 侧改用 `ObjectProvider` | `microsphere-spring-guice/src/main/java` 中不存在这些类型 |

## 8. 构建与验证

| 任务 | 命令 / 事实 |
|---|---|
| 构建并测试该模块 | `mvn -pl microsphere-spring-guice -am test --activate-profiles test,coverage,spring-framework-7.0` |
| 固定聚合版本 | CI 传入 `-Drevision=0.0.1-SNAPSHOT`（`.github/workflows/maven-build.yml:47`） |
| Spring 矩阵 | `spring-framework-6.0 / 6.1 / 6.2 / 7.0`（`microsphere-spring-parent/pom.xml:138-173`），`7.0` 为 `activeByDefault`（`:163-172`） |
| JDK 矩阵 | `17`、`21`、`25`（`.github/workflows/maven-build.yml:25`） |
| 构建父 POM | `microsphere-build:0.3.17`（`pom.xml:5-9`）—— ⚠ `docs/developer-guide/current/zh/README.md:19` 记为 release 分支上的 `0.3.16` |
| 测试清单 | `EnableGuiceTest` —— 测试类本身即被测 bean（`EnableGuiceTest.java:36-40`）；4 个注入点（`:42-52`）、4 个断言（`:54-60`） |
| 测试**未**覆盖 | 构造器与 setter 形式的 `@Inject`、必需性失败路径、任何 `Injector`/`Module`/`Provider`/`Scope` 的使用；Guice 依赖只以注解类型身份参与 |
| optional 依赖的影响 | 模块自身的编译类路径能看到 Guice（optional 只压制传递性），这就是单个测试无需额外声明即可编译通过的原因 |
| 发布的元数据 | `.flattened-pom.xml` 对 Guice 保留了 `<optional>true</optional>`（`:66-72`），因此消费者侧的自备要求在 flatten 之后依然存在 |

## 9. 注意事项与陷阱

| # | 观察结论 | 证据 |
|---|---|---|
| 1 | Guice 是 optional 且永不传递；消费者缺少 `com.google.inject:guice` 时，失败发生在桥接类初始化那一刻，而不是消费者代码编译期。 | `microsphere-spring-guice/pom.xml:48-54`；`GuiceInjectAnnotationBeanPostProcessor.java:19,33` |
| 2 | ⚠ 不存在 `Injector`、`Module`、`Provider`、`Scope` 集成，且方向只有"Guice 注解 → Spring"。`docs/developer-guide/current/zh/README.md:83` 的"Spring ↔ Guice 注入桥接"表述过强。 | `microsphere-spring-guice/src/main/java`（共 3 个类） |
| 3 | ⚠ `@Inject(optional = false)` 并**不**是必需的：默认值属性在进入 `determineRequiredStatus` 之前就已被过滤。`docs/user-guide/current/zh/guice.md:42` 与 `docs/user-guide/1.x/zh/guice.md:71` 声称其会抛异常。 | `AnnotatedInjectionBeanPostProcessor.java:249`；`AnnotationUtils.java:657-659` |
| 4 | 裸写 `@Inject` 在此是可选的，与 Guice 自身行为相反 —— 写错类型时静默得到 `null`，而非启动期报错。 | `GuiceInjectAnnotationBeanPostProcessor.java:43-45` |
| 5 | `optional = true` 不容忍重复候选，只容忍缺失。 | `AnnotatedInjectionBeanPostProcessor.java:671-680,798-802` |
| 6 | 字段写入是无条件的：字段本有初始值时，若无匹配 bean，`@Inject` 会用 `null` 抹掉它。 | `:778-792` |
| 7 | 两个具体类型都是包级私有；用户代码只能继承 public 父类，或在另一个后处理器里用 `instanceof AnnotatedInjectionBeanPostProcessor` 介入。 | `GuiceInjectAnnotationBeanPostProcessor.java:31`；`AnnotatedInjectionBeanPostProcessor.java:160` |
| 8 | 没有 Environment 级开关：与聚合内其它 `@Enable*` 入口不同，本注解导入的是普通类而非 `AnnotatedBeanCapableImportBeanDefinitionRegistrar`，因此 `microsphere.spring…enabled=false` 在这里无效。 | `EnableGuice.java:38-40` |
| 9 | `microsphere.spring.injection.metadata.cache.size` 只在类初始化时从**系统属性**读取（`-D…`），不来自 Spring `Environment`；而且两个缓存从不淘汰元素 —— 名字承诺了实现并不存在的上限。 | `AnnotatedInjectionBeanPostProcessor.java:167,179-185,363,542,633-639` |
| 10 | 零参数方法上的 `@Inject` 会被当作回调执行；静态字段上的则被忽略并打 `warn`。两者都与 Guice 编译期的校验不同。 | `:434-439,483-490,861-863` |
| 11 | 只绑定了 `com.google.inject.Inject`。使用 `jakarta.inject.Inject` 或 `javax.inject.Inject` 的类对本桥接不可见 —— 全聚合 main 源码中这两个名字仅出现在一处注释里（`AbstractInjectionPointDependencyResolver.java:394`）。 | `GuiceInjectAnnotationBeanPostProcessor.java:33` |
| 12 | Guice 7.0.0 需要 Java 11+，本线路的 Java 17 基线（`pom.xml:56`）满足它；Java 8 与 Guice 7 的冲突只存在于 1.x 线路（`docs/user-guide/1.x/zh/guice.md:13-19`）。 | `microsphere-spring-guice/pom.xml:21-23` |
| 13 | 桥接与 `@Autowired` 同在 `populateBean` 阶段执行，且处于 `PriorityOrdered` 区间的末端，因此同一字段上混用两种注解会得到两次独立解析。 | `AnnotatedInjectionBeanPostProcessor.java:246,408` |
| 14 | ⚠ 四个类型上的 `@since 1.0.0` 与被分析的 `0.2.40-SNAPSHOT` 不一致，不要用 `@since` 判断版本归属。 | `EnableGuice.java:33`、`pom.xml:55` |

---

**相关页面** — [开发者文档总览](./README.md) · [English 版](../guice.md) · [用户文档（Guice）](../../../user-guide/current/zh/guice.md) · [1.x 开发者文档](../1.x/zh/guice.md) · [父 POM profile](../parent.md) · [依赖管理](../dependencies.md)
