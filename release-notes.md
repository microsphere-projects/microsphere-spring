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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.17...0.1.18