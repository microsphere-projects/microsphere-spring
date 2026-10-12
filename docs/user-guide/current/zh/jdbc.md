# microsphere-spring-jdbc · 0.2.x

[← 当前版本总览](./README.md) · [English](../jdbc.md) · [开发者文档](../../../developer-guide/current/zh/jdbc.md) · [1.x 文档](../../1.x/zh/jdbc.md)

`microsphere-spring-jdbc` 把每个 `DataSource` Bean 包装为 P6Spy 的 `P6DataSource`，无需改动应用代码即可观测 SQL 执行。`@EnableP6DataSource` 是唯一开关，其余调优全部通过 Bean 与 Spring 环境属性完成。

---

## 版本信息

| 项目 | 值 |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-jdbc` |
| 分支 | `release`（0.2.x 线） |
| 版本 | `0.2.39` / 开发版 `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework 兼容范围 | **6.0.x – 7.0.x**（需 `spring-jdbc` 对应版本，由宿主应用提供） |
| 必需依赖 | `microsphere-spring-context` |
| 可选依赖 | `spring-context`、`spring-jdbc`、**`p6spy:p6spy` 3.9.1** |

**必须自行添加 P6Spy**：`p6spy` 在本模块是 optional，不会传递引入任何依赖；不引入则 `@EnableP6DataSource` 因 classpath 上缺少 `P6DataSource` 类而无法工作。

```xml
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

---

## 1. `@EnableP6DataSource`

`io.microsphere.spring.jdbc.p6spy.annotation.EnableP6DataSource`，`@Target(TYPE) @Retention(RUNTIME) @Documented`，通过 `@Import({P6DataSourceBeanDefinitionRegistrar, SpringProtocolURLStreamHandler, SpringP6SpyURLConnectionFactory})` 生效。**该注解没有任何属性**——一切调优都通过 Bean 与 Spring 环境属性完成。

启用：

```java
@Configuration
@EnableP6DataSource
public class DataSourceConfig {
}
```

工作机制：

1. `P6DataSourceBeanDefinitionRegistrar`（package-private，继承 `AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableP6DataSource>`）初始化 P6Spy 的 `P6ModuleManager.getInstance()`，追加 `CompoundJdbcEventListenerFactory`，注册全部 `P6OptionChangedListener` Bean，并注册 `P6DataSourceBeanPostProcessor`。
2. `P6DataSourceBeanPostProcessor`（`io.microsphere.spring.jdbc.p6spy.beans.factory.config`，继承 `GenericBeanPostProcessorAdapter<DataSource>`，实现 `EnvironmentAware`）在 `postProcessAfterInitialization` 阶段把**每个 `DataSource` Bean** 替换为 `new P6DataSource(bean.unwrap(DataSource.class))`；解包失败或命中排除名单则原样保留。
3. `SpringProtocolURLStreamHandler` + `SpringP6SpyURLConnectionFactory` 注册子协议 `p6spy`，把 `p6spy://<authority>` 重写为 `spring:env:property-sources://<authority>`（默认 authority `microsphere.jdbc.p6spy`），使 P6Spy 能从 Spring 属性源读取选项。

---

## 2. 配置属性

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `microsphere.jdbc.p6spy.excluded-datasource-beans` | `Set<String>` | 空 | 不包装的 DataSource Bean 名（常量 `P6DataSourceBeanPostProcessor.EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME`） |
| `microsphere.jdbc.p6spy.options` | 前缀 | — | P6Spy 选项的属性化入口，由 `PropertySourcesP6LoadableOptionsAdapter` 读取；**该类不自动装配**，需自行注册 |

排除迁移工具使用的数据源：

```properties
microsphere.jdbc.p6spy.excluded-datasource-beans=flywayDataSource,liquibaseDataSource
```

模块内另有 `NoOpP6LoadableOptions`（屏蔽 P6Spy 自身的 `p6spy.properties` 加载）；**本仓库未内置任何 `p6spy.properties`**（主/测试资源中都没有），因此默认沿用 P6Spy 自身缺省行为，除非你在 classpath 上自行添加一个。

---

## 3. SQL 追踪：注册 `JdbcEventListener`

`CompoundJdbcEventListenerFactory`（P6Spy `P6Factory`）的 `getJdbcEventListener()` 返回按 `@Order` 排序聚合的全部 `JdbcEventListener` Bean（`getOptions()` 返回 `NoOpP6LoadableOptions`）。因此**追踪 SQL 的正确做法是注册 `JdbcEventListener` Bean**，而不是配置文件。

```java
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.logging.LoggingEventListener;

@Component
public class SlowSqlListener extends LoggingEventListener {
    @Override
    public void onAfterAnyExecute(StatementInformation statementInformation,
                                  long timeElapsedNanos, SQLException e) {
        super.onAfterAnyExecute(statementInformation, timeElapsedNanos, e);
        long millis = timeElapsedNanos / 1_000_000;
        if (millis > 500) {
            log.warn("SLOW SQL ({} ms): {}", millis, statementInformation.getSqlWithValues());
        }
    }
}
```

由于 `CompoundJdbcEventListenerFactory` 会按 `@Order` 顺序组合全部 `JdbcEventListener` Bean，输出 SQL 日志就等于发布监听器 Bean。上例继承 P6Spy 的 `LoggingEventListener`（签名已对照 `p6spy-3.9.1` 校验：`onAfterAnyExecute(StatementInformation, long, SQLException)`），用 `getSqlWithValues()` 打印带绑定参数的 SQL 来标记慢语句。

也可实现 `com.p6spy.engine.event.JdbcEventListener` 自行接管 `onBeforeGetConnection`、`onBeforeAnyExecute`、`onAfterResultSetGet` 等钩子；`P6OptionChangedListener` Bean 会在注册器阶段一并注册。

---

## 4. 验证包装与解包

```java
@Autowired
@Qualifier("testDataSource")
private DataSource dataSource;      // 运行时实际类型为 P6DataSource

void check() throws SQLException {
    DataSource raw = dataSource.unwrap(DataSource.class);   // 取回被包装的原始 DataSource
}
```

模块测试中的真实用法（`EnableP6DataSourceTest`，同时验证 `assertSame(dataSource.unwrap(DataSource.class), ci.getDataSource())`）：

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EnableP6DataSourceTest.class})
@EnableP6DataSource
@EnableEmbeddedDatabase(dataSource = "testDataSource")   // 来自 microsphere-spring-test
class EnableP6DataSourceTest extends LoggingEventListener {
    @Autowired @Qualifier("testDataSource") private DataSource dataSource;
}
```

排除生效的测试断言（`P6DataSourceBeanPostProcessorTest`）：`@TestPropertySource(properties = "microsphere.jdbc.p6spy.excluded-datasource-beans=nonWrappedDataSource")` 时该 Bean 保持 `DriverManagerDataSource`，其余被包装为 `P6DataSource`。

---

## 5. 类清单

| 类 | 可见性 | 说明 |
|---|---|---|
| `p6spy.annotation.EnableP6DataSource` | public | 开关注解（无属性） |
| `p6spy.annotation.P6DataSourceBeanDefinitionRegistrar` | package-private | 注册装配逻辑与工厂 |
| `p6spy.beans.factory.config.P6DataSourceBeanPostProcessor` | public | 包装 `DataSource` Bean |
| `p6spy.beans.factory.CompoundJdbcEventListenerFactory` | public | 聚合 `JdbcEventListener` |
| `p6spy.net.SpringP6SpyURLConnectionFactory` | public | 子协议 `p6spy` 的连接工厂 |
| `p6spy.NoOpP6LoadableOptions` | public | 空选项实现 |
| `p6spy.PropertySourcesP6LoadableOptionsAdapter` | public | 从 Spring 属性源读 P6Spy 选项（需手动注册） |
