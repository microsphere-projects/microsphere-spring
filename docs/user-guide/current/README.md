# Microsphere Spring · Current Line (0.2.x) User Guide

[简体中文](./zh/README.md) · [User guide overview](../README.md) · [Developer Guide](../../developer-guide/current/README.md) · [1.x (0.1.x) guide](../1.x/README.md)

> This directory documents the **`release` branch** (the 0.2.x line), whose `src/` content is identical to `main`.
> Applications still on the previous maintenance line (0.1.x / `release-1.x`) must use [../1.x/](../1.x/README.md).

---

## 1. Version information

| Item | Value |
|---|---|
| Branch | `release` (`src/` identical to `main`) |
| Development version | `0.2.40-SNAPSHOT` (root POM `<revision>`) |
| Latest released version | `0.2.39` (Maven Central) |
| Maven groupId | `io.github.microsphere-projects` |
| Java version | **Java 17** (root POM `<java.version>17</java.version>`) |
| Servlet namespace | **`jakarta.servlet.*`** (Jakarta EE 11 BOM; no `javax.servlet` dependency) |
| License | Apache License 2.0 |

## 2. Spring Framework compatibility range

This line is regression-tested, one profile at a time, against the following Spring Framework releases (`microsphere-spring-parent/pom.xml`):

| Maven profile | Spring Framework | Project Reactor BOM | Status |
|---|---|---|---|
| `spring-framework-6.0` | 6.0.23 | 2022.0.22 | verified |
| `spring-framework-6.1` | 6.1.21 | 2023.0.19 | verified |
| `spring-framework-6.2`  | 6.2.18 | 2024.0.17 | verified |
| `spring-framework-7.0`  | 7.0.9  | 2025.0.7  | **default** |

**Conclusion**: artifacts built from this branch are supported on **Spring Framework 6.0.x – 7.0.x** with **Java 17+**. Applications still on Spring 5.3.x / `javax.servlet` must use [0.1.x (`release-1.x`)](../1.x/README.md) instead.

## 3. Third-party dependency versions

The values below are managed by the parent POM. Those published with `0.2.39` are listed; Dependabot keeps the `main` / `release` working tree one patch ahead (Jackson `2.22.3`, SnakeYAML `2.7`, ZooKeeper `3.9.6`).

| Dependency | Version | Feature |
|---|---|---|
| microsphere-java | `0.3.19` | Base utilities for every module (compile, required) |
| microsphere-logging | `0.2.2` | Logging facade and test log toggles |
| Jakarta EE BOM | `11.0.0` | Web / test modules (optional) |
| Jackson BOM | `2.22.2` | `@JsonPropertySource`, endpoint metadata JSON (optional) |
| SnakeYAML | `2.6` | `@YamlPropertySource` (optional) |
| P6Spy | `3.9.1` | `@EnableP6DataSource` (optional) |
| Google Guice | `7.0.0` | `@EnableGuice` (optional; pinned in `microsphere-spring-guice/pom.xml`, not parent-managed) |
| ZooKeeper / Curator | `3.9.5` / `5.9.0` | Embedded ZooKeeper test support (optional) |

**Dependency posture (important).** Except for `microsphere-java-core` and the intra-library module edges, which are `compile`, Spring, Jakarta, Jackson, SnakeYAML, P6Spy and Guice are all declared `optional` (or `provided`). The library therefore **never transmits Spring into your build**: the host application must supply a matching Spring version itself, and you add a third-party dependency only when you actually use the feature that needs it (P6Spy, Guice, …).

## 4. Installation

Import the BOM once, then declare modules without versions:

```xml
<properties>
    <microsphere-spring.version>0.2.39</microsphere-spring.version>
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
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

`microsphere-spring-dependencies` manages these coordinates: `context`, `guice`, `jdbc`, `web`, `webmvc`, `webflux`, `test`.

## 5. Module dependency graph

```
microsphere-java-core
    └── microsphere-spring-context ──┬── microsphere-spring-jdbc    (add p6spy yourself)
                                     ├── microsphere-spring-guice   (add guice 7.x yourself)
                                     └── microsphere-spring-web ──┬── microsphere-spring-webmvc
                                                                 └── microsphere-spring-webflux
microsphere-spring-parent / microsphere-spring-dependencies : POM only, no code
microsphere-spring-test : standalone leaf; all third-party dependencies are optional
```

## 6. Module documentation index

| Document | Module | Contents |
|---|---|---|
| [context.md](./context.md) | microsphere-spring-context | Property source extensions, YAML/JSON, TTL caching, configuration binding, listenable `Environment`, parallel pre-instantiation, event extension |
| [web.md](./web.md) | microsphere-spring-web | `WebEndpointMapping` registry, handler-method interception SPI |
| [webmvc.md](./webmvc.md) | microsphere-spring-webmvc | `@EnableWebMvcExtension`, MVC interceptors, reversed proxy |
| [webflux.md](./webflux.md) | microsphere-spring-webflux | `@EnableWebFluxExtension`, reactive interception |
| [jdbc.md](./jdbc.md) | microsphere-spring-jdbc | `@EnableP6DataSource` SQL observability |
| [guice.md](./guice.md) | microsphere-spring-guice | `@EnableGuice` `@Inject` bridging |
| [test.md](./test.md) | microsphere-spring-test | Embedded database / Tomcat / ZooKeeper, test helpers |
| [parent.md](./parent.md) | microsphere-spring-parent | Version alignment, Spring profiles, build properties |
| [dependencies.md](./dependencies.md) | microsphere-spring-dependencies | BOM contents and import semantics |

Chinese mirrors of every page live in [`./zh/`](./zh/).

## 7. Verifying compatibility yourself

```bash
./mvnw clean verify -P spring-framework-6.0   # Spring 6.0.23
./mvnw clean verify -P spring-framework-6.1   # Spring 6.1.21
./mvnw clean verify -P spring-framework-6.2   # Spring 6.2.18
./mvnw clean verify                           # default: Spring 7.0.9
```

## 8. Known limitations on this line

- TTL caching ships **no usable integration**: the Redis classes (`TTLRedisCacheWriterWrapper`, `TTLRedisConfiguration`) are entirely commented out, and nothing else reads `TTLContext`. With a stock cache manager `@TTLCacheable`'s `expire` is ignored — you must supply a TTL-aware `Cache`/`CacheWriter` yourself.
- `@EnableConfigurationBeanBinding` registers **no** `ConfigurationBeanAliasGenerator` in its own `spring.factories`, so relaxed (hyphen/underscore) binding is inactive until you declare the generators.
- `@Idempotent` and `RestControlMapping` are **not present** in the 0.2.x source, despite older README mentions.
- `microsphere-spring-compatible` (present on the 0.1.x line) **does not exist** here.
