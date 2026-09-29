[← Handbook index](./README.md)

# 4. Build & Test

## 4.1 POM Hierarchy and CI-Friendly Versions

```
microsphere-build:0.3.16        (external parent — supplies all build plugins)
  └─ microsphere-spring          <version>${revision}</version>, <revision>0.2.40-SNAPSHOT</revision>
       ├─ microsphere-spring-parent / -dependencies / library modules  (all inherit ${revision})
```

- Never hard-code a version in a module POM; the reactor propagates `${revision}`.
- `flatten-maven-plugin` (`flattenMode=resolveCiFriendliesOnly`, from `microsphere-build`) rewrites `${revision}` into
  real values at publish time. `.flattened-pom.xml` is a build artifact and is git-ignored.
- `java.version` is `17` (the root POM overrides the grandparent default of `8`); `microsphere-build` activates the
  `java9+` profile which sets `maven.compiler.release`, and `java16+` which adds `--add-opens` JVM args for tests.
- `maven-compiler-plugin` runs with `<parameters>true</parameters>` — parameter name discovery (used heavily by
  injection-point resolution) depends on this flag.

## 4.2 Version Properties (microsphere-spring-parent)

| Property | Value | Managed dependency |
|----------|-------|--------------------|
| `microsphere-java.version` | 0.3.19 | core utility BOM `microsphere-java-dependencies` |
| `microsphere-logging.version` | 0.2.2 | `microsphere-logging-dependencies` |
| `jakartaee.version` | 11.0.0 | `jakarta.jakartaee-bom` |
| `jackson.version` | 2.22.3 | `jackson-bom` |
| `snakeyaml.version` | 2.7 | YAML support for property sources |
| `p6spy.version` | 3.9.1 | JDBC monitoring |
| `zookeeper.version` | 3.9.6 | embedded ZooKeeper tests |
| `curator.version` | 5.9.0 | `curator-recipes`, `curator-test` (both exclude zookeeper) |

BOMs imported by the parent: `spring-framework-bom`, `reactor-bom`, `jackson-bom`, `jakarta.jakartaee-bom`,
`microsphere-java-dependencies`, `microsphere-logging-dependencies`.

## 4.3 Spring Compatibility Profiles

The parent POM declares profiles that pin the Spring/Reactor line under test. This is how one codebase serves
Spring 6.0 → 7.0:

| Profile | `spring-framework.version` | `reactor.version` | Active by default |
|---------|---------------------------|-------------------|-------------------|
| `spring-framework-6.0` | 6.0.23 | 2022.0.22 | no |
| `spring-framework-6.1` | 6.1.21 | 2023.0.19 | no |
| `spring-framework-6.2` | 6.2.18 | 2024.0.17 | no |
| `spring-framework-7.0` | 7.0.9 | 2025.0.7 | **yes** |

```bash
# Build/test against a specific Spring line
./mvnw verify -P spring-framework-6.0
./mvnw verify -P spring-framework-7.0
```

**Rule for contributors:** any API you use must exist in Spring 6.0.x, because CI runs the full matrix
(3 JDKs × 4 Spring profiles = 12 combinations). Never rely on Spring-7-only APIs without a fallback.

## 4.4 Common Commands

```bash
./mvnw verify                        # full build + tests (default profile: Spring 7.0)
./mvnw package -DskipTests           # fast packaging
./mvnw test -pl microsphere-spring-context              # one module's tests
./mvnw verify -pl microsphere-spring-webmvc -am         # one module + its dependencies
./mvnw test -pl microsphere-spring-context -Dtest=*PropertySource*   # pattern of test classes
./mvnw install -DskipTests           # publish to ~/.m2 for downstream local projects
./mvnw javadoc:javadoc -pl microsphere-spring-context   # local Javadoc

# CI-equivalent invocation (as run by GitHub Actions)
mvn --batch-mode --update-snapshots -Drevision=0.0.1-SNAPSHOT \
    test --activate-profiles test,coverage,spring-framework-7.0
```

Test-class conventions from `microsphere-build`: Surefire includes `**/*Test.java` and `**/*Tests.java`, excludes
`**/Abstract*.java` (which is why the reusable bases in `microsphere-spring-test` are named `Abstract*` and annotated
`@Disabled`). Checkstyle runs only under `-Ptest` and is skipped by default (`-Ddisable.checks=false` to enable).
JaCoCo runs under `-Pcoverage`.

## 4.5 Testing Approach

- **Framework**: JUnit 5 + Spring Test; Mockito, Hamcrest, JSONassert available.
- **Use the in-house helpers** (`microsphere-spring-test`) instead of hand-rolling contexts:

```java
// Boot a plain annotation context and assert against it
SpringTestUtils.testInSpringContainer(context -> {
    assertNotNull(context.getBean(MyService.class));
}, AppConfig.class);

// Embedded database for JDBC tests
@EnableEmbeddedDatabase(dataSource = "testDataSource", type = EmbeddedDatabaseType.H2)
@Configuration
class DaoTestConfig {}

// Full embedded Tomcat web integration test
@EmbeddedTomcatConfiguration(port = 8080, docBase = "classpath:/webapp")
class TomcatIT { }

// Embedded ZooKeeper for registry-based tests
@EmbeddedZookeeperServer(port = 2181)
class ZooKeeperIT { }
```

- **Endpoint testing bases**: `AbstractWebMvcTest` (MockMvc) and `AbstractWebFluxTest` (WebTestClient) come wired with
  `TestController` and `RouterFunctionTestConfig`; they are `@Disabled` bases — extend them and call the provided
  `test*` helpers or add your own.
- **Request fixtures**: `SpringTestWebUtils.createWebRequest(...)`, `MockServletWebRequest`,
  `WebTestUtils.mockServerWebExchange()`, `TestServletContext` (captures servlet/filter registrations).
- **Test-as-documentation**: test class names mirror the class under test; test method names state the expected
  behavior. Reading a module's tests is the fastest way to learn its contract.
- **Coverage**: JaCoCo + Codecov, per-PR upload. Find under-tested classes on the
  [Codecov dashboard](https://app.codecov.io/gh/microsphere-projects/microsphere-spring).

---
Previous: [3. Setup & Quick Start](./03-setup-and-quick-start.md) · Next: [5. Module Guides](./modules/README.md)
