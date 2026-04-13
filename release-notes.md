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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.1.10...0.1.11