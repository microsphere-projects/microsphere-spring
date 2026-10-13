# microsphere-spring-guice · 0.2.x

[← Current overview](./README.md) · [简体中文](./zh/guice.md) · [Developer guide](../../developer-guide/current/guice.md) · [1.x docs](../1.x/guice.md)

## Version information

| Item | Value |
|---|---|
| artifactId | `io.github.microsphere-projects:microsphere-spring-guice` |
| Branch | `release` (0.2.x line) |
| Version | `0.2.39` / development `0.2.40-SNAPSHOT` |
| Java | 17 |
| Spring Framework compatibility range | **6.0.x – 7.0.x** |
| Guice version | `com.google.inject:guice` **7.0.0** (optional, you must provide it) |
| Required dependency | `microsphere-spring-context` |
| Optional dependencies | `spring-context`, `guice` |

```xml
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>7.0.0</version>
</dependency>
```

Guice 7 targets Java 11+ and uses the `com.google.inject` annotation package, which is compatible with this line's Java 17 baseline.

This is a three-class module whose only public surface is `@EnableGuice`. `microsphere-spring-context` is required, while `spring-context` and `com.google.inject:guice` **7.0.0** are optional — add Guice yourself, or the annotation type `com.google.inject.Inject` will not be present.

---

## 1. Scope of support (read first)

`@EnableGuice` (`io.microsphere.spring.guice.annotation.EnableGuice`, `@Target(TYPE)`) has **no attributes**, **creates no Guice `Injector`** and **loads no Guice `Module`**. Its single job is registering `GuiceInjectAnnotationBeanPostProcessor`, a package-private subclass of the context module's `AnnotatedInjectionBeanPostProcessor` bound to `com.google.inject.Inject`. In other words: **Guice-`@Inject`ed fields, methods and constructors on Spring-managed beans are satisfied by the Spring container** — nothing more.

Required-ness rule (`determineRequiredStatus` maps Guice's `optional` attribute onto Spring's required flag):

| Guice declaration | Spring behaviour |
|---|---|
| `@Inject` (no `optional` key) | `required = false` (the default branch of `determineRequiredStatus`, treated as not required) |
| `@Inject(optional = true)` | `required = false`, stays `null` when the bean is absent, no failure |
| `@Inject(optional = false)` | ⚠ also `required = false` — see the note below |

A bare `@Inject` is therefore treated as **not required**: a missing candidate yields `null` rather than an exception.

⚠ **`optional = false` does not make the injection required, and no spelling of Guice's `@Inject` does.** The processor is
constructed with `setIgnoreDefaultValue(true)` (`AnnotatedInjectionBeanPostProcessor.java:249`), which drops every attribute
whose value equals its default; Guice declares `boolean optional() default false` (`com.google.inject.Inject`), so
`optional = false` is filtered out before `determineRequiredStatus` ever sees it (`GuiceInjectAnnotationBeanPostProcessor.java:42-47`
then takes the `!containsKey` branch and returns `false`). `optional = true` also returns `false`, because the override
returns `Boolean.FALSE.equals(value)`. Net effect: **every** Guice `@Inject` site is optional, and the attribute cannot opt
back into fail-fast behaviour. If you need a missing bean to be an error, use Spring's `@Autowired` (required by default)
instead.

What is **not** provided: no `Injector`, no `Module` consumption, no `Provider`/`Key`/`Scope` bridge, and no attribute on `@EnableGuice` to pass modules. The module suits Guice→Spring migration where legacy classes keep their `@Inject` annotations; it does not substitute a real Guice container (`Module`, `Key`, `Provider`, `Scope`) — keep one if you need Guice's own object-graph semantics.

---

## 2. Usage

```java
import com.google.inject.Inject;
import io.microsphere.spring.guice.annotation.EnableGuice;

@Configuration
@EnableGuice
public class IntegrationConfig {
}

@Service
public class LegacyService {          // a class migrated from a Guice project
    @Inject
    private OrderRepository repository;        // injected by Spring

    @Inject(optional = true)
    private MetricsReporter metrics;           // null when absent
}
```

Real usage from the module's own tests (`EnableGuiceTest`):

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EnableGuiceTest.class})
@EnableGuice
class EnableGuiceTest {
    @Inject private ApplicationContext context;
    @Inject(optional = true) private String notExists;   // asserted to stay null
}
```

---

## 3. Wiring

| Class | Visibility | Description |
|---|---|---|
| `guice.annotation.EnableGuice` | public | `@Import(GuiceConfiguration.class)`, no attributes |
| `guice.annotation.GuiceConfiguration` | package-private | `@Import(GuiceInjectAnnotationBeanPostProcessor.class)` |
| `guice.annotation.GuiceInjectAnnotationBeanPostProcessor` | package-private | `extends AnnotatedInjectionBeanPostProcessor`, `ANNOTATION_TYPE = Inject.class`, `OPTIONAL_ATTRIBUTE_NAME = "optional"`; overrides `determineRequiredStatus(AnnotationAttributes)` |

Because the post-processor inherits the context module's injection infrastructure, `@Inject` resolution (fields, methods, constructors; type and name matching; `BeanFactory` hierarchy traversal) behaves like Spring's own `@Autowired`/`@Resource` support.
