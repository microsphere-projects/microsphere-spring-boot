# Dependencies BOM（依赖清单）

> [English](../dependencies.md) · 文档索引：[中文](../../zh/README.md)

> **分支：** `release` · **版本线：** `0.2.x` · **Artifact：** `microsphere-spring-boot-dependencies`
> **打包：** `pom` · **管理：** 5 个 artifact · **Spring Boot：** `3.0.13` – `4.1.1`

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

`microsphere-spring-boot-dependencies` 是面向使用者的 BOM。它只有一个 `<dependencyManagement>` 块，
把本项目已发布的五个库 artifact 固定到 `${revision}`，这样应用声明依赖时不必重复写版本：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webmvc</artifactId>
    <!-- 版本由 BOM 提供 -->
</dependency>
```

这是引入这些库的推荐方式：与 [Parent POM](../parent.md) 不同，它不会占用你的继承链 ——
你的 parent 仍然可以是 `spring-boot-starter-parent`。

---

## 2. 坐标与版本信息

```xml
<groupId>io.github.microsphere-projects</groupId>
<artifactId>microsphere-spring-boot-dependencies</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
```

| 事实 | `release` 分支取值 |
|---|---|
| 版本属性来源 | 根聚合 POM，`<revision>0.2.35-SNAPSHOT</revision>` |
| 最近发布版本 | `0.2.34` |
| 自身的 parent | `microsphere-spring-boot-parent`（`../microsphere-spring-boot-parent/pom.xml`） |
| 受管 artifact | `core`、`webmvc`、`webflux`、`actuator`、`test` |
| 受管 artifact 的 Java 基线 | 17 |

---

## 3. 受管 artifact

| Artifact | 打包 | 是否在 BOM | 通常何时依赖它 |
|---|---|---|---|
| `microsphere-spring-boot-core` | jar | 是 | 通过下列任一模块传递引入。 |
| `microsphere-spring-boot-webmvc` | jar | 是 | Servlet 栈应用。 |
| `microsphere-spring-boot-webflux` | jar | 是 | 响应式栈应用。 |
| `microsphere-spring-boot-actuator` | jar | 是 | 需要额外端点时。 |
| `microsphere-spring-boot-test` | jar | 是 | 仅 `test` 作用域。 |
| `microsphere-spring-boot-compatible` | jar | **否** | 永远不要 —— 构建期 shim，不发布。 |
| `microsphere-spring-boot-parent` | pom | **否** | 作为 `<parent>` 使用，不是依赖。 |
| `microsphere-spring-boot`（根聚合） | pom | **否** | 仅用于 reactor。 |

---

## 4. 为什么排除 `compatible` 与 `parent`

**`compatible`** 带有 `maven-deploy-plugin` 的 `<skip>true</skip>` 和
`central-publishing-maven-plugin` 的 `<skipPublishing>true</skipPublishing>`，因此任何构件都不会进入仓库。
把它写进 BOM 会造出一个无法解析的受管坐标，下游构建一旦依赖它就会在解析阶段失败。排除它，BOM 才能
兑现一个承诺：*列在这里的都拉得到。*

该模块只在构建期挂载，由 `microsphere-spring-boot-core/pom.xml` 的 `spring-boot-4.0` 与
`spring-boot-4.1` profile 完成：

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
    <!-- spring-boot-4.1 声明同样的依赖 -->
</profiles>
```

**`parent`** 被排除，是因为 parent POM 与受管依赖是两套机制。极少数情况下两者都想用时，仍要导入本
BOM —— parent 自身的 `dependencyManagement` 并不包含 Microsphere artifact。

---

## 5. 导入 BOM

### 5.1 独立应用（不使用 Microsphere parent）

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

### 5.2 在 microsphere-spring-boot-parent 之下构建

Spring Boot 本身无需再导入（parent 已经导入 `spring-boot-dependencies`），但 Microsphere 坐标仍要导入
本 BOM：

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

### 5.3 公司级多模块项目

在公司根 POM 里导入一次，子模块声明依赖时即可省略版本。子模块 `dependencyManagement` 的顺序决定冲突，
因此需要覆盖某个模块版本时，把你自己的固定版本写在 Microsphere 导入**之前**。

---

## 6. 依赖关系图

```mermaid
graph TD
    APP["你的应用"]
    BOM["microsphere-spring-boot-dependencies<br/>仅 dependencyManagement"]
    ACT["microsphere-spring-boot-actuator"]
    WEBMVC["microsphere-spring-boot-webmvc"]
    WEBFLUX["microsphere-spring-boot-webflux"]
    CORE["microsphere-spring-boot-core"]
    TEST["microsphere-spring-boot-test<br/>test scope"]
    COMPAT["microsphere-spring-boot-compatible<br/>不发布，由 profile 挂载"]
    SPRING["microsphere-spring-context<br/>来自 microsphere-spring-dependencies"]
    BOOT["spring-boot-starter<br/>来自 spring-boot-dependencies"]

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
    CORE -. -Pspring-boot-4.0 / 4.1 .-> COMPAT
    CORE -. test scope .-> TEST

    classDef bom fill:#efe,stroke:#5a5;
    class BOM bom;
```

虚线表示“管理”或“条件挂载”，都不是编译依赖。`compatible` 只在 Boot 4 profile 下出现在 `core` 的
classpath 上，并且刻意不进入 BOM。

---

## 7. 版本对齐规则

| 规则 | 说明 |
|---|---|
| 一个应用只用一条版本线 | 五个 artifact 都固定在同一个 `${revision}`，项目也是整体发布。把 `0.2.x` 的 core 与 `0.1.x` 的 webmvc 混用是不支持的 —— 两条线面向不同的 Spring Boot 世代。 |
| BOM 版本 vs Spring Boot 版本 | BOM 只固定 Microsphere artifact。Boot 版本由你的 `spring-boot-starter-parent`（或你自己的 `spring-boot-dependencies` 导入）决定，且对 `0.2.x` 必须落在 `3.0.13` – `4.1.1` 之内。 |
| patch 自由度 | 兼容性表里固定的 patch 是 CI 实际验证过的版本；同 minor 线内其他 patch 预期可用。 |
| BOM 不传递 Boot 版本 | 本 BOM 不导入 `spring-boot-dependencies`。若依赖 parent 的继承链，要记住 `spring-boot.version` 由激活的 profile 决定。 |
| 快照构建 | 两次发布之间存在 `-SNAPSHOT` 版本（分支尖端是 `0.2.35-SNAPSHOT`），发布到 snapshot 仓库而非 Central。 |

---

## 8. 事实核对

以上结论来自直接阅读 `release` 分支的 POM，而非推测：

| 结论 | 核对位置 |
|---|---|
| 恰好五个受管 artifact | `microsphere-spring-boot-dependencies/pom.xml` 的 `<dependencyManagement>` 块 |
| `compatible` 不受管 | 同一文件 —— 没有 `compatible` 条目 |
| `compatible` 不部署 | `microsphere-spring-boot-compatible/pom.xml`：`maven-deploy-plugin` `<skip>true</skip>` 与 `central-publishing-maven-plugin` `<skipPublishing>true</skipPublishing>` |
| Boot 4 profile 把 `compatible` 挂到 `core` | `microsphere-spring-boot-core/pom.xml`，`<profiles>` 中的 `spring-boot-4.0` 与 `spring-boot-4.1` |
| 所有受管条目都用 `${revision}` | 同一文件 —— 每个 `<version>` 都是 `${revision}` |

可以在本地复现这些固定关系：

```bash
# 从已发布 BOM 解析每个 artifact 的受管版本
mvn -q dependency:resolve -Dclassifier=
mvn -q help:effective-pom -Doutput=file=target/effective-pom.xml
grep -A3 'microsphere-spring-boot' target/effective-pom.xml | head -40
```

---

## 9. 故障排查

| 现象 | 原因 | 处理 |
|---|---|---|
| `'dependencies.dependency.version' ... is missing` | 没有导入 BOM，或导入位置在冲突项之后。 | 把 Microsphere 导入提到冲突项之前，或显式写版本。 |
| `Could not find artifact io.github.microsphere-projects:microsphere-spring-boot-compatible` | 有依赖直接引用了 `compatible`。 | 删除它；该模块刻意不发布，只在 reactor 构建内挂载。 |
| classpath 上出现两个 Spring Boot 版本 | 应用导入某一版本的 `spring-boot-dependencies`，而 parent profile 解析出另一个。 | Boot 版本只保留一个来源。 |
| 同时出现 `0.1.x` 与 `0.2.x` artifact | 某个传递依赖引入了另一条版本线。 | 加 `<exclusion>`；两条线不可互换。 |
| BOM 导入被静默忽略 | 缺少 `<type>pom</type>` 或 `<scope>import</scope>`。 | 两者都必需。 |

---

## 10. 参考

| 文档 | 范围 |
|---|---|
| [parent.md](../parent.md) | 本 BOM 所继承的 Parent POM。 |
| [README.md](../README.md) | 本分支的安装与兼容性矩阵。 |
| [core.md](../core.md) | 其他所有 artifact 依赖的模块。 |
| [compatible.md](../compatible.md) | shim 模块为何不进 BOM。 |
