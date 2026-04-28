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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-spring-boot/compare/0.1.10...0.1.11