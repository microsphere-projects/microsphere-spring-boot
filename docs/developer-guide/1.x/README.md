# Developer Guide — `release-1.x` branch (0.1.x)

> [中文](./zh/README.md) · [Developer Guide home](../README.md) ·
> [User Guide for this branch](../../user-guide/1.x/README.md)

> **Branch:** `release-1.x` · **Version line:** `0.1.x` · **Artifacts:** `microsphere-spring-boot-*`
> **Spring Boot:** `2.1.18.RELEASE` – `2.7.18` (profile-selected, default `2.7.18`) · **Java:** 8 … 25 in CI

Contents:
[1 What This Branch Ships](#1-what-this-branch-ships) ·
[2 Repository Shape](#2-repository-shape) ·
[3 Module Dependency Graph](#3-module-dependency-graph) ·
[4 Extension-Point Inventory](#4-extension-point-inventory) ·
[5 Dual Auto-Configuration Registration](#5-dual-auto-configuration-registration) ·
[6 Cross-Branch Source Differences](#6-cross-branch-source-differences) ·
[7 The Shim Direction Is Reversed](#7-the-shim-direction-is-reversed) ·
[8 Build and CI Differences](#8-build-and-ci-differences) ·
[9 Module Documents](#9-module-documents) ·
[10 Contribution Checklist](#10-contribution-checklist)

---

## 1. What This Branch Ships

`release-1.x` maintains the `0.1.x` line for Spring Boot `2.1` through `2.7`. The class inventory is the same
as the `release` branch — the same 47 core classes, 10 actuator classes, 3 + 3 web classes and 4 test base
classes — but the *implementations* are the Java 8 / Boot 2 compatible variants, and the registration files
carry both the legacy and the modern form.

| Module | Artifact | Published | In BOM | Main sources | Tests |
|---|---|---|---|---|---|
| `microsphere-spring-boot-parent` | pom | yes | no | — | — |
| `microsphere-spring-boot-dependencies` | pom (BOM) | yes | — | — | — |
| `microsphere-spring-boot-compatible` | jar | **no** | **no** | 12 files | 0 |
| `microsphere-spring-boot-test` | jar | yes | yes | 4 | 5 |
| `microsphere-spring-boot-core` | jar | yes | yes | 47 | 48 |
| `microsphere-spring-boot-webmvc` | jar | yes | yes | 3 | 5 |
| `microsphere-spring-boot-webflux` | jar | yes | yes | 3 | 4 |
| `microsphere-spring-boot-actuator` | jar | yes | yes | 10 | 5 |

Version facts from `pom.xml`: `<revision>0.1.35-SNAPSHOT</revision>`, and **no `<java.version>` property** —
the Java level comes from the external `microsphere-build:0.3.14` parent rather than this repository.

---

## 2. Repository Shape

The module layout is identical to `release`; only the POM contents and the resource files differ.

```text
microsphere-spring-boot/                    revision 0.1.35-SNAPSHOT, no java.version
├── pom.xml                                 the same 8 <modules>, in the same order
├── microsphere-spring-boot-parent/         profiles spring-boot-2.1 … spring-boot-2.7
├── microsphere-spring-boot-dependencies/   the same five managed artifacts
├── microsphere-spring-boot-compatible/     12 shims, attached by profiles 2.1 / 2.2 / 2.3
├── microsphere-spring-boot-test/           same four base classes
├── microsphere-spring-boot-core/           same 47 classes, Boot 2 / Java 8 implementations
├── microsphere-spring-boot-webmvc/           javax.servlet-based condition
├── microsphere-spring-boot-webflux/
├── microsphere-spring-boot-actuator/       micrometer pinned to 1.3.20 under profile 2.1
└── .github/workflows/                      maven-build, maven-publish, sync-branches-from-upstream
```

Notable absence: this branch has **no** `merge-main-to-branches.yml`, and its publish workflow merges back into
`dev-1.x` rather than `main`. The `0.1.x` line is therefore self-contained: nothing flows from it into the
`0.2.x` line automatically, and shared fixes are ported by hand.

---

## 3. Module Dependency Graph

```mermaid
graph TD
    PARENT["microsphere-spring-boot-parent"]
    BOM["microsphere-spring-boot-dependencies (BOM)"]
    COMPAT["microsphere-spring-boot-compatible<br/>(never published)"]
    TEST["microsphere-spring-boot-test"]
    CORE["microsphere-spring-boot-core"]
    MVC["microsphere-spring-boot-webmvc"]
    FLUX["microsphere-spring-boot-webflux"]
    ACT["microsphere-spring-boot-actuator"]

    PARENT -. inheritance .-> BOM
    PARENT -. inheritance .-> COMPAT
    PARENT -. inheritance .-> TEST
    PARENT -. inheritance .-> CORE
    PARENT -. inheritance .-> MVC
    PARENT -. inheritance .-> FLUX
    PARENT -. inheritance .-> ACT

    BOM -. version management .-> CORE
    BOM -. version management .-> MVC
    BOM -. version management .-> FLUX
    BOM -. version management .-> ACT

    CORE -. test scope .-> TEST
    MVC --> CORE
    FLUX --> CORE
    ACT --> CORE
    CORE -. profiles 2.1 and 2.2 and 2.3 .-> COMPAT
    COMPAT -. own profile 2.1 pins spring-core 5.2.25.RELEASE .-> COMPAT2["spring-core"]
    ACT -. profile 2.1 pins micrometer-core 1.3.20 .-> MICRO["micrometer-core"]
```

Two edges have no counterpart on the `release` branch:

* `microsphere-spring-boot-compatible/pom.xml` carries its own `spring-boot-2.1` profile pinning
  `spring-core:5.2.25.RELEASE` (optional), because the shim sources must compile against the oldest supported
  Spring Framework, not the one Boot 2.1 happens to ship.
* `microsphere-spring-boot-actuator/pom.xml` carries a `spring-boot-2.1` profile pinning
  `io.micrometer:micrometer-core:1.3.20`.

---

## 4. Extension-Point Inventory

`core/src/main/resources/META-INF/spring.factories` registers the same ten classes as `release`, plus one extra
key. Verbatim:

| Key | Registered classes |
|---|---|
| `ApplicationContextInitializer` | `report.ConditionEvaluationReportInitializer`, `env.config.OriginTrackedConfigurationPropertyInitializer` |
| `SpringApplicationRunListener` | `classloading.BannedArtifactClassLoadingListener`, `listener.FailureReportSpringApplicationRunListener` |
| `ApplicationListener` | `diagnostics.ArtifactsCollisionDiagnosisListener`, `report.ConditionEvaluationReportListener`, `env.DefaultPropertiesApplicationListener` |
| `SpringBootExceptionReporter` | `report.ConditionEvaluationSpringBootExceptionReporter` |
| `FailureAnalyzer` | `diagnostics.ArtifactsCollisionFailureAnalyzer` |
| `AutoConfigurationImportFilter` | `autoconfigure.ConfigurableAutoConfigurationImportFilter` |
| `io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor` | `env.SpringApplicationDefaultPropertiesPostProcessor` |
| `EnableAutoConfiguration` | `context.autoconfigure.ConfigurationPropertiesAutoConfiguration` |

Ordering constants match the `release` branch: `HIGHEST_PRECEDENCE` for the banned-artifact listener,
`HIGHEST_PRECEDENCE + 99` for the import filter, `LOWEST_PRECEDENCE - 1` for the default-properties listener.
Lifecycle ordering and the binding instrumentation path are therefore the same as documented in
[../current/README.md](../current/README.md); only the implementations differ (§6).

---

## 5. Dual Auto-Configuration Registration

Every module that contributes auto-configuration carries **both** registration mechanisms:

| Module | `META-INF/spring.factories` | `META-INF/spring/AutoConfiguration.imports` |
|---|---|---|
| `core` | `EnableAutoConfiguration=...ConfigurationPropertiesAutoConfiguration` | same single class |
| `actuator` | `EnableAutoConfiguration=...ActuatorAutoConfiguration, ...ActuatorEndpointsAutoConfiguration` | the same two classes |
| `webmvc` | `EnableAutoConfiguration=...WebMvcAutoConfiguration` | same single class |
| `webflux` | `EnableAutoConfiguration=...WebFluxAutoConfiguration` | same single class |

Why both: `AutoConfiguration.imports` was introduced in Spring Boot `2.7`, and Boot `2.7` reads both files while
`2.1`–`2.6` read only `spring.factories`. One file per mechanism keeps a single artifact working across the whole
line. Consequences a maintainer must respect:

* Adding an auto-configuration class means editing **two** files; forgetting `spring.factories` silently drops
  support for Boot `2.1`–`2.6`.
* Listing a class in both files on Boot `2.7` is harmless (Boot de-duplicates), which is why the redundancy is
  accepted rather than resolved by profile-specific resources.
* On `release` the `spring.factories` auto-configuration key is gone entirely — the branch only targets Boot 3,
  where the legacy key is ignored.

---

## 6. Cross-Branch Source Differences

The following table was produced by diffing the two branch trees file by file. It is the authoritative list of
what a maintainer porting a fix between the lines has to reconcile in main sources (test sources differ in the
same spirit).

| File | `release` | `release-1.x` | Reason |
|---|---|---|---|
| `core/.../SpringBootVersion.java` | enum constants `SPRING_BOOT_3_0 … 4.x` | `SPRING_BOOT_2_0 … 2.7` | each line enumerates its own supported versions |
| `core/.../listener/SpringApplicationRunListenerAdapter.java` | `@Deprecated(since = "0.2.9", forRemoval = true)` | not deprecated; `starting(ConfigurableBootstrapContext)` delegates to `starting()` | the deprecation was introduced later on the 0.2.x line; porting code must not copy the annotation into a 0.1.x release |
| `core/.../context/properties/bind/util/BindUtils.java` | `import static ...BindConstructorProvider.DEFAULT` and a direct call | the class is loaded by name (`ClassLoaderUtils.loadClass`) and invoked by reflection; `getBindConstructor(...)` returns `null` when the provider is absent, with the comment `// Spring Boot < 2.2.1` | `BindConstructorProvider` does not exist before Boot 2.2.1 |
| `core/.../context/properties/source/util/ConfigurationPropertyUtils.java` | calls `DataObjectPropertyName.toDashedForm(...)` directly, and `name.getParent()` | resolves `DataObjectPropertyName` by name with a hand-written `toDashedForm(String, int)` fallback, and computes the parent with `name.chop(numberOfElements - 1)` guarded by `EMPTY` | `DataObjectPropertyName` is a later addition; `getParent()` is not usable across this Boot range |
| `core/.../context/properties/bind/ListenableBindHandlerAdapter.java` | `super.onCreate(name, target, context, result)` | resolves a `MethodHandle` for `AbstractBindHandler.onCreate` through a trusted lookup (`MethodHandleUtils.LookupMode.TRUSTED`) and falls back to the raw result when the handle is unavailable (`NOT_FOUND_METHOD_HANDLE`) | `onCreate` is not reachable via `super` on this Boot range |
| `core/.../context/properties/bind/ConfigurationPropertiesBeanContext.java` | `BeanDefinition beanDefinition = ...; beanDefinition.getResolvableType()` | casts to `RootBeanDefinition` and uses `BeanDefinitionUtils.getResolvableType(beanDefinition)` | the type resolution API differs on the older `BeanDefinition` implementation |
| `core/.../autoconfigure/ConfigurableAutoConfigurationImportFilter.java` | pattern matching: `property instanceof String exclude` | Java 8 form: `instanceof String` plus an explicit cast | language level |
| `core/.../diagnostics/ArtifactsCollisionDiagnosisListener.java` | `artifact instanceof MavenArtifact mavenArtifact` | Java 8 form with explicit cast | language level |
| `core/.../util/SpringApplicationUtils.java` | text block for the log pattern, `switch` expression with `isTraceEnabled()` guards, logs `additionalProfiles` | string concatenation, classic `switch` with `break`, no `additionalProfiles` argument | Java 8 language level, and one fewer reported field |
| `actuator/.../endpoint/ConfigurationMetadataEndpoint.java` | `ConfigurationMetadataDescriptor implements OperationResponseBody` | plain DTO, no interface | `OperationResponseBody` arrived after this line's oldest target |
| `actuator/.../endpoint/ConfigurationPropertiesEndpoint.java` | `ConfigurationPropertiesDescriptor implements OperationResponseBody` | plain DTO, no interface | same |
| `webmvc/.../WebMvcAutoConfiguration.java` | `@AutoConfigureAfter(name = {...autoconfigure.web.servlet.WebMvcAutoConfiguration, ...webmvc.autoconfigure.WebMvcAutoConfiguration})` | only the `org.springframework.boot.autoconfigure.web.servlet` name | the second name is Boot `4.0+`, which cannot be a target here |
| `webmvc/.../condition/ConditionalOnWebMvcAvailable.java` | `@ConditionalOnClass(name = {"jakarta.servlet.Servlet", ...})` | `"javax.servlet.Servlet"` | the Servlet namespace split at Boot 3.0 |
| `webflux/.../WebFluxAutoConfiguration.java` | two `@AutoConfigureAfter` names (Boot `2.0–4.0` and Boot `4.0+`) | only `org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration` | same as webmvc |
| `compatible/.../package-info.java` | "forked from the Spring Boot 3.5.x" | "forked from the Spring Boot 2.7.x(latest)" | each line forks from its newest supported Boot |
| POMs (root, parent, core, actuator, test, compatible) | `microsphere-build:0.3.16`, `microsphere-spring:0.2.39`, `jolokia:2.6.3`, `junit-jupiter:6.1.3`, Boot 3/4 profiles, `spring-milestone` repository, `jakarta.annotation-api` test dep in `core` | `microsphere-build:0.3.14`, `microsphere-spring:0.1.39`, `jolokia:1.7.2`, `junit-jupiter:5.14.4`, Boot 2 profiles with `.RELEASE` suffixes, no repositories block, no `jakarta.annotation-api` | version-line management |

The recurring pattern: **on this line, an API that may be absent is reached reflectively or by name; on
`release`, an API that moved is referenced by name in a condition.** Both avoid a hard dependency on a class
that one Boot generation does not have at that coordinate.

---

## 7. The Shim Direction Is Reversed

On `release`, `compatible` supplies classes that Spring Boot **removed or relocated in Boot 4**, so profiles
`4.0`/`4.1` attach it. On `release-1.x`, it supplies classes that early Boot 2 **did not have yet**, so profiles
`2.1`/`2.2`/`2.3` attach it (`microsphere-spring-boot-core/pom.xml` lines 91–127).

| Shim on this line | Boot version that introduced it | Needed by profiles |
|---|---|---|
| `BootstrapContext`, `BootstrapRegistry`, `ConfigurableBootstrapContext`, `DefaultBootstrapContext`, `BootstrapContextClosedEvent` | 2.4.0 | 2.1, 2.2, 2.3 |
| `actuate.endpoint.EndpointId` | 2.0.6 (`of(Environment, String)` only from 2.2.0) | 2.1, 2.2, 2.3 |
| `actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint` and its `AbstractEndpointCondition` / `OnAvailableEndpointCondition` | 2.2.0 | 2.1, 2.2, 2.3 |
| `actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter` | 2.2.7 | 2.1, 2.2, 2.3 |
| `context.properties.ConstructorBinding` | 2.2.0 | 2.1, 2.2, 2.3 |

The binding constraint is the bootstrap API (`@since 2.4.0`): `core`'s `SpringApplicationRunListenerAdapter` and
`LoggingSpringApplicationRunListener` take a `ConfigurableBootstrapContext` in their signatures, which Boot 2.1
– 2.3 do not provide. Forked sources here come from Boot `2.7.x`, the newest version this line supports.

---

## 8. Build and CI Differences

| Aspect | `release` | `release-1.x` |
|---|---|---|
| CI JDK matrix | `17`, `21`, `25` | `8`, `11`, `17`, `21`, `25` |
| CI Boot profiles | 8 (`3.0` … `4.1`) | 7 (`2.1` … `2.7`) |
| Matrix cells | 24 | 35 |
| Build triggers | push `main`, `dev`; PR `main`, `dev`, `release` | push `dev-1.x`; PR `dev-1.x`, `release-1.x` |
| Action versions | `checkout@v7.0.1`, `setup-java@v6.0.1`, `codecov-action@v7.1.1` | `checkout@v5`, `setup-java@v5`, `codecov-action@v5` |
| Publish JDK | `17` | `11` |
| Publish branch | push to `release` | push to `release-1.x` |
| Post-release merge | `release` → `main` | `release-1.x` → `dev-1.x` |
| `merge-main-to-branches.yml` | present | **absent** |
| Boot version literals | plain (`3.5.16`, `4.1.1`) | `.RELEASE` suffix on `2.1.18.RELEASE`, `2.2.13.RELEASE`, `2.3.12.RELEASE`; plain from `2.4.13` on |
| Coverage upload slug | `microsphere-projects/microsphere-spring-boot` | the same slug, from both lines |

The build command is otherwise identical:
`mvn --batch-mode --update-snapshots --file pom.xml -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles test,coverage,<profile>`.

Consequences for local work: JDK 8 and 11 are required to reproduce the low end of the matrix; a Boot 4 profile
does not exist here, and selecting one fails with an "unknown profile" warning plus the default `2.7.18` line.

---

## 9. Module Documents

| Document | Scope |
|---|---|
| [core.md](./core.md) | The Boot 2 implementations: reflective capability probing, Java 8 idioms, `SpringBootVersion` range |
| [actuator.md](./actuator.md) | Endpoint model on Boot 2, the shim-provided endpoint conditions, micrometer pin |
| [webmvc.md](./webmvc.md) | `javax.servlet` availability condition and single-name ordering |
| [webflux.md](./webflux.md) | Reactive-stack auto-configuration on Boot 2 |
| [test.md](./test.md) | Base classes on the 0.1.x line and their JUnit 5 bounds |
| [compatible.md](./compatible.md) | The twelve shims and the profiles that attach them |
| [parent.md](./parent.md) | Boot 2 profile table, `.RELEASE` suffixes, no milestone repository |
| [dependencies.md](./dependencies.md) | BOM for the 0.1.x artifacts and Boot-line pairing |
| [build-and-release.md](./build-and-release.md) | 35-cell CI matrix, publishing from `release-1.x`, `dev-1.x` merge-back |

The `release` equivalents describe the same architectures; read those first and this directory for the deltas.

---

## 10. Contribution Checklist

Deltas on top of the branch-independent checklist in
[../current/README.md](../current/README.md):

1. **Stay Java 8 compatible.** No pattern-matching `instanceof`, no text blocks, no `switch` expressions, no
   `var`. Verify with `./mvnw -Pspring-boot-2.1 test` on JDK 8 or 11.
2. **Stay Boot 2.1 compatible.** Any Boot API newer than this line's floor is reached by name or by reflection,
   with a null/silent fallback — follow `BindUtils` and `ConfigurationPropertyUtils` rather than adding a hard
   import.
3. **Register auto-configuration twice**: `spring.factories` under `EnableAutoConfiguration` **and**
   `AutoConfiguration.imports`.
4. **Do not port `@Deprecated` markers** from `release` (notably `SpringApplicationRunListenerAdapter`) unless
   the equivalent deprecation was announced on this line.
5. **Shim changes go to `compatible`**, and the attaching profiles are `2.1`/`2.2`/`2.3` — never a Boot 3 or 4
   profile, which does not exist here.
6. **Docs**: `docs/developer-guide/1.x/<module>.md` plus `docs/developer-guide/1.x/zh/<module>.md`, and the
   matching user-guide pair when the change is user-visible.
7. **Release** is cut from `release-1.x` with the publish workflow; the resulting version belongs to the `0.1.x`
   range and merges into `dev-1.x`, not `main`.
