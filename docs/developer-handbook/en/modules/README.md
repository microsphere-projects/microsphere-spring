[← Handbook index](../README.md)

# 5. Module Guides — Index

Deep, per-module references: public annotations (with attribute defaults), SPI interfaces, key classes, how each
feature is enabled, and the configuration properties involved.

| Order | Module | Guide | What's inside |
|-------|--------|-------|---------------|
| 1 | `microsphere-spring-context` | [context.md](./context.md) | property sources & listenable environment, configuration bean binding, TTL caching, bean dependency graph & parallel instantiation, event extension, annotation-import framework, `spring:` URL protocol, converters |
| 2 | `microsphere-spring-web` | [web.md](./web.md) | `WebEndpointMapping` metadata model, handler-method interception SPI, request rules/expressions, `SpringWebHelper`, events |
| 3 | `microsphere-spring-webmvc` | [webmvc.md](./webmvc.md) | `@EnableWebMvcExtension` full attribute table, MVC interception weaver, endpoint resolvers, reverse-proxy mapping, servlet filters/listeners, utils |
| 4 | `microsphere-spring-webflux` | [webflux.md](./webflux.md) | `@EnableWebFluxExtension`, reactive weaver & WebFilters, `ServerWebRequest`, functional-endpoint visitors, metadata resolvers |
| 5 | `microsphere-spring-jdbc` | [jdbc.md](./jdbc.md) | `@EnableP6DataSource`, DataSource wrapping, P6Spy options via Environment, `p6spy://` URL |
| 6 | `microsphere-spring-guice` | [guice.md](./guice.md) | `@EnableGuice`, injection bridge, the minimal extension pattern |
| 7 | `microsphere-spring-test` | [test.md](./test.md) | embedded Tomcat / database / ZooKeeper, MVC & WebFlux test bases, request fixtures, `@Conditional` test helpers |
| — | parent & dependencies POMs | [parent-and-dependencies.md](./parent-and-dependencies.md) | version management, Spring compatibility profiles, published BOM |

**Suggested reading order for contributors**: context → test → web → webmvc *or* webflux → jdbc → guice
(same recommendation as the repository [onboarding plan](../../../onboarding-plan.md)).

Handbook: [← 4. Build & Test](../04-build-and-test.md) · [6. Coding Conventions →](../06-coding-conventions.md)
