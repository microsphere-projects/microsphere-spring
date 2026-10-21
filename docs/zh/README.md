# Microsphere Spring — 文档总索引

[English](../README.md) · **简体中文**

本目录收录
[`microsphere-spring`](https://github.com/microsphere-projects/microsphere-spring)（Microsphere
系列项目的 Spring Framework 扩展库）的全部文档，按版本线与 Maven 模块组织，分为三套：

| 文档集 | 用途 | 位置 |
|---|---|---|
| [用户文档（User Guide）](../user-guide/zh/README.md) | 各模块能力、公开 API、代码示例、版本信息与兼容范围 | [`docs/user-guide/`](../user-guide/zh/README.md) |
| [开发者文档（Developer Guide）](../developer-guide/zh/README.md) | 架构设计、源码分析、设计模式、复杂流程图 | [`docs/developer-guide/`](../developer-guide/zh/README.md) |
| [源码差异（Source Diff）](../diff/source/zh/main-diff-0.2.39-vs-0.1.39.md) | 最新两个已发布 tag 之间的逐模块实现差异，`src/main` 与 `src/test` 分文档记录 | [`docs/diff/source/`](../diff/source/zh/) |

## 目录结构

```
docs/
├── README.md                 # English 总索引
├── zh/README.md              # 本文件（简体中文总索引）
├── user-guide/
│   ├── README.md             # User Guide index (English)
│   ├── zh/README.md          # 用户文档索引（简体中文）
│   ├── current/              # release 分支（0.2.x）英文
│   │   └── zh/               # release 分支（0.2.x）中文
│   └── 1.x/                  # release-1.x 分支（0.1.x）英文
│       └── zh/               # release-1.x 分支（0.1.x）中文
├── developer-guide/
│   ├── README.md             # Developer Guide index (English)
│   ├── zh/README.md          # 开发者文档索引（简体中文）
│   ├── current/              # release 分支（0.2.x）英文
│   │   └── zh/               # release 分支（0.2.x）中文
│   └── 1.x/                  # release-1.x 分支（0.1.x）英文
│       └── zh/               # release-1.x 分支（0.1.x）中文
└── diff/
    └── source/               # tag 间源码差异（英文）
        ├── main-diff-0.2.39-vs-0.1.39.md
        ├── test-diff-0.2.39-vs-0.1.39.md
        └── zh/               # 简体中文
            ├── main-diff-0.2.39-vs-0.1.39.md
            └── test-diff-0.2.39-vs-0.1.39.md
```

**语言约定**：英文为默认语言，存放在规范路径（无 `zh/` 前缀）；简体中文版本使用**完全相同的文件名**，
存放在平行的 `zh/` 目录中，两个版本在页首互相链接。

**分支约定**：仅覆盖两个已发布分支：

| 分支 | 目录名 | 版本线 | Java | 命名空间 | 默认 Spring |
|---|---|---|---|---|---|
| `release` | `current/` | 0.2.x（已发布 `0.2.39`，开发中 `0.2.40-SNAPSHOT`） | 17 | `jakarta.*` | Spring Framework 7.0.9 |
| `release-1.x` | `1.x/` | 0.1.x（已发布 `0.1.39`，开发中 `0.1.40-SNAPSHOT`） | 8 | `javax.*` | Spring Framework 5.3.39 |

## 模块覆盖范围

每套文档、每种语言都为每个 Maven 模块提供独立文档：

| Maven 模块 | 职责 |
|---|---|
| `microsphere-spring-parent` | 通过 Profile 选择 Spring / Reactor BOM，管理第三方版本 |
| `microsphere-spring-dependencies` | 应用导入的 BOM，统一所有模块版本 |
| `microsphere-spring-context` | Environment、属性源、宽松绑定、TTL 缓存、容器扩展 |
| `microsphere-spring-web` | 与传输方式无关的端点元数据模型与拦截 SPI |
| `microsphere-spring-webmvc` | Servlet / Spring MVC 适配层 |
| `microsphere-spring-webflux` | 响应式 / WebFlux 适配层 |
| `microsphere-spring-jdbc` | 基于 Spring 属性驱动的 P6Spy `DataSource` 包装 |
| `microsphere-spring-guice` | 单向桥接：Spring Bean 上的 Guice `@Inject` 注入点由 Spring 容器满足 |
| `microsphere-spring-test` | 内嵌 Tomcat / ZooKeeper / 数据库的测试支撑 |
| `microsphere-spring-compatible` | **仅 1.x** —— 面向 Spring 4.3 的 `org.springframework.*` 兼容垫片 |

模块之间的依赖关系以 Mermaid 流程图绘制，总图见
[开发者文档总览](../developer-guide/zh/README.md#模块依赖关系图)，各模块视角的依赖图见每篇模块文档开头。

## 阅读建议

- **评估或使用本库** → 从[用户文档 — current](../user-guide/current/zh/README.md)（0.2.x）或
  [用户文档 — 1.x](../user-guide/1.x/zh/README.md)（0.1.x）开始。
- **扩展、评审或维护本库** → 从[开发者文档 — current](../developer-guide/current/zh/README.md) 或
  [开发者文档 — 1.x](../developer-guide/1.x/zh/README.md) 开始。
- **判断应使用哪条版本线** → 先看上文分支表，再看
  [用户文档 current/README.md](../user-guide/current/zh/README.md) 中的跨分支差异表。
- **逐行对比两条版本线的实现** → 阅读
  [`0.2.39` vs `0.1.39` 源码差异](../diff/source/zh/main-diff-0.2.39-vs-0.1.39.md)，
  `src/main` 与 `src/test` 各一篇，均按 Maven 模块组织。

## 标记约定

| 标记 | 含义 |
|---|---|
| **必需（Required）** | compile 作用域依赖，会传递引入。 |
| **可选（Optional）** | 模块 pom 中声明为 `optional`，需由应用自行提供。 |
| **容器提供（Provided）** | 由容器或平台在运行时提供。 |
| ⚠ / *注意* | 实际行为与注解名称或根 README 描述不一致之处。 |
| 流程图 | Mermaid 图，GitHub 与任何支持 Mermaid 的 Markdown 渲染器均可显示。 |

版本号、全限定类名、注解属性名、配置键与 Maven 坐标在中英文文档中保持完全一致，仅正文语句做了语言转换。
