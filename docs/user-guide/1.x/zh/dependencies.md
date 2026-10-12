# Dependencies BOM（依赖清单）

> [English](../dependencies.md) · 文档索引：[中文](../../zh/README.md)

> **分支：** `release-1.x` · **版本线：** `0.1.x` · **Artifact：** `microsphere-spring-boot-dependencies`
> **打包：** `pom` · **管理：** 5 个 artifact · **Spring Boot：** `2.1.18.RELEASE` – `2.7.18`

目录：
[1 模块概览](#1-模块概览) ·
[2 坐标与版本信息](#2-坐标与版本信息) ·
[3 受管 artifact](#3-受管-artifact) ·
[4 为什么排除 compatible 与 parent](#4-为什么排除-compatible-与-parent) ·
[5 导入 BOM](#5-导入-bom) ·
[6 依赖关系图](#6-依赖关系图) ·
[7 版本对齐规则](#7-版本对齐规则) ·
[8 事实核对](#8-事实核对) ·
[9 故障排查](#9-故障排查) ·
[10 参考](#10-参考)

---

## 1. 模块概览

`release-1.x` 分支上的 `microsphere-spring-boot-dependencies` 与 `release` 分支上的**结构完全一致**：
一个 `<packaging>pom</packaging>` 模块，内容只有一个 `<dependencyManagement>` 块，把五个已发布 artifact
固定到 `${revision}`。不同的是这些 artifact 里装了什么 —— Boot 2 时代的 API、`javax` Servlet 类型，
以及双份自动配置注册。

当你的应用处于 Spring Boot `2.1` – `2.7`，又不想重复写版本时，就用它。

---

## 2. 坐标与版本信息

```xml
<groupId>io.github.microsphere-projects</groupId>
<artifactId>microsphere-spring-boot-dependencies</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
```

| 事实 | `release-1.x` 取值 |
|---|---|
| 版本属性来源 | 根聚合 POM，`<revision>0.1.35-SNAPSHOT</revision>` |
| 最近发布版本 | `0.1.34` |
| 自身的 parent | `microsphere-spring-boot-parent`（`../microsphere-spring-boot-parent/pom.xml`） |
| 受管 artifact | `core`、`webmvc`、`webflux`、`actuator`、`test` |
| 配套的 `microsphere-spring` 版本线 | `0.1.39` |

---

## 3. 受管 artifact

| Artifact | 打包 | 是否在 BOM | 通常何时依赖它 |
|---|---|---|---|
| `microsphere-spring-boot-core` | jar | 是 | 通过下列任一模块传递引入。 |
| `microsphere-spring-boot-webmvc` | jar | 是 | Boot 2 Servlet 栈应用。 |
| `microsphere-spring-boot-webflux` | jar | 是 | Boot 2 响应式栈应用。 |
| `microsphere-spring-boot-actuator` | jar | 是 | 需要额外端点时。 |
| `microsphere-spring-boot-test` | jar | 是 | 仅 `test` 作用域。 |
| `microsphere-spring-boot-compatible` | jar | **否** | 永远不要 —— 不部署、不发布。 |
| `microsphere-spring-boot-parent` | pom | **否** | 仅作为 `<parent>`。 |

---

## 4. 为什么排除 `compatible` 与 `parent`

**`compatible`** 声明了 `maven-deploy-plugin` 的 `<skip>true</skip>` 与
`central-publishing-maven-plugin` 的 `<skipPublishing>true</skipPublishing>`，因此没有任何构件进入仓库。
在这一条线上，它提供 12 个 Boot `2.1`–`2.3` 尚不存在的 shim 类（bootstrap 系列类型、`EndpointId`、
`@ConstructorBinding`、端点可用性条件以及 `IncludeExcludeEndpointFilter`），而挂载它的正是这三个 profile：

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

下游对它的依赖永远无法解析，所以 BOM 刻意不包含它。

**`parent`** 被排除，是因为 parent POM 不是依赖。另外注意：本分支的 `compatible/pom.xml` 里还有一个
属于它自己的 `spring-boot-2.1` profile，把 `spring-core` 固定为 `5.2.25.RELEASE` —— 这是构建期细节，
使用者看不到。

---

## 5. 导入 BOM

### 5.1 典型的 Boot 2 应用

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

### 5.2 与你所在的 Boot 版本线配对

| 你的 Spring Boot | `0.1.x` 是否适用 |
|---|---|
| `2.1.18.RELEASE` – `2.3.12.RELEASE` | 适用 —— 且需要 shim 类型，这正是项目在这几条线上把 `compatible` 编进 `core` 的原因。 |
| `2.4.13` – `2.6.15` | 适用，且不需要 shim。 |
| `2.7.18` | 适用；这是项目默认发布的版本线。 |
| `3.x`、`4.x` | 不适用 —— 请改用 `0.2.x` 的 BOM。 |

### 5.3 只覆盖某个 artifact

排在导入**之前**的条目优先，因此把单个模块写在 BOM 导入之前即可覆盖：

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

只把它当作临时回退手段；这些 artifact 是整体设计、整体发布的。

---

## 6. 依赖关系图

```mermaid
graph TD
    APP["你的应用（Boot 2.x）"]
    BOM["microsphere-spring-boot-dependencies<br/>仅 dependencyManagement"]
    ACT["microsphere-spring-boot-actuator"]
    WEBMVC["microsphere-spring-boot-webmvc"]
    WEBFLUX["microsphere-spring-boot-webflux"]
    CORE["microsphere-spring-boot-core"]
    TEST["microsphere-spring-boot-test<br/>test scope"]
    COMPAT["microsphere-spring-boot-compatible<br/>不发布"]
    SPRING["microsphere-spring-context 0.1.39"]
    BOOT["spring-boot-starter 2.x"]

    APP -. import .-> BOM
    BOM -. 固定版本 .-> ACT
    BOM -. 固定版本 .-> WEBMVC
    BOM -. 固定版本 .-> WEBFLUX
    BOM -. 固定版本 .-> CORE
    BOM -. 固定版本 .-> TEST

    ACT --> CORE
    WEBMVC --> CORE
    WEBFLUX --> CORE
    CORE --> SPRING
    CORE --> BOOT
    CORE -. -Pspring-boot-2.1 / 2.2 / 2.3 .-> COMPAT
    ACT -. -Pspring-boot-2.1 固定 1.3.20 .-> MICRO["micrometer-core"]
    CORE -. test scope .-> TEST

    classDef bom fill:#efe,stroke:#5a5;
    class BOM bom;
```

指向 `micrometer-core` 的那条边表示：`microsphere-spring-boot-actuator/pom.xml` 内的
`spring-boot-2.1` profile 把该 artifact 固定为 `1.3.20`，它并不是对另一个 Microsphere 模块的依赖。

---

## 7. 版本对齐规则

| 规则 | 说明 |
|---|---|
| 绝不混用 `0.1.x` 与 `0.2.x` | 不同的 Boot 世代、不同的 Servlet 包名（`javax` vs `jakarta`）、不同的自动配置注册方式。 |
| 五个 artifact 共用 `${revision}` | 它们一同发布；出现混版通常是某个传递依赖带进来的。 |
| BOM 不固定 Spring Boot | Boot 版本由你的 `spring-boot-starter-parent`（或你自己导入的 Boot BOM）决定，且必须落在 `2.1.18.RELEASE` – `2.7.18`。 |
| `.RELEASE` 后缀 | `2.1`–`2.3` 必须原样保留；Maven 把带后缀与不带后缀视为不同 artifact。 |
| JDK | JDK 8 及以上均可；CI 覆盖 8、11、17、21、25。 |

---

## 8. 事实核对

| 结论 | 核对位置 |
|---|---|
| 五个受管 artifact，全部为 `${revision}` | `microsphere-spring-boot-dependencies/pom.xml` |
| 没有 `compatible` 条目 | 同一文件 |
| `compatible` 在部署与发布阶段被跳过 | `microsphere-spring-boot-compatible/pom.xml` |
| profile `2.1`/`2.2`/`2.3` 把 `compatible` 挂到 `core` | `microsphere-spring-boot-core/pom.xml`，`<profiles>` |
| `micrometer-core:1.3.20` 固定在 `spring-boot-2.1` | `microsphere-spring-boot-actuator/pom.xml` |
| `spring-core:5.2.25.RELEASE` 固定在 `spring-boot-2.1` | `microsphere-spring-boot-compatible/pom.xml` |

```bash
# 确认你的构建实际看到的受管版本
mvn -q help:effective-pom -Doutput=file=target/effective-pom.xml
grep -A3 'microsphere-spring-boot' target/effective-pom.xml | head -40
```

---

## 9. 故障排查

| 现象 | 原因 | 处理 |
|---|---|---|
| `Cannot resolve io.github.microsphere-projects:microsphere-spring-boot-compatible:jar:0.1.34` | 直接依赖了未发布的 shim。 | 删除它；只有在**构建本项目**时才需要 `-Pspring-boot-2.1`。 |
| `ClassNotFoundException: org.springframework.boot.actuate.endpoint.EndpointId` | 在 Boot `2.1`–`2.3` 上运行、但构建时未包含 `compatible`。 | 使用已发布 artifact，或用匹配的 profile 重新构建。 |
| 编译期 `package jakarta.servlet does not exist` | `0.2.x` 的模块出现在 `0.1.x` 的 classpath 上。 | 排除它。 |
| `@ConstructorBinding` 抛 `NoSuchMethodError` | Boot `2.1`–`2.2` 运行时缺少 shim。 | 停留在受支持的 Boot patch 线上。 |
| `micrometer-core` 版本冲突告警 | 你的应用固定的 Micrometer 比 Boot 2.1 管理的 `1.3.20` 更新。 | 与你的 Boot 版本线对齐；项目自身的固定只作用于构建期。 |

---

## 10. 参考

| 文档 | 范围 |
|---|---|
| [parent.md](../parent.md) | 本 BOM 背后的 Parent POM。 |
| [README.md](../README.md) | 本分支概览与安装。 |
| [core.md](../core.md) | 其他所有 artifact 依赖的模块。 |
| [compatible.md](../compatible.md) | 12 个 shim 类及其触发 profile。 |
