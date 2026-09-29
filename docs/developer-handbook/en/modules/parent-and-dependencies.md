[← Module guides](./README.md)

# microsphere-spring-parent & microsphere-spring-dependencies

These two modules publish no classes: they are the build's version-control spine. Read this before adding a
dependency.

## 1. `microsphere-spring-parent` — internal version alignment

Inherited by every library module (via `relativePath`). It declares **no** `<build>`/plugin management — that comes
from the external grandparent `microsphere-build:0.3.16`.

### Properties

| Property | Value |
|----------|-------|
| `microsphere-java.version` | 0.3.19 |
| `microsphere-logging.version` | 0.2.2 |
| `jakartaee.version` | 11.0.0 |
| `jackson.version` | 2.22.3 |
| `snakeyaml.version` | 2.7 |
| `p6spy.version` | 3.9.1 |
| `zookeeper.version` | 3.9.6 |
| `curator.version` | 5.9.0 |

### `dependencyManagement`

- BOM imports: `org.springframework:spring-framework-bom`, `io.projectreactor:reactor-bom`,
  `com.fasterxml.jackson:jackson-bom`, `jakarta.platform:jakarta.jakartaee-bom`,
  `io.github.microsphere-projects:microsphere-java-dependencies`,
  `io.github.microsphere-projects:microsphere-logging-dependencies`.
- Direct entries: `org.yaml:snakeyaml`, `p6spy:p6spy`, `org.apache.zookeeper:zookeeper`,
  `org.apache.curator:curator-recipes`, `org.apache.curator:curator-test` (both Curator artifacts exclude
  `zookeeper` to avoid version conflicts).

### Spring compatibility profiles

| Profile | spring-framework | reactor | default |
|---------|------------------|---------|---------|
| `spring-framework-6.0` | 6.0.23 | 2022.0.22 | |
| `spring-framework-6.1` | 6.1.21 | 2023.0.19 | |
| `spring-framework-6.2` | 6.2.18 | 2024.0.17 | |
| `spring-framework-7.0` | 7.0.9 | 2025.0.7 | **activeByDefault** |

```bash
./mvnw verify -P spring-framework-6.0     # prove you did not use a newer Spring API
```

## 2. `microsphere-spring-dependencies` — the published BOM

Parent: `microsphere-spring-parent`. It manages exactly the 7 published library artifacts, all at `${revision}`:

`microsphere-spring-context`, `-guice`, `-jdbc`, `-web`, `-webmvc`, `-webflux`, `-test`.

That is the whole contract consumers import. When a new library module is added, **add it here too** — otherwise
consumers cannot depend on it version-free.

## 3. Root `pom.xml` vs parent

| | root `pom.xml` | `microsphere-spring-parent` |
|--|----------------|------------------------------|
| Role | aggregator + CI metadata | version alignment POM |
| Version | `${revision}` (`0.2.40-SNAPSHOT`) | `${revision}` |
| Declares | `<modules>` (9), license, developers, SCM, `java.version=17` | dependencyManagement + Spring profiles |
| Build plugins | none (inherited from `microsphere-build`) | none |

## 4. What `microsphere-build` contributes (external parent)

- Always-on: `maven-compiler-plugin` with `<parameters>true</parameters>`, `maven-source-plugin` (jar-no-fork),
  `flatten-maven-plugin` (`flattenMode=resolveCiFriendliesOnly`, `process-resources`, `updatePomFile=true`) — this is
  how `${revision}` becomes a real version at publish time.
- Profiles: `publish` (javadoc, release, enforcer, gpg, git-commit-id, central-publishing), `ci` (sign-maven-plugin
  keyed by `SIGN_KEY_ID`/`SIGN_KEY`/`SIGN_KEY_PASS`), `test` (failsafe, checkstyle, surefire), `coverage` (jacoco).
- Surefire conventions: include `**/*Test.java`, `**/*Tests.java`; exclude `**/Abstract*.java`.
- Checkstyle config ships inside the parent (`checkstyle/checkstyle.xml`), skipped by default via
  `disable.checks=true`; enable with `-Ptest -Ddisable.checks=false`.
- Enforcer bans duplicate POM dependency versions and banned dependencies.

## 5. Rules for Dependency Changes

1. New third-party version → add a property + `dependencyManagement` entry in `microsphere-spring-parent`; module POMs
   reference it with **no** `<version>`.
2. Integration libraries that not everyone needs → mark `optional` in the module POM (as p6spy, guice, zookeeper,
   tomcat-embed, spring-webflux are).
3. Consumer-visible artifact → ensure it is managed in `microsphere-spring-dependencies`.
4. Dependabot opens daily Maven PRs and weekly GitHub Actions PRs (limit 10 open) — review them like any other PR:
   confirm `./mvnw verify` on both extremes of the Spring matrix.
5. Never downgrade `jakartaee.version` or `java.version` on `main`; the `1.x` line (javax, Java 8) is where legacy
   targets live.

---
[← Module guides](./README.md)
