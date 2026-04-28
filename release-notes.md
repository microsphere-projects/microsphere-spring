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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring/compare/0.2.11...0.2.13