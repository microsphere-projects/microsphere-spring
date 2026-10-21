# Microsphere Spring — Documentation Index

**English** · [简体中文](./zh/README.md)

This directory holds the full documentation set for
[`microsphere-spring`](https://github.com/microsphere-projects/microsphere-spring), the Spring
Framework extension library of the Microsphere projects.

Three guide sets are provided, each organised by version line and by Maven module:

| Guide | Purpose | Location |
|---|---|---|
| [User Guide](./user-guide/README.md) | What each module offers, public APIs, code examples, version and compatibility information | [`docs/user-guide/`](./user-guide/README.md) |
| [Developer Guide](./developer-guide/README.md) | Architecture design, source-code analysis, design patterns, flow charts of the complex flows | [`docs/developer-guide/`](./developer-guide/README.md) |
| [Source Diff](./diff/source/main-diff-0.2.39-vs-0.1.39.md) | Module-by-module implementation diff between the two latest released tags, `src/main` and `src/test` in separate documents | [`docs/diff/source/`](./diff/source/) |

## Documentation layout

```
docs/
├── README.md                 # this file (English)
├── zh/README.md              # 简体中文总索引
├── user-guide/
│   ├── README.md             # User Guide index (English)
│   ├── zh/README.md          # 用户文档索引（简体中文）
│   ├── current/              # release branch (0.2.x) — English
│   │   └── zh/               # release branch (0.2.x) — 简体中文
│   └── 1.x/                  # release-1.x branch (0.1.x) — English
│       └── zh/               # release-1.x branch (0.1.x) — 简体中文
├── developer-guide/
│   ├── README.md             # Developer Guide index (English)
│   ├── zh/README.md          # 开发者文档索引（简体中文）
│   ├── current/              # release branch (0.2.x) — English
│   │   └── zh/               # release branch (0.2.x) — 简体中文
│   └── 1.x/                  # release-1.x branch (0.1.x) — English
│       └── zh/               # release-1.x branch (0.1.x) — 简体中文
└── diff/
    └── source/               # tag-to-tag source diff (English)
        ├── main-diff-0.2.39-vs-0.1.39.md
        ├── test-diff-0.2.39-vs-0.1.39.md
        └── zh/               # 简体中文
            ├── main-diff-0.2.39-vs-0.1.39.md
            └── test-diff-0.2.39-vs-0.1.39.md
```

**Language convention.** English is the default language and lives at the canonical path. Every
English document has a Simplified Chinese counterpart with the **same file name** inside a parallel
`zh/` directory, and both versions link to each other at the top of the page.

**Branch convention.** Only the two released branches are documented:

| Branch | Directory | Version line | Java | Namespace | Default Spring |
|---|---|---|---|---|---|
| `release` | `current/` | 0.2.x (`0.2.39` released, `0.2.40-SNAPSHOT` in development) | 17 | `jakarta.*` | Spring Framework 7.0.9 |
| `release-1.x` | `1.x/` | 0.1.x (`0.1.39` released, `0.1.40-SNAPSHOT` in development) | 8 | `javax.*` | Spring Framework 5.3.39 |

## Module coverage

One Markdown document per Maven module, in each guide set and each language:

| Maven module | Purpose |
|---|---|
| `microsphere-spring-parent` | Profile-driven Spring / Reactor BOM selection, managed third-party versions |
| `microsphere-spring-dependencies` | BOM imported by applications to align all module versions |
| `microsphere-spring-context` | Environment, property sources, relaxed binding, TTL caching, container extensions |
| `microsphere-spring-web` | Transport-neutral web endpoint metadata model and interception SPI |
| `microsphere-spring-webmvc` | Servlet / Spring MVC adapters of the web kernel |
| `microsphere-spring-webflux` | Reactive / WebFlux adapters of the web kernel |
| `microsphere-spring-jdbc` | P6Spy-backed `DataSource` decoration driven by Spring properties |
| `microsphere-spring-guice` | One-way bridge: Guice `@Inject` sites on Spring beans are satisfied by the Spring container |
| `microsphere-spring-test` | Embedded Tomcat / ZooKeeper / database test scaffolding |
| `microsphere-spring-compatible` | **1.x only** — `org.springframework.*` compatibility shims for Spring 4.3 |

The inter-module dependency graph is drawn as Mermaid flow charts in
[the Developer Guide overview](./developer-guide/README.md#module-dependency-graph) and, at module
granularity, at the top of each module document.

## How to read these docs

- **Evaluating or using the library** → start with
  [User Guide — current](./user-guide/current/README.md) (0.2.x) or
  [User Guide — 1.x](./user-guide/1.x/README.md) (0.1.x).
- **Extending, reviewing or maintaining the library** → start with
  [Developer Guide — current](./developer-guide/current/README.md) or
  [Developer Guide — 1.x](./developer-guide/1.x/README.md).
- **Deciding which version line applies to you** → see the branch table above, then
  [User Guide — current/README.md](./user-guide/current/README.md) which keeps a cross-branch
  difference table.
- **Comparing the two implementations line by line** → read the
  [`0.2.39` vs `0.1.39` source diffs](./diff/source/main-diff-0.2.39-vs-0.1.39.md), one document for
  `src/main` and one for `src/test`, each organised per Maven module.

## Conventions

| Marker | Meaning |
|---|---|
| **Required** | Compile-scope dependency; you get it transitively. |
| **Optional** | Declared `optional` in the module pom; your application must provide it. |
| **Provided** | Supplied by the container or platform at runtime. |
| `⚠` / *Caveat* | Behaviour that differs from the annotation's name or from the root README. |
| Flow chart | Mermaid diagram; rendered by GitHub and by any Mermaid-capable Markdown viewer. |

Version numbers, fully qualified class names, annotation attribute names, property keys and Maven
coordinates are identical in the English and Chinese documents; only prose is translated.
