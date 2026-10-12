# Microsphere Spring Boot — User Guide

A detailed guide to every module, feature, and configuration property of **Microsphere Spring Boot**.

---

## 1. Version Information

### 1.1 Current release

| Item | Value |
|---|---|
| Group ID | `io.github.microsphere-projects` |
| Latest published release | **`0.2.34`** (Maven Central, published 2026-08-31) |
| Version under development (`main`) | `0.2.35-SNAPSHOT` |
| License | Apache License 2.0 |
| Source repository | https://github.com/microsphere-projects/microsphere-spring-boot |

### 1.2 Spring Boot compatibility

Microsphere Spring Boot is built and CI-tested against one Spring Boot patch version per minor line, selected by a Maven
profile. Any patch release inside those minor lines is expected to work; the pinned patch versions below are what the
project actually compiles and tests against.

**`main` branch — `0.2.x` line**

| Maven profile | Spring Boot version pinned | Status |
|---|---|---|
| `-Pspring-boot-3.0` | `3.0.13` | Supported |
| `-Pspring-boot-3.1` | `3.1.12` | Supported |
| `-Pspring-boot-3.2` | `3.2.12` | Supported |
| `-Pspring-boot-3.3` | `3.3.13` | Supported |
| `-Pspring-boot-3.4` | `3.4.13` | Supported |
| `-Pspring-boot-3.5` | `3.5.16` | **Default** (`activeByDefault`) |
| `-Pspring-boot-4.0` | `4.0.8` | Supported (requires the `compatible` shim, see §7) |
| `-Pspring-boot-4.1` | `4.1.1` | Supported (requires the `compatible` shim, see §7) |

**`1.x` branch — `0.1.x` line**

| Branch | Spring Boot range | Latest release |
|---|---|---|
| `1.x` | `2.0.x` – `2.7.x` | `0.1.34` |

> The `0.1.x` and `0.2.x` lines are maintained separately. There is no single artifact that runs on both Boot 2 and
> Boot 3/4 — pick the line that matches your Spring Boot generation.

### 1.3 Other requirements

| Requirement | Version |
|---|---|
| Java | 17 or later (build baseline `java.version=17`) |
| Maven | 3.9+ (or use the bundled `mvnw` / `mvnw.cmd` wrapper) |
| Upstream dependency | `microsphere-spring` `0.2.39` (imported as a BOM by the parent POM) |
| Optional, for metadata endpoints | `spring-boot-configuration-processor` on the classpath |
| Optional, for scheduler metrics | Micrometer `MeterRegistry` bean |

### 1.4 Which version should I use?

```xml
<properties>
    <!-- 0.2.x for Spring Boot 3.0–4.1, 0.1.x for Spring Boot 2.x -->
    <microsphere-spring-boot.version>0.2.34</microsphere-spring-boot.version>
</properties>
```

Always align the *minor* line with your Spring Boot generation, and prefer the latest patch of that line.

---

## 2. Module Overview

| Module | Artifact | Published? | Purpose |
|---|---|---|---|
| Parent POM | `microsphere-spring-boot-parent` | yes | Dependency management, Java 17 baseline, Spring Boot version profiles |
| BOM | `microsphere-spring-boot-dependencies` | yes | Manages versions of `core`, `webmvc`, `webflux`, `actuator`, `test` |
| Core | `microsphere-spring-boot-core` | yes | Default properties, auto-config filtering, bind listeners, diagnostics, conditions |
| Actuator | `microsphere-spring-boot-actuator` | yes | Four custom endpoints, monitored task scheduler, opinionated endpoint defaults |
| Web MVC | `microsphere-spring-boot-webmvc` | yes | Servlet-stack extensions (content caching, content negotiation, handler logging) |
| WebFlux | `microsphere-spring-boot-webflux` | yes | Reactive-stack extensions |
| Test | `microsphere-spring-boot-test` | yes | Base classes for auto-configuration tests |
| Compatible | `microsphere-spring-boot-compatible` | **no** (`maven-deploy-plugin` skip + central publishing skip) | Build-time shims for Boot 4.x, see §7 |

Every functional module (`actuator`, `webmvc`, `webflux`) depends on `core`, so adding any of them transitively brings
in the core features.

---

## 3. Getting Started

### 3.1 Import the BOM

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>${microsphere-spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 3.2 Add the modules you need

No `<version>` is required after the BOM is imported:

```xml

<dependencies>
    <!-- Configuration, conditions, diagnostics -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-core</artifactId>
    </dependency>

    <!-- Extra actuator endpoints + monitored scheduler -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-actuator</artifactId>
    </dependency>

    <!-- Servlet stack only (omit for reactive applications) -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-webmvc</artifactId>
    </dependency>

    <!-- Test support, scope test -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3.3 Verify the installation

```java
import io.microsphere.spring.boot.SpringBootVersion;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApplication {

    public static void main(String[] args) {
        System.out.println("Microsphere Spring Boot running on Spring Boot "
                + SpringBootVersion.CURRENT.getVersion());
        SpringApplication.run(MyApplication.class, args);
    }
}
```

With `core` on the classpath you get the default properties from §4.1 immediately — note that `server.shutdown`
becomes `graceful` without any configuration on your side.

---

## 4. Core (`microsphere-spring-boot-core`)

Base package: `io.microsphere.spring.boot`. Property-name prefix used across the project:
`microsphere.spring.boot.` (constant `io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX`).

### 4.1 Default properties

**What it does.** `io.microsphere.spring.boot.env.DefaultPropertiesApplicationListener` reacts to
`ApplicationEnvironmentPreparedEvent` (order = `LOWEST_PRECEDENCE - 1`) and loads every classpath resource matching

```
classpath*:/META-INF/config/default/*.*
```

into Spring Boot's `defaultProperties` property source — the *lowest* precedence source, so anything you set in
`application.properties`, an environment variable, or a command-line argument wins.

**Why it matters.** Libraries and modules can ship sensible defaults without forcing them on applications. Values from
different files are merged with `putIfAbsent`: the first definition of a key wins and later duplicates are logged as a
warning instead of overwriting each other.

The `core` module itself ships `META-INF/config/default/core.properties`:

```properties
## Graceful shutdown
server.shutdown = graceful
spring.lifecycle.timeout-per-shutdown-phase = 60s

## Disable noisy Spring MVC built-in filters
spring.mvc.hiddenmethod.filter.enabled = false
spring.mvc.formcontent.filter.enabled = false
spring.mvc.favicon.enabled = false

## Exclude a Boot auto-configuration by default (see §4.3)
microsphere.autoconfigure.exclude=\
org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration

## Microsphere Spring switches
microsphere.spring.event-publishing-bean.enabled = true
microsphere.spring.listenable-autowire-candidate-resolver.enabled = false
microsphere.spring.listenable-environment.enabled = false
```

**Ship your own defaults.** Put a file at `src/main/resources/META-INF/config/default/my-module.properties` in your own
library or application:

```properties
# config/default/my-module.properties equivalent, merged with all other default files
my-app.client.connect-timeout=5s
my-app.client.read-timeout=30s
```

**Extend the mechanism.** Implement `io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor` and register it in
`META-INF/spring.factories`:

```java
public class MyDefaultsPostProcessor implements DefaultPropertiesPostProcessor {

    @Override
    public void initializeResources(Set<String> resources) {
        // add extra locations to scan
        resources.add("classpath*:/META-INF/config/team-defaults/*.properties");
    }

    @Override
    public void postProcess(Map<String, Object> defaults) {
        // inspect / mutate the merged map before it is published
        defaults.putIfAbsent("my-app.feature.enabled", Boolean.TRUE);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE; // default
    }
}
```

```properties
# META-INF/spring.factories
io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=com.example.MyDefaultsPostProcessor
```

### 4.2 `SpringBootVersion` — version-aware code

**What it does.** Enum `io.microsphere.spring.boot.SpringBootVersion` models Spring Boot releases from
`SPRING_BOOT_3_0` up to `SPRING_BOOT_4_1_0`, plus a `CURRENT` constant resolved at class-loading time from
`org.springframework.boot.SpringBootVersion`.

The version string of a constant is derived from its own name: `SPRING_BOOT_` is stripped and `_` becomes `.`
(`SPRING_BOOT_3_2_5` → `3.2.5`).

**API.**

| Method | Alias | Meaning |
|---|---|---|
| `getVersion()` | — | semantic version string |
| `getMajor()` / `getMinor()` / `getPatch()` | — | numeric components |
| `gt(...)` | `isGreaterThan(...)` | strictly greater |
| `ge(...)` | `isGreaterOrEqual(...)` | greater or equal |
| `lt(...)` | `isLessThan(...)` | strictly less |
| `le(...)` | `isLessOrEqual(...)` | less or equal |
| `eq(...)` | `equals(...)` | same version |

**Example — guard a new API at runtime:**

```java
import io.microsphere.spring.boot.SpringBootVersion;

@Component
public class HealthContributorAdapter {

    public Object describe() {
        if (SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_3_3)) {
            return modernImplementation();
        }
        return legacyImplementation();
    }
}
```

**Example — log the detected version on startup:**

```java
SpringBootVersion current = SpringBootVersion.CURRENT;
log.info("Spring Boot {} (major={}, minor={}, patch={})",
        current.getVersion(), current.getMajor(), current.getMinor(), current.getPatch());
```

### 4.3 `ConfigurableAutoConfigurationImportFilter` — property-driven exclusions

**What it does.** Registered via `META-INF/spring.factories` as a Spring Boot
`AutoConfigurationImportFilter`, it removes auto-configuration classes named in the property
`microsphere.autoconfigure.exclude` **before** any bean is created.

**Difference from `spring.autoconfigure.exclude`:** the standard property *replaces* — the highest-precedence property
source wins and all other exclusions are lost. The Microsphere property **merges** values from every source, so
independent teams and libraries can each contribute exclusions without stepping on one another.

Both comma-separated and indexed-array forms are read (from the `Environment` directly and through `Binder`):

```properties
# comma-separated
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration

# or indexed array
microsphere.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
microsphere.autoconfigure.exclude[1]=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

**Programmatically (e.g. from an `EnvironmentPostProcessor` or a test harness):**

```java
import io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter;

ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(
        environment, "com.example.FooAutoConfiguration");

Set<String> excluded = ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment);
```

**When to use which:**

| Goal | Use |
|---|---|
| One authoritative exclusion list owned by the application | `spring.autoconfigure.exclude` |
| Several modules/teams excluding different auto-configurations independently | `microsphere.autoconfigure.exclude` |
| Excluding from a reusable library's default-properties file | `microsphere.autoconfigure.exclude` |

### 4.4 `@ConditionalOnPropertyPrefix`

**What it does.** Annotation `io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix` (backed by
`OnPropertyPrefixCondition`, applicable to types and methods) registers a bean only when the `Environment` contains at
least one property whose *name starts with* one of the given prefixes.

Spring Boot's own `@ConditionalOnProperty` needs an exact property name; this condition only needs the family.

```java
import io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix;

@ConditionalOnPropertyPrefix("myapp.datasource")
@Configuration
public class DataSourceConfig {
    // loaded only if some "myapp.datasource.*" property is present
}
```

Multiple prefixes are OR-ed — a match on any one activates the bean:

```java
@ConditionalOnPropertyPrefix({"myapp.redis", "myapp.cache"})
@Bean
public CacheManager cacheManager() {
    return new RedisCacheManager();
}
```

### 4.5 `BindListener` — observe `@ConfigurationProperties` binding

**What it does.** Interface `io.microsphere.spring.boot.context.properties.bind.BindListener` exposes five lifecycle
callbacks, all with `default` (no-op) implementations so you override only what you need:

| Callback | Signature | Fires when |
|---|---|---|
| `onStart` | `<T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)` | binding begins |
| `onSuccess` | `void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | binding succeeded |
| `onCreate` | `void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | a new instance was created for an unbound result |
| `onFailure` | `void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)` | binding failed |
| `onFinish` | `void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | binding finished (not called on failure) |

`ListenableConfigurationPropertiesBindHandlerAdvisor` (a Boot
`ConfigurationPropertiesBindHandlerAdvisor`, wired by `io.microsphere.spring.boot.context.autoconfigure.ConfigurationPropertiesAutoConfiguration`)
discovers every `BindListener` bean and chains them around all binding operations. Declaring a listener as a bean is
all that is required.

```java
import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.stereotype.Component;

@Component
public class AuditBindListener implements BindListener {

    private static final Logger log = LoggerFactory.getLogger(AuditBindListener.class);

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

**Use cases:** auditing which configuration actually reached the application, detecting typos in property names,
measuring binding time, or asserting that sensitive keys never appear in binding results.

### 4.6 `ConfigurationPropertiesBeanPropertyChangedEvent`

**What it does.** Event `io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent<T>`
(extends `BeanPropertyChangedEvent`) is published to the `ApplicationContext` whenever a property of a
`@ConfigurationProperties` bean changes value — typically during a runtime refresh.

The event exposes: `getOldValue()`, `getNewValue()` and `getSource()` (the bean instance) inherited from
`BeanPropertyChangedEvent`, `getPropertyType()` for the changed property's `ResolvableType`, and
`getConfigurationProperty()` for the `ConfigurationProperty` — which carries the *origin* (file and line) of the value.

```java
import io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PoolResizeListener {

    @EventListener
    public void onChange(ConfigurationPropertiesBeanPropertyChangedEvent<?> event) {
        // "app.pool.size" is the bound property name, with origin info attached
        if (!"app.pool.size".equals(event.getConfigurationProperty().getName().toString())) {
            return;
        }
        Integer oldValue = (Integer) event.getOldValue();
        Integer newValue = (Integer) event.getNewValue();
        log.info("Pool resized {} -> {} (origin: {})",
                oldValue, newValue, event.getConfigurationProperty().getOrigin());
        // resize the pool live, no restart needed
    }
}
```

The event is published by `EventPublishingConfigurationPropertiesBeanPropertyChangedListener`, which snapshots each
`@ConfigurationProperties` bean after the initial binding and compares on subsequent re-binding.

**Note:** event publishing depends on the `microsphere.spring.event-publishing-bean.enabled=true` default shipped by
`core.properties`. Set it to `false` if you want binding listeners but no application events.

### 4.7 Classpath diagnostics

Three cooperating pieces turn classpath problems into readable startup failures instead of mysterious runtime errors.

#### 4.7.1 Banned artifacts

`io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener` is a `SpringApplicationRunListener` that
runs at `ApplicationStartingEvent` — the earliest possible moment — and blocks the class loading of explicitly banned
JAR artifacts.

It is opt-in through a **JVM system property** (not an `application.properties` entry, because it must be readable
before the `Environment` exists):

```bash
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar app.jar
```

```properties
# constant: BannedArtifactClassLoadingListener.BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME
microsphere.spring.boot.banned-artifacts.enabled=true
```

or from `main` before `SpringApplication.run`:

```java
System.setProperty("microsphere.spring.boot.banned-artifacts.enabled", "true");
```

The banning rules themselves come from the Microsphere artifact-banning configuration; the listener guarantees the
logic executes exactly once per `SpringApplication`.

**Use case:** prevent an old, conflicting library version from leaking in through transitive dependencies.

#### 4.7.2 Artifact collision detection

`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener` scans the classpath for duplicate versions
of the same artifact and throws
`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionException` (carrying the colliding artifact IDs). Enabled via
an environment property, default `false`:

```properties
microsphere.spring.boot.artifacts-collision.enabled=true
```

`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionFailureAnalyzer` (registered as a Boot `FailureAnalyzer`)
then renders a structured report including the exact Maven command to investigate:

```
mvn dependency:tree -Dincludes=com.example:lib-a,com.example:lib-b
```

#### 4.7.3 Condition evaluation report

Registered via `META-INF/spring.factories`: `ConditionEvaluationReportInitializer`, `ConditionEvaluationReportListener`,
`ConditionEvaluationSpringBootExceptionReporter`, and `FailureReportSpringApplicationRunListener`. Together they capture
Spring Boot's `ConditionEvaluationReport` and print a human-readable summary of which auto-configurations matched — and
crucially *why* the others did not — when startup fails.

Scope it with:

```properties
# only report conditions for these base packages (default: io.microsphere)
microsphere.spring.boot.conditions.report.base-packages=com.mycompany,io.microsphere
```

Diagnostic logging level used by these listeners:

```properties
microsphere.spring.boot.logging.level=TRACE   # default
```

### 4.8 `OnceApplicationPreparedEventListener`

**What it does.** Abstract base for listeners of `ApplicationPreparedEvent` (fired just before the context is
refreshed) that must run **at most once per application context** — useful under context reuse in test suites.
Processed context IDs are tracked in a `ConcurrentSkipListSet`.

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class MySetupListener extends OnceApplicationPreparedEventListener {

    @Override
    protected boolean isIgnored(SpringApplication application, String[] args,
                                ConfigurableApplicationContext context) {
        return false; // false = run for this context
    }

    @Override
    protected void onApplicationEvent(SpringApplication application, String[] args,
                                      ConfigurableApplicationContext context) {
        // one-time setup, guaranteed not to repeat for the same context id
    }
}
```

Register it in `META-INF/spring.factories` under `org.springframework.context.ApplicationListener`.

### 4.9 Utilities

`io.microsphere.spring.boot.util.SpringApplicationUtils`:

| Method | Purpose |
|---|---|
| `addDefaultPropertiesResource(...)` / `addDefaultPropertiesResources(...)` | register extra resource paths loaded as default properties |
| `getResourceLoader(SpringApplication)` | safe `ResourceLoader` lookup with `DefaultResourceLoader` fallback |
| `getLoggingLevel(Environment)` | read `microsphere.spring.boot.logging.level` |
| `log(...)` | emit a `SpringApplication` diagnostics summary at the configured level |

```java
SpringApplicationUtils.addDefaultPropertiesResources(
        application, "classpath*:/META-INF/config/team-defaults/*.properties");
```

> **Deprecated:** `SpringApplicationRunListenerAdapter` — use `ApplicationListener<SpringApplicationEvent>` instead. It
> remains only for source compatibility and should not be used in new code.

---

## 5. Actuator (`microsphere-spring-boot-actuator`)

Package: `io.microsphere.spring.boot.actuate`. Two auto-configurations are registered in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
`ActuatorAutoConfiguration` and `ActuatorEndpointsAutoConfiguration`.

### 5.1 Custom endpoints

| Endpoint id | Class | Default web path | Requires |
|---|---|---|---|
| `artifacts` | `endpoint.ArtifactsEndpoint` (`@Endpoint`) | `/actuator/microsphere/artifacts` | — |
| `webEndpoints` | `endpoint.WebEndpoints` (`@WebEndpoint`) | `/actuator/microsphere/web/endpoints` | a web application |
| `configMetadata` | `endpoint.ConfigurationMetadataEndpoint` (`@Endpoint`) | `/actuator/microsphere/config/metadata` | `spring-boot-configuration-processor` on classpath |
| `configProperties` | `endpoint.ConfigurationPropertiesEndpoint` (`@Endpoint`) | `/actuator/microsphere/config/properties` | — |

Paths come from `management.endpoints.web.path-mapping.*` defaults, so they move if you change your management base
path (`management.endpoints.web.base-path`).

**What each returns:**

- `artifacts` — `List<Artifact>` of every JAR detected on the classpath by `ArtifactDetector` (groupId, artifactId,
  version, location). Use it to audit exactly what is deployed.
- `webEndpoints` — invokes every parameter-less read operation of all registered web endpoints and returns the combined
  results as one map. A single-call "runtime snapshot".
- `configMetadata` — the `spring-configuration-metadata.json` content (groups + properties) that IDEs use for
  completion; generated by the annotation processor.
- `configProperties` — all known configuration properties, from both service-loader declarations and the configuration
  metadata repository.

**Enable/disable individually:**

```properties
management.endpoint.artifacts.enabled=true
management.endpoint.webEndpoints.enabled=true
management.endpoint.configMetadata.enabled=true
management.endpoint.configProperties.enabled=true

# optional: re-map a path
management.endpoints.web.path-mapping.artifacts=mycompany/jars
```

**Example response — `/actuator/microsphere/artifacts`** (each element is an `io.microsphere.classloading.Artifact`;
`getArtifactId()` is available in code, alongside the rest of the Maven coordinates):

```json
[
  {
    "groupId": "io.github.microsphere-projects",
    "artifactId": "microsphere-spring-boot-core",
    "version": "0.2.34"
  }
]
```

The read operation is `getArtifactMetaInfoList()`, backed by `ArtifactDetector.detect(false)` over the application
`ClassLoader` passed into the endpoint constructor.

### 5.2 Opinionated endpoint defaults

`META-INF/config/default/endpoints.properties` ships as a default-properties file (§4.1), so **your** configuration
always overrides it:

- `management.endpoints.enabled-by-default = false` — nothing is exposed unless chosen deliberately.
- Enabled: `env`, `health`, `info`, `loggers`, `metrics`, `mappings`, `jolokia`, `prometheus`, plus the four Microsphere
  endpoints.
- Disabled: `auditevents`, `beans`, `caches`, `conditions`, `configprops`, `flyway`, `httptrace`, `integrationgraph`,
  `liquibase`, `scheduledtasks`, `sessions`, `shutdown`, `threaddump`, `heapdump`, `logfile`.
- Web exposure `include = *`, JMX exposure `exclude = *`.
- `management.endpoint.health.show-details = always`, `show-components = always`.
- Cache TTLs are defined once and referenced:

```properties
microsphere.cache.no-time-to-live = 0s
microsphere.cache.short-time-to-live = 15s
microsphere.cache.middle-time-to-live = 1m
microsphere.cache.long-time-to-live = 1h
microsphere.cache.long-long-time-to-live = 1d

management.endpoint.env.cache.time-to-live = ${microsphere.cache.middle-time-to-live}
management.endpoint.health.cache.time-to-live = ${microsphere.cache.no-time-to-live}
management.endpoint.artifacts.cache.time-to-live = ${microsphere.cache.long-long-time-to-live}
```

Override a TTL in your own `application.properties`:

```properties
management.endpoint.env.cache.time-to-live=0s
```

### 5.3 `MonitoredThreadPoolTaskScheduler`

**What it does.** `io.microsphere.spring.boot.actuate.MonitoredThreadPoolTaskScheduler` extends Spring's
`ThreadPoolTaskScheduler` and instruments its `ScheduledExecutorService` with Micrometer
(`ExecutorServiceMetrics.monitor(...)`). The executor is captured in `createExecutor(...)` and swapped for the
monitored delegate in `afterSingletonsInstantiated()`, once the `MeterRegistry` exists.

**Auto-configuration.** `ActuatorAutoConfiguration` (`@AutoConfigureOrder(LOWEST_PRECEDENCE)`) defines bean
`actuatorTaskScheduler` only when a `MeterRegistry` bean is present
(`@ConditionalOnBean(type = "io.micrometer.core.instrument.MeterRegistry")`), with `destroyMethod = "shutdown"`.

```properties
microsphere.spring.boot.actuator.task-scheduler.pool-size=2
microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix=my-actuator-
```

Defaults: pool size `1`, thread name prefix `microsphere-spring-boot-actuator-task-`, daemon threads.

**Inject it:**

```java
@Autowired
@Qualifier("actuatorTaskScheduler")
private ThreadPoolTaskScheduler scheduler;
```

**Or declare your own monitored scheduler** (the constructor is no-arg — the `MeterRegistry` is resolved from the
`ApplicationContext`, so the bean must be managed by Spring):

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
        return scheduler;
    }
}
```

Metrics then appear under the standard `executor.*` Micrometer names (active, queued, completed, duration), tagged with
the bean name — visible via `/actuator/metrics/executor.active`.

### 5.4 Conditions

| Annotation | Passes when |
|---|---|
| `ConditionalOnActuatorEndpointPresent` | `...endpoint.annotation.Endpoint` is on the classpath |
| `ConditionalOnConfigurationProcessorPresent` | `spring-boot-configuration-processor` is on the classpath |

All endpoint beans are additionally guarded by `@ConditionalOnMissingBean` and `@ConditionalOnAvailableEndpoint`, so
your own beans always win.

---

## 6. Web MVC / WebFlux / Test modules

### 6.1 `microsphere-spring-boot-webmvc`

`io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` is guarded by `@ConditionalOnWebMvcAvailable`
and applies `@EnableWebMvcExtension(registerHandlerInterceptors = true, reversedProxyHandlerMapping = true)`. Its
`@AutoConfigureAfter` lists both the Boot 2.x/3.x and the Boot 4.x class names for Spring Boot's own Web MVC
auto-configuration, which is what keeps one source tree working across the version range.

| Bean | Enabled by property | Default |
|---|---|---|
| `contentCachingFilter` (`ContentCachingFilter`) | `microsphere.spring.boot.webmvc.filter.enabled` | on (`matchIfMissing = true`) |
| `contentNegotiationManagerWebMvcConfigurer` | `microsphere.spring.webmvc.content-negotiation.enabled` | on |
| `exclusiveViewResolverApplicationListener` | view-resolver bean-name property (see `io.microsphere.spring.webmvc` constants) | off unless set |
| `LoggingConfiguration` (handler-interceptor / argument-resolver logging) | `microsphere.spring.boot.webmvc.logging.enabled` | on |

Whole-module switch: `microsphere.spring.boot.webmvc.enabled`.

```properties
microsphere.spring.boot.webmvc.enabled=true
microsphere.spring.boot.webmvc.filter.enabled=false
microsphere.spring.boot.webmvc.logging.enabled=true
microsphere.spring.webmvc.content-negotiation.enabled=false
```

### 6.2 `microsphere-spring-boot-webflux`

`io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration`, guarded by `@ConditionalOnWebFluxAvailable`
and applying `@EnableWebFluxExtension(reversedProxyHandlerMapping = true)`. Switch:

```properties
microsphere.spring.boot.webflux.enabled=true
```

### 6.3 `microsphere-spring-boot-test`

`io.microsphere.spring.boot.test.AbstractAutoConfigurationTest<A, R extends AbstractApplicationContextRunner>` turns an
auto-configuration class into a parameterized context runner and supplies three inherited tests:
`testAutoConfiguredClasses`, `testOnGlobalDisabledProperty`, `testOnGlobalMissingClass`.

Subclasses must be annotated with `@SpringBootTest`; its `properties` and `classes` attributes are reused to configure
the runner, and the global-disable/missing-class scenarios are derived from it.

```java
import io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration;
import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ActuatorAutoConfiguration.class,
        properties = "microsphere.spring.boot.actuator.task-scheduler.pool-size=3")
class ActuatorAutoConfigurationTest extends AutoConfigurationTest<ActuatorAutoConfiguration> {
}
```

Available base classes:

| Class | Runner | Use for |
|---|---|---|
| `AutoConfigurationTest<A>` | `ApplicationContextRunner` | non-web auto-configurations |
| `WebAutoConfigurationTest<A>` | servlet `WebApplicationContextRunner` | Spring MVC auto-configurations |
| `ReactiveWebAutoConfigurationTest<A>` | reactive runner | WebFlux auto-configurations |

---

## 7. Compatible (`microsphere-spring-boot-compatible`) — how cross-version support works

**What it does.** Backport copies of Spring Boot APIs that moved or disappeared between releases, so the same source
tree compiles against Boot 3.0 through Boot 4.1:

| Shim class (package `org.springframework.boot…`) | Originally introduced |
|---|---|
| `.BootstrapContext`, `.BootstrapRegistry`, `.ConfigurableBootstrapContext`, `.DefaultBootstrapContext`, `.BootstrapContextClosedEvent` | Boot 2.4.0 |
| `.web.servlet.MultipartConfigFactory` | Boot 1.4.0 |
| `.autoconfigure.web.ServerProperties` | — |
| `.autoconfigure.web.servlet.MultipartProperties` | — |
| `.autoconfigure.jackson.JacksonProperties` | — |

**Important facts:**

- This module is **not published to Maven Central** (`maven-deploy-plugin` skip plus `central-publishing-maven-plugin`
  skipPublishing) and is **not** managed by the BOM. You never depend on it directly.
- It is attached to `core` only by the `spring-boot-4.0` and `spring-boot-4.1` Maven profiles, when those APIs are no
  longer provided by Spring Boot itself. The version boundary is expressed by the Maven profile, not by runtime
  version checks.
- Consequence for you: nothing to do. It matters only when building from source against a Boot 4.x profile.

---

## 8. Startup sequence — how the pieces fit

```
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar app.jar
        │
        ├─ ApplicationStartingEvent
        │     └─ BannedArtifactClassLoadingListener      (blocks banned JARs, once per SpringApplication)
        │
        ├─ Environment prepared
        │     ├─ DefaultPropertiesApplicationListener    (loads META-INF/config/default/*.* as defaultProperties)
        │     ├─ ConditionEvaluationReportInitializer
        │     └─ OriginTrackedConfigurationPropertyInitializer
        │
        ├─ Auto-configuration import
        │     ├─ ConfigurableAutoConfigurationImportFilter  (microsphere.autoconfigure.exclude — merged)
        │     └─ @ConditionalOnPropertyPrefix / Boot conditions on each bean
        │
        ├─ @ConfigurationProperties binding
        │     └─ ListenableConfigurationPropertiesBindHandlerAdvisor
        │           ├─ BindListener.onStart/onSuccess/onCreate/onFailure/onFinish
        │           └─ ConfigurationPropertiesBeanPropertyChangedEvent (published to the context)
        │
        ├─ ApplicationPreparedEvent
        │     └─ OnceApplicationPreparedEventListener subclasses (once per context id)
        │
        ├─ On failure
        │     ├─ ArtifactsCollisionFailureAnalyzer        ("mvn dependency:tree -Dincludes=...")
        │     └─ ConditionEvaluationSpringBootExceptionReporter
        │
        └─ Running
              ├─ /actuator/microsphere/artifacts
              ├─ /actuator/microsphere/config/metadata
              ├─ /actuator/microsphere/config/properties
              ├─ /actuator/microsphere/web/endpoints       (aggregates all parameter-less read operations)
              └─ actuatorTaskScheduler                     (Micrometer executor.* metrics)
```

---

## 9. Recipes

| Goal | How |
|---|---|
| Activate a bean only when a property family exists | `@ConditionalOnPropertyPrefix("my.feature")` |
| Let several libraries each exclude auto-configurations | `microsphere.autoconfigure.exclude` in each module's `META-INF/config/default/*.properties` |
| Audit every bound property at startup | declare a `BindListener` `@Component` |
| React to a live config change | `@EventListener` on `ConfigurationPropertiesBeanPropertyChangedEvent` |
| See which JAR versions are deployed | `GET /actuator/microsphere/artifacts` |
| One-call runtime snapshot | `GET /actuator/microsphere/web/endpoints` |
| IDE-quality property docs at runtime | add `spring-boot-configuration-processor`, then `GET /actuator/microsphere/config/metadata` |
| Metrics for scheduled tasks | rely on `actuatorTaskScheduler`, or expose a `MonitoredThreadPoolTaskScheduler` bean |
| Fail fast on duplicate JARs | `microsphere.spring.boot.artifacts-collision.enabled=true` |
| Block a known-bad artifact | `-Dmicrosphere.spring.boot.banned-artifacts.enabled=true` |
| Branch on Spring Boot version | `SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_3_3)` |
| Debug why an auto-configuration is off | enable the condition report, set `microsphere.spring.boot.conditions.report.base-packages` |

---

## 10. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `server.shutdown` became `graceful` unexpectedly | Expected default from `core.properties`; set `server.shutdown=immediate` in your own config to override |
| `shutdown` endpoint not reachable | Actuator defaults set `enabled-by-default=false`; add `management.endpoint.shutdown.enabled=true` |
| `/actuator/microsphere/config/metadata` returns 404 | `spring-boot-configuration-processor` is absent — the endpoint is conditional on it |
| Scheduler metrics missing | No `MeterRegistry` bean → `actuatorTaskScheduler` is not created; add Micrometer/`spring-boot-starter-actuator` + a registry |
| Exclusion "lost" compared to another module | You used `spring.autoconfigure.exclude` (replace semantics); switch to `microsphere.autoconfigure.exclude` (merge) |
| Classpath collision at startup | Read the `FailureAnalyzer` output and run the suggested `mvn dependency:tree -Dincludes=...` |
| Code does not compile against Boot 4.x | Build with `-Pspring-boot-4.0` or `-Pspring-boot-4.1` so the `compatible` shims are attached |
| `BindListener` never called | The listener is not a Spring bean, or `ConfigurationPropertiesAutoConfiguration` was excluded |
| Wrong Microsphere line for your Boot | `0.2.x` → Boot 3.0–4.1; `0.1.x` → Boot 2.x. No cross-generation artifact exists |

---

## 11. Building from source

```bash
git clone https://github.com/microsphere-projects/microsphere-spring-boot.git
cd microsphere-spring-boot

# default profile = Spring Boot 3.5.16
./mvnw verify                      # Linux/macOS
mvnw.cmd verify                    # Windows

# build and test against another Spring Boot line
./mvnw verify -Pspring-boot-3.0
./mvnw verify -Pspring-boot-4.1    # pulls in the compatible shim module

# install locally, skipping tests
./mvnw install -DskipTests
```

Requires JDK 17+. Contributing guidelines and the change log live in
[README.md](./README.md) and [release-notes.md](./release-notes.md).

---

## 12. Reference: property index

| Property | Where it is read | Default | Effect |
|---|---|---|---|
| `microsphere.autoconfigure.exclude` | `ConfigurableAutoConfigurationImportFilter` | set by `core.properties` | merged auto-configuration exclusions |
| `META-INF/config/default/*.*` | `DefaultPropertiesApplicationListener` | loaded | classpath pattern for default-properties files |
| `microsphere.spring.boot.banned-artifacts.enabled` | `BannedArtifactClassLoadingListener` | off | **JVM system property**; enables artifact banning |
| `microsphere.spring.boot.artifacts-collision.enabled` | `ArtifactsCollisionDiagnosisListener` | `false` | fails startup on duplicate artifacts |
| `microsphere.spring.boot.conditions.report.base-packages` | condition reporting | `io.microsphere` | packages covered by the condition report |
| `microsphere.spring.boot.logging.level` | `SpringApplicationUtils` | `TRACE` | level of diagnostic summaries |
| `microsphere.spring.event-publishing-bean.enabled` | Microsphere Spring | `true` | publishes `...BeanPropertyChangedEvent` |
| `microsphere.spring.boot.actuator.task-scheduler.pool-size` | `ActuatorAutoConfiguration` | `1` | scheduler pool size |
| `microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix` | `ActuatorAutoConfiguration` | `microsphere-spring-boot-actuator-task-` | thread names |
| `management.endpoint.{artifacts,webEndpoints,configMetadata,configProperties}.enabled` | `endpoints.properties` | `true` | the four custom endpoints |
| `management.endpoints.web.path-mapping.*` | `endpoints.properties` | `microsphere/...` | custom endpoint paths |
| `microsphere.cache.{no,short,middle,long,long-long}-time-to-live` | `endpoints.properties` | `0s/15s/1m/1h/1d` | actuator cache TTL presets |
| `microsphere.spring.boot.webmvc.enabled` | `WebMvcAutoConfiguration` | on | whole webmvc module switch |
| `microsphere.spring.boot.webmvc.filter.enabled` | `WebMvcAutoConfiguration` | on (`matchIfMissing`) | `ContentCachingFilter` |
| `microsphere.spring.boot.webmvc.logging.enabled` | `WebMvcAutoConfiguration` | on (`matchIfMissing`) | handler/argument-resolver logging |
| `microsphere.spring.webmvc.content-negotiation.enabled` | `WebMvcAutoConfiguration` | on (`matchIfMissing`) | content-negotiation configurer |
| `microsphere.spring.boot.webflux.enabled` | `WebFluxAutoConfiguration` | on | whole webflux module switch |

---

## 13. Getting Help

| Resource | Link |
|---|---|
| Project README | [README.md](./README.md) |
| Release notes | [release-notes.md](./release-notes.md) |
| Issues | https://github.com/microsphere-projects/microsphere-spring-boot/issues |
| Generated docs (DeepWiki) | https://deepwiki.com/microsphere-projects/microsphere-spring-boot |
| Generated docs (ZRead) | https://zread.ai/microsphere-projects/microsphere-spring-boot |

When reporting an issue, include: your Spring Boot version, the Microsphere Spring Boot version, the Maven profile used,
the modules you depend on, and a minimal reproducer.

Microsphere Spring Boot is released under the [Apache License 2.0](./LICENSE).
