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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.20...0.2.21