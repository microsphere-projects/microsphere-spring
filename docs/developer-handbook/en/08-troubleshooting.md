[← Handbook index](./README.md)

# 8. Troubleshooting

## 8.1 Build & Environment

| Symptom | Cause | Fix |
|---------|-------|-----|
| `java: error: release version 17 not supported` | IDE/CLI using JDK < 17 | Set project SDK / `JAVA_HOME` to 17+ |
| `mvnw: Permission denied` | Missing execute bit | `chmod +x mvnw` |
| `Could not resolve dependencies` | Corporate proxy or stale local cache | Configure `~/.m2/settings.xml`, or force update: `./mvnw -U verify` |
| Tests pass in CI, fail locally (or vice versa) | OS path separators, locale, JDK-specific `--add-opens` behavior | `./mvnw verify -Dfile.encoding=UTF-8`; also try JDK 17 vs 21 since CI tests 17/21/25 |
| Only some Spring profiles build | You used an API introduced after 6.0 | Reproduce with `./mvnw verify -P spring-framework-6.0`; add a fallback or guard |
| `Non-resolvable parent POM microsphere-build:0.3.16` | Missing external parent | Build once with network access so Maven Central caches it |
| Strange versions in consumer builds | `${revision}` leaked into an installed POM | Rebuild with `-Drevision=<real version>`; never commit `.flattened-pom.xml` |
| IDE cannot resolve sibling modules | Maven reactor not imported | Re-import the root `pom.xml` as a Maven project |
| `ClassNotFoundException` for p6spy/guice/tomcat/zookeeper at runtime | Those are `optional` deps and not transitive | Add the dependency explicitly for the feature you enabled |

## 8.2 Feature-Level Issues

### TTL caching

| Symptom | Cause | Fix |
|---------|-------|-----|
| `@TTLCacheable` has no effect | Missing `@EnableTTLCaching`, or the cache is not Spring-managed | Add `@EnableTTLCaching`; verify a `CacheManager` bean exists |
| Compile error: `cannot find symbol: method ttl()` | The attribute is named `expire`, not `ttl` (the root README snippet is outdated) | Use `expire = 300, timeUnit = TimeUnit.SECONDS` |
| Missing/zero TTL behavior | `@TTLCacheable` requires an explicit TTL; there is no global default | Specify `expire` on every annotated method |
| Redis TTL not applied | `cache.redis.TTLRedisCacheWriterWrapper` / `TTLRedisConfiguration` are entirely commented out upstream | Not currently supported — apply TTL via your `RedisCacheManager` config, or `TTLContext.doWithTTL(...)` |
| `NoClassDefFoundError: TTLCacheResolver` | Package is `io.microsphere.spring.cache.intereptor` (upstream typo) | Import the actual package; expect it to be renamed later |

### Property sources

| Symptom | Cause | Fix |
|---------|-------|-----|
| `@ResourcePropertySource` loads nothing | Wildcard matched no resource | Use `classpath*:/dir/*.properties` and confirm the file is on the classpath; set `ignoreResourceNotFound=true` only if absence is legitimate |
| Values do not refresh | `autoRefreshed=false` (default) | Set `autoRefreshed = true` |
| Loaded in unexpected order | Wildcard ordering is comparator-driven | Set `first=true` or `before`/`after="<sourceName>"`, or supply a custom `resourceComparator` |
| YAML keys missing | Nested YAML maps need their full path | Keys are flattened (`a.b.c`); check with `PropertySourcesUtils.getSubProperties("a.")` |
| Property source overrides your app config | Enhanced sources are added by default at the tail | Use `first=true`/`before=`/`after=` to position deliberately |

### Listenable Environment

| Symptom | Cause | Fix |
|---------|-------|-----|
| Your `EnvironmentListener` never fires | It isn't registered before the environment is read (or `ListenableConfigurableEnvironmentInitializer` did not run) | Register the listener early (an `ApplicationContextInitializer` or `@Component` in the root context) and confirm the initializer ran; debug with `LoggingEnvironmentListener` |
| No property-access audit entries | `microsphere.spring.listenable-autowire-candidate-resolver.enabled` not set | Set it to `true` for autowire-candidate auditing |

### Web extensions

| Symptom | Cause | Fix |
|---------|-------|-----|
| Endpoint registry empty | `registerWebEndpointMappings=false`, or `-web` alone on the classpath | Enable the flag and add `microsphere-spring-webmvc` / `-webflux` |
| `HandlerMethodInterceptor` not invoked | Missing `@EnableWeb*Extension`, `interceptHandlerMethods=false`, or the interceptor bean is not in a `sources` location | Turn on interception; register the interceptor as a bean (or via spring.factories / ServiceLoader) |
| Interceptor order wrong | Order is `AnnotationAwareOrderComparator` | Add `@Order`/implement `Ordered` |
| `@RequestBody` capture returns null | `storeRequestBodyArgument=false`, or the converter is not in `SUPPORTED_CONVERTER_TYPES` (Jackson/String only) | Enable the flag; use Jackson or String conversion |
| Interceptor fires twice | `registerHandlerInterceptors=true` **and** your own `addInterceptors` registration | Use one path only; `LazyCompositeHandlerInterceptor` already excludes explicitly-registered beans |
| Behind a gateway, `ReversedProxyHandlerMapping` does nothing | Propagator doesn't send `microsphere_wem_id`, or the endpoint isn't `RequestMappingHandlerMapping`-sourced | Ensure the upstream sets the id header; note current mapping-source limitation |
| Reactive stack: `RequestAttributes` features unreliable | `requestContextStrategy=DEFAULT` binds nothing | Set `THREAD_LOCAL`, or `INHERITABLE_THREAD_LOCAL` when threads are spawned |
| MVC init-params not applied in a war | `EnableWebMvcExtensionListener` SCI not discovered | Confirm `META-INF/services/jakarta.servlet.ServletContainerInitializer` is present in the packaged artifact (shade/uber-jar merges drop it otherwise) |

### JDBC / Guice

| Symptom | Cause | Fix |
|---------|-------|-----|
| No SQL logs after `@EnableP6DataSource` | p6spy dependency missing (optional), or no appender configured | Add `p6spy:p6spy`; set `microsphere.jdbc.p6spy.options.*` (or `spy.properties`) |
| Wrong `DataSource` injected / proxy chain broken | The datasource you meant to exclude got wrapped | Add it to `microsphere.jdbc.p6spy.excluded-datasource-beans` |
| Guice `@Inject` left null | `@EnableGuice` missing, or Guice not on the classpath | Add both; remember a real Guice `Injector` is not created, so Guice `Module` bindings are not honored |

## 8.3 Diagnostics That Actually Help

```properties
# What got registered by an @EnableWeb*Extension
logging.level.io.microsphere.spring.webmvc.annotation=TRACE
logging.level.io.microsphere.spring.webflux.annotation=TRACE
# Who reads which property
logging.level.io.microsphere.spring.core.env=TRACE
```

```java
// Dump every registered endpoint from your own context
@Autowired WebEndpointMappingRegistry registry;
registry.getWebEndpointMappings().forEach(m -> System.out.println(m.toExpression()));

// Which bean costs startup time? register the listener:
@Bean public BeanListener beanTimeStatistics() { return new BeanTimeStatistics(); }
```

- Read the module's tests — they document real usage and are the fastest oracle when behavior surprises you.
- Use [DeepWiki](https://deepwiki.com/microsphere-projects/microsphere-spring) or
  [Zread](https://zread.ai/microsphere-projects/microsphere-spring) to ask questions against the codebase.
- When reporting a bug: include Java version, Spring Framework version (and which compatibility profile), the
  Microsphere Spring version, the `@Enable*` annotations in use, a minimal reproducer, and the full stack trace.
  Search [existing issues](https://github.com/microsphere-projects/microsphere-spring/issues) first, then open one —
  or ask in [Discussions](https://github.com/microsphere-projects/microsphere-spring/discussions).

---
Previous: [7. CI/CD & Release](./07-ci-cd-and-release.md) · Next: [9. References](./09-references.md)
