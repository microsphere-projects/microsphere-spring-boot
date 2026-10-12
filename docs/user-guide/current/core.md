# Core Module

> [中文](./zh/core.md) · [Documentation index](../README.md)
>
> **Branch:** `release` · **Version line:** `0.2.x` · **Artifact:** `microsphere-spring-boot-core`
> **Spring Boot:** `3.0.13` – `4.1.1` · **Java:** 17+

Contents:
[1 Overview](#1-module-overview) ·
[2 Version information](#2-version-information) ·
[3 Default properties](#3-default-properties) ·
[4 SpringBootVersion](#4-springbootversion-version-aware-api) ·
[5 Auto-configuration exclusion](#5-configurableautoconfigurationimportfilter-property-driven-exclusion) ·
[6 Prefix condition](#6-conditionalonpropertyprefix-prefix-condition) ·
[7 BindListener](#7-bindlistener-binding-callbacks) ·
[8 Property change events](#8-configurationpropertiesbeanpropertychangedevent-property-change-events) ·
[9 Classpath diagnostics](#9-classpath-diagnostics) ·
[10 One-time listener](#10-onceapplicationpreparedeventlistener-one-time-listener) ·
[11 Utilities](#11-springapplicationutils-utility-class) ·
[12 Extension points](#12-extension-point-registration) ·
[13 Property reference](#13-property-reference) ·
[14 FAQ](#14-faq)

---

## 1. Module Overview

`core` is the base module of the `0.2.x` line and owns two themes: **configuration** and **diagnostics**. It lets
libraries and applications carry overridable defaults, lets auto-configuration exclusions be merged from many sources,
turns `@ConfigurationProperties` binding into an observable and reactive event stream, and converts classpath problems
into human-readable startup failure reports at the earliest possible moment. `actuator`, `webmvc` and `webflux` all
depend on it, so introducing any of them activates these capabilities.

Base package: `io.microsphere.spring.boot`. Sub-packages that users interact with directly:

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

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-core` |
| Latest published | `0.2.34` (2026-08-31) |
| Branch in development | `0.2.35-SNAPSHOT` |
| Spring Boot | profiles pin `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16 (default) / 4.0.8 / 4.1.1` |
| Java | `17` (root POM `<java.version>`); CI validates JDK `17 / 21 / 25` |
| Upstream | `microsphere-spring:0.2.39` (BOM import) |
| Boot 4.x attached dependency | `microsphere-spring-boot-compatible`, added by this module's POM only under `-Pspring-boot-4.0` / `-Pspring-boot-4.1` |

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-core</artifactId>
    <!-- version from the microsphere-spring-boot-dependencies BOM -->
</dependency>
```

---

## 3. Default Properties

`DefaultPropertiesApplicationListener` reacts to `ApplicationEnvironmentPreparedEvent`
(`DEFAULT_ORDER = LOWEST_PRECEDENCE - 1`), reads every classpath resource matching
`classpath*:/META-INF/config/default/*.*`, and publishes them into Spring Boot's `defaultProperties` source — the
**lowest**-precedence source, so `application.properties`, environment variables and command-line arguments all win.
Merging uses `putIfAbsent`: the first definition of a key survives, later duplicates only log a warning instead of
overwriting each other. Loading is delegated to `PropertySourceLoaders`, so both `.properties` and `.yml` work (that is
why the pattern is `*.*`).

The pattern constant lives on the built-in implementation:

```java
// io.microsphere.spring.boot.env.SpringApplicationDefaultPropertiesPostProcessor
public static final String DEFAULT_PROPERTIES_RESOURCES_PATTERN = "classpath*:/META-INF/config/default/*.*";
```

### 3.1 Shipped Defaults

`src/main/resources/META-INF/config/default/core.properties`:

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

To go back to Boot's native behaviour, just override it explicitly, e.g. `server.shutdown=immediate`.

### 3.2 Ship Defaults from Your Own Library

Put `src/main/resources/META-INF/config/default/my-module.properties` into your library's jar; at runtime it is merged
with the default files above:

```properties
# META-INF/config/default/my-module.properties
my-app.client.connect-timeout = 5s
my-app.client.read-timeout = 30s
microsphere.autoconfigure.exclude = org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

### 3.3 Extend through the SPI

```java
package com.example;

import io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor;

import java.util.Map;
import java.util.Set;

public class TeamDefaultsPostProcessor implements DefaultPropertiesPostProcessor {

    @Override
    public void initializeResources(Set<String> resources) {
        // add extra locations to scan
        resources.add("classpath*:/META-INF/config/team-defaults/*.properties");
    }

    @Override
    public void postProcess(Map<String, Object> defaults) {
        // inspect or top up before publication
        defaults.putIfAbsent("my-app.feature.enabled", Boolean.TRUE);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE; // interface default
    }
}
```

```properties
# META-INF/spring.factories
io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=com.example.TeamDefaultsPostProcessor
```

**Interface signature**: `DefaultPropertiesPostProcessor extends Ordered` declares
`void initializeResources(Set<String>)`, a default `postProcess(Map<String, Object>)` and a default `getOrder()`.

---

## 4. SpringBootVersion Version-Aware API

The enum `io.microsphere.spring.boot.SpringBootVersion` models Spring Boot releases as constants — on this branch
`SPRING_BOOT_3_0` … `SPRING_BOOT_4_1_0` — plus `CURRENT`, resolved at class-load time from
`org.springframework.boot.SpringBootVersion`. A constant's version is derived from its own name: strip `SPRING_BOOT_`
(exactly 12 characters, `name.substring(12)`) and replace `_` with `.`, so `SPRING_BOOT_3_2_5` is `3.2.5`.

| Method | Alias | Meaning |
|---|---|---|
| `getVersion()` | — | version string |
| `getMajor()` / `getMinor()` / `getPatch()` | — | numeric parts |
| `gt(SpringBootVersion)` | `isGreaterThan` | greater than |
| `ge(SpringBootVersion)` | `isGreaterOrEqual` | at least |
| `lt(SpringBootVersion)` | `isLessThan` | less than |
| `le(SpringBootVersion)` | `isLessOrEqual` | at most |
| `eq(SpringBootVersion)` | `equals` | equal |
| `resolveVersion(String)` | — | static; name or version string -> version |

```java
import io.microsphere.spring.boot.SpringBootVersion;

// runtime branch on the running release
if (SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_3_3)) {
    return modernImplementation();
}
return legacyImplementation();
```

```java
// log the detected version
SpringBootVersion current = SpringBootVersion.CURRENT;
log.info("Spring Boot {}.{}, raw={}", current.getMajor(), current.getMinor(), current.getVersion());
```

> **Cross-branch note.** The enum differs per line: `0.2.x` (this branch) models Boot 3/4, while `0.1.x` models Boot
> `2.0`–`2.7.18`. Constants such as `SPRING_BOOT_3_3` therefore exist only on `0.2.x`.

---

## 5. ConfigurableAutoConfigurationImportFilter Property-Driven Exclusion

Registered through `spring.factories` as a Spring Boot `AutoConfigurationImportFilter`,
`io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter` drops auto-configuration classes
named by `microsphere.autoconfigure.exclude` (constant `AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME`) before any bean is
created; `getOrder()` is `HIGHEST_PRECEDENCE + 99`. Its one crucial — and crucially different — semantic compared with
standard `spring.autoconfigure.exclude` is this: the standard property **replaces** (the highest-precedence source wins
alone), while this property **merges** every source. Independent teams and libraries can therefore each contribute
exclusions without erasing the others'.

```properties
# comma-separated
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration

# or indexed array (also recognised)
microsphere.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
microsphere.autoconfigure.exclude[1]=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

**Programmatic API** — four public static methods:

```java
import io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter;

// add one
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(
        environment, "com.example.FooAutoConfiguration");

// add several
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
        environment, "com.example.FooAutoConfiguration", "com.example.BarAutoConfiguration");
ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(
        environment, List.of("com.example.FooAutoConfiguration"));

// read the merged result
Set<String> excluded = ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses(environment);
```

Values are read both from the comma-separated `PropertySource` value and through `Binder` for array form.

| Scenario | Use |
|---|---|
| one authoritative list owned by the app | `spring.autoconfigure.exclude` |
| independently excluded by several libraries or teams | `microsphere.autoconfigure.exclude` |
| exclude from a library's defaults | `microsphere.autoconfigure.exclude` |

---

## 6. ConditionalOnPropertyPrefix Prefix Condition

A custom condition annotation with a **single member**, `String[] value()`, targeting types and methods
(`@Target({TYPE, METHOD})`, `@Retention(RUNTIME)`, `@Conditional(OnPropertyPrefixCondition.class)`) and delegating to
`OnPropertyPrefixCondition`. The condition matches when **any** property name present in the `Environment` **starts with
any** of the given prefixes (a trailing `.` is appended automatically, and placeholders are resolved). Spring Boot's own
`@ConditionalOnProperty` needs an exact name; this one only needs a family.

```java
import io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix;

@ConditionalOnPropertyPrefix("myapp.datasource")
@Configuration
public class DataSourceConfig {
    // loaded only when some "myapp.datasource.*" property exists
}
```

**Multiple prefixes are OR-ed**:

```java
@ConditionalOnPropertyPrefix({"myapp.redis", "myapp.cache"})
@Bean
public CacheManager cacheManager() {
    return new RedisCacheManager();
}
```

**Typical use**: "Enable the feature if any property of its family is configured" — no extra `...enabled` switch needed.

---

## 7. BindListener Binding Callbacks

`io.microsphere.spring.boot.context.properties.bind.BindListener` exposes the binding of `@ConfigurationProperties` as
five lifecycle callbacks, all `default` methods, so you override only what you need.

| Callback | Signature | Fires when |
|---|---|---|
| `onStart` | `<T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)` | binding begins |
| `onSuccess` | `void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | binding succeeded |
| `onCreate` | `void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | a new instance was created for an unbound result |
| `onFailure` | `void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)` | binding failed |
| `onFinish` | `void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | binding finished; not called on failure |

Note the `void` return type: unlike Spring Boot's `BindHandler`, whose callbacks return
`ConversionService.Convertible` results, these listeners only observe — they never alter the binding result.

**Wiring**: `ConfigurationPropertiesAutoConfiguration` — the auto-configuration class annotated with
`@ConditionalOnClass(name = "io.microsphere.spring.context.annotation.BeanCapableImportCandidate")`,
`@AutoConfigureBefore(org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration)` and
`@EnableConfigurationPropertiesExtension` — registers
`ListenableConfigurationPropertiesBindHandlerAdvisor`, which implements Boot's
`ConfigurationPropertiesBindHandlerAdvisor` and is `BeanFactoryAware`. It discovers every `BindListener` bean in the
`BeanFactory` and chains them around each binding through `ListenableBindHandlerAdapter extends AbstractBindHandler`
(constructor `(Iterable<BindListener>)` and `(BindHandler, Iterable<BindListener>)`; the aggregate implementation
`BindListeners` is available as well). **Declaring your listener as a bean is all it takes.**

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

**Manual composition** (outside the container):

```java
import io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;

BindHandler handler = new ListenableBindHandlerAdapter(List.of(new AuditBindListener()));
Binder.get(environment, handler).bind("my-app", Bindable.of(MyAppProperties.class));
```

**Common uses**: audit whether configuration really landed, catch mistyped property names, measure binding time, assert
that secret keys never appear in binding results.

---

## 8. ConfigurationPropertiesBeanPropertyChangedEvent Property Change Events

`io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent<T>` (extending
`io.microsphere.spring.context.event.BeanPropertyChangedEvent`) is published to the `ApplicationContext` whenever a
property of a `@ConfigurationProperties` bean changes value. Declared getters: `ResolvableType getPropertyType()` and
`ConfigurationProperty getConfigurationProperty()` — the latter carries `getOrigin()` of the change source, down to file
and line. The parent provides `getOldValue()`, `getNewValue()` and `getSource()` (the bean instance in which the change
happened). `EventPublishingConfigurationPropertiesBeanPropertyChangedListener` is the publisher: after all singletons
are instantiated it snapshots each `@ConfigurationProperties` bean and compares subsequent re-binding against that
snapshot.

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
        // resize without a restart
    }
}
```

**Switch**: publishing depends on `microsphere.spring.event-publishing-bean.enabled=true`, a `core.properties` default.
Set it to `false` to keep `BindListener` callbacks but suppress application events.

---

## 9. Classpath Diagnostics

Three mechanisms turn dependency conflicts from runtime mysteries into readable startup failures.

### 9.1 Banned Artifacts

`io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener` is a `SpringApplicationRunListener` acting
at `ApplicationStartingEvent` — the earliest moment — that prevents banned JARs from being loaded by a class loader. The
switch constant is `BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME = "microsphere.spring.boot.banned-artifacts.enabled"`,
default `false`, and it is read from **JVM system properties**
(`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`, read through `Boolean.getBoolean`), because it must be
decided before the `Environment` exists. Configure it with `-D` or with `System.setProperty` in `main`; putting it in
`application.properties` has **no effect**.

```bash
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar app.jar
```

```java
public static void main(String[] args) {
    System.setProperty("microsphere.spring.boot.banned-artifacts.enabled", "true");
    SpringApplication.run(MyApplication.class, args);
}
```

Internally the listener guarantees the banning logic runs once per `SpringApplication`.

### 9.2 Artifact Collision Detection

`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener` (an `ApplicationListener`) scans the
classpath for duplicated versions of the same artifact and throws `ArtifactsCollisionException`, which carries the set
of colliding artifact IDs. The switch constant is
`ENABLED_PROPERTY_NAME = "microsphere.spring.boot.artifacts-collision.enabled"`, default `false`, and its source **is**
the `Environment` (`environment.getProperty(name, Boolean.class, false)`), so unlike 9.1 it can live in a configuration
file:

```properties
microsphere.spring.boot.artifacts-collision.enabled=true
```

When a collision is thrown, `ArtifactsCollisionFailureAnalyzer` (a Boot `FailureAnalyzer`) renders a structured report
with a ready-made troubleshooting command:

```
mvn dependency:tree -Dincludes=com.example:lib-a,com.example:lib-b
```

### 9.3 Condition Evaluation Report

`ConditionEvaluationReportInitializer` (an `ApplicationContextInitializer`), `ConditionEvaluationReportListener` (an
`ApplicationListener`) and `ConditionEvaluationSpringBootExceptionReporter` (a `SpringBootExceptionReporter`) cooperate
to cache Spring Boot's `ConditionEvaluationReport` and, on startup failure, print which auto-configurations matched,
which did not, and why. The reported scope is adjustable:

```properties
# default io.microsphere, widen as needed
microsphere.spring.boot.conditions.report.base-packages=com.mycompany,io.microsphere
```

The diagnostics verbosity is `microsphere.spring.boot.logging.level`, default `TRACE`.

---

## 10. OnceApplicationPreparedEventListener One-Time Listener

An abstract `ApplicationListener<ApplicationPreparedEvent>` (just before the context is refreshed) that also implements
`Ordered` and deduplicates by context id, guaranteeing one execution per context — important under context reuse in
tests. Subclasses implement two abstract methods. Repository implementations:
`LoggingOnceApplicationPreparedEventListener`, the abstract `OnceMainApplicationPreparedEventListener` (bootstrap-aware
via the constant `BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME = "spring.cloud.bootstrap.name"` and
`DEFAULT_BOOTSTRAP_CONTEXT_ID = "bootstrap"`, and detecting `BootstrapApplicationListener`), and
`LoggingOnceMainApplicationPreparedEventListener`.

```java
public class MySetupListener extends OnceApplicationPreparedEventListener {

    @Override
    protected boolean isIgnored(SpringApplication application, String[] args,
                                ConfigurableApplicationContext context) {
        return false; // true = skip this context
    }

    @Override
    protected void onApplicationEvent(SpringApplication application, String[] args,
                                      ConfigurableApplicationContext context) {
        // at most once per context
    }
}
```

```properties
# META-INF/spring.factories
org.springframework.context.ApplicationListener=com.example.MySetupListener
```

---

## 11. SpringApplicationUtils Utility Class

Public static methods of `io.microsphere.spring.boot.util.SpringApplicationUtils`:

| Method | Purpose |
|---|---|
| `addDefaultPropertiesResource(String)` | append one resource location |
| `addDefaultPropertiesResources(String...)` | append several |
| `getDefaultPropertiesResources()` | read the current set |
| `getResourceLoader(SpringApplication)` | obtain the `ResourceLoader` safely, falling back to `DefaultResourceLoader` |
| `getLoggingLevel(ConfigurableApplicationContext)` / `getLoggingLevel(PropertyResolver)` | read `microsphere.spring.boot.logging.level` |
| `log(SpringApplication, String[], String, Object...)` | print a `SpringApplication` diagnostics summary at the configured level |
| `log(SpringApplication, String[], ConfigurableApplicationContext, String, Object...)` | same, including the context |

```java
SpringApplicationUtils.addDefaultPropertiesResources(
        "classpath*:/META-INF/config/team-defaults/*.properties");
```

> **Deprecated.** `io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter` still exists in the sources,
> but new code should implement `SpringApplicationRunListener` directly, or use
> `ApplicationListener<SpringApplicationEvent>`.

---

## 12. Extension Point Registration

Everything that activates merely by having `core` on the classpath — registered in
`src/main/resources/META-INF/spring.factories` plus, for the auto-configuration class,
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

| Spring Boot extension point | Implementation |
|---|---|
| `EnableAutoConfiguration` (imports file) | `...context.autoconfigure.ConfigurationPropertiesAutoConfiguration` |
| `AutoConfigurationImportFilter` | `...autoconfigure.ConfigurableAutoConfigurationImportFilter` |
| `SpringApplicationRunListener` | `...classloading.BannedArtifactClassLoadingListener`, `...listener.FailureReportSpringApplicationRunListener` |
| `ApplicationListener` | `...diagnostics.ArtifactsCollisionDiagnosisListener`, `...report.ConditionEvaluationReportListener`, `...env.DefaultPropertiesApplicationListener` |
| `ApplicationContextInitializer` | `...report.ConditionEvaluationReportInitializer`, `...env.config.OriginTrackedConfigurationPropertyInitializer` |
| `SpringBootExceptionReporter` | `...report.ConditionEvaluationSpringBootExceptionReporter` |
| `FailureAnalyzer` | `...diagnostics.ArtifactsCollisionFailureAnalyzer` |
| `DefaultPropertiesPostProcessor` (this library's own SPI) | `...env.SpringApplicationDefaultPropertiesPostProcessor` |

Apart from `org.springframework.boot.diagnostics.AbstractFailureAnalyzer`, `core` exposes no presentation-style
entry points and registers no web-layer components; everything is configuration plus diagnostics.

In addition, `io.microsphere.spring.boot.context.properties.metadata.ConfigurationMetadataReader` reads
`classpath*:/META-INF/spring-configuration-metadata.json` and
`classpath*:/META-INF/additional-spring-configuration-metadata.json` (constants `METADATA_PATH`,
`ADDITIONAL_METADATA_PATH`) and feeds the `actuator` module's metadata endpoint.

---

## 13. Property Reference

| Property | Source | Default | Effect |
|---|---|---|---|
| `microsphere.autoconfigure.exclude` | Environment (merged) | set by `core.properties` | exclude auto-configurations |
| `microsphere.spring.boot.logging.level` | Environment | `TRACE` | diagnostics level |
| `microsphere.spring.boot.banned-artifacts.enabled` | **JVM system property** | `false` | enable artifact banning |
| `microsphere.spring.boot.artifacts-collision.enabled` | Environment | `false` | enable collision detection |
| `microsphere.spring.boot.conditions.report.base-packages` | Environment | `io.microsphere` | condition-report scope |
| `microsphere.spring.event-publishing-bean.enabled` | Environment (Microsphere Spring) | `true` | publish change events |
| `microsphere.spring.listenable-autowire-candidate-resolver.enabled` | Environment | `false` | Microsphere Spring switch |
| `microsphere.spring.listenable-environment.enabled` | Environment | `false` | Microsphere Spring switch |
| `server.shutdown` / `spring.lifecycle.timeout-per-shutdown-phase` | Environment | `graceful` / `60s` | library defaults |
| `spring.mvc.hiddenmethod.filter.enabled` / `formcontent` / `favicon` | Environment | `false` | quiet noisy filters |

---

## 14. FAQ

| Symptom | Answer |
|---|---|
| my `server.shutdown` setting changed | It is a `defaultProperties` value in the lowest-precedence source; override it explicitly. |
| the banned-artifacts switch in `application.properties` is ignored | That key is read from JVM system properties only; use `-D` or `System.setProperty`. |
| my `BindListener` is never called | It must be a Spring bean, and `ConfigurationPropertiesAutoConfiguration` must not be excluded. |
| no property change events arrive | Check `microsphere.spring.event-publishing-bean.enabled`, and whether the change really comes from re-binding: the first binding never publishes. |
| my exclusions "disappeared" | You used `spring.autoconfigure.exclude` (replacing semantics); switch to `microsphere.autoconfigure.exclude` (merging). |
| which version of a library actually loaded | `GET /actuator/microsphere/artifacts`, see [actuator.md](./actuator.md). |

---

**Next:** [actuator.md](./actuator.md) · [webmvc.md](./webmvc.md) · [Documentation index](../README.md)
