# Source Diff — `src/test`: tag `0.2.34` vs tag `0.1.34`

Comparison scope: the two newest tags, `0.2.34` (Java 17 / Spring Boot 3.5.x–4.x line) and `0.1.34` (Java 8 / Spring Boot 2.x line). Diff direction is `0.1.34 → 0.2.34` ("new" means `0.2.34`). This file covers `src/test` only; `src/main` is compared in [main-diff-0.2.34-vs-0.1.34.md](./main-diff-0.2.34-vs-0.1.34.md).

- All modules match by name; `src/test` sources exist only in `actuator`, `core`, `test`, `webflux`, `webmvc` (`compatible` has none in both tags).
- Excluded difference classes: **Java 17 vs Java 8 syntax/API** (pattern-matching `instanceof`, etc.) and **Java EE vs Jakarta EE** (`javax.annotation/javax.servlet → jakarta.*`).

## Module Overview

| Maven module | test files `0.1.34` | test files `0.2.34` | Changed files | Substantive differences (after exclusions) |
|---|---|---|---|---|
| actuator | 6 | 6 | 0 | 0 |
| compatible | 0 | 0 | 0 | N/A |
| core | 53 | 53 | 10 | 9 (1 excluded) |
| dependencies / parent | 0 | 0 | 0 | N/A |
| test | 5 | 5 | 2 | 2 |
| webflux | 4 | 4 | 0 | 0 |
| webmvc | 5 | 5 | 2 | 2 (mixed with 1 excluded import change) |

## microsphere-spring-boot-core (src/test)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `SpringBootVersionTest` | Asserts ranges/operators over `SPRING_BOOT_2_0…2_7` constants | Asserts ranges/operators over `SPRING_BOOT_3_0…4_1` constants (e.g. 3.5.0–3.5.14, 4.0.0–4.0.7) | **Mirrors main change**: the `SpringBootVersion` enum was re-based; the test simply follows the new catalog |
| `ApplicationAutoConfigurationTest` | `import javax.annotation.Resource` | `import jakarta.annotation.Resource` | Excluded (Java EE → Jakarta EE) |
| `LoggingOnceMainApplicationPreparedEventListenerTest` | Helper builds a context and sets the empty `URLClassLoader` on `DefaultListableBeanFactory` | Sets the empty class loader directly on the `GenericApplicationContext`; helper methods deleted | **Test simplification**: a context-level class loader covers all lookups on the Boot 3 baseline, making the bean-factory-level plumbing unnecessary |
| `BindableConfigurationBeanBinderTest` | `assertThrows(BindException…)` wrapped in `if (CURRENT.ge(SPRING_BOOT_2_2))` | Unconditional `assertThrows` | **Removal of cross-version guard**: 0.2.x only supports Boot ≥ 3.0, so the Boot-2.2 branch is dead code |
| `TestConstructorBindingConfigurationProperties` | Boot 2 `context.properties.ConstructorBinding` on the constructor, mutable fields + setters + no-arg constructor | Boot 3 `context.properties.bind.ConstructorBinding` import; `final` fields, constructor-only binding | **API compatibility**: annotation moved packages in Boot 2.2+/removed from old FQCN in Boot 3; constructor binding now implied by a single constructor |
| `EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest` | Tests `ServerProperties` + `TestConfigurationProperties` changes only | Adds `WebProperties` (`spring.web.locale`, `locale-resolver`, `static-locations`) as a bound bean and a new `testWebProperties` case asserting the change-event old/new values | **Feature need / expanded coverage**: change-propagation is now proven on a third property class with nested + list binding; `WebProperties` exists since Boot 2.4/3.x |
| `ListenableBindHandlerAdapterTest` | Branches on `onCreateMethodHandle == NOT_FOUND_METHOD_HANDLE`; reflective `Binder.bindOrCreate` invocation | Direct `binder.bindOrCreate(...)` call; `createBinder` helpers return `BindHandler` instead of `ListenableBindHandlerAdapter` | **Mirrors main change**: reflective fallback removed from `ListenableBindHandlerAdapter`, so the test drops its version-branching too |
| `BindUtilsTest` | `getBindConstructor(of(ServerProperties.class), false/true)` expected `null`, including for the constructor-bound test class; assertions for `TestBindConstructorClass` commented out | `getBindConstructor(forClass(TestConstructorBindingConfigurationProperties.class))` expected **non-null**; commented-out assertions re-enabled (now expecting `null`) | **Mirrors main change**: the direct `BindConstructorProvider.DEFAULT` call resolves bind constructors differently from the old reflective path, flipping the observable outcome |
| `ConfigurationPropertyUtilsTest` | Includes `testToDashedFormWithStart` comparing `toDashedForm(name)` to `toDashedForm(name, 0)` | Test and helper removed | **Mirrors main change**: the protected fallback overload `toDashedForm(name, start)` was deleted |
| `ArtifactsCollisionFailureAnalyzerTest` | Class annotated `@Component` (and imports it) | `@Component` removed | **Test cleanup**: a test class must not be a scanned component; irrelevant on either line but fixed while modernizing |

## microsphere-spring-boot-test (src/test)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `ReactiveWebApplicationAutoConfiguration` | `@ConditionalOnClass` + `@EnableConfigurationProperties` use `JacksonProperties` | Use `JmxProperties` instead | **Avoid fork collision**: in 0.2.x `org.springframework.boot.autoconfigure.jackson.JacksonProperties` is a *forked* class inside the unpublished `compatible` module (attached under Boot 4 profiles); switching the fixture to `JmxProperties` (never forked) keeps the auto-config condition testing Boot's own class |
| `ReactiveWebApplicationAutoConfigurationTest` | Adds `JacksonProperties` to expected auto-configured/missing class sets | Adds `JmxProperties` | Same — follows the fixture change |

## microsphere-spring-boot-webmvc (src/test)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `AbstractWebMvcAutoConfigurationTest` | Context loads `TestController` only; 5 endpoint checks | Adds `RouterFunctionTestConfig` to `@SpringJUnitConfig` and a new `testUpdatePerson()` (`PUT /test/person/{id}`) to `testWebEndpoints()` (the `javax→jakarta ServletException` import is excluded) | **Feature need / expanded coverage**: functional tests now also verify `RouterFunction` endpoints alongside annotated controllers |
| `WebMvcAutoConfigurationAllEnabledTest` | Asserts exactly 2 content-negotiation strategies, fixed order, strategy #2 is `ParameterContentNegotiationStrategy("p")` | Asserts the list is non-empty and any `ParameterContentNegotiationStrategy` found carries parameter name `"p"` | **Assertion adaptation**: Boot 3/4 default strategy composition and ordering differ from Boot 2, so the brittle positional assertion was relaxed to a membership check |

## Summary of Causes

| Cause category | Instances |
|---|---|
| Mirroring main-side removal of reflection/cross-version fallbacks | `ListenableBindHandlerAdapterTest`, `BindUtilsTest`, `ConfigurationPropertyUtilsTest`, `BindableConfigurationBeanBinderTest` |
| Spring Boot API relocation (constructor binding) | `TestConstructorBindingConfigurationProperties` |
| Version catalog re-base | `SpringBootVersionTest` |
| Expanded functional coverage | `EventPublishing…Test` (WebProperties), `AbstractWebMvcAutoConfigurationTest` (RouterFunction) |
| Fixture adjustment to avoid forked-class collision | `ReactiveWebApplicationAutoConfiguration(+Test)` |
| Test simplification / cleanup | `LoggingOnce…Test`, `ArtifactsCollisionFailureAnalyzerTest` |
| Assertion robustness under Boot 3/4 defaults | `WebMvcAutoConfigurationAllEnabledTest` |
| Excluded: Java EE → Jakarta EE | `ApplicationAutoConfigurationTest`, `AbstractWebMvcAutoConfigurationTest` import |
