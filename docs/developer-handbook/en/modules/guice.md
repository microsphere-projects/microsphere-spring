[← Module guides](./README.md)

# microsphere-spring-guice

Bridges Google Guice `@Inject` into the Spring bean lifecycle. Package `io.microsphere.spring.guice` — two
package-private classes and one annotation, which makes it the clearest illustration of this codebase's extension
pattern. `com.google.inject:guice` 7.0.0 is an **optional** dependency (declare it yourself). No
`src/main/resources`.

## 1. `@EnableGuice`

`io.microsphere.spring.guice.annotation.EnableGuice` — no attributes;
`@Target(TYPE) @Retention(RUNTIME) @Documented`; meta-annotated `@Import(GuiceConfiguration.class)`. `@since 1.0.0`.

```java
@Configuration
@EnableGuice
public class IntegrationConfig { }
```

## 2. What Gets Wired

```
@EnableGuice
  └─ @Import(GuiceConfiguration)                      (package-private)
       └─ @Import(GuiceInjectAnnotationBeanPostProcessor)   (package-private)
            └─ extends AnnotatedInjectionBeanPostProcessor bound to com.google.inject.Inject
```

`GuiceInjectAnnotationBeanPostProcessor` performs `@Inject`-style injection for Guice's annotation on Spring-managed
beans and treats Guice's `optional` attribute as the inverse of `required`.

## 3. Public API

Only `@EnableGuice` is public. Guice's own `@com.google.inject.Inject` is the injection marker users write. There are
no public interfaces or helper classes in this module.

## 4. Usage

```java
@Component
public class OrderService {

    @com.google.inject.Inject              // satisfied by a Spring bean
    private PaymentGateway gateway;

    @com.google.inject.Inject(optional = true)   // tolerated when absent
    private AuditClient auditClient;
}
```

Mixed usage is the point: existing Guice-annotated code (a shared library, a migrated module) keeps its annotations
while Spring supplies the dependencies.

## 5. Why Contributors Should Read This Module

It is the smallest end-to-end example of the pattern used everywhere in this repository, and it exposes the reusable
engine: `AnnotatedInjectionBeanPostProcessor` (in `microsphere-spring-context`) implements field/setter/constructor
injection for **any** annotation type you pass to it.

```java
@Configuration
public class CustomInjectionConfig {

    @Bean
    public static AnnotatedInjectionBeanPostProcessor myInjectPostProcessor() {
        return new AnnotatedInjectionBeanPostProcessor(MyInject.class);
    }
}
```

So adding support for another DI annotation (CDI `@Inject`, `@Resource`-like variants, an in-house annotation)
usually requires no new engine code — only an `@Enable*` wrapper around a bean declaration of this post-processor.

## 6. Gotchas

- Guice must be on the classpath explicitly (optional dependency).
- The post-processor is `static` `@Bean`-declared in the engine path so it runs before regular `BeanPostProcessor`
  registration; keep that ordering if you write a similar one.
- This bridge resolves Guice annotations against the **Spring** container; a real Guice `Injector` is not created, so
  Guice modules/bindings are not honored.

---
[← Module guides](./README.md)
