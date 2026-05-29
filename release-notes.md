# Release Notes

## v0.1.11

## Release Notes – Version 0.1.11

### New Features
- Utilize type-based `@ConditionalOnBean` for `MeterRegistry` support. (#d658294)

### Dependency Updates
- Bump `microsphere-spring` dependency to 0.1.14 with improved compatibility. (#ec0c13d)
- Drop previously excluded `microsphere-spring-test` dependencies. (#33c07fd)
- Import JUnit BOM via `dependencyManagement` for streamlined dependency handling. (#6b50a6a)

### Build and Workflow Enhancements
- Add Java 17, 21, and 25 to CI matrix for extended compatibility testing. (#7daa832)
- Limit Java matrix range and optimize build with `max-parallel` setting. (#4770853)
- Switch Maven wrapper source to Maven Central. (#545c8aa)
- Update Maven wrapper to versions 3.9.14 (#b89c428) and 3.9.15 (#93220d1).
- Enhance release workflow notes and define permissions. (#62bd45a, #95cf770, #c9b3be8)
- Remove explicit permissions for build jobs. (#c9b3be8)

### Test Improvements
- Replace `MockEnvironment` setup with `.withProperty` for cleaner tests. (#0663ff5)
- Register test class explicitly in `@SpringBootTest` configurations. (#bb1f3ba)
- Add tests for `METER_REGISTRY_CLASS_NAME` constant validation. (#bde8838)

### Documentation
- Align README branch versions and apply formatting improvements. (#71dcac9)

### Other Changes
- Remove Spring milestone repository from the parent POM file. (#1b390b5)
- Update parent package version to 0.2.7. (#02fbe38)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.10...0.1.11## v0.1.12

# Release Notes: Version 0.1.12

## New Features
- **AutoRegistration Enhancements**: Introduced `AutoRegistrationBean` initializer with corresponding tests. ([db49543](https://github.com/your-repo/commit/db49543))

## Test Improvements
- Split `AutoRegistration` tests into separate classes for better modularity. ([1eed80a](https://github.com/your-repo/commit/1eed80a))

## Dependency Updates
- Upgraded `microsphere-spring` to version 0.1.15. ([183602a](https://github.com/your-repo/commit/183602a))

## Documentation
- Updated branch version information in the README. ([9b2393d](https://github.com/your-repo/commit/9b2393d))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` as part of workflow adjustments. ([96146df](https://github.com/your-repo/commit/96146df))
- Updated patch version post-0.1.11 release. ([5238bb0](https://github.com/your-repo/commit/5238bb0))

---

For a detailed list of changes, refer to the [Full Changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.11...0.1.12## v0.1.13

# Release Notes for Version 0.1.13

## Dependency Updates
- Updated `microsphere-spring.version` to `0.1.16`. [d267d74]
- Upgraded JUnit Jupiter to `5.14.4`. [ff56177]
- Bumped parent POM version to `0.2.9`. [aa161ae]

## Build and Workflow Enhancements
- Removed `max-parallel` from Maven build workflow. [b696c6f]
- Adjusted Maven workflow commands, caching, and server credentials settings. [04e68e4]
- Added Maven server credentials to the publish workflow. [7f5bada]
- Corrected Java matrix variable casing in CI. [aa463e3]

## Documentation
- Updated branch latest versions in `README`. [a5041cf]

## Other Changes
- Merged `release-1.x` into `dev-1.x`. [64ad557]
- Bumped version to next patch after publishing `0.1.12`. [b79c823]

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.12...0.1.13## v0.1.14

# Release Notes - Version 0.1.14

## Documentation
- Revamped README with expanded documentation and reorganized structure for better clarity. ([74451ee](#), [e6803c8](#))
- Updated branch information and enhanced GitHub prompts in README. ([947722c](#), [6df99d2](#))
- Listed and bumped latest version numbers in README. ([47016bc](#))

## Dependency Updates
- Upgraded `microsphere-spring` version to 0.1.17 and 0.1.18. ([741761c](#), [0d75a73](#))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for synchronization. ([c0f8305](#))
- Bumped version to prepare for the next patch release post 0.1.13. ([f42f4a5](#))

## Other Changes
- Minor structural and branch updates. ([774871a](#), [12eabb7](#))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.13...0.1.14## v0.1.15

# Release Notes - Version 0.1.15  

## New Features  
- Added user guide and updated README to enhance documentation. ([bda80e1](#))  

## Bug Fixes  
- Resolved duplicated line separators in the codebase. ([780c3a9](#))  
- Improved Java source code formatting for better readability and maintainability. ([567e68e](#))  

## Dependency Updates  
- Upgraded `microsphere-spring` to version **0.1.19**. ([7a6bf7a](#))  
- Updated parent POM to version **0.3.0**. ([007c496](#))  

## Other Changes  
- Utilized collection factory helpers and implemented minor fixes. ([d7f8357](#))  
- Updated README to reflect the latest versions **0.2.15** and **0.1.15**. ([b2ad9d4](#))  

---

**Full Changelog:** [View commits](#)  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.14...0.1.15