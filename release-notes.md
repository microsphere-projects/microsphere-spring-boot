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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.14...0.1.15## v0.1.16

# Release Notes - Version 0.1.16

## Dependency Updates
- **microsphere-spring** version updated to `0.1.20`. [aaa5160]

## Bug Fixes
- Fixed table formatting issues in the `README`. [63b4148]

## Documentation
- Removed `Maintainers` section from the `README`. [a1ebd6d]

## Code Quality Improvements
- Normalized formatting and improved Javadoc spacing across the codebase. [2ea9dbc]
- Removed trailing newlines from all 113 Java source files for consistency. [750e25c]

## Other Changes
- Updated version references in `README` to `0.2.16` and `0.1.16`. [be16d1e]

---

For the complete list of changes, refer to the [Full Changelog](https://github.com/mercyblitz/microsphere-spring-boot/compare/0.1.15...0.1.16).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.15...0.1.16## v0.1.17

# Release Notes for v0.1.17

## Dependency Updates
- Updated `microsphere-spring` version numbers for compatibility improvements. ([2dc6aae](https://github.com/mercyblitz/commit/2dc6aae))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to maintain branch consistency. ([cacd141](https://github.com/mercyblitz/commit/cacd141))
- Bumped version to the next patch after publishing v0.1.16. ([06cd144](https://github.com/mercyblitz/commit/06cd144))  

---

**Note:** This release includes backend optimizations and preparatory updates for future development.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.16...0.1.17## v0.1.18

# Release Notes - Version 0.1.18

## Dependency Updates
- Bumped `microsphere-spring.version` to `0.1.22`.  
- Updated parent POM version to `0.3.1`.

## Bug Fixes
- Fixed typo in test class name.  

## Documentation
- Updated README versions to `0.2.18` and `0.1.18`.  

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for synchronization.  
- Adjusted version to the next patch after publishing `0.1.17`.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.17...0.1.18## v0.1.19

# Release Notes - Version 0.1.19

## Dependency Updates
- Bumped `microsphere-spring.version` to 0.1.23. ([42e2a66](https://github.com/mercyblitz/project/commit/42e2a66))

## Documentation
- Updated latest versions in `README`. ([f3fc9ae](https://github.com/mercyblitz/project/commit/f3fc9ae))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x`. ([e8c35ac](https://github.com/mercyblitz/project/commit/e8c35ac))
- Bumped version to `0.1.19` as next patch after publishing `0.1.18`. ([86263ed](https://github.com/mercyblitz/project/commit/86263ed))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.18...0.1.19## v0.1.20

# Release Notes - Version 0.1.20

## New Features
- **WebFlux Support:**  
  - Added `microsphere-spring-boot-webflux` module for WebFlux support.  
  - Introduced `@EnableWebFluxExtension` in test properties.  
  - Implemented reversed proxy handler mapping and `HandlerMethod` for WebFlux.  
  - Registered WebFlux auto-configuration using `spring.factories`.  
  - Improved configuration ordering with `@AutoConfigureAfter`.  

- **WebMvc Enhancements:**  
  - Added `microsphere-spring-boot-webmvc` module for enhanced WebMvc integration.  
  - Conditionalized WebMvc auto-configuration and tests.  
  - Refactored WebMvc tests with an abstract test implementation.

## Dependency Updates
- Bumped `microsphere-spring` to `0.1.25`.  
- Updated parent POM to version `0.3.3`.  
- Added JUnit dependency for enhanced testing.

## Test Improvements
- Redesigned tests with abstract test classes for better reuse.  
- Replaced `super.test()` with specialized `testWebEndpoints()` in Web tests.  
- Broadened `@ConditionalOnWebMvcAvailable` checks for more robust test coverage.

## Build and Workflow Enhancements
- Merged changes from `release-1.x` into `dev-1.x`.  
- Updated `README.md` to reflect the latest release versions.  
- Incremented version to prepare for the next patch.

## Other Changes
- Registered WebMvc auto-configuration in `spring.factories`.  
- Refactored test annotations and improved modularity.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.19...0.1.20## v0.1.21

# Release Notes - Version 0.1.21

### Dependency Updates
- Bumped `microsphere-spring.version` to `0.1.26`.  
- Updated parent version to `0.3.4`.  
- Improved Spring Boot compatibility for the main branch.

### Code Improvements
- Switched to name-based `ConditionalOnClass` checks to enhance compatibility.

### Documentation
- Updated README to reflect the latest version numbers.

### Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to sync branches.  
- Bumped version to the next patch after publishing `0.1.20`.  

---

For a list of all changes, refer to the full changelog.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.20...0.1.21## v0.1.22

# Release Notes - Version 0.1.22

## Dependency Updates
- Bumped `microsphere-spring` versions. (#0053a15)

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to align branches. (#c62c699)
- Prepared for the next development iteration by bumping the version post-0.1.21 release. (#0770091)

## Other Changes
- Merged updates from `dev-1.x`. (#865b2f1) 

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.21...0.1.22## v0.1.23

# Release Notes - Version 0.1.23

## New Features
- **Added**: Support for `META-INF` path in test resources.
- **Moved**: `core.properties` and added Microsphere defaults.

## Bug Fixes
- **Fixed**: Missing trailing newlines in files.
- **Resolved**: Removal of jolokia version and adjustment of EOF newlines.

## Dependency Updates
- **Upgraded**: `microsphere-spring` to versions `0.1.28`, `0.1.29`, and `0.1.30`.

## Documentation
- **Updated**: README versions bumped to `0.2.23` and `0.1.23`.

## Build and Workflow Enhancements
- **Merge**: Integrated `release-1.x` into `dev-1.x`.
- **Version Increment**: Bumped project version after publishing `0.1.22`.

---

For more details on the changes, refer to the commit history.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.22...0.1.23## v0.1.24

# Release Notes - Version 0.1.24

## New Features
- Introduced `WebMVC` property constants. ([#138](https://github.com/mercyblitz/repo/pull/138))
- Added `ConfigurationProperties` auto-configuration and related tests.
- Added `EnableConfigurationPropertiesExtension` with accompanying tests.
- Extended `ConfigurableApplicationContextInitializer` for enhanced initialization support.
- Supported cloning of `ConfigurationProperties` beans via a generic API.

## Bug Fixes
- Corrected `WebFlux` enabled default constant name.
- Removed redundancy in `Advisor`'s `Ordered` implementation.

## Documentation
- Added Javadoc references for `EnableConfigurationProperties`.
- Clarified Javadoc link text in test classes.
- Removed extra Javadoc from extension classes.

## Dependency Updates
- Upgraded `microsphere-spring` to version 0.1.32.

## Test Improvements
- Employed `TestBindListener` in `BindListenerTest`.
- Enhanced test coverage for cloned `ConfigurationProperties`.

## Build and Workflow Enhancements
- Removed `importBeanNameGenerator` argument from bean registrations.
- Removed `AutoRegistrationBeanInitializer` and associated tests.

---

**Note**: Upgrade recommended to leverage new features and improvements.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.23...0.1.24## v0.1.25

# Release Notes for v0.1.25

## Bug Fixes
- Fixed issues with bound property checks and bean property copying. ([cc1a735](#))  
- Resolved missing property assignments during bind updates. ([dda90ed](#))  
- Guarded against unresolved configuration properties. ([52fd585](#))  
- Ensured safe parent lookup for configuration names. ([9abd8cd](#))  
- Fixed bind constructor handling across Spring Boot versions. ([0696e48](#))  

## Enhancements
- Simplified boolean checks and finalized static map handling. ([16b709c](#))  
- Improved `getPrefix` to handle source names correctly. ([337b6b0](#))  
- Made property events optional during updates. ([ccaeb56](#))  
- Refactored configuration property change binding flow. ([332f040](#))  

## Test Improvements
- Added null-case tests for bean context creation. ([3b42ffc](#))  
- Expanded test coverage for bean context validation. ([1f2e67f](#))  
- Refined `BindUtils` constructor binding and assertion tests. ([f199d13](#), [33a0ebd](#))  

## Performance and Reliability
- Captured bean property values in bind contexts for consistency. ([5f5bb85](#))  
- Used assignable checks for reliable property updates. ([6c10c85](#))  
- Cloned mutable configuration values before publishing for safety. ([f4fedd2](#))  

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` and prepared for future development.  
  ([fc73c88](#), [651b8c5](#), [2c63fb9](#))  

---

**Note:** This release focused primarily on improving bean binding behavior, configuration property handling, and increasing test robustness.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.24...0.1.25## v0.1.26

# Release Notes: Version 0.1.26

## New Features
- **Constructor Binding Compatibility**: Added support for `@ConstructorBinding` to improve compatibility. [#4f0267b](https://github.com/mercyblitz/project/commit/4f0267b)

## Bug Fixes
- Fixed bean type resolution for configuration properties. [#7c52cfc](https://github.com/mercyblitz/project/commit/7c52cfc)
- Guarded against `null` bean context during property binding. [#62a289b](https://github.com/mercyblitz/project/commit/62a289b)

## Dependency Updates
- Upgraded `microsphere-spring` to the latest version. [#1a72ab9](https://github.com/mercyblitz/project/commit/1a72ab9)
- Bumped `microsphere-build` parent to version `0.3.5`. [#d971d06](https://github.com/mercyblitz/project/commit/d971d06)

## Test Improvements
- Added test case for `null` bean property handling. [#740bd89](https://github.com/mercyblitz/project/commit/740bd89)

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to stay up-to-date with the latest releases. [#36b0ea6](https://github.com/mercyblitz/project/commit/36b0ea6)

## Other Changes
- Simplified constructor binding null assertions for cleaner code. [#eecd173](https://github.com/mercyblitz/project/commit/eecd173)
- Refined configuration property change tracking. [#b5a8597](https://github.com/mercyblitz/project/commit/b5a8597)
- Updated README references to reflect new version numbers. [#7f621ca](https://github.com/mercyblitz/project/commit/7f621ca)

---

**Full Changelog**: [Compare 0.1.25...0.1.26](https://github.com/mercyblitz/project/compare/0.1.25...0.1.26)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.25...0.1.26## v0.1.27

# Release Notes: Version 0.1.27

## Bug Fixes
- Hardened configuration property binding to improve application stability. ([#143](https://github.com/mercyblitz/dev-1.x/pull/143))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` branch to sync updates.  
- Bumped version to `0.1.27` post-release of `0.1.26`.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.26...0.1.27## v0.1.28

# Release Notes: Version 0.1.28

## Bug Fixes
- Improved candidate checks in the bean context to enhance robustness. (#144)

## Documentation
- Updated `README` with the latest release versions for better clarity. (#30f609f)

## Other Changes
- Migrated codebase updates from `release-1.x` to `dev-1.x`. (#69b400d)
- Transitioned to using `ResolvableType.resolve` for resolving bean classes. (#e12cbfc)
- Incremented patch version post publishing `0.1.27`. (#ece42aa) 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.27...0.1.28## v0.1.29

# Release Notes: v0.1.29

## Documentation
- Updated **README** to include Spring Boot version matrix. (3a1cbda)

## Test Improvements
- Added tests for `HttpAutoConfiguration` and renamed existing tests. (bb0399c)  
- Introduced a dedicated test module and actuator endpoint condition. (debfc7b)  
- Switched test module to use `javax.servlet` API. (b2ccf93)  

## Build and Workflow Enhancements
- Bumped version to the next patch after publishing v0.1.28. (04a7100)  
- Merged `release-1.x` into `dev-1.x`. (a07666c)  

## Other Changes
- Tidied up `@ConstructorBinding` annotation formatting. (9ba70aa)  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.28...0.1.29## v0.1.30

# Release Notes - Version 0.1.30

## Dependency Updates
- Upgraded `microsphere-spring` to versions **0.1.34** and **0.1.35**.  
- Updated `microsphere-build` parent to versions **0.3.6**, **0.3.7**, and **0.3.8**.

## Documentation
- Enhanced README to reflect latest module versions for better clarity.

## Test Improvements
- Aligned auto-configuration tests and improved dependency setup for consistent behavior.

## Build and Workflow Enhancements
- Merged `release-1.x` branch into `dev-1.x`.  
- Updated project patch version post-0.1.29 publication.  

---

*No new features or bug fixes introduced in this release.*

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.29...0.1.30## v0.1.31

# Release Notes for Version 0.1.31

## New Features
- Added Boot annotation processors to module POMs for enhanced configuration. (#85688dc)

## Dependency Updates
- Cleaned up POM dependencies and added annotation processor. (#18030e7)
- Dropped JUnit 4 test dependencies in the `webmvc` module. (#b364b5e)

## Test Improvements
- Refactored auto-configuration tests for runner types. (#2c85736)
- Used `JacksonProperties` in reactive test configuration. (#2a20d4b)
- Used `CacheProperties` in reactive test configuration. (#b95650c)

## Documentation
- Updated README with the latest version numbers. (#5a9eb70)

## Other Changes
- Merged `release-1.x` branch into `dev-1.x`. (#5a3c44d)
- Bumped version to next patch after publishing 0.1.30. (#0db762e)

---

For the full list of changes, refer to the commit history.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.30...0.1.31