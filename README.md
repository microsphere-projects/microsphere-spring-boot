# Microsphere Spring Boot

> Microsphere Projects for Spring Boot

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-spring-boot)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-spring-boot)
[![Maven Build](https://github.com/microsphere-projects/microsphere-spring-boot/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-spring-boot/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-spring-boot/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-spring-boot)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-spring-boot.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-spring-boot.svg)

Microsphere Spring Boot is a collection of libraries that extends Spring Boot's capabilities with additional features
focused on configuration management, application diagnostics, and enhanced monitoring. The project is structured as a
multi-module Maven project that follows Spring Boot's conventions while providing value-added functionality.

## Table of Contents

- [What the Project Does](#what-the-project-does)
- [Why the Project is Useful](#why-the-project-is-useful)
- [Modules](#modules)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Add the BOM](#add-the-bom)
    - [Add Module Dependencies](#add-module-dependencies)
- [Usage](#usage)
    - [Default Properties](#default-properties)
    - [Excluding Auto-Configurations](#excluding-auto-configurations)
    - [Configuration Properties Binding Listeners](#configuration-properties-binding-listeners)
    - [Actuator Endpoints](#actuator-endpoints)
    - [Monitored Task Scheduler](#monitored-task-scheduler)
    - [Classpath Artifact Detection](#classpath-artifact-detection)
- [Building from Source](#building-from-source)
- [Getting Help](#getting-help)
- [Contributing](#contributing)
- [Maintainers](#maintainers)
- [License](#license)

## What the Project Does

Microsphere Spring Boot provides production-ready enhancements for Spring Boot applications, covering:

- **Default Properties**: Pre-configured sensible defaults (graceful shutdown, MVC filter toggling) loaded from
  `config/default/*.properties` on the classpath, which can be overridden per-module.
- **Advanced Auto-Configuration Management**: A configurable import filter that allows merging multiple
  `microsphere.autoconfigure.exclude` property sources without losing earlier exclusions.
- **Configuration Property Binding Listeners**: Event-driven hooks (`BindListener`) that notify your beans whenever a
  `@ConfigurationProperties`-bound property value changes.
- **Custom Actuator Endpoints**: Four additional actuator endpoints that expose classpath artifact metadata,
  web-endpoint mappings, configuration metadata, and bound configuration properties.
- **Monitored Task Scheduler**: A `ThreadPoolTaskScheduler` wrapper that records execution metrics via Micrometer,
  giving you task latency and count out of the box.
- **Artifact Collision Detection**: Startup-time detection of duplicate JAR artifacts on the classpath, surfaced as a
  descriptive `FailureAnalyzer` message.
- **Multi-Version Compatibility**: A dedicated compatibility shim module keeps the codebase working with Spring Boot
  2.x through 4.x without requiring separate forks.

## Why the Project is Useful

| Benefit                                | Detail                                                                                                                       |
|----------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| **Opinionated defaults**               | Sensible, overridable defaults (graceful shutdown, disabled noisy filters) reduce boilerplate in every application.          |
| **Observable configuration**           | Know immediately when any `@ConfigurationProperties` value changes at runtime via `BindListener`.                            |
| **Richer actuator surface**            | Four extra endpoints (`/actuator/microsphere/*`) expose the runtime state that Spring Boot's built-in endpoints don't cover. |
| **Safe auto-configuration exclusions** | `microsphere.autoconfigure.exclude` merges all sources; the standard `spring.autoconfigure.exclude` replaces them.           |
| **Scheduling metrics**                 | `MonitoredThreadPoolTaskScheduler` instruments every scheduled task with Micrometer counters and timers at zero config.      |
| **Dependency health**                  | Banned-artifact and collision detection catches classpath problems at startup rather than at runtime.                        |
| **Long Spring Boot lifecycle**         | Supports Spring Boot 2.0 – 4.x across two maintained release lines (`0.1.x` for Boot 2, `0.2.x` for Boot 3/4).               |

## Modules

| **Module**                               | **Artifact ID**                        | **Purpose**                                                                                   |
|------------------------------------------|----------------------------------------|-----------------------------------------------------------------------------------------------|
| **microsphere-spring-boot-parent**       | `microsphere-spring-boot-parent`       | Parent POM: build plugins, Java 17 baseline, Spring Boot version profiles                     |
| **microsphere-spring-boot-dependencies** | `microsphere-spring-boot-dependencies` | BOM — import this to manage all module versions                                               |
| **microsphere-spring-boot-compatible**   | `microsphere-spring-boot-compatible`   | Compatibility shims for running across Spring Boot 2.x – 4.x (not published to Maven Central) |
| **microsphere-spring-boot-core**         | `microsphere-spring-boot-core`         | Core features: default properties, auto-configuration filter, bind listeners, diagnostics     |
| **microsphere-spring-boot-actuator**     | `microsphere-spring-boot-actuator`     | Actuator extensions: custom endpoints, monitored scheduler, opinionated endpoint defaults     |

## Getting Started

### Prerequisites

| Requirement | Version                                                             |
|-------------|---------------------------------------------------------------------|
| Java        | 17+                                                                 |
| Maven       | 3.9+ (or use the included `mvnw` wrapper)                           |
| Spring Boot | 3.0.x – 3.5.x, 4.0.x (`main` branch) / 2.0.x – 2.7.x (`1.x` branch) |

### Add the BOM

Import the Microsphere Spring Boot BOM into your `pom.xml` so all module versions are managed for you:

```xml

<dependencyManagement>
    <dependencies>
        <!-- Microsphere Spring Boot BOM -->
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

Choose the version that matches your Spring Boot generation:

| Branch | Spring Boot Compatibility | Latest Version |
|--------|---------------------------|----------------|
| `main` | 3.0.x – 3.5.x, 4.0.x      | `0.2.13`       |
| `1.x`  | 2.0.x – 2.7.x             | `0.1.13`       |

### Add Module Dependencies

After importing the BOM, add only the modules you need — no version required:

```xml

<dependencies>
    <!-- Core features: default properties, auto-config filter, bind listeners, diagnostics -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-core</artifactId>
    </dependency>

    <!-- Actuator extensions: custom endpoints, monitored scheduler -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-actuator</artifactId>
    </dependency>
</dependencies>
```

## Usage

### Default Properties

`microsphere-spring-boot-core` automatically loads property files from `config/default/*.properties` on the classpath.
The built-in `core.properties` enables graceful shutdown and disables several noisy Spring MVC filters out of the box:

```properties
# Graceful shutdown (built into core.properties)
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=60s
# Disabled by default
spring.mvc.hiddenmethod.filter.enabled=false
spring.mvc.formcontent.filter.enabled=false
```

To add your own module-level defaults, place a file at `config/default/<your-module>.properties` on the classpath.
These files are merged together, so none of the values from other modules are lost.

### Excluding Auto-Configurations

`microsphere.autoconfigure.exclude` works like Spring Boot's standard `spring.autoconfigure.exclude` but **merges**
values from all property sources instead of replacing them. Add it to any default-properties file or to
`application.properties`:

```properties
# config/default/my-module.properties
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
```

> **Tip:** Use `microsphere.autoconfigure.exclude` when multiple teams need to exclude different auto-configurations
> independently. Use the standard `spring.autoconfigure.exclude` when you want a single, authoritative exclusion list.

### Configuration Properties Binding Listeners

Register a `BindListener` bean to be notified whenever a `@ConfigurationProperties`-bound property changes:

```java
import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.stereotype.Component;

@Component
public class MyBindListener implements BindListener {

    @Override
    public void onStart(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        // called before binding
    }

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Object result) {
        System.out.println("Bound " + name + " = " + result);
    }
}
```

A `ConfigurationPropertiesBeanPropertyChangedEvent` is also published to the Spring `ApplicationContext` whenever a
`@ConfigurationProperties` bean property value changes, so you can use `@EventListener` as an alternative.

### Actuator Endpoints

`microsphere-spring-boot-actuator` registers four additional actuator endpoints (all enabled by default):

| Endpoint           | Default Path                              | Description                                                                       |
|--------------------|-------------------------------------------|-----------------------------------------------------------------------------------|
| `artifacts`        | `/actuator/microsphere/artifacts`         | Lists all JAR artifacts detected on the classpath                                 |
| `webEndpoints`     | `/actuator/microsphere/web/endpoints`     | Lists all registered Spring MVC / WebFlux actuator endpoint mappings              |
| `configMetadata`   | `/actuator/microsphere/config/metadata`   | Exposes Spring Boot configuration metadata (`spring-configuration-metadata.json`) |
| `configProperties` | `/actuator/microsphere/config/properties` | Exposes all currently bound `@ConfigurationProperties` values                     |

The module also ships with an opinionated `endpoints.properties` default that enables only the most-used standard
endpoints (`health`, `info`, `env`, `loggers`, `metrics`, `mappings`, `prometheus`, `jolokia`) and configures
appropriate TTL-based caching for every endpoint.

### Monitored Task Scheduler

Replace `ThreadPoolTaskScheduler` with `MonitoredThreadPoolTaskScheduler` to get automatic Micrometer metrics
(task count, execution time) for every scheduled task:

```java
import io.microsphere.spring.boot.actuate.MonitoredThreadPoolTaskScheduler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    @Bean
    public MonitoredThreadPoolTaskScheduler taskScheduler(MeterRegistry meterRegistry) {
        MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler(meterRegistry);
        scheduler.setPoolSize(4);
        return scheduler;
    }
}
```

### Classpath Artifact Detection

`BannedArtifactClassLoadingListener` checks for explicitly banned JARs at startup. If a banned artifact is detected,
the application fails fast with a human-readable `FailureAnalyzer` message that identifies the offending dependency.

The collision detector (`ArtifactsCollisionDiagnosisListener`) also checks for duplicate versions of the same artifact
and raises `ArtifactsCollisionException` with full classpath details so you can fix the conflict before the application
reaches production.

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-spring-boot.git
cd microsphere-spring-boot
```

2. Build and run tests:

   **Linux / macOS**

   ```bash
   ./mvnw verify
   ```

   **Windows**

   ```powershell
   mvnw.cmd verify
   ```

3. Install to your local Maven repository (skipping tests for speed):

   ```bash
   ./mvnw install -DskipTests
   ```

> **Java version:** The build requires Java 17 or later.

## Getting Help

| Resource                  | Link                                                                                   |
|---------------------------|----------------------------------------------------------------------------------------|
| **User Guide (DeepWiki)** | https://deepwiki.com/microsphere-projects/microsphere-spring-boot                      |
| **User Guide (ZRead)**    | https://zread.ai/microsphere-projects/microsphere-spring-boot                          |
| **GitHub Wiki**           | https://github.com/microsphere-projects/microsphere-spring-boot/wiki                   |
| **JavaDoc — core**        | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-boot-core     |
| **JavaDoc — actuator**    | https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-boot-actuator |
| **Issue Tracker**         | https://github.com/microsphere-projects/microsphere-spring-boot/issues                 |

**Reporting a bug:**

1. Search [existing issues](https://github.com/microsphere-projects/microsphere-spring-boot/issues) first.
2. If the issue does not
   exist, [open a new issue](https://github.com/microsphere-projects/microsphere-spring-boot/issues/new).
3. Include the Spring Boot version, Microsphere Spring Boot version, a minimal reproducer, and any relevant logs.

## Contributing

Contributions of all kinds are welcome — bug fixes, documentation improvements, and new features.

1. Fork the repository and create a feature branch.
2. Make your changes and add or update tests as appropriate.
3. Run `./mvnw verify` to ensure all tests pass before submitting.
4. Open a pull request against the `main` branch (or `1.x` for Spring Boot 2.x fixes).
5. Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) before participating in the community.

## Maintainers

| Name     | GitHub                                       | Email                |
|----------|----------------------------------------------|----------------------|
| Mercy Ma | [@mercyblitz](https://github.com/mercyblitz) | mercyblitz@gmail.com |

The project is maintained under the [Microsphere Projects](https://github.com/microsphere-projects) organisation.

## License

Microsphere Spring Boot is released under the [Apache License 2.0](./LICENSE).
