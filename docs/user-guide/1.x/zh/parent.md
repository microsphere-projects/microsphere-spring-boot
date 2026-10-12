# Parent POM（父工程）

> [English](../parent.md) · 文档索引：[中文](../../zh/README.md)

> **分支：** `release-1.x` · **版本线：** `0.1.x` · **Artifact：** `microsphere-spring-boot-parent`
> **打包：** `pom` · **Spring Boot：** `2.1.18.RELEASE` – `2.7.18`（由 profile 选择） · **Java：** 8 基线

目录：
[1 模块概览](#1-模块概览) ·
[2 坐标与版本信息](#2-坐标与版本信息) ·
[3 与 0.2.x parent 的差异](#3-与-02x-parent-的差异) ·
[4 POM 继承链](#4-pom-继承链) ·
[5 受管属性](#5-受管属性) ·
[6 依赖管理](#6-依赖管理) ·
[7 Spring Boot 版本 profile](#7-spring-boot-版本-profile) ·
[8 如何使用 Parent](#8-如何使用-parent) ·
[9 Parent 不做的事](#9-parent-不做的事) ·
[10 故障排查](#10-故障排查) ·
[11 参考](#11-参考)

---

## 1. 模块概览

`release-1.x` 分支上的 `microsphere-spring-boot-parent` 与 `release` 分支上的职责完全一致：它是
`<packaging>pom</packaging>` 的版本层，固定 Spring Boot 版本线、导入第三方 BOM、并补齐没有任何 BOM
覆盖的少数版本。reactor 里每个模块都继承它，因此本项目中没有任何依赖需要写死版本号。

实际影响最大的差别只有一处 —— *范围*：这个 parent 只认识 Spring Boot `2.x`，而它所管辖的模块是围绕
`javax.*` Servlet API 编译的，不是 `jakarta.*`。

| 用法 | 适用场景 |
|---|---|
| 作为 `<parent>` | 你维护 Spring Boot 2.x 应用，或公司 reactor 必须与这些固定版本一致。 |
| 完全不继承，只用 BOM | 已经继承 `spring-boot-starter-parent`。见 [dependencies.md](../dependencies.md)。 |

---

## 2. 坐标与版本信息

```xml
<groupId>io.github.microsphere-projects</groupId>
<artifactId>microsphere-spring-boot-parent</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
```

| 事实 | `release-1.x` 取值 |
|---|---|
| 版本属性来源 | 根聚合 POM，`<revision>0.1.35-SNAPSHOT</revision>` |
| 最近发布版本 | `0.1.34` |
| Java 基线 | 该分支根 POM 未声明；CI 从 JDK `8` 一路验证到 `25` |
| 外部构建父工程 | `io.github.microsphere-projects:microsphere-build:0.3.14` |
| 声明的 Spring Boot 仓库 | 无（没有 `spring-milestone`） |
| 是否被 BOM 管理 | **否** |
| 是否发布到 Central | 是 |

---

## 3. 与 0.2.x parent 的差异

| 维度 | `release-1.x`（0.1.x） | `release`（0.2.x） |
|---|---|---|
| 构建父工程版本 | `microsphere-build:0.3.14` | `microsphere-build:0.3.16` |
| `microsphere-spring.version` | `0.1.39` | `0.2.39` |
| `jolokia.version` | `1.7.2` | `2.6.3` |
| `junit-jupiter.version`（bom） | `5.14.4` | `6.1.3` |
| Profile 数量 | 7 个（`spring-boot-2.1` … `2.7`） | 8 个（`spring-boot-3.0` … `4.1`） |
| 默认 profile | `spring-boot-2.7` → `2.7.18` | `spring-boot-3.5` → `3.5.16` |
| 版本后缀风格 | `2.1.18.RELEASE`、`2.2.13.RELEASE`、`2.3.12.RELEASE`（其后为纯数字） | 全部为纯数字 |
| 根 POM 的 `<java.version>` | 无 | `17` |
| `spring-milestone` 仓库 | 无 | 有 |
| 把 `compatible` 挂到 `core` 的 profile | `2.1`、`2.2`、`2.3`（老 Boot 缺少新 API） | `4.0`、`4.1`（新 Boot 移动了这些 API） |

`.RELEASE` 后缀只出现在 Boot `2.1`–`2.3`；从 `2.4` 开始 Spring Boot 去掉了它。由于后缀是 Maven 版本串
的一部分，对 Maven 来说 `2.3.12.RELEASE` 与 `2.3.12` 是**不同**的 artifact —— 不要把它“规范化”掉。

---

## 4. POM 继承链

```mermaid
graph TD
    MB["microsphere-build : 0.3.14<br/>外部构建父工程"]
    AGG["microsphere-spring-boot : ${revision}<br/>根聚合 POM<br/>revision=0.1.35-SNAPSHOT<br/>&lt;modules&gt; 列表"]
    PARENT["microsphere-spring-boot-parent<br/>spring-boot.version profiles<br/>dependencyManagement 导入"]
    BOM["microsphere-spring-boot-dependencies<br/>管理 5 个已发布 artifact"]
    CORE["microsphere-spring-boot-core"]
    WEBMVC["microsphere-spring-boot-webmvc"]
    WEBFLUX["microsphere-spring-boot-webflux"]
    ACT["microsphere-spring-boot-actuator"]
    TEST["microsphere-spring-boot-test"]
    COMPAT["microsphere-spring-boot-compatible<br/>不发布"]

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

聚合（根 POM 的 `<modules>`）决定一条命令编译哪些模块；继承（`<parent>`）决定一个 POM 得到什么 ——
属性、插件配置、`dependencyManagement`。两个分支的 8 个 `<module>` 条目完全相同，包括只构建、
不部署的 `compatible`。

---

## 5. 受管属性

来自 `microsphere-spring-boot-parent/pom.xml`：

| 属性 | 取值 | 作用 |
|---|---|---|
| `microsphere-spring.version` | `0.1.39` | `microsphere-spring-dependencies` 的版本，即同级 `microsphere-spring` 项目 `0.1.x` 线的 BOM。 |
| `jolokia.version` | `1.7.2` | `org.jolokia:jolokia-core`，Actuator 模块验证 JMX 桥接时使用的版本；`1.7.2` 是与 Boot 2 匹配的最后一个 `javax` 世代。 |
| `junit-jupiter.version` | `5.14.4` | `org.junit:junit-bom` 导入的版本。 |

来自根聚合 POM：

| 属性 | 取值 | 作用 |
|---|---|---|
| `revision` | `0.1.35-SNAPSHOT` | reactor 版本的唯一来源。 |

`spring-boot.version` 只在 profile 内定义，因此它的取值完全取决于当前激活了哪个 profile。

---

## 6. 依赖管理

结构与 `release` 分支一致：

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

| 条目 | 类型 | 本线的说明 |
|---|---|---|
| `junit-bom` | import | 排在最前，因此它的 JUnit 5 版本优先于 `spring-boot-dependencies` 可能给出的版本。 |
| `jolokia-core` | 直接固定 | `1.7.2`，对应 Boot 2 的 `javax` JMX 技术栈。 |
| `spring-boot-dependencies` | import | 同时提供 `javax.servlet:javax.servlet-api`，`test` 模块以 optional 方式依赖它。 |
| `microsphere-spring-dependencies` | import | `0.1.39` —— 与 `microsphere-spring` 的对应版本线配对。 |

由于 `spring-boot-dependencies` 是用 `${spring-boot.version}` 导入的，你最终拿到**哪一种** Servlet API
是 profile 选择的结果，而不是模块代码决定的。

---

## 7. Spring Boot 版本 profile

| Profile | `spring-boot.version` | 说明 |
|---|---|---|
| `spring-boot-2.1` | `2.1.18.RELEASE` | 同时把 `compatible` 挂到 `core`；`actuator` 固定 `micrometer-core`，`compatible` 固定 `spring-core:5.2.25.RELEASE`。 |
| `spring-boot-2.2` | `2.2.13.RELEASE` | 同样挂载 `compatible`。 |
| `spring-boot-2.3` | `2.3.12.RELEASE` | 同样挂载 `compatible`。 |
| `spring-boot-2.4` | `2.4.13` | |
| `spring-boot-2.5` | `2.5.15` | |
| `spring-boot-2.6` | `2.6.15` | |
| `spring-boot-2.7` | `2.7.18` | `<activeByDefault>true</activeByDefault>` |

显式激活任意 profile 都会让默认 profile 失效。不带 `-P` 时目标是 `2.7.18`。

```bash
# 默认版本线（2.7.18）
./mvnw clean verify

# 最老的支持线，此时 shim 模块参与编译
./mvnw clean verify -Pspring-boot-2.1

# 查看某模块实际解析到的版本
./mvnw -pl microsphere-spring-boot-core -am -Pspring-boot-2.3 help:effective-pom | grep spring-boot.version
```

`spring-boot-2.1` 下额外的那几个固定版本存在的原因：Boot 2.1 所管理的 `micrometer-core` 与 `spring-core`
比这些模块代码编译时所依赖的更老，因此 profile 只提升这几个 artifact，而不是整条 Boot 版本线。

---

## 8. 如何使用 Parent

### 8.1 继承它

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
        <!-- 由 BOM 而非 parent 管理版本，见下方说明 -->
    </dependency>
</dependencies>
```

说明：parent 固定的是 Spring Boot、`jolokia-core` 以及 Microsphere *Spring* 系列库；五个 Microsphere
*Spring Boot* artifact 由 [dependencies.md](../dependencies.md) 固定。因此还要导入 BOM，或者显式写版本。

### 8.2 选择 Boot 版本线

```bash
mvn -Pspring-boot-2.7 clean package   # 默认
mvn -Pspring-boot-2.4 clean package   # 2.4.13
```

### 8.3 JDK 匹配

本分支 CI 用 JDK `8`、`11`、`17`、`21`、`25` 对每个 profile 各跑一遍 reactor。模块代码按
`javax`/Java 8 源码级别编写，因此用更新的 JDK 构建是被验证过的；但这一线并不承诺更高的字节码目标。

---

## 9. Parent 不做的事

| 误解 | 事实 |
|---|---|
| “它管理 Microsphere Spring Boot 各 artifact 的版本。” | 那是 `microsphere-spring-boot-dependencies`。 |
| “它配置了 `spring-boot-maven-plugin`。” | 没有；那属于 `spring-boot-starter-parent`。 |
| “它设置了 Java 版本。” | 本分支根 POM 里没有 `<java.version>` 属性。 |
| “它声明了 milestone 仓库。” | 没有 —— 这一线完全基于 GA 发布构建。 |
| “它让 `compatible` 成为可发布的依赖。” | 两个分支上 `compatible` 都在部署与发布阶段被跳过。 |

---

## 10. 故障排查

| 现象 | 原因 | 处理 |
|---|---|---|
| `Could not find artifact org.springframework.boot:spring-boot-starter:jar:2.1.18` | Boot 的 artifact 名是 `2.1.18.RELEASE`，后缀属于版本串。 | `2.1`–`2.3` 保留 `.RELEASE` 后缀。 |
| `'dependencies.dependency.version' ... is missing` | profile 名拼错，导致 `spring-boot.version` 从未解析。 | 精确使用 §7 表中的 profile id，例如 `-Pspring-boot-2.5`。 |
| `package jakarta.servlet does not exist` | `0.2.x` 的 artifact 混进了 `0.1.x` 构建。 | 排除它；两条版本线不可互换。 |
| `cannot find symbol class EndpointId` | 你在 Boot `2.1`–`2.3` 下编译却没有激活对应 profile，于是 `compatible` 未挂载。 | 用 `-Pspring-boot-2.1`/`2.2`/`2.3` 构建。 |
| 测试 classpath 上出现两个 JUnit 版本 | 有传递导入排在 `junit-bom` 之前。 | 调整顺序，让 `junit-bom`（或你自己的固定版本）优先。 |
| 安装到本地仓库的 POM 里出现字面量 `${revision}` | 从未展开 flatten 的工作树安装。 | 使用已发布 artifact，或运行完整 reactor 构建。 |

---

## 11. 参考

| 文档 | 范围 |
|---|---|
| [dependencies.md](../dependencies.md) | 本线的 BOM。 |
| [core.md](../core.md) | 其他所有 artifact 依赖的模块。 |
| [compatible.md](../compatible.md) | 由 profile `2.1`–`2.3` 挂载的 12 个 shim 类。 |
| [actuator.md](../actuator.md) | `micrometer-core` 固定版本所在位置。 |
| [README.md](../README.md) | 本分支概览与安装。 |
