# Release Notes

## v0.1.10

# Release Notes - Version 0.1.10

## New Features
- Add source for `RESET_BEAN_NAME` configuration. [#211](https://github.com/mercyblitz/microsphere-spring/pull/211)
- Add workflow to sync branches from upstream. [#211](https://github.com/mercyblitz/microsphere-spring/pull/211)
- Add Copilot-generated release note generation. [af26f822](https://github.com/mercyblitz/microsphere-spring/commit/af26f822)

## Bug Fixes
- Use `isErrorEnabled` check to guard `logger.error` calls. [0a3f139c](https://github.com/mercyblitz/microsphere-spring/commit/0a3f139c)

## Other Changes
- Lifecycle logging updated to `INFO` level. [93f4719a](https://github.com/mercyblitz/microsphere-spring/commit/93f4719a)
- Improved Java matrix workflow spacing and values. [61a29495](https://github.com/mercyblitz/microsphere-spring/commit/61a29495)
- Bump dependencies:
  - `microsphere-logging` to 0.1.6. [5e51efbc](https://github.com/mercyblitz/microsphere-spring/commit/5e51efbc)
  - `microsphere-java` to 0.2.8. [e4a5e658](https://github.com/mercyblitz/microsphere-spring/commit/e4a5e658)
  - Parent POM to 0.2.7. [d0a872f8](https://github.com/mercyblitz/microsphere-spring/commit/d0a872f8)
- Disable Surefire system classloader in CI. [cb4ecfc7](https://github.com/mercyblitz/microsphere-spring/commit/cb4ecfc7)
- Remove unused Surefire `useSystemClassLoader` setting. [3e620623](https://github.com/mercyblitz/microsphere-spring/commit/3e620623)
- Code style improvement: Added space after `if` keyword in conditions. [17258665](https://github.com/mercyblitz/microsphere-spring/commit/17258665)

---

For a detailed view of the changes, refer to the [commit history](https://github.com/mercyblitz/microsphere-spring/commits/main).

## v0.1.11

# Release Notes for v0.1.11

## Dependency Updates
- Updated Maven wrapper to version `3.9.14`. ([3d74a856](#))

## Build and Workflow Enhancements
- Enhanced release notes and release creation workflow. ([4a7892a1](#))
- Removed `surefire.useSystemClassLoader` flag for simplified builds. ([bcf2dfbe](#))

## Other Changes
- Added a trailing newline to `maven-wrapper.properties`. ([461991f2](#))
- Bumped version number to prepare for the next patch release. ([b10b3193](#))

---

For a complete list of changes, refer to the full [changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.10...0.1.11## v0.1.13

# Release Notes for Version 0.1.13

## New Features
- Added `empty contextConfigLocation` init-param. ([e647454c](https://github.com/microsphere-projects/commit/e647454c))
- Embedded Tomcat now stops using a shutdown hook. ([7397e146](https://github.com/microsphere-projects/commit/7397e146))
- Tomcat stops on context close via listener. ([dbe5a97e](https://github.com/microsphere-projects/commit/dbe5a97e))

## Dependency Updates
- Bumped `microsphere-logging` to `0.1.7`. ([8c63e5fe](https://github.com/microsphere-projects/commit/8c63e5fe))
- Bumped `microsphere-java` to `0.2.9`. ([bf524657](https://github.com/microsphere-projects/commit/bf524657))
- Updated Microsphere versions and imported BOM dependencies. ([6d0e3dd5](https://github.com/microsphere-projects/commit/6d0e3dd5))
- Updated Tomcat dependencies to use `tomcat-embed-core`, removing legacy dependencies. ([b53686f4](https://github.com/microsphere-projects/commit/b53686f4))

## Test Improvements
- Added `dispatcherServlet-servlet.xml` for tests. ([82b20166](https://github.com/microsphere-projects/commit/82b20166))
- Removed hardcoded paths to `dispatcherServlet-servlet.xml` from tests. ([1a23102c](https://github.com/microsphere-projects/commit/1a23102c))
- Updated `web.xml` to Java EE 3.1 namespace and schema for tests. ([94c3651b](https://github.com/microsphere-projects/commit/94c3651b), [57226bcb](https://github.com/microsphere-projects/commit/57226bcb))

## Build and Workflow Enhancements
- Added Java 11 support in CI and configured Surefire classloader. ([531ff0b3](https://github.com/microsphere-projects/commit/531ff0b3))
- Limited Java versions in CI build matrix. ([d47911d4](https://github.com/microsphere-projects/commit/d47911d4))
- Dropped Java 11 from CI build matrix. ([2da1c2b5](https://github.com/microsphere-projects/commit/2da1c2b5))
- Bumped Maven distribution to `3.9.15`. ([de210994](https://github.com/microsphere-projects/commit/de210994))
- Updated Maven wrapper to point to Maven Central. ([7a88a961](https://github.com/microsphere-projects/commit/7a88a961))
- Removed hardcoded Tomcat version in CI configuration. ([01c82cf4](https://github.com/microsphere-projects/commit/01c82cf4))
- Set GitHub actions workflow strategy `max-parallel` to 5. ([4520cc3a](https://github.com/microsphere-projects/commit/4520cc3a))

## Other Changes
- Merged `release-1.x` into `dev-1.x` with minor version bump to `0.1.13`. ([2e50d2ac](https://github.com/microsphere-projects/commit/2e50d2ac), [6643a825](https://github.com/microsphere-projects/commit/6643a825)) 

**Full Changelog**: [0.1.11...0.1.13](https://github.com/microsphere-projects/compare/0.1.11...0.1.13)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.11...0.1.13## v0.1.14

# Release Notes - Version 0.1.14

## Build and Workflow Enhancements
- Expanded Java matrix to include newer versions. ([#219](https://github.com/microsphere-projects/dev-1.x))
- Tidy up dependencies and bumped Microsphere versions. ([#220](https://github.com/microsphere-projects/dev-1.x))
- Merged `release-1.x` into `dev-1.x` branch. ([#220](https://github.com/microsphere-projects/dev-1.x))

## Other Changes
- Bumped version to `0.1.14` after publishing `0.1.13`. ([#219](https://github.com/microsphere-projects/dev-1.x)) 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.13...0.1.14## v0.1.15

# Release Notes for v0.1.15

## New Features
- **AutoRegistrationBean**: Introduced support for `AutoRegistrationBean`.  
- Added `AnnotatedBeanCapableImportCandidate` for improved annotation-based imports and related refactor.  
- `parseExpressions` methods are now public to enhance configurability (#223).  

## Bug Fixes
- **WebEndpointMapping**: Fixed handling of `toExpression` and normalization logic.  
- Removed redundant `WebEndpointMappingExpressionVisitor` for improved clarity and performance.  

## Documentation
- Updated `README` to reflect branch versions and compatibility details for better transparency.  

## Build and Workflow Enhancements
- Changed CI workflow Java version from 11 to 8 for broader compatibility.
  
## Test Improvements
- Reordered imports in WebMVC test classes for better readability.  
- Added new `toExpression` tests for `WebEndpointMapping`.  

## Other Changes
- Moved web test utilities to `microsphere-spring-test`.  
- Introduced `WebMvcConfigurerAdapter` and a default adapter implementation (#224).  
- Registered processor as an interceptor (#222).  

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.14...0.1.15## v0.1.16

# Release Notes for Version 0.1.16

## Build and Workflow Enhancements
- Removed `max-parallel` limit from CI matrix to optimize testing. ([b0c74f65](https://github.com/mercyblitz/repo/commit/b0c74f65))
- Added OSSRH credentials to Maven publish workflow. ([836fb445](https://github.com/mercyblitz/repo/commit/836fb445))
- Improved Maven usage with caching and adjusted `mvn/mvnw` in workflows. ([64381fc9](https://github.com/mercyblitz/repo/commit/64381fc9))
- Fixed Java setup step name and normalized matrix case in workflows. ([8cfd5c1d](https://github.com/mercyblitz/repo/commit/8cfd5c1d))

## Dependency Updates
- Upgraded `microsphere-logging` to version 0.1.10. ([3aa82ee9](https://github.com/mercyblitz/repo/commit/3aa82ee9))
- Updated `microsphere-java` and parent project versions. ([d30029c7](https://github.com/mercyblitz/repo/commit/d30029c7))

## Documentation
- Updated branch versions in `README`. ([de642806](https://github.com/mercyblitz/repo/commit/de642806))

## Other Changes
- Merged `release-1.x` into `dev-1.x`. ([dcf0ce99](https://github.com/mercyblitz/repo/commit/dcf0ce99))
- Incremented version number to 0.1.16 for the new release. ([dc746de4](https://github.com/mercyblitz/repo/commit/dc746de4)) 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.15...0.1.16## v0.1.17

# Release Notes for Version 0.1.17

## New Features
- Added `generateBeanName` and a map-returning registrar for enhanced bean naming support. ([88d3351e](https://github.com/mercyblitz/microsphere-spring/commit/88d3351e))
- Utilized `BeanUtils.generateBeanName` and `BeanDefinitionReaderUtils` for consistent bean name generation. ([44e1e75d](https://github.com/mercyblitz/microsphere-spring/commit/44e1e75d), [3b0a879d](https://github.com/mercyblitz/microsphere-spring/commit/3b0a879d))

## Bug Fixes
- Fixed README table alignment for better clarity. ([711f5747](https://github.com/mercyblitz/microsphere-spring/commit/711f5747))

## Documentation
- Enhanced README with TOC, examples, and additional documentation. ([0692f1ac](https://github.com/mercyblitz/microsphere-spring/commit/0692f1ac))
- Updated branch names in README for accuracy. ([550b9ea6](https://github.com/mercyblitz/microsphere-spring/commit/550b9ea6), [ea7eec55](https://github.com/mercyblitz/microsphere-spring/commit/ea7eec55))

## Dependency Updates
- Bumped Microsphere Java version and logging library version. ([8b5981d9](https://github.com/mercyblitz/microsphere-spring/commit/8b5981d9))

## Test Improvements
- Removed obsolete test classes: `IdempotentConfig` and `testRegisterSpringFactoriesBeans`. ([4fdba373](https://github.com/mercyblitz/microsphere-spring/commit/4fdba373), [91ccb844](https://github.com/mercyblitz/microsphere-spring/commit/91ccb844))

## Other Changes
- Removed idempotent implementation and corresponding tests. ([064fd636](https://github.com/mercyblitz/microsphere-spring/commit/064fd636))
- Version bumped to 0.1.17 after publishing 0.1.16. ([0048a2d6](https://github.com/mercyblitz/microsphere-spring/commit/0048a2d6))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.16...0.1.17## v0.1.18

# Release Notes - v0.1.18

## New Features
- **Personalized Onboarding:** Added personalized onboarding prompt for Microsphere. (#95a20d4a)

## Test Improvements
- Added unit tests for:
  - `DefaultApplicationListenerInterceptorChain`. (#51dfd9ab)
  - `DefaultApplicationEventInterceptorChain`. (#d591c1c2)
  - `YamlPropertySourceFactory`. (#3a0e2577)
  - `JsonPropertySourceFactory`. (#29e00624)
  - `DefaultResourceComparator`. (#16d93234)
  - `CompositeAutowireCandidateResolvingListener`. (#3c3d3d2c)
- Added tests for autowire resolving listener and initializer. (#a1a47b76, #b8f80626)

## Documentation
- Added missing JavaDoc `@author` and `@since` tags to 5 Java files. (#187c469c)

## Dependency Updates
- Bumped `microsphere-logging` to **v0.1.12**. (#20a87900)
- Bumped `microsphere-java` to **v0.3.4**. (#6b28caa5)

## Build and Workflow Enhancements
- Added `.github` prompt templates for development assistants. (#c6a8aafe)  
- Merged `release-1.x` into `dev-1.x` for workflow consistency. (#23fd909e)  
- Adjusted version bumping process after publishing. (#92d3f3a4)

## Other Changes
- Improved resource handling through reader closure, added logging, and ensured `ThreadLocal` cleanup. (#e4bb2dcd)  

**[Full Changelog](https://github.com/mercyblitz/microsphere-spring/compare/0.1.17...0.1.18)**

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.17...0.1.18## v0.1.19

```markdown
# Release Notes - Version 0.1.19

## New Features
- Added `nullSafeBeanClassLoader` utility with corresponding tests. (#2903fec0, #2473fc39)
- Introduced `BeanSource` enum with unit test. (#ada02e16)
- Added logging rules to embedded Tomcat tests. (#ab0e3803)
- Enhanced class loader handling in `ResourceLoader`. (#d6795806)

## Bug Fixes
- Fixed potential `NullPointerException` by using `ObjectUtils.defaultIfNull` in `AnnotationUtils` and `TTLContext#getEffectiveTTL`. (#079b827c, #9e724041)
- Cast `newLinkedList` output to resolve generic type mismatch. (#2d5fc9a0)

## Documentation
- Updated README with latest versions: `0.2.19`/`0.1.19`. (#32e78e3d)
- Updated documentation for `nullSafeBeanClassLoader`. (#2903fec0)

## Dependency Updates
- Bumped `microsphere-logging` to `0.1.13`. (#e6bc40e8)
- Upgraded `microsphere-java` to `0.3.5`. (#d65b8a7e)
- Upgraded `microsphere-build` parent to `0.3.0`. (#f40c6220)

## Test Improvements
- Added unit tests for `classloader` utilities. (#ca731877)
- Ensured tests in `testFilter` run only for TRACE level. (#cb181e3d)
- Migrated to JUnit4 for `BeanSourceTest`. (#15f20af5)

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to keep branches in sync. (#f23d9d09)
- Bumped version to `0.1.19` post-release of `0.1.18`. (#aa8f7852)

## Other Changes
- Cleaned up source code: removed duplicated blank lines, unused imports, trailing whitespace, and improved formatting. (#e78d1e3d, #78568f87, #d6ee6bf8)
- Replaced direct collection constructions with `ListUtils`, `SetUtils`, and `MapUtils` factories for cleaner code. (#ff8c9447, #7b85b665, #31ccab97, #e9cf08a3)
- Imported `SpringLoggingTest` into `CompositeWebFilterTest` for improved test clarity. (#7f8791ba)
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.18...0.1.19## v0.1.20

# Release Notes for v0.1.20

## Dependency Updates
- Bumped `microsphere-logging` to version `0.1.14`.  
- Updated `microsphere-java` to version `0.3.6`.

## Documentation
- Updated README to reflect `0.2.20` and `0.1.20` versions.

## Code Style Improvements
- Removed trailing newlines from 602 Java source files for consistency.  
- Reformatted and tidied Spring-compatible utilities for better readability.  
- General whitespace and formatting cleanup.

## Other Changes
- Merged release and development branches to maintain alignment.  
- Incremented version to `0.1.20`.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.19...0.1.20## v0.1.21

# Release Notes: Version 0.1.21  

## New Features  
- Added static `registerBeans` for registering multiple `BeanSource`s.  
- Introduced `bean registration` functionality to `BeanSource`.  
- Implemented `getBeanClass`, `getBeanDefinition`, and `getBeanTypes` utilities.  
- Added generic bean registration and factory loader support.  

## Dependency Updates  
- Updated Jackson library to version `2.22.0`.  

## Documentation  
- Updated README with versions `0.2.21` and `0.1.21`.  

## Other Changes  
- Removed unused imports in `BeanRegistrar`.  
- Merged `release-1.x` into `dev-1.x`.  

---

Thank you for using this release! 🚀

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.20...0.1.21## v0.1.22

# Release Notes for Version 0.1.22

## New Features
- **Event Handling:** Added support to register event interceptors from configurable sources. (#fb341ea2)  
- **Bean Management:**  
  - Introduced `getBeanDefinition` method for the registry. (#f9bd6fcb)  
  - Added overloads for `registerBeans` in `BeanDefinitionRegistry`. (#10e9bf4a)  
  - Enabled bean source support with a new composite registry. (#bad51f05)  

## Dependency Updates
- Upgraded `microsphere-logging` to version 0.1.15. (#23c28487)  
- Upgraded `microsphere-java` to version 0.3.7. (#6ef40eab)  
- Updated parent POM version to 0.3.1. (#ea412d8b)  

## Test Improvements
- Added test to ensure primary beans are registered without names. (#6da92d8d)  
- Imported `SimpleWebEndpointMappingRegistry` for testing. (#ccc591e5)  
- Introduced null-check tests with annotated getters. (#534935a3)  

## Documentation
- Updated README to reflect the latest listed versions. (#37b131c7)  

## Build and Workflow Enhancements
- Replaced `jakarta.servlet` imports with `javax.servlet` for broader compatibility. (#b66395be)  
- Merged `release-1.x` into `dev-1.x`. (#96eddf01)  
- Version bumped to next patch after publishing 0.1.21. (#32d98002)  

## Other Changes
- Removed registrations for `WebEndpointMappingFactory`. (#c644dfa2)  
- Directly built bean definitions in the registrar. (#2d16db7e)  

---

Thank you to everyone who contributed to this release!

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.21...0.1.22## v0.1.23

# Release Notes for Version 0.1.23

## New Features
- **AutoRegistrationBean:** Replaced with `SpringFactoriesLoader` for improved bean auto-registration logic. ([e57439fa](https://example.com/commit/e57439fa))

## Documentation
- Enhanced `README.md` for better clarity. ([0b97c96a](https://example.com/commit/0b97c96a))

## Dependency Updates
- Bumped `microsphere-logging` dependency to version `0.1.16`. ([a0574895](https://example.com/commit/a0574895))

## Build and Workflow Enhancements
- Merged `release-1.x` branch into `dev-1.x` to sync changes. ([abcac6fb](https://example.com/commit/abcac6fb))
- Updated `pom.xml` with relevant tweaks. ([237b0d79](https://example.com/commit/237b0d79))
- Bumped version for post-0.1.22 development. ([cd3f01d4](https://example.com/commit/cd3f01d4))

## Other Changes
- General improvements via merged branch `dev-1.x`. ([d7eda57d](https://example.com/commit/d7eda57d))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.22...0.1.23## v0.1.24

# Release Notes for Version 0.1.24

## New Features
- **Annotation Override Strategy:**
  - Introduced `@OverrideAnnotationAttributes` for customizable annotation attribute overrides. [#5b3da2bd, #eb593575, #80fa644e]
  - Added strategy-based override and placeholder-resolving APIs for annotation attributes. [#b42ce03b, #4cd80649]
  - Added `ofAnnotationAttributes` helper to `AnnotationUtils`. [#f9f8f787]

- **Configuration Enhancements:**
  - Added support for resolving placeholders in properties when loading configuration. [#60f801a7, #dd348cf8]
  - Introduced `PREFIX_PROPERTY_NAME_PREFIX` constant for clearer configuration. [#187ab67a]

- **Database Support:**
  - Added H2 embedded database support and related test dependency. [#2cdfa221, #013c8887]

- **Utility Improvements:**
  - Added `initializeBean` helper method for better bean initialization. [#08b09b83]

## Bug Fixes
- Fixed behavior of `equals` for annotation attributes and added factory overloads. [#38f9320d]
- Corrected prefix inclusion in missing configuration property logs for better debugging. [#8c9b5699]

## Documentation
- Updated `README.md` with version changes. [#3d3fed12]
- Enhanced Javadoc and documentation for `@OverrideAnnotationAttributes` and import requirements. [#cbdb39d2, #c11fe11c]

## Dependency Updates
- Added H2 database dependency in the test module. [#013c8887]

## Test Improvements
- Improved unit tests:
  - Added `ImportOptionalTest`. [#9c176dff]
  - Refactored and enhanced hash code test cases. [#c5fa6a64]
  - Adjusted null annotation test assertions. [#235c3e84]

## Other Changes
- **Refactoring:**
  - Updated `WebMvc` and `WebFlux` registration to extend base classes for consistency. [#890a597d, #75623817]
  - Simplified property loading with environment-aware methods. [#249aae75]
  - Removed unnecessary `throws IOException` declarations. [#b501fd20]
  - Cleaned up code style, formatting, and test variable naming. [#2ed4f8d0, #dae1f462, #eb593575]
  
- **Build/Workflow:**
  - Merged release branch `release-1.x` into development branch `dev-1.x`. [#ad872dc3]
  - Bumped to version `0.1.24` post `0.1.23` publishing. [#c6524958]

---

**Full Changelog:** Omitted from this document.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.23...0.1.24## v0.1.25

# Release Notes for Version 0.1.25

## Dependency Updates
- Bumped `microsphere-logging` to `0.1.16` for improved logging capabilities. (6b4e6af5)
- Upgraded `microsphere-java` to `0.3.8` for enhanced Java support. (b37617b8)
- Updated `microsphere-build` parent to `0.3.3`. (85b526fb)

## Documentation
- Updated README with the latest versions: `0.2.25` and `0.1.25`. (053b5d20)

## Other Changes
- Removed redundant `loadProperties` overload and corresponding tests for streamlined codebase. (bfe977ef)
- Updated default property prefix in `microsphere` to use `@` for better clarity. (78774b10)

---

Full Changelog: [0.1.24...0.1.25](link-to-changelog)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.24...0.1.25## v0.1.26

# Release Notes for Version 0.1.26

## New Features
- Added `registerBeanDefinition` overload supporting `BeanDefinition`. ([6ab6bd8](#))
- Introduced `genericBeanDefinitionBuilder` helper for improved bean definition handling. ([7cdfbc6](#), [d89f0b6](#))

## Improvements
- Refactored multiple registrars to use `Annotated` base classes for consistency and clarity. ([d213fe7](#), [a4e5720](#))
- Enhanced `EnableSpringConverterAdapterRegistrar` for better modularity. ([d8f70ba](#))
- Refactored `P6DataSource` and `EventExtensionRegistrar` for cleaner implementation. ([3cc1a28](#), [64a9e40](#))
- Simplified annotation attribute resolution for registrars. ([34894ec](#))
- Improved handling of import candidates using `Annotated` loaders. ([31987fa](#))

## Dependency Updates
- Upgraded `microsphere-logging` to version `0.1.17`. ([9e64b2d](#))
- Updated `microsphere-java` to version `0.3.9`. ([04ddce3](#))
- Bumped `microsphere-build` parent to version `0.3.4`. ([c3b44b5](#))

## Documentation
- Updated README to reflect new branch versions `0.2.26/0.1.26`. ([8621288](#))

## Build and Workflow Enhancements
- Merged branch `release-1.x` into `dev-1.x`. ([68b44bc](#))
- Updated project version to prepare for the next patch release. ([bc752c3](#))

## Other Changes
- Replaced `ImportSelector` with generic bean definition approach for simplicity. ([1d84ca0](#), [c2d659a](#), [f483558](#))
- Used `AnnotationBeanNameGenerator` and `FullyQualifiedAnnotationBeanNameGenerator` for better bean naming. ([488ed86](#), [ec69981](#))
- Minor signature reformatting for methods like `selectImports`. ([95a9d45](#))
- Commented out `@Override` annotations for compatibility purposes. ([3a8b306](#))
- Leveraged custom `WebMvcConfigurerAdapter` from `microsphere`. ([c9d1c16](#))

---

**Full Changelog**: [v0.1.25...v0.1.26](#)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.25...0.1.26## v0.1.27

# Release Notes for Version 0.1.27

## New Features
- **Improved Component Registration**: Updated to use `genericBeanDefinitionBuilder` in registrar for enhanced flexibility. (#76d0365ac)
- **Streamlined Bean Naming**: Adopted `AnnotationBeanNameGenerator.INSTANCE` for consistent annotation-based bean naming. (#0823b6bb8)

## Bug Fixes
- **Spring Compatibility**: Commented out `@Override` annotation to resolve compatibility issues with specific Spring versions. (#31890531a)

## Dependency Updates
- **Microsphere Logging**: Downgraded `microsphere-logging` dependency to version `0.1.17` for stability improvements. (#460b1112c)

## Other Changes
- Split `AnnotatedBeanCapableImportCandidate` into two distinct implementations for better modularity. (#c74176d31)
- Bumped README module versions to `0.2.27/0.1.27`. (#636beeb02)

---

**Full Changelog:** Available [here](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.26...0.1.27## v0.1.28

# Release Notes for Version 0.1.28

### New Features
- Added `spring.factories` mapping for WebFlux helper. (9c6c2fe6)

### Improvements
- Simplified `@Import` annotation usage. (79e42bddb)
- Added `BeanDefinitionRegistry` storage and bean registration. (de78e6541)
- Unset primary on beans for composite registry. (5c0264d85)

### Dependency Updates
- Upgraded `microsphere-logging` to version 0.1.18. (e9eea35bc)
- Upgraded `microsphere-java` to version 0.3.10. (ee99f2e71)

### Documentation
- Updated README to reflect latest versions (0.2.28 and 0.1.28). (f446f0d27)

### Other Changes
- Removed unused `spring-api.json` resource. (bab0dab21)
- Chore: Merged `release-1.x` into `dev-1.x`. [skip ci] (304774df1)
- Updated versioning post-0.1.27 release. (f86728c82)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.27...0.1.28## v0.1.29

# Release Notes for Version 0.1.29

## New Features
- Introduced `ConfigurableApplicationContextInitializer`, enabling a more flexible initialization process. (#21e6a0594, #21af07638)
- Made `EventPublishingBeanInitializer` configurable for enhanced customization. (#7b86115b3)
- Added support for `ApplicationContextInitializers` via `spring.factories`. (#331476951)
- Enabled a configurable property in the initializer for better control over application setup. (#2554b6688)

## Bug Fixes
- Refactored environment setup to improve reliability in configuration. (#31a242811)
- Addressed logging inconsistency by logging disabled initializer settings at INFO level. (#65780b0be)

## Test Improvements
- Enabled listenable environment in test scenarios for enhanced testability. (#10c9a3447)

## Documentation
- Updated the README with the latest version details. (#64396508c)

## Dependency Updates
- Replaced traditional instantiation logic with `MethodUtils` for enhanced compatibility and maintainability. (#ec48c2db4)

## Other Changes
- Merged `release-1.x` into `dev-1.x` for aligning branches. (#21af07638)
- Incremented version to `0.1.29` post-release of `0.1.28`. (#26ad9f4ef)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.28...0.1.29## v0.1.30

# Release Notes for Version 0.1.30

## Documentation
- Improved Javadocs and usage examples for Initializer. ([#260](https://github.com/mercyblitz/dev-1.x))  
- Enhanced Javadoc for `EventPublishingBeanInitializer`.  
- Updated documentation for `ListenableAutowireCandidateResolver`.  
- Bumped README versions to reflect 0.2.30/0.1.30.

## Other Changes
- Simplified initialization logic for `ListenableAutowireCandidateResolver`.  
- Merged `release-1.x` into `dev-1.x`.  
- Preparation for the next patch post-0.1.29 release.  

---

Full Changelog: See [GitHub Commits](https://github.com/mercyblitz/dev-1.x/compare/0.1.29...0.1.30) for more details.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.29...0.1.30## v0.1.31

# Release Notes for v0.1.31

## New Features
- **BeanFactory Enhancements**: Introduced direct registration methods using `BeanFactory`, including initializers and resolvers.  
  - Added `AutoRegistrationBeanInitializer` to streamline component registration.  
  - Enabled `EnableAutoRegistrationBean` registration via configuration beans.  

## Documentation
- Updated README to reflect the latest version numbers.  

## Test Improvements
- Replaced `registry` usage with `beanFactory` in tests for consistency and accuracy.  

## Other Changes
- Internal merge: synced `release-1.x` into `dev-1.x`.  

---

**Note:** This release focuses on improving the registration process using `BeanFactory` for better configurability and lifecycle management.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.30...0.1.31