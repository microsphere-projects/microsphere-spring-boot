# Core Module

> [中文](./zh/core.md) · [Documentation index](../README.md)
>
> **Branch:** `release-1.x` · **Version line:** `0.1.x` · **Artifact:** `microsphere-spring-boot-core`
> **Spring Boot:** `2.1.18.RELEASE` – `2.7.18` · **Java:** 8+

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

The `0.1.x` `core` offers the same capability set as `0.2.x`: overridable module-level defaults, mergeable
auto-configuration exclusions, property-prefix conditions, observable `@ConfigurationProperties` binding with change
events, and diagnostics that fail the boot on classpath conflicts. The differences are in **delivery**: Spring Boot 2.x
had no `AutoConfiguration.imports` before 2.7, so this branch registers both `spring.factories` and the `imports` file;
the version enum models Boot 2.x only; and the `compatible` shims serve **older** Boot (2.1-2.3) rather than newer.

Base package: `io.microsphere.spring.boot`; user-facing sub-packages:

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

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-core` |
| Latest published | `0.1.34` |
| Branch in development | `0.1.35-SNAPSHOT` |
| Spring Boot profiles pinned | `2.1.18.RELEASE / 2.2.13.RELEASE / 2.3.12.RELEASE / 2.4.13 / 2.5.15 / 2.6.15 / 2.7.18 (default)` |
| Java | The branch POM sets no language level (inherited from `microsphere-build:0.3.14`); CI validates JDK `8 / 11 / 17 / 21 / 25`, so the supported floor is Java 8 |
| Upstream | `microsphere-spring:0.1.39` (BOM import) |
| Older-profile attached dependency | `microsphere-spring-boot-compatible`, added by this module's POM only under `-Pspring-boot-2.1` / `2.2` / `2.3` |
| Registration | `META-INF/spring.factories` (`EnableAutoConfiguration`) **and** `META-INF/spring/...AutoConfiguration.imports` |

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-core</artifactId>
    <!-- version managed by the microsphere-spring-boot-dependencies:0.1.34 BOM -->
</dependency>
```

---

## 3. Default Properties

`DefaultPropertiesApplicationListener` listens on `ApplicationEnvironmentPreparedEvent`
(`DEFAULT_ORDER = LOWEST_PRECEDENCE - 1`), scans every classpath resource matching
`classpath*:/META-INF/config/default/*.*` and publishes the result into `defaultProperties`, Spring Boot's
lowest-precedence source - so your own configuration always wins. Merging is `putIfAbsent`, so duplicate keys log a
warning instead of overwriting each other.

```java
// io.microsphere.spring.boot.env.SpringApplicationDefaultPropertiesPostProcessor
public static final String DEFAULT_PROPERTIES_RESOURCES_PATTERN = "classpath*:/META-INF/config/default/*.*";
```

The file shipped by this module, `META-INF/config/default/core.properties`:

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

> **Boot 2 semantics.** `spring.mvc.favicon.enabled` is a valid property on Spring Boot 2.x; on Boot 3 the property was
> removed together with the favicon endpoint. This default file is therefore **written for the Boot 2 line** - the
> `0.2.x` file is identical apart from the first two entries becoming harmless redundancy. `spring.mvc.favicon.enabled`
> is meaningful on Boot 2.x but removed in Boot 3, where the entry becomes inert.

**Adding your own defaults**: put `src/main/resources/META-INF/config/default/my-module.properties` in your jar.

```properties
my-app.client.connect-timeout = 5s
microsphere.autoconfigure.exclude = org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

**SPI extension**:

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
        return LOWEST_PRECEDENCE;   // interface default
    }
}
```

```properties
# META-INF/spring.factories
io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=com.example.TeamDefaultsPostProcessor
```

**Signature**: `DefaultPropertiesPostProcessor extends Ordered` declares `void initializeResources(Set<String>)`, a
default `void postProcess(Map<String, Object>)` and a default `int getOrder()`. Resource loading delegates to
`PropertySourceLoaders`, so `.properties` and `.yml` both work (which is exactly why the pattern is `*.*`).

---

## 4. SpringBootVersion Version-Aware API

On this branch the enum models Spring Boot 2.x only: first constant `SPRING_BOOT_2_0`, last numeric constant
`SPRING_BOOT_2_7_18`, followed by the runtime-detected `CURRENT`. Name-to-version conversion is identical to `0.2.x`
(`name.substring(12)`, `_` to `.`). **`SPRING_BOOT_3_*` and `SPRING_BOOT_4_*` constants do not exist here** - the most
common compile error when porting code from `0.2.x`.

| Method | Alias | Meaning |
|---|---|---|
| `getVersion()` | - | version string |
| `getMajor()` / `getMinor()` / `getPatch()` | - | numeric parts |
| `gt(...)` | `isGreaterThan` | greater than |
| `ge(...)` | `isGreaterOrEqual` | at least |
| `lt(...)` | `isLessThan` | less than |
| `le(...)` | `isLessOrEqual` | at most |
| `eq(...)` | `equals` | equal |
| `resolveVersion(String)` | - | static parsing |

```java
import io.microsphere.spring.boot.SpringBootVersion;

// behaviour that landed in 2.6
if (SpringBootVersion.CURRENT.ge(SpringBootVersion.SPRING_BOOT_2_6)) {
    useNewerPath();
} else {
    useOlderPath();
}
```

```java
// exact patch-level guard
if (SpringBootVersion.CURRENT.lt(SpringBootVersion.SPRING_BOOT_2_7_18)) {
    log.warn("consider upgrading to 2.7.18");
}
```

---

## 5. ConfigurableAutoConfigurationImportFilter Property-Driven Exclusion

Registered as a Spring Boot `AutoConfigurationImportFilter` through `spring.factories`, ordered
`HIGHEST_PRECEDENCE + 99`, it removes auto-configuration classes named by `microsphere.autoconfigure.exclude` before any
bean is created. The essential difference from standard `spring.autoconfigure.exclude` (also referenced via
`SpringBootPropertyConstants`, together with the attached property-source name `configurationProperties`) is semantics:
the standard property **replaces**, this one **merges** across all sources.

```properties
# comma-separated
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration

# indexed array (both forms are read)
microsphere.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

**Programmatic API** - four public static methods:

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

> **The multi-module payoff.** In a multi-module Boot 2 project, each starter writing its own
> `spring.autoconfigure.exclude` erases the others; putting them in each module's `META-INF/config/default/*.properties`
> and using `microsphere.autoconfigure.exclude` keeps them all. When several starters each set
> `spring.autoconfigure.exclude` they erase one another; putting `microsphere.autoconfigure.exclude` in each module's
> defaults keeps all of them.

---

## 6. ConditionalOnPropertyPrefix Prefix Condition

A condition annotation whose **only member** is `String[] value()`, delegating to `OnPropertyPrefixCondition`. It matches
when any property name in the `Environment` starts with any of the given prefixes (a trailing `.` is appended and
placeholders are resolved). Spring Boot 2.x's `@ConditionalOnProperty` demands an exact name; this only needs the family
to exist.

```java
import io.microsphere.spring.boot.condition.ConditionalOnPropertyPrefix;

@ConditionalOnPropertyPrefix("myapp.datasource")
@Configuration
public class DataSourceConfig {
    // assembled only if some myapp.datasource.* exists
}
```

```java
// multiple prefixes are OR-ed
@ConditionalOnPropertyPrefix({"myapp.redis", "myapp.cache"})
@Bean
public CacheManager cacheManager() {
    return new RedisCacheManager();
}
```

---

## 7. BindListener Binding Callbacks

Five `default` callbacks, signatures identical to the `0.2.x` line:

| Callback | Signature | Fires when |
|---|---|---|
| `onStart` | `<T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)` | binding begins |
| `onSuccess` | `void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | succeeded |
| `onCreate` | `void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | a new instance for an unbound result |
| `onFailure` | `void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)` | failed |
| `onFinish` | `void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)` | finished, skipped on failure |

**Wiring**: `ConfigurationPropertiesAutoConfiguration` (annotated with
`@ConditionalOnClass(name = "io.microsphere.spring.context.annotation.BeanCapableImportCandidate")`,
`@AutoConfigureBefore(org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration)` and
`@EnableConfigurationPropertiesExtension`) registers `ListenableConfigurationPropertiesBindHandlerAdvisor` (the Boot
`ConfigurationPropertiesBindHandlerAdvisor`, `BeanFactoryAware`), which discovers every `BindListener` bean in the
container and chains them into each binding through `ListenableBindHandlerAdapter` (`extends AbstractBindHandler`,
constructors `(Iterable<BindListener>)` and `(BindHandler, Iterable<BindListener>)`; the aggregate implementation
`BindListeners`). **Put your listener in the container and it participates in every binding.**

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

**Outside the container**:

```java
import io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;

BindHandler handler = new ListenableBindHandlerAdapter(List.of(new AuditBindListener()));
Binder.get(environment, handler).bind("my-app", Bindable.of(MyAppProperties.class));
```

> **Boot 2 caveat.** The methods return `void`, unlike the return-value semantics of Spring Boot 2.x `BindHandler`:
> listeners observe only and never rewrite a binding result. To alter a result, implement `BindHandler` yourself and
> compose it via the two-argument `ListenableBindHandlerAdapter(BindHandler, Iterable<BindListener>)` constructor.
> The `void` returns mean listeners observe only. To alter a result, implement `BindHandler` yourself and compose it via
> the two-argument `ListenableBindHandlerAdapter` constructor.

---

## 8. ConfigurationPropertiesBeanPropertyChangedEvent Property Change Events

Declared getters `getPropertyType()` and `getConfigurationProperty()` (which carries `getOrigin()`, down to file and
line); the parent supplies `getOldValue()`, `getNewValue()` and `getSource()`. The publishing listener snapshots each
`@ConfigurationProperties` bean after all singletons are ready and compares later re-binding against it - the initial
binding never publishes. Switch: `microsphere.spring.event-publishing-bean.enabled = true`, a default from
`core.properties`.

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

Under `@RefreshScope` or a config-center push, this is the shortest path from "a value changed" to "something reacted".

---

## 9. Classpath Diagnostics

### 9.1 Banned Artifacts

`io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener` (a `SpringApplicationRunListener`) prevents
the banned JAR from being loaded at `ApplicationStartingEvent`. The switch constant
`BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME = "microsphere.spring.boot.banned-artifacts.enabled"`, default `false`, is read
from **JVM system properties** (the annotation declares `source = SYSTEM_PROPERTIES_SOURCE`, read through
`Boolean.getBoolean`), because it must be decided before the `Environment` exists. Setting it in a configuration file has
no effect:

```bash
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar app.jar
```

```java
public static void main(String[] args) {
    System.setProperty("microsphere.spring.boot.banned-artifacts.enabled", "true");
    SpringApplication.run(MyApplication.class, args);
}
```

### 9.2 Artifact Collision Detection

`io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener` scans for duplicated versions of the same
artifact and throws `ArtifactsCollisionException`. The switch constant
`ENABLED_PROPERTY_NAME = "microsphere.spring.boot.artifacts-collision.enabled"`, default `false`, **is read from the
`Environment`** (`environment.getProperty(name, Boolean.class, false)`):

```properties
microsphere.spring.boot.artifacts-collision.enabled=true
```

`ArtifactsCollisionFailureAnalyzer` (a Boot `FailureAnalyzer`) then renders the report with a ready-made command:

```
mvn dependency:tree -Dincludes=com.example:lib-a,com.example:lib-b
```

### 9.3 Condition Evaluation Report

`ConditionEvaluationReportInitializer` (an `ApplicationContextInitializer`) + `ConditionEvaluationReportListener` (an
`ApplicationListener`) + `ConditionEvaluationSpringBootExceptionReporter` (a `SpringBootExceptionReporter`) cache the
`ConditionEvaluationReport` and, on startup failure, print the matched and unmatched candidates and their reasons; the
reported scope is adjustable:

```properties
microsphere.spring.boot.conditions.report.base-packages=com.mycompany,io.microsphere   # default io.microsphere
microsphere.spring.boot.logging.level=TRACE                                            # default TRACE
```

`LoggingSpringApplicationRunListener` and `FailureReportSpringApplicationRunListener` both participate in the reporting
chain on this branch.

---

## 10. OnceApplicationPreparedEventListener One-Time Listener

`io.microsphere.spring.boot.context.OnceApplicationPreparedEventListener` is an abstract
`ApplicationListener<ApplicationPreparedEvent>` base class (also implementing `Ordered`) that deduplicates by context id,
guaranteeing a single execution per context. Two abstract methods:

```java
protected abstract boolean isIgnored(SpringApplication application, String[] args,
                                     ConfigurableApplicationContext context);

protected abstract void onApplicationEvent(SpringApplication application, String[] args,
                                           ConfigurableApplicationContext context);
```

`OnceMainApplicationPreparedEventListener` skips the bootstrap context (constants for `spring.cloud.bootstrap.name` and
the default id `bootstrap`, plus detection of `BootstrapApplicationListener`), so under Spring Cloud only the main
context runs - once. It also adds `protected final boolean isIgnored(ConfigurableApplicationContext)` plus a
`protected boolean isIgnored(...)` overload; the repository ships `LoggingOnceApplicationPreparedEventListener` and
`LoggingOnceMainApplicationPreparedEventListener`, so in a Boot 2 app with both a bootstrap and a main context only the
main context is handled once.

```java
public class MySetupListener extends OnceMainApplicationPreparedEventListener {

    @Override
    protected boolean isIgnored(SpringApplication application, String[] args,
                                ConfigurableApplicationContext context) {
        return false;   // true = skip this context
    }

    @Override
    protected void onApplicationEvent(SpringApplication application, String[] args,
                                      ConfigurableApplicationContext context) {
        // at most once per main context
    }
}
```

```properties
# META-INF/spring.factories
org.springframework.context.ApplicationListener=com.example.MySetupListener
```

---

## 11. SpringApplicationUtils Utility Class

`io.microsphere.spring.boot.util.SpringApplicationUtils` public static methods:

| Method | Purpose |
|---|---|
| `addDefaultPropertiesResource(String)` | append one location |
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

> **Deprecated.** `io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter` still exists in the sources;
> new code should implement `SpringApplicationRunListener` directly, or use an
> `ApplicationListener<SpringApplicationEvent>`. Prefer implementing `SpringApplicationRunListener` directly, or an
> `ApplicationListener<SpringApplicationEvent>`.

---

## 12. Extension Point Registration

Auto-configuration is registered **twice** - under Boot 2's `EnableAutoConfiguration` key in `spring.factories` *and* in
the `AutoConfiguration.imports` file (recognised from Boot 2.7, easing migration). All other extension points live only
in `spring.factories`:

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

The `actuator`, `webmvc` and `webflux` modules each provide the same dual registration.

---

## 13. Property Reference

| Property | Source | Default | Effect |
|---|---|---|---|
| `microsphere.autoconfigure.exclude` | Environment (merged) | set by `core.properties` | exclude auto-configurations |
| `microsphere.spring.boot.logging.level` | Environment | `TRACE` | diagnostics level |
| `microsphere.spring.boot.banned-artifacts.enabled` | **JVM system property** | `false` | enable banning |
| `microsphere.spring.boot.artifacts-collision.enabled` | Environment | `false` | enable collision check |
| `microsphere.spring.boot.conditions.report.base-packages` | Environment | `io.microsphere` | report scope |
| `microsphere.spring.event-publishing-bean.enabled` | Environment | `true` | publish events |
| `microsphere.spring.listenable-autowire-candidate-resolver.enabled` | Environment | `false` | Microsphere Spring switch |
| `microsphere.spring.listenable-environment.enabled` | Environment | `false` | Microsphere Spring switch |
| `spring.cloud.bootstrap.name` | Environment | `bootstrap` (in-library constant) | bootstrap-context detection |

---

## 14. FAQ

| Symptom | Answer |
|---|---|
| Porting to `0.2.x` fails: `SPRING_BOOT_2_7` constant missing | The enum models versions per branch: `0.1.x` has only Boot 2.x constants, `0.2.x` only Boot 3/4. Rewrite the guard. |
| The banned-artifacts switch has no effect | It is read only from JVM system properties; use `-D` or `System.setProperty`. |
| `BindListener` is silent | It must be a bean and `ConfigurationPropertiesAutoConfiguration` must not be excluded. |
| No property-change events arrive | Check `microsphere.spring.event-publishing-bean.enabled`; the initial binding never publishes, only re-binding does. |
| Auto-configuration inactive on Boot 2.4/2.5 | Expected: the `AutoConfigurationImportFilter` extension point only exists from Boot 2.7, so the merge feature is unavailable on 2.4-2.6 while the other `spring.factories` registrations still work. |
| Setup logic runs twice under Spring Cloud | Extend `OnceMainApplicationPreparedEventListener` instead of `OnceApplicationPreparedEventListener` to skip the bootstrap context. |

---

**Next:** [actuator.md](./actuator.md) · [webmvc.md](./webmvc.md) · [Documentation index](../README.md)
