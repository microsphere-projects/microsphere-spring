# microsphere-spring-jdbc (1.x)

[← 1.x overview](./README.md) · [简体中文](./zh/jdbc.md) · [Developer guide](../../developer-guide/1.x/jdbc.md) · [0.2.x docs](../current/jdbc.md)

## Version information

| Item | Value |
|------|-----|
| artifactId | `microsphere-spring-jdbc` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version in development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility range | **4.3.x – 5.3.x** |
| P6Spy | `p6spy:p6spy` **3.9.1** (`optional`, must be added by yourself) |

Dependencies:

| Dependency | Notes |
|------|------|
| `microsphere-java-core` | required |
| `microsphere-spring-context` | required (internal dependency; `SpringProtocolURLStreamHandler` lives in that module) |
| `spring-context` / `spring-jdbc` | `optional` |
| `p6spy` | `optional` — **this module does not bring P6Spy in transitively** |
| `microsphere-annotation-processor` | optional at compile time |

What it does: **transparently replaces the container's `DataSource` beans with P6Spy wrappers**, which gives you SQL tracing, elapsed-time statistics and slow-query logging without touching business code or the JDBC URL (a datasource-level proxy rather than a driver-level swap).

> ⚠️ You must add the missing dependency first, otherwise `@EnableP6DataSource` fails because the P6Spy classes cannot be found:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-jdbc</artifactId>
</dependency>

<!-- the library declares it as optional, so add it explicitly -->
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

---

## 1. `@EnableP6DataSource`

`io.microsphere.spring.jdbc.p6spy.annotation.EnableP6DataSource` — **it has no attributes at all**:

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

It imports three things:

| Imported item | Module it lives in | Purpose |
|--------|----------|------|
| `P6DataSourceBeanDefinitionRegistrar` | jdbc | initializes P6Spy's `P6ModuleManager`, registers `P6DataSourceBeanPostProcessor` |
| `SpringProtocolURLStreamHandler` | **context** (`io.microsphere.spring.net`) | makes `spring:` protocol URLs openable (configuration externalized into Spring property sources) |
| `SpringP6SpyURLConnectionFactory` | jdbc | redirects the `p6spy:` sub-protocol to `spring:env:property-sources://…` |

`P6DataSourceBeanDefinitionRegistrar` does two things (checked against the source, item by item):

```java
initP6ModuleManager(getBeanFactory());                     // 1
registerBeanDefinition(registry, P6DataSourceBeanPostProcessor.class);  // 2
```

Step 1 expands into:

1. obtain `P6ModuleManager.getInstance()`;
2. **append** a `CompoundJdbcEventListenerFactory(beanFactory)` to its `getFactories()` list;
3. take all `P6OptionChangedListener` beans from the container (sorted by `Ordered`) and call `registerOptionChangedListener(...)`.

`CompoundJdbcEventListenerFactory` implements P6Spy's `P6Factory`:

```java
@Override
public P6LoadableOptions getOptions(P6OptionsRepository optionsRepository) {
    return new NoOpP6LoadableOptions();          // note: NOT the PropertySources variant
}

@Override
public JdbcEventListener getJdbcEventListener() {
    List<JdbcEventListener> listeners = getSortedBeans(beanFactory, JdbcEventListener.class);
    return new CompoundJdbcEventListener(listeners);   // aggregates every listener in the container
}
```

> ⚠️ About `microsphere.jdbc.p6spy.options.*`: this module does ship `PropertySourcesP6LoadableOptionsAdapter`, which exposes the properties under that prefix as P6Spy defaults; but `CompoundJdbcEventListenerFactory.getOptions(...)` returns `NoOpP6LoadableOptions` (`load` is an empty implementation, `getDefaults` returns `emptyMap()`), so **this adapter is not auto-wired**. P6Spy's own configuration therefore still goes through its own `spy.properties`; do not expect `microsphere.jdbc.p6spy.options.appender=…` to take effect.

## 2. `P6DataSourceBeanPostProcessor`: wrapping rules

```java
public class P6DataSourceBeanPostProcessor
        extends GenericBeanPostProcessorAdapter<DataSource> implements EnvironmentAware {

    public static final String EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME
            = "microsphere.jdbc.p6spy.excluded-datasource-beans";
}
```

For every `DataSource` bean, during `postProcessAfterInitialization`:

| Situation | Result |
|------|------|
| The bean name is in the `microsphere.jdbc.p6spy.excluded-datasource-beans` collection | **returned as is**, not wrapped |
| `bean.unwrap(DataSource.class)` succeeds | returns `new P6DataSource(unwrapped)` |
| `unwrap` throws `SQLException` | trace log, then **returned as is** (no exception thrown, startup is not interrupted) |

The exclusion list uses collection binding, so comma-separated values are enough:

```properties
# skip datasources used by migration tooling / health checks
microsphere.jdbc.p6spy.excluded-datasource-beans=flywayDataSource,healthDataSource
```

> The property is read through `environment.getProperty(name, Set.class, emptySet())`, so only forms convertible into a `Set` work (comma-separated string, YAML list).

How to enable it:

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

To verify that the wrapping took effect — the bean taken from the context should `unwrap` back to the original datasource while itself being a `P6DataSource`:

```java
@Autowired
@Qualifier("testDataSource")
private DataSource dataSource;

assertThat(dataSource).isInstanceOf(P6DataSource.class);
assertThat(dataSource.unwrap(DataSource.class)).isNotSameAs(dataSource);
```

## 3. Writing SQL listeners

Declare a Spring bean as P6Spy's `JdbcEventListener` (or extend `com.p6spy.engine.logging.LoggingEventListener`), and `CompoundJdbcEventListenerFactory` will collect and aggregate them sorted by `Ordered`.

The 1.x module's own test uses exactly this shape (excerpt):

```java
@EnableP6DataSource
@EnableEmbeddedDatabase(dataSource = "testDataSource")
public class EnableP6DataSourceTest extends LoggingEventListener {

    @Autowired @Qualifier("testDataSource")
    private DataSource dataSource;

    @Override
    public void onBeforeGetConnection(ConnectionInformation connectionInformation) {
        // proves that P6DataSource internally holds the original DataSource
        assertSame(dataSource.unwrap(DataSource.class), connectionInformation.getDataSource());
    }
}
```

Slow-query alerting example (`LoggingEventListener` hooks, signatures from P6Spy 3.9.1):

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

Common hooks (all can be overridden in a `LoggingEventListener` subclass):

| Method | When |
|------|------|
| `onBeforeGetConnection(ConnectionInformation)` | before acquiring a connection |
| `onAfterGetConnection(ConnectionInformation, SQLException)` | after acquiring a connection |
| `onBeforeAnyExecute(StatementInformation)` | before any statement executes |
| `onAfterAnyExecute(StatementInformation, long elapsed, SQLException)` | after any statement executes (includes elapsed milliseconds) |
| `onCommit(ConnectionInformation, SQLException)` / `onRollback(...)` | transaction boundaries |

`getSqlWithValues()` returns SQL with the parameter values inlined, which makes it easy to copy into a client and reproduce; in production, note that it may contain sensitive literals.

## 4. Supplying P6Spy configuration from Spring property sources

`SpringP6SpyURLConnectionFactory` redirects `p6spy:` URLs to the property-source protocol:

```java
@Override
protected String getSubProtocol() {
    return "p6spy";
}

@Override
public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
    String prefix = getPrefix(url);                       // authority, defaults to "microsphere.jdbc.p6spy"
    URL targetURL = new URL("spring:env:property-sources://" + prefix);
    return targetURL.openConnection();
}
```

Which lets you write:

```
p6spy:microsphere.jdbc.p6spy
```

or specify an authority:

```
p6spy:com.acme.spy
```

Meaning: the configuration items P6Spy needs are read from environment properties under the `<authority>.` prefix, so they can live alongside `application.properties` / a configuration center.

> The prerequisite is that the `spring:` protocol works — `@EnableP6DataSource` also imports `SpringProtocolURLStreamHandler` (which actually lives in `microsphere-spring-context`), and that registers the `URLStreamHandler` for `spring:`.

## 5. Class inventory

| Class | Description |
|----|------|
| `jdbc.p6spy.annotation.EnableP6DataSource` | the only entry-point annotation, no attributes |
| `jdbc.p6spy.annotation.P6DataSourceBeanDefinitionRegistrar` | initializes `P6ModuleManager` and registers the BPP (package-private class) |
| `jdbc.p6spy.beans.factory.config.P6DataSourceBeanPostProcessor` | the `DataSource` → `P6DataSource` wrapper |
| `jdbc.p6spy.beans.factory.CompoundJdbcEventListenerFactory` | `P6Factory`: aggregates the container's `JdbcEventListener`s |
| `jdbc.p6spy.NoOpP6LoadableOptions` | empty `P6LoadableOptions` (the implementation actually in use) |
| `jdbc.p6spy.PropertySourcesP6LoadableOptionsAdapter` | reads the `microsphere.jdbc.p6spy.options` prefix, **not auto-wired** |
| `jdbc.p6spy.net.SpringP6SpyURLConnectionFactory` | `p6spy:` → `spring:env:property-sources://` |
