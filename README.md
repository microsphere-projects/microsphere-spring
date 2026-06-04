# Microsphere Spring

> Microsphere Projects for Spring Framework

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-spring)
[![Maven Build](https://github.com/microsphere-projects/microsphere-spring/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-spring/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-spring/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-spring)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-spring.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-spring.svg)

Microsphere Spring is a modular library of Spring Framework extensions that solves real-world challenges in production
Spring applications. It enhances dependency injection, configuration management, web endpoint handling, event
processing, caching, and JDBC monitoring — all while remaining a drop-in addition to any existing Spring application.

## Table of Contents

- [Features](#features)
- [Modules](#modules)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Usage Examples](#usage-examples)
- [Building from Source](#building-from-source)
- [Documentation](#documentation)
- [Getting Help](#getting-help)
- [Contributing](#contributing)
- [Maintainers](#maintainers)
- [License](#license)

## Features

- **Parallel bean instantiation** — resolves bean dependency graphs and initializes independent singletons concurrently
  to reduce startup time
- **Listenable `Environment`** — intercept property resolution and profile activation events via `EnvironmentListener`
  and `PropertyResolverListener`
- **Enhanced `@PropertySource`** — `@ResourcePropertySource` adds wildcard resource patterns, ordering control,
  inheritance, auto-refresh on change, and built-in YAML/JSON support (`@YamlPropertySource`, `@JsonPropertySource`)
- **TTL caching** — `@EnableTTLCaching` / `@TTLCacheable` extend Spring Cache with per-entry time-to-live configuration,
  including Redis support
- **Web endpoint registry** — collects `WebEndpointMapping` metadata from Spring MVC, WebFlux, and classic Servlet at
  startup for introspection and routing
- **Handler method interception** — `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` provide AOP-style
  hooks around MVC and WebFlux controller invocations
- **P6Spy JDBC monitoring** — `@EnableP6DataSource` wraps existing `DataSource` beans transparently for SQL tracing
- **Google Guice integration** — `@EnableGuice` bridges Guice `@Inject` injection points into the Spring bean lifecycle
- **Rich testing utilities** — `EmbeddedTomcatContextLoader`, `EnableEmbeddedDatabase`, `AbstractWebFluxTest`, and
  servlet/MVC test helpers

## Modules

| Module                              | Purpose                                 | Key Annotations / APIs                                                                                                          |
|-------------------------------------|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| **microsphere-spring-parent**       | Parent POM with dependency management   | —                                                                                                                               |
| **microsphere-spring-dependencies** | External dependency BOM                 | —                                                                                                                               |
| **microsphere-spring-context**      | Core context & configuration extensions | `@ResourcePropertySource`, `@YamlPropertySource`, `@JsonPropertySource`, `@EnableTTLCaching`, `@EnableConfigurationBeanBinding` |
| **microsphere-spring-web**          | Shared web abstractions                 | `WebEndpointMapping`, `HandlerMethodInterceptor`, `@EnableWebExtension`, `@Idempotent`                                          |
| **microsphere-spring-webmvc**       | Spring MVC extensions                   | `@EnableWebMvcExtension`, `MethodHandlerInterceptor`, `ReversedProxyHandlerMapping`                                             |
| **microsphere-spring-webflux**      | Reactive web extensions                 | `@EnableWebFluxExtension`, `InterceptingHandlerMethodProcessor`, `ReversedProxyHandlerMapping`                                  |
| **microsphere-spring-jdbc**         | JDBC / P6Spy integration                | `@EnableP6DataSource`                                                                                                           |
| **microsphere-spring-guice**        | Google Guice bridge                     | `@EnableGuice`                                                                                                                  |
| **microsphere-spring-test**         | Testing utilities                       | `@EnableEmbeddedDatabase`, `EmbeddedTomcatContextLoader`, `AbstractWebFluxTest`                                                 |

## Prerequisites

- **Java 17** or later
- **Maven 3.6+** (or use the included `mvnw` / `mvnw.cmd` wrapper)
- Spring Framework **6.0.x – 7.0.x** (`main` branch) or **4.3.x – 5.3.x** (`1.x` branch)

## Getting Started

### 1. Import the BOM

Add `microsphere-spring-dependencies` to your `<dependencyManagement>` block so you never need to manage individual
module versions:

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-dependencies</artifactId>
            <version>${microsphere-spring.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Choose the version that matches your Spring Framework line:

| Branch | Spring Framework compatibility | Latest version |
|--------|--------------------------------|----------------|
| `main` | 6.0.x – 7.0.x                  | 0.2.22         |
| `1.x`  | 4.3.x – 5.3.x                  | 0.1.22         |

### 2. Add individual modules

Include only the modules you need — no version required after importing the BOM:

```xml

<dependencies>
    <!-- Core context enhancements -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-context</artifactId>
    </dependency>

    <!-- Spring MVC extensions (optional) -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-webmvc</artifactId>
    </dependency>
</dependencies>
```

## Usage Examples

### Auto-refreshable property source — `@ResourcePropertySource`

`@ResourcePropertySource` extends Spring's `@PropertySource` with wildcard patterns, ordering, inheritance, and live
reload when the underlying file changes.

```java

@Configuration
@ResourcePropertySource(
        name = "app-config",
        value = "classpath*:/META-INF/config/*.properties",
        autoRefreshed = true   // reload whenever any matched file changes
)
public class AppConfig {
    @Autowired
    private Environment environment;
}
```

### YAML and JSON property sources

```java

@Configuration
@YamlPropertySource("classpath:/config/application.yaml")
@JsonPropertySource("classpath:/config/feature-flags.json")
public class AppConfig {
}
```

### TTL caching — `@EnableTTLCaching` and `@TTLCacheable`

Add per-entry time-to-live to any Spring-managed cache (including Redis):

```java

@Configuration
@EnableTTLCaching
public class CachingConfig {
}

// In a service bean:
@TTLCacheable(cacheNames = "products", ttl = 300, timeUnit = TimeUnit.SECONDS)
public Product findById(Long id) { ...}
```

### Spring MVC extensions — `@EnableWebMvcExtension`

Enable handler method interception, web endpoint metadata collection, and more with a single annotation:

```java

@Configuration
@EnableWebMvcExtension(
        interceptHandlerMethods = true,   // wrap controller methods
        registerWebEndpointMappings = true // expose endpoint metadata at startup
)
public class WebConfig {
}
```

Implement `HandlerMethodInterceptor` to add cross-cutting behavior around any controller invocation:

```java

@Component
public class LoggingInterceptor implements HandlerMethodInterceptor {
    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, ...) {
        log.info("Invoking {}", handlerMethod.getMethod().getName());
    }
}
```

### P6Spy JDBC monitoring — `@EnableP6DataSource`

Wrap every existing `DataSource` bean for transparent SQL tracing without changing application code:

```java

@Configuration
@EnableP6DataSource
public class DataSourceConfig {
}
```

### Google Guice integration — `@EnableGuice`

Allow Guice `@Inject`-annotated fields to be satisfied by Spring-managed beans:

```java

@Configuration
@EnableGuice
public class IntegrationConfig {
}
```

## Building from Source

You don't need to build from source to use the library. Only do this if you want to try unreleased changes or contribute
to the project.

```bash
# Clone the repository
git clone https://github.com/microsphere-projects/microsphere-spring.git
cd microsphere-spring

# Linux / macOS
./mvnw package

# Windows
mvnw.cmd package
```

Run the full test suite:

```bash
# Linux / macOS
./mvnw verify

# Windows
mvnw.cmd verify
```

## Documentation

| Resource                    | Link                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Interactive docs (DeepWiki) | [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-spring)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| Interactive docs (Zread)    | [![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-spring) |
| GitHub Wiki                 | [microsphere-spring wiki](https://github.com/microsphere-projects/microsphere-spring/wiki)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| Release notes               | [release-notes.md](./release-notes.md)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

### JavaDoc

- [microsphere-spring-context](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-context)
- [microsphere-spring-web](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-web)
- [microsphere-spring-webmvc](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-webmvc)
- [microsphere-spring-webflux](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-webflux)
- [microsphere-spring-jdbc](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-jdbc)
- [microsphere-spring-guice](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-guice)
- [microsphere-spring-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-test)

## Getting Help

- **Bug reports and feature requests** — search
  the [existing issues](https://github.com/microsphere-projects/microsphere-spring/issues) first; if not
  found, [open a new issue](https://github.com/microsphere-projects/microsphere-spring/issues/new) and include your
  Spring and Java versions, a minimal reproducer, and the full stack trace if applicable
- **Questions and discussions** —
  use [GitHub Discussions](https://github.com/microsphere-projects/microsphere-spring/discussions)
- **Interactive documentation** — ask questions directly against the codebase
  via [DeepWiki](https://deepwiki.com/microsphere-projects/microsphere-spring)
  or [Zread](https://zread.ai/microsphere-projects/microsphere-spring)

## Contributing

Contributions are welcome! Please:

1. Read the [Code of Conduct](./CODE_OF_CONDUCT.md) before participating
2. Fork the repository and create a feature branch from `main`
3. Write or update tests for any changed behavior
4. Submit a pull request — the CI build (Maven + JUnit) must pass

## Maintainers

| Name                                      | Role                       | Contact              |
|-------------------------------------------|----------------------------|----------------------|
| [Mercy Ma](https://github.com/mercyblitz) | Lead architect & developer | mercyblitz@gmail.com |

The project is developed under the [Microsphere Projects](https://github.com/microsphere-projects) organisation.

## License

Microsphere Spring is released under the [Apache License 2.0](./LICENSE).
