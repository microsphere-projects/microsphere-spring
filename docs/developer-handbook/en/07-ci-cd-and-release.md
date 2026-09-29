[← Handbook index](./README.md)

# 7. CI/CD & Release

## 7.1 Branch Model

| Branch | Purpose |
|--------|---------|
| `main` | development trunk; Spring 6.0.x–7.0.x, Java 17+, jakarta.* |
| `dev` | integration branch — `main` is auto-merged into it |
| `release` | publishing branch — pushing here triggers a Maven Central release; `main` is auto-merged into it |
| `1.x` | legacy line (Spring 4.3/5.3, Java 8, javax.*) — **not** auto-synced; back-port manually |

Merges into `main` are propagated to `dev` and `release` automatically, so never commit directly to `dev`/`release`.
For forks, `sync-branches-from-upstream.yml` runs every 5 minutes and force-syncs (`--force-with-lease`) each branch
from upstream, but only when the fork branch is **not** ahead.

## 7.2 Workflows (`.github/workflows/`)

| Workflow | Trigger | What it does |
|----------|---------|--------------|
| `maven-build.yml` | push `main`, `dev`; PR `main`, `dev`, `release` | Matrix **JDK 17/21/25 × Spring profiles 6.0/6.1/6.2/7.0** (12 legs) on ubuntu-latest: `mvn --batch-mode --update-snapshots -Drevision=0.0.1-SNAPSHOT test --activate-profiles test,coverage,<profile>`, then uploads coverage to Codecov on every leg (`codecov-action@v7.1.1`, slug `microsphere-projects/microsphere-spring`) |
| `maven-publish.yml` | push `release`; `workflow_dispatch` with required `revision` input | Job `build`: validates `^[0-9]+\.[0-9]+\.[0-9]+$`, JDK 17, `./mvnw -Drevision=<v> -Dgpg.skip=true deploy --activate-profiles publish,ci` to OSSRH (`MAVEN_USERNAME`/`MAVEN_PASSWORD` from `OSS_SONATYPE_*`; signing via `SIGN_KEY_ID`/`SIGN_KEY`/`SIGN_KEY_PASS`). Job `release` (needs build): tag + push `<revision>`, generate release notes with GitHub Models (`gpt-4o`) into `release-notes.md`, `gh release create --latest`, bump root `pom.xml` `revision` to the next `-SNAPSHOT`, commit `chore: bump version to next patch after publishing <rev>`, then merge `origin/release` back into `main` (`--no-ff`, `[skip ci]`) — fails loudly on conflict |
| `merge-main-to-branches.yml` | push `main` | For each of `dev`, `release`: `git merge --no-ff origin/main` and push (skips missing branches, exits 1 on conflict) |
| `sync-branches-from-upstream.yml` | push `main`/`dev`/`dev-1.x`; cron `*/5 * * * *` | Fork-only upstream sync (see §7.1) |
| `wiki-publish.yml` | push `main` touching `*/src/main/java/**/*.java` or the generator; `workflow_dispatch` | `.github/scripts/generate-wiki-docs.py --output wiki-output` → publish into the `<repo>.wiki` git repo |

`.github/dependabot.yml`: Maven daily (max 10 open PRs), GitHub Actions weekly.
`.github/prompts/` holds the Copilot prompt files used for repo automation (README, API docs, unit tests, onboarding,
code review, explain-code).

## 7.3 What CI Will Fail You For

- Any matrix leg red (a Spring 6.0-only incompatibility fails 3 of 12 legs) — reproduce locally with
  `./mvnw verify -P spring-framework-6.0`.
- A test that only passes on one JDK (reflection/`--add-opens`, locale, or path assumptions).
- Adding a dependency version directly in a module POM instead of the parent (enforcer / review).
- Forgetting to add a new artifact to `microsphere-spring-dependencies`.
- Breaking `${revision}` handling (hard-coded versions, or committing `.flattened-pom.xml`).

## 7.4 Releasing (Maintainers)

Two equivalent paths:

1. Merge to `main`, let `merge-main-to-branches.yml` propagate to `release`, then push (or dispatch) with the target
   version.
2. `workflow_dispatch` on `Maven Publish` with `revision` = `major.minor.patch` (the default placeholder
   `${major}.${minor}.${patch}` must be replaced).

The workflow handles: Central deployment, tag, AI-drafted release notes appended to `release-notes.md`, GitHub
Release marked `--latest`, next `-SNAPSHOT` bump, and `release` → `main` merge. Review the generated release notes
commit — Copilot-drafted sections (New Features / Bug Fixes / Documentations / Dependency Updates / Test Improvements /
Build and Workflow Enhancements / Other Changes) sometimes need a follow-up edit.

Required repository secrets: `OSS_SONATYPE_USERNAME`, `OSS_SONATYPE_PASSWORD`, `OSS_SIGNING_KEY_ID_LONG`,
`SIGN_KEY`, `SIGN_KEY_PASS`, `CODECOV_TOKEN`, `WORKFLOW_TOKEN`.

## 7.5 Local Dry Runs

```bash
# Verify a would-be release version formats correctly and the POMs flatten
./mvnw -Drevision=0.2.40 flatten:flatten help:effective-pom -pl microsphere-spring-context | head -40

# Full pre-publish check across the matrix
for p in spring-framework-6.0 spring-framework-6.1 spring-framework-6.2 spring-framework-7.0; do
  ./mvnw -q verify -P $p || echo "FAILED $p"
done
```

---
Previous: [6. Coding Conventions](./06-coding-conventions.md) · Next: [8. Troubleshooting](./08-troubleshooting.md)
