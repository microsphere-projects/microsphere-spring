# Microsphere Spring · 维护线（0.1.x）用户指南

[← 1.x 版本总览](./README.md) · [English](../README.md) · [开发者文档](../../../developer-guide/1.x/zh/README.md) · [0.2.x 文档](../../current/zh/README.md)

> 本目录文档对应 **`release-1.x` 分支**（0.1.x 版本线）。
> 新版本线（0.2.x / `release` 分支）请见 [../../current/zh/](../../current/zh/README.md)。

---

## 1. 版本信息

| 项目 | 值 |
|---|---|
| 分支 | `release-1.x` |
| 开发版本 | `0.1.40-SNAPSHOT`（根 POM `<revision>`） |
| 最新已发布版本 | `0.1.39`（Maven Central） |
| Maven groupId | `io.github.microsphere-projects` |
| 构建父 POM | `microsphere-build:0.3.14`（本分支未声明 `java.version`，由父 POM 继承） |
| Java 版本 | **Java 8**（`maven.compiler.source/target/release = ${java.version} = 8`） |
| Servlet 命名空间 | **`javax.servlet.*`**（`javax.servlet-api` 4.0.1） |
| 内嵌 Tomcat | `tomcat-embed-core` **9.0.119**（父 POM 覆盖，适配 `javax.servlet`） |
| 模块数 | 9 个（比 0.2.x 多一个 `microsphere-spring-compatible`） |
| 许可证 | Apache License 2.0 |

## 2. Spring Framework 兼容范围

| Maven profile | Spring Framework | Project Reactor BOM | 状态 |
|---|---|---|---|
| `spring-framework-4.3` | 4.3.30.RELEASE | Bismuth-SR17 | 已验证（额外引入 `microsphere-spring-compatible`） |
| `spring-framework-5.0` | 5.0.20.RELEASE | Bismuth-SR17 | 已验证（额外引入 `microsphere-spring-compatible`） |
| `spring-framework-5.1` | 5.1.20.RELEASE | Californium-SR23 | 已验证 |
| `spring-framework-5.2` | 5.2.25.RELEASE | Dysprosium-SR25 | 已验证 |
| `spring-framework-5.3` | 5.3.39 | 2020.0.47 | **默认** |

**结论**：本版本线适用于 **Spring Framework 4.3.x – 5.3.x** 与 **Java 8+**。需要 Spring 6/7 或 Jakarta 命名空间请改用 [0.2.x（`release`）](../../current/zh/README.md)。

## 3. 第三方依赖版本

| 依赖 | 版本 | 说明 |
|---|---|---|
| microsphere-java | `0.3.19` | 唯一 compile 必需依赖来源 |
| microsphere-logging | `0.2.2` | 日志门面 |
| Jackson BOM | `2.22.2` | YAML/JSON 属性源、端点元数据 JSON（optional） |
| SnakeYAML | `2.6` | `@YamlPropertySource`（optional） |
| P6Spy | `3.9.1` | `@EnableP6DataSource`（optional） |
| Google Guice | `7.0.0` | `@EnableGuice`（optional，**版本线冲突见第 6 节**） |
| javax.servlet-api | `4.0.1` | Web/测试模块（optional/provided），版本由 `microsphere-java-dependencies` 管理 |
| Tomcat embed | `9.0.119` | 内嵌 Tomcat 测试（父 POM 覆盖） |
| ZooKeeper / Curator | `3.9.5` / `5.9.0` | 内嵌 ZooKeeper 测试（optional） |
| JUnit | 由 BOM 管理（分支内未显式声明） | web/webmvc/jdbc 测试基于 **JUnit 4**，webflux/test 的 WebFlux 基类基于 **JUnit 5** |

`microsphere-spring-dependencies` 仅管理 7 个已发布模块：`context`、`guice`、`jdbc`、`web`、`webmvc`、`webflux`、`test`（**不含** `microsphere-spring-compatible`）。

## 4. 安装

```xml
<properties>
    <microsphere-spring.version>0.1.39</microsphere-spring.version>
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
</dependencies>
```

Spring 依赖同样全部为 `optional`，宿主应用必须自备 Spring 4.3–5.3 与 `javax.servlet-api`。

## 5. 与 0.2.x 的关键差异

| 方面 | 0.1.x（本分支） | 0.2.x（`release`） |
|---|---|---|
| Spring / Java | 4.3.x – 5.3.x / Java 8 | 6.0.x – 7.0.x / Java 17 |
| Servlet | `javax.servlet` | `jakarta.servlet` |
| MVC 配置基类 | `WebMvcConfigurerAdapter`（Spring 4.3 兼容） | `WebMvcConfigurer` |
| Servlet 函数式端点适配 | **不存在**（仅 `webflux.function.server.*`） | 有 `web.servlet.function.*` 适配器 |
| `WebEndpointMappingVisitor` | **1.x 独有**（表达式式条件构建） | 已移除 |
| 传统 war 装配入口 | `META-INF/services/javax.servlet.ServletContainerInitializer` | `.../jakarta.servlet.ServletContainerInitializer` |
| 测试基类 | `AbstractWebMvcTest` 基于 JUnit 4（`@Ignore @RunWith(SpringRunner)`） | JUnit 5（`@Disabled`） |
| 内嵌 Tomcat 注解 | `@RunWith(SpringRunner)`，**仅 JUnit 4** | `@ExtendWith(SpringExtension)`，JUnit 5 |
| 额外模块 | `microsphere-spring-compatible`（低版本 Spring API 回填，不发布） | 无 |
| `PropertySourcesP6LoadableOptionsAdapter` | 存在但**未被任何代码引用**（死代码） | 同样需手动注册 |

## 6. 已知冲突与限制

1. **Guice 7.0.0 与 Java 8 冲突**：`microsphere-spring-guice` 声明 `guice.version = 7.0.0`，而 Guice 7 要求 Java 11+。在本分支（Java 8）上使用 `@EnableGuice` 需自行改用 Guice 5.x/4.x，或运行在 Java 11+ 上。
2. **Redis TTL 不可用**：`TTLRedisCacheWriterWrapper` / `TTLRedisConfiguration` 在本分支同样整体注释，且 pom 中没有 spring-data-redis 依赖。
3. **Spring 4.3 / 5.0 的 `Profiles` API**：`ListenableConfigurableEnvironment` 依赖 5.1 引入的 `org.springframework.core.env.Profiles`，通过 `microsphere-spring-compatible` 回填；在真正的 4.3/5.0 运行时，相关能力会抛 `UnsupportedOperationException("... not supported before Spring Framework 5.1")`。
4. **测试模块 profile 拼写问题**：`microsphere-spring-test` 内定义了名为 `spring-framework-4.3` 的 profile，但其 `spring-framework.version` 值为 `5.0.20.RELEASE`；用该 profile 验证 4.3 时请注意。
5. **`@EnableEmbeddedDatabase(port = ...)` 未生效**：本分支注册器同样不读取该属性。
6. **`microsphere-spring-compatible` 未发布**：`maven-deploy-plugin` 配置了 `<skip>true</skip>`，不在 BOM 中，仅供库内部在 4.3/5.0 profile 下可选引入。

## 7. 模块文档索引

| 文档 | 模块 | 内容 |
|---|---|---|
| [context.md](./context.md) | microsphere-spring-context | 属性源、YAML/JSON、TTL 缓存、配置绑定、可监听 Environment、并行初始化、事件扩展 |
| [web.md](./web.md) | microsphere-spring-web | `WebEndpointMapping`、注册表 SPI、方法拦截 SPI、`WebEndpointMappingVisitor` |
| [webmvc.md](./webmvc.md) | microsphere-spring-webmvc | `@EnableWebMvcExtension`、拦截器、网关反向代理、javax war 装配 |
| [webflux.md](./webflux.md) | microsphere-spring-webflux | `@EnableWebFluxExtension`、响应式拦截 |
| [jdbc.md](./jdbc.md) | microsphere-spring-jdbc | `@EnableP6DataSource` |
| [guice.md](./guice.md) | microsphere-spring-guice | `@EnableGuice` 与 Java 8/Guice 7 冲突说明 |
| [test.md](./test.md) | microsphere-spring-test | JUnit 4 内嵌库/容器与测试工具 |
| [compatible.md](./compatible.md) | microsphere-spring-compatible | 低版本 Spring API 回填（1.x 独有） |
| [parent.md](./parent.md) | microsphere-spring-parent | 父 POM 的版本锁定、第三方属性键与 Spring profile 矩阵 |
| [dependencies.md](./dependencies.md) | microsphere-spring-dependencies | 应用导入的 BOM：受管理的模块坐标与传递依赖说明 |

## 8. 兼容性验证

```bash
./mvnw clean verify                          # 默认 Spring 5.3.39
./mvnw clean verify -P spring-framework-5.2  # Spring 5.2.25.RELEASE
./mvnw clean verify -P spring-framework-5.1  # Spring 5.1.20.RELEASE
./mvnw clean verify -P spring-framework-5.0  # Spring 5.0.20.RELEASE（引入 compatible）
./mvnw clean verify -P spring-framework-4.3  # Spring 4.3.30.RELEASE（引入 compatible）
```
