# Microsphere Spring Boot - User Guide

## Overview

`microsphere-spring-boot` is an **extension library for Spring Boot** that adds extra utilities and features on top of the standard framework. It is organized into three modules:

| Module | What it adds |
|---|---|
| `microsphere-spring-boot-core` | Core utilities: conditions, property loading, lifecycle hooks, diagnostics |
| `microsphere-spring-boot-actuator` | Custom Actuator endpoints and a monitored task scheduler |
| `microsphere-spring-boot-compatible` | Compatibility shims for older Spring Boot APIs |

---

## Module 1 — Core (`microsphere-spring-boot-core`)

### 1. `SpringBootVersion` (enum)

**What it does:** Lists every Spring Boot release from 3.0 through 4.0.x as an enum constant, plus a special `CURRENT` constant that detects the running version at startup.

**Key concept:** Each enum name like `SPRING_BOOT_3_2_5` is parsed automatically — `_` is swapped for `.` — to produce the semantic version `3.2.5`. The `CURRENT` entry reads the real version from Spring Boot's own `SpringBootVersion` class.

**Methods:** `gt()`, `lt()`, `ge()`, `le()`, `eq()` let you write version guards like:

```java
if (SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_3_3)) {
    // use 3.3+ APIs
}
```

**Use case:** Writing code that needs to behave differently on different Spring Boot releases without hardcoded string comparisons.

---

### 2. `ConditionalOnPropertyPrefix` + `OnPropertyPrefixCondition`

**What it does:** A custom Spring `@Conditional` annotation that activates a bean only when **at least one property whose name starts with a given prefix** is present in the environment.

**Key concept:** Spring Boot's built-in `@ConditionalOnProperty` requires an exact property name. This annotation instead checks for a *prefix*, so any property under `myapp.feature.*` will trigger activation.

**Example:**

```java
@ConditionalOnPropertyPrefix("myapp.datasource")
@Configuration
public class DataSourceConfig {
    // only loaded when any "myapp.datasource.*" property exists
}
```

The logic lives in `OnPropertyPrefixCondition.getMatchOutcome()`, which scans all property names in the environment and returns a match if any starts with one of the given prefixes.

---

### 3. `ConfigurableAutoConfigurationImportFilter`

**What it does:** Implements Spring Boot's `AutoConfigurationImportFilter` to **exclude specific auto-configuration classes at startup** — driven by a property, not just `@SpringBootApplication(exclude=...)`.

**Key concept:** Reads the property `microsphere.autoconfigure.exclude` (comma-separated or indexed array). The filter runs before beans are created and marks matching auto-configuration classes as "not to be imported."

**Example:**

```properties
# application.properties
microsphere.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

Or programmatically:

```java
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(
    environment, "com.example.FooAutoConfiguration");
```

**Use case:** Dynamically excluding auto-configurations without recompiling or changing annotations. Useful in multi-module applications or test harnesses.

---

### 4. `BannedArtifactClassLoadingListener`

**What it does:** Listens for the very first moment Spring Boot starts (`ApplicationStartingEvent`) and, when enabled, **prevents banned JAR artifacts from being loaded by the class loader**.

**Key concept:** Enabled by a JVM system property:

```
-Dmicrosphere.spring.boot.banned-artifacts.enabled=true
```

It uses `BannedArtifactClassLoadingExecutor` to perform the actual blocking. A `ConcurrentHashMap` ensures the banning logic only runs once per `SpringApplication` instance.

**Use case:** Enforcing that certain dependency versions or legacy JARs are never loaded — for example, preventing an old conflicting version of a library from sneaking in via transitive dependencies.

---

### 5. `OnceApplicationPreparedEventListener` (abstract class)

**What it does:** A base class for listeners that react to Spring Boot's `ApplicationPreparedEvent` (the moment just before the application context is refreshed) **but only once per application context**, never twice.

**Key concept:** It tracks a set of already-processed context IDs in a `ConcurrentSkipListSet`. If the same context fires the event again (e.g., in a test environment with context reuse), the logic is skipped.

Subclasses implement two methods:
- `isIgnored(...)` — return `true` to skip this context entirely
- `onApplicationEvent(...)` — the actual one-time logic

**Example:**

```java
public class MySetupListener extends OnceApplicationPreparedEventListener {
    @Override
    protected boolean isIgnored(SpringApplication app, String[] args, ConfigurableApplicationContext ctx) {
        return false; // always run
    }

    @Override
    protected void onApplicationEvent(SpringApplication app, String[] args, ConfigurableApplicationContext ctx) {
        System.out.println("Setup once for context: " + ctx.getId());
    }
}
```

---

### 6. `BindListener` (interface) + `ListenableConfigurationPropertiesBindHandlerAdvisor`

**What it does:** Adds **lifecycle callbacks around Spring Boot's `@ConfigurationProperties` binding**.

`BindListener` is an interface with five default-implemented callback hooks:

| Callback | When it fires |
|---|---|
| `onStart` | Binding begins |
| `onSuccess` | Binding completed successfully |
| `onCreate` | A new instance was created for an unbound result |
| `onFailure` | Binding failed |
| `onFinish` | Binding finished (success or unbound; not called on failure) |

`ListenableConfigurationPropertiesBindHandlerAdvisor` is a `ConfigurationPropertiesBindHandlerAdvisor` that discovers all `BindListener` Spring beans from the `BeanFactory` and chains them around every binding operation. You just declare your listener as a Spring `@Component` and it is automatically picked up.

**Example:**

```java
@Component
public class AuditBindListener implements BindListener {
    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext ctx, Object result) {
        System.out.println("Bound: " + name + " = " + result);
    }
}
```

---

### 7. `ConfigurationPropertiesBeanPropertyChangedEvent`

**What it does:** A Spring application **event** that is published whenever a property on a `@ConfigurationProperties` bean changes (e.g., when properties are refreshed at runtime).

It wraps:
- The bean instance
- The property name
- The old value
- The new value
- The `ConfigurationProperty` metadata (including the source file/origin of the change)

**Use case:** React to live configuration changes, e.g., update a connection pool size when `app.pool.size` changes at runtime without restarting.

---

### 8. `DefaultPropertiesPostProcessor` (interface) + `DefaultPropertiesApplicationListener`

**What it does:** Provides an extensible mechanism for loading and overriding Spring Boot's **"defaultProperties"** property source.

`DefaultPropertiesPostProcessor` is an SPI interface registered via `META-INF/spring.factories`. Implementations declare resource locations and can post-process the resulting property map.

`DefaultPropertiesApplicationListener` listens for `ApplicationEnvironmentPreparedEvent` and drives all registered `DefaultPropertiesPostProcessor` instances.

**Example:**

```java
// Register in META-INF/spring.factories:
// io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=com.example.MyProcessor

public class MyProcessor implements DefaultPropertiesPostProcessor {
    @Override
    public void initializeResources(Set<String> resources) {
        resources.add("classpath*:META-INF/my-defaults.properties");
    }

    @Override
    public void postProcess(Map<String, Object> defaults) {
        defaults.putIfAbsent("my.timeout", "5000");
    }
}
```

---

### 9. `ArtifactsCollisionException` + `ArtifactsCollisionFailureAnalyzer`

**What it does:** Provides a **friendly startup failure message** when duplicate or conflicting JARs are detected on the classpath.

`ArtifactsCollisionException` is a `RuntimeException` that carries the set of colliding artifact IDs.

`ArtifactsCollisionFailureAnalyzer` implements Spring Boot's `FailureAnalyzer` interface. When this exception is thrown, Spring Boot's failure analysis mechanism calls it to produce a structured error report, including the Maven command needed to investigate the conflict:

```
mvn dependency:tree -Dincludes=com.example:lib-a,com.example:lib-b
```

**Use case:** Clear, actionable error reporting when a production deployment includes conflicting library versions.

---

### 10. `SpringApplicationRunListenerAdapter` *(deprecated)*

**What it does:** Was a convenience base class implementing `SpringApplicationRunListener` with no-op methods for every lifecycle phase so subclasses only needed to override what they cared about. **Now deprecated** since Spring Boot's `ApplicationListener<SpringApplicationEvent>` is the preferred approach.

---

### 11. `SpringApplicationUtils`

**What it does:** A utility class with static helpers for the `SpringApplication`:

- `addDefaultPropertiesResource(...)` / `addDefaultPropertiesResources(...)` — register resource paths that will be loaded as default properties
- `getResourceLoader(SpringApplication)` — safely retrieves a `ResourceLoader`, falling back to `DefaultResourceLoader`
- `getLoggingLevel(...)` — reads the configured logging level from the environment
- `log(...)` — logs a detailed `SpringApplication` diagnostics summary at whatever level is configured

---

### 12. `ConditionEvaluationReportBuilder`

**What it does:** Caches and provides access to Spring Boot's `ConditionEvaluationReport` (the report of which auto-configurations matched and which did not) keyed by bean factory. Accessed by the report listener and exception reporter classes to build human-readable condition evaluation summaries.

---

## Module 2 — Actuator (`microsphere-spring-boot-actuator`)

### 13. `MonitoredThreadPoolTaskScheduler`

**What it does:** A subclass of Spring's `ThreadPoolTaskScheduler` that **automatically registers itself with Micrometer** (`MeterRegistry`) for metrics collection.

After all Spring beans are ready (`SmartInitializingSingleton`), it wraps the underlying executor with `ExecutorServiceMetrics.monitor(...)` so that task queue sizes, execution times, etc., appear in your metrics system (Prometheus, Datadog, etc.).

```java
// Automatically exposed as "actuatorTaskScheduler" bean if MeterRegistry is present
@Autowired
@Qualifier("actuatorTaskScheduler")
ThreadPoolTaskScheduler scheduler;
```

---

### 14. `ActuatorAutoConfiguration`

**What it does:** Auto-configures the `MonitoredThreadPoolTaskScheduler` bean (named `actuatorTaskScheduler`) only when a `MeterRegistry` bean is present. Pool size and thread name prefix are configurable via properties:

```properties
microsphere.spring.boot.actuator.task-scheduler.pool-size=2
microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix=my-actuator-
```

---

### 15. `ArtifactsEndpoint`

**What it does:** An Actuator endpoint (`/actuator/artifacts`) that returns a list of all **JAR artifacts** detected on the classpath via `ArtifactDetector`. Useful for auditing exactly which library versions are loaded.

---

### 16. `ConfigurationMetadataEndpoint`

**What it does:** An Actuator endpoint (`/actuator/configMetadata`) that exposes the **Spring Boot configuration metadata** generated by the `spring-boot-configuration-processor` annotation processor — the same data that IDEs use to provide auto-complete for `application.properties`. Returns groups and properties as JSON.

---

### 17. `ConfigurationPropertiesEndpoint`

**What it does:** An Actuator endpoint (`/actuator/configProperties`) that returns all **known configuration properties** from two sources: those declared via Java service loaders, and those from the configuration metadata repository.

---

### 18. `WebEndpoints`

**What it does:** An aggregate Actuator endpoint (`/actuator/webEndpoints`) that **invokes all other web endpoint read operations** (those with no parameters) in a single HTTP request and returns their combined results as a map. Think of it as a "health dashboard in one call."

---

### 19. `ConditionalOnConfigurationProcessorPresent`

**What it does:** A meta-annotation shortcut that checks whether `spring-boot-configuration-processor` is on the classpath. Configuration metadata endpoints are only registered when it is.

---

## Module 3 — Compatible (`microsphere-spring-boot-compatible`)

**What it does:** Contains backport copies of several Spring Boot APIs (`BootstrapContext`, `BootstrapRegistry`, `DefaultBootstrapContext`, `JacksonProperties`, `ServerProperties`, `MultipartProperties`, `MultipartConfigFactory`) so this library can compile against older Spring Boot versions. These files mirror the real Spring Boot classes and are used to ensure the library remains compatible across versions.

---

## How the Pieces Fit Together

```
Spring Boot Startup
       │
       ├── BannedArtifactClassLoadingListener  (very first — blocks banned JARs)
       │
       ├── DefaultPropertiesApplicationListener  (environment prepared — loads defaults)
       │
       ├── ConfigurableAutoConfigurationImportFilter  (auto-config filtering)
       │
       ├── @ConditionalOnPropertyPrefix  (conditions on beans)
       │
       ├── ListenableConfigurationPropertiesBindHandlerAdvisor  (@ConfigurationProperties binding)
       │        └── BindListener callbacks  (onStart / onSuccess / onFailure / onFinish)
       │                 └── ConfigurationPropertiesBeanPropertyChangedEvent  (property change events)
       │
       ├── OnceApplicationPreparedEventListener  (one-time context setup)
       │
       └── Actuator endpoints  (when app is running)
                ├── /actuator/artifacts
                ├── /actuator/configMetadata
                ├── /actuator/configProperties
                └── /actuator/webEndpoints  ← aggregates all other read endpoints
```

---

## Common Use Cases

| Goal | What to use |
|---|---|
| Activate a bean only when a family of properties exists | `@ConditionalOnPropertyPrefix("my.feature")` |
| Exclude an auto-configuration via property at deploy time | `microsphere.autoconfigure.exclude=...` |
| Listen to every `@ConfigurationProperties` bind event | Implement `BindListener` and declare as `@Component` |
| React when a config property changes at runtime | Listen for `ConfigurationPropertiesBeanPropertyChangedEvent` |
| Show which JARs are loaded in production | `GET /actuator/artifacts` |
| Get a single snapshot of all actuator data | `GET /actuator/webEndpoints` |
| Load default properties from multiple classpath files | Implement `DefaultPropertiesPostProcessor` |
| Prevent conflicting JARs from loading | Enable `-Dmicrosphere.spring.boot.banned-artifacts.enabled=true` |
| Write version-aware code | `SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_3_3)` |
