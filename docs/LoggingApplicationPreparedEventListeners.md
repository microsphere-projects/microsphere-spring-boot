# Logging Application Prepared Event Listeners

## Overview

This page covers two concrete logging implementations of the application prepared event listener pattern:

1. **`LoggingOnceApplicationPreparedEventListener`** — Logs when any application context is prepared (once per context)
2. **`LoggingOnceMainApplicationPreparedEventListener`** — Logs only when the main application context is prepared (ignoring bootstrap contexts)

Both listeners execute at `Ordered.LOWEST_PRECEDENCE` and respect the Microsphere logging level configuration.

## Package

`io.microsphere.spring.boot.context`

## Since

`1.0.0`

## API Details

### Class: `LoggingOnceApplicationPreparedEventListener`

**Extends:** `OnceApplicationPreparedEventListener`

```java
public LoggingOnceApplicationPreparedEventListener()
```
Sets order to `LOWEST_PRECEDENCE`.

```java
protected boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context)
```
Returns `true` if the Microsphere logging level is not at TRACE level, effectively skipping logging in production.

```java
protected void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context)
```
Logs the application prepared event details.

---

### Class: `LoggingOnceMainApplicationPreparedEventListener`

**Extends:** `OnceMainApplicationPreparedEventListener`

```java
public LoggingOnceMainApplicationPreparedEventListener()
```
Sets order to `LOWEST_PRECEDENCE`.

```java
protected void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context)
```
Logs the main application prepared event details.

## Configuration Properties

| Property | Type | Default | Description |
|---|---|---|---|
| `microsphere.spring.boot.logging.level` | `String` | `TRACE` | Logging level for Microsphere components |

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

Both listeners are automatically registered via `META-INF/spring.factories`:

```properties
org.springframework.context.ApplicationListener=\
  io.microsphere.spring.boot.context.LoggingOnceApplicationPreparedEventListener,\
  io.microsphere.spring.boot.context.LoggingOnceMainApplicationPreparedEventListener
```

No additional configuration is required. To see log output, ensure the logging level is set:

```properties
microsphere.spring.boot.logging.level=TRACE
```

## Related Components

- [OnceApplicationPreparedEventListener](OnceApplicationPreparedEventListener.md) — Base class for once-per-context listeners
- [OnceMainApplicationPreparedEventListener](OnceMainApplicationPreparedEventListener.md) — Base class filtering for main context
- [PropertyConstants (Core)](PropertyConstants-Core.md) — Defines the logging level property name
- [SpringApplicationUtils](SpringApplicationUtils.md) — Used for logging utility methods
