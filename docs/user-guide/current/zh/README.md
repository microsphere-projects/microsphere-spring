# Microsphere Spring · 当前版本用户指南（0.2.x）

[English](../README.md) · [用户文档总览](../../zh/README.md) · [开发者文档](../../../developer-guide/current/zh/README.md) · [1.x（0.1.x）文档](../../1.x/zh/README.md)

> 本目录文档对应 **`release` 分支**（0.2.x 版本线），其 `src/` 内容与 `main` 一致。
> 仍在上一维护线（0.1.x / `release-1.x` 分支）上的应用请改用 [../1.x/](../../1.x/zh/README.md)。

---

## 1. 版本信息

| 项目 | 值 |
|---|---|
| 分支 | `release`（`src/` 内容与 `main` 一致） |
| 开发版本 | `0.2.40-SNAPSHOT`（根 POM `<revision>`） |
| 最新已发布版本 | `0.2.39`（Maven Central） |
| Maven groupId | `io.github.microsphere-projects` |
| Java 版本 | **Java 17**（根 POM `<java.version>17</java.version>`） |
| Servlet 命名空间 | **`jakarta.servlet.*`**（Jakarta EE 11 BOM，无 `javax.servlet` 依赖） |
| 许可证 | Apache License 2.0 |

## 2. Spring Framework 兼容范围

本版本线通过 Maven profile 对以下 Spring Framework 版本逐一回归验证（`microsphere-spring-parent/pom.xml`）：

| Maven profile | Spring Framework | Project Reactor BOM | 状态 |
|---|---|---|---|
| `spring-framework-6.0` | 6.0.23 | 2022.0.22 | 已验证 |
| `spring-framework-6.1` | 6.1.21 | 2023.0.19 | 已验证 |
| `spring-framework-6.2` | 6.2.18 | 2024.0.17 | 已验证 |
| `spring-framework-7.0` | 7.0.9 | 2025.0.7 | **默认** |

**结论**：本分支产物适用于 **Spring Framework 6.0.x – 7.0.x** 与 **Java 17+**。仍在 Spring 5.3.x / `javax.servlet` 上的应用请改用 [0.1.x（`release-1.x`）](../../1.x/zh/README.md)。

## 3. 第三方依赖版本（父 POM 统一管理）

下表为随 `0.2.39` 发布的取值；Dependabot 会让 `main` / `release` 工作树比发布版新一个补丁（Jackson `2.22.3`、SnakeYAML `2.7`、ZooKeeper `3.9.6`）。

| 依赖 | 版本 | 关联特性 |
|---|---|---|
| microsphere-java | `0.3.19` | 全模块基础工具（compile 必需） |
| microsphere-logging | `0.2.2` | 日志门面与测试日志开关 |
| Jakarta EE BOM | `11.0.0` | Web / 测试模块（optional） |
| Jackson BOM | `2.22.2` | `@JsonPropertySource`、端点元数据 JSON（optional） |
| SnakeYAML | `2.6` | `@YamlPropertySource`（optional） |
| P6Spy | `3.9.1` | `@EnableP6DataSource`（optional） |
| Google Guice | `7.0.0` | `@EnableGuice`（optional；版本写在 `microsphere-spring-guice/pom.xml` 中，非父 POM 统一管理） |
| ZooKeeper / Curator | `3.9.5` / `5.9.0` | 内嵌 ZooKeeper 测试支持（optional） |

**依赖约定（重要）**：除 `microsphere-java-core` 与库内模块互相依赖为 `compile` 外，Spring、Jakarta、Jackson、SnakeYAML、P6Spy、Guice 等一律声明为 `optional`（或 `provided`）。即本库**不会传递引入 Spring**，宿主应用必须自备匹配版本的 Spring；只有实际使用某特性时，才需显式添加对应第三方依赖（如 P6Spy、Guice）。

## 4. 安装

先导入 BOM 统一管版本，再按需引入模块（无需写版本号）：

```xml
<properties>
    <microsphere-spring.version>0.2.39</microsphere-spring.version>
</properties>

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

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-context</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

`microsphere-spring-dependencies` 管理以下模块坐标：`context`、`guice`、`jdbc`、`web`、`webmvc`、`webflux`、`test`。

## 5. 模块依赖关系

```
microsphere-java-core
    └── microsphere-spring-context ──┬── microsphere-spring-jdbc    （需自备 p6spy）
                                     ├── microsphere-spring-guice   （需自备 guice 7.x）
                                     └── microsphere-spring-web ──┬── microsphere-spring-webmvc
                                                                 └── microsphere-spring-webflux
microsphere-spring-parent / microsphere-spring-dependencies ：仅 POM，无代码
microsphere-spring-test ：独立模块，第三方依赖全为 optional
```

## 6. 模块文档索引

| 文档 | 模块 | 内容 |
|---|---|---|
| [context.md](./context.md) | microsphere-spring-context | 属性源扩展、YAML/JSON、TTL 缓存、配置绑定、可监听 Environment、并行初始化、事件扩展 |
| [web.md](./web.md) | microsphere-spring-web | `WebEndpointMapping` 注册表、控制器方法拦截 SPI |
| [webmvc.md](./webmvc.md) | microsphere-spring-webmvc | `@EnableWebMvcExtension`、MVC 拦截器、网关反向代理 |
| [webflux.md](./webflux.md) | microsphere-spring-webflux | `@EnableWebFluxExtension`、响应式拦截 |
| [jdbc.md](./jdbc.md) | microsphere-spring-jdbc | `@EnableP6DataSource` SQL 监控 |
| [guice.md](./guice.md) | microsphere-spring-guice | `@EnableGuice` 注入桥接 |
| [test.md](./test.md) | microsphere-spring-test | 内嵌库/容器与测试工具 |
| [parent.md](./parent.md) | microsphere-spring-parent | 版本对齐、Spring profile、构建属性 |
| [dependencies.md](./dependencies.md) | microsphere-spring-dependencies | BOM 内容与管理语义 |

对应英文页面位于同级目录（去掉 `zh/` 前缀）。

## 7. 兼容性自测

```bash
./mvnw clean verify -P spring-framework-6.0   # Spring 6.0.23
./mvnw clean verify -P spring-framework-6.1   # Spring 6.1.21
./mvnw clean verify -P spring-framework-6.2   # Spring 6.2.18
./mvnw clean verify                           # 默认 Spring 7.0.9
```

## 8. 本版本线已知限制

- TTL 缓存**没有可用的落地实现**：`TTLRedisCacheWriterWrapper` 与 `TTLRedisConfiguration` 在源码中整体处于注释状态，此外没有任何代码读取 `TTLContext`。因此配合原生 CacheManager 使用时 `@TTLCacheable` 的 `expire` 会被忽略，必须自行接入支持 TTL 的 `Cache`/`CacheWriter`。
- `@EnableConfigurationBeanBinding` 自身**未在 spring.factories 中注册任何 `ConfigurationBeanAliasGenerator`**，因此中划线/下划线的宽松绑定默认不生效，需应用方自行声明。
- 0.2.x 源码中**不存在** `@Idempotent` 与 `RestControlMapping`，尽管旧版 README 曾提及。
- 本分支**没有** `microsphere-spring-compatible` 模块（该模块仅存在于 0.1.x 线）。
