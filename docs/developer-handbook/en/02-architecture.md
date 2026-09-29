[← Handbook index](./README.md)

# 2. Architecture

## 2.1 Module Layering

The repository builds 9 Maven modules. Every functional module inherits `microsphere-spring-parent`; third-party
versions are centralized in the parent and in the published BOM `microsphere-spring-dependencies`.

```
io.github.microsphere-projects:microsphere-build:0.3.16   (external grandparent POM — plugins)
 └─ microsphere-spring (root pom.xml)                     (aggregator, version = ${revision})
     ├─ microsphere-spring-parent        version-alignment POM: dependencyManagement + Spring compatibility profiles
     ├─ microsphere-spring-dependencies  published BOM: manages the 7 library artifacts at ${revision}
     ├─ microsphere-spring-context       core extensions — everything else builds on this
     │   ├─ microsphere-spring-web       transport-agnostic web abstractions
     │   │   ├─ microsphere-spring-webmvc    Spring MVC (servlet) implementation
     │   │   └─ microsphere-spring-webflux   Spring WebFlux (reactive) implementation
     │   ├─ microsphere-spring-jdbc      P6Spy integration
     │   ├─ microsphere-spring-guice     Google Guice bridge
     │   └─ microsphere-spring-test      testing utilities (depends on the others)
```

Key dependency facts:

- `context` depends on `microsphere-java` (core utilities) and `microsphere-logging`; it is the only hard internal dependency of the other modules.
- `web` defines contracts only; **webmvc and webflux are symmetric implementations** wired through the
  `io.microsphere.spring.web.util.SpringWebHelper` SPI (registered in each module's `META-INF/spring.factories`).
- Heavy integrations are `optional` dependencies (p6spy, guice, zookeeper, tomcat-embed, spring-webflux…), so
  consumers only pull in what they annotate.

## 2.2 Package Layout

Base package: `io.microsphere.spring.*`. Package names mirror the Spring originals they extend:

| Microsphere package | Mirrors / purpose |
|---------------------|-------------------|
| `io.microsphere.spring.core.env` | `org.springframework.core.env` — listenable environment, env utils |
| `io.microsphere.spring.config.env` | property sources, YAML/JSON factories, env events |
| `io.microsphere.spring.beans.factory[.annotation]` | `org.springframework.beans.factory` — DI extensions, config binding, dependency graph |
| `io.microsphere.spring.context.annotation` | the annotation-driven import framework |
| `io.microsphere.spring.context.event` | event/listener extension, bean lifecycle events |
| `io.microsphere.spring.cache[.annotation]` | Spring Cache TTL extensions |
| `io.microsphere.spring.net` | `spring:` URL protocol handlers |
| `io.microsphere.spring.web.*` | web contracts: `metadata`, `method.support`, `rule`, `event`, `util` |
| `io.microsphere.spring.webmvc.*`, `io.microsphere.spring.web.servlet.*` | servlet/MVC implementation |
| `io.microsphere.spring.webflux.*` | reactive implementation |
| `io.microsphere.spring.jdbc.p6spy.*` | P6Spy wiring |
| `io.microsphere.spring.guice.annotation` | Guice bridge |
| `io.microsphere.spring.test.*` | test support by stack: `tomcat.embedded`, `jdbc.embedded`, `zookeeper.embedded`, `webmvc`, `webflux`, `util` |

## 2.3 The `@Enable*` Extension Pattern

This is the architecture idiom used by every feature; learn it once and you can read the whole codebase:

1. A public `@Enable<Xxx>` annotation carries the user-facing attributes and `@Import`s a
   **package-private** `ImportBeanDefinitionRegistrar` (the annotations are the stable API; registrars are internals).
2. Registrars extend the framework bases from `microsphere-spring-context`:
   `AnnotatedBeanCapableImportBeanDefinitionRegistrar<A>` / `AnnotatedBeanCapableImportSelector<A>` — which provide
   `BeanFactory`/`Environment` awareness and attribute access.
3. `@OverrideAnnotationAttributes` (with `OverrideAnnotationAttributesStrategy`, default
   `ConfigurationPropertyOverrideAnnotationAttributesStrategy`) lets a derived annotation override the meta-annotation's
   attributes — this is how `@EnableWebMvcExtension` / `@EnableWebFluxExtension` re-expose `@EnableWebExtension`
   attributes via `@AliasFor`.
4. `@ImportOptional` tolerates missing classes on the classpath (keeps modules optional-dependency-friendly).
5. Kill-switches: registrars honor `EnvironmentEnabled` / properties of the form
   `microsphere.spring.<feature>.enabled` (see §2.4), so features can be disabled without code changes.

Automatic (no `@Enable*`) wiring happens only through `META-INF/spring.factories`
`ApplicationContextInitializer` entries in `microsphere-spring-context` and SPI entries per module (e.g.
`InjectionPointDependencyResolver`, `SpringWebHelper`, `WebEndpointMappingFactory`).

## 2.4 Configuration Property Namespace

All runtime switches use the `microsphere.spring.` prefix (constants in `*PropertyConstants` classes per module).
Notable properties:

| Property | Module | Purpose |
|----------|--------|---------|
| `microsphere.spring.beans.auto-registered` (default `true`) | context | disable `@EnableAutoRegistrationBean` bean auto-registration |
| `microsphere.spring.pre-instantiation.singletons.threads` / `.thread.name-prefix` | context | parallel pre-instantiation pool |
| `microsphere.spring.listenable-autowire-candidate-resolver.enabled` | context | enable listenable `AutowireCandidateResolver` |
| `microsphere.spring.filter-mode` | context | `SEQUENTIAL` / `CONDITIONAL` condition-evaluation mode |
| `microsphere.spring.configuration-property.repository.max-size` | context | `ConfigurationPropertyRepository` cache bound |
| `microsphere.spring.context-initializer.<beanName>.enabled` | context | per-initializer switch |
| `microsphere.jdbc.p6spy.excluded-datasource-beans` | jdbc | DataSources to skip when wrapping with P6Spy |
| `microsphere.jdbc.p6spy.options.*` | jdbc | P6Spy options via Spring `PropertySources` |
| `microsphere.spring.webmvc.content-negotiation.*` | webmvc | bind to `ContentNegotiationManagerFactoryBean` |
| `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | webmvc | restrict view resolution to one resolver |
| `microsphere.spring.web.*` | web | web extension switches |

## 2.5 How webmvc and webflux Share `web`

- `web` defines: `WebEndpointMapping` (+ registry/resolver/factory/filter SPI), interception SPI
  (`HandlerMethodInterceptor`, `HandlerMethodArgumentInterceptor`, `HandlerMethodAdvice`), request-rule expressions
  (`WebRequestRule`, media/name-value expressions), events, and the `SpringWebHelper` abstraction over request access.
- `@EnableWebExtension` registers the generic infrastructure; the stack-specific `@EnableWebMvcExtension` /
  `@EnableWebFluxExtension` add their own registrars which register stack resolvers
  (`HandlerMappingWebEndpointMappingResolver`), the weaver (`InterceptingHandlerMethodProcessor`), and optional
  pieces (`ReversedProxyHandlerMapping`, storing advices/interceptors).
- At runtime, `WebRequestUtils` dispatches to `SpringWebMvcHelper` or `SpringWebFluxHelper` discovered through
  `spring.factories`; `UnknownSpringWebHelper` is the safe fallback.

---
Previous: [1. Overview](./01-overview.md) · Next: [3. Setup & Quick Start](./03-setup-and-quick-start.md)
