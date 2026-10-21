# Source Diff — `src/main`: tag `0.2.34` vs tag `0.1.34`

Comparison scope: the two newest tags, `0.2.34` (Maven `revision` = `0.2.34-SNAPSHOT`, Java 17 / Spring Boot 3.5.x–4.x line) and `0.1.34` (Maven `revision` = `0.1.34-SNAPSHOT`, Java 8 / Spring Boot 2.x line). Diff direction is `0.1.34 → 0.2.34` ("new" means `0.2.34`). Only `src/main` content is covered here; `src/test` is compared separately in [test-diff-0.2.34-vs-0.1.34.md](./test-diff-0.2.34-vs-0.1.34.md).

- All 8 Maven modules exist under the same names in both tags; `dependencies` and `parent` contain no `src/main` sources (N/A).
- Excluded difference classes: **Java 17 vs Java 8 syntax/API** (pattern-matching `instanceof`, text blocks, arrow `switch`, `@Deprecated(forRemoval)`, `String.strip()`) and **Java EE vs Jakarta EE** (`javax.* → jakarta.*`).

## Module Overview

| Maven module | main files `0.1.34` | main files `0.2.34` | Changed entries | Substantive differences (after exclusions) |
|---|---|---|---|---|
| actuator | 14 | 13 | 2 java (M), 1 resource (D) | 3 |
| compatible | 12 | 10 | 6 java (M), 6 java (D), 4 java (A) | Fork inventory re-anchored Boot 2.7 → Boot 3.5 |
| core | 50 | 50 | 11 java (M), 1 resource (M) | 10 |
| dependencies | 0 | 0 | 0 | N/A |
| parent | 0 | 0 | 0 | N/A |
| test | 4 | 4 | 0 | 0 |
| webflux | 5 | 4 | 1 java (M), 1 resource (D) | 2 |
| webmvc | 5 | 4 | 2 java (M), 1 resource (D) | 2 (1 java + 1 resource; the other java change is excluded) |

## microsphere-spring-boot-actuator (src/main)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `ConfigurationMetadataEndpoint` | `ConfigurationMetadataDescriptor` is a plain POJO | Descriptor `implements OperationResponseBody` | **API compatibility**: Spring Boot 3 actuator serialization requires `OperationResponseBody` as the return-value marker |
| `ConfigurationPropertiesEndpoint` | `ConfigurationPropertiesDescriptor` is a plain POJO | Descriptor `implements OperationResponseBody` | **API compatibility**: same as above |
| `META-INF/spring.factories` | Registers both auto-configurations under `EnableAutoConfiguration=` | Deleted (registration kept only in `AutoConfiguration.imports`) | **API compatibility**: Boot 3 removed `spring.factories` auto-configuration support; the `.imports` file already existed on both tags, so `0.2.34` drops the legacy duplicate |

## microsphere-spring-boot-compatible (src/main)

Fork baseline changed from **Spring Boot 2.7.x** to **Spring Boot 3.5.x** (stated in `package-info.java`). Reason: the module supplies Boot-version classes missing on the *supported lower bound* of each line — Boot 2.1–2.7 for `0.1.x`, Boot 3.0 for `0.2.x`.

### Modified with no substantive content change

| File | Difference |
|---|---|
| `BootstrapContext`, `BootstrapContextClosedEvent`, `BootstrapRegistry`, `ConfigurableBootstrapContext`, `DefaultBootstrapContext` | Missing trailing newline at EOF on `0.1.34` only (whitespace) |
| `package-info` (root package) | Comment updates the fork baseline 2.7.x → 3.5.x |

### Only in `0.1.34` — Boot 2 shims, removed in `0.2.34`

| File | Reason for removal |
|---|---|
| `actuate/autoconfigure/endpoint/condition/AbstractEndpointCondition` | **API compatibility**: fork of Boot 2.0–2.6 endpoint-condition internals; the API exists natively since Boot 2.7/3.x |
| `actuate/autoconfigure/endpoint/condition/ConditionalOnAvailableEndpoint` | Same — shim annotation no longer needed |
| `actuate/autoconfigure/endpoint/condition/OnAvailableEndpointCondition` | Same — companion condition implementation |
| `actuate/autoconfigure/endpoint/expose/IncludeExcludeEndpointFilter` | Same — Boot 2 filter API (incl. deprecated `DefaultIncludes`); Boot 3 uses `EndpointExposure` |
| `actuate/endpoint/EndpointId` | Same — value type native in all supported Boot 3 versions |
| `context/properties/ConstructorBinding` | Same — Boot 3 moved `@ConstructorBinding` to `org.springframework.boot.context.properties.bind`; the old FQCN is obsolete |

### Only in `0.2.34` — new Boot 3.5 forks

| File | Reason for addition |
|---|---|
| `autoconfigure/jackson/JacksonProperties` | **Feature need**: property classes for the module's binding/auto-configuration tests and extensions must be attachable on the Boot 3.0 lower bound |
| `autoconfigure/web/ServerProperties` | **Feature need**: same; `String.strip()` usage inside is a Java 11 API — excluded as JDK difference |
| `autoconfigure/web/servlet/MultipartProperties` | **Feature need**: same |
| `web/servlet/MultipartConfigFactory` | **Feature need**: same; its `jakarta.servlet` import is the Boot 3 EE baseline — excluded |

## microsphere-spring-boot-core (src/main)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `SpringBootVersion` | Enum constants for Boot **2.0–2.7** (incl. 2.x patch ranges) | Enum constants for Boot **3.0–4.1** | **Feature need**: the version catalog must describe the versions the 0.2.x line supports |
| `ConfigurableAutoConfigurationImportFilter` | `instanceof` + cast | Pattern-matching `instanceof` | Excluded (Java 17 syntax) |
| `ConfigurationPropertiesBeanContext` | Casts merged definition to `RootBeanDefinition`, resolves type via `BeanDefinitionUtils.getResolvableType` | Uses `BeanDefinition.getResolvableType()` directly | **API compatibility**: newer Spring Framework exposes the type on the interface, so the reflection-era helper is unnecessary |
| `ListenableBindHandlerAdapter` | Reflective `MethodHandle` lookup of `AbstractBindHandler.onCreate` with a not-found fallback (method added in Boot 2.2.2) | Plain `super.onCreate(...)` | **API compatibility**: on a Boot ≥ 3.0 baseline the method always exists; the cross-version probe is dead weight |
| `BindUtils` | Reflective load of `BindConstructorProvider` + `DEFAULT` field, null-safe for Boot < 2.2.1 | Direct `BindConstructorProvider.DEFAULT.getBindConstructor(...)` | **API compatibility**: same — dynamic fallback for Boot < 2.2.1 dropped |
| `ConfigurationPropertyUtils` | Manual `getParent` via `chop()`/`EMPTY`; `toDashedForm` probes `DataObjectPropertyName` reflectively with a hand-copied `BeanPropertyName` fallback (`toDashedForm(name, start)` overload) | `ConfigurationPropertyName.getParent()`; direct `DataObjectPropertyName.toDashedForm`; fallback overload removed | **API compatibility**: the Boot 2.2.3-era private API is stable and public on Boot 3; fallback emulation for older Boot removed |
| `ArtifactsCollisionDiagnosisListener` | `instanceof` + cast | Pattern-matching `instanceof` | Excluded (Java 17 syntax) |
| `FailureReportSpringApplicationRunListener` | Not deprecated | `@Deprecated(forRemoval = true, since = "0.2.9")` | **Feature need / lifecycle**: Boot 3 replaces `SpringApplicationRunListener` hooks with `SpringApplicationEvent` listeners; users are steered away |
| `LoggingSpringApplicationRunListener` | Not deprecated | `@Deprecated(forRemoval = true, since = "0.2.9")` | Same |
| `SpringApplicationRunListenerAdapter` | `starting(ConfigurableBootstrapContext)` chains to `starting()`; not deprecated | Chained call removed; `@Deprecated(forRemoval = true)` | **API compatibility + behavior change**: Boot 3's `SpringApplicationRunListener` supplies default methods, so the adapter no longer fakes the no-arg `starting()` callback |
| `SpringApplicationUtils` | Startup log pattern lacks "additional profiles"; TRACE/DEBUG unguarded | Adds `additional profiles` fed by `SpringApplication.getAdditionalProfiles()`; TRACE/DEBUG guarded; text block + arrow `switch` | **Feature need**: richer startup diagnostics (API added in Boot 2.7/3). Guards avoid building the argument array when the level is off. Syntax parts excluded as Java 17 |
| `META-INF/spring.factories` | Also carries the `EnableAutoConfiguration=ConfigurationPropertiesAutoConfiguration` entry | That entry removed; non-autoconfiguration entries (`AutoConfigurationImportFilter`, `DefaultPropertiesPostProcessor`, …) kept | **API compatibility**: Boot 3 only reads auto-configurations from `AutoConfiguration.imports` (already present); other SPI keys remain valid in `spring.factories` |

## microsphere-spring-boot-dependencies / microsphere-spring-boot-parent

No `src/main` sources — N/A.

## microsphere-spring-boot-test (src/main)

No differences in `src/main` (4 files identical).

## microsphere-spring-boot-webflux (src/main)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `WebFluxAutoConfiguration` | `@AutoConfigureAfter` lists only `org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration` | Additionally lists `org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration` | **API compatibility**: Spring Boot 4.0 relocated the built-in auto-configuration; listing both names keeps ordering correct on Boot 3.x and 4.x |
| `META-INF/spring.factories` | Registers `WebFluxAutoConfiguration` via `EnableAutoConfiguration` | Deleted | **API compatibility**: Boot 3 registration moved to `AutoConfiguration.imports` (already shipped on both tags) |

## microsphere-spring-boot-webmvc (src/main)

| File | `0.1.34` | `0.2.34` | Reason for the difference |
|---|---|---|---|
| `WebMvcAutoConfiguration` | `@AutoConfigureAfter` lists only `org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration` | Additionally lists `org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` | **API compatibility**: Boot 4.0 relocation anchoring, same as webflux |
| `ConditionalOnWebMvcAvailable` | `@ConditionalOnClass` requires `javax.servlet.Servlet` | Requires `jakarta.servlet.Servlet` | Excluded (Java EE → Jakarta EE) |
| `META-INF/spring.factories` | Registers `WebMvcAutoConfiguration` via `EnableAutoConfiguration` | Deleted | **API compatibility**: Boot 3 `.imports` registration |

## Summary of Causes

| Cause category | Instances |
|---|---|
| API compatibility with Spring Boot 3/4 (registration mechanism, new marker interfaces, relocated classes, stronger bean-definition APIs) | actuator descriptors, 4 × `spring.factories`, webmvc/webflux ordering, `BindUtils`, `ListenableBindHandlerAdapter`, `ConfigurationPropertyUtils`, `ConfigurationPropertiesBeanContext` |
| Removal of cross-version reflection fallbacks (0.1.x must serve Boot 2.1–2.7; 0.2.x assumes Boot ≥ 3.0) | `BindUtils`, `ListenableBindHandlerAdapter`, `ConfigurationPropertyUtils`, `SpringApplicationRunListenerAdapter` |
| Compatible-module fork re-anchoring (Boot 2.7 shims → Boot 3.5 property classes) | 6 deletions + 4 additions in `compatible` |
| Feature needs (version catalog re-base, richer startup log, deprecation strategy) | `SpringBootVersion`, `SpringApplicationUtils`, 3 × `@Deprecated` listeners |
| Excluded: Java 8 vs 17 syntax/API | 2 × pattern-matching `instanceof`, text block/arrow `switch`, `String.strip()` |
| Excluded: Java EE vs Jakarta EE | `ConditionalOnWebMvcAvailable`, new multipart forks |
