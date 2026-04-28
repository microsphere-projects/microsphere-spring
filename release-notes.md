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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.13...0.1.14