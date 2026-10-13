# microsphere-spring-compatible（1.x 独有）

[← 1.x 版本总览](./README.md) · [English](../compatible.md) · [开发者文档](../../../developer-guide/1.x/zh/compatible.md) · [0.2.x 文档](../../current/zh/README.md) —— 该模块仅存在于 1.x 版本线

## 版本信息

| 项目 | 值 |
|------|-----|
| artifactId | `microsphere-spring-compatible` |
| 分支 | **仅存在于 `release-1.x`**（0.2.x 分支没有该模块） |
| 分支上版本 | `0.1.39` |
| 开发中版本 | `0.1.40-SNAPSHOT` |
| Java 版本 | **8** |
| Spring Framework 兼容范围 | 面向 **4.3.x – 5.0.x**（补齐 5.1+ 才有的类） |
| 打包 | `jar` |
| 依赖 | 仅 `com.google.code.findbugs:jsr305`（`optional`） |

> ⚠️ **该模块不发布到 Maven Central**：其 `pom.xml` 显式配置了
> ```xml
> <plugin>
>     <groupId>org.apache.maven.plugins</groupId>
>     <artifactId>maven-deploy-plugin</artifactId>
>     <configuration><skip>true</skip></configuration>
> </plugin>
> ```
> 并且 `microsphere-spring-dependencies` BOM **也没有**管理它的坐标。因此外部工程无法直接
> `dependency` 引用它——它只在本地 reactor 构建（4.3 / 5.0 profile）时生效。

## 1. 它解决什么问题

本库的源码大量使用了 Spring 5.1+ 才引入或变为 public 的 API。为了在 **Spring 4.3 / 5.0** 上仍能编译并运行，
这个模块在 **`org.springframework` 包名下**提供了同名替代品——本质是"由库来提供的 mini backport"。

`microsphere-spring-context` 的 profile 声明了这层关系：

```xml
<profile>
    <id>spring-framework-4.3</id>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-compatible</artifactId>
            <version>${revision}</version>
            <optional>true</optional>
        </dependency>
    </dependencies>
</profile>
```

（`spring-framework-5.0` profile 同样追加该依赖；5.1 / 5.2 / 5.3 不追加。）

## 2. 内容清单

模块总共 **15 个源文件**，全部落在 Spring 的包里：

| 类 | 类型 | 补齐自 | 关键成员 |
|----|------|--------|----------|
| `org.springframework.core.env.Profiles` | `@FunctionalInterface` | Spring 5.1 | `boolean matches(Predicate<String> activeProfiles)`；静态 `of(String... profileExpressions)` |
| `org.springframework.core.env.ProfilesParser` | 包私有 `final class` | Spring 5.1 | `static Profiles parse(String...)`，内部实现 `and`/`or`/`!` 表达式解析与 `assertWellFormed` 校验 |
| `org.springframework.lang.NonNull` | 注解 | Spring 4.3 之后普及 | 空安全标注 |
| `org.springframework.lang.Nullable` | 注解 | 同上 | ⚠ 本库源码没有任何文件导入它——代码统一使用 `io.microsphere.annotation.Nullable`（74 个文件）；该垫片只是为了让被 fork 的类能自洽存在 |
| `org.springframework.util.Assert` | 工具类 | 5.x 版本增强方法 | 断言族 |
| `org.springframework.util.ClassUtils` | 工具类 | 5.x 增强 | 类加载/可赋值判断 |
| `org.springframework.util.CollectionUtils` | 工具类 | 5.x 增强 | 集合判空/合并 |
| `org.springframework.util.ConcurrentReferenceHashMap` | 容器 | 5.x 增强 | 软/弱引用缓存映射 |
| `org.springframework.util.MultiValueMap` | 接口 | 5.x 增强 | 多值映射 |
| `org.springframework.util.MultiValueMapAdapter` | 实现 | **Spring 5.3**（源码标注 `@since 5.3`；4.3 无此类） | `MultiValueMap` 的默认实现 |
| `org.springframework.util.ObjectUtils` | 工具类 | 5.x 增强 | 空安全比较/哈希 |
| `org.springframework.util.ReflectionUtils` | 工具类 | 5.x 增强 | 反射查找 |
| `org.springframework.util.StreamUtils` | 工具类 | 5.x 增强 | 流拷贝 |
| `org.springframework.util.StringUtils` | 工具类 | 5.x 增强 | `hasText`、`commaDelimitedCollectionToString` 等 |
| `org.springframework.package-info` | `package-info.java` | — | 包级注解/文档 |

> 命名规则：**包名与类名与真实 Spring 完全一致**，所以你的代码不需要任何条件判断即可编译。

## 3. 运行时行为与限制

由于只是"够用即可"的替代品：

1. **只能与 Spring 4.3 / 5.0 同时使用。** 若 classpath 上存在真实的 Spring 5.1+，同名类会出现两份定义，
   实际生效者由 classpath 顺序决定——这是**未定义行为**，可能表现为诡异的 `NoSuchMethodError` 或行为不一致。
2. **`Profiles` 的补齐不等于能力齐平。** 本库
   `io.microsphere.spring.core.env.ListenableConfigurableEnvironment` 在低于 Spring 5.1 的运行时里对
   `acceptsProfiles(Profiles)` 明确抛出：
   ```java
   throw new UnsupportedOperationException(
       "The method acceptsProfiles(Profiles) is not supported before Spring Framework 5.1");
   ```
   也就是说：`@ResourcePropertySource` 等常规路径在 4.3 上可用，但依赖 profile 表达式的新式 API 仍不可用。
3. **不可用于生产依赖声明。** 因为未部署（见版本信息），你只能在本地构建本仓库时使用它；
   若要在业务工程里锁定 4.3，请自行评估是否引入等价 backport。

## 4. 本地构建验证

```bash
# 切到 release-1.x 分支后
./mvnw -P spring-framework-4.3 clean verify     # 会编译并使用 compatible 模块
./mvnw -P spring-framework-5.0 clean verify
./mvnw -P spring-framework-5.3 clean verify     # 默认，不引用 compatible
```

由于 4.3 / 5.0 的构建依赖本地 reactor 中的该模块，请**整体构建**而不是只 `-pl microsphere-spring-context`：

```bash
./mvnw -P spring-framework-4.3 -pl microsphere-spring-parent,microsphere-spring-compatible,microsphere-spring-context -am clean install
```

## 5. 迁移到 0.2.x 时怎么处理

0.2.x 分支（Spring 6.0+）里这些 API 都由 Spring 原生提供，因此：

- 删除对 `microsphere-spring-compatible` 的一切引用；
- 保留 `org.springframework.lang.Nullable` / `NonNull` 的 import（现在指向真实 Spring）；
- 检查是否用到 `MultiValueMapAdapter`（Spring 5+ 提供）——无需替代。
