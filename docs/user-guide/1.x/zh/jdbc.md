# microsphere-spring-jdbc（1.x）

[← 1.x 版本总览](./README.md) · [English](../jdbc.md) · [开发者文档](../../../developer-guide/1.x/zh/jdbc.md) · [0.2.x 文档](../../current/zh/jdbc.md)

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-jdbc` |
| 分支 | `release-1.x` |
| 已发布版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | **4.3.x – 5.3.x** |
| P6Spy | `p6spy:p6spy` **3.9.1**（`optional`，需自行添加） |

依赖：

| 依赖 | 说明 |
|------|------|
| `microsphere-java-core` | 必需 |
| `microsphere-spring-context` | 必需（内部依赖，`SpringProtocolURLStreamHandler` 就在这个模块里） |
| `spring-context` / `spring-jdbc` | `optional` |
| `p6spy` | `optional` —— **本模块不会传递 P6Spy** |
| `microsphere-annotation-processor` | 编译期可选 |

功能定位：**透明地把容器里的 `DataSource` Bean 换成 P6Spy 包装**，从而获得 SQL 追踪、耗时统计与慢查询日志，无需修改业务代码或 JDBC URL（数据源层面代理，而不是驱动层面替换）。

> ⚠️ 必须先补依赖，否则 `@EnableP6DataSource` 会因找不到 P6Spy 类而失败：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-jdbc</artifactId>
</dependency>

<!-- 库把它声明为 optional，需显式引入 -->
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

---

## 1. `@EnableP6DataSource`

`io.microsphere.spring.jdbc.p6spy.annotation.EnableP6DataSource`——**没有任何属性**：

```java
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Import(value = {
        P6DataSourceBeanDefinitionRegistrar.class,
        SpringProtocolURLStreamHandler.class,
        SpringP6SpyURLConnectionFactory.class
})
public @interface EnableP6DataSource {
}
```

它导入三样东西：

| 导入项 | 所在模块 | 作用 |
|--------|----------|------|
| `P6DataSourceBeanDefinitionRegistrar` | jdbc | 初始化 P6Spy 的 `P6ModuleManager`，注册 `P6DataSourceBeanPostProcessor` |
| `SpringProtocolURLStreamHandler` | **context**（`io.microsphere.spring.net`） | 让 `spring:` 协议的 URL 可被打开（配置外置到 Spring 属性源） |
| `SpringP6SpyURLConnectionFactory` | jdbc | 把 `p6spy:` 子协议重定向到 `spring:env:property-sources://…` |

`P6DataSourceBeanDefinitionRegistrar` 做两件事（源码逐条核对）：

```java
initP6ModuleManager(getBeanFactory());                     // 1
registerBeanDefinition(registry, P6DataSourceBeanPostProcessor.class);  // 2
```

其中第 1 步展开为：

1. 取得 `P6ModuleManager.getInstance()`；
2. 向它的 `getFactories()` 列表**追加**一个 `CompoundJdbcEventListenerFactory(beanFactory)`；
3. 从容器里取出所有 `P6OptionChangedListener` Bean（按 `Ordered` 排序）并 `registerOptionChangedListener(...)`。

`CompoundJdbcEventListenerFactory` 实现 P6Spy 的 `P6Factory`：

```java
@Override
public P6LoadableOptions getOptions(P6OptionsRepository optionsRepository) {
    return new NoOpP6LoadableOptions();          // 注意：不是 PropertySources 版
}

@Override
public JdbcEventListener getJdbcEventListener() {
    List<JdbcEventListener> listeners = getSortedBeans(beanFactory, JdbcEventListener.class);
    return new CompoundJdbcEventListener(listeners);   // 容器里的监听器全部聚合
}
```

> ⚠️ 关于 `microsphere.jdbc.p6spy.options.*`：本模块确实带了 `PropertySourcesP6LoadableOptionsAdapter`，它把该前缀下的属性作为 P6Spy 的 defaults 暴露出来；但 `CompoundJdbcEventListenerFactory.getOptions(...)` 返回的是 `NoOpP6LoadableOptions`（`load` 空实现、`getDefaults` 返回 `emptyMap()`），**这个适配器没有被自动接线**。因此 P6Spy 自身配置仍走它自己的 `spy.properties`，别指望 `microsphere.jdbc.p6spy.options.appender=…` 生效。

## 2. `P6DataSourceBeanPostProcessor`：包装规则

```java
public class P6DataSourceBeanPostProcessor
        extends GenericBeanPostProcessorAdapter<DataSource> implements EnvironmentAware {

    public static final String EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME
            = "microsphere.jdbc.p6spy.excluded-datasource-beans";
}
```

对每个 `DataSource` Bean，在 `postProcessAfterInitialization` 阶段：

| 情况 | 结果 |
|------|------|
| Bean 名在 `microsphere.jdbc.p6spy.excluded-datasource-beans` 集合中 | **原样返回**，不包装 |
| `bean.unwrap(DataSource.class)` 成功 | 返回 `new P6DataSource(unwrapped)` |
| `unwrap` 抛 `SQLException` | trace 日志后**原样返回**（不抛异常、不中断启动） |

排除列表用集合绑定，逗号分隔即可：

```properties
# 跳过迁移工具/健康检查用的数据源
microsphere.jdbc.p6spy.excluded-datasource-beans=flywayDataSource,healthDataSource
```

> 该属性通过 `environment.getProperty(name, Set.class, emptySet())` 读取，因此只有能被转换成 `Set` 的形式（逗号分隔字符串、YAML 列表）才有效。

启用方式：

```java
@Configuration
@EnableP6DataSource
public class DataSourceConfig {

    @Bean
    public DataSource testDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.sqlite.JDBC")
                .url("jdbc:sqlite::memory:")
                .build();
    }
}
```

验证包装是否生效——从上下文取出的 Bean 应当能 `unwrap` 回原始数据源，而其本身是 `P6DataSource`：

```java
@Autowired
@Qualifier("testDataSource")
private DataSource dataSource;

assertThat(dataSource).isInstanceOf(P6DataSource.class);
assertThat(dataSource.unwrap(DataSource.class)).isNotSameAs(dataSource);
```

## 3. 编写 SQL 监听器

把 Spring Bean 声明为 P6Spy 的 `JdbcEventListener`（或继承 `com.p6spy.engine.logging.LoggingEventListener`），`CompoundJdbcEventListenerFactory` 会自动按 `Ordered` 收集并聚合。

1.x 自带测试就用了这个形态（节选）：

```java
@EnableP6DataSource
@EnableEmbeddedDatabase(dataSource = "testDataSource")
public class EnableP6DataSourceTest extends LoggingEventListener {

    @Autowired @Qualifier("testDataSource")
    private DataSource dataSource;

    @Override
    public void onBeforeGetConnection(ConnectionInformation connectionInformation) {
        // 证明 P6DataSource 内部持有的是原始 DataSource
        assertSame(dataSource.unwrap(DataSource.class), connectionInformation.getDataSource());
    }
}
```

慢查询告警示例（`LoggingEventListener` 的钩子，签名来自 P6Spy 3.9.1）：

```java
@Component
public class SlowSqlListener extends LoggingEventListener implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(SlowSqlListener.class);
    private static final long SLOW_MILLIS = 200;

    @Override
    public void onAfterAnyExecute(StatementInformation info, long elapsed, SQLException e) {
        if (elapsed > SLOW_MILLIS) {
            log.warn("slow sql {}ms :: {}", elapsed, info.getSqlWithValues());
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
```

常用钩子（都可在 `LoggingEventListener` 子类里覆写）：

| 方法 | 时机 |
|------|------|
| `onBeforeGetConnection(ConnectionInformation)` | 取连接前 |
| `onAfterGetConnection(ConnectionInformation, SQLException)` | 取连接后 |
| `onBeforeAnyExecute(StatementInformation)` | 任意语句执行前 |
| `onAfterAnyExecute(StatementInformation, long elapsed, SQLException)` | 任意语句执行后（含耗时毫秒） |
| `onCommit(ConnectionInformation, SQLException)` / `onRollback(...)` | 事务边界 |

`getSqlWithValues()` 返回已内联参数值的 SQL，便于直接复制到客户端复现；生产环境请注意它可能含敏感字面量。

## 4. 用 Spring 属性源提供 P6Spy 配置

`SpringP6SpyURLConnectionFactory` 把 `p6spy:` URL 重定向到属性源协议：

```java
@Override
protected String getSubProtocol() {
    return "p6spy";
}

@Override
public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
    String prefix = getPrefix(url);                       // authority，缺省为 "microsphere.jdbc.p6spy"
    URL targetURL = new URL("spring:env:property-sources://" + prefix);
    return targetURL.openConnection();
}
```

于是可以写：

```
p6spy:microsphere.jdbc.p6spy
```

或指定 authority：

```
p6spy:com.acme.spy
```

含义是：P6Spy 需要的配置项从环境里 `<authority>.` 前缀的属性读取，从而与 `application.properties` / 配置中心统一。

> 前提是 `spring:` 协议可用——`@EnableP6DataSource` 同时导入了 `SpringProtocolURLStreamHandler`（实际位于 `microsphere-spring-context`），它注册了 `spring:` 的 `URLStreamHandler`。

## 5. 类清单

| 类 | 说明 |
|----|------|
| `jdbc.p6spy.annotation.EnableP6DataSource` | 唯一入口注解，无属性 |
| `jdbc.p6spy.annotation.P6DataSourceBeanDefinitionRegistrar` | 初始化 `P6ModuleManager` 并注册 BPP（包私有类） |
| `jdbc.p6spy.beans.factory.config.P6DataSourceBeanPostProcessor` | `DataSource` → `P6DataSource` 包装器 |
| `jdbc.p6spy.beans.factory.CompoundJdbcEventListenerFactory` | `P6Factory`：聚合容器内 `JdbcEventListener` |
| `jdbc.p6spy.NoOpP6LoadableOptions` | 空 `P6LoadableOptions`（当前实际被使用的实现） |
| `jdbc.p6spy.PropertySourcesP6LoadableOptionsAdapter` | 读 `microsphere.jdbc.p6spy.options` 前缀，**未自动接线** |
| `jdbc.p6spy.net.SpringP6SpyURLConnectionFactory` | `p6spy:` → `spring:env:property-sources://` |
