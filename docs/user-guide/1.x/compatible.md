# microsphere-spring-compatible (1.x only)

[← 1.x overview](./README.md) · [简体中文](./zh/compatible.md) · [Developer guide](../../developer-guide/1.x/compatible.md) · [0.2.x docs](../current/README.md) — this module exists **only on the 1.x line**

## Version information

| Item | Value |
|------|-----|
| artifactId | `microsphere-spring-compatible` |
| Branch | **only on `release-1.x`** (the 0.2.x branch has no such module) |
| Version on the branch | `0.1.39` |
| Version in development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility range | aimed at **4.3.x – 5.0.x** (fills in classes that only exist from 5.1+) |
| Packaging | `jar` |
| Dependencies | only `com.google.code.findbugs:jsr305` (`optional`) |

> ⚠️ **This module is not published to Maven Central**: its `pom.xml` explicitly configures
> ```xml
> <plugin>
>     <groupId>org.apache.maven.plugins</groupId>
>     <artifactId>maven-deploy-plugin</artifactId>
>     <configuration><skip>true</skip></configuration>
> </plugin>
> ```
> and the `microsphere-spring-dependencies` BOM does **not** manage its coordinates either. So external
> projects cannot reference it with a plain `dependency` — it only takes effect while building the local
> reactor (4.3 / 5.0 profiles).

## 1. What problem it solves

The library's source code makes heavy use of APIs that were introduced in, or became public in, Spring
5.1+. In order to still compile and run on **Spring 4.3 / 5.0**, this module provides same-named
substitutes **under the `org.springframework` package names** — in essence "a mini backport shipped by
the library".

`microsphere-spring-context` declares that relationship in its profile:

```xml
<profile>
    <id>spring-framework-4.3</id>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-compatible</artifactId>
            <version>${revision}</version>
            <optional>true</optional>
        </dependency>
    </dependencies>
</profile>
```

(The `spring-framework-5.0` profile adds the same dependency; 5.1 / 5.2 / 5.3 do not.)

## 2. Inventory

The module holds **15 source files** in total, all inside Spring's packages:

| Class | Kind | Backfilled from | Key members |
|----|------|--------|----------|
| `org.springframework.core.env.Profiles` | `@FunctionalInterface` | Spring 5.1 | `boolean matches(Predicate<String> activeProfiles)`; static `of(String... profileExpressions)` |
| `org.springframework.core.env.ProfilesParser` | package-private `final class` | Spring 5.1 | `static Profiles parse(String...)`, internally implements `and`/`or`/`!` expression parsing plus `assertWellFormed` validation |
| `org.springframework.lang.NonNull` | annotation | popularized after Spring 4.3 | null-safety marker |
| `org.springframework.lang.Nullable` | annotation | same as above | ⚠ not imported by any library source file — this codebase uses `io.microsphere.annotation.Nullable` (74 files); the shim exists only because the forked classes reference it |
| `org.springframework.util.Assert` | utility class | enhanced in 5.x | the assertion family |
| `org.springframework.util.ClassUtils` | utility class | enhanced in 5.x | class loading / assignability checks |
| `org.springframework.util.CollectionUtils` | utility class | enhanced in 5.x | collection emptiness checks / merging |
| `org.springframework.util.ConcurrentReferenceHashMap` | container | enhanced in 5.x | soft/weak-reference map |
| `org.springframework.util.MultiValueMap` | interface | enhanced in 5.x | multi-value map |
| `org.springframework.util.MultiValueMapAdapter` | implementation | **Spring 5.3** (annotated `@since 5.3`; absent in 4.3) | the default `MultiValueMap` implementation |
| `org.springframework.util.ObjectUtils` | utility class | enhanced in 5.x | null-safe comparison / hashing |
| `org.springframework.util.ReflectionUtils` | utility class | enhanced in 5.x | reflection lookups |
| `org.springframework.util.StreamUtils` | utility class | enhanced in 5.x | stream copying |
| `org.springframework.util.StringUtils` | utility class | enhanced in 5.x | `hasText`, `commaDelimitedCollectionToString` and so on |
| `org.springframework.package-info` | `package-info.java` | — | package-level annotations/documentation |

> Naming rule: **the package names and class names are exactly the same as real Spring's**, so your code
> compiles without any conditional logic.

## 3. Runtime behaviour and limitations

Since these are only "good enough" substitutes:

1. **It may be used together with Spring 4.3 / 5.0 only.** If genuine Spring 5.1+ is also on the
   classpath, the same-named classes exist twice and which definition wins is decided by classpath
   order — that is **undefined behaviour** and may surface as bizarre `NoSuchMethodError`s or
   inconsistent behaviour.
2. **Backfilling `Profiles` does not mean feature parity.** In runtimes below Spring 5.1, this library's
   `io.microsphere.spring.core.env.ListenableConfigurableEnvironment` explicitly throws for
   `acceptsProfiles(Profiles)`:
   ```java
   throw new UnsupportedOperationException(
       "The method acceptsProfiles(Profiles) is not supported before Spring Framework 5.1");
   ```
   In other words: the regular paths such as `@ResourcePropertySource` work on 4.3, but the newer APIs
   that rely on profile expressions still do not.
3. **Not something to declare as a production dependency.** Because it is not deployed (see Version
   information), you can only use it while building this repository locally; if a business project must
   pin 4.3, evaluate introducing an equivalent backport yourself.

## 4. Verifying with a local build

```bash
# after switching to the release-1.x branch
./mvnw -P spring-framework-4.3 clean verify     # compiles and uses the compatible module
./mvnw -P spring-framework-5.0 clean verify
./mvnw -P spring-framework-5.3 clean verify     # default, does not reference compatible
```

Because the 4.3 / 5.0 builds depend on this module inside the local reactor, build the **whole
reactor** rather than just `-pl microsphere-spring-context`:

```bash
./mvnw -P spring-framework-4.3 -pl microsphere-spring-parent,microsphere-spring-compatible,microsphere-spring-context -am clean install
```

## 5. What to do when migrating to 0.2.x

On the 0.2.x branch (Spring 6.0+) all of these APIs are provided by Spring itself, so:

- remove every reference to `microsphere-spring-compatible`;
- keep the `org.springframework.lang.Nullable` / `NonNull` imports (they now point at real Spring);
- check whether `MultiValueMapAdapter` is used (provided since Spring 5+) — no substitute needed.
