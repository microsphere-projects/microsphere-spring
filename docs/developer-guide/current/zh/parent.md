# Parent POM `microsphere-spring-parent`

[← 开发者文档总览](./README.md) · [English](../parent.md) · [用户文档](../../../user-guide/current/zh/parent.md)

---

## 定位

`microsphere-spring-parent` 是**构建侧**。它是仓库根 POM，也是每个模块（以及任何下游项目）都应继承的 parent。它提供插件管理、发布配置、Java 版本和源码 jar——也就是让 `mvn deploy` 真正能端到端跑通的那部分。

- **继承**：本仓库的每一个模块，以及使用本生态的下游项目。
- **打包方式**：`pom`（根目录 `microsphere-spring-build`）。
- **它管插件，不管版本**——版本属于 `microsphere-spring-dependencies`（BOM），见[依赖管理](./dependencies.md)。parent 导入该 BOM，使受管版本对继承者可用。

面向的读者：*维护模块发布，或构建任何依赖它们的项目的开发者。*

---

## Parent 提供什么

| 关注点 | Parent 提供的 | 为什么重要 |
|--------|--------------|-----------|
| Java 版本 | 编译与测试目标固定在 **Java 17** | 每个模块编译出的字节码保持一致 |
| 元数据 | `name`、`description`、`url`、`organization`、`licenses`、`developers`、`scm` 从继承处向下传播 | jar 与 POM 携带正确的 provenance（来源信息） |
| 构建插件 | 编译、源码 jar、Javadoc、PGP 签名、中央发布、FTP、SSH 外部（wagon）、版本管理、git commit id 的 `<pluginManagement>` | 模块**无需**钉插件版本即可构建 |
| 发布 | 自动激活 `release`；带 `--activate-profiles central` 的 deploy；`-DretryFailedDeploymentCount` | `mvn deploy` 端到端可用 |
| 属性 | `${revision}`（版本）、`${maven.deploy.retry.count}`、`${nexusurl}`，以及从 BOM 导入中拾取的 `${microsphere-spring-dependencies.version}` | 单一版本真相来源 |

> **约定，而非机制。** 没有强制性的 parent 检查。约定是下游项目*应当*继承 `microsphere-spring-parent`；不这样做也能构建，但模块就失去了版本与 provenance 保证。

---

## 继承 Parent

下游项目只需 parent，不写任何其他东西：

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-parent</artifactId>
    <version>2.0.0</version>
</parent>

<groupId>com.acme</groupId>
<artifactId>app</artifactId>
<version>1.0.0</version>
```

parent 导入 `microsphere-spring-dependencies`，因此每个 BOM 管理的依赖都能不带版本地解析。若你的项目需要继承*另一个* parent，那就改为导入 BOM，见[依赖管理](./dependencies.md)。

> **注意 `<version>`。** 本手册撰写时最新已发布的 parent 是 `2.0.0`；请查看 [Maven Central](https://search.maven.org/search?q=g:io.github.microsphere-projects%20a:microsphere-spring-parent) 上的新版本。`${revision}` 属性是**父 POM 内部**使用的（配合 flatten-maven-plugin），不是给下游引用者用的。

---

## 模块如何继承

仓库内的每个模块都声明**完全相同**的 parent 块：

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-parent</artifactId>
    <version>${revision}</version>
</parent>
```

`${revision}`（例如 `2.0.0`）在**构建时**解析，并通过 flatten-maven-plugin 写入安装/发布出去的 POM。因此模块 POM 里没有硬编码的版本号；整棵版本树只有一个真相来源。

**加一个模块**——新目录，一个 POM 带上面的 parent 块加 `<artifactId>`，再在根 POM 的 `<modules>` 中列出它：

```xml
<modules>
    <module>microsphere-spring-context</module>
    <module>microsphere-spring-web</module>
    <module>microsphere-spring-webmvc</module>
    <module>microsphere-spring-webflux</module>
    <module>microsphere-spring-guice</module>
    <module>microsphere-spring-jdbc</module>
    <module>microsphere-spring-test</module>
    <module>microsphere-spring-sid</module>   <!-- 新模块 -->
</modules>
```

**去掉一个模块**——把它从 `<modules>` 中移除（并删除目录）。它就不再进入 reactor。

---

## 插件管理与版本同步

`<pluginManagement>` 为构建、发布与部署工具钉住插件版本：

| 插件 | 版本 | 用途 |
|------|------|------|
| `maven-compiler-plugin` | （Boot BOM） | Java 17 源码与目标 |
| `maven-source-plugin` | 3.4.0 | jar 与 javadoc jar（始终附加） |
| `nexus-staging-maven-plugin` | 1.7.0 | 经 OSSRH staging 部署 |
| `maven-gpg-plugin` | 1.6 | 签名制品 |
| `flatten-maven-plugin` | （版本化管理） | 解析 `${revision}` 与 `${sha1}` |
| `maven-release-plugin` | （版本化管理） | `autoVersionSubmodules=true`、tag `@{project.version}` |
| `maven-scm-provider-gitexe` | （版本化管理） | release 插件使用的 SCM provider |
| `maven-versions-plugin`（versions-maven-plugin） | 2.21.0 | 版本管理 goal |
| `ftp-wagon` / `ssha-external-wagon` | 3.5.2 / 0.4.0 | FTP 与 SSH 外部部署传输 |

> **同步机制**：插件版本来自根 POM 的 `<properties>`；依赖版本来自 BOM。两者都*不是*从 Spring Boot 的 parent 继承而来——本 parent 刻意继承 `spring-boot-dependencies` BOM，而不是 `spring-boot-starter-parent`，以便插件版本仍由我们掌控，而依赖版本仍与 Boot 矩阵一致。

**加一个插件到模块**——不带 `<version>`：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

> **版本漂移检查**：如果你给某个模块添加的插件在 `<pluginManagement>` 中没有条目，Maven 会告警并选取一个最新默认版本。这会造成构建不可复现。请改为把版本加到根 POM 的 `<pluginManagement>` 里。

---

## 发布 profile

`release` profile 带有 `<activation><activeByDefault>true</activeByDefault></activation>`，**并且**在 `mvn deploy` 时自动激活。它会用 `-DretryFailedDeploymentCount`（默认来自 `${maven.deploy.retry.count}` = `5`）配置 deploy，并在属性 `${nexusurl}` 存在时配置 OSSRH staging。

下游项目很少需要触碰它——它是给发布流程用的。

---

## 版本策略

### 生态版本线

各模块一起版本化，所以一次生态发布意味着每个模块都升到同一 `${revision}`。一个模块的破坏性变更，会带动整个生态。

### 与 Spring Boot 对齐

Spring Boot 的版本由 BOM（以及它所导入的 Boot BOM）管理。parent 不声明 Boot 版本；它只是让继承者能看到 BOM 管理的版本。支持的 Boot 版本由 BOM 决定。请阅读[依赖管理](./dependencies.md)。

### 给模块打破坏性变更标记

破坏性变更时，把 Maven **major** 版本递增（例如 `2.x` → `3.x`）——这由生态版本的 major 位完成。次要版本（`2.0.x`）必须保持向后兼容。

### 快照与正式版本

正式版本发布到 OSSRH，快照发布到其快照仓库。本 parent 不为正式版本添加 `-parameters` 或类似的构建差异——字节码在两种模式下是一致的。

---

## 打 tag 与发布

parent 配置了 `autoVersionSubmodules`，因此 release 流程一次就对整棵树生效。发布配置见根 `pom.xml` 的 release 插件设置。
