# Actuator 模块

> [English](../actuator.md) · 文档索引：[中文](../../zh/README.md)

> **分支：** `release` · **版本线：** `0.2.x` · **Artifact：** `microsphere-spring-boot-actuator`
> **Spring Boot：** `3.0.13` – `4.1.1` · **Java：** 17+ · 依赖 `microsphere-spring-boot-core`

目录：
[1 概览](#1-模块概览) ·
[2 版本信息](#2-版本信息) ·
[3 自定义端点](#3-自定义端点) ·
[4 端点默认策略](#4-端点默认策略) ·
[5 被监控调度器](#5-monitoredthreadpooltaskscheduler-被监控调度器) ·
[6 自动配置与条件](#6-自动配置与条件) ·
[7 属性清单](#7-配置属性清单) ·
[8 常见问题](#8-常见问题)

---

## 1. 模块概览

本模块做两件事：一是新增四个只读 Actuator 端点，把“运行时到底加载了什么、配置元数据长什么样、绑定后的配置值是什么、
所有端点的一次性快照”这些 Spring Boot 自带端点不覆盖的信息暴露出来；二是带来一套“有主张”的端点默认策略（默认只开常用
端点、逐个设置缓存 TTL），以及一个自动接入 Micrometer 的线程池调度器。

包结构：`io.microsphere.spring.boot.actuate`（`MonitoredThreadPoolTaskScheduler`）、`.autoconfigure`、`.condition`、
`.constants`、`.endpoint`。

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-actuator` |
| 最新发布 | `0.2.34` |
| 分支在研 | `0.2.35-SNAPSHOT` |
| Spring Boot profile 固定 | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16（默认）/ 4.0.8 / 4.1.1` |
| Java | `17`，CI 验证 JDK `17 / 21 / 25` |
| 模块依赖 | `microsphere-spring-boot-core`、`spring-boot-starter-actuator`、`spring-boot-starter-web`、`spring-boot-starter-webflux`（可选） |
| Jolokia | `2.6.3`（默认策略里启用了 `jolokia` 端点） |
| 自动配置注册 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-actuator</artifactId>
</dependency>
```

---

## 3. 自定义端点

| 端点 id | 类型 | 实现类 | 默认路径 | 读操作 | 生效条件 |
|---|---|---|---|---|---|
| `artifacts` | `@Endpoint` | `...actuate.endpoint.ArtifactsEndpoint` | `/actuator/microsphere/artifacts` | `List<Artifact> getArtifactMetaInfoList()` | — |
| `webEndpoints` | `@WebEndpoint` | `...actuate.endpoint.WebEndpoints` | `/actuator/microsphere/web/endpoints` | `Map<String, Object> invokeReadOperations()` | `@ConditionalOnWebApplication` |
| `configMetadata` | `@Endpoint` | `...actuate.endpoint.ConfigurationMetadataEndpoint` | `/actuator/microsphere/config/metadata` | `ConfigurationMetadataDescriptor getConfigurationMetadata()` | classpath 上有 `spring-boot-configuration-processor` |
| `configProperties` | `@Endpoint` | `...actuate.endpoint.ConfigurationPropertiesEndpoint` | `/actuator/microsphere/config/properties` | `ConfigurationPropertiesDescriptor getConfigurationProperties()` | — |

四个端点**都是只读**：源码中没有 `@WriteOperation`。路径来自默认属性里的
`management.endpoints.web.path-mapping.*`，因此改变 `management.endpoints.web.base-path` 时前缀会随之变化。

### 3.1 artifacts

`ArtifactsEndpoint` 的构造器接收 `ClassLoader`，内部用 `io.microsphere.classloading.ArtifactDetector` 执行
`detect(false)` 并返回 `List<Artifact>`（每个元素是一个 `io.microsphere.classloading.Artifact`，代码里可用
`getArtifactId()`，另含完整 Maven 坐标）。用途：核对生产环境里真实加载的库版本。

```bash
curl -s http://localhost:8080/actuator/microsphere/artifacts
```

```json
[
  {
    "groupId": "io.github.microsphere-projects",
    "artifactId": "microsphere-spring-boot-core",
    "version": "0.2.34"
  }
]
```

### 3.2 webEndpoints

`@WebEndpoint(id = "webEndpoints")` 的 `invokeReadOperations()` 会调用所有**无参数**的端点读操作，把结果聚合成一个 map——
一次 HTTP 请求拿到“运行时快照”，适合排障脚本和冒烟检查。

```bash
curl -s http://localhost:8080/actuator/microsphere/web/endpoints | jq '.health, .info, .env'
```

### 3.3 configMetadata

返回 `ConfigurationMetadataEndpoint.ConfigurationMetadataDescriptor`（嵌套静态类，实现 `OperationResponseBody`），提供
`getGroups()` 与 `getProperties()`；数据由 `core` 的 `ConfigurationMetadataReader` 读取
`META-INF/spring-configuration-metadata.json` 与 `META-INF/additional-spring-configuration-metadata.json`，再经
`ConfigurationMetadataRepository` 汇总。这正是 IDE 做属性补全用的同一份数据，因此可用来核对“某个属性为什么没有提示”。

> 该端点由内嵌配置类 `ConfigurationProcessorConfiguration` 注册，条件为
> `@ConditionalOnConfigurationProcessorPresent`；缺少注解处理器时端点根本不存在（404）。

### 3.4 configProperties

返回 `ConfigurationPropertiesDescriptor`（`getConfigurationProperties()`），汇总两处来源：通过 Java ServiceLoader 声明的配置属性，
以及配置元数据仓库里的属性。适合核对“哪些属性被声明、绑定后的实际值”。

### 3.5 手工注册端点

所有自动配置的端点 Bean 都带 `@ConditionalOnMissingBean`，你自己的定义优先：

```java
import io.microsphere.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EndpointConfig {

    @Bean
    public ArtifactsEndpoint artifactsEndpoint(ClassLoader classLoader) {
        return new ArtifactsEndpoint(classLoader);
    }
}
```

---

## 4. 端点默认策略

模块自带 `src/main/resources/META-INF/config/default/endpoints.properties`，通过
[core.md §3 默认属性机制](./core.md)的默认属性机制进入 `defaultProperties`（最低优先级）。它的主张是：**默认全关，只开常用**，
并给每个端点配一个合适的缓存 TTL。

```properties
management.endpoints.enabled-by-default = false
management.endpoints.web.exposure.include = *
management.endpoints.jmx.exposure.exclude = *

# 开启的端点（true）
management.endpoint.env.enabled = true
management.endpoint.health.enabled = true
management.endpoint.info.enabled = true
management.endpoint.loggers.enabled = true
management.endpoint.metrics.enabled = true
management.endpoint.mappings.enabled = true
management.endpoint.jolokia.enabled = true
management.endpoint.prometheus.enabled = true

# 关闭的端点（false）
# auditevents, beans, caches, conditions, configprops, flyway, httptrace,
# integrationgraph, liquibase, scheduledtasks, sessions, shutdown, threaddump, heapdump, logfile

# TTL 预设
microsphere.cache.no-time-to-live = 0s
microsphere.cache.short-time-to-live = 15s
microsphere.cache.middle-time-to-live = 1m
microsphere.cache.long-time-to-live = 1h
microsphere.cache.long-long-time-to-live = 1d

# health 细节
management.health.defaults.enabled = true
management.endpoint.health.show-details = always
management.endpoint.health.show-components = always

# 本模块四个端点 + 路径
management.endpoint.artifacts.enabled = true
management.endpoints.web.path-mapping.artifacts = microsphere/artifacts
management.endpoint.webEndpoints.enabled = true
management.endpoints.web.path-mapping.webEndpoints = microsphere/web/endpoints
management.endpoint.configMetadata.enabled = true
management.endpoints.web.path-mapping.configMetadata = microsphere/config/metadata
management.endpoint.configProperties.enabled = true
management.endpoints.web.path-mapping.configProperties = microsphere/config/properties
```

TTL 预设被逐个端点引用（示例）：

```properties
management.endpoint.env.cache.time-to-live = ${microsphere.cache.middle-time-to-live}
management.endpoint.health.cache.time-to-live = ${microsphere.cache.no-time-to-live}
management.endpoint.metrics.cache.time-to-live = ${microsphere.cache.no-time-to-live}
management.endpoint.artifacts.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
management.endpoint.webEndpoints.cache.time-to-live = ${microsphere.cache.short-time-to-live}
management.endpoint.configMetadata.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
management.endpoint.configProperties.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
```

覆盖示例——由于是 `defaultProperties`，你的任何配置都会生效：

```properties
# application.properties
management.endpoint.shutdown.enabled=true          # 打开默认关闭的 shutdown
management.endpoint.env.cache.time-to-live=0s      # 让 env 实时
management.endpoints.web.base-path=/admin          # 路径变为 /admin/microsphere/artifacts
```

---

## 5. MonitoredThreadPoolTaskScheduler 被监控调度器

`io.microsphere.spring.boot.actuate.MonitoredThreadPoolTaskScheduler` 继承 Spring 的 `ThreadPoolTaskScheduler`，
实现 `ApplicationContextAware` 与 `SmartInitializingSingleton`，**构造器是无参的**。它在
`createExecutor(int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler)` 里用
`DelegatingScheduledExecutorService` 包住真实执行器，并在所有单例就绪后（`afterSingletonsInstantiated()`）从
`ApplicationContext` 取得 `MeterRegistry`，把委托替换为
`ExecutorServiceMetrics.monitor(registry, scheduledExecutor, beanName)`。`getScheduledExecutor()` 返回被监控的委托，因此
提交给调度器的任务都会产生标准 `executor.*` 指标（活跃、排队、完成、耗时），并以 Bean 名作为 tag。

自动配置（`ActuatorAutoConfiguration`，`@AutoConfigureOrder(LOWEST_PRECEDENCE)`）：

```java
@ConditionalOnBean(type = "io.micrometer.core.instrument.MeterRegistry")
@Bean(name = "actuatorTaskScheduler", destroyMethod = "shutdown")
public ThreadPoolTaskScheduler actuatorTaskScheduler(
        @Value("${microsphere.spring.boot.actuator.task-scheduler.pool-size:1}") int poolSize,
        @Value("${microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix:microsphere-spring-boot-actuator-task-}")
        String threadNamePrefix) { ... }
```

也就是说：**没有 `MeterRegistry` 就没有这个 Bean**。注入方式：

```java
@Autowired
@Qualifier("actuatorTaskScheduler")
private ThreadPoolTaskScheduler scheduler;
```

调优：

```properties
microsphere.spring.boot.actuator.task-scheduler.pool-size=2
microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix=my-actuator-
```

自建调度器——由于 `MeterRegistry` 从容器解析，这个 Bean 必须由 Spring 管理：

```java
import io.microsphere.spring.boot.actuate.MonitoredThreadPoolTaskScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    @Bean
    public MonitoredThreadPoolTaskScheduler taskScheduler() {
        MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("my-tasks-");
        scheduler.setDaemon(true);
        return scheduler;
    }
}
```

验证：

```bash
curl -s http://localhost:8080/actuator/metrics/executor.active | jq '.tagValues'
```

---

## 6. 自动配置与条件

`AutoConfiguration.imports` 内容：

```
io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration
io.microsphere.spring.boot.actuate.autoconfigure.ActuatorEndpointsAutoConfiguration
```

| 自动配置 | 类级条件 | 注册的 Bean |
|---|---|---|
| `ActuatorAutoConfiguration` | 无类级条件；方法级 `@ConditionalOnBean(type = MeterRegistry)` | `actuatorTaskScheduler` |
| `ActuatorEndpointsAutoConfiguration` | `@ConditionalOnActuatorEndpointPresent`，`BeanClassLoaderAware` | `artifactsEndpoint`、`webEndpoints`（`@ConditionalOnWebApplication`）；内嵌 `ConfigurationProcessorConfiguration`（`@ConditionalOnConfigurationProcessorPresent`）→ `configurationMetadataReader`、`configurationMetadataRepository`、`configurationMetadataEndpoint`、`configurationPropertiesEndpoint` |

所有端点 Bean 另带 `@ConditionalOnMissingBean` 与 `@ConditionalOnAvailableEndpoint`。

| 条件注解 | 检查内容 |
|---|---|
| `io.microsphere.spring.boot.actuate.condition.ConditionalOnActuatorEndpointPresent` | meta-`@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")` |
| `...actuate.condition.ConditionalOnConfigurationProcessorPresent` | meta-`@ConditionalOnClass(name = "org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata")` |

两者均为 `@Target({TYPE, METHOD})`、`@Retention(RUNTIME)`、`@Documented`，且自身不携带 Condition 实现类（复用 Spring Boot 的
`@ConditionalOnClass` 语义）。

启用注解处理器的写法：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

另有 `META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader` =
`io.microsphere.metadata.MetadataResourceConfigurationPropertyLoader`，使配置元数据可被 Microsphere 的属性加载 SPI 发现。

---

## 7. 配置属性清单

| 属性 | 默认 | 作用 |
|---|---|---|
| `microsphere.spring.boot.actuator.task-scheduler.pool-size` | `1` | 调度器线程池大小 |
| `microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix` | `microsphere-spring-boot-actuator-task-` | 线程名前缀 |
| `management.endpoints.enabled-by-default` | `false`（库默认） | 端点总开关 |
| `management.endpoints.web.exposure.include` | `*` | Web 暴露 |
| `management.endpoints.jmx.exposure.exclude` | `*` | 关闭 JMX 暴露 |
| `management.endpoint.{artifacts,webEndpoints,configMetadata,configProperties}.enabled` | `true` | 四个自定义端点 |
| `management.endpoints.web.path-mapping.{artifacts,webEndpoints,configMetadata,configProperties}` | `microsphere/...` | 自定义端点路径 |
| `microsphere.cache.{no,short,middle,long,long-long}-time-to-live` | `0s/15s/1m/1h/1d` | TTL 预设 |
| `management.endpoint.<id>.cache.time-to-live` | 引用上面的预设 | 单端点缓存 |
| `management.endpoint.health.show-details` / `show-components` | `always` | health 细节 |

---

## 8. 常见问题

| 现象 | 处理 |
|---|---|
| 所有端点都 404 | 库默认 `enabled-by-default=false`；显式开启需要的端点，或改 `management.endpoint.<id>.enabled`。 |
| `/actuator/microsphere/config/metadata` 404 | 缺少 `spring-boot-configuration-processor`，端点未注册（见 §6）。 |
| `actuatorTaskScheduler` 不存在 | 没有 `MeterRegistry` Bean；引入 Micrometer 注册表。 |
| `new MonitoredThreadPoolTaskScheduler(registry)` 编译不过 | 构造器是无参的；`MeterRegistry` 由容器解析，Bean 必须交给 Spring 管理。 |
| health 里看不到组件 | 默认已 `show-details/show-components=always`；若被覆盖请检查自身配置。 |
| 想改路径前缀 | 改 `management.endpoints.web.base-path` 或对应 `path-mapping`。 |

---

**上一级：** [README.md](./README.md) · 相关：[core.md](./core.md)（默认属性与元数据读取）
