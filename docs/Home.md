# Microsphere Spring Boot

[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%7C%204.x-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-spring-boot.svg)](https://search.maven.org/search?q=g:io.github.microsphere-projects%20a:microsphere-spring-boot)
[![GitHub Stars](https://img.shields.io/github/stars/microsphere-projects/microsphere-spring-boot?style=social)](https://github.com/microsphere-projects/microsphere-spring-boot)

**Microsphere Spring Boot** is an extension library for [Spring Boot](https://spring.io/projects/spring-boot) that provides enhanced auto-configuration, configuration properties binding, diagnostics, monitoring, and a rich set of utilities. It is part of the [Microsphere Projects](https://github.com/microsphere-projects) ecosystem.

- **Group ID:** `io.github.microsphere-projects`
- **Current Version:** `0.2.5-SNAPSHOT`
- **Java Requirement:** 17+
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Supported Spring Boot Versions

| Version | Status |
|---------|--------|
| 3.0.13  | ✅ Supported |
| 3.1.12  | ✅ Supported |
| 3.2.12  | ✅ Supported |
| 3.3.13  | ✅ Supported |
| 3.4.11  | ✅ Supported |
| 3.5.7   | ✅ Supported (Default) |
| 4.0.0-RC2 | ✅ Supported |

---

## Modules

### microsphere-spring-boot-core

Core Spring Boot extensions including enhanced auto-configuration filters, configuration properties binding infrastructure, environment and property source management, diagnostics, conditional annotations, and general-purpose utilities.

### microsphere-spring-boot-actuator

Actuator extensions for monitoring and management. Provides custom actuator endpoints for inspecting artifacts, configuration metadata, and configuration properties at runtime, as well as monitored thread pool scheduling.

---

## Getting Started

### Maven BOM (Recommended)

Add the BOM to your `<dependencyManagement>` section:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>0.2.5-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Core Module

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-core</artifactId>
</dependency>
```

### Actuator Module

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-actuator</artifactId>
</dependency>
```

---

## Table of Contents

### Auto-Configuration

- [ConfigurableAutoConfigurationImportFilter](ConfigurableAutoConfigurationImportFilter.md) — Filter for excluding auto-configuration classes via property

### Class Loading

- [BannedArtifactClassLoadingListener](BannedArtifactClassLoadingListener.md) — Prevents loading of banned/duplicate artifacts

### Conditions

- [ConditionalOnPropertyPrefix](ConditionalOnPropertyPrefix.md) — Conditional annotation checking for property prefixes

### Event Listeners

- [OnceApplicationPreparedEventListener](OnceApplicationPreparedEventListener.md) — Base class for one-time ApplicationPreparedEvent handling
- [OnceMainApplicationPreparedEventListener](OnceMainApplicationPreparedEventListener.md) — One-time listener for main application context only
- [LoggingApplicationPreparedEventListeners](LoggingApplicationPreparedEventListeners.md) — Logging listeners for application prepared events

### Configuration Properties Binding

- [BindableConfigurationBeanBinder](BindableConfigurationBeanBinder.md) — Binds configuration properties to beans via Spring Boot Binder
- [ConfigurationPropertiesBeanInfo](ConfigurationPropertiesBeanInfo.md) — Introspection information for @ConfigurationProperties beans
- [ListenableConfigurationPropertiesBindHandlerAdvisor](ListenableConfigurationPropertiesBindHandlerAdvisor.md) — Advisor chaining BindListener beans
- [BindListener](BindListener.md) — Interface and composite for property binding events
- [ConfigurationPropertiesBeanContext](ConfigurationPropertiesBeanContext.md) — Context managing @ConfigurationProperties bean binding
- [ConfigurationPropertiesBeanPropertyChangedEvent](ConfigurationPropertiesBeanPropertyChangedEvent.md) — Event published on property changes
- [EventPublishingConfigurationPropertiesBeanPropertyChangedListener](EventPublishingConfigurationPropertiesBeanPropertyChangedListener.md) — Publishes property change events
- [ListenableBindHandlerAdapter](ListenableBindHandlerAdapter.md) — Adapts BindListeners to BindHandler

### Binding Utilities

- [BindHandlerUtils](BindHandlerUtils.md) — Utilities for creating BindHandler instances
- [BindUtils](BindUtils.md) — Utilities for binding configuration properties

### Configuration Metadata

- [ConfigurationMetadataReader](ConfigurationMetadataReader.md) — Reads Spring Boot configuration metadata from classpath
- [ConfigurationMetadataRepository](ConfigurationMetadataRepository.md) — Repository for configuration metadata groups, properties, and hints

### Configuration Property Utilities

- [ConfigurationPropertyUtils](ConfigurationPropertyUtils.md) — Utilities for property name manipulation
- [ConfigurationPropertiesUtils](ConfigurationPropertiesUtils.md) — Utilities for @ConfigurationProperties annotation discovery

### Diagnostics

- [ArtifactsCollisionDiagnosis](ArtifactsCollisionDiagnosis.md) — Detection, exception, and failure analysis for artifact collisions

### Environment & Properties

- [DefaultPropertiesApplicationListener](DefaultPropertiesApplicationListener.md) — Loads and merges default properties
- [DefaultPropertiesPostProcessor](DefaultPropertiesPostProcessor.md) — SPI interface for customizing default properties
- [PropertySourceLoaders](PropertySourceLoaders.md) — Composite property source loader
- [SpringApplicationDefaultPropertiesPostProcessor](SpringApplicationDefaultPropertiesPostProcessor.md) — Loads default properties from classpath resources
- [OriginTrackedConfigurationPropertyInitializer](OriginTrackedConfigurationPropertyInitializer.md) — Adds origin tracking to property sources

### Run Listeners

- [SpringApplicationRunListeners](SpringApplicationRunListeners.md) — Adapter and implementations for SpringApplicationRunListener

### Condition Evaluation Reporting

- [ConditionEvaluationReport](ConditionEvaluationReport.md) — Building, initializing, listening, and reporting condition evaluations

### Utilities

- [SpringApplicationUtils](SpringApplicationUtils.md) — Utility methods for SpringApplication

### Constants

- [PropertyConstants (Core)](PropertyConstants-Core.md) — Core module property name constants
- [SpringBootPropertyConstants](SpringBootPropertyConstants.md) — Standard Spring Boot property name constants

### Actuator — Monitoring

- [MonitoredThreadPoolTaskScheduler](MonitoredThreadPoolTaskScheduler.md) — ThreadPoolTaskScheduler with Micrometer metrics

### Actuator — Auto-Configuration

- [ActuatorAutoConfiguration](ActuatorAutoConfiguration.md) — Auto-configuration for actuator task scheduler
- [ActuatorEndpointsAutoConfiguration](ActuatorEndpointsAutoConfiguration.md) — Auto-configuration for actuator endpoints

### Actuator — Conditions

- [ConditionalOnConfigurationProcessorPresent](ConditionalOnConfigurationProcessorPresent.md) — Conditional for spring-boot-configuration-processor

### Actuator — Endpoints

- [ArtifactsEndpoint](ArtifactsEndpoint.md) — Actuator endpoint for classpath artifacts
- [ConfigurationMetadataEndpoint](ConfigurationMetadataEndpoint.md) — Actuator endpoint for configuration metadata
- [ConfigurationPropertiesEndpoint](ConfigurationPropertiesEndpoint.md) — Actuator endpoint for configuration properties
- [WebEndpoints](WebEndpoints.md) — Aggregates all web endpoint read operations

### Actuator — Constants

- [PropertyConstants (Actuator)](PropertyConstants-Actuator.md) — Actuator module property name constants

---

## Contributing

Please see the [GitHub repository](https://github.com/microsphere-projects/microsphere-spring-boot) for contribution guidelines.

## License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
