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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.12...0.2.13## v0.2.14

# Release Notes - Version 0.2.14

## New Features
- **README Enhancements**: Added a comprehensive README with a TOC, feature documentation, and usage examples. (#114, 439c750)
- **.github Templates**: Added AI prompt templates to the repository. (b370c07)

## Documentation
- Updated version numbers in README.md. (0eeb0ff)  
- Formatted README markdown tables and lists for improved readability. (b2e30fb)  
- Revised branch names in README table for clarity. (d6a566f)  

## Dependency Updates
- **microsphere-spring**: Bumped dependency version to `0.2.17`. (fbc1937)  
- **microsphere-spring**: Bumped dependency version to `0.2.18`. (479e08b)

## Build and Workflow Enhancements
- Merged changes from `main` into `release` branch and vice versa. ([skip ci], multiple commits)  
- Bumped version to `0.2.14` after publishing `0.2.13`. (c72aba2)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.13...0.2.14## v0.2.15

# Release Notes for v0.2.15

## New Features
- Added [user-guide.md](./user-guide.md) with comprehensive module and class explanations. (#119)

## Documentation
- Updated README to include GitHub user guide link. (#f651995)
- Enhanced README with updated JavaDoc links to the user guide. (#5ce9a5f)

## Code Quality Improvements
- Removed empty test method `testGetPrefixForAlias`. (#a1bb99d)
- Cleaned up trailing whitespace and consolidated blank lines. (#1aeaf6d)
- Removed unused `java.util` imports. (#c625c71)
- Utilized collection factory helpers for cleaner code. (#bb0b20c)

## Dependency Updates
- Bumped `microsphere-spring.version` to `0.2.19`. (#b621c0f)
- Updated parent POM version to `0.3.0`. (#520b60c)

## Build and Workflow Enhancements
- Merged `main` branch into `release` multiple times for CI alignment. [skip ci]

---

For a detailed list of changes, view the [Full Changelog](https://github.com/microsphere-projects/microsphere/compare/v0.2.14...v0.2.15).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.14...0.2.15## v0.2.16

# Release Notes - Version 0.2.16

## Dependency Updates
- Updated `microsphere-spring.version` to `0.2.20`. ([183e86f](#))

## Documentation
- Updated `README` to reflect new version numbers and removed the "Maintainers" section. ([45cdf30](#))

## Other Changes
- Reformatted code and Javadoc for improved style consistency. ([baab74e](#))
- Bumped version to prepare for the next patch. ([35f15d9](#))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.15...0.2.16## v0.2.17

# Release Notes - Version 0.2.17

## Dependency Updates
- **microsphere-spring** versions updated.  

## Other Changes
- Prepared for the next development cycle by bumping patch version.  
- Merged `main` into `release` and vice versa.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.16...0.2.17## v0.2.18

# Release Notes for Version 0.2.18

## Bug Fixes
- Fixed typo in class name: `Disalbed` ➡️ `Disabled`. ([ae77745](https://github.com/microsphere-projects/microsphere-spring-boot/commit/ae77745))

## Documentation
- Updated `README` to reflect new latest versions. ([7b7e666](https://github.com/microsphere-projects/microsphere-spring-boot/commit/7b7e666))

## Dependency Updates
- Bumped `microsphere-spring` version to `0.2.22`. ([07b6ae2](https://github.com/microsphere-projects/microsphere-spring-boot/commit/07b6ae2))
- Updated parent POM version to `0.3.1`. ([89d952a](https://github.com/microsphere-projects/microsphere-spring-boot/commit/89d952a))

## Other Changes
- Various maintenance merges with `main` and `release` branches. ([d7e973f](https://github.com/microsphere-projects/microsphere-spring-boot/commit/d7e973f), [6b4cd0d](https://github.com/microsphere-projects/microsphere-spring-boot/commit/6b4cd0d), [cc36a5f](https://github.com/microsphere-projects/microsphere-spring-boot/commit/cc36a5f), [c809acc](https://github.com/microsphere-projects/microsphere-spring-boot/commit/c809acc))
- Prepared for next patch development following the release of `0.2.17`. ([bb12eb8](https://github.com/microsphere-projects/microsphere-spring-boot/commit/bb12eb8))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.17...0.2.18## v0.2.19

# Release Notes for Version 0.2.19

## Dependency Updates
- **microsphere-spring.version**: Bumped to `0.2.23`.

## Documentation
- Updated README to reflect versions `0.2.19` and `0.1.19`.

## Build and Workflow Enhancements
- Merged `main` into `release` and `release` into `main`.  
- Incremented version to the next patch (`0.2.19`) post `0.2.18` release.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.18...0.2.19## v0.2.20

# Release Notes - Version 0.2.20

## New Features
- Added `microsphere-spring-boot-webmvc` module. ([62249a7](#))
- Introduced WebFlux auto-configuration module. ([9c4b382](#))
- Enabled reversed proxy handler mapping. ([2345783](#))
- Added support for Spring Boot 4 in `WebMvcAutoConfiguration`. ([9542a50](#))

## Bug Fixes
- Fixed test property keys for `EnableWebFluxExtension`. ([3af57ab](#))
- Fixed typo in `WebMvcAutoConfiguration` comment. ([c079456](#))

## Dependency Updates
- Upgraded `microsphere-spring` to version `0.2.25`. ([506e76c](#))
- Updated `microsphere-build` parent to version `0.3.3`. ([884e9fb](#))

## Test Improvements
- Unified WebMVC tests to call `super.test()`. ([66f6aca](#))
- Refactored `WebMvcAutoConfiguration` tests and added base test. ([16729ff](#))
- Enhanced WebMvc auto-config tests with additional coverage. ([4817f56](#))  
- Simplified tests to invoke specific cases in `WebMvcAutoConfigurationTest`. ([3a4dbe9](#))
- Refactored tests to call `testWebEndpoints()`. ([5bd6dc9](#))  

## Build and Workflow Enhancements
- Removed `jackson-databind` test dependency. ([d6d8fa7](#))
- Updated README to ensure the latest version documentation. ([90ab736](#))

## Other Changes
- Broadened `@ConditionalOnClass` checks for Web MVC compatibility. ([e806646](#))
- Replaced `test` scope for Jackson dependency; relaxed negotiation test. ([097d841](#))
- Improved `OnPropertyPrefixCondition` annotation handling for `ANNOTATION_TYPE`. ([810220e](#))
- Removed redundant `PropertyConstants` test file. ([bb64a67](#))
- Added specific handler and interceptor conditions for WebMVC. ([3b70727](#))

For a complete list of changes, please see the [full changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.19...0.2.20## v0.2.21

# Release Notes - Version 0.2.21

## New Features
- **Spring Boot 4.1 Support**:  
  - Added `spring-boot-4.1` profile to Maven `pom.xml`. ([209e962](https://github.com/commit-id/209e962))  
  - Updated CI matrix to include `spring-boot-4.1`. ([b29e407](https://github.com/commit-id/b29e407))  
  - Added Spring Boot `4.0.7` and `4.1` enum entries. ([747dfe4](https://github.com/commit-id/747dfe4))

## Documentation Updates
- Updated README to indicate compatibility with Spring Boot `4.1.x`. ([f1d569f](https://github.com/commit-id/f1d569f))  
- Updated README to include the latest version details. ([322634b](https://github.com/commit-id/322634b))

## Dependency Updates
- Bumped `microsphere-spring` version to `0.2.26`. ([580af0a](https://github.com/commit-id/580af0a))  
- Upgraded parent project version to `0.3.4`. ([d15cda4](https://github.com/commit-id/d15cda4))  
- Updated Spring Boot version dependencies. ([a120113](https://github.com/commit-id/a120113))

## Build and Workflow Enhancements
- Merged `main` branch into `release` branch. ([9368d2d](https://github.com/commit-id/9368d2d), [8169716](https://github.com/commit-id/8169716), [caea46a](https://github.com/commit-id/caea46a), [05ddcde](https://github.com/commit-id/05ddcde), [b35eb66](https://github.com/commit-id/b35eb66), [cedd52f](https://github.com/commit-id/cedd52f))  
- Merged `release` branch back into `main`. ([415cb1f](https://github.com/commit-id/415cb1f))  
- Bumped version to prepare for `0.2.21`. ([45f9ba9](https://github.com/commit-id/45f9ba9))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.20...0.2.21## v0.2.22

# Release Notes for v0.2.22

## Dependency Updates
- Updated Microsphere versions to `0.2.27`, `0.2.22`, and `0.1.22`.  

## Other Changes
- Merged `main` into `release` branch and vice versa.  
- Bumped version to next patch after publishing `v0.2.21`.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.21...0.2.22## v0.2.23

# Release Notes - Version 0.2.23

## Dependency Updates
- Bumped `microsphere-spring` version to `0.2.30`.  
- Incremented `microsphere-spring` version to `0.2.29` and subsequently to `0.2.28`.  

## Other Changes
- Moved `core.properties` to `META-INF` and added `microsphere` properties.  
- Removed unused imports in web modules.  
- Updated test resource path to `META-INF`.  
- Updated README version table to reflect `0.2.23/0.1.23`.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.22...0.2.23## v0.2.24

# Release Notes: Version 0.2.24

## New Features  
- Added property constants for WebMVC feature toggles.  
- Introduced `EnableConfigurationPropertiesExtension` with supporting tests.  
- Enabled cloning of `ConfigurationProperties` bean for wrappers via a generic method.  
- Registered `ConfigurationProperties` auto-configuration.  
- Implemented `ConfigurableApplicationContextInitializer` for improved extensibility.

## Bug Fixes  
- Fixed default enabled constant for WebFlux.  

## Dependency Updates  
- Upgraded `microsphere-spring` to version 0.2.32.  

## Documentation  
- Updated README to reflect the new version (0.2.24).  
- Removed redundant class-level Javadoc in some classes.  

## Other Changes  
- Removed outdated components: `TestAutoRegistrationBean`, `factory entry`, and `AutoRegistrationBeanInitializer`.  
- Simplified logic by removing `importBeanNameGenerator` usage in registrar and `Ordered` implementation from advisor.  

---

Full Changelog: [View here](link-to-full-changelog)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.23...0.2.24## v0.2.25

# Release Notes: Version 0.2.25

## New Features
- Added support for publishing change events for constructor-bound properties. ([ad1c82c](https://github.com/your-repo/commit/ad1c82c))
- Introduced tracking of property values in bean context. ([ed4e72e](https://github.com/your-repo/commit/ed4e72e))

## Bug Fixes
- Fixed configuration properties context prefix validation. ([64ca5ea](https://github.com/your-repo/commit/64ca5ea))
- Hardened configuration property parent lookup. ([576f437](https://github.com/your-repo/commit/576f437))
- Corrected issues with mismatched configuration bean types. ([60be436](https://github.com/your-repo/commit/60be436))
- Fixed property assignment type checks. ([cf33ad9](https://github.com/your-repo/commit/cf33ad9))
- Resolved context test issues for configuration properties. ([96540e0](https://github.com/your-repo/commit/96540e0))
- Ensured cloning of mutable property values before binding. ([149b9df](https://github.com/your-repo/commit/149b9df))
- Fixed bean property cloning in bind metadata. ([c20497d](https://github.com/your-repo/commit/c20497d))

## Documentation
- Added Javadoc examples for exclusion APIs. ([fc87971](https://github.com/your-repo/commit/fc87971))

## Test Improvements
- Added tests for bean property accessors. ([ab152c9](https://github.com/your-repo/commit/ab152c9))
- Added tests for map change events in test properties. ([b2085c4](https://github.com/your-repo/commit/b2085c4))
- Removed unused actuator test setup. ([bb8d79d](https://github.com/your-repo/commit/bb8d79d))

## Other Changes
- Applied minor core code cleanups. ([149065e](https://github.com/your-repo/commit/149065e))
- Simplified configuration property set logic. ([29a5423](https://github.com/your-repo/commit/29a5423))
- Refactored configuration properties binding logic. ([b32dbac](https://github.com/your-repo/commit/b32dbac))
- Used `ResolvableType` for more robust instance checks. ([2c82025](https://github.com/your-repo/commit/2c82025))
- Introduced `BindUtils` for constructor lookup and helper methods. ([e71e0fc](https://github.com/your-repo/commit/e71e0fc), [d671698](https://github.com/your-repo/commit/d671698))
- Guarded bound-property checks with configuration values. ([8554686](https://github.com/your-repo/commit/8554686))

For details, see the [full changelog](https://github.com/your-repo/compare/0.2.24...0.2.25).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.24...0.2.25## v0.2.26

# Release Notes for v0.2.26  

## New Features  
- Added support for constructor binding property assertions.  
- Introduced projects map to test configuration properties for enhanced flexibility.  

## Bug Fixes  
- Fixed issue with constructor-bound properties rebinding to improve consistency.  
- Guarded null bean context during property binding to prevent potential errors.  

## Dependency Updates  
- Bumped `microsphere-spring` to v0.2.33.  
- Bumped `microsphere-build` to v0.3.5.  
- Updated `org.junit:junit-bom` from v6.1.0 to v6.1.1.  

## Test Improvements  
- Added test cases for null bean properties to improve coverage.  
- Enhanced constructor-binding test wiring.  

## Build and Workflow Enhancements  
- Merged updates from the main branch. ([skip ci])  

## Other Changes  
- Removed unused test assertion imports.  
- Updated README to reflect the latest version references.  
- Deferred bean context initialization until singletons are ready for better startup behavior.  
- Refined bean property logging details for improved visibility.  
- Enhanced configuration properties change tracking for more precise monitoring.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.25...0.2.26## v0.2.27

# Release Notes - Version 0.2.27

## 🛠️ Bug Fixes
- Harden property binding bean updates to improve resilience. (#d1584d7)

## 📚 Documentation
- Updated README with the latest version matrix. (#7a4e47a)

## 🏗️ Build and Workflow Enhancements
- Merged main into release branch. ([#f8b7228](#), [#2587062](#))
- Merged release branch into main. (#bcd160b)
- Bumped version to prepare for 0.2.27 release. (#d8f81a0)

--- 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.26...0.2.27## v0.2.28

# Release Notes: Version 0.2.28

## Bug Fixes
- Hardened property descriptor checks to prevent potential issues. (#6d5c502)
- Added guard for `null` in candidate class detection to enhance stability. (#233e6dd)
- Improved bean class resolution using `ResolvableType.resolve`. (#8f14522)

## Documentation
- Updated README to reflect the latest version numbers. (#2f2bd2e)

## Build and Workflow Enhancements
- Merged main into release branches multiple times to maintain consistency. (#ce7656b, #8934031, #ce9a99a, #1b62136)
- Bumped version to prepare for the next patch release after 0.2.27. (#50965f5)

---

No new features or dependency updates were introduced in this release.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.27...0.2.28## v0.2.29

# Release Notes - Version 0.2.29

## New Features
- Added `microsphere-spring-boot-test` module. (#812b894)
- Introduced reusable auto-configuration test base. (#e6f0a0a)
- Added actuator endpoint presence condition. (#7a1e8c9)

## Documentation
- Updated README with Spring Boot version ranges. (#7b32110)
- Updated README with the latest release versions. (#2847e11)
- Added Javadoc reference for `ConfigurationMetadata`. (#8ce3a57)

## Dependency Updates
- Set Spring Boot 3.5 as default profile. (#09172e8)
- Defaulted to Spring Boot 4.1 and updated HTTP test. (#69043b2)

## Build and Workflow Enhancements
- Merged `main` branch into `release`. [skip ci] (#5f881df, #8a0b20f, #1bac768, #baa1114)
- Bumped version to the next patch after publishing 0.2.28. [skip ci] (#5608718)

---

For a complete list of changes, refer to the full changelog.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.28...0.2.29## v0.2.30

# Release Notes for Version 0.2.30

## Dependency Updates
- **microsphere-spring** bumped to `0.2.35`. [f8ce07c](https://example.com/commit/f8ce07c)
- **microsphere-build** parent updated to:
  - `0.3.8`. [64b3d43](https://example.com/commit/64b3d43)
  - `0.3.7`. [8e40682](https://example.com/commit/8e40682)
  - `0.3.6`. [aaf78d2](https://example.com/commit/aaf78d2)

## Documentation
- Clarified Spring Boot version comments in code. [f57b920](https://example.com/commit/f57b920)
- Updated README with the latest release versions. [6aa1a25](https://example.com/commit/6aa1a25)

## Test Improvements
- Refactored auto-configuration property tests. [890cb5a](https://example.com/commit/890cb5a)
- Aligned test dependencies across Spring Boot modules. [96f2c0a](https://example.com/commit/96f2c0a)

## Build and Workflow Enhancements
- Merged `main` into `release` branch for sync. [177dae0](https://example.com/commit/177dae0), [6b2c843](https://example.com/commit/6b2c843), etc.

## Other Changes
- Removed `spring.factories` entry for actuator. [c1d37ed](https://example.com/commit/c1d37ed)
- Annotated API sources in the auto-config class. [97a1cf0](https://example.com/commit/97a1cf0)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.29...0.2.30## v0.2.31

# Release Notes - Version 0.2.31

## Test Improvements
- Unified auto-configuration test runners and added reactive test cases. [eae9ade]
- Scoped auto-configuration to concrete WebFlux tests. [c674539]
- Scoped test auto-configuration using `AutoConfigureAfter`. [c60adc9]
- Updated reactive test configuration class conditions. [e2f58c2]
- Used `JmxProperties`, `JacksonProperties`, and `CacheProperties` in reactive auto-configuration tests for consistency. [d0f5320], [9fbeccc], [ec476bf], [8093e1b], [7d88e4f]
- Removed unused imports from auto-configuration tests. [0580d72]

## Dependency Updates
- Added Boot processor dependencies to module POMs for better alignment. [6e5316e]
- Aligned module POM dependencies and updated comments. [eff01fe]

## Documentation
- Updated `README` branch version matrix. [6989c9a]

## Other Changes
- Used string class names in WebMVC and WebFlux availability conditions for improved readability. [46215e7], [d610bc1]

---

*Full changelog between version 0.2.30 and 0.2.31 can be found [here](#).*

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.2.30...0.2.31