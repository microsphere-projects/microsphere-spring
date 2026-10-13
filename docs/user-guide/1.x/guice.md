# microsphere-spring-guice · 1.x

[← 1.x overview](./README.md) · [简体中文](./zh/guice.md) · [Developer guide](../../developer-guide/1.x/guice.md) · [0.2.x docs](../current/guice.md)

## Version information

| Item | Value |
|------|-------|
| artifactId | `microsphere-spring-guice` |
| Branch | `release-1.x` |
| Released version | `0.1.39` |
| Version in development | `0.1.40-SNAPSHOT` |
| Java version | **8** |
| Spring Framework compatibility range | **4.3.x – 5.3.x** |
| Guice | `com.google.inject:guice` **7.0.0** (`optional`) |

> ⚠️ **Important conflict**: Guice **7.0.0 requires Java 11+**, while this branch compiles to Java 8. Depending on this module directly and running on JDK 8 will fail. Workable options:
> 1. downgrade Guice to **5.x / 4.2.x** in your own `<dependencyManagement>` (the `com.google.inject.Inject` annotation is still used, and this module's code is unaffected);
> 2. or run the application on JDK 11+ (the library bytecode still targets 8).

Dependencies:

| Dependency | Notes |
|------------|-------|
| `microsphere-spring-context` | Required (it provides `AnnotatedInjectionBeanPostProcessor`) |
| `spring-context` | `optional` |
| `guice` | `optional` — **it is not transitive to you**, you must declare it yourself |

## 1. Scope of support: only `@Inject` is bridged

This module has just 3 classes and a very limited responsibility:

| Class | Visibility | Role |
|-------|------------|------|
| `guice.annotation.EnableGuice` | public | Entry-point annotation, `@Import(GuiceConfiguration.class)`, **no attributes** |
| `guice.annotation.GuiceConfiguration` | **package-private** | Only `@Import(GuiceInjectAnnotationBeanPostProcessor.class)` |
| `guice.annotation.GuiceInjectAnnotationBeanPostProcessor` | **package-private** | Extends `AnnotatedInjectionBeanPostProcessor`, with the annotation type fixed to `com.google.inject.Inject` |

What it does **not** do (verify this before adopting the module):

- it does not create or expose a Guice `Injector`;
- it does not read `com.google.inject.Module`, nor does it bind Spring beans into Guice;
- Guice's `@Provides`, `Provider<T>` semantics, `Scope`, `multibinding` and AOP are not supported;
- `javax.inject.Inject` is not supported (that is the standard annotation Spring has long handled itself);
- Guice's constructor-injection resolution is not supported (injection still happens through Spring's bean lifecycle).

Its positioning is therefore: **let existing code that already carries Guice `@Inject` be injected by Spring without changing the annotation** — a migration aid bridge, not a Guice container integration.

## 2. Mapping the `required` semantics

Guice's `@Inject` expresses "may be absent" through `optional`, while Spring uses `required`. `determineRequiredStatus` performs the conversion:

```java
private static final String OPTIONAL_ATTRIBUTE_NAME = "optional";

@Override
protected boolean determineRequiredStatus(AnnotationAttributes attributes) {
    if (!attributes.containsKey(OPTIONAL_ATTRIBUTE_NAME)) {
        return false;                        // attribute absent → not required
    }
    return Boolean.FALSE.equals(attributes.getBoolean(OPTIONAL_ATTRIBUTE_NAME));
}
```

Cross-reference table:

| Declaration | Resolved `required` | When no matching bean is found |
|-------------|---------------------|--------------------------------|
| `@Inject private Foo foo;` (no `optional` element) | `false` | **`null` is injected, no error** |
| `@Inject(optional = true) private Foo foo;` | `false` | `null` is injected |
| `@Inject(optional = false) private Foo foo;` | ⚠ `false` as well | `null` is injected — no exception |

> ⚠ **Guice's `@Inject` is always optional under this module, and you cannot turn that off.** The processor is built with
> `setIgnoreDefaultValue(true)` (`microsphere-spring-context/.../AnnotatedInjectionBeanPostProcessor.java`, constructor),
> which removes every attribute whose value equals its annotation default. Guice declares `boolean optional() default false`,
> so `optional = false` is dropped before `GuiceInjectAnnotationBeanPostProcessor.determineRequiredStatus` runs and the
> `!containsKey("optional")` branch returns `false`. `optional = true` also yields `false`, because the override returns
> `Boolean.FALSE.equals(value)`. Note the first row in particular: the opposite of Spring's `@Autowired(required = true)`
> default — **a bare `@Inject` is optional here**. If you need "missing means failure", use Spring's `@Autowired`
> (required by default) or `@Resource` with your own validation, not `optional = false`.

## 3. Usage

```java
@Configuration
@EnableGuice
public class IntegrationConfig {
}
```

```java
@Service
public class OrderService {

    @Inject                                   // satisfied by Spring
    private OrderRepository repository;

    @Inject(optional = true)                  // null when absent
    private AuditClient auditClient;

    @Inject(optional = false)                 // ⚠ identical to the bare form above — still optional
    private PaymentGateway paymentGateway;
}
```

The test shipped with 1.x is itself the minimal setup (JUnit 4 + `SpringRunner`):

```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EnableGuiceTest.class})
@EnableGuice
public class EnableGuiceTest {

    @Inject
    private ApplicationContext context;                 // a plain Spring bean can satisfy this

    @Inject
    private GuiceConfiguration guiceConfiguration;      // configuration classes imported via @Import can be injected too

    @Inject(optional = true)
    private GuiceInjectAnnotationBeanPostProcessor postProcessor;

    @Inject(optional = true)
    private String notExists;                           // asserted to be null

    @Test
    public void testInject() {
        assertNotNull(context);
        assertNull(notExists);
    }
}
```

## 4. Wiring

```
@EnableGuice
   └── @Import GuiceConfiguration            (package-private)
          └── @Import GuiceInjectAnnotationBeanPostProcessor   (package-private)
                 └── extends AnnotatedInjectionBeanPostProcessor
                        (microsphere-spring-context, annotation type = com.google.inject.Inject)
```

`AnnotatedInjectionBeanPostProcessor` comes from the injection infrastructure described in [context.md](./context.md): it scans fields and methods for the designated annotation, resolves dependencies from the Spring container by type plus qualifier, and uses `determineRequiredStatus` to decide whether the dependency is enforced.

## 5. Migration advice

If you originally relied on any of the following Guice capabilities, this module **cannot** replace them:

| Guice capability | Equivalent approach in Spring |
|------------------|-------------------------------|
| `Injector` / `Module` / `bind()` | `@Configuration` + `@Bean` |
| `@Provides` | `@Bean` methods |
| `Provider<T>` (deferred provision) | `ObjectProvider<T>` (Spring 4.3+) |
| custom `Scope` | `@Scope` + a `Scope` implementation |
| `@Named` qualification | `@Qualifier` |
| multibinding | `List<T>` / `Map<String, T>` injection |
| Guice AOP | Spring AOP |
