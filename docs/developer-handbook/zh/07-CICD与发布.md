[← 手册目录](./README.md)

# 7. CI/CD 与发布

## 7.1 分支模型

| 分支 | 用途 |
|------|------|
| `main` | 开发主干；Spring 6.0.x–7.0.x、Java 17+、`jakarta.*` |
| `dev` | 集成分支——`main` 自动合入 |
| `release` | 发布分支——推送到此分支触发发布到 Maven Central；`main` 自动合入 |
| `1.x` | 旧版本线（Spring 4.3/5.3、Java 8、`javax.*`）——**不自动同步**，需手工回溯 |

合入 `main` 的变更会自动传播到 `dev` 与 `release`，因此不要直接向 `dev`/`release` 提交。
Fork 场景下 `sync-branches-from-upstream.yml` 每 5 分钟运行一次，仅在 fork 分支**未领先**于上游时以
`--force-with-lease` 同步。

## 7.2 工作流清单（`.github/workflows/`）

| 工作流 | 触发条件 | 行为 |
|--------|----------|------|
| `maven-build.yml` | push `main`、`dev`；PR 目标 `main`、`dev`、`release` | 矩阵 **JDK 17/21/25 × Spring profile 6.0/6.1/6.2/7.0**（12 个组合）：`mvn --batch-mode --update-snapshots -Drevision=0.0.1-SNAPSHOT test --activate-profiles test,coverage,<profile>`；每个组合都会上传覆盖率到 Codecov（`codecov-action@v7.1.1`，slug `microsphere-projects/microsphere-spring`） |
| `maven-publish.yml` | push `release`；或 `workflow_dispatch` 并传入必填 `revision` | 作业 `build`：校验版本匹配 `^[0-9]+\.[0-9]+\.[0-9]+$`，JDK 17，`./mvnw -Drevision=<v> -Dgpg.skip=true deploy --activate-profiles publish,ci` 发布到 OSSRH（`MAVEN_USERNAME`/`MAVEN_PASSWORD` 取自 `OSS_SONATYPE_*`；签名用 `SIGN_KEY_ID`/`SIGN_KEY`/`SIGN_KEY_PASS`）。作业 `release`（依赖 build）：打并推 `<revision>` tag、用 GitHub Models（`gpt-4o`）生成发布说明追加进 `release-notes.md`、`gh release create --latest`、把根 `pom.xml` 的 `revision` 递增为下一个 `-SNAPSHOT`、提交 `chore: bump version to next patch after publishing <rev>`、最后把 `origin/release` 以 `--no-ff` 合回 `main`（`[skip ci]`），冲突则报错终止 |
| `merge-main-to-branches.yml` | push `main` | 对 `dev`、`release` 分别 `git merge --no-ff origin/main` 并推送（分支不存在则跳过，冲突 exit 1） |
| `sync-branches-from-upstream.yml` | push `main`/`dev`/`dev-1.x`；定时 `*/5 * * * *` | 仅 fork 生效的上游分支同步（见 §7.1） |
| `wiki-publish.yml` | push `main` 且改动 `*/src/main/java/**/*.java` 或生成脚本；`workflow_dispatch` | 运行 `.github/scripts/generate-wiki-docs.py --output wiki-output` → 发布到 `<repo>.wiki` 独立 git 仓库 |

`.github/dependabot.yml`：Maven 每日检查（最多 10 个开放 PR），GitHub Actions 每周。
`.github/prompts/` 存放仓库自动化使用的 Copilot 提示词（README、API 文档、单元测试、入职计划、代码评审、代码讲解）。

## 7.3 CI 常见的失败原因

- 任一矩阵组合失败（只兼容新 Spring 的写法会让 12 个组合中的 3 个变红）——本地用
  `./mvnw verify -P spring-framework-6.0` 复现。
- 仅在某个 JDK 上通过的测试（反射/`--add-opens`、Locale、路径分隔符差异）。
- 在模块 POM 里直接写依赖版本而不是放父 POM（enforcer 与评审都会拦）。
- 新增库构件却忘了加入 `microsphere-spring-dependencies`。
- 破坏 `${revision}` 机制（硬编码版本，或提交了 `.flattened-pom.xml`）。

## 7.4 发布流程（维护者）

两条等价路径：

1. 合入 `main`，由 `merge-main-to-branches.yml` 传播到 `release`，随后以目标版本推送（或手动触发）。
2. 在 `Maven Publish` 上 `workflow_dispatch`，`revision` 填 `major.minor.patch`（默认占位
   `${major}.${minor}.${patch}` 必须替换）。

工作流负责：发布到 Central、打 tag、AI 起草发布说明追加到 `release-notes.md`、创建标记 `--latest` 的 GitHub
Release、递增下一个 `-SNAPSHOT`、`release` → `main` 回 merge。请人工复核自动生成的发布说明提交——Copilot 起草的分节
（New Features / Bug Fixes / Documentations / Dependency Updates / Test Improvements / Build and Workflow
Enhancements / Other Changes）有时需要追加修订。

所需仓库密钥：`OSS_SONATYPE_USERNAME`、`OSS_SONATYPE_PASSWORD`、`OSS_SIGNING_KEY_ID_LONG`、`SIGN_KEY`、
`SIGN_KEY_PASS`、`CODECOV_TOKEN`、`WORKFLOW_TOKEN`。

## 7.5 本地演练

```bash
# 校验待发布版本号格式，并确认 POM 能被 flatten
./mvnw -Drevision=0.2.40 flatten:flatten help:effective-pom -pl microsphere-spring-context | head -40

# 发布前的全矩阵检查
for p in spring-framework-6.0 spring-framework-6.1 spring-framework-6.2 spring-framework-7.0; do
  ./mvnw -q verify -P $p || echo "FAILED $p"
done
```

---
上一章：[6. 编码规范](./06-编码规范.md) · 下一章：[8. 故障排查](./08-故障排查.md)
