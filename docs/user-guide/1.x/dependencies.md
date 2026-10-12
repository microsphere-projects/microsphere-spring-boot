# Dependencies BOM

> [中文](./zh/dependencies.md) · [Documentation index](../README.md)

> **Branch:** `release-1.x` · **Version line:** `0.1.x` · **Artifact:** `microsphere-spring-boot-dependencies`
> **Packaging:** `pom` · **Manages:** 5 artifacts · **Spring Boot:** `2.1.18.RELEASE` – `2.7.18`

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

`microsphere-spring-boot-dependencies` on the `release-1.x` branch is byte-for-byte the same *shape* as
on `release`: a `<packaging>pom</packaging>` module whose only content is a `<dependencyManagement>`
block pinning the five published artifacts to `${revision}`. What differs is what those artifacts
contain — Boot 2-era APIs, `javax` Servlet types, and dual auto-configuration registration.

Use it when your application is on Spring Boot `2.1` – `2.7` and you want the modules without repeating
versions.

---

## 2. Coordinates and Version Information

```xml
<groupId>io.github.microsphere-projects</groupId>
<artifactId>microsphere-spring-boot-dependencies</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
```

| Fact | Value on `release-1.x` |
|---|---|
| Version property source | Root aggregator, `<revision>0.1.35-SNAPSHOT</revision>` |
| Last published release | `0.1.34` |
| Own parent | `microsphere-spring-boot-parent` (`../microsphere-spring-boot-parent/pom.xml`) |
| Artifacts managed | `core`, `webmvc`, `webflux`, `actuator`, `test` |
| Companion `microsphere-spring` line | `0.1.39` |

---

## 3. Managed Artifacts

| Artifact | Packaging | BOM entry | You normally depend on it |
|---|---|---|---|
| `microsphere-spring-boot-core` | jar | yes | Transitively, through any module below. |
| `microsphere-spring-boot-webmvc` | jar | yes | Boot 2 servlet-stack applications. |
| `microsphere-spring-boot-webflux` | jar | yes | Boot 2 reactive-stack applications. |
| `microsphere-spring-boot-actuator` | jar | yes | When you want the extra endpoints. |
| `microsphere-spring-boot-test` | jar | yes | `test` scope only. |
| `microsphere-spring-boot-compatible` | jar | **no** | Never — not deployed, not published. |
| `microsphere-spring-boot-parent` | pom | **no** | `<parent>` only. |

---

## 4. Why `compatible` and `parent` Are Excluded

**`compatible`** declares `maven-deploy-plugin` with `<skip>true</skip>` and
`central-publishing-maven-plugin` with `<skipPublishing>true</skipPublishing>`, so nothing reaches a
repository. On this branch it supplies twelve shim classes that Boot `2.1`–`2.3` do not have
(the bootstrap types, `EndpointId`, `@ConstructorBinding`, the endpoint availability conditions and
`IncludeExcludeEndpointFilter`), and it is attached to `core` by those same three profiles:

```xml
<profile>
    <id>spring-boot-2.1</id>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-compatible</artifactId>
            <version>${revision}</version>
        </dependency>
    </dependencies>
</profile>
```

A downstream dependency on it could never resolve, which is why the BOM omits it.

**`parent`** is excluded because a parent POM is not a dependency. Note also that on this branch the
parent carries an extra `spring-boot-2.1` profile of its own inside `compatible/pom.xml` that pins
`spring-core:5.2.25.RELEASE` — a build-time detail, invisible to consumers.

---

## 5. Import the BOM

### 5.1 Typical Boot 2 application

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>0.1.34</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-webmvc</artifactId>
    </dependency>
</dependencies>
```

### 5.2 Pair it with the Boot line you are on

| Your Spring Boot | Expect from `0.1.x` |
|---|---|
| `2.1.18.RELEASE` – `2.3.12.RELEASE` | Yes — and the shim types are needed, which is why the project builds `compatible` into `core` for these lines. |
| `2.4.13` – `2.6.15` | Yes, without the shim. |
| `2.7.18` | Yes; this is the default line the project publishes from. |
| `3.x`, `4.x` | No — use the `0.2.x` BOM instead. |

### 5.3 Override one artifact

Entries listed **before** the import win, so pinning a single module above the import is enough:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-actuator</artifactId>
            <version>0.1.30</version>
        </dependency>

        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>0.1.34</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Do this only for a temporary rollback; the artifacts are versioned and released together.

---

## 6. Dependency Graph

```mermaid
graph TD
    APP["Your application (Boot 2.x)"]
    BOM["microsphere-spring-boot-dependencies<br/>dependencyManagement only"]
    ACT["microsphere-spring-boot-actuator"]
    WEBMVC["microsphere-spring-boot-webmvc"]
    WEBFLUX["microsphere-spring-boot-webflux"]
    CORE["microsphere-spring-boot-core"]
    TEST["microsphere-spring-boot-test<br/>test scope"]
    COMPAT["microsphere-spring-boot-compatible<br/>not published"]
    SPRING["microsphere-spring-context 0.1.39"]
    BOOT["spring-boot-starter 2.x"]

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
    CORE -. -Pspring-boot-2.1 / 2.2 / 2.3 .-> COMPAT
    ACT -. -Pspring-boot-2.1 pins 1.3.20 .-> MICRO["micrometer-core"]
    CORE -. test scope .-> TEST

    classDef bom fill:#efe,stroke:#5a5;
    class BOM bom;
```

The `micrometer-core` edge records that the `spring-boot-2.1` profile inside
`microsphere-spring-boot-actuator/pom.xml` pins that artifact to `1.3.20`; it is not a dependency on
another Microsphere module.

---

## 7. Version Alignment Rules

| Rule | Detail |
|---|---|
| Never mix `0.1.x` with `0.2.x` | Different Boot generations, different Servlet packages (`javax` vs `jakarta`), different auto-configuration registration. |
| All five artifacts share `${revision}` | They are released together; a mixed set is a symptom of a stray transitive dependency. |
| The BOM does not pin Spring Boot | Your `spring-boot-starter-parent` (or your own Boot BOM import) chooses the Boot version, and it must be inside `2.1.18.RELEASE` – `2.7.18`. |
| `.RELEASE` suffix | Preserve it verbatim for `2.1`–`2.3`; Maven treats suffixed and unsuffixed versions as distinct artifacts. |
| JDK | Any JDK from 8 upward works; CI covers 8, 11, 17, 21, 25. |

---

## 8. Verified Behavior

| Claim | Where to check |
|---|---|
| Five managed artifacts, all at `${revision}` | `microsphere-spring-boot-dependencies/pom.xml` |
| No `compatible` entry | Same file |
| `compatible` skipped at deploy and publish | `microsphere-spring-boot-compatible/pom.xml` |
| Profiles `2.1`/`2.2`/`2.3` attach `compatible` to `core` | `microsphere-spring-boot-core/pom.xml`, `<profiles>` |
| `micrometer-core:1.3.20` pinned under `spring-boot-2.1` | `microsphere-spring-boot-actuator/pom.xml` |
| `spring-core:5.2.25.RELEASE` pinned under `spring-boot-2.1` | `microsphere-spring-boot-compatible/pom.xml` |

```bash
# confirm the managed versions your build sees
mvn -q help:effective-pom -Doutput=file=target/effective-pom.xml
grep -A3 'microsphere-spring-boot' target/effective-pom.xml | head -40
```

---

## 9. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Cannot resolve io.github.microsphere-projects:microsphere-spring-boot-compatible:jar:0.1.34` | Direct dependency on the unpublished shim. | Remove it; add `-Pspring-boot-2.1` when *building* the project itself. |
| `ClassNotFoundException: org.springframework.boot.actuate.endpoint.EndpointId` | Running Boot `2.1`–`2.3` against a build that omitted `compatible`. | Use published artifacts (they already shade the need away) or rebuild with the matching profile. |
| `package jakarta.servlet does not exist` at compile time | A `0.2.x` module on a `0.1.x` classpath. | Exclude it. |
| `NoSuchMethodError` on `@ConstructorBinding` | Boot `2.1`–`2.2` runtime without the shim. | Stay on a supported Boot patch line. |
| Version conflict warnings for `micrometer-core` | Your app pins a newer Micrometer than Boot 2.1's managed `1.3.20`. | Align with your Boot line; the project's own pin is build-time only. |

---

## 10. See Also

| Document | Scope |
|---|---|
| [parent.md](./parent.md) | The Parent POM behind this BOM. |
| [README.md](./README.md) | Branch overview and installation. |
| [core.md](./core.md) | The module every other artifact depends on. |
| [compatible.md](./compatible.md) | The twelve shim classes and their triggering profiles. |
