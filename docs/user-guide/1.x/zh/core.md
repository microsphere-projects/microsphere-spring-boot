# Core 模块

> [English](../core.md) · 文档索引：[中文](../../zh/README.md)
>
> **分支：** `release-1.x` · **版本线：** `0.1.x` · **Artifact：** `microsphere-spring-boot-core`
> **Spring Boot：** `2.1.18.RELEASE` – `2.7.18` · **Java：** 8+

目录：
[1 概览](#1-模块概览) ·
[2 版本信息](#2-版本信息) ·
[3 默认属性](#3-默认属性机制) ·
[4 SpringBootVersion](#4-springbootversion-版本感知-api) ·
[5 自动配置排除](#5-configurableautoconfigurationimportfilter-属性驱动排除) ·
[6 前缀条件](#6-conditionalonpropertyprefix-前缀条件) ·
[7 BindListener](#7-bindlistener-绑定回调) ·
[8 属性变更事件](#8-configurationpropertiesbeanpropertychangedevent-属性变更事件) ·
[9 Classpath 诊断](#9-classpath-诊断) ·
[10 一次性监听器](#10-onceapplicationpreparedeventlistener-一次性监听器) ·
[11 工具类](#11-springapplicationutils-工具类) ·
[12 扩展点注册](#12-扩展点注册) ·
[13 属性清单](#13-配置属性清单) ·
[14 常见问题](#14-常见问题)

---

## 1. 模块概览

`0.1.x` 线的 `core` 与 `0.2.x` 线提供同一套能力：可覆盖的模块级默认属性、可合并的自动配置排除、属性前缀条件、
`@ConfigurationProperties` 绑定的可观测化与变更事件、以及把 classpath 冲突提前到启动期报错的诊断机制。差别集中在**承载方式**：
Spring Boot 2.x 没有 `AutoConfiguration.imports`（2.7 才引入），因此本分支同时提供 `spring.factories` 与 `imports` 两份注册；
版本枚举只建模 Boot 2.x；`compatible` shim 服务于**低版本** Boot（2.1–2.3）而不是高版本。

基础包为 `io.microsphere.spring.boot`，用户会直接接触到的子包如下：

```
io.microsphere.spring.boot                             SpringBootVersion
io.microsphere.spring.boot.autoconfigure               ConfigurableAutoConfigurationImportFilter
io.microsphere.spring.boot.classloading                BannedArtifactClassLoadingListener
io.microsphere.spring.boot.condition                   ConditionalOnPropertyPrefix
io.microsphere.spring.boot.context                     OnceApplicationPreparedEventListener
                                                       OnceMainApplicationPreparedEventListener
io.microsphere.spring.boot.context.autoconfigure       ConfigurationPropertiesAutoConfiguration
io.microsphere.spring.boot.context.properties.bind     BindListener / BindListeners / ListenableBindHandlerAdapter
                                                       ListenableConfigurationPropertiesBindHandlerAdvisor
                                                       ConfigurationPropertiesBeanPropertyChangedEvent
                                                       EventPublishingConfigurationPropertiesBeanPropertyChangedListener
io.microsphere.spring.boot.context.properties.source.util  ConfigurationPropertyUtils
io.microsphere.spring.boot.diagnostics                 ArtifactsCollisionDiagnosisListener / ArtifactsCollisionException
                                                       ArtifactsCollisionFailureAnalyzer
io.microsphere.spring.boot.env                         DefaultPropertiesApplicationListener / DefaultPropertiesPostProcessor
                                                       SpringApplicationDefaultPropertiesPostProcessor
io.microsphere.spring.boot.listener                    FailureReportSpringApplicationRunListener
                                                       LoggingSpringApplicationRunListener / SpringApplicationRunListenerAdapter
io.microsphere.spring.boot.report                      ConditionEvaluationReportInitializer / ...Listener / ...ExceptionReporter
io.microsphere.spring.boot.util                        SpringApplicationUtils
```

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-core` |
| 最新发布 | `0.1.34` |
| 分支在研 | `0.1.35-SNAPSHOT` |
| Spring Boot profile 固定 | `2.1.18.RELEASE / 2.2.13.RELEASE / 2.3.12.RELEASE / 2.4.13 / 2.5.15 / 2.6.15 / 2.7.18（默认）` |
| Java | 分支 POM 未设定语言级别（继承 `microsphere-build:0.3.14`）；CI 验证 JDK `8 / 11 / 17 / 21 / 25`，因此支持下限为 Java 8 |
| 上游 | `microsphere-spring:0.1.39`（BOM 导入） |
| 低版本附加依赖 | `microsphere-spring-boot-compatible`，仅 `-Pspring-boot-2.1` / `2.2` / `2.3` 时由本模块 POM 引入 |
| 注册 | `META-INF/spring.factories`（`EnableAutoConfiguration`）**与** `META-INF/spring/…AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-core</artifactId>
    <!-- 由 microsphere-spring-boot-dependencies:0.1.34 管理 -->
</dependency>
```

---

## 3. 默认属性机制

`io.microsphere.spring.boot.env.DefaultPropertiesApplicationListener` 监听 `ApplicationEnvironmentPreparedEvent`
（`DEFAULT_ORDER = LOWEST_PRECEDENCE - 1`），扫描 `classpath*:/META-INF/config/default/*.*` 的所有资源，合并进 Spring Boot 优先级
最低的 `defaultProperties` 属性源。你的 `application.properties`、配置中心、环境变量、命令行参数都会覆盖它。合并按
`putIfAbsent` 进行：同一个键先到先得，后来的重复键只记录 warn。

```java
// io.microsphere.spring.boot.env.SpringApplicationDefaultPropertiesPostProcessor
public static final String DEFAULT_PROPERTIES_RESOURCES_PATTERN = "classpath*:/META-INF/config/default/*.*";
```

本模块自带的文件 `META-INF/config/default/core.properties`:

```properties
server.shutdown = graceful
spring.lifecycle.timeout-per-shutdown-phase = 60s

spring.mvc.hiddenmethod.filter.enabled = false
spring.mvc.formcontent.filter.enabled = false
spring.mvc.favicon.enabled = false

microsphere.autoconfigure.exclude=\
org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration

microsphere.spring.event-publishing-bean.enabled = true
microsphere.spring.listenable-autowire-candidate-resolver.enabled = false
microsphere.spring.listenable-environment.enabled = false
```

> **Boot 2 语义提醒** `spring.mvc.favicon.enabled` 在 Spring Boot 2.x 是有效属性；到 Boot 3 该属性已随
> favicon 端点一并移除。因此这份默认属性文件是**按 Boot 2 线编写**的，`0.2.x` 线内容相同但前两项之外多为无害冗余。

给自己的库加默认值：在你的 jar 里放
`src/main/resources/META-INF/config/default/my-module.properties`。

```properties
my-app.client.connect-timeout = 5s
microsphere.autoconfigure.exclude = org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

SPI 扩展：

```java
package com.example;

import io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor;

import java.util.Map;
import java.util.Set;

public class TeamDefaultsPostProcessor implements DefaultPropertiesPostProcessor {

    @Override
    public void initializeResources(Set<String> resources) {
        resources.add("classpath*:/META-INF/config/team-defaults/*.properties");
    }

    @Override
    public void postProcess(Map<String, Object> defaults) {
        defaults.putIfAbsent("my-app.feature.enabled", Boolean.TRUE);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;   // 接口默认
    }
}
```

```properties
# META-INF/spring.factories
io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=com.example.TeamDefaultsPostProcessor
```

接口签名：`DefaultPropertiesPostProcessor extends Ordered` 声明
`void initializeResources(Set<String>)`、`default void postProcess(Map<String, Object>)`、`default int getOrder()`。
资源加载委托 `PropertySourceLoaders`，`.properties` 与 `.yml` 都支持（模式写 `*.*` 正是为此）。

---

## 4. SpringBootVersion 版本感知 API

本分支的 `io.microsphere.spring.boot.SpringBootVersion` 只建模 Spring Boot 2.x：首个常量 `SPRING_BOOT_2_0`，末个数值常量
`SPRING_BOOT_2_7_18`，其后是运行时探测常量 `CURRENT`。常量名到版本号的规则与 `0.2.x` 一致：`name.substring(12)`（去掉
`SPRING_BOOT_`）后把 `_` 换成 `.`，因此 `SPRING_BOOT_2_7_18` 即 `2.7.18`。**在本分支上不存在 `SPRING_BOOT_3_*`、`SPRING_BOOT_4_*`
常量**——从 `0.2.x` 移植代码时这里是首要编译错误来源。

| 方法 | 别名 | 语义 |
|---|---|---|
| `getVersion()` | — | 版本字符串 |
| `getMajor()` / `getMinor()` / `getPatch()` | — | 数值分量 |
| `gt(...)` | `isGreaterThan` | 大于 |
| `ge(...)` | `isGreaterOrEqual` | 不小于 |
| `lt(...)` | `isLessThan` | 小于 |
| `le(...)` | `isLessOrEqual` | 不大于 |
| `eq(...)` | `equals` | 相等 |
| `resolveVersion(String)` | — | 静态解析 |

```java
import io.microsphere.spring.boot.SpringBootVersion;

// 2.6 起才有的行为
if (SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_2_6)) {
    useNewerPath();
} else {
    useOlderPath();
}
```

```java
// 逐补丁号精确判断
if (SpringBootVersion.CURRENT.lt(SpringBootVersion.SPRING_BOOT_2_7_18)) {
    log.warn("建议升级到 2.7.18");
}
```

---

## 5. ConfigurableAutoConfigurationImportFilter 属性驱动排除

`io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter` 经 `spring.factories` 注册为
Spring Boot 的 `AutoConfigurationImportFilter`（该扩展点自 Boot 2.7 起才存在，`AutoConfigurationImportFilter` SPI 在 2.7 引入；
本分支仍注册它，在支持的 Boot 版本上生效），顺序 `HIGHEST_PRECEDENCE + 99`，按属性
`microsphere.autoconfigure.exclude`（常量 `AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME`）在 Bean 创建前剔除自动配置。与标准
`spring.autoconfigure.exclude`（本库另有 `SpringBootPropertyConstants` 引用该键与附着属性源名 `configurationProperties`）的关键差别：
标准属性**替换**、本属性**合并**。

```properties
# 逗号分隔
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration

# 索引数组（两种形式都识别）
microsphere.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

程序化 API —— 四个公开静态方法：

```java
import io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter;

ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(
        environment, "com.example.FooAutoConfiguration");

ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
        environment, "com.example.FooAutoConfiguration", "com.example.BarAutoConfiguration");

ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
        environment, List.of("com.example.FooAutoConfiguration"));

Set<String> excluded = ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment);
```

> **多模块价值** 在 Boot 2 时代的多模块工程里，每个 starter 各自写
> `spring.autoconfigure.exclude` 会互相抹掉；把它们写进各自的 `META-INF/config/default/*.properties` 并使用
> `microsphere.autoconfigure.exclude` 即可全部保留。

---

## 6. ConditionalOnPropertyPrefix 前缀条件

`io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix`：`@Target({TYPE, METHOD})`、
`@Retention(RUNTIME)`、`@Conditional(OnPropertyPrefixCondition.class)`，**唯一成员**是 `String[] value()`。只要
`Environment` 中任一属性名以任一给定前缀开头即成立（自动补 `.`，支持占位符解析）。Spring Boot 2.x 自带的
`@ConditionalOnProperty` 需要精确属性名，本注解只要求“属性族存在”。

```java
import io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix;

@ConditionalOnPropertyPrefix("myapp.datasource")
@Configuration
public class DataSourceConfig {
    // 仅当存在某个 myapp.datasource.* 时装配
}
```

```java
// 多前缀之间是 OR
@ConditionalOnPropertyPrefix({"myapp.redis", "myapp.cache"})
@Bean
public CacheManager cacheManager() {
    return new RedisCacheManager();
}
```

---

## 7. BindListener 绑定回调

`io.microsphere.spring.boot.context.properties.bind.BindListener` 提供五个 `default` 回调，方法签名与 `0.2.x`
完全一致：

| 回调 | 签名 | 触发时机 |
|---|---|---|
| `onStart` | `<T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)` | 绑定开始 |
| `onSuccess` | `void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | 成功 |
| `onCreate` | `void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | 未绑定结果新建实例 |
| `onFailure` | `void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)` | 失败 |
| `onFinish` | `void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | 结束，失败时不调用 |

装配链路：`ConfigurationPropertiesAutoConfiguration`（标注
`@ConditionalOnClass(name = "io.microsphere.spring.context.annotation.BeanCapableImportCandidate")`、
`@AutoConfigureBefore(org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration)`、
`@EnableConfigurationPropertiesExtension`）注册
`ListenableConfigurationPropertiesBindHandlerAdvisor`（Boot `ConfigurationPropertiesBindHandlerAdvisor`，
`BeanFactoryAware`），它从容器发现所有 `BindListener` Bean 并通过 `ListenableBindHandlerAdapter`
（`extends AbstractBindHandler`，构造器 `(Iterable<BindListener>)` 与 `(BindHandler, Iterable<BindListener>)`；聚合实现
`BindListeners`）串联到每次绑定。**把监听器声明为 Bean 即可生效。**

```java
package com.example;

import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.stereotype.Component;

@Component
public class AuditBindListener implements BindListener {

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Object result) {
        log.info("Bound {} = {}", name, result);
    }

    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Exception error) {
        log.warn("Failed to bind {}", name, error);
    }
}
```

容器外的手工用法：

```java
import io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;

BindHandler handler = new ListenableBindHandlerAdapter(List.of(new AuditBindListener()));
Binder.get(environment, handler).bind("my-app", Bindable.of(MyAppProperties.class));
```

> **Boot 2 注意事项** 方法返回 `void`，与 Spring Boot 2.x `BindHandler` 的返回值语义不同：监听器只观察，不改写绑定
> 结果。若需要改写，请自行实现 `BindHandler` 并用 `ListenableBindHandlerAdapter(BindHandler, Iterable<BindListener>)`
> 组合。

---

## 8. ConfigurationPropertiesBeanPropertyChangedEvent 属性变更事件

`io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent<T>` 继承
`io.microsphere.spring.context.event.BeanPropertyChangedEvent`，声明两个 getter：
`ResolvableType getPropertyType()`、`ConfigurationProperty getConfigurationProperty()`（后者携带 `getOrigin()`，可精确到文件与行）；
父类提供 `getOldValue()`、
`getNewValue()`、`getSource()`（发生变更的 Bean 实例）。发布者
`EventPublishingConfigurationPropertiesBeanPropertyChangedListener` 在全部单例就绪后为每个 `@ConfigurationProperties` Bean 建立
快照，之后的重新绑定与快照比对并发事件——首次绑定不发事件。总开关是默认属性里的
`microsphere.spring.event-publishing-bean.enabled = true`。

```java
package com.example;

import io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PoolResizeListener {

    @EventListener
    public void onChange(ConfigurationPropertiesBeanPropertyChangedEvent<?> event) {
        if (!"app.pool.size".equals(event.getConfigurationProperty().getName().toString())) {
            return;
        }
        Integer oldValue = (Integer) event.getOldValue();
        Integer newValue = (Integer) event.getNewValue();
        log.info("Pool resized {} -> {} (origin: {})",
                oldValue, newValue, event.getConfigurationProperty().getOrigin());
    }
}
```

在 `@RefreshScope` / 配置中心推送场景下，这是把“配置改了”转成“动作发生了”的最短路径。

---

## 9. Classpath 诊断

### 9.1 禁用指定构件

`io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener`（`SpringApplicationRunListener`）在
`ApplicationStartingEvent` 阻止被禁用 JAR 被加载。开关常量
`BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME = "microsphere.spring.boot.banned-artifacts.enabled"`，默认 `false`，**来源是 JVM
系统属性**（注解声明 `source = SYSTEM_PROPERTIES_SOURCE`，实现使用 `Boolean.getBoolean`）。写在配置文件里无效：

```bash
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar app.jar
```

```java
public static void main(String[] args) {
    System.setProperty("microsphere.spring.boot.banned-artifacts.enabled", "true");
    SpringApplication.run(MyApplication.class, args);
}
```

### 9.2 冲突检测

`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener` 扫描同一 artifact 的重复版本并抛出
`ArtifactsCollisionException`。开关常量 `ENABLED_PROPERTY_NAME = "microsphere.spring.boot.artifacts-collision.enabled"`，
默认 `false`，**来源是 `Environment`**（`environment.getProperty(name, Boolean.class, false)`）：

```properties
microsphere.spring.boot.artifacts-collision.enabled=true
```

随后 `ArtifactsCollisionFailureAnalyzer`（Boot `FailureAnalyzer`）给出结构化报告与现成排查命令：

```
mvn dependency:tree -Dincludes=com.example:lib-a,com.example:lib-b
```

### 9.3 条件评估报告

`ConditionEvaluationReportInitializer`（`ApplicationContextInitializer`）+ `ConditionEvaluationReportListener`
（`ApplicationListener`）+ `ConditionEvaluationSpringBootExceptionReporter`（`SpringBootExceptionReporter`）缓存
`ConditionEvaluationReport`，启动失败时输出命中与未命中及原因；范围可调：

```properties
microsphere.spring.boot.conditions.report.base-packages=com.mycompany,io.microsphere   # 默认 io.microsphere
microsphere.spring.boot.logging.level=TRACE                                            # 默认 TRACE
```

本分支另有 `LoggingSpringApplicationRunListener` 与 `FailureReportSpringApplicationRunListener` 两个
`SpringApplicationRunListener` 参与日志与失败报告。

---

## 10. OnceApplicationPreparedEventListener 一次性监听器

`io.microsphere.spring.boot.context.OnceApplicationPreparedEventListener` 是
`ApplicationListener<ApplicationPreparedEvent>` 抽象基类（同时实现 `Ordered`），按上下文 id 去重，保证同一 context 只执行一次。
两个抽象方法：

```java
protected abstract boolean isIgnored(SpringApplication application, String[] args,
                                     ConfigurableApplicationContext context);

protected abstract void onApplicationEvent(SpringApplication application, String[] args,
                                           ConfigurableApplicationContext context);
```

**Boot 2 线特有的 bootstrap 感知** 抽象子类
`OnceMainApplicationPreparedEventListener` 会识别 Spring Cloud 的 bootstrap 上下文：常量
`BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME = "spring.cloud.bootstrap.name"`、`DEFAULT_BOOTSTRAP_CONTEXT_ID = "bootstrap"`，并检测
`BootstrapApplicationListener`；额外提供 `protected final boolean isIgnored(ConfigurableApplicationContext)` 与
`protected boolean isIgnored(...)` 重载。仓库内已有实现 `LoggingOnceApplicationPreparedEventListener` 与
`LoggingOnceMainApplicationPreparedEventListener`——在同时存在 bootstrap 与主上下文的 Boot 2 应用里，只有主上下文会被处理一次。

```java
public class MySetupListener extends OnceMainApplicationPreparedEventListener {

    @Override
    protected boolean isIgnored(SpringApplication application, String[] args,
                                ConfigurableApplicationContext context) {
        return false;   // true = 跳过本 context
    }

    @Override
    protected void onApplicationEvent(SpringApplication application, String[] args,
                                      ConfigurableApplicationContext context) {
        // 每个主上下文最多一次
    }
}
```

```properties
# META-INF/spring.factories
org.springframework.context.ApplicationListener=com.example.MySetupListener
```

---

## 11. SpringApplicationUtils 工具类

`io.microsphere.spring.boot.util.SpringApplicationUtils` 公开静态方法：

| 方法 | 用途 |
|---|---|
| `addDefaultPropertiesResource(String)` | 追加一个默认属性资源路径 |
| `addDefaultPropertiesResources(String...)` | 追加多个 |
| `getDefaultPropertiesResources()` | 读取当前集合 |
| `getResourceLoader(SpringApplication)` | 安全获取 `ResourceLoader`（回退 `DefaultResourceLoader`） |
| `getLoggingLevel(ConfigurableApplicationContext)` / `getLoggingLevel(PropertyResolver)` | 读取 `microsphere.spring.boot.logging.level` |
| `log(SpringApplication, String[], String, Object...)` | 按配置级别打印 `SpringApplication` 诊断摘要 |
| `log(SpringApplication, String[], ConfigurableApplicationContext, String, Object...)` | 同上并附带上下文 |

```java
SpringApplicationUtils.addDefaultPropertiesResources(
        "classpath*:/META-INF/config/team-defaults/*.properties");
```

> **已废弃** `io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter` 仍在源码中；新代码请直接实现
> `SpringApplicationRunListener` 或使用 `ApplicationListener<SpringApplicationEvent>`。

---

## 12. 扩展点注册

本分支**双份注册**：`META-INF/spring.factories` 用 Boot 2 的键
`org.springframework.boot.autoconfigure.EnableAutoConfiguration`，同时 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
也存在（Boot 2.7 起识别，便于向 Boot 3 迁移）。其余扩展点只在 `spring.factories` 中：

```properties
# microsphere-spring-boot-core/src/main/resources/META-INF/spring.factories
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.spring.boot.report.ConditionEvaluationReportInitializer,\
io.microsphere.spring.boot.env.config.OriginTrackedConfigurationPropertyInitializer

org.springframework.boot.SpringApplicationRunListener=\
io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener,\
io.microsphere.spring.boot.listener.FailureReportSpringApplicationRunListener

org.springframework.context.ApplicationListener=\
io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener,\
io.microsphere.spring.boot.report.ConditionEvaluationReportListener,\
io.microsphere.spring.boot.env.DefaultPropertiesApplicationListener

org.springframework.boot.SpringBootExceptionReporter=\
io.microsphere.spring.boot.report.ConditionEvaluationSpringBootExceptionReporter

org.springframework.boot.diagnostics.FailureAnalyzer=\
io.microsphere.spring.boot.diagnostics.ArtifactsCollisionFailureAnalyzer

org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter

org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.spring.boot.context.autoconfigure.ConfigurationPropertiesAutoConfiguration

io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=\
io.microsphere.spring.boot.env.SpringApplicationDefaultPropertiesPostProcessor
```

`actuator`、`webmvc`、`webflux` 三个模块各自提供同样的两份自动配置注册。

---

## 13. 配置属性清单

| 属性 | 来源 | 默认 | 作用 |
|---|---|---|---|
| `microsphere.autoconfigure.exclude` | Environment（合并） | 由 `core.properties` 设定 | 排除自动配置 |
| `microsphere.spring.boot.logging.level` | Environment | `TRACE` | 诊断日志级别 |
| `microsphere.spring.boot.banned-artifacts.enabled` | **JVM 系统属性** | `false` | 启用构件禁用 |
| `microsphere.spring.boot.artifacts-collision.enabled` | Environment | `false` | 启用冲突检测 |
| `microsphere.spring.boot.conditions.report.base-packages` | Environment | `io.microsphere` | 条件报告范围 |
| `microsphere.spring.event-publishing-bean.enabled` | Environment | `true` | 发布属性变更事件 |
| `microsphere.spring.listenable-autowire-candidate-resolver.enabled` | Environment | `false` | Microsphere Spring 开关 |
| `microsphere.spring.listenable-environment.enabled` | Environment | `false` | Microsphere Spring 开关 |
| `spring.cloud.bootstrap.name` | Environment | `bootstrap`（库内常量） | bootstrap 上下文识别 |

---

## 14. 常见问题

| 现象 | 处理 |
|---|---|
| 升级到 `0.2.x` 时编译报 `SPRING_BOOT_2_7` 不存在 | 枚举按分支建模版本：`0.1.x` 只有 Boot 2.x 常量，`0.2.x` 只有 Boot 3/4 常量。改写判断条件。 |
| 禁用构件开关不生效 | 只从 JVM 系统属性读取，需 `-D` 或 `System.setProperty`。 |
| `BindListener` 不触发 | 必须是 Spring Bean，且 `ConfigurationPropertiesAutoConfiguration` 未被排除。 |
| 属性变更事件收不到 | 检查 `microsphere.spring.event-publishing-bean.enabled`；首次绑定不发事件，只有重新绑定才会。 |
| Boot 2.4/2.5 上自动配置没生效 | 属正常：`AutoConfigurationImportFilter` 扩展点在 Boot 2.7 才引入，2.4–2.6 上排除合并不可用；其余 `spring.factories` 注册项仍生效。 |
| Spring Cloud 应用里 setup 逻辑跑了两遍 | 继承 `OnceMainApplicationPreparedEventListener` 而非 `OnceApplicationPreparedEventListener`，即可跳过 bootstrap 上下文。 |

---

**下一步：** [actuator.md](./actuator.md) · [webmvc.md](./webmvc.md) · 文档索引：[中文](../../zh/README.md)
