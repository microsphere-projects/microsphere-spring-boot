# 源码对比 — `src/main`：tag `0.2.34` vs tag `0.1.34`

对比范围：最新的两个 tag —— `0.2.34`（Maven `revision` = `0.2.34-SNAPSHOT`，Java 17 / Spring Boot 3.5.x–4.x 线）与 `0.1.34`（Maven `revision` = `0.1.34-SNAPSHOT`，Java 8 / Spring Boot 2.x 线）。差异方向为 `0.1.34 → 0.2.34`（“新”指 `0.2.34`）。本文仅覆盖 `src/main`；`src/test` 单独对比见 [test-diff](./test-diff-0.2.34-vs-0.1.34.md)。

- 两个分支上 8 个 Maven 模块全部同名匹配；`dependencies` 与 `parent` 不含 `src/main` 源码（N/A）。
- 排除的差异类别：**Java 17 与 Java 8 语法/API 差异**（`instanceof` 模式匹配、文本块、箭头 `switch`、`@Deprecated(forRemoval)`、`String.strip()`）与 **Java EE 与 Jakarta EE 差异**（`javax.* → jakarta.*`）。

## 模块总览

| Maven 模块 | main 文件数 `0.1.34` | main 文件数 `0.2.34` | 变更条目 | 排除后的实质差异 |
|---|---|---|---|---|
| actuator | 14 | 13 | 2 个 java（M）、1 个资源（D） | 3 |
| compatible | 12 | 10 | 6 个 java（M）、6 个 java（D）、4 个 java（A） | Fork 清单从 Boot 2.7 换基到 Boot 3.5 |
| core | 50 | 50 | 11 个 java（M）、1 个资源（M） | 10 |
| dependencies | 0 | 0 | 0 | N/A |
| parent | 0 | 0 | 0 | N/A |
| test | 4 | 4 | 0 | 0 |
| webflux | 5 | 4 | 1 个 java（M）、1 个资源（D） | 2 |
| webmvc | 5 | 4 | 2 个 java（M）、1 个资源（D） | 2（1 java + 1 资源；另一 java 变更属排除项） |

## microsphere-spring-boot-actuator（src/main）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `ConfigurationMetadataEndpoint` | `ConfigurationMetadataDescriptor` 为普通 POJO | Descriptor `implements OperationResponseBody` | **API 兼容**：Spring Boot 3 actuator 序列化要求返回值实现 `OperationResponseBody` 标记接口 |
| `ConfigurationPropertiesEndpoint` | `ConfigurationPropertiesDescriptor` 为普通 POJO | Descriptor `implements OperationResponseBody` | **API 兼容**：同上 |
| `META-INF/spring.factories` | 通过 `EnableAutoConfiguration=` 注册两个自动配置 | 删除（仅保留 `AutoConfiguration.imports` 注册） | **API 兼容**：Boot 3 移除了 `spring.factories` 的自动配置注册；`.imports` 文件在两个 tag 上均已存在，`0.2.34` 只是删掉遗留的重复注册 |

## microsphere-spring-boot-compatible（src/main）

fork 基线从 **Spring Boot 2.7.x** 改为 **Spring Boot 3.5.x**（`package-info.java` 中说明）。原因：该模块为各版本线**所支持的最低 Boot 版本**补齐缺失的类 —— 0.1.x 线补 Boot 2.1–2.7，0.2.x 线补 Boot 3.0。

### 有改动但无实质区别

| 文件 | 差异 |
|---|---|
| `BootstrapContext`、`BootstrapContextClosedEvent`、`BootstrapRegistry`、`ConfigurableBootstrapContext`、`DefaultBootstrapContext` | 仅 `0.1.34` 缺少文件末尾换行（空白差异） |
| `package-info`（根包） | 注释中 fork 基线由 2.7.x 改为 3.5.x |

### 仅存在于 `0.1.34` —— Boot 2 垫片，在 `0.2.34` 中删除

| 文件 | 删除原因 |
|---|---|
| `actuate/autoconfigure/endpoint/condition/AbstractEndpointCondition` | **API 兼容**：Boot 2.0–2.6 endpoint 条件内部实现的 fork；Boot 2.7/3.x 已原生提供 |
| `actuate/autoconfigure/endpoint/condition/ConditionalOnAvailableEndpoint` | 同上 —— 垫片注解不再需要 |
| `actuate/autoconfigure/endpoint/condition/OnAvailableEndpointCondition` | 同上 —— 配套条件实现 |
| `actuate/autoconfigure/endpoint/expose/IncludeExcludeEndpointFilter` | 同上 —— Boot 2 过滤器 API（含废弃的 `DefaultIncludes`）；Boot 3 使用 `EndpointExposure` |
| `actuate/endpoint/EndpointId` | 同上 —— 所有受支持 Boot 3 版本已原生提供该值类型 |
| `context/properties/ConstructorBinding` | 同上 —— Boot 3 中 `@ConstructorBinding` 已迁至 `org.springframework.boot.context.properties.bind`，旧全限定名作废 |

### 仅存在于 `0.2.34` —— 新增 Boot 3.5 fork

| 文件 | 新增原因 |
|---|---|
| `autoconfigure/jackson/JacksonProperties` | **功能需要**：模块的绑定/自动配置测试与扩展需要这些属性类在 Boot 3.0 下限版本上可用 |
| `autoconfigure/web/ServerProperties` | **功能需要**：同上；文件内的 `String.strip()` 属 Java 11 API，按 JDK 差异排除 |
| `autoconfigure/web/servlet/MultipartProperties` | **功能需要**：同上 |
| `web/servlet/MultipartConfigFactory` | **功能需要**：同上；其 `jakarta.servlet` import 属 Boot 3 的 EE 基线，按规则排除 |

## microsphere-spring-boot-core（src/main）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `SpringBootVersion` | 枚举常量为 Boot **2.0–2.7**（含 2.x 补丁区间） | 枚举常量为 Boot **3.0–4.1** | **功能需要**：版本目录必须描述 0.2.x 线所支持的版本 |
| `ConfigurableAutoConfigurationImportFilter` | `instanceof` + 强转 | `instanceof` 模式匹配 | 排除（Java 17 语法） |
| `ConfigurationPropertiesBeanContext` | 将 merged definition 强转 `RootBeanDefinition`，经 `BeanDefinitionUtils.getResolvableType` 取类型 | 直接调用 `BeanDefinition.getResolvableType()` | **API 兼容**：新版 Spring Framework 已在接口上暴露类型信息，反射时代的工具方法不再需要 |
| `ListenableBindHandlerAdapter` | 反射 `MethodHandle` 查找 `AbstractBindHandler.onCreate` 并做未找到回退（该方法 Boot 2.2.2 才有） | 直接 `super.onCreate(...)` | **API 兼容**：Boot ≥ 3.0 基线上该方法必然存在，跨版本探测成为死代码 |
| `BindUtils` | 反射加载 `BindConstructorProvider` 并读取 `DEFAULT` 字段，对 Boot < 2.2.1 做空值回退 | 直接 `BindConstructorProvider.DEFAULT.getBindConstructor(...)` | **API 兼容**：同上，放弃 Boot < 2.2.1 兼容路径 |
| `ConfigurationPropertyUtils` | `getParent` 用 `chop()`/`EMPTY` 手工实现；`toDashedForm` 反射探测 `DataObjectPropertyName`，保留手抄的 `BeanPropertyName` 回退（重载 `toDashedForm(name, start)`） | `ConfigurationPropertyName.getParent()`；直调 `DataObjectPropertyName.toDashedForm`；删除回退重载 | **API 兼容**：Boot 2.2.3 时代的私有 API 在 Boot 3 已稳定公开，旧版本模拟代码删除 |
| `ArtifactsCollisionDiagnosisListener` | `instanceof` + 强转 | `instanceof` 模式匹配 | 排除（Java 17 语法） |
| `FailureReportSpringApplicationRunListener` | 未废弃 | `@Deprecated(forRemoval = true, since = "0.2.9")` | **功能需要/生命周期**：Boot 3 用 `SpringApplicationEvent` 监听器取代 `SpringApplicationRunListener` 钩子，引导用户迁移 |
| `LoggingSpringApplicationRunListener` | 未废弃 | `@Deprecated(forRemoval = true, since = "0.2.9")` | 同上 |
| `SpringApplicationRunListenerAdapter` | `starting(ConfigurableBootstrapContext)` 会链式转调 `starting()`；未废弃 | 转调删除；`@Deprecated(forRemoval = true)` | **API 兼容 + 行为变更**：Boot 3 的接口已提供默认方法，适配器不再模拟无参 `starting()` 回调 |
| `SpringApplicationUtils` | 启动日志缺少 "additional profiles"；TRACE/DEBUG 无守卫 | 新增 `additional profiles`（取自 `SpringApplication.getAdditionalProfiles()`）；增加级别守卫；文本块 + 箭头 `switch` | **功能需要**：更完整的启动诊断（该 API 自 Boot 2.7/3 提供）；守卫避免关闭级别时构造参数数组。语法部分按 Java 17 差异排除 |
| `META-INF/spring.factories` | 还包含 `EnableAutoConfiguration=ConfigurationPropertiesAutoConfiguration` 条目 | 删除该条目；保留 `AutoConfigurationImportFilter`、`DefaultPropertiesPostProcessor` 等非自动配置条目 | **API 兼容**：Boot 3 的自动配置只从 `AutoConfiguration.imports` 读取（已存在）；其余 SPI key 在 `spring.factories` 中仍然有效 |

## microsphere-spring-boot-dependencies / microsphere-spring-boot-parent

无 `src/main` 源码 —— N/A。

## microsphere-spring-boot-test（src/main）

`src/main` 无差异（4 个文件一致）。

## microsphere-spring-boot-webflux（src/main）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `WebFluxAutoConfiguration` | `@AutoConfigureAfter` 仅列 `org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration` | 追加 `org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration` | **API 兼容**：Spring Boot 4.0 迁移了内置自动配置类；同时列出两个类名可在 Boot 3.x 与 4.x 上都保持正确的排序 |
| `META-INF/spring.factories` | 通过 `EnableAutoConfiguration` 注册 `WebFluxAutoConfiguration` | 删除 | **API 兼容**：Boot 3 改用 `AutoConfiguration.imports`（两个 tag 均已提供） |

## microsphere-spring-boot-webmvc（src/main）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `WebMvcAutoConfiguration` | `@AutoConfigureAfter` 仅列 `org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration` | 追加 `org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` | **API 兼容**：与 webflux 相同的 Boot 4.0 新类名锚定 |
| `ConditionalOnWebMvcAvailable` | `@ConditionalOnClass` 要求 `javax.servlet.Servlet` | 要求 `jakarta.servlet.Servlet` | 排除（Java EE → Jakarta EE） |
| `META-INF/spring.factories` | 通过 `EnableAutoConfiguration` 注册 `WebMvcAutoConfiguration` | 删除 | **API 兼容**：Boot 3 `.imports` 注册机制 |

## 差异原因归纳

| 原因类别 | 实例 |
|---|---|
| 与 Spring Boot 3/4 的 API 兼容（注册机制、新标记接口、类名迁移、更强的 BeanDefinition API） | actuator 描述符、4 处 `spring.factories`、webmvc/webflux 排序、`BindUtils`、`ListenableBindHandlerAdapter`、`ConfigurationPropertyUtils`、`ConfigurationPropertiesBeanContext` |
| 移除跨版本反射回退（0.1.x 须兼容 Boot 2.1–2.7；0.2.x 只假定 Boot ≥ 3.0） | `BindUtils`、`ListenableBindHandlerAdapter`、`ConfigurationPropertyUtils`、`SpringApplicationRunListenerAdapter` |
| compatible 模块 fork 换基（Boot 2.7 垫片 → Boot 3.5 属性类） | `compatible` 中 6 删 + 4 增 |
| 功能需要（版本目录换基、启动日志增强、废弃策略） | `SpringBootVersion`、`SpringApplicationUtils`、3 个 `@Deprecated` 监听器 |
| 排除项：Java 8 与 17 语法/API | 2 处 `instanceof` 模式匹配、文本块/箭头 `switch`、`String.strip()` |
| 排除项：Java EE 与 Jakarta EE | `ConditionalOnWebMvcAvailable`、新增 multipart fork 文件 |
