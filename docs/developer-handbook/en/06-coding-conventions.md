[← Handbook index](./README.md)

# 6. Coding Conventions

## 6.1 Source Format and Required Headers

- **Apache license header** on every new `.java` file. Copy the block from any existing file, e.g.
  `microsphere-spring-context/src/main/java/io/microsphere/spring/net/SpringProtocolURLStreamHandler.java`.
  (148 of 161 files in the context module carry it — new files must too.)
- **Class Javadoc** with author and version tags — this is enforced by convention across all 261 documented types:

```java
/**
 * <u>MyFeature</u> is ...
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 * @see SomeSpringClass
 */
```

  Use the real branch's version in `@since` (`1.0.0` on the `1.x` line, `0.2.x` where relevant). `{@link}`-heavy
  Spring-style prose is the house style.
- 4-space indentation, no tabs; import order: third-party/Spring imports first, then `java.*`, then static imports.
- No `.editorconfig` or in-repo formatter; Checkstyle config comes from `microsphere-build`
  (`-Ptest -Ddisable.checks=false` to run it).

## 6.2 The Extension Pattern (Mandatory for New Features)

New capabilities are exposed as opt-in annotations, never as auto-applied behavior:

1. Define `@Enable<Xxx>` in `io.microsphere.spring.<area>.annotation` with
   `@Target(TYPE) @Retention(RUNTIME) @Documented` and `@Import(...)` of a **package-private** registrar.
2. Extend `AnnotatedBeanCapableImportBeanDefinitionRegistrar<Xxx>` (or `AnnotatedBeanCapableImportSelector`) so the
   registrar gets `BeanFactory`/`Environment` awareness and annotation attributes for free.
3. Add a kill-switch: honor `EnvironmentEnabled` or a `microsphere.spring.<feature>.enabled` property, and log each
   registration decision at TRACE (as `WebFluxExtensionBeanDefinitionRegistrar` does).
4. When composing an existing `@Enable*`, meta-annotate it and use `@OverrideAnnotationAttributes` + `@AliasFor`
   to re-expose and override attributes (see `@EnableWebMvcExtension` → `@EnableWebExtension`).
5. Use `@ImportOptional` for imports whose classes may be absent at runtime.
6. Keep registrars/loaders internal: the annotation is the public contract; users should never import a registrar
   directly.

## 6.3 Naming and Structure

- **Mirror Spring packages**: put extension classes in paths parallel to what they enhance —
  `io.microsphere.spring.core.env` ↔ `org.springframework.core.env`,
  `io.microsphere.spring.beans.factory` ↔ `org.springframework.beans.factory`.
- **Suffixes carry meaning**: `*Registrar` (ImportBeanDefinitionRegistrar), `*Loader` (ImportSelector loading
  resources/property sources), `*PostProcessor` (Bean(Body)FactoryPostProcessor), `*Advice`/`*Interceptor`
  (cross-cutting hooks), `*Resolver`/`*Factory`/`*Registry` (SPI roles), `*Utils` (stateless static helpers declared
  `abstract` and implementing `Utils`), `Adapter` (no-op-default interfaces/base classes).
- **Infrastructure bean names are camelCase of the class** and often exposed as a `BEAN_NAME` constant
  (`interceptingHandlerMethodProcessor`, `delegatingHandlerMethodAdvice`, `lazyCompositeHandlerInterceptor`,
  `ttlCacheResolver`, `configurationBeanBindingPostProcessor`). Keep that convention so users can override by name.
- **Property names** go in a `constants/PropertyConstants` class per module with the module prefix
  (`microsphere.spring.webmvc.`), never inlined as string literals.
- Prefer subclassing/adapting Spring types over replacing them, and resolve generic types with `ResolvableType`
  rather than requiring explicit `Class` parameters.

## 6.4 Dependency Discipline

- No version numbers in module POMs — use the parent's `dependencyManagement`/properties.
- Mark integration dependencies `optional` (p6spy, guice, zookeeper, tomcat-embed, spring-webflux) so consumers are
  not forced to carry them; guard code paths so absence degrades gracefully.
- SPI registration lives in `META-INF/spring.factories` (this codebase still uses spring.factories rather than
  `spring/*.imports`); load via `SpringFactoriesLoaderUtils` for caching and custom class loaders.

## 6.5 Compatibility Floor

- `main`: Java 17 baseline; Spring **6.0.x** is the lowest supported line — verify against `-P spring-framework-6.0`
  before opening a PR that touches Spring API usage. Jakarta EE 11 / `jakarta.*` namespace.
- `1.x`: Java 8 + Spring 4.3.x/5.3.x, `javax.*` namespace. Changes that belong on both lines must be back-ported
  explicitly (the automation only propagates `main` → `dev`/`release`, not `main` → `1.x`).

## 6.6 Commits, Tests, PRs

- Commit messages: concise imperative subject, e.g. `feat: add ...`, `fix: ...`, `refactor: ...`, `chore: bump ...`;
  release automation uses `chore: bump version to next patch after publishing <rev>`. Check `git log --oneline -20`
  before writing your own.
- Every behavior change ships with tests; run the matrix you can locally at minimum
  (`./mvnw verify` plus `-P spring-framework-6.0` when Spring APIs are involved).
- PR body explains the *why*, links the issue, and notes which Spring lines were verified. CI must be green before
  review; merges to `main` are auto-propagated to `dev` and `release`.

---
Previous: [5. Module Guides](./modules/README.md) · Next: [7. CI/CD & Release](./07-ci-cd-and-release.md)
