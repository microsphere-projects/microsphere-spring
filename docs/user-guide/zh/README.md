# 用户文档（User Guide）

[← 文档总索引](../../zh/README.md) · [English](../README.md) · **简体中文** ·
[开发者文档](../../developer-guide/zh/README.md)

用户文档逐模块说明 `microsphere-spring` 的公开 API：模块能力、它引入的注解与 Bean、可直接运行的代码示例
（改写自各模块自身的测试用例）、已发布版本号，以及每条版本线所支持的 Spring Framework 与 Java 范围。

## 版本线

| 版本线 | 分支 | 文档目录 | Java | Servlet 命名空间 | Spring Framework 兼容范围 |
|---|---|---|---|---|---|
| **0.2.x**（当前） | `release` | [`current/`](../current/zh/README.md) | 17 | `jakarta.servlet.*` | 6.0.x – 7.0.x（已验证 6.0.23 / 6.1.21 / 6.2.18 / 7.0.9） |
| **0.1.x**（维护） | `release-1.x` | [`1.x/`](../1.x/zh/README.md) | 8 | `javax.servlet.*` | 4.3.x – 5.3.x（已验证 4.3.30.RELEASE / 5.0.20.RELEASE / 5.1.20.RELEASE / 5.2.25.RELEASE / 5.3.39） |

请按照你构建所使用的分支选择对应目录。两个目录中的文件名完全一致，比较同一模块在不同版本线的差异时，
只需替换路径中的目录名。

## 模块清单

### current — 0.2.x（[`current/`](../current/zh/README.md)）

| 模块 | English | 简体中文 |
|---|---|---|
| `microsphere-spring-parent` | [parent.md](../current/parent.md) | [中文](../current/zh/parent.md) |
| `microsphere-spring-dependencies` | [dependencies.md](../current/dependencies.md) | [中文](../current/zh/dependencies.md) |
| `microsphere-spring-context` | [context.md](../current/context.md) | [中文](../current/zh/context.md) |
| `microsphere-spring-web` | [web.md](../current/web.md) | [中文](../current/zh/web.md) |
| `microsphere-spring-webmvc` | [webmvc.md](../current/webmvc.md) | [中文](../current/zh/webmvc.md) |
| `microsphere-spring-webflux` | [webflux.md](../current/webflux.md) | [中文](../current/zh/webflux.md) |
| `microsphere-spring-jdbc` | [jdbc.md](../current/jdbc.md) | [中文](../current/zh/jdbc.md) |
| `microsphere-spring-guice` | [guice.md](../current/guice.md) | [中文](../current/zh/guice.md) |
| `microsphere-spring-test` | [test.md](../current/test.md) | [中文](../current/zh/test.md) |

### 1.x — 0.1.x（[`1.x/`](../1.x/zh/README.md)）

| 模块 | English | 简体中文 |
|---|---|---|
| `microsphere-spring-parent` | [parent.md](../1.x/parent.md) | [中文](../1.x/zh/parent.md) |
| `microsphere-spring-dependencies` | [dependencies.md](../1.x/dependencies.md) | [中文](../1.x/zh/dependencies.md) |
| `microsphere-spring-context` | [context.md](../1.x/context.md) | [中文](../1.x/zh/context.md) |
| `microsphere-spring-web` | [web.md](../1.x/web.md) | [中文](../1.x/zh/web.md) |
| `microsphere-spring-webmvc` | [webmvc.md](../1.x/webmvc.md) | [中文](../1.x/zh/webmvc.md) |
| `microsphere-spring-webflux` | [webflux.md](../1.x/webflux.md) | [中文](../1.x/zh/webflux.md) |
| `microsphere-spring-jdbc` | [jdbc.md](../1.x/jdbc.md) | [中文](../1.x/zh/jdbc.md) |
| `microsphere-spring-guice` | [guice.md](../1.x/guice.md) | [中文](../1.x/zh/guice.md) |
| `microsphere-spring-test` | [test.md](../1.x/test.md) | [中文](../1.x/zh/test.md) |
| `microsphere-spring-compatible` | [compatible.md](../1.x/compatible.md) | [中文](../1.x/zh/compatible.md) |

## 每篇模块文档包含的内容

1. **版本信息** —— artifactId、所属分支、已发布与开发中的版本号、Java 基线、Spring Framework 兼容范围，
   以及依赖姿态表（必需 / 可选 / 容器提供）。
2. **API 说明** —— 每个公开注解的属性、类型、默认值与语义，以及模块注册的 Bean 与配置键。
3. **代码示例** —— 改写自各模块 `src/test/java` 下的真实测试用例，保证示例可编译、可运行。
4. **兼容性说明** —— 在不同 Spring 版本之间、以及 0.1.x 与 0.2.x 两条版本线之间的行为差异，
   并标注实现与注解名称不一致的注意点。

## 语言与路径约定

- 英文为默认语言，存放在规范路径（如 `current/context.md`）。
- 简体中文存放在平行的 `zh/` 目录，文件名完全一致（如 `current/zh/context.md`），两文在页首互相链接。
- Maven 坐标、全限定类名、注解属性名、配置键与版本号在中英文文档中保持一致。
