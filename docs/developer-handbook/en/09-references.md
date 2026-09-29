[← Handbook index](./README.md)

# 9. References

## 9.1 Repository Documents

| Document | Path / Link |
|----------|-------------|
| Feature overview, module table, usage examples | [README.md](../../../README.md) |
| Onboarding plan (3-phase learning path) | [onboarding-plan.md](../../../onboarding-plan.md) |
| Release notes (generated per release) | [release-notes.md](../../../release-notes.md) |
| Code of Conduct | [CODE_OF_CONDUCT.md](../../../CODE_OF_CONDUCT.md) |
| License | [LICENSE](../../../LICENSE) (Apache 2.0) |
| This handbook | [docs/developer-handbook](../README.md) |

## 9.2 Interactive / External Docs

- DeepWiki (ask questions against the codebase): https://deepwiki.com/microsphere-projects/microsphere-spring
- Zread: https://zread.ai/microsphere-projects/microsphere-spring
- GitHub Wiki (auto-published from sources): https://github.com/microsphere-projects/microsphere-spring/wiki
- Codecov: https://app.codecov.io/gh/microsphere-projects/microsphere-spring
- CI: https://github.com/microsphere-projects/microsphere-spring/actions/workflows/maven-build.yml

## 9.3 Per-Module Javadoc

| Module | Javadoc |
|--------|---------|
| context | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-context |
| web | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-web |
| webmvc | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-webmvc |
| webflux | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-webflux |
| jdbc | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-jdbc |
| guice | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-guice |
| test | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-test |

Local Javadoc: `./mvnw javadoc:javadoc -pl <module>`.

## 9.4 Upstream Projects

| Project | Why it matters here | Link |
|---------|---------------------|------|
| Spring Framework | Everything extends it; reference docs explain the base classes | https://docs.spring.io/spring-framework/reference/ |
| Spring Framework Javadoc | Current API for the classes we subclass | https://docs.spring.io/spring-framework/docs/current/javadoc-api/ |
| microsphere-java | Core utility library this project depends on (`microsphere-java-dependencies`) | https://github.com/microsphere-projects/microsphere-java |
| microsphere-logging | Logging abstraction (`microsphere-logging-dependencies`) | https://github.com/microsphere-projects/microsphere-logging |
| P6Spy | JDBC tracing engine behind `@EnableP6DataSource` | https://p6spy.io |
| Google Guice | Injection engine bridged by `@EnableGuice` | https://github.com/google/guice |
| Apache Curator / ZooKeeper | Embedded ZooKeeper for integration tests | https://curator.apache.org |
| Maven (CI-friendly versions / `${revision}`) | Required to understand the build | https://maven.apache.org/guides/ |
| JUnit 5 | Test framework used throughout | https://junit.org/junit5/docs/current/user-guide/ |

## 9.5 Community

- Issues: https://github.com/microsphere-projects/microsphere-spring/issues
- Discussions (questions, coordination before starting work):
  https://github.com/microsphere-projects/microsphere-spring/discussions
- Organisation: https://github.com/microsphere-projects
- Lead architect: Mercy Ma ([@mercyblitz](https://github.com/mercyblitz)), mercyblitz@gmail.com

## 9.6 Where to Read Next by Goal

| You want to… | Start with |
|--------------|-----------|
| Use one feature in an app | [Module guides](./modules/README.md), then that module's tests under `src/test/java` |
| Add a new extension | [Architecture §2.3](./02-architecture.md), [Conventions §6.2](./06-coding-conventions.md), then read `microsphere-spring-guice` end-to-end |
| Contribute a first PR | [onboarding-plan.md](../../../onboarding-plan.md) Phase 2–3, then [Build & Test](./04-build-and-test.md) |
| Debug a Spring matrix failure | [CI/CD §7.2](./07-ci-cd-and-release.md) + [Troubleshooting §8.1](./08-troubleshooting.md) |
| Release a version | [CI/CD §7.4](./07-ci-cd-and-release.md) |

---
Previous: [8. Troubleshooting](./08-troubleshooting.md) · [Back to index](./README.md)
