# Core 模块

> [English](../core.md) · 文档索引：[中文](../../zh/README.md)
>
> **分支：** `release` · **版本线：** `0.2.x` · **Artifact：** `microsphere-spring-boot-core`
> **Spring Boot：** `3.0.13` – `4.1.1` · **Java：** 17+

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

`core` 是整个 `0.2.x` 线的基础模块，负责“配置”和“诊断”两条主线：它让库和应用可以携带可被覆盖的默认配置，让自动配置的排除
可以由多个来源合并，把 `@ConfigurationProperties` 的绑定过程变成可观测、可响应的事件流，并在启动的最早期把 classpath 上的
依赖问题变成人类可读的失败报告。`actuator`、`webmvc`、`webflux` 三个模块都依赖它，因此任一模块被引入时这些能力都会生效。

基础包为 `io.microsphere.spring.boot`，用户会直接接触到的子包如下：

```
io.microsphere.spring.boot                        SpringBootVersion
io.microsphere.spring.boot.autoconfigure         ConfigurableAutoConfigurationImportFilter
io.microsphere.spring.boot.classloading          BannedArtifactClassLoadingListener
io.microsphere.spring.boot.condition             ConditionalOnPropertyPrefix
io.microsphere.spring.boot.context.autoconfigure ConfigurationPropertiesAutoConfiguration
io.microsphere.spring.boot.context.properties.bind     BindListener / ListenableBindHandlerAdapter
                                                   BindListeners / ListenableConfigurationPropertiesBindHandlerAdvisor
                                                   ConfigurationPropertiesBeanPropertyChangedEvent
                                                   EventPublishingConfigurationPropertiesBeanPropertyChangedListener
io.microsphere.spring.boot.context.properties.metadata  ConfigurationMetadataReader
io.microsphere.spring.boot.context.properties.annotation EnableConfigurationPropertiesExtension
io.microsphere.spring.boot.diagnostics           ArtifactsCollisionDiagnosisListener / ArtifactsCollisionException
                                                   ArtifactsCollisionFailureAnalyzer
io.microsphere.spring.boot.env                   DefaultPropertiesApplicationListener / DefaultPropertiesPostProcessor
                                                   SpringApplicationDefaultPropertiesPostProcessor
io.microsphere.spring.boot.env.config.metadata   ConfigurationMetadataRepository
io.microsphere.spring.boot.listener              FailureReportSpringApplicationRunListener
io.microsphere.spring.boot.report                ConditionEvaluationReportInitializer / ...Listener / ...ExceptionReporter
io.microsphere.spring.boot.util                  SpringApplicationUtils
```

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-core` |
| 最新发布 | `0.2.34`（2026-08-31） |
| 分支在研 | `0.2.35-SNAPSHOT` |
| Spring Boot | profile 固定 `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16（默认）/ 4.0.8 / 4.1.1` |
| Java | `17`（根 POM `<java.version>`），CI 验证 JDK `17 / 21 / 25` |
| 上游 | `microsphere-spring:0.2.39`（BOM 导入） |
| Boot 4.x 附加依赖 | `microsphere-spring-boot-compatible`，仅 `-Pspring-boot-4.0` / `-Pspring-boot-4.1` 时由本模块 POM 引入 |

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-core</artifactId>
    <!-- 版本由 microsphere-spring-boot-dependencies BOM 管理 -->
</dependency>
```

---

## 3. 默认属性机制

`io.microsphere.spring.boot.env.DefaultPropertiesApplicationListener` 监听 `ApplicationEnvironmentPreparedEvent`
（顺序常量 `DEFAULT_ORDER = LOWEST_PRECEDENCE - 1`），把所有匹配
`classpath*:/META-INF/config/default/*.*` 的 classpath 资源解析后写入 Spring Boot 的 `defaultProperties` 属性源——这是优先级
**最低**的属性源，因此 `application.properties`、环境变量、命令行参数都会覆盖它。合并使用 `putIfAbsent`：同一个键先到先得，
后来的重复键只产生一条 warn 日志而不会互相覆盖。资源解析委托 `PropertySourceLoaders`，所以 `.properties` 与 `.yml` 都被支持
（模式里的 `*.*` 就是为此）。

该模式常量定义在内置实现上：

```java
// io.microsphere.spring.boot.env.SpringApplicationDefaultPropertiesPostProcessor
public static final String DEFAULT_PROPERTIES_RESOURCES_PATTERN = "classpath*:/META-INF/config/default/*.*";
```

### 3.1 本模块自带的默认值

`src/main/resources/META-INF/config/default/core.properties`：

```properties
## Spring Boot Graceful Shutdown
server.shutdown = graceful
spring.lifecycle.timeout-per-shutdown-phase = 60s

## Disabled Spring Boot Built-in Filters
spring.mvc.hiddenmethod.filter.enabled = false
spring.mvc.formcontent.filter.enabled = false
spring.mvc.favicon.enabled = false

## Excluded auto-configuration by default (see section 5)
microsphere.autoconfigure.exclude=\
org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration

## Microsphere Spring switches
microsphere.spring.event-publishing-bean.enabled = true
microsphere.spring.listenable-autowire-candidate-resolver.enabled = false
microsphere.spring.listenable-environment.enabled = false
```

要改回 Spring Boot 原生行为，只需在自己的配置里显式覆盖，例如 `server.shutdown=immediate`。

### 3.2 为自己的库添加默认配置

在你的库的 jar 里放 `src/main/resources/META-INF/config/default/my-module.properties`，它会在运行时与上面的默认属性文件合并：

```properties
# META-INF/config/default/my-module.properties
my-app.client.connect-timeout = 5s
my-app.client.read-timeout = 30s
microsphere.autoconfigure.exclude = org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

### 3.3 SPI 扩展

```java
package com.example;

import io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor;

import java.util.Map;
import java.util.Set;

public class TeamDefaultsPostProcessor implements DefaultPropertiesPostProcessor {

    @Override
    public void initializeResources(Set<String> resources) {
        // 追加额外扫描位置
        resources.add("classpath*:/META-INF/config/team-defaults/*.properties");
    }

    @Override
    public void postProcess(Map<String, Object> defaults) {
        // 在发布前审查或补齐
        defaults.putIfAbsent("my-app.feature.enabled", Boolean.TRUE);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE; // 接口默认值
    }
}
```

```properties
# META-INF/spring.factories
io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=com.example.TeamDefaultsPostProcessor
```

**接口签名**：`DefaultPropertiesPostProcessor extends Ordered` 声明
`void initializeResources(Set<String>)`、`default void postProcess(Map<String, Object>)`、`default int getOrder()`。

---

## 4. SpringBootVersion 版本感知 API

`io.microsphere.spring.boot.SpringBootVersion` 是枚举，把 Spring Boot 版本建模为常量：本分支覆盖
`SPRING_BOOT_3_0` … `SPRING_BOOT_4_1_0`，末尾是 `CURRENT`，其值在类加载时由
`org.springframework.boot.SpringBootVersion` 探测得到。常量名到版本号的转换规则是：去掉 `SPRING_BOOT_`（固定 12 个字符，
即 `name.substring(12)`）后把 `_` 换成 `.`，所以 `SPRING_BOOT_3_2_5` 就是 `3.2.5`。

| 方法 | 别名 | 语义 |
|---|---|---|
| `getVersion()` | — | 版本字符串 |
| `getMajor()` / `getMinor()` / `getPatch()` | — | 数值分量 |
| `gt(SpringBootVersion)` | `isGreaterThan` | 大于 |
| `ge(SpringBootVersion)` | `isGreaterOrEqual` | 不小于 |
| `lt(SpringBootVersion)` | `isLessThan` | 小于 |
| `le(SpringBootVersion)` | `isLessOrEqual` | 不大于 |
| `eq(SpringBootVersion)` | `equals` | 相等 |
| `resolveVersion(String)` | — | 静态，名称/版本字符串 → 版本 |

```java
import io.microsphere.spring.boot.SpringBootVersion;

// 运行期分支：3.3 及以上使用新 API
if (SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_3_3)) {
    return modernImplementation();
}
return legacyImplementation();
```

```java
// 启动日志里打印真实版本
SpringBootVersion current = SpringBootVersion.CURRENT;
log.info("Spring Boot {}.{}, raw={}", current.getMajor(), current.getMinor(), current.getVersion());
```

> **跨分支提示** 该枚举在两条线上内容不同：`0.2.x`（本分支）建模 Boot 3/4，`0.1.x` 建模 Boot
> `2.0`–`2.7.18`。因此 `SPRING_BOOT_3_3` 这类常量只存在于 `0.2.x`。

---

## 5. ConfigurableAutoConfigurationImportFilter 属性驱动排除

`io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter` 通过 `spring.factories`
注册为 Spring Boot 的 `AutoConfigurationImportFilter`，在所有 Bean 创建之前按属性
`microsphere.autoconfigure.exclude`（常量 `AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME`）剔除自动配置类，排序为
`HIGHEST_PRECEDENCE + 99`。它与标准 `spring.autoconfigure.exclude` 的**唯一但关键**差别是语义：标准属性是“替换”，最高优先级
属性源独赢；本属性是“合并”，所有属性源里的排除项都会累加。因此多个团队/多个库各自排除不同自动配置时不会互相丢失。

```properties
# 逗号分隔
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration

# 或索引数组（同样被识别）
microsphere.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
microsphere.autoconfigure.exclude[1]=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

**程序化写入** —— 四个公开静态方法：

```java
import io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter;

// 追加单个
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(
        environment, "com.example.FooAutoConfiguration");

// 追加多个
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
        environment, "com.example.FooAutoConfiguration", "com.example.BarAutoConfiguration");
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
        environment, List.of("com.example.FooAutoConfiguration"));

// 读取当前合并结果
Set<String> excluded = ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment);
```

值既从 `PropertySource` 的逗号串读取，也通过 `Binder` 读取数组形式。

| 场景 | 建议 |
|---|---|
| 应用持有唯一权威排除清单 | `spring.autoconfigure.exclude` |
| 多个库/团队各自排除 | `microsphere.autoconfigure.exclude` |
| 在库的默认属性文件里排除 | `microsphere.autoconfigure.exclude` |

---

## 6. ConditionalOnPropertyPrefix 前缀条件

`io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix` 是自定义条件注解，`@Target({TYPE, METHOD})`、
`@Retention(RUNTIME)`、`@Conditional(OnPropertyPrefixCondition.class)`。它**只有一个成员** `String[] value()`。匹配逻辑：只要
`Environment` 中存在任一属性名以任一给定前缀开头，条件即成立（前缀会自动补 `.`，并支持占位符解析）。Spring Boot 自带的
`@ConditionalOnProperty` 要求精确属性名，而这个注解只要求“属性族”。

```java
import io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix;

@ConditionalOnPropertyPrefix("myapp.datasource")
@Configuration
public class DataSourceConfig {
    // 只有存在某个 myapp.datasource.* 配置时才加载
}
```

**多个前缀之间是 OR 关系**：

```java
@ConditionalOnPropertyPrefix({"myapp.redis", "myapp.cache"})
@Bean
public CacheManager cacheManager() {
    return new RedisCacheManager();
}
```

**典型用法**：“配了任一该族的属性就启用该功能”，无需再定义一个 `...enabled` 开关。

---

## 7. BindListener 绑定回调

`io.microsphere.spring.boot.context.properties.bind.BindListener` 把 `@ConfigurationProperties` 的绑定过程暴露为
五个生命周期回调，全部是 `default` 方法，只覆盖你关心的即可：

| 回调 | 签名 | 触发时机 |
|---|---|---|
| `onStart` | `<T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)` | 绑定开始 |
| `onSuccess` | `void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | 绑定成功 |
| `onCreate` | `void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | 未绑定结果新建实例时 |
| `onFailure` | `void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)` | 绑定失败 |
| `onFinish` | `void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | 绑定结束，失败时不调用 |

注意：这些方法返回 `void`，与 Spring Boot `BindHandler` 返回 `ConversionService.Convertible` 结果的语义不同——它们是观察者，
不参与改写绑定结果。

**装配链路**：
`ConfigurationPropertiesAutoConfiguration`（自动配置类，标注
`@ConditionalOnClass(name = "io.microsphere.spring.context.annotation.BeanCapableImportCandidate")`、
`@AutoConfigureBefore(org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration)`、
`@EnableConfigurationPropertiesExtension`）注册
`ListenableConfigurationPropertiesBindHandlerAdvisor`（实现 Boot 的
`ConfigurationPropertiesBindHandlerAdvisor`，`BeanFactoryAware`），它从 `BeanFactory` 里发现所有 `BindListener` Bean，并经
`ListenableBindHandlerAdapter extends AbstractBindHandler`（构造器 `(Iterable<BindListener>)` 与
`(BindHandler, Iterable<BindListener>)`；另有聚合实现 `BindListeners`）把它们串到每次绑定上。因此**只要把你的监听器声明成
Bean 就够了**。

```java
package com.example;

import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AuditBindListener implements BindListener {

    private static final Logger log = LoggerFactory.getLogger(AuditBindListener.class);

    private final List<String> boundNames = new CopyOnWriteArrayList<>();

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Object result) {
        boundNames.add(name.toString());
        log.info("Bound {} = {}", name, result);
    }

    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Exception error) {
        log.warn("Failed to bind {}", name, error);
    }

    public List<String> getBoundNames() {
        return boundNames;
    }
}
```

不想用 Bean 时可直接组装（容器之外使用）：

```java
import io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;

BindHandler handler = new ListenableBindHandlerAdapter(List.of(new AuditBindListener()));
Binder.get(environment, handler).bind("my-app", Bindable.of(MyAppProperties.class));
```

**常见用途**：审计配置是否真的生效、发现属性名拼写错误、统计绑定耗时、断言敏感键不出现在绑定结果里。

---

## 8. ConfigurationPropertiesBeanPropertyChangedEvent 属性变更事件

`io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent<T>`（继承
`io.microsphere.spring.context.event.BeanPropertyChangedEvent`）在某个 `@ConfigurationProperties` Bean 的属性值发生变化时发布到
`ApplicationContext`。声明的 getter 有两个：`ResolvableType getPropertyType()` 与
`ConfigurationProperty getConfigurationProperty()`（后者携带变更来源的 `getOrigin()`，可精确到文件与行）；父类提供
`getOldValue()`、`getNewValue()`、`getSource()`（即发生变更的 Bean 实例）。发布者是
`EventPublishingConfigurationPropertiesBeanPropertyChangedListener`，它在全部单例初始化完成后为每个
`@ConfigurationProperties` Bean 建立快照，之后的重新绑定即与快照比较。

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
        // 无需重启即可调整连接池
    }
}
```

**开关**：事件发布依赖 `microsphere.spring.event-publishing-bean.enabled=true`（`core.properties` 的默认值）。想要绑定
回调但不想要应用事件，把它设为 `false`。

---

## 9. Classpath 诊断

三组机制把“依赖冲突”从运行期玄学变成启动期的可读失败。

### 9.1 禁用指定构件

`io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener` 是 `SpringApplicationRunListener`，在
`ApplicationStartingEvent`（最早的时刻）生效，阻止被禁用 JAR 被类加载器加载。开关常量
`BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME = "microsphere.spring.boot.banned-artifacts.enabled"`，默认 `false`，且**读取来源是
JVM 系统属性**（`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`，通过 `Boolean.getBoolean` 读取）——因为它必须在
`Environment` 尚不存在时就能判定。因此用 `-D` 或在 `main` 里 `System.setProperty` 设置，写在 `application.properties` 里**无效**。

```bash
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar app.jar
```

```java
public static void main(String[] args) {
    System.setProperty("microsphere.spring.boot.banned-artifacts.enabled", "true");
    SpringApplication.run(MyApplication.class, args);
}
```

监听器内部保证禁用逻辑对每个 `SpringApplication` 只执行一次。

### 9.2 构件冲突检测

`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener`（`ApplicationListener`）扫描 classpath 上同一
artifact 的重复版本，抛出 `ArtifactsCollisionException`（携带冲突 artifact ID 集合）。开关常量
`ENABLED_PROPERTY_NAME = "microsphere.spring.boot.artifacts-collision.enabled"`，默认 `false`，**来源是 `Environment`**
（`environment.getProperty(name, Boolean.class, false)`）。与 9.1 不同，这个开关可以写在配置文件里：

```properties
microsphere.spring.boot.artifacts-collision.enabled=true
```

冲突一旦触发，`ArtifactsCollisionFailureAnalyzer`（Boot `FailureAnalyzer`）输出结构化报告，并给出现成的排查命令：

```
mvn dependency:tree -Dincludes=com.example:lib-a,com.example:lib-b
```

### 9.3 条件评估报告

`ConditionEvaluationReportInitializer`（`ApplicationContextInitializer`）、`ConditionEvaluationReportListener`
（`ApplicationListener`）与 `ConditionEvaluationSpringBootExceptionReporter`（`SpringBootExceptionReporter`）协作缓存
Spring Boot 的 `ConditionEvaluationReport`，在启动失败时打印“哪些自动配置命中了、哪些没命中以及为什么”。范围可调：

```properties
# 默认只报告 io.microsphere，可扩展
microsphere.spring.boot.conditions.report.base-packages=com.mycompany,io.microsphere
```

诊断日志级别由 `microsphere.spring.boot.logging.level` 控制，默认 `TRACE`。

---

## 10. OnceApplicationPreparedEventListener 一次性监听器

`io.microsphere.spring.boot.context.OnceApplicationPreparedEventListener` 是
`ApplicationListener<ApplicationPreparedEvent>`（上下文刷新前夕）的抽象基类，实现 `Ordered`，并按上下文 id 去重，保证同一个
context 只执行一次——在测试里复用上下文时尤其重要。子类需要实现两个抽象方法。仓库内已有实现：
`LoggingOnceApplicationPreparedEventListener`、抽象的 `OnceMainApplicationPreparedEventListener`（额外识别 Spring Cloud
bootstrap 上下文，常量 `BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME = "spring.cloud.bootstrap.name"`、
`DEFAULT_BOOTSTRAP_CONTEXT_ID = "bootstrap"`，并检测 `BootstrapApplicationListener`）以及
`LoggingOnceMainApplicationPreparedEventListener`。

```java
public class MySetupListener extends OnceApplicationPreparedEventListener {

    @Override
    protected boolean isIgnored(SpringApplication application, String[] args,
                                ConfigurableApplicationContext context) {
        return false; // true = 本 context 跳过
    }

    @Override
    protected void onApplicationEvent(SpringApplication application, String[] args,
                                      ConfigurableApplicationContext context) {
        // 每个 context 最多执行一次
    }
}
```

```properties
# META-INF/spring.factories
org.springframework.context.ApplicationListener=com.example.MySetupListener
```

---

## 11. SpringApplicationUtils 工具类

`io.microsphere.spring.boot.util.SpringApplicationUtils` 的公开静态方法：

| 方法 | 用途 |
|---|---|
| `addDefaultPropertiesResource(String)` | 追加一个默认属性资源路径 |
| `addDefaultPropertiesResources(String...)` | 追加多个 |
| `getDefaultPropertiesResources()` | 读取当前集合 |
| `getResourceLoader(SpringApplication)` | 安全获取 `ResourceLoader`，回退 `DefaultResourceLoader` |
| `getLoggingLevel(ConfigurableApplicationContext)` / `getLoggingLevel(PropertyResolver)` | 读取 `microsphere.spring.boot.logging.level` |
| `log(SpringApplication, String[], String, Object...)` | 按配置的级别打印 `SpringApplication` 诊断摘要 |
| `log(SpringApplication, String[], ConfigurableApplicationContext, String, Object...)` | 同上，附带上下文 |

```java
SpringApplicationUtils.addDefaultPropertiesResources(
        "classpath*:/META-INF/config/team-defaults/*.properties");
```

> **已废弃** `io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter` 仍存在于源码中，但新代码应直接实现
> `SpringApplicationRunListener` 或使用 `ApplicationListener<SpringApplicationEvent>`。

---

## 12. 扩展点注册

引入 `core` 即自动生效的注册项（`src/main/resources/META-INF/spring.factories` 与
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`）：

| Spring Boot 扩展点 | 注册实现 |
|---|---|
| `EnableAutoConfiguration`（imports 文件） | `...context.autoconfigure.ConfigurationPropertiesAutoConfiguration` |
| `AutoConfigurationImportFilter` | `...autoconfigure.ConfigurableAutoConfigurationImportFilter` |
| `SpringApplicationRunListener` | `...classloading.BannedArtifactClassLoadingListener`、`...listener.FailureReportSpringApplicationRunListener` |
| `ApplicationListener` | `...diagnostics.ArtifactsCollisionDiagnosisListener`、`...report.ConditionEvaluationReportListener`、`...env.DefaultPropertiesApplicationListener` |
| `ApplicationContextInitializer` | `...report.ConditionEvaluationReportInitializer`、`...env.config.OriginTrackedConfigurationPropertyInitializer` |
| `SpringBootExceptionReporter` | `...report.ConditionEvaluationSpringBootExceptionReporter` |
| `FailureAnalyzer` | `...diagnostics.ArtifactsCollisionFailureAnalyzer` |
| `DefaultPropertiesPostProcessor`（本库自有 SPI） | `...env.SpringApplicationDefaultPropertiesPostProcessor` |

`core` **没有** `org.springframework.boot.diagnostics.AbstractFailureAnalyzer` 之外的界面式入口，也不注册 Web 层组件；全部能力
都是“配置 + 诊断”。

另外，`io.microsphere.spring.boot.context.properties.metadata.ConfigurationMetadataReader` 读取
`classpath*:/META-INF/spring-configuration-metadata.json` 与
`classpath*:/META-INF/additional-spring-configuration-metadata.json`（常量 `METADATA_PATH`、`ADDITIONAL_METADATA_PATH`），是
`actuator` 模块元数据端点的数据来源。

---

## 13. 配置属性清单

| 属性 | 来源 | 默认值 | 作用 |
|---|---|---|---|
| `microsphere.autoconfigure.exclude` | Environment（合并） | 由 `core.properties` 设定 | 排除自动配置 |
| `microsphere.spring.boot.logging.level` | Environment | `TRACE` | 诊断日志级别 |
| `microsphere.spring.boot.banned-artifacts.enabled` | **JVM 系统属性** | `false` | 启用构件禁用 |
| `microsphere.spring.boot.artifacts-collision.enabled` | Environment | `false` | 启用冲突检测 |
| `microsphere.spring.boot.conditions.report.base-packages` | Environment | `io.microsphere` | 条件报告范围 |
| `microsphere.spring.event-publishing-bean.enabled` | Environment（Microsphere Spring） | `true` | 发布属性变更事件 |
| `microsphere.spring.listenable-autowire-candidate-resolver.enabled` | Environment | `false` | Microsphere Spring 开关 |
| `microsphere.spring.listenable-environment.enabled` | Environment | `false` | Microsphere Spring 开关 |
| `server.shutdown` / `spring.lifecycle.timeout-per-shutdown-phase` | Environment | `graceful` / `60s` | 本库默认值 |
| `spring.mvc.hiddenmethod.filter.enabled` / `formcontent` / `favicon` | Environment | `false` | 关闭噪声过滤器 |

---

## 14. 常见问题

| 现象 | 处理 |
|---|---|
| 我的 `server.shutdown` 被改了 | 那是 `core.properties` 的默认值，处在最低优先级源，显式覆盖即可。 |
| 写在 `application.properties` 的禁用构件开关没生效 | 该键只从 JVM 系统属性读取，用 `-D` 或 `System.setProperty`。 |
| `BindListener` 没被调用 | 它必须是 Spring Bean；同时确认 `ConfigurationPropertiesAutoConfiguration` 没被你排除。 |
| 属性变更事件收不到 | 检查 `microsphere.spring.event-publishing-bean.enabled`，以及变更是否真的来自重新绑定（首次绑定不发事件）。 |
| 排除项“丢了” | 你用了 `spring.autoconfigure.exclude`（替换语义），换成 `microsphere.autoconfigure.exclude`（合并）。 |
| 想确认库加载了哪个版本 | `GET /actuator/microsphere/artifacts`，见 [actuator.md](../actuator.md)。 |

---

**下一步：** [actuator.md](../actuator.md) · [webmvc.md](../webmvc.md) · 文档索引：[中文](../../zh/README.md)
