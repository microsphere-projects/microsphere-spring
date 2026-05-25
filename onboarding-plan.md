# Onboarding Plan — Microsphere Spring

> **Audience:** Experienced developer new to the Microsphere Spring stack
> **Repository:** [microsphere-projects/microsphere-spring](https://github.com/microsphere-projects/microsphere-spring)

---

## Phase 1 — Foundation

### Goals
Set up a working local environment and identify the most important documentation to read first.

### Step-by-step Environment Setup

1. **Install prerequisites**
   - Java 17 or later (`java -version` to verify)
   - Maven 3.6+ — or use the bundled wrapper (`./mvnw --version`)
   - Git (`git --version`)
   - An IDE with strong Java/Spring support (IntelliJ IDEA recommended; Eclipse or VS Code with Java extensions also work)

2. **Clone the repository**
   ```bash
   git clone https://github.com/microsphere-projects/microsphere-spring.git
   cd microsphere-spring
   ```

3. **Import into your IDE**
   - IntelliJ IDEA: *File → Open* → select the root `pom.xml` → *Open as Project*
   - Eclipse: *File → Import → Existing Maven Projects* → select the root directory
   - Wait for the IDE to download dependencies and index sources

4. **Build the project**
   ```bash
   # Linux / macOS
   ./mvnw package -DskipTests

   # Windows
   mvnw.cmd package -DskipTests
   ```
   A clean `BUILD SUCCESS` at the end means your environment is ready.

5. **Run the full test suite**
   ```bash
   ./mvnw verify
   ```
   All tests should pass on `main`.

### Troubleshooting Tips

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| `java: error: release version 17 not supported` | IDE/compiler using Java < 17 | Set the project SDK to Java 17 in IDE settings |
| `Could not resolve dependencies` | Corporate proxy or offline Maven cache | Add proxy settings to `~/.m2/settings.xml` or run `./mvnw -U verify` to force update |
| Tests fail locally but pass in CI | OS-specific path or locale differences | Run `./mvnw verify -Dfile.encoding=UTF-8` |
| `mvnw: Permission denied` | Missing execute bit | `chmod +x mvnw` |

### Priority Documentation to Read First

1. **[README.md](./README.md)** — features overview, module table, usage examples, and build instructions
2. **[CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)** — community norms before any contribution
3. **[release-notes.md](./release-notes.md)** — recent changes and versioning context
4. **[DeepWiki interactive docs](https://deepwiki.com/microsphere-projects/microsphere-spring)** — ask questions directly against the codebase
5. **[Javadoc for microsphere-spring-context](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-context)** — start here; it is the core module everything else depends on

---

## Phase 2 — Exploration

### Goals
Navigate the codebase confidently, understand workflows, and identify beginner-friendly tasks.

### Module Map

Start by reading each module's `pom.xml` and `src/` tree. The recommended reading order is:

| Order | Module | Why start here |
|-------|--------|----------------|
| 1 | `microsphere-spring-context` | Core bean lifecycle, property sources, TTL caching — everything else builds on this |
| 2 | `microsphere-spring-test` | Testing utilities you'll use in every PR |
| 3 | `microsphere-spring-web` | Shared web abstractions (MVC + WebFlux share this) |
| 4 | `microsphere-spring-webmvc` or `microsphere-spring-webflux` | Pick the stack you'll work on first |
| 5 | `microsphere-spring-jdbc` | Isolated P6Spy integration — good self-contained read |
| 6 | `microsphere-spring-guice` | Smallest module; good for understanding the extension pattern |

### Hands-on Codebase Discovery Exercises

1. **Trace a feature end-to-end**
   - Pick `@ResourcePropertySource` (defined in `microsphere-spring-context`)
   - Find the annotation, the processor/post-processor that handles it, and the matching test class
   - Grep tip: `grep -r "ResourcePropertySource" microsphere-spring-context/src --include="*.java" -l`

2. **Run a single module's tests**
   ```bash
   ./mvnw test -pl microsphere-spring-context
   ```
   Read the test output — JUnit test names reveal intended behaviour.

3. **Read a test as documentation**
   - Tests in `microsphere-spring-test` show how `EmbeddedTomcatContextLoader` and `@EnableEmbeddedDatabase` are used
   - Tests in `microsphere-spring-context` demonstrate property source loading, TTL caching, and environment events

4. **Browse CI workflows**
   - `.github/workflows/maven-build.yml` — understand what the CI gate checks before you submit a PR

### Beginner-Friendly First Tasks (Suggested)

Since there are no open issues at the time this plan was written, here are task *types* well-suited for an experienced developer new to this stack:

| Task type | Where to look | Why it's beginner-friendly |
|-----------|---------------|----------------------------|
| **Improve Javadoc** | Any public class/method with sparse or missing `/** */` comments | No functional risk; forces you to read the code deeply |
| **Add a missing test** | Classes with low Codecov coverage (see [Codecov dashboard](https://app.codecov.io/gh/microsphere-projects/microsphere-spring)) | Validates your understanding without changing production logic |
| **Improve README or module-level docs** | Each module's `src/main/resources` or root `README.md` | Low risk, immediately visible value |
| **Reproduce + document a bug** | Search [GitHub Discussions](https://github.com/microsphere-projects/microsphere-spring/discussions) for questions without clear resolution | Builds credibility and opens a real issue |
| **Write an integration test** | `microsphere-spring-test` utilities make this easy | Shows you understand the testing framework |

> **Tip:** Before starting any task, open a [GitHub Discussion](https://github.com/microsphere-projects/microsphere-spring/discussions) to check if someone is already working on it.

### Key Resources

- [GitHub Issues](https://github.com/microsphere-projects/microsphere-spring/issues) — watch for newly opened issues
- [GitHub Discussions](https://github.com/microsphere-projects/microsphere-spring/discussions) — community Q&A
- [Codecov](https://app.codecov.io/gh/microsphere-projects/microsphere-spring) — find under-tested areas

---

## Phase 3 — Integration

### Goals
Make your first contribution, learn team processes, and build confidence through early wins.

### Learning Team Processes

1. **Read the contributing guide** in [README.md § Contributing](./README.md#contributing):
   - Fork → feature branch from `main` → write/update tests → open a PR
   - CI must pass (Maven + JUnit) before merge

2. **Understand branching**
   - `main` — Spring Framework 6.x/7.x
   - `1.x` — Spring Framework 4.x/5.x
   - Target the right branch for your change

3. **Follow the commit message style** used in recent commits (`git log --oneline -20`)

4. **Watch existing PRs** on GitHub to understand the review style and expectations before submitting your own

### Making Your First Contribution

Follow this incremental sequence for maximum confidence:

**Win 1 — Documentation (Day 1–3)**
- Pick one public API with sparse Javadoc in `microsphere-spring-context`
- Add or improve the `/** */` comment
- Open a PR with a short description — this establishes you as a contributor

**Win 2 — Test coverage (Day 4–7)**
- Find a class with low or no test coverage using the Codecov dashboard
- Add a focused JUnit 5 test using the helpers from `microsphere-spring-test`
- Run `./mvnw verify` locally before pushing

**Win 3 — Small bug fix or feature (Week 2+)**
- Pick an open issue (or open one yourself from your exploration notes)
- Discuss approach in the issue before coding
- Follow the fork → branch → PR workflow

### Recommended Learning Resources

| Resource | Purpose |
|----------|---------|
| [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/) | Deepen understanding of core Spring concepts used throughout |
| [Spring Framework Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/) | Understand the base classes Microsphere Spring extends |
| [DeepWiki for this repo](https://deepwiki.com/microsphere-projects/microsphere-spring) | Ask "how does X work?" questions against the actual codebase |
| [Maven Getting Started Guide](https://maven.apache.org/guides/getting-started/) | Understand the build tool if Maven is new to you |
| [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) | Testing patterns used throughout the project |

### Confidence-Building Checklist

- [ ] Local build passes (`./mvnw verify`)
- [ ] Read README, CODE_OF_CONDUCT, and release notes
- [ ] Traced one feature end-to-end through source and tests
- [ ] Ran a single module's tests and understood the output
- [ ] Opened or commented on a GitHub Discussion
- [ ] Submitted first PR (documentation or test improvement)
- [ ] First PR merged ✓
- [ ] Submitted a PR with a small code change
- [ ] Reviewed someone else's PR

---

## Quick Reference

```bash
# Build (skip tests for speed)
./mvnw package -DskipTests

# Full build + tests
./mvnw verify

# Single module tests
./mvnw test -pl microsphere-spring-context

# Generate Javadoc locally
./mvnw javadoc:javadoc -pl microsphere-spring-context
```

**Key contacts:**
- Lead architect: [Mercy Ma (@mercyblitz)](https://github.com/mercyblitz) — mercyblitz@gmail.com
- Community: [GitHub Discussions](https://github.com/microsphere-projects/microsphere-spring/discussions)
