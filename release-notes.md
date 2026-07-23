# Release Notes

## v0.2.10

# Release Notes for Version 0.2.10

## New Features
- **Automated Release Notes**: Added support for automated generation of release notes. ([b1c11aaa](https://github.com/microsphere-projects/repo/commit/b1c11aaa))

## Dependency Updates
- **microsphere-logging-dependencies**: Updated to `0.1.6`. ([0c165dda](https://github.com/microsphere-projects/repo/commit/0c165dda))
- **microsphere-java**: Updated to `0.2.8`. ([cf266e30](https://github.com/microsphere-projects/repo/commit/cf266e30))

## Build and Workflow Enhancements
- **Surefire Updates**: Disabled Surefire system classloader and removed related configuration. ([04b1edb3](https://github.com/microsphere-projects/repo/commit/04b1edb3), [cf31a585](https://github.com/microsphere-projects/repo/commit/cf31a585))
- **Sync Workflow Enhancements**: Improved branch sync functionality to handle forks ahead of upstream. ([0c63b065](https://github.com/microsphere-projects/repo/commit/0c63b065), [dc9ac97b](https://github.com/microsphere-projects/repo/commit/dc9ac97b))
- **Maven Publish Workflow**: Enhanced configurations for Surefire and model testing. ([a31eee73](https://github.com/microsphere-projects/repo/commit/a31eee73))
- **CI Quote Normalization**: Standardized quotes in workflow and configuration files. ([f7997e13](https://github.com/microsphere-projects/repo/commit/f7997e13), [e80fa373](https://github.com/microsphere-projects/repo/commit/e80fa373))
- **Dependabot Indentation Fixes**: Corrected indentation in `dependabot.yml`. ([38e92db1](https://github.com/microsphere-projects/repo/commit/38e92db1), [0a413226](https://github.com/microsphere-projects/repo/commit/0a413226))
- **EOF Newline Cleanup**: Removed unnecessary EOF newlines in specific scripts and workflows. ([093780cf](https://github.com/microsphere-projects/repo/commit/093780cf), [133a581c](https://github.com/microsphere-projects/repo/commit/133a581c))

## Test Improvements
- **Surefire Configuration**: Adjusted system classloader behavior to improve test isolation. ([04b1edb3](https://github.com/microsphere-projects/repo/commit/04b1edb3), [cf31a585](https://github.com/microsphere-projects/repo/commit/cf31a585))

## Other Changes
- Version bumped to prepare for development after release. ([1f145354](https://github.com/microsphere-projects/repo/commit/1f145354))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.9...0.2.10## v0.2.11

# Release Notes - Version 0.2.11  

## Dependency Updates
- Updated Maven Wrapper to version `3.9.14`.  
- Added a final newline to `maven-wrapper.properties` for improved configuration.  

## Build and Workflow Enhancements
- Removed `surefire.useSystemClassLoader` flag for streamlining test execution.  
- Bumped parent version to `0.2.7` for consistency.  

## Other Changes
- Added a comment for `P6Spy` in the parent POM for clarification.  

---  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.10...0.2.11## v0.2.13

# Release Notes for Version 0.2.13

## Dependency Updates
- Updated `spring-framework-bom` from `7.0.6` to `7.0.7`. ([#216](https://github.com/microsphere-projects/microsphere-spring/pull/216))
- Updated `reactor-bom` from `2025.0.4` to `2025.0.5`. ([#215](https://github.com/microsphere-projects/microsphere-spring/pull/215))
- Bumped `microsphere-java` to `0.2.9`.
- Bumped `microsphere-logging` to `0.1.7`.
- Updated Microsphere versions and imported BOMs.

## Build and Workflow Enhancements
- Switched Maven wrapper to the official Maven repository for improved compatibility.
- Updated Maven wrapper to version `3.9.15`.

## Bug Fixes
- Removed unnecessary blank lines in `applicationContext.xml`.
- Eliminated empty `contextConfigLocation` init-param in configurations.
- Added `contextConfigLocation` init-param to `web.xml` for improved initialization.

## Test Improvements
- Added support for Spring `6.2.17`, `6.2.18`, `7.0.6`, and `7.0.7`, and updated test ranges accordingly.

## Other Changes
- Removed unused Tomcat optional dependencies to streamline the build.

### [Full Changelog](https://github.com/microsphere-projects/microsphere-spring/compare/0.2.11...0.2.13)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.11...0.2.13## v0.2.14

# Release Notes for v0.2.14

## Dependency Updates
- Updated Microsphere versions and removed BOM imports. ([30f00fc2](<commit-link>))

## Other Changes
- Version bumped to 0.2.14 following release of 0.2.13. ([845f509d](<commit-link>))
- Merged `main` into `release` and vice versa. ([6c67a00d](<commit-link>), [b99d3b42](<commit-link>)) 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.13...0.2.14## v0.2.15

# Release Notes - Version 0.2.15

## New Features
- **WebSocket APIs**: Added support for WebSocket APIs with optional test dependencies. ([2b551f2b](https://github.com/user/repo/commit/2b551f2b))
- **Auto-Registration**:
    - Introduced `AutoRegistrationBean` interface and `EnableAutoRegistrationBean` for SPI-based auto-registration. ([14d7bc20](https://github.com/user/repo/commit/14d7bc20), [ad306cec](https://github.com/user/repo/commit/ad306cec))
    - Added constants for auto-registered beans configuration. ([7fd89ff1](https://github.com/user/repo/commit/7fd89ff1))
    - Implemented automatic registration for `InterceptingHandlerMethodProcessor`. ([496cf078](https://github.com/user/repo/commit/496cf078))
- **Spring MVC & WebFlux Improvements**:
    - Added `SimpleUrlHandlerMapping` test and updated related configurations. ([59fe4737](https://github.com/user/repo/commit/59fe4737))
    - Added MVC test utilities and refactored testing approach. ([3f3c911f](https://github.com/user/repo/commit/3f3c911f))
    - Introduced `AbstractWebFluxTest` and `WebFluxTest`. ([0783c476](https://github.com/user/repo/commit/0783c476)) 

## Test Improvements
- Consolidated test utilities into `microsphere-spring-test` module. ([e4d72165](https://github.com/user/repo/commit/e4d72165), [3b9a529e](https://github.com/user/repo/commit/3b9a529e))
- Introduced helpers for annotation attribute loading. ([7742fb9e](https://github.com/user/repo/commit/7742fb9e), [5728ef79](https://github.com/user/repo/commit/5728ef79), [c791ca6d](https://github.com/user/repo/commit/c791ca6d))
- Improved test structure:
    - Moved `@Test` annotations to concrete test classes. ([4bd0016f](https://github.com/user/repo/commit/4bd0016f))
    - Added unit tests for `PersonHandler`. ([6756fe16](https://github.com/user/repo/commit/6756fe16))
    - Added tests for `EnableAutoRegistrationBean`. ([fee35c29](https://github.com/user/repo/commit/fee35c29))
    - Refactored test assertions for `BeanCapable` candidates. ([bb331320](https://github.com/user/repo/commit/bb331320))

## Dependency Updates
- Updated `com.fasterxml.jackson:jackson-bom` from `2.21.2` to `2.21.3`. ([d29a9277](https://github.com/user/repo/commit/d29a9277))

## Documentation
- Updated README to include branch compatibility and version details. ([a4eb0ee4](https://github.com/user/repo/commit/a4eb0ee4))

## Build and Workflow Enhancements
- Merged main branch changes into release branch. ([3c7812e2](https://github.com/user/repo/commit/3c7812e2), [6087e219](https://github.com/user/repo/commit/6087e219), [98a79609](https://github.com/user/repo/commit/98a79609))
- Increased version to next patch post-release. ([a26ceb18](https://github.com/user/repo/commit/a26ceb18))

---

**Note**: For a detailed view of all changes, refer to the [Full Changelog](https://github.com/user/repo/compare/0.2.14...0.2.15).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.14...0.2.15## v0.2.16

# Release Notes for v0.2.16

## Dependency Updates
- Bumped `microsphere-logging` to v0.1.10.  
- Updated Microsphere module and parent versions.  

## Build and Workflow Enhancements
- Adjusted Maven workflows and appended EOF in scripts.  
- Updated `maven-publish.yml`.  

## Documentation
- Updated `README` to reflect bumped branch versions.  

## Other Changes
- Removed `logback-test.xml` from the webmvc module.  

---

Thank you for using our library! 🚀

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.15...0.2.16## v0.2.17

# Release Notes for v0.2.17

## New Features
- Introduced `generateBeanName` methods for improved default bean name generation.  
- Updated usage to leverage `Set` and `BeanUtils` for factory bean name handling.  

## Bug Fixes
- Fixed variable case in `setup-java` step of the workflow configuration.

## Documentation
- Revamped README with comprehensive content, formatted text, tables, and badges for improved clarity.  

## Dependency Updates
- Bumped `microsphere-logging` to v0.1.11.  
- Bumped `microsphere-java` to v0.3.3.  

## Test Improvements
- Removed redundant and idempotent test classes:
  - `IdempotentConfig`
  - `testRegisterSpringFactoriesBeans`
  - Return-value assertion in `BeanInitializerTest`.  

## Build and Workflow Enhancements
- Updated branch names in README table for consistency.  

## Other Changes
- Various chore updates and merges to streamline the project structure and align branches `[skip ci]`.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.16...0.2.17## v0.2.18

# Release Notes for Version 0.2.18

## New Features
- Added `@SpringLoggingTest` annotation to `WebRequestUtilsTest`. ([e6fa00bd](https://github.com/microsphere-projects/microsphere/commit/e6fa00bd))
- Introduced `onboarding-plan.md` for new team members onboarding. ([227b1fde](https://github.com/microsphere-projects/microsphere/commit/227b1fde))

## Bug Fixes
- Addressed resource leak, silent exception, and `ThreadLocal` memory leak based on code review feedback. ([aecf7a97](https://github.com/microsphere-projects/microsphere/commit/aecf7a97))

## Dependency Updates
- Updated `microsphere-logging` to `0.1.12`. ([3d58c28f](https://github.com/microsphere-projects/microsphere/commit/3d58c28f))
- Bumped `microsphere-java` to `0.3.4`. ([8c1eac83](https://github.com/microsphere-projects/microsphere/commit/8c1eac83))

## Test Improvements
- Added unit tests for 8 previously untested classes in `microsphere-spring-context`. ([1c75a784](https://github.com/microsphere-projects/microsphere/commit/1c75a784))

## Documentations
- Personalization of onboarding prompt values. ([234a88da](https://github.com/microsphere-projects/microsphere/commit/234a88da))
- Added `.github` prompt templates to streamline development workflows. ([e08f7c90](https://github.com/microsphere-projects/microsphere/commit/e08f7c90))

## Other Changes
- Removed redundant YAML resource test. ([c93dedc2](https://github.com/microsphere-projects/microsphere/commit/c93dedc2))
- Various merge and version bump commits. 

--- 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.17...0.2.18## v0.2.19

# Release Notes for Version 0.2.19

## New Features
- **Utility Methods**: Added `nullSafeBeanClassLoader` and `nullSafeClassLoader` utilities with documentation and tests.  
- **Enhancements**: Introduced `BeanSource` enum with corresponding unit tests.  

## Bug Fixes
- Removed excessive and trailing blank lines across various Java files.  
- Fixed formatting for `@ContextConfiguration` classes.  
- Resolved duplicate JavaDoc tags and improved consistency.  

## Documentation
- Enhanced JavaDoc descriptions for `@Referenced` annotation.  
- Added missing `@author` and `@since` JavaDoc tags to all Java files.  

## Dependency Updates
- Upgraded `microsphere-java` to version **0.3.5**.  
- Upgraded `microsphere-logging` to version **0.1.13**.  
- Updated project parent version to **0.3.0**.  

## Test Improvements
- Replaced repetitive list/set/map instantiations with utility methods (`ListUtils`, `SetUtils`, `MapUtils`).  
- Added tests for new `ClassLoader` utility methods.  
- Improved test cleanliness and formatting (normalized JavaDocs, removed unnecessary lines).  

## Build and Workflow Enhancements
- Updated README to reflect new version (**0.2.19** and **0.1.19**).  
- Regular merges from `main` branch into `release` to maintain consistency.  

## Other Changes
- General code cleanup: Removed unused imports, optimized code formatting, and applied Apache headers to test files.  

**Full Changelog**: View the [GitHub Changelog](https://github.com/microsphere-projects/microsphere/compare/0.2.18...0.2.19) for more details. 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.18...0.2.19## v0.2.20

# Release Notes - Version 0.2.20

## Dependency Updates
- **microsphere-logging**: Bumped to version `0.1.14`. ([0275c6e](#))
- **microsphere-java**: Bumped to version `0.3.6`. ([8ef7276](#))

## Documentation
- Updated README with the latest listed versions. ([e412710](#))

## Code Style Improvements
- Normalized code formatting and Javadocs for consistency. ([bb243df](#))
- Removed end-of-file newlines from all Java source files for uniformity. ([ca11232](#))

## Other Changes
- Merged `main` into `release` multiple times to sync changes. ([7659799](#), [46f8038](#), [3e113f6](#), [2ba4613](#))
- Removed duplicate line separators in source files. ([80b9d49](#))
- Bumped version post-release to prepare for further development. ([62e065b](#))

--- 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.19...0.2.20## v0.2.21

# Release Notes - Version 0.2.21

## New Features
- Added `getBeanTypes` API to `BeanSource` for retrieving bean types.  
- Implemented generic bean registration and builder overloads.  
- Introduced `GenericBeanNameGenerator` and supporting tests.  
- Extended `BeanSource` with bean registration support and static `registerBeans` overload.  
- Added utility methods for bean classes/definitions with tests.  
- New `loadFactoryClasses` and `loadFactoryNames` methods for factory handling.  

## Bug Fixes
- Adjusted test methods to use the passed `beanFactory`.  
- Updated `resolveBeanType` to use `BeanDefinition` type.  

## Dependency Updates
- Bumped Jackson to version 2.22.0.  
- Downgraded Jackson to version 2.21.3.  

## Documentation
- Updated `README` to reference version 0.2.21.  

## Build and Workflow Enhancements
- Removed `spring-milestone` repository from parent POM.  
- Multiple merges from `main` into `release` branches for alignment.  

---

For a complete list of changes, see the full [changelog](https://github.com/your-repo/compare/0.2.20...0.2.21).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.20...0.2.21## v0.2.22

# Release Notes - Version 0.2.22

### New Features
- **BeanSource Enhancements**:
  - Added support for `BeanSource` sources and renamed registrar. [#e6c3f899](https://github.com/your-repo/commit/e6c3f899)
  - Enabled `BeanSource` registration for web extensions. [#f0baceb5](https://github.com/your-repo/commit/f0baceb5)
  - Enhanced `EnableWebFluxExtension` with `BeanSource` sources. [#24619e29](https://github.com/your-repo/commit/24619e29)
- Introduced `CompositeWebEndpointMappingRegistry` for better endpoint mapping management. [#7c5682e6](https://github.com/your-repo/commit/7c5682e6)
- Added support for registering beans with `BeanDefinitionRegistry`. [#f058ee27](https://github.com/your-repo/commit/f058ee27, #44f91109](https://github.com/your-repo/commit/44f91109)
- Added helper methods like `getBeanDefinition(BeanDefinitionRegistry)` and `register event interceptors` from `BeanSource`. [#6f870363](https://github.com/your-repo/commit/6f870363), [#b9d60bdb](https://github.com/your-repo/commit/b9d60bdb)

### Dependency Updates
- Bumped **microsphere-logging** version to `0.1.15`. [#5208a54c](https://github.com/your-repo/commit/5208a54c)
- Bumped **microsphere-java** version to `0.3.7`. [#5c19d817](https://github.com/your-repo/commit/5c19d817)
- Updated parent POM version to `0.3.1`. [#371991b0](https://github.com/your-repo/commit/371991b0)

### Documentation
- Updated `README` with new release versions. [#5f22aaba](https://github.com/your-repo/commit/5f22aaba)

### Test Improvements
- Added tests for `CompositeWebEndpointMappingRegistry`. [#9a4dc47f](https://github.com/your-repo/commit/9a4dc47f)
- Added tests for registering primary beans without names. [#c8ba8bbc](https://github.com/your-repo/commit/c8ba8bbc)
- Imported `SimpleWebEndpointMappingRegistry` in tests for validation. [#d1aeb63f](https://github.com/your-repo/commit/d1aeb63f)
- Introduced tests for `deny-filter` functionality. [#614da06a](https://github.com/your-repo/commit/614da06a)

### Build and Workflow Enhancements
- Merged updates from the main branch into release branch multiple times. [#1f7cea0a](https://github.com/your-repo/commit/1f7cea0a), [#787c6ce9](https://github.com/your-repo/commit/787c6ce9), [#f1a444e3](https://github.com/your-repo/commit/f1a444e3), [#c952c9ed](https://github.com/your-repo/commit/c952c9ed), [#460cdee0](https://github.com/your-repo/commit/460cdee0), and others.
- Bumped repository version to prepare for the next release. [#31aa4e8a](https://github.com/your-repo/commit/31aa4e8a)

### Other Changes
- Minor refactoring and cleanup, including removing extra blank lines. [#e8555f1d](https://github.com/your-repo/commit/e8555f1d)

For a complete list of changes, please refer to the [Full Changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.21...0.2.22## v0.2.23

# Release Notes - Version 0.2.23

## Build and Workflow Enhancements
- Merged main into release and updated workflow metadata. [skip ci]
- Bumped version placeholders to 0.2.23 in README.
- Incremented version for the next development cycle after 0.2.22.

## Other Changes
- Used `SpringFactoriesLoader.loadFactories` with the classloader for improved compatibility.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.22...0.2.23## v0.2.24

# Release Notes for Version 0.2.24

## New Features
- **Embedded Database Support**: Added H2 embedded database support. ([1c55d038](https://github.com/your-repo/commit/1c55d038))
- Introduced `@OverrideAnnotationAttributes` for overriding annotation attributes via a strategy. ([85aeb2da](https://github.com/your-repo/commit/85aeb2da))
- Added utility methods like `ofAnnotationAttributes` for easier handling of attributes. ([65c01cf9](https://github.com/your-repo/commit/65c01cf9))
- Introduced `initializeBean` and `invokeAwareInterfaces` methods. ([352d53ee](https://github.com/your-repo/commit/352d53ee))

## Bug Fixes
- Fixed incorrect behavior in `equals` and added immutability and nullability enhancements. ([946c119a](https://github.com/your-repo/commit/946c119a))
- Included missing property prefix in configuration warnings. ([6ca3c137](https://github.com/your-repo/commit/6ca3c137))

## Documentation
- Enhanced Javadocs for various methods and enums, including `OverrideAnnotationAttributes` and `EmbeddedDatabaseType`. ([a388972b](https://github.com/your-repo/commit/a388972b), [21aa75bd](https://github.com/your-repo/commit/21aa75bd))
- Updated README with the latest version updates. ([d3cfb2e4](https://github.com/your-repo/commit/d3cfb2e4))

## Dependency Updates
- Added H2 dependency to `microsphere-spring-test`. ([9aa3e612](https://github.com/your-repo/commit/9aa3e612))

## Test Improvements
- Added `ImportOptionalTest` for annotation attribute overrides. ([38cdbdc5](https://github.com/your-repo/commit/38cdbdc5))
- Improved tests for `loadProperties` to utilize `PropertyResolver` and `PropertySource`. ([e6a5febd](https://github.com/your-repo/commit/e6a5febd), [502277a1](https://github.com/your-repo/commit/502277a1))

## Build and Workflow Enhancements
- Refactored multiple tasks, including reordering imports and normalizing code spacing. ([c5afc256](https://github.com/your-repo/commit/c5afc256), [c7df5b86](https://github.com/your-repo/commit/c7df5b86), [c96b0d50](https://github.com/your-repo/commit/c96b0d50))
- Improved class and method signatures for better readability. ([9a13a7c6](https://github.com/your-repo/commit/9a13a7c6), [2ea4db28](https://github.com/your-repo/commit/2ea4db28))

## Other Changes
- Refactored WebMvc configuration and event annotation attributes for cleaner logic and extendability. ([a6035467](https://github.com/your-repo/commit/a6035467), [cb49cdf7](https://github.com/your-repo/commit/cb49cdf7))
- Removed unused imports and improved error handling for properties loading. ([2ecbbf07](https://github.com/your-repo/commit/2ecbbf07), [8de97acb](https://github.com/your-repo/commit/8de97acb))

---

For the full changelog, visit the [repository](https://github.com/your-repo/commits/main).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.23...0.2.24## v0.2.25

# Release Notes - Version 0.2.25

## New Features
- Added support for `SPRING_7_0_8` in tests. ([93b3bd11](https://github.com/microsphere-projects/microsphere-spring/commit/93b3bd11))
- Use `@` prefix for annotation property names. ([8b834507](https://github.com/microsphere-projects/microsphere-spring/commit/8b834507))

## Dependency Updates
- Bumped `org.springframework:spring-framework-bom` from `7.0.7` to `7.0.8`. ([8773b656](https://github.com/microsphere-projects/microsphere-spring/commit/8773b656))
- Bumped `io.projectreactor:reactor-bom` from `2025.0.5` to `2025.0.6`. ([5768a8b7](https://github.com/microsphere-projects/microsphere-spring/commit/5768a8b7))
- Bumped `microsphere-logging` to `0.1.16`. ([6e6416c8](https://github.com/microsphere-projects/microsphere-spring/commit/6e6416c8))
- Bumped `microsphere-java` to `0.3.8`. ([0dde252e](https://github.com/microsphere-projects/microsphere-spring/commit/0dde252e))
- Bumped parent POM version to `0.3.3`. ([aaaea24a](https://github.com/microsphere-projects/microsphere-spring/commit/aaaea24a))

## Test Improvements
- Updated tests to align with `SPRING_7_0_8`. ([93b3bd11](https://github.com/microsphere-projects/microsphere-spring/commit/93b3bd11))

## Documentation
- Updated README versions to reflect `0.2.25`/`0.1.25`. ([3b68df22](https://github.com/microsphere-projects/microsphere-spring/commit/3b68df22))

## Other Changes
- Removed redundant `loadProperties` overload and associated tests. ([34b6b18c](https://github.com/microsphere-projects/microsphere-spring/commit/34b6b18c))
- Various merge and chore updates to sync branches and increment versions. ([multiple merge and chore commits](https://github.com/microsphere-projects/microsphere-spring/compare/0.2.24...0.2.25))

---

Thank you for using Microsphere!

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.24...0.2.25## v0.2.26

# Release Notes for Version 0.2.26  

## New Features  
- **BeanNameGenerator Support**: Added `BeanNameGenerator` support with updated examples in Javadoc. ([#515e4cf](https://github.com/.../commit/515e4cf))  
- Added `registerBeanDefinition` overload with corresponding tests for enhanced flexibility. ([#de7685bf](https://github.com/.../commit/de7685bf))  

## Dependency Updates  
- Bumped `microsphere-logging` to version 0.1.17. ([#34435ce](https://github.com/.../commit/34435ce))  
- Bumped `microsphere-java` to version 0.3.9. ([#ec7c4f5](https://github.com/.../commit/ec7c4f5))  
- Updated `microsphere-build` parent to version 0.3.4. ([#5a66372](https://github.com/.../commit/5a66372))  

## Refactors  
- Refactored `EnableSpringConverterAdapter` registrar and related APIs for better clarity and structure. ([#75fcdd4](https://github.com/.../commit/75fcdd4))  
- Improved annotated import candidate API usability. ([#d5c74a1](https://github.com/.../commit/d5c74a1), [#a3ea1ba](https://github.com/.../commit/a3ea1ba))  

## Documentation  
- Updated `README.md` with the latest versions for improved clarity. ([#b85ee77](https://github.com/.../commit/b85ee77))  
- Updated Javadocs for `BeanNameGenerator` and other API changes.  

## Code Cleanup  
- Removed unused imports and updated Javadoc references related to `ImportSelector` and `ImportBeanDefinitionRegistrar`. ([#1ae9c38](https://github.com/.../commit/1ae9c38), [#d8fd63d](https://github.com/.../commit/d8fd63d), [#884b63c](https://github.com/.../commit/884b63c))  
- Refactored and reformatted method parameters for better readability. ([#f641e20](https://github.com/.../commit/f641e20), [#e9a33a5](https://github.com/.../commit/e9a33a5), [#ecfaa36](https://github.com/.../commit/ecfaa36))  

## Test Improvements  
- Added tests for `BeanNameGenerator` and `registerBeanDefinition` API changes.  

## Build and Workflow Enhancements  
- Regular merges from `main` into release branch were performed to maintain up-to-date changes ([Multiple commits with `[skip ci]`](https://github.com/...)).  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.25...0.2.26## v0.2.27

# Release Notes - Version 0.2.27

## New Features
- Split `AnnotatedBeanCapableImportCandidate` for better modularity. ([3d258b28d](https://example.com))

## Build and Workflow Enhancements
- Merged `main` branch into `release`. ([22079bf4f](https://example.com), [697a5422d](https://example.com))
- Merged `release` branch into `main` after publish. ([18b6fde4e](https://example.com))
- Bumped version to next patch after publishing `0.2.26`. ([508d72811](https://example.com))

## Other Changes
- Bumped dependencies, cleaned imports, and removed `Maintainers`. ([5030701c9](https://example.com))  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.26...0.2.27## v0.2.28

# Release Notes for v0.2.28

## 🚀 New Features
- **Spring WebFlux Support:** Registered `SpringWebFluxHelper` in `spring.factories`. (#2c8275507)

## 🛠 Bug Fixes
- Unset primary on existing beans when adding composite beans to avoid conflicts. (#08cb589fc)

## 🏗️ Build and Workflow Enhancements
- **Merge Updates:** Regularly merged `main` into `release` to keep branches in sync. [skip ci]

## ⚙️ Dependency Updates
- Bumped `microsphere-logging` to `0.1.18`. (#23efe8555)
- Updated `microsphere-java` version to `0.3.10`. (#2a7aa757c)

## 🔄 Other Changes
- Simplified `@Import` usage in `EnableWebMvcExtension`. (#6e4da135f)
- Replaced custom bean factory usage with `DefaultListableBeanFactory` for better standardization. (#a3325bbfc)
- Removed unused `spring-api.json` resource file. (#b747980a5)

---

Looking forward to hearing your feedback on this release! 🚀

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.27...0.2.28## v0.2.29

# Release Notes for Version 0.2.29

## 🚀 New Features
- **Configurable Application Context**: Introduced `ConfigurableApplicationContextInitializer`, enabling better application context customization.  
  ([#a9bae0ec1](https://github.com/microsphere-projects/microsphere-spring/commit/a9bae0ec1))  
- **Enhanced Initialization Strategies**: Updated to use `MethodUtils` for consistent instantiation strategy handling.  
  ([#f1eca5cbf](https://github.com/microsphere-projects/microsphere-spring/commit/f1eca5cbf))  
- **ApplicationContext Improvements**: Made `ApplicationContextInitializers` configurable and added support for `@SpringJUnitConfig` with a listenable environment.  
  ([#21b48d705](https://github.com/microsphere-projects/microsphere-spring/commit/21b48d705),  
   [#cb0718b93](https://github.com/microsphere-projects/microsphere-spring/commit/cb0718b93))  

## 🛠️ Other Changes
- Improved logging readability by restructuring `logger.info` calls and removing unused imports.  
  ([#82aaf6252](https://github.com/microsphere-projects/microsphere-spring/commit/82aaf6252),  
   [#caaedeb44](https://github.com/microsphere-projects/microsphere-spring/commit/caaedeb44))  
- Updated README to reflect the latest release (v0.2.29).  
  ([#a2d286e57](https://github.com/microsphere-projects/microsphere-spring/commit/a2d286e57))  

---

**[See Full Changelog](https://github.com/microsphere-projects/microsphere-spring/compare/0.2.28...0.2.29)**

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.28...0.2.29## v0.2.30

# Release Notes - Version 0.2.30

## Documentation
- Improved Javadoc for multiple components, including:
  - `EventPublishingBeanInitializer`
  - `ListenableAutowireCandidateResolver`
- Updated Javadoc examples to enhance clarity and consistency.
- Removed unused imports in documentation examples.
- Updated the README with the latest branch versions.

## Other Changes
- Moved resolver registration logic to the initializer for better organization.
- Removed the unused `enable` property from `ListenableAutowireCandidateResolver`.

---

For a detailed list of changes, see the [Full Changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.29...0.2.30## v0.2.31

# Release Notes - Version 0.2.31  

## New Features  
- Added `BeanFactory` overload methods to `BeanRegistrar` for enhanced flexibility.  
- Enabled and tested `AutoRegistrationBeanInitializer` for improved auto-registration capabilities.  
- Registered `EnableAutoRegistrationBean` via configuration.  

## Bug Fixes  
- Resolved testing inconsistencies by fixing and consolidating test methods for `BeanFactory` overloads.  

## Documentation  
- Improved initializer documentation and added details for `isRegistered`.  

## Test Improvements  
- Refactored `BeanRegistrar` tests to directly utilize `BeanFactory`.  
- Added comprehensive test coverage for `BeanFactory` overload methods in `BeanRegistrar`.  

## Other Changes  
- Removed outdated test file `BeanInitializerTest.java`.  

**Note:** This release introduces multiple improvements and refactors to enhance modularity, testability, and functionality related to the `BeanFactory`.  

**Full Changelog:** Available in the project repository.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.30...0.2.31## v0.2.32

# Release Notes - Version 0.2.32

## New Features
- **Environment Configuration**: Added `EnvironmentEnabled` interface to manage enabled property access with public accessors. ([#a31ce0024](https://example.com/commit/a31ce0024))
- **Spring Factories**: Added a test for Spring factories initializers. ([#a12a87620](https://example.com/commit/a12a87620))

## Bug Fixes
- Addressed inconsistencies in WebFlux testing by using `loadFactoryClasses`. ([#9dfd1daa0](https://example.com/commit/9dfd1daa0))

## Test Improvements
- Added new tests for `WebFluxExtensionInitializer` and `EnvironmentEnabled` functionality. ([#a12a87620](https://example.com/commit/a12a87620), [#5ba4edbcd](https://example.com/commit/5ba4edbcd))

## Other Changes
- Improved logging granularity for the `isEnabled` method with trace and info levels. ([#e60c94f04](https://example.com/commit/e60c94f04))
- Removed unused imports and simplified codebase. ([#15ecc12dc](https://example.com/commit/15ecc12dc), [#2c8c7ad43](https://example.com/commit/2c8c7ad43))
- Reverted changes to `auto-register` property behavior in initializer methods. ([#8c8672940](https://example.com/commit/8c8672940))
- Updated `README.md` to reflect the latest version `0.2.32` in examples. ([#2e518788b](https://example.com/commit/2e518788b))
- Various chore and merge commits. ([#1452f5342](https://example.com/commit/1452f5342), [#de4d38b78](https://example.com/commit/de4d38b78))  

---

For the full list of changes, refer to the [changelog](https://example.com/changelog).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.31...0.2.32## v0.2.33

# Release Notes - Version 0.2.33  

## Dependency Updates  
- **Microsphere Java**: Bumped to `0.3.11`.  
- **Microsphere Logging**: Bumped to latest version.  
- **Microsphere Build**: Bumped to `0.3.5`.  

## Documentation  
- Updated `README` branch version numbers.  

## Other Changes  
- Maintenance: Merged `main` into `release`.  
- Maintenance: Bumped version to next patch post `0.2.32`.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.32...0.2.33## v0.2.34

# Release Notes - Version 0.2.34

## Bug Fixes
- Fixed loop condition in header entry lookup for improved accuracy. ([2ebb47f](#))

## Documentations
- Updated README branch version table with latest entries. ([cc5eb4b](#))
- Standardized Javadoc example headings for better consistency. ([7e2512b](#))

## Dependency Updates
- Bumped `microsphere-logging` to version 0.1.20 → 0.1.21. ([36bf141](#))
- Bumped `microsphere-java` to version 0.3.13 → 0.3.14. ([9a415ef](#))
- Upgraded parent POM to `microsphere-build` version 0.3.6 → 0.3.7. ([3ba8081](#))

## Other Changes
- Removed unused loggers from multiple classes to improve code clarity. ([97c93a2](#), [550afb9](#), [f3e5b79](#), [d08520c](#))
- Tidied up trailing whitespaces in advice class. ([26f4e95](#))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.33...0.2.34## v0.2.35

# Release Notes for v0.2.35

## Dependency Updates
- Upgraded `microsphere-logging` to v0.1.22. ([0b06522c4](#))
- Updated `microsphere-build` parent to v0.3.8. ([47a4ef7e5](#))

## Documentation
- Updated `README` with the latest branch versions. ([f133adc2b](#))

## Build and Workflow Enhancements
- Merged `main` into `release` for synchronization. ([f51a86eb3](#), [c3556bb2e](#), [77968cbfb](#), [c4b62b091](#))
- Bumped version for post-publishing maintenance. ([3c712e14d](#))

---

No new features or bug fixes were introduced in this release.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.34...0.2.35## v0.2.36

# Release Notes for Version 0.2.36

## New Features
- Added annotation processor to the following modules:
  - Test
  - JDBC
  - Guice

## Dependency Updates
- Updated dependency declarations in the `web` module.
- Reordered test dependencies in `webmvc` and `webflux` POMs.
- Reordered annotation dependencies in the `context` POM.
- Removed redundant SLF4J API dependencies.

## Documentation
- Polished annotation documentation and test imports.
- Updated README with latest branch version details.
- Cleaned up Javadoc example usage headings.

## Test Improvements
- Adjusted test dependencies across multiple modules.

## Other Changes
- Maintenance commits to merge branches and bump version numbers.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.35...0.2.36