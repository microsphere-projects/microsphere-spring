[← Handbook index](./README.md)

# 1. Overview

**Microsphere Spring** is a modular library of Spring Framework extensions that solves real-world challenges in
production Spring applications. Every capability is a drop-in addition — it enhances an existing Spring application
without replacing any infrastructure, and all features are opt-in via `@Enable*` annotations.

## 1.1 Feature Summary

| Feature | Module | Entry API |
|---------|--------|-----------|
| Parallel bean pre-instantiation (faster startup) | context | `ParallelPreInstantiationSingletonsBeanFactoryListener` |
| Listenable `Environment` (intercept property resolution & profile events) | context | `EnvironmentListener`, `PropertyResolverListener` |
| Enhanced `@PropertySource`: wildcards, ordering, inheritance, auto-refresh | context | `@ResourcePropertySource` |
| YAML / JSON property sources | context | `@YamlPropertySource`, `@JsonPropertySource` |
| Default-properties source injection | context | `@DefaultPropertiesPropertySource` |
| Configuration bean binding (prefix-based POJO binding as beans) | context | `@EnableConfigurationBeanBinding` |
| Per-entry TTL caching | context | `@EnableTTLCaching` + `@TTLCacheable` / `@TTLCachePut` |
| Event & listener interception, async listening, bean lifecycle events | context | `@EnableEventExtension`, `BeanListener`, `ApplicationEventInterceptor` |
| `spring:` URL protocol (access resources/env via URLs) | context | `SpringProtocolURLStreamHandler` |
| Web endpoint registry (metadata from MVC, WebFlux, Servlet) | web + webmvc/webflux | `WebEndpointMapping`, `@EnableWeb*Extension` |
| Handler-method interception (AOP-style controller hooks) | web + webmvc/webflux | `HandlerMethodInterceptor`, `HandlerMethodArgumentInterceptor` |
| Reverse-proxy handler shortcut (skip re-matching by endpoint id) | webmvc / webflux | `ReversedProxyHandlerMapping` |
| P6Spy JDBC/SQL monitoring wrapping existing DataSources | jdbc | `@EnableP6DataSource` |
| Google Guice `@Inject` bridged into Spring lifecycle | guice | `@EnableGuice` |
| Embedded Tomcat / embedded DB / embedded ZooKeeper test support | test | `@EmbeddedTomcatConfiguration`, `@EnableEmbeddedDatabase`, `@EmbeddedZookeeperServer` |

## 1.2 Branches and Versions

| Branch | Spring Framework compatibility | Latest release | Notes |
|--------|-------------------------------|----------------|-------|
| `main` | 6.0.x – 7.0.x | `0.2.39` (`0.2.40-SNAPSHOT` in development) | Java 17+, Jakarta EE (jakarta.* namespace) |
| `1.x`  | 4.3.x – 5.3.x | `0.1.39` | Java 8+ / javax.* namespace |

Release versioning is semantic (`0.2.x`); the Maven groupId is `io.github.microsphere-projects` for all artifacts.

## 1.3 Who This Handbook Is For

- **Application developers** — chapters 1–5 and 8 tell you how to add the modules and which API to use per feature.
- **Contributors / library developers** — chapters 2, 4, 6, 7 explain the architecture, build system, conventions,
  and release pipeline. Also see the repository's [onboarding-plan.md](../../onboarding-plan.md).

---
Next: [2. Architecture](./02-architecture.md)
