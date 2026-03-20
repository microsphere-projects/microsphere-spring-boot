# BannedArtifactClassLoadingListener

## Overview

`BannedArtifactClassLoadingListener` is a `SpringApplicationRunListener` that prevents loading of banned artifacts during application startup. When enabled, it checks the classpath for artifacts that have been deemed undesirable or conflicting and prevents them from being loaded.

The listener extends `SpringApplicationRunListenerAdapter` and executes at the highest precedence order. It uses a `ConcurrentMap` to track which `SpringApplication` instances have already been processed, ensuring the ban check runs only once per application.

## Package

`io.microsphere.spring.boot.classloading`

## Since

`1.0.0`

## API Details

### Class: `BannedArtifactClassLoadingListener`

**Extends:** `SpringApplicationRunListenerAdapter`

### Constructor

```java
public BannedArtifactClassLoadingListener(SpringApplication springApplication, String... args)
```
Initializes the listener with the highest precedence order.

### Key Methods

```java
public void starting()
```
Called when the application starts. Checks if banned artifact detection is enabled and bans artifacts if not already processed.

### Internal Logic

1. Checks if `microsphere.spring.boot.banned-artifacts.enabled` system property is `true`
2. If enabled and not yet processed for this `SpringApplication`, executes artifact banning
3. Marks the application as processed to prevent re-execution

## Configuration Properties

| Property | Type | Default | Source | Description |
|---|---|---|---|---|
| `microsphere.spring.boot.banned-artifacts.enabled` | `boolean` | `false` | System Properties | Enable/disable banned artifact detection |

## Version Compatibility

| Spring Boot Version | Compatible |
|---|---|
| 3.0.x | ✅ |
| 3.1.x | ✅ |
| 3.2.x | ✅ |
| 3.3.x | ✅ |
| 3.4.x | ✅ |
| 3.5.x | ✅ |
| 4.0.x | ✅ |

## Example Code

### Enable banned artifact detection

```bash
# Via JVM system property
java -Dmicrosphere.spring.boot.banned-artifacts.enabled=true -jar myapp.jar
```

### Registration

Registered via `META-INF/spring.factories`:

```properties
org.springframework.boot.SpringApplicationRunListener=\
  io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener
```

## Related Components

- [SpringApplicationRunListeners](SpringApplicationRunListeners.md) — Base adapter class
- [ArtifactsCollisionDiagnosis](ArtifactsCollisionDiagnosis.md) — Related artifact collision detection
- [PropertyConstants (Core)](PropertyConstants-Core.md) — Property name prefix definitions
