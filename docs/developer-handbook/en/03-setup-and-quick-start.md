[← Handbook index](./README.md)

# 3. Setup & Quick Start

## 3.1 Prerequisites

| Tool | Version | Verify |
|------|---------|--------|
| JDK | 17+ (CI tests 17 / 21 / 25) | `java -version` |
| Maven | 3.6+, or the bundled wrapper | `./mvnw --version` (wrapper 3.3.4 → Maven 3.9.16) |
| Git | any recent | `git --version` |
| IDE | IntelliJ IDEA recommended | — |

For **users of the library**, Spring Framework must be 6.0.x – 7.0.x (`main`/0.2.x line) or 4.3.x – 5.3.x
(`1.x`/0.1.x line).

## 3.2 Import the BOM

Add `microsphere-spring-dependencies` to `<dependencyManagement>`; after that, module dependencies need no version.

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
```

The BOM manages exactly these 7 artifacts: `microsphere-spring-context`, `-web`, `-webmvc`, `-webflux`, `-jdbc`,
`-guice`, `-test`.

## 3.3 Add Only the Modules You Need

```xml
<dependencies>
    <!-- core: property sources, listenable env, TTL cache, config binding -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-context</artifactId>
    </dependency>

    <!-- Spring MVC extensions (pulls microsphere-spring-web automatically) -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-webmvc</artifactId>
    </dependency>

    <!-- test utilities: test scope only -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Integration notes:

- **Spring Boot** works the same way — these are plain Spring Framework extensions; put the `@Enable*` annotations on
  any `@Configuration` class (or the Boot application class).
- Heavy third-party deps (p6spy, guice, tomcat-embed, zookeeper) are **not** transitive — add them yourself when you
  use the corresponding feature.

## 3.4 Five-Minute Smoke Test

```java
@Configuration
@EnableTTLCaching
@EnableWebMvcExtension                                  // in a web app
@ResourcePropertySource(name = "app", value = "classpath:/config/*.properties", autoRefreshed = true)
public class AppConfig {
}
```

```java
@Service
public class ProductService {

    @TTLCacheable(cacheNames = "products", expire = 300, timeUnit = TimeUnit.SECONDS)
    public Product findById(Long id) { /* ... */ }
}
```

Run the app, call the service twice within 300 s — the second call is served from cache; entries expire automatically.

## 3.5 Clone and Build the Repository (Contributors)

```bash
git clone https://github.com/microsphere-projects/microsphere-spring.git
cd microsphere-spring

./mvnw package -DskipTests   # environment check — expect BUILD SUCCESS
./mvnw verify                # full build + tests
```

IDE import: open the root `pom.xml` (IDEA: *File → Open → Open as Project*; Eclipse: *Import → Existing Maven
Projects*). The project uses `${revision}` versions — after changing module structure, re-import the Maven project
so IDE module resolution stays correct.

---
Previous: [2. Architecture](./02-architecture.md) · Next: [4. Build & Test](./04-build-and-test.md)
