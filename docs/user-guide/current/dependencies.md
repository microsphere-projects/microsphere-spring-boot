# Dependencies BOM

> [中文](./zh/dependencies.md) · [Documentation index](../README.md)

> **Branch:** `release` · **Version line:** `0.2.x` · **Artifact:** `microsphere-spring-boot-dependencies`
> **Packaging:** `pom` · **Manages:** 5 artifacts · **Spring Boot:** `3.0.13` – `4.1.1`

Contents:
[1 Module Overview](#1-module-overview) ·
[2 Coordinates and Version Information](#2-coordinates-and-version-information) ·
[3 Managed Artifacts](#3-managed-artifacts) ·
[4 Why compatible and parent Are Excluded](#4-why-compatible-and-parent-are-excluded) ·
[5 Import the BOM](#5-import-the-bom) ·
[6 Dependency Graph](#6-dependency-graph) ·
[7 Version Alignment Rules](#7-version-alignment-rules) ·
[8 Verified Behavior](#8-verified-behavior) ·
[9 Troubleshooting](#9-troubleshooting) ·
[10 See Also](#10-see-also)

---

## 1. Module Overview

`microsphere-spring-boot-dependencies` is the consumer-facing BOM. It contains a single
`<dependencyManagement>` block that pins the five published library artifacts of this project to
`${revision}`, so an application can declare them without repeating versions:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webmvc</artifactId>
    <!-- version comes from the BOM -->
</dependency>
```

This is the recommended way to adopt the libraries in a Spring Boot application, because unlike the
[Parent POM](./parent.md) it does not take over your inheritance chain — `spring-boot-starter-parent`
can stay your parent.

---

## 2. Coordinates and Version Information

```xml
<groupId>io.github.microsphere-projects</groupId>
<artifactId>microsphere-spring-boot-dependencies</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
```

| Fact | Value on `release` |
|---|---|
| Version property source | Root aggregator, `<revision>0.2.35-SNAPSHOT</revision>` |
| Last published release | `0.2.34` |
| Own parent | `microsphere-spring-boot-parent` (`../microsphere-spring-boot-parent/pom.xml`) |
| Artifacts managed | `core`, `webmvc`, `webflux`, `actuator`, `test` |
| Java baseline of managed artifacts | 17 |

---

## 3. Managed Artifacts

| Artifact | Packaging | BOM entry | You normally depend on it |
|---|---|---|---|
| `microsphere-spring-boot-core` | jar | yes | Transitively, via any module below. |
| `microsphere-spring-boot-webmvc` | jar | yes | Servlet-stack applications. |
| `microsphere-spring-boot-webflux` | jar | yes | Reactive-stack applications. |
| `microsphere-spring-boot-actuator` | jar | yes | When you want the extra endpoints. |
| `microsphere-spring-boot-test` | jar | yes | `test` scope only. |
| `microsphere-spring-boot-compatible` | jar | **no** | Never — build-time shim, not published. |
| `microsphere-spring-boot-parent` | pom | **no** | Used as `<parent>`, not as a dependency. |
| `microsphere-spring-boot` (root aggregator) | pom | **no** | Reactor only. |

---

## 4. Why `compatible` and `parent` Are Excluded

**`compatible`** carries `maven-deploy-plugin` with `<skip>true</skip>` and
`central-publishing-maven-plugin` with `<skipPublishing>true</skipPublishing>`, so no artifact ever
reaches a repository. Listing it in the BOM would create a managed coordinate that cannot be resolved,
and a downstream build that happened to depend on it would fail at resolution time. Excluding it keeps
the BOM a promise: *everything listed here is downloadable.*

The module is attached at build time only, by the `spring-boot-4.0` and `spring-boot-4.1` profiles of
`microsphere-spring-boot-core/pom.xml`:

```xml
<profiles>
    <profile>
        <id>spring-boot-4.0</id>
        <dependencies>
            <dependency>
                <groupId>io.github.microsphere-projects</groupId>
                <artifactId>microsphere-spring-boot-compatible</artifactId>
                <version>${revision}</version>
            </dependency>
        </dependencies>
    </profile>
    <!-- spring-boot-4.1 declares the same dependency -->
</profiles>
```

**`parent`** is excluded because a parent POM and a managed dependency are different mechanisms. In the
rare case where you want both, the parent's own `dependencyManagement` still does not carry the
Microsphere artifacts, so importing this BOM remains the way to pin them.

---

## 5. Import the BOM

### 5.1 Standalone application (no Microsphere parent)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>
    <relativePath/>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>0.2.34</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-actuator</artifactId>
    </dependency>
</dependencies>
```

### 5.2 Inside a microsphere-spring-boot-parent build

Nothing to import for Spring Boot itself (the parent already imports `spring-boot-dependencies`), but
still import this BOM for the Microsphere coordinates:

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-parent</artifactId>
    <version>0.2.34</version>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>0.2.34</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 5.3 Multi-module company project

Import once in the company root POM; every child then declares artifacts without versions. Import order
in the child's `dependencyManagement` decides conflicts, so place your own pinned versions **above**
the Microsphere import if you need to override a module version.

---

## 6. Dependency Graph

```mermaid
graph TD
    APP["Your application"]
    BOM["microsphere-spring-boot-dependencies<br/>dependencyManagement only"]
    ACT["microsphere-spring-boot-actuator"]
    WEBMVC["microsphere-spring-boot-webmvc"]
    WEBFLUX["microsphere-spring-boot-webflux"]
    CORE["microsphere-spring-boot-core"]
    TEST["microsphere-spring-boot-test<br/>test scope"]
    COMPAT["microsphere-spring-boot-compatible<br/>not published, profile-attached"]
    SPRING["microsphere-spring-context<br/>from microsphere-spring-dependencies"]
    BOOT["spring-boot-starter<br/>from spring-boot-dependencies"]

    APP -. import .-> BOM
    BOM -. pins versions .-> ACT
    BOM -. pins versions .-> WEBMVC
    BOM -. pins versions .-> WEBFLUX
    BOM -. pins versions .-> CORE
    BOM -. pins versions .-> TEST

    ACT --> CORE
    WEBMVC --> CORE
    WEBFLUX --> CORE
    CORE --> SPRING
    CORE --> BOOT
    CORE -. -Pspring-boot-4.0 / 4.1 .-> COMPAT
    CORE -. test scope .-> TEST

    classDef bom fill:#efe,stroke:#5a5;
    class BOM bom;
```

Dashed edges are management or conditional relationships, never compile dependencies. `compatible` is a
jar on `core`'s classpath only under the Boot 4 profiles, and it is deliberately absent from the BOM.

---

## 7. Version Alignment Rules

| Rule | Detail |
|---|---|
| One version line per application | All five artifacts are managed at the same `${revision}`, and the project publishes them together. Mixing `0.2.x` core with an `0.1.x` webmvc is unsupported — the two lines target different Spring Boot generations. |
| BOM version vs Spring Boot version | The BOM pins Microsphere artifacts only. Your `spring-boot-starter-parent` (or your own `spring-boot-dependencies` import) picks the Boot line, and that must fall inside `3.0.13` – `4.1.1` for `0.2.x`. |
| Patch freedom | Within a minor line, the pinned patch in the compatibility table is what CI exercised; any patch of that line is expected to work. |
| No transitive Boot version | The BOM does not import `spring-boot-dependencies`. If you rely on the parent's chain instead, remember the parent resolves `spring-boot.version` from an active profile. |
| Snapshot builds | `-SNAPSHOT` revisions exist between releases (for example `0.2.35-SNAPSHOT` on the branch tip). They are published to the snapshot repository, not Central. |

---

## 8. Verified Behavior

The claims above come from reading the POM on the `release` branch, not from inference:

| Claim | Where to check |
|---|---|
| Exactly five managed artifacts | `microsphere-spring-boot-dependencies/pom.xml`, the `<dependencyManagement>` block |
| `compatible` is not managed | Same file — no `compatible` entry |
| `compatible` is not deployed | `microsphere-spring-boot-compatible/pom.xml`: `maven-deploy-plugin` `<skip>true</skip>` and `central-publishing-maven-plugin` `<skipPublishing>true</skipPublishing>` |
| Boot 4 profiles attach `compatible` to `core` | `microsphere-spring-boot-core/pom.xml`, `<profiles>` `spring-boot-4.0` and `spring-boot-4.1` |
| All managed entries use `${revision}` | Same file — every `<version>` is `${revision}` |

You can reproduce the effective pinning locally:

```bash
# resolve the managed version of each artifact, from the published BOM
mvn -q dependency:resolve -Dclassifier=
mvn -q help:effective-pom -Doutput=file=target/effective-pom.xml
grep -A3 'microsphere-spring-boot' target/effective-pom.xml | head -40
```

---

## 9. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `'dependencies.dependency.version' ... is missing` | The BOM import is absent, or placed after a conflicting import. | Move the Microsphere import above the competing one, or add the version explicitly. |
| `Could not find artifact io.github.microsphere-projects:microsphere-spring-boot-compatible` | Something depends on `compatible` directly. | Remove it; the module is intentionally unpublished and is only attached inside the reactor build. |
| Two Spring Boot versions on the classpath | Application imports `spring-boot-dependencies` at one version while the parent profile resolves another. | Keep the Boot version in exactly one place. |
| `0.1.x` and `0.2.x` artifacts both present | A transitive dependency pulled the other line. | Add an `<exclusion>`; the lines are not interchangeable. |
| BOM import silently ignored | `<type>pom</type>` or `<scope>import</scope>` missing. | Both are required. |

---

## 10. See Also

| Document | Scope |
|---|---|
| [parent.md](./parent.md) | The Parent POM that this BOM inherits. |
| [README.md](./README.md) | Installation and compatibility matrix for this branch. |
| [core.md](./core.md) | The module every other artifact depends on. |
| [compatible.md](./compatible.md) | Why the shim module stays out of the BOM. |
