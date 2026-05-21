# Release Notes

## v0.2.11

# Release Notes for Version 0.2.11

## New Features
- Introduced `Use MockEnvironment.withProperty` in tests for enhanced configuration ([c7e2eb2](https://example.com)).
- Added support for Spring Boot 3.5.14 and 4.0.6 ([3f1a57f](https://example.com)).
- Added test class annotations to `SpringBootTest` ([1ffa066](https://example.com)).
- Added assertions and constants for `MeterRegistry` class names for better test coverage ([34bf684](https://example.com), [525068f](https://example.com)).

## Dependency Updates
- Bumped `Spring Boot 4.0` to version `4.0.6` ([3665d16](https://example.com)).
- Bumped `microsphere-spring` to `0.2.14` ([76a0385](https://example.com)) and `0.2.13` ([e5665d5](https://example.com)).
- Added JUnit BOM and Spring milestone repository ([5b475ec](https://example.com)).

## Test Improvements
- Improved tests by using `MockEnvironment.withProperty` for a cleaner and consistent testing environment ([c7e2eb2](https://example.com)).
- Added and enhanced test class coverage for various components ([1ffa066](https://example.com), [34bf684](https://example.com)).

## Build and Workflow Enhancements
- Updated Maven wrapper to `3.9.15` ([09a656f](https://example.com)) and `3.9.14` ([a6331e9](https://example.com)).
- Pointed Maven wrapper to Maven Central for improved accessibility ([5ec0c31](https://example.com)).
- Bumped parent POM version to `0.2.7` ([50c4c5b](https://example.com)).
- Added workflow for syncing fork branches from upstream ([19a1b4b](https://example.com)).
- Refactored workflows for better structure, added workflow permission, and matrix build configuration ([59d8dab](https://example.com), [8821b7a](https://example.com)).
- Enhanced release notes and automated release creation ([8bf5cf2](https://example.com)).

## Other Changes
- Fixed indentation in `dependabot.yml` updates list ([779b968](https://example.com), [ae77002](https://example.com)).
- Removed exclusions from `microsphere` test dependency for cleaner POM ([a92f014](https://example.com)).
- Simplified and cleaned up POM structure ([76a0385](https://example.com), [8201e9a](https://example.com)).

**Note**: This release contains updates to support new Spring Boot versions and improve test configurations, along with various dependency, workflow, and build improvements.

For a detailed list of changes, please refer to the [Full Changelog](https://example.com).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.10...0.2.11## v0.2.12

# Release Notes - Version 0.2.12

## 🆕 New Features
- Added `AutoRegistrationBeanInitializer` and corresponding tests. ([5b54aac](#))
- Parameterized `ApplicationContextInitializer` for enhanced configurability. ([4ec4d1c](#))

## 📚 Documentation
- Updated `README` with the latest branch versions. ([ab0b788](#))

## 📦 Dependency Updates
- Upgraded `microsphere-spring` to version `0.2.15`. ([e8d171c](#))
- Bumped `org.jolokia:jolokia-core` from `2.5.1` to `2.6.0`. ([00b2af3](#))

## 🔨 Build and Workflow Enhancements
- Merged `main` into `release` branch for improved branch synchronization. ([1c0f927](#), [5d0db80](#), [3d2eb69](#))
- Bumped version to prepare for next development cycle. ([78e7efa](#))

---

Thank you for using our project!

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.11...0.2.12## v0.2.13

# Release Notes: Version 0.2.13

## Dependency Updates
- Updated `org.junit:junit-bom` from `6.0.3` to `6.1.0`. ([#112](https://github.com/microsphere-projects/microsphere/pull/112))
- Bumped `microsphere-spring.version` to `0.2.16`.
- Updated parent version to `0.2.9`.

## Build and Workflow Enhancements
- Added Maven server credentials to the workflow.
- Adjusted Maven workflows and ensured the addition of EOF newline for consistency.

## Documentations
- Updated README files to reflect changes in branch versions.

## Other Changes
- Routine merges between `main` and `release` branches.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.12...0.2.13