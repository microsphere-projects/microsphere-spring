[← Module guides](./README.md)

# microsphere-spring-jdbc

P6Spy SQL monitoring for existing Spring `DataSource` beans. Package `io.microsphere.spring.jdbc.p6spy`. Depends on
`microsphere-spring-context`; `p6spy:p6spy` (3.9.1), `spring-jdbc` and `spring-context` are **optional** — add p6spy
yourself when you use this feature. The module ships **no** `src/main/resources`; activation is annotation-only.

## 1. `@EnableP6DataSource`

`io.microsphere.spring.jdbc.p6spy.annotation.EnableP6DataSource` — no attributes;
`@Target(TYPE) @Retention(RUNTIME) @Documented`; meta-annotated
`@Import({P6DataSourceBeanDefinitionRegistrar, SpringProtocolURLStreamHandler, SpringP6SpyURLConnectionFactory})`.
`@since 1.0.0`.

```java
@Configuration
@EnableP6DataSource
public class DataSourceConfig { }
```

## 2. What Gets Wired

1. The package-private `P6DataSourceBeanDefinitionRegistrar` (extends
   `AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableP6DataSource>`) initializes the P6Spy `P6ModuleManager`,
   adds a `CompoundJdbcEventListenerFactory`, registers all `P6OptionChangedListener` beans, and registers
   `P6DataSourceBeanPostProcessor`.
2. `P6DataSourceBeanPostProcessor` (`GenericBeanPostProcessorAdapter<DataSource>` + `EnvironmentAware`) wraps
   **every** `DataSource` bean in a P6Spy `P6DataSource` delegate — no application code changes, no URL/driver
   rewriting.
3. `SpringProtocolURLStreamHandler` + `SpringP6SpyURLConnectionFactory` register the `p6spy` sub-protocol of the
   `spring:` URL scheme, mapping `p6spy://...` URLs onto `spring:env:property-sources://...` (default authority
   `microsphere.jdbc.p6spy`) so P6Spy options can be read as a Spring-managed resource.

## 3. Public API

| Type | Role |
|------|------|
| `beans.factory.CompoundJdbcEventListenerFactory` | `P6Factory` that builds a `CompoundJdbcEventListener` from all sorted `JdbcEventListener` beans in the `ConfigurableListableBeanFactory`; `getOptions()` returns `NoOpP6LoadableOptions` |
| `PropertySourcesP6LoadableOptionsAdapter` | `P6LoadableOptions` adapter resolving P6Spy option defaults from Spring `PropertySources` under prefix `microsphere.jdbc.p6spy.options`; constructor takes `ConfigurableEnvironment` |
| `NoOpP6LoadableOptions` | no-op `P6LoadableOptions` (`load()` does nothing, `getDefaults()` empty) — stops P6Spy requiring `spy.properties` |
| `net.SpringP6SpyURLConnectionFactory` | `SpringSubProtocolURLConnectionFactory` for the `p6spy` sub-protocol |

No public interfaces are declared in this module; extension happens by contributing `JdbcEventListener` /
`P6OptionChangedListener` beans.

## 4. Configuration Properties

| Property | Constant | Purpose |
|----------|----------|---------|
| `microsphere.jdbc.p6spy.excluded-datasource-beans` | `P6DataSourceBeanPostProcessor.EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME` | DataSource bean names that must **not** be proxied |
| `microsphere.jdbc.p6spy.options.*` | — | P6Spy options provided through the Spring `Environment` instead of `spy.properties` |

## 5. Recipes

**Log SQL with timing, plus a custom listener**

```properties
application.properties
microsphere.jdbc.p6spy.options.appender=SingleLineLogFormat
microsphere.jdbc.p6spy.options.category=sql
```

```java
@Configuration
@EnableP6DataSource
public class DataSourceConfig {

    @Bean
    @Order(10)
    public JdbcEventListener sqlMetricsListener() {
        return new SqlMetricsListener();
    }
}
```

**Exclude a datasource from wrapping**

```properties
microsphere.jdbc.p6spy.excluded-datasource-beans=xaDataSource,healthCheckDataSource
```

## 6. Gotchas

- You must add the `p6spy` dependency explicitly — it is optional and will not be pulled transitively.
- Ordering matters: `JdbcEventListener` beans are sorted, so use `@Order` when several listeners compete.
- Exclude datasources you must not proxy (XA, pool health checks) — wrapping is applied to all by default.
- Tests in this module run against SQLite (`org.xerial:sqlite-jdbc`) with `microsphere-spring-test`'s
  `@EnableEmbeddedDatabase`; no real DB is needed locally.

---
[← Module guides](./README.md)
