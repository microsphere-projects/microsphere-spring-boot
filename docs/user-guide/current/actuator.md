# Actuator Module

> [中文](./zh/actuator.md) · [Documentation index](../README.md)

> **Branch:** `release` · **Version line:** `0.2.x` · **Artifact:** `microsphere-spring-boot-actuator`
> **Spring Boot:** `3.0.13` – `4.1.1` · **Java:** 17+ · Depends on `microsphere-spring-boot-core`

Contents:
[1 Module Overview](#1-module-overview) ·
[2 Version Information](#2-version-information) ·
[3 Custom Endpoints](#3-custom-endpoints) ·
[4 Endpoint Defaults](#4-endpoint-defaults) ·
[5 MonitoredThreadPoolTaskScheduler](#5-monitoredthreadpooltaskscheduler) ·
[6 Autoconfiguration and Conditions](#6-autoconfiguration-and-conditions) ·
[7 Property Reference](#7-property-reference) ·
[8 FAQ](#8-faq)

---

## 1. Module Overview

The module does two things. First, it adds four read-only actuator endpoints covering information Spring Boot's own
endpoints do not expose: what is actually loaded at runtime, what the configuration metadata looks like, the bound
configuration values, and a one-shot snapshot of every endpoint. Second, it ships an opinionated endpoint policy (only
commonly used endpoints enabled, with per-endpoint cache TTLs) plus a thread-pool scheduler that instruments itself with
Micrometer.

Packages: `io.microsphere.spring.boot.actuate` (`MonitoredThreadPoolTaskScheduler`), `.autoconfigure`, `.condition`,
`.constants`, `.endpoint`.

---

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-actuator` |
| Latest published | `0.2.34` |
| In development | `0.2.35-SNAPSHOT` |
| Spring Boot profile pins | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16 (default) / 4.0.8 / 4.1.1` |
| Java | `17`; CI validates JDK `17 / 21 / 25` |
| Module deps | `microsphere-spring-boot-core`, `spring-boot-starter-actuator`, `spring-boot-starter-web`, `spring-boot-starter-webflux` (optional) |
| Jolokia | `2.6.3` (the default policy enables the `jolokia` endpoint) |
| Registration | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-actuator</artifactId>
</dependency>
```

---

## 3. Custom Endpoints

| Endpoint id | Type | Class | Default path | Read operation | Requires |
|---|---|---|---|---|---|
| `artifacts` | `@Endpoint` | `...actuate.endpoint.ArtifactsEndpoint` | `/actuator/microsphere/artifacts` | `List<Artifact> getArtifactMetaInfoList()` | — |
| `webEndpoints` | `@WebEndpoint` | `...actuate.endpoint.WebEndpoints` | `/actuator/microsphere/web/endpoints` | `Map<String, Object> invokeReadOperations()` | `@ConditionalOnWebApplication` |
| `configMetadata` | `@Endpoint` | `...actuate.endpoint.ConfigurationMetadataEndpoint` | `/actuator/microsphere/config/metadata` | `ConfigurationMetadataDescriptor getConfigurationMetadata()` | `spring-boot-configuration-processor` on the classpath |
| `configProperties` | `@Endpoint` | `...actuate.endpoint.ConfigurationPropertiesEndpoint` | `/actuator/microsphere/config/properties` | `ConfigurationPropertiesDescriptor getConfigurationProperties()` | — |

All four are **read-only** — no `@WriteOperation` exists in the sources. The paths come from
`management.endpoints.web.path-mapping.*` defaults, so changing `management.endpoints.web.base-path` moves them with it.

### 3.1 artifacts

`ArtifactsEndpoint` takes a `ClassLoader` in its constructor and delegates to
`io.microsphere.classloading.ArtifactDetector` with `detect(false)`, returning a `List<Artifact>`. Each element is an
`io.microsphere.classloading.Artifact`, readable via `getArtifactId()` and also carrying the full Maven coordinates. Use
it to audit the library versions actually loaded in an environment.

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

`invokeReadOperations()` on `@WebEndpoint(id = "webEndpoints")` invokes every parameter-less read operation across the
registered web endpoints and returns the combined map — one HTTP request for a runtime snapshot, handy for triage scripts
and smoke checks.

```bash
curl -s http://localhost:8080/actuator/microsphere/web/endpoints | jq '.health, .info, .env'
```

### 3.3 configMetadata

Returns a `ConfigurationMetadataEndpoint.ConfigurationMetadataDescriptor` (a nested static class implementing
`OperationResponseBody`) with `getGroups()` and `getProperties()`. The data is read by `core`'s
`ConfigurationMetadataReader` from `META-INF/spring-configuration-metadata.json` and
`META-INF/additional-spring-configuration-metadata.json`, then aggregated by `ConfigurationMetadataRepository`. This is
the same data IDEs use for property completion, so the endpoint explains why a property has no hint.

> Registered by the nested `ConfigurationProcessorConfiguration` under
> `@ConditionalOnConfigurationProcessorPresent`; without the annotation processor the endpoint does not exist at all
> (404).

### 3.4 configProperties

Returns a `ConfigurationPropertiesDescriptor` (`getConfigurationProperties()`) that merges two sources: configuration
properties declared through Java ServiceLoader entries and the properties in the configuration metadata repository.
Useful to confirm what is declared and what actually bound.

### 3.5 Register an Endpoint Manually

Every auto-configured endpoint bean is `@ConditionalOnMissingBean`, so yours wins:

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

## 4. Endpoint Defaults

The module ships `src/main/resources/META-INF/config/default/endpoints.properties`, which reaches `defaultProperties`
(lowest precedence) through the core default-properties mechanism ([core.md](./core.md)). Its stance: **everything off by
default, only the usual set on**, plus a sensible cache TTL per endpoint.

```properties
management.endpoints.enabled-by-default = false
management.endpoints.web.exposure.include = *
management.endpoints.jmx.exposure.exclude = *

# Enabled endpoints (true)
management.endpoint.env.enabled = true
management.endpoint.health.enabled = true
management.endpoint.info.enabled = true
management.endpoint.loggers.enabled = true
management.endpoint.metrics.enabled = true
management.endpoint.mappings.enabled = true
management.endpoint.jolokia.enabled = true
management.endpoint.prometheus.enabled = true

# Disabled endpoints (false)
# auditevents, beans, caches, conditions, configprops, flyway, httptrace,
# integrationgraph, liquibase, scheduledtasks, sessions, shutdown, threaddump, heapdump, logfile

# TTL presets
microsphere.cache.no-time-to-live = 0s
microsphere.cache.short-time-to-live = 15s
microsphere.cache.middle-time-to-live = 1m
microsphere.cache.long-time-to-live = 1h
microsphere.cache.long-long-time-to-live = 1d

# health details
management.health.defaults.enabled = true
management.endpoint.health.show-details = always
management.endpoint.health.show-components = always

# This module's four endpoints + paths
management.endpoint.artifacts.enabled = true
management.endpoints.web.path-mapping.artifacts = microsphere/artifacts
management.endpoint.webEndpoints.enabled = true
management.endpoints.web.path-mapping.webEndpoints = microsphere/web/endpoints
management.endpoint.configMetadata.enabled = true
management.endpoints.web.path-mapping.configMetadata = microsphere/config/metadata
management.endpoint.configProperties.enabled = true
management.endpoints.web.path-mapping.configProperties = microsphere/config/properties
```

The presets are referenced per endpoint, for example:

```properties
management.endpoint.env.cache.time-to-live = ${microsphere.cache.middle-time-to-live}
management.endpoint.health.cache.time-to-live = ${microsphere.cache.no-time-to-live}
management.endpoint.metrics.cache.time-to-live = ${microsphere.cache.no-time-to-live}
management.endpoint.artifacts.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
management.endpoint.webEndpoints.cache.time-to-live = ${microsphere.cache.short-time-to-live}
management.endpoint.configMetadata.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
management.endpoint.configProperties.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
```

**Override example** — because these are `defaultProperties`, any of your own configuration takes effect:

```properties
# application.properties
management.endpoint.shutdown.enabled=true          # enable shutdown, which is off by default
management.endpoint.env.cache.time-to-live=0s      # make env real-time
management.endpoints.web.base-path=/admin          # paths become /admin/microsphere/artifacts
```

---

## 5. MonitoredThreadPoolTaskScheduler

`io.microsphere.spring.boot.actuate.MonitoredThreadPoolTaskScheduler` extends Spring's `ThreadPoolTaskScheduler`,
implements `ApplicationContextAware` and `SmartInitializingSingleton`, and has a **no-arg constructor**. Inside
`createExecutor(int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler)` it wraps the real executor
in a `DelegatingScheduledExecutorService`, and once all singletons are ready `afterSingletonsInstantiated()` takes the
`MeterRegistry` from the `ApplicationContext` and swaps the delegate for
`ExecutorServiceMetrics.monitor(registry, scheduledExecutor, beanName)`. `getScheduledExecutor()` returns the monitored
delegate, so every task submitted to the scheduler produces the standard `executor.*` metrics (active, queued, completed,
duration) tagged by bean name.

**Auto-configuration** (`ActuatorAutoConfiguration`, `@AutoConfigureOrder(LOWEST_PRECEDENCE)`):

```java
@ConditionalOnBean(type = "io.micrometer.core.instrument.MeterRegistry")
@Bean(name = "actuatorTaskScheduler", destroyMethod = "shutdown")
public ThreadPoolTaskScheduler actuatorTaskScheduler(
        @Value("${microsphere.spring.boot.actuator.task-scheduler.pool-size:1}") int poolSize,
        @Value("${microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix:microsphere-spring-boot-actuator-task-}")
        String threadNamePrefix) { ... }
```

Which means **no `MeterRegistry`, no bean**. Injection:

```java
@Autowired
@Qualifier("actuatorTaskScheduler")
private ThreadPoolTaskScheduler scheduler;
```

**Tuning**:

```properties
microsphere.spring.boot.actuator.task-scheduler.pool-size=2
microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix=my-actuator-
```

**Your own monitored scheduler** — because the `MeterRegistry` is resolved from the container, the bean must be managed by
Spring:

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

**Verify**:

```bash
curl -s http://localhost:8080/actuator/metrics/executor.active | jq '.tagValues'
```

---

## 6. Autoconfiguration and Conditions

Contents of `AutoConfiguration.imports`:

```
io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration
io.microsphere.spring.boot.actuate.autoconfigure.ActuatorEndpointsAutoConfiguration
```

| Auto-config | Class-level conditions | Registered beans |
|---|---|---|
| `ActuatorAutoConfiguration` | no class-level condition; method-level `@ConditionalOnBean(type = MeterRegistry)` | `actuatorTaskScheduler` |
| `ActuatorEndpointsAutoConfiguration` | `@ConditionalOnActuatorEndpointPresent`, `BeanClassLoaderAware` | `artifactsEndpoint`, `webEndpoints` (`@ConditionalOnWebApplication`); nested `ConfigurationProcessorConfiguration` (`@ConditionalOnConfigurationProcessorPresent`) → `configurationMetadataReader`, `configurationMetadataRepository`, `configurationMetadataEndpoint`, `configurationPropertiesEndpoint` |

Endpoint beans additionally carry `@ConditionalOnMissingBean` and `@ConditionalOnAvailableEndpoint`.

| Condition annotation | Checks |
|---|---|
| `io.microsphere.spring.boot.actuate.condition.ConditionalOnActuatorEndpointPresent` | meta-`@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")` |
| `...actuate.condition.ConditionalOnConfigurationProcessorPresent` | meta-`@ConditionalOnClass(name = "org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata")` |

Both are `@Target({TYPE, METHOD})`, `@Retention(RUNTIME)`, `@Documented`, and neither carries its own `Condition`
implementation (they reuse Spring Boot's `@ConditionalOnClass` semantics).

**Getting the metadata endpoint** requires the annotation processor:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

There is also `META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader` =
`io.microsphere.metadata.MetadataResourceConfigurationPropertyLoader`, which makes the configuration metadata discoverable
through Microsphere's property-loading SPI.

---

## 7. Property Reference

| Property | Default | Effect |
|---|---|---|
| `microsphere.spring.boot.actuator.task-scheduler.pool-size` | `1` | scheduler thread pool size |
| `microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix` | `microsphere-spring-boot-actuator-task-` | thread name prefix |
| `management.endpoints.enabled-by-default` | `false` (library default) | endpoint master switch |
| `management.endpoints.web.exposure.include` | `*` | expose over HTTP |
| `management.endpoints.jmx.exposure.exclude` | `*` | disable JMX exposure |
| `management.endpoint.{artifacts,webEndpoints,configMetadata,configProperties}.enabled` | `true` | the four custom endpoints |
| `management.endpoints.web.path-mapping.{artifacts,webEndpoints,configMetadata,configProperties}` | `microsphere/...` | custom endpoint paths |
| `microsphere.cache.{no,short,middle,long,long-long}-time-to-live` | `0s/15s/1m/1h/1d` | TTL presets |
| `management.endpoint.<id>.cache.time-to-live` | preset-backed (references the presets above) | per-endpoint cache |
| `management.endpoint.health.show-details` / `show-components` | `always` | health detail verbosity |

---

## 8. FAQ

| Symptom | Answer |
|---|---|
| Every endpoint returns 404 | The library default is `enabled-by-default=false`; enable the endpoints you need explicitly or set `management.endpoint.<id>.enabled`. |
| `/actuator/microsphere/config/metadata` returns 404 | `spring-boot-configuration-processor` is missing, so the endpoint is never registered (see §6). |
| `actuatorTaskScheduler` is absent | There is no `MeterRegistry` bean; add a Micrometer registry. |
| `new MonitoredThreadPoolTaskScheduler(registry)` does not compile | The constructor is no-arg; the `MeterRegistry` is resolved from the container, so the bean must be Spring-managed. |
| Health shows no components | The defaults are already `show-details/show-components=always`; check your own overrides. |
| Want a different path prefix | Change `management.endpoints.web.base-path` or the matching `path-mapping`. |

---

**Up:** [README.md](./README.md) · Related: [core.md](./core.md) (default properties and metadata readers)
