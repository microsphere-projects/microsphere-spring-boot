# 源码对比 — `src/test`：tag `0.2.34` vs tag `0.1.34`

对比范围：最新的两个 tag —— `0.2.34`（Java 17 / Spring Boot 3.5.x–4.x 线）与 `0.1.34`（Java 8 / Spring Boot 2.x 线）。差异方向为 `0.1.34 → 0.2.34`（“新”指 `0.2.34`）。本文仅覆盖 `src/test`；`src/main` 对比见 [main-diff](./main-diff-0.2.34-vs-0.1.34.md)。

- 各模块均同名匹配；`src/test` 源码仅存在于 `actuator`、`core`、`test`、`webflux`、`webmvc`（`compatible` 两个 tag 上均无测试源码）。
- 排除的差异类别：**Java 17 与 Java 8 语法/API 差异**（`instanceof` 模式匹配等）与 **Java EE 与 Jakarta EE 差异**（`javax.annotation/javax.servlet → jakarta.*`）。

## 模块总览

| Maven 模块 | test 文件数 `0.1.34` | test 文件数 `0.2.34` | 变更文件数 | 排除后的实质差异 |
|---|---|---|---|---|
| actuator | 6 | 6 | 0 | 0 |
| compatible | 0 | 0 | 0 | N/A |
| core | 53 | 53 | 10 | 9（1 个属排除项） |
| dependencies / parent | 0 | 0 | 0 | N/A |
| test | 5 | 5 | 2 | 2 |
| webflux | 4 | 4 | 0 | 0 |
| webmvc | 5 | 5 | 2 | 2（另含 1 处属排除项的 import 变更） |

## microsphere-spring-boot-core（src/test）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `SpringBootVersionTest` | 针对 `SPRING_BOOT_2_0…2_7` 常量断言版本区间/比较运算符 | 改为针对 `SPRING_BOOT_3_0…4_1`（如 3.5.0–3.5.14、4.0.0–4.0.7） | **与 main 同步**：`SpringBootVersion` 枚举已换基，测试跟随新目录 |
| `ApplicationAutoConfigurationTest` | `import javax.annotation.Resource` | `import jakarta.annotation.Resource` | 排除（Java EE → Jakarta EE） |
| `LoggingOnceMainApplicationPreparedEventListenerTest` | 辅助方法先建 context，再把空 `URLClassLoader` 设到 `DefaultListableBeanFactory` | 直接把空类加载器设到 `GenericApplicationContext`；删除辅助方法 | **测试简化**：Boot 3 基线上 context 级类加载器即可覆盖所有查找路径，无需 bean-factory 级的布点 |
| `BindableConfigurationBeanBinderTest` | `assertThrows(BindException…)` 包在 `if (CURRENT.ge(SPRING_BOOT_2_2))` 中 | 无条件 `assertThrows` | **移除跨版本守卫**：0.2.x 只支持 Boot ≥ 3.0，Boot 2.2 分支已成死代码 |
| `TestConstructorBindingConfigurationProperties` | 使用 Boot 2 的 `context.properties.ConstructorBinding` 标注构造器，字段可变 + setter + 无参构造器 | 改用 Boot 3 的 `context.properties.bind.ConstructorBinding`；字段 `final`，仅构造器绑定 | **API 兼容**：注解在 Boot 2.2+ 迁移包名、Boot 3 删除旧全限定名；单构造器即隐式启用构造器绑定 |
| `EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest` | 仅测试 `ServerProperties` + `TestConfigurationProperties` 的变更传播 | 新增绑定 `WebProperties`（`spring.web.locale`、`locale-resolver`、`static-locations`），新增 `testWebProperties` 用例断言变更事件的旧值/新值 | **功能需要/扩大覆盖**：在第三个属性类（含嵌套 + 列表绑定）上验证变更传播；`WebProperties` 自 Boot 2.4/3.x 起存在 |
| `ListenableBindHandlerAdapterTest` | 按 `onCreateMethodHandle == NOT_FOUND_METHOD_HANDLE` 分支；反射调用 `Binder.bindOrCreate` | 直接调用 `binder.bindOrCreate(...)`；`createBinder` 辅助方法返回类型改为 `BindHandler` | **与 main 同步**：main 中已移除反射回退，测试相应去掉版本分支 |
| `BindUtilsTest` | `getBindConstructor(of(ServerProperties.class), …)` 期望 `null`，构造器绑定的测试类也期望 `null`；`TestBindConstructorClass` 的断言被注释 | `getBindConstructor(forClass(TestConstructorBindingConfigurationProperties.class))` 期望**非 null**；注释掉的断言恢复（期望 `null`） | **与 main 同步**：直调 `BindConstructorProvider.DEFAULT` 与原反射路径的解析结果不同，可观察行为随之翻转 |
| `ConfigurationPropertyUtilsTest` | 含 `testToDashedFormWithStart`，对比 `toDashedForm(name)` 与 `toDashedForm(name, 0)` | 该测试与辅助方法删除 | **与 main 同步**：main 中 protected 回退重载 `toDashedForm(name, start)` 已删除 |
| `ArtifactsCollisionFailureAnalyzerTest` | 类上有 `@Component`（并有对应 import） | 移除 `@Component` | **测试清理**：测试类不应成为被扫描的组件，借现代化改造一并修正 |

## microsphere-spring-boot-test（src/test）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `ReactiveWebApplicationAutoConfiguration` | `@ConditionalOnClass` + `@EnableConfigurationProperties` 使用 `JacksonProperties` | 改用 `JmxProperties` | **规避 fork 冲突**：0.2.x 中 `org.springframework.boot.autoconfigure.jackson.JacksonProperties` 是未发布的 `compatible` 模块里的 **fork 类**（Boot 4 profile 下附加）；测试夹具改用未被 fork 的 `JmxProperties`，确保条件判断针对 Boot 原生类 |
| `ReactiveWebApplicationAutoConfigurationTest` | 将 `JacksonProperties` 加入期望的自动配置/缺失类集合 | 改为 `JmxProperties` | 同上 —— 跟随夹具调整 |

## microsphere-spring-boot-webmvc（src/test）

| 文件 | `0.1.34` | `0.2.34` | 差异原因 |
|---|---|---|---|
| `AbstractWebMvcAutoConfigurationTest` | context 仅加载 `TestController`；5 项端点检查 | `@SpringJUnitConfig` 加入 `RouterFunctionTestConfig`，`testWebEndpoints()` 新增 `testUpdatePerson()`（`PUT /test/person/{id}`）；（`javax→jakarta ServletException` import 变更属排除项） | **功能需要/扩大覆盖**：功能测试同时验证 `RouterFunction` 端点与注解式控制器 |
| `WebMvcAutoConfigurationAllEnabledTest` | 断言内容协商策略恰好 2 个、顺序固定、第 2 个为参数名 `"p"` 的 `ParameterContentNegotiationStrategy` | 只断言列表非空，且其中若存在 `ParameterContentNegotiationStrategy` 则参数名为 `"p"` | **断言适配**：Boot 3/4 默认策略的组成与顺序不同于 Boot 2，按位置断言过于脆弱，改为存在性检查 |

## 差异原因归纳

| 原因类别 | 实例 |
|---|---|
| 与 main 侧移除反射/跨版本回退保持同步 | `ListenableBindHandlerAdapterTest`、`BindUtilsTest`、`ConfigurationPropertyUtilsTest`、`BindableConfigurationBeanBinderTest` |
| Spring Boot API 迁移（构造器绑定） | `TestConstructorBindingConfigurationProperties` |
| 版本目录换基 | `SpringBootVersionTest` |
| 扩大功能覆盖 | `EventPublishing…Test`（WebProperties）、`AbstractWebMvcAutoConfigurationTest`（RouterFunction） |
| 测试夹具规避 fork 类冲突 | `ReactiveWebApplicationAutoConfiguration(+Test)` |
| 测试简化/清理 | `LoggingOnce…Test`、`ArtifactsCollisionFailureAnalyzerTest` |
| 适配 Boot 3/4 默认行为的断言健壮化 | `WebMvcAutoConfigurationAllEnabledTest` |
| 排除项：Java EE → Jakarta EE | `ApplicationAutoConfigurationTest`、`AbstractWebMvcAutoConfigurationTest` 的 import |
