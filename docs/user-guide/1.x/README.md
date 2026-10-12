# Microsphere Spring · Maintenance Line (0.1.x) User Guide

[← 1.x overview](./README.md) · [简体中文](./zh/README.md) · [Developer guide](../../developer-guide/1.x/README.md) · [0.2.x docs](../current/README.md)

> This directory documents the **`release-1.x` branch** (the 0.1.x line).
> For the current line (0.2.x / `release` branch), see [../current/](../current/README.md).

---

## 1. Version information

| Item | Value |
|---|---|
| Branch | `release-1.x` |
| Development version | `0.1.40-SNAPSHOT` (root POM `<revision>`) |
| Latest released version | `0.1.39` (Maven Central) |
| Maven groupId | `io.github.microsphere-projects` |
| Build parent | `microsphere-build:0.3.14` (this branch declares no `java.version`; it is inherited from the parent POM) |
| Java version | **Java 8** (`maven.compiler.source/target/release = ${java.version} = 8`) |
| Servlet namespace | **`javax.servlet.*`** (`javax.servlet-api` 4.0.1) |
| Embedded Tomcat | `tomcat-embed-core` **9.0.119** (overridden by the parent POM, adapted to `javax.servlet`) |
| Modules | 9 (one more than 0.2.x: `microsphere-spring-compatible`) |
| License | Apache License 2.0 |

## 2. Spring Framework compatibility range

| Maven profile | Spring Framework | Project Reactor BOM | Status |
|---|---|---|---|
| `spring-framework-4.3` | 4.3.30.RELEASE | Bismuth-SR17 | verified (adds `microsphere-spring-compatible`) |
| `spring-framework-5.0` | 5.0.20.RELEASE | Bismuth-SR17 | verified (adds `microsphere-spring-compatible`) |
| `spring-framework-5.1` | 5.1.20.RELEASE | Californium-SR23 | verified |
| `spring-framework-5.2` | 5.2.25.RELEASE | Dysprosium-SR25 | verified |
| `spring-framework-5.3` | 5.3.39 | 2020.0.47 | **default** |

**Conclusion**: this version line targets **Spring Framework 4.3.x – 5.3.x** and **Java 8+**. For Spring 6/7 or the Jakarta namespace, use [0.2.x (`release`)](../current/README.md) instead.

## 3. Third-party dependency versions

| Dependency | Version | Notes |
|---|---|---|
| microsphere-java | `0.3.19` | the only required compile-scope dependency source |
| microsphere-logging | `0.2.2` | logging facade |
| Jackson BOM | `2.22.2` | YAML/JSON property sources, endpoint-metadata JSON (optional) |
| SnakeYAML | `2.6` | `@YamlPropertySource` (optional) |
| P6Spy | `3.9.1` | `@EnableP6DataSource` (optional) |
| Google Guice | `7.0.0` | `@EnableGuice` (optional, **see section 6 for the version-line conflict**) |
| javax.servlet-api | `4.0.1` | web/test modules (optional/provided); the version is managed by `microsphere-java-dependencies` |
| Tomcat embed | `9.0.119` | embedded-Tomcat tests (overridden by the parent POM) |
| ZooKeeper / Curator | `3.9.5` / `5.9.0` | embedded-ZooKeeper tests (optional) |
| JUnit | managed by the BOM (not declared explicitly inside the branch) | web/webmvc/jdbc tests are based on **JUnit 4**, while the WebFlux base classes in webflux/test are based on **JUnit 5** |

`microsphere-spring-dependencies` manages only the 7 published modules: `context`, `guice`, `jdbc`, `web`, `webmvc`, `webflux`, `test` (**excluding** `microsphere-spring-compatible`).

## 4. Install

```xml
<properties>
    <microsphere-spring.version>0.1.39</microsphere-spring.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-dependencies</artifactId>
            <version>${microsphere-spring.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-context</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-webmvc</artifactId>
    </dependency>
</dependencies>
```

The Spring dependencies are all `optional` as well: the host application must supply Spring 4.3–5.3 and `javax.servlet-api` itself.

## 5. Key differences from 0.2.x

| Aspect | 0.1.x (this branch) | 0.2.x (`release`) |
|---|---|---|
| Spring / Java | 4.3.x – 5.3.x / Java 8 | 6.0.x – 7.0.x / Java 17 |
| Servlet | `javax.servlet` | `jakarta.servlet` |
| MVC configuration base | `WebMvcConfigurerAdapter` (Spring 4.3 compatibility) | `WebMvcConfigurer` |
| Servlet functional-endpoint adapters | **do not exist** (only `webflux.function.server.*`) | `web.servlet.function.*` adapters are present |
| `WebEndpointMappingVisitor` | **1.x only** (expression-style condition building) | removed |
| Classic war bootstrap entry point | `META-INF/services/javax.servlet.ServletContainerInitializer` | `.../jakarta.servlet.ServletContainerInitializer` |
| Test base classes | `AbstractWebMvcTest` based on JUnit 4 (`@Ignore @RunWith(SpringRunner)`) | JUnit 5 (`@Disabled`) |
| Embedded Tomcat annotations | `@RunWith(SpringRunner)`, **JUnit 4 only** | `@ExtendWith(SpringExtension)`, JUnit 5 |
| Extra module | `microsphere-spring-compatible` (low-version Spring API backfill, not published) | none |
| `PropertySourcesP6LoadableOptionsAdapter` | present but **referenced by no code** (dead code) | also requires manual registration |

## 6. Known conflicts and limitations

1. **Guice 7.0.0 conflicts with Java 8**: `microsphere-spring-guice` declares `guice.version = 7.0.0`, while Guice 7 requires Java 11+. On this branch (Java 8), using `@EnableGuice` means downgrading Guice to 5.x/4.x yourself, or running on Java 11+.
2. **Redis TTL unavailable**: `TTLRedisCacheWriterWrapper` / `TTLRedisConfiguration` are commented out as a whole on this branch too, and the pom has no spring-data-redis dependency.
3. **The `Profiles` API on Spring 4.3 / 5.0**: `ListenableConfigurableEnvironment` depends on `org.springframework.core.env.Profiles`, introduced in 5.1, and it is backfilled by `microsphere-spring-compatible`; on a genuine 4.3/5.0 runtime the related capabilities throw `UnsupportedOperationException("... not supported before Spring Framework 5.1")`.
4. **Test-module profile naming issue**: `microsphere-spring-test` defines a profile named `spring-framework-4.3`, yet its `spring-framework.version` value is `5.0.20.RELEASE`; keep this in mind when validating 4.3 with that profile.
5. **`@EnableEmbeddedDatabase(port = ...)` has no effect**: the registrar on this branch does not read that attribute either.
6. **`microsphere-spring-compatible` is not published**: `maven-deploy-plugin` is configured with `<skip>true</skip>`, it is not in the BOM, and it is only used internally by this library as an optional dependency under the 4.3/5.0 profiles.

## 7. Module documentation index

| Document | Module | Contents |
|---|---|---|
| [context.md](./context.md) | microsphere-spring-context | property sources, YAML/JSON, TTL cache, configuration binding, listenable Environment, parallel initialization, event extensions |
| [web.md](./web.md) | microsphere-spring-web | `WebEndpointMapping`, registry SPI, method-interception SPI, `WebEndpointMappingVisitor` |
| [webmvc.md](./webmvc.md) | microsphere-spring-webmvc | `@EnableWebMvcExtension`, interceptors, gateway reverse proxy, javax war bootstrap |
| [webflux.md](./webflux.md) | microsphere-spring-webflux | `@EnableWebFluxExtension`, reactive interception |
| [jdbc.md](./jdbc.md) | microsphere-spring-jdbc | `@EnableP6DataSource` |
| [guice.md](./guice.md) | microsphere-spring-guice | `@EnableGuice` and the Java 8 / Guice 7 conflict notes |
| [test.md](./test.md) | microsphere-spring-test | JUnit 4 embedded libraries/containers and test utilities |
| [compatible.md](./compatible.md) | microsphere-spring-compatible | low-version Spring API backfill (1.x only) |
| [parent.md](./parent.md) | microsphere-spring-parent | version pins, third-party property keys and the Spring profile matrix of the parent POM |
| [dependencies.md](./dependencies.md) | microsphere-spring-dependencies | the BOM applications import: the managed module coordinates and transitive-dependency notes |

## 8. Verifying compatibility

```bash
./mvnw clean verify                          # default Spring 5.3.39
./mvnw clean verify -P spring-framework-5.2  # Spring 5.2.25.RELEASE
./mvnw clean verify -P spring-framework-5.1  # Spring 5.1.20.RELEASE
./mvnw clean verify -P spring-framework-5.0  # Spring 5.0.20.RELEASE (pulls in compatible)
./mvnw clean verify -P spring-framework-4.3  # Spring 4.3.30.RELEASE (pulls in compatible)
```
