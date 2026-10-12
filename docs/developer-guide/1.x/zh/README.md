# 开发者文档 — `release-1.x` 分支（0.1.x）

> [English](../README.md) · [开发者文档首页：中文](../../zh/README.md) ·
> 本分支用户文档：[中文](../../../user-guide/1.x/zh/README.md)

> **分支：** `release-1.x` · **版本线：** `0.1.x` · **Artifact：** `microsphere-spring-boot-*`
> **Spring Boot：** `2.1.18.RELEASE` – `2.7.18`（由 profile 选择，默认 `2.7.18`） · **Java：** CI 覆盖 8 … 25

目录：
[1 本分支发布什么](#1-本分支发布什么) ·
[2 仓库形态](#2-仓库形态) ·
[3 模块依赖关系图](#3-模块依赖关系图) ·
[4 扩展点清单](#4-扩展点清单) ·
[5 自动配置的双重注册](#5-自动配置的双重注册) ·
[6 跨分支源码差异](#6-跨分支源码差异) ·
[7 垫片的方向是反的](#7-垫片的方向是反的) ·
[8 构建与 CI 差异](#8-构建与-ci-差异) ·
[9 模块文档](#9-模块文档) ·
[10 贡献检查清单](#10-贡献检查清单)

---

## 1. 本分支发布什么

`release-1.x` 维护面向 Spring Boot `2.1` 到 `2.7` 的 `0.1.x` 版本线。类清单与 `release` 分支完全一致——同样
47 个 core 类、10 个 actuator 类、3 + 3 个 web 类、4 个测试基类——但**实现**是 Java 8 / Boot 2 兼容的变体，
并且注册文件同时保留新旧两种形式。

| 模块 | Artifact | 是否发布 | 是否在 BOM | 主源码文件数 | 测试文件数 |
|---|---|---|---|---|---|
| `microsphere-spring-boot-parent` | pom | 是 | 否 | — | — |
| `microsphere-spring-boot-dependencies` | pom（BOM） | 是 | — | — | — |
| `microsphere-spring-boot-compatible` | jar | **否** | **否** | 12 | 0 |
| `microsphere-spring-boot-test` | jar | 是 | 是 | 4 | 5 |
| `microsphere-spring-boot-core` | jar | 是 | 是 | 47 | 48 |
| `microsphere-spring-boot-webmvc` | jar | 是 | 是 | 3 | 5 |
| `microsphere-spring-boot-webflux` | jar | 是 | 是 | 3 | 4 |
| `microsphere-spring-boot-actuator` | jar | 是 | 是 | 10 | 5 |

来自 `pom.xml` 的版本事实：`<revision>0.1.35-SNAPSHOT</revision>`，并且**没有 `<java.version>` 属性**——
Java 级别由外部 `microsphere-build:0.3.14` 父工程决定，而不是本仓库。

---

## 2. 仓库形态

模块布局与 `release` 完全相同，差异只在 POM 内容与资源文件。

```text
microsphere-spring-boot/                    revision 0.1.35-SNAPSHOT，无 java.version
├── pom.xml                                 同样 8 个 <modules>，顺序一致
├── microsphere-spring-boot-parent/         profile：spring-boot-2.1 … spring-boot-2.7
├── microsphere-spring-boot-dependencies/   同样受管的五个 artifact
├── microsphere-spring-boot-compatible/     12 个垫片，由 profile 2.1 / 2.2 / 2.3 挂载
├── microsphere-spring-boot-test/           同样四个基类
├── microsphere-spring-boot-core/           同样 47 个类，Boot 2 / Java 8 实现
├── microsphere-spring-boot-webmvc/         基于 javax.servlet 的条件
├── microsphere-spring-boot-webflux/
├── microsphere-spring-boot-actuator/       profile 2.1 把 micrometer 固定在 1.3.20
└── .github/workflows/                      maven-build、maven-publish、sync-branches-from-upstream
```

值得注意的缺失：本分支**没有** `merge-main-to-branches.yml`，其发布工作流合并回 `dev-1.x` 而不是 `main`。
因此 `0.1.x` 线是自包含的：没有任何东西自动流入 `0.2.x` 线，共享修复需要手工移植。

---

## 3. 模块依赖关系图

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

有两条边在 `release` 分支上不存在：

* `microsphere-spring-boot-compatible/pom.xml` 自带 `spring-boot-2.1` profile，把 `spring-core:5.2.25.RELEASE`
  （optional）固定住，因为垫片源码必须能针对最老的受支持 Spring Framework 编译，而不是 Boot 2.1 恰好带的那个版本。
* `microsphere-spring-boot-actuator/pom.xml` 带有 `spring-boot-2.1` profile，固定
  `io.micrometer:micrometer-core:1.3.20`。

---

## 4. 扩展点清单

`core/src/main/resources/META-INF/spring.factories` 注册的和 `release` 一样的十个类，只是多出一个键。原文如下：

| 键 | 注册的类 |
|---|---|
| `ApplicationContextInitializer` | `report.ConditionEvaluationReportInitializer`、`env.config.OriginTrackedConfigurationPropertyInitializer` |
| `SpringApplicationRunListener` | `classloading.BannedArtifactClassLoadingListener`、`listener.FailureReportSpringApplicationRunListener` |
| `ApplicationListener` | `diagnostics.ArtifactsCollisionDiagnosisListener`、`report.ConditionEvaluationReportListener`、`env.DefaultPropertiesApplicationListener` |
| `SpringBootExceptionReporter` | `report.ConditionEvaluationSpringBootExceptionReporter` |
| `FailureAnalyzer` | `diagnostics.ArtifactsCollisionFailureAnalyzer` |
| `AutoConfigurationImportFilter` | `autoconfigure.ConfigurableAutoConfigurationImportFilter` |
| `io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor` | `env.SpringApplicationDefaultPropertiesPostProcessor` |
| `EnableAutoConfiguration` | `context.autoconfigure.ConfigurationPropertiesAutoConfiguration` |

顺序常量与 `release` 一致：banned-artifact 监听器是 `HIGHEST_PRECEDENCE`，导入过滤器是
`HIGHEST_PRECEDENCE + 99`，默认属性监听器是 `LOWEST_PRECEDENCE - 1`。因此生命周期顺序与绑定可观测化路径与
[../current/zh/README.md](../../current/zh/README.md) 中记录的一致，只有实现不同（见第 6 节）。

---

## 5. 自动配置的双重注册

每个提供自动配置的模块都**同时**带着两套注册机制：

| 模块 | `META-INF/spring.factories` | `META-INF/spring/AutoConfiguration.imports` |
|---|---|---|
| `core` | `EnableAutoConfiguration=...ConfigurationPropertiesAutoConfiguration` | 同一个类 |
| `actuator` | `EnableAutoConfiguration=...ActuatorAutoConfiguration, ...ActuatorEndpointsAutoConfiguration` | 同样两个类 |
| `webmvc` | `EnableAutoConfiguration=...WebMvcAutoConfiguration` | 同一个类 |
| `webflux` | `EnableAutoConfiguration=...WebFluxAutoConfiguration` | 同一个类 |

为什么是两套：`AutoConfiguration.imports` 从 Spring Boot `2.7` 才引入，Boot `2.7` 两个文件都读，而
`2.1`–`2.6` 只读 `spring.factories`。一个机制一个文件，才能让同一个 artifact 覆盖整条版本线。维护者必须注意的
推论：

* 新增自动配置类要改**两个**文件；漏掉 `spring.factories` 会让 Boot `2.1`–`2.6` 静默失去支持。
* 在 Boot `2.7` 上同时列进两个文件是无害的（Boot 会去重），所以这种冗余被接受，而没有用 profile 专属资源去消除。
* `release` 分支上自动配置那个 `spring.factories` 键被彻底移除——那条线只面向 Boot 3，旧键会被忽略。

---

## 6. 跨分支源码差异

下表由两个分支的源码树逐文件 diff 得出，是主源码中跨线移植修复时必须处理的全部差异（测试源码的差异性质相同）。

| 文件 | `release` | `release-1.x` | 原因 |
|---|---|---|---|
| `core/.../SpringBootVersion.java` | 枚举常量 `SPRING_BOOT_3_0 … 4.x` | `SPRING_BOOT_2_0 … 2.7` | 每条线只枚举自己支持的版本 |
| `core/.../listener/SpringApplicationRunListenerAdapter.java` | `@Deprecated(since = "0.2.9", forRemoval = true)` | 未标记废弃；`starting(ConfigurableBootstrapContext)` 转调 `starting()` | 废弃是在 0.2.x 线上后来才宣布的；移植代码不得把该注解带进 0.1.x 发布 |
| `core/.../context/properties/bind/util/BindUtils.java` | `import static ...BindConstructorProvider.DEFAULT` 并直接调用 | 按名字加载该类（`ClassLoaderUtils.loadClass`）并反射调用；provider 不存在时 `getBindConstructor(...)` 返回 `null`，源码注释为 `// Spring Boot < 2.2.1` | `BindConstructorProvider` 在 Boot 2.2.1 之前不存在 |
| `core/.../context/properties/source/util/ConfigurationPropertyUtils.java` | 直接调用 `DataObjectPropertyName.toDashedForm(...)`，父级用 `name.getParent()` | 按名字解析 `DataObjectPropertyName`，并提供手写的 `toDashedForm(String, int)` 兜底；父级用 `name.chop(numberOfElements - 1)` 并以 `EMPTY` 兜边 | `DataObjectPropertyName` 是较晚加入的；`getParent()` 在这段 Boot 区间不可用 |
| `core/.../context/properties/bind/ListenableBindHandlerAdapter.java` | `super.onCreate(name, target, context, result)` | 通过 trusted lookup（`MethodHandleUtils.LookupMode.TRUSTED`）为 `AbstractBindHandler.onCreate` 解析 `MethodHandle`，拿不到（`NOT_FOUND_METHOD_HANDLE`）时回退为原始 result | 这段 Boot 区间里 `onCreate` 无法用 `super` 访问 |
| `core/.../context/properties/bind/ConfigurationPropertiesBeanContext.java` | `BeanDefinition beanDefinition = ...; beanDefinition.getResolvableType()` | 先转型为 `RootBeanDefinition`，再用 `BeanDefinitionUtils.getResolvableType(beanDefinition)` | 旧 `BeanDefinition` 实现上的类型解析 API 不同 |
| `core/.../autoconfigure/ConfigurableAutoConfigurationImportFilter.java` | 模式匹配：`property instanceof String exclude` | Java 8 写法：`instanceof String` 加显式转型 | 语言级别 |
| `core/.../diagnostics/ArtifactsCollisionDiagnosisListener.java` | `artifact instanceof MavenArtifact mavenArtifact` | Java 8 写法加显式转型 | 语言级别 |
| `core/.../util/SpringApplicationUtils.java` | 文本块拼日志模板、`switch` 表达式并带 `isTraceEnabled()` 守卫、输出 `additionalProfiles` | 字符串拼接、经典 `switch` + `break`、没有 `additionalProfiles` 参数 | Java 8 语言级别，且上报字段少一个 |
| `actuator/.../endpoint/ConfigurationMetadataEndpoint.java` | `ConfigurationMetadataDescriptor implements OperationResponseBody` | 普通 DTO，不实现接口 | `OperationResponseBody` 晚于本线最老的目标版本 |
| `actuator/.../endpoint/ConfigurationPropertiesEndpoint.java` | `ConfigurationPropertiesDescriptor implements OperationResponseBody` | 普通 DTO，不实现接口 | 同上 |
| `webmvc/.../WebMvcAutoConfiguration.java` | `@AutoConfigureAfter(name = {...autoconfigure.web.servlet.WebMvcAutoConfiguration, ...webmvc.autoconfigure.WebMvcAutoConfiguration})` | 只有 `org.springframework.boot.autoconfigure.web.servlet` 那一个名字 | 第二个名字属 Boot `4.0+`，在本线不可能是目标 |
| `webmvc/.../condition/ConditionalOnWebMvcAvailable.java` | `@ConditionalOnClass(name = {"jakarta.servlet.Servlet", ...})` | `"javax.servlet.Servlet"` | Servlet 命名空间在 Boot 3.0 处分裂 |
| `webflux/.../WebFluxAutoConfiguration.java` | 两个 `@AutoConfigureAfter` 名字（Boot `2.0–4.0` 与 Boot `4.0+`） | 只有 `org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration` | 同 webmvc |
| `compatible/.../package-info.java` | “forked from the Spring Boot 3.5.x” | “forked from the Spring Boot 2.7.x(latest)” | 每条线都从自己所支持的最新 Boot 里 fork |
| 各 POM（root、parent、core、actuator、test、compatible） | `microsphere-build:0.3.16`、`microsphere-spring:0.2.39`、`jolokia:2.6.3`、`junit-jupiter:6.1.3`、Boot 3/4 profile、`spring-milestone` 仓库、core 有 `jakarta.annotation-api` 测试依赖 | `microsphere-build:0.3.14`、`microsphere-spring:0.1.39`、`jolokia:1.7.2`、`junit-jupiter:5.14.4`、Boot 2 profile 且带 `.RELEASE` 后缀、无 repositories 块、无 `jakarta.annotation-api` | 版本线管理 |

反复出现的规律是：**本线对「可能不存在的 API」采用按名字或反射访问；`release` 对「换过位置的 API」采用在条件里
按名字引用。** 两者都是在避免硬依赖某个在另一代 Boot 里并不位于该坐标的类。

---

## 7. 垫片的方向是反的

在 `release` 上，`compatible` 补的是 Spring Boot **在 Boot 4 中移除或迁移**的类，所以由 profile
`4.0`/`4.1` 挂载。在 `release-1.x` 上，它补的是早期 Boot 2 **尚未提供**的类，所以由 profile
`2.1`/`2.2`/`2.3` 挂载（`microsphere-spring-boot-core/pom.xml` 第 91–127 行）。

| 本线的垫片 | 引入它的 Boot 版本 | 需要它的 profile |
|---|---|---|
| `BootstrapContext`、`BootstrapRegistry`、`ConfigurableBootstrapContext`、`DefaultBootstrapContext`、`BootstrapContextClosedEvent` | 2.4.0 | 2.1、2.2、2.3 |
| `actuate.endpoint.EndpointId` | 2.0.6（`of(Environment, String)` 从 2.2.0 起） | 2.1、2.2、2.3 |
| `actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint` 及 `AbstractEndpointCondition` / `OnAvailableEndpointCondition` | 2.2.0 | 2.1、2.2、2.3 |
| `actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter` | 2.2.7 | 2.1、2.2、2.3 |
| `context.properties.ConstructorBinding` | 2.2.0 | 2.1、2.2、2.3 |

决定性约束是 bootstrap API（`@since 2.4.0`）：`core` 的 `SpringApplicationRunListenerAdapter` 与
`LoggingSpringApplicationRunListener` 的方法签名里带有 `ConfigurableBootstrapContext`，而 Boot 2.1–2.3 并不提供。
本线 fork 的源码来自 Boot `2.7.x`，即本线支持的最新版本。

---

## 8. 构建与 CI 差异

| 方面 | `release` | `release-1.x` |
|---|---|---|
| CI JDK 矩阵 | `17`、`21`、`25` | `8`、`11`、`17`、`21`、`25` |
| CI Boot profile | 8 个（`3.0` … `4.1`） | 7 个（`2.1` … `2.7`） |
| 矩阵单元数 | 24 | 35 |
| 构建触发 | push `main`、`dev`；PR `main`、`dev`、`release` | push `dev-1.x`；PR `dev-1.x`、`release-1.x` |
| Action 版本 | `checkout@v7.0.1`、`setup-java@v6.0.1`、`codecov-action@v7.1.1` | `checkout@v5`、`setup-java@v5`、`codecov-action@v5` |
| 发布用 JDK | `17` | `11` |
| 发布分支 | push `release` | push `release-1.x` |
| 发布后合并 | `release` → `main` | `release-1.x` → `dev-1.x` |
| `merge-main-to-branches.yml` | 存在 | **不存在** |
| Boot 版本字面量 | 纯号（`3.5.16`、`4.1.1`） | `2.1.18.RELEASE`、`2.2.13.RELEASE`、`2.3.12.RELEASE` 带 `.RELEASE`；从 `2.4.13` 起为纯号 |
| 覆盖率上传 slug | `microsphere-projects/microsphere-spring-boot` | 两条线用的是同一个 slug |

构建命令本身完全一致：
`mvn --batch-mode --update-snapshots --file pom.xml -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles test,coverage,<profile>`。

对本地工作的影响：要复现矩阵低端需要 JDK 8 与 11；本线不存在 Boot 4 的 profile，选它会报「无法激活的 profile」
告警并落回默认的 `2.7.18`。

---

## 9. 模块文档

| 文档 | 范围 |
|---|---|
| [core.md](./core.md) | Boot 2 实现：反射式能力探测、Java 8 写法、`SpringBootVersion` 覆盖范围 |
| [actuator.md](./actuator.md) | Boot 2 上的端点模型、由垫片提供的端点条件、micrometer 固定版本 |
| [webmvc.md](./webmvc.md) | `javax.servlet` 可用性条件与单名字排序 |
| [webflux.md](./webflux.md) | Boot 2 上的响应式栈自动配置 |
| [test.md](./test.md) | 0.1.x 线的基类及其 JUnit 5 边界 |
| [compatible.md](./compatible.md) | 十二个垫片以及挂载它们的 profile |
| [parent.md](./parent.md) | Boot 2 profile 表、`.RELEASE` 后缀、没有 milestone 仓库 |
| [dependencies.md](./dependencies.md) | 0.1.x artifact 的 BOM 与 Boot 线配对 |
| [build-and-release.md](./build-and-release.md) | 35 单元 CI 矩阵、从 `release-1.x` 发布、合并回 `dev-1.x` |

`release` 的同名文档描述的是同一套架构；建议先读那一份，本目录只用来看差异。

---

## 10. 贡献检查清单

在 [../current/zh/README.md](../../current/zh/README.md) 中与分支无关的检查清单之上，本线追加：

1. **保持 Java 8 兼容。** 不用模式匹配 `instanceof`、不用文本块、不用 `switch` 表达式、不用 `var`。用 JDK 8 或
   11 跑 `./mvnw -Pspring-boot-2.1 test` 验证。
2. **保持 Boot 2.1 兼容。** 任何晚于本线下限的 Boot API，都要按名字或反射访问，并提供 null / 静默兜底——参照
   `BindUtils` 与 `ConfigurationPropertyUtils` 的写法，而不是直接加 import。
3. **自动配置注册两次**：`spring.factories` 的 `EnableAutoConfiguration` **和** `AutoConfiguration.imports`。
4. **不要移植 `@Deprecated` 标记**（尤其是 `SpringApplicationRunListenerAdapter`），除非本线也宣布过同样的废弃。
5. **垫片改动只进 `compatible`**，挂载它的 profile 是 `2.1`/`2.2`/`2.3`——绝不能是 Boot 3 或 Boot 4 的 profile，
   本线没有那些 profile。
6. **文档**：`docs/developer-guide/1.x/<module>.md` 与 `docs/developer-guide/1.x/zh/<module>.md`；若改动对用户
   可见，对应的用户文档一对也要改。
7. **发版**通过发布工作流从 `release-1.x` 切出；产出的版本属于 `0.1.x` 区间，并合并进 `dev-1.x` 而非 `main`。
