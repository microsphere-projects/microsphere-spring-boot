# Parent POM

> [中文](./zh/parent.md) · [Documentation index](../README.md)

> **Branch:** `release-1.x` · **Version line:** `0.1.x` · **Artifact:** `microsphere-spring-boot-parent`
> **Packaging:** `pom` · **Spring Boot:** `2.1.18.RELEASE` – `2.7.18` (profile-selected) · **Java:** 8 baseline

Contents:
[1 Module Overview](#1-module-overview) ·
[2 Coordinates and Version Information](#2-coordinates-and-version-information) ·
[3 Differences from the 0.2.x Parent](#3-differences-from-the-02x-parent) ·
[4 POM Inheritance Chain](#4-pom-inheritance-chain) ·
[5 Managed Properties](#5-managed-properties) ·
[6 Dependency Management](#6-dependency-management) ·
[7 Spring Boot Version Profiles](#7-spring-boot-version-profiles) ·
[8 How to Use the Parent](#8-how-to-use-the-parent) ·
[9 What the Parent Does Not Do](#9-what-the-parent-does-not-do) ·
[10 Troubleshooting](#10-troubleshooting) ·
[11 See Also](#11-see-also)

---

## 1. Module Overview

`microsphere-spring-boot-parent` on the `release-1.x` branch plays the same role as it does on
`release`: it is the `<packaging>pom</packaging>` version layer that pins the Spring Boot line, imports
the third-party BOMs, and fixes the few versions no BOM manages. Every module in the reactor inherits
from it, so no dependency in this project carries a literal version.

The one difference that matters in practice is the *range*: this parent knows only Spring Boot `2.x`,
and the modules it governs are compiled against `javax.*` Servlet APIs rather than `jakarta.*`.

| Use | When |
|---|---|
| As `<parent>` | You maintain a Spring Boot 2.x application or a company reactor that must match these pins. |
| Not at all, only the BOM | You already inherit `spring-boot-starter-parent`. See [dependencies.md](./dependencies.md). |

---

## 2. Coordinates and Version Information

```xml
<groupId>io.github.microsphere-projects</groupId>
<artifactId>microsphere-spring-boot-parent</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
```

| Fact | Value on `release-1.x` |
|---|---|
| Version property source | Root aggregator `pom.xml`, `<revision>0.1.35-SNAPSHOT</revision>` |
| Last published release | `0.1.34` |
| Java baseline | Not declared in the root POM on this branch; CI verifies from JDK `8` through `25` |
| External build parent | `io.github.microsphere-projects:microsphere-build:0.3.14` |
| Spring Boot repositories declared | None (no `spring-milestone`) |
| Managed by the BOM? | **No** |
| Published to Central? | Yes |

---

## 3. Differences from the 0.2.x Parent

| Aspect | `release-1.x` (0.1.x) | `release` (0.2.x) |
|---|---|---|
| Build parent version | `microsphere-build:0.3.14` | `microsphere-build:0.3.16` |
| `microsphere-spring.version` | `0.1.39` | `0.2.39` |
| `jolokia.version` | `1.7.2` | `2.6.3` |
| `junit-jupiter.version` (bom) | `5.14.4` | `6.1.3` |
| Profiles | 7 (`spring-boot-2.1` … `2.7`) | 8 (`spring-boot-3.0` … `4.1`) |
| Default profile | `spring-boot-2.7` → `2.7.18` | `spring-boot-3.5` → `3.5.16` |
| Version suffix style | `2.1.18.RELEASE`, `2.2.13.RELEASE`, `2.3.12.RELEASE` (then plain) | plain, all lines |
| `<java.version>` in root POM | absent | `17` |
| `spring-milestone` repository | absent | present |
| Profiles that attach `compatible` to `core` | `2.1`, `2.2`, `2.3` (old Boot lacks the newer APIs) | `4.0`, `4.1` (new Boot moved those APIs) |

The `.RELEASE` suffix appears only on Boot `2.1`–`2.3`; from `2.4` onward Spring Boot dropped it. Since
the suffix is part of the Maven version string, `2.3.12.RELEASE` and `2.3.12` are *different*
artifacts to Maven — do not normalize it away.

---

## 4. POM Inheritance Chain

```mermaid
graph TD
    MB["microsphere-build : 0.3.14<br/>external build parent"]
    AGG["microsphere-spring-boot : ${revision}<br/>root aggregator<br/>revision=0.1.35-SNAPSHOT<br/>&lt;modules&gt; list"]
    PARENT["microsphere-spring-boot-parent<br/>spring-boot.version profiles<br/>dependencyManagement imports"]
    BOM["microsphere-spring-boot-dependencies<br/>manages the 5 published artifacts"]
    CORE["microsphere-spring-boot-core"]
    WEBMVC["microsphere-spring-boot-webmvc"]
    WEBFLUX["microsphere-spring-boot-webflux"]
    ACT["microsphere-spring-boot-actuator"]
    TEST["microsphere-spring-boot-test"]
    COMPAT["microsphere-spring-boot-compatible<br/>not published"]

    MB --> AGG
    AGG -->|relativePath ../pom.xml| PARENT
    PARENT --> BOM
    PARENT --> CORE
    PARENT --> WEBMVC
    PARENT --> WEBFLUX
    PARENT --> ACT
    PARENT --> TEST
    PARENT --> COMPAT

    CORE -. -Pspring-boot-2.1 / 2.2 / 2.3 .-> COMPAT

    classDef pom fill:#eef,stroke:#557,stroke-width:1px;
    class MB,AGG,PARENT,BOM pom;
```

Aggregation (`<modules>` in the root POM) decides what one build command compiles; inheritance
(`<parent>`) decides what a POM receives — properties, plugin configuration, `dependencyManagement`.
The eight `<module>` entries are identical on both branches, including `compatible`, which is built but
never deployed.

---

## 5. Managed Properties

From `microsphere-spring-boot-parent/pom.xml`:

| Property | Value | Purpose |
|---|---|---|
| `microsphere-spring.version` | `0.1.39` | Version of `microsphere-spring-dependencies`, the BOM for the `0.1.x` line of the sibling `microsphere-spring` project. |
| `jolokia.version` | `1.7.2` | `org.jolokia:jolokia-core`, the JMX bridge the Actuator module exercises. `1.7.2` is the last `javax`-based line, matching Boot 2. |
| `junit-jupiter.version` | `5.14.4` | Version of the `org.junit:junit-bom` import. |

From the root aggregator:

| Property | Value | Purpose |
|---|---|---|
| `revision` | `0.1.35-SNAPSHOT` | Single source of truth for the reactor version. |

`spring-boot.version` is defined only inside profiles, so its value depends entirely on which profile is
active.

---

## 6. Dependency Management

Identical in shape to the `release` branch:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>${junit-jupiter.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <dependency>
            <groupId>org.jolokia</groupId>
            <artifactId>jolokia-core</artifactId>
            <version>${jolokia.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-dependencies</artifactId>
            <version>${microsphere-spring.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

| Entry | Kind | Notes for this line |
|---|---|---|
| `junit-bom` | import | First, so its JUnit 5 versions win over what `spring-boot-dependencies` would suggest. |
| `jolokia-core` | direct | `1.7.2`; Boot 2's `javax` JMX stack. |
| `spring-boot-dependencies` | import | Also brings `javax.servlet:javax.servlet-api`, which the `test` module depends on as optional. |
| `microsphere-spring-dependencies` | import | `0.1.39` — the paired `microsphere-spring` release line. |

Because `spring-boot-dependencies` is imported with `${spring-boot.version}`, **which** Servlet API you
get is a consequence of profile choice, not of module code.

---

## 7. Spring Boot Version Profiles

| Profile | `spring-boot.version` | Notes |
|---|---|---|
| `spring-boot-2.1` | `2.1.18.RELEASE` | Also attaches `compatible` to `core`; `actuator` pins `micrometer-core`, and `compatible` pins `spring-core:5.2.25.RELEASE`. |
| `spring-boot-2.2` | `2.2.13.RELEASE` | Also attaches `compatible` to `core`. |
| `spring-boot-2.3` | `2.3.12.RELEASE` | Also attaches `compatible` to `core`. |
| `spring-boot-2.4` | `2.4.13` | |
| `spring-boot-2.5` | `2.5.15` | |
| `spring-boot-2.6` | `2.6.15` | |
| `spring-boot-2.7` | `2.7.18` | `<activeByDefault>true</activeByDefault>` |

Activating any profile explicitly deactivates the default one. A build with no `-P` targets
`2.7.18`.

```bash
# default line (2.7.18)
./mvnw clean verify

# oldest supported line, with the shim module in play
./mvnw clean verify -Pspring-boot-2.1

# inspect what a module resolves to
./mvnw -pl microsphere-spring-boot-core -am -Pspring-boot-2.3 help:effective-pom | grep spring-boot.version
```

The three extra pins under `spring-boot-2.1` exist because Boot 2.1's managed `micrometer-core` and
`spring-core` are older than what these modules' code compiles against; the profiles raise just those
artifacts instead of the whole Boot line.

---

## 8. How to Use the Parent

### 8.1 Inherit it

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-parent</artifactId>
    <version>0.1.34</version>
</parent>

<properties>
    <spring-boot.version>2.7.18</spring-boot.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-webmvc</artifactId>
        <!-- version managed by the BOM, not by this parent — see caveat -->
    </dependency>
</dependencies>
```

Caveat: the parent pins Spring Boot, `jolokia-core`, and the Microsphere *Spring* libraries, but the
five Microsphere *Spring Boot* artifacts are pinned by [dependencies.md](./dependencies.md). Import the
BOM as well, or state the version.

### 8.2 Choose a Boot line

```bash
mvn -Pspring-boot-2.7 clean package   # default
mvn -Pspring-boot-2.4 clean package   # 2.4.13
```

### 8.3 Match the JDK

CI on this branch runs the reactor on JDK `8`, `11`, `17`, `21`, and `25` against every profile. The
modules are written to `javax`/Java 8 source level, so building with a newer JDK is supported; targeting
a newer bytecode level is not what this line promises.

---

## 9. What the Parent Does Not Do

| Myth | Reality |
|---|---|
| "It manages the Microsphere Spring Boot artifact versions." | That is `microsphere-spring-boot-dependencies`. |
| "It configures `spring-boot-maven-plugin`." | It does not; that belongs to `spring-boot-starter-parent`. |
| "It sets a Java version." | The root POM on this branch has no `<java.version>` property. |
| "It declares a milestone repository." | It does not — this line is built entirely against GA releases. |
| "It makes `compatible` a published dependency." | `compatible` is skipped at deploy and publishing time on both branches. |

---

## 10. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Could not find artifact org.springframework.boot:spring-boot-starter:jar:2.1.18` | The Boot artifact is `2.1.18.RELEASE`; the suffix is part of the version. | Keep the `.RELEASE` suffix for `2.1`–`2.3`. |
| `'dependencies.dependency.version' ... is missing` | Profile name mistyped, so `spring-boot.version` never resolved. | Use a profile id from §7 exactly, e.g. `-Pspring-boot-2.5`. |
| `package jakarta.servlet does not exist` | A `0.2.x` artifact leaked into a `0.1.x` build. | Exclude it; the lines are not interchangeable. |
| `cannot find symbol class EndpointId` | You compile against Boot `2.1`–`2.3` *without* the profile, so `compatible` was not attached. | Build with `-Pspring-boot-2.1`/`2.2`/`2.3`. |
| Two JUnit versions in the test classpath | A transitive import listed before `junit-bom`. | Reorder so `junit-bom` (or your own pins) wins. |
| `${revision}` appears literally in an installed POM | Installed from an unflattened working tree. | Use published artifacts or the full reactor build. |

---

## 11. See Also

| Document | Scope |
|---|---|
| [dependencies.md](./dependencies.md) | The BOM for this line. |
| [core.md](./core.md) | The module every other artifact depends on. |
| [compatible.md](./compatible.md) | The 12-file shim set attached by profiles `2.1`–`2.3`. |
| [actuator.md](./actuator.md) | Where the `micrometer-core` pin lives. |
| [README.md](./README.md) | Branch overview and installation. |
