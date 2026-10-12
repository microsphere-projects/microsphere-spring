# microsphere-spring-jdbc · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/jdbc.md) · [Developer guide](../../developer-guide/current/jdbc.md) · [1.x docs](../1.x/jdbc.md)

`microsphere-spring-jdbc` wraps every `DataSource` bean with P6Spy's `P6DataSource` so SQL execution can be observed without touching application code. `@EnableP6DataSource` is the only switch; everything else is a bean or an environment property.

---

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-jdbc` |
| Branch | `release` (the 0.2.x line) |
| Version | `0.2.39` / development version `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework compatibility | **6.0.x – 7.0.x** (the matching `spring-jdbc` version must be provided by the host application) |
| Required dependency | `microsphere-spring-context` |
| Optional dependencies | `spring-context`, `spring-jdbc`, **`p6spy:p6spy` 3.9.1** |

**P6Spy must be added explicitly**: `p6spy` is `optional` in this module and nothing is pulled in transitively, so without it `@EnableP6DataSource` cannot work — there is no `P6DataSource` class on the classpath.

```xml
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

---

## 1. `@EnableP6DataSource`

`io.microsphere.spring.jdbc.p6spy.annotation.EnableP6DataSource`, `@Target(TYPE) @Retention(RUNTIME) @Documented`, takes effect through `@Import({P6DataSourceBeanDefinitionRegistrar, SpringProtocolURLStreamHandler, SpringP6SpyURLConnectionFactory})`. **The annotation declares no attributes at all** — every knob is a bean or a Spring environment property.

Enable it:

```java
@Configuration
@EnableP6DataSource
public class DataSourceConfig {
}
```

How it works:

1. `P6DataSourceBeanDefinitionRegistrar` (package-private, extending `AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableP6DataSource>`) initializes P6Spy's `P6ModuleManager.getInstance()`, appends `CompoundJdbcEventListenerFactory`, registers all `P6OptionChangedListener` beans, and registers `P6DataSourceBeanPostProcessor`.
2. `P6DataSourceBeanPostProcessor` (`io.microsphere.spring.jdbc.p6spy.beans.factory.config`, extending `GenericBeanPostProcessorAdapter<DataSource>`, implementing `EnvironmentAware`) replaces **every `DataSource` bean** with `new P6DataSource(bean.unwrap(DataSource.class))` in `postProcessAfterInitialization`; if unwrapping fails or the bean is on the exclusion list, it is left untouched.
3. `SpringProtocolURLStreamHandler` + `SpringP6SpyURLConnectionFactory` register the `p6spy` sub-protocol, rewriting `p6spy://<authority>` to `spring:env:property-sources://<authority>` (default authority `microsphere.jdbc.p6spy`) so P6Spy can read its options from Spring property sources.

---

## 2. Configuration properties

| Property | Type | Default | Description |
|---|---|---|---|
| `microsphere.jdbc.p6spy.excluded-datasource-beans` | `Set<String>` | empty | DataSource bean names that must not be wrapped (constant `P6DataSourceBeanPostProcessor.EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME`) |
| `microsphere.jdbc.p6spy.options` | prefix | — | Property-based entry point for P6Spy options, read by `PropertySourcesP6LoadableOptionsAdapter`; **that class is not auto-wired**, you must register it yourself |

Exclude the datasources used by migration tooling:

```properties
microsphere.jdbc.p6spy.excluded-datasource-beans=flywayDataSource,liquibaseDataSource
```

The module also contains `NoOpP6LoadableOptions` (which neutralizes P6Spy's own `p6spy.properties` loading). **This repository ships no `p6spy.properties` at all** — neither in main nor in test resources — so P6Spy's own defaults apply unless you add one to your classpath.

---

## 3. SQL tracing: registering a `JdbcEventListener`

`CompoundJdbcEventListenerFactory` (a P6Spy `P6Factory`) returns, from its `getJdbcEventListener()`, all `JdbcEventListener` beans aggregated and sorted by `@Order` (its `getOptions()` returns `NoOpP6LoadableOptions`). **The correct way to trace SQL is therefore to register `JdbcEventListener` beans**, not to edit a configuration file.

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

Because `CompoundJdbcEventListenerFactory` composes all `JdbcEventListener` beans in `@Order` sequence, tracing SQL means publishing listener beans. The example above extends P6Spy's `LoggingEventListener` (signature verified against `p6spy-3.9.1`: `onAfterAnyExecute(StatementInformation, long, SQLException)`) to flag slow statements with bound-parameter SQL via `getSqlWithValues()`.

You can also implement `com.p6spy.engine.event.JdbcEventListener` directly to take over hooks such as `onBeforeGetConnection`, `onBeforeAnyExecute` or `onAfterResultSetGet`; `P6OptionChangedListener` beans are registered by the registrar as well.

---

## 4. Verifying the wrapper

```java
@Autowired
@Qualifier("testDataSource")
private DataSource dataSource;      // the runtime type is P6DataSource

void check() throws SQLException {
    DataSource raw = dataSource.unwrap(DataSource.class);   // retrieve the wrapped original DataSource
}
```

Real usage from the module's own tests (`EnableP6DataSourceTest`, which also asserts `assertSame(dataSource.unwrap(DataSource.class), ci.getDataSource())`):

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EnableP6DataSourceTest.class})
@EnableP6DataSource
@EnableEmbeddedDatabase(dataSource = "testDataSource")   // from microsphere-spring-test
class EnableP6DataSourceTest extends LoggingEventListener {
    @Autowired @Qualifier("testDataSource") private DataSource dataSource;
}
```

The test that proves exclusion works (`P6DataSourceBeanPostProcessorTest`): with `@TestPropertySource(properties = "microsphere.jdbc.p6spy.excluded-datasource-beans=nonWrappedDataSource")` that bean stays a plain `DriverManagerDataSource` while the others become `P6DataSource`.

---

## 5. Class inventory

| Class | Visibility | Description |
|---|---|---|
| `p6spy.annotation.EnableP6DataSource` | public | Enabling annotation (no attributes) |
| `p6spy.annotation.P6DataSourceBeanDefinitionRegistrar` | package-private | Registers the registrar logic and factories |
| `p6spy.beans.factory.config.P6DataSourceBeanPostProcessor` | public | Wraps `DataSource` beans |
| `p6spy.beans.factory.CompoundJdbcEventListenerFactory` | public | Composes `JdbcEventListener` beans |
| `p6spy.net.SpringP6SpyURLConnectionFactory` | public | Connection factory for the `p6spy` sub-protocol |
| `p6spy.NoOpP6LoadableOptions` | public | No-op options implementation |
| `p6spy.PropertySourcesP6LoadableOptionsAdapter` | public | Reads P6Spy options from Spring property sources (manual registration) |
