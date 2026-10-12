# User Guide

[← Documentation index](../README.md) · **English** · [简体中文](./zh/README.md) · [Developer Guide](../developer-guide/README.md)

The User Guide documents the public API of every `microsphere-spring` module: what the module does,
which annotations and beans it contributes, runnable code examples (adapted from the modules' own
test cases), the released versions, and the Spring Framework / Java ranges each version line supports.

## Version lines

| Version line | Branch | Guide directory | Java | Servlet namespace | Spring Framework range |
|---|---|---|---|---|---|
| **0.2.x** (current) | `release` | [`current/`](./current/README.md) | 17 | `jakarta.servlet.*` | 6.0.x – 7.0.x (6.0.23 / 6.1.21 / 6.2.18 / 7.0.9 verified) |
| **0.1.x** (maintenance) | `release-1.x` | [`1.x/`](./1.x/README.md) | 8 | `javax.servlet.*` | 4.3.x – 5.3.x (4.3.30.RELEASE / 5.0.20.RELEASE / 5.1.20.RELEASE / 5.2.25.RELEASE / 5.3.39 verified) |

Pick the directory that matches the branch you build against. The two directories contain the same
file names, so you can compare a module across version lines by swapping the directory in the path.

## Modules

### current — 0.2.x ([`current/`](./current/README.md))

| Module | English | 简体中文 |
|---|---|---|
| `microsphere-spring-parent` | [parent.md](./current/parent.md) | [中文](./current/zh/parent.md) |
| `microsphere-spring-dependencies` | [dependencies.md](./current/dependencies.md) | [中文](./current/zh/dependencies.md) |
| `microsphere-spring-context` | [context.md](./current/context.md) | [中文](./current/zh/context.md) |
| `microsphere-spring-web` | [web.md](./current/web.md) | [中文](./current/zh/web.md) |
| `microsphere-spring-webmvc` | [webmvc.md](./current/webmvc.md) | [中文](./current/zh/webmvc.md) |
| `microsphere-spring-webflux` | [webflux.md](./current/webflux.md) | [中文](./current/zh/webflux.md) |
| `microsphere-spring-jdbc` | [jdbc.md](./current/jdbc.md) | [中文](./current/zh/jdbc.md) |
| `microsphere-spring-guice` | [guice.md](./current/guice.md) | [中文](./current/zh/guice.md) |
| `microsphere-spring-test` | [test.md](./current/test.md) | [中文](./current/zh/test.md) |

### 1.x — 0.1.x ([`1.x/`](./1.x/README.md))

| Module | English | 简体中文 |
|---|---|---|
| `microsphere-spring-parent` | [parent.md](./1.x/parent.md) | [中文](./1.x/zh/parent.md) |
| `microsphere-spring-dependencies` | [dependencies.md](./1.x/dependencies.md) | [中文](./1.x/zh/dependencies.md) |
| `microsphere-spring-context` | [context.md](./1.x/context.md) | [中文](./1.x/zh/context.md) |
| `microsphere-spring-web` | [web.md](./1.x/web.md) | [中文](./1.x/zh/web.md) |
| `microsphere-spring-webmvc` | [webmvc.md](./1.x/webmvc.md) | [中文](./1.x/zh/webmvc.md) |
| `microsphere-spring-webflux` | [webflux.md](./1.x/webflux.md) | [中文](./1.x/zh/webflux.md) |
| `microsphere-spring-jdbc` | [jdbc.md](./1.x/jdbc.md) | [中文](./1.x/zh/jdbc.md) |
| `microsphere-spring-guice` | [guice.md](./1.x/guice.md) | [中文](./1.x/zh/guice.md) |
| `microsphere-spring-test` | [test.md](./1.x/test.md) | [中文](./1.x/zh/test.md) |
| `microsphere-spring-compatible` | [compatible.md](./1.x/compatible.md) | [中文](./1.x/zh/compatible.md) |

## What every module page contains

1. **Version information** — artifactId, branch, released and in-development versions, Java baseline,
   Spring Framework compatibility range, and the dependency posture table (required / optional /
   provided).
2. **API reference** — every public annotation with its attributes, types, defaults and semantics,
   plus the beans and property keys the module registers.
3. **Code examples** — adapted from the module's own test cases under
   `src/test/java`, so the snippets reflect code that actually compiles and runs.
4. **Compatibility notes** — behaviour that changes across Spring versions or between the 0.1.x and
   0.2.x lines, and the caveats where the implementation differs from the annotation name.

## Language and path conventions

- English is the default language and lives at the canonical path (`current/context.md`).
- Simplified Chinese lives in the parallel `zh/` directory with the identical file name
  (`current/zh/context.md`), and both files link to each other at the top of the page.
- Maven coordinates, FQCNs, annotation attributes, property keys and version numbers are identical in
  both languages.
