# OnceMainApplicationPreparedEventListener

## Overview

`OnceMainApplicationPreparedEventListener` extends `OnceApplicationPreparedEventListener` to only process the main application context, ignoring bootstrap contexts. This is especially important in Spring Cloud environments where a bootstrap `ApplicationContext` is created before the main application context.

The listener detects bootstrap contexts by checking for the presence of `BootstrapApplicationListener` in the classpath and comparing the context ID against the configured bootstrap context ID.

## Package

`io.microsphere.spring.boot.context`

## Since

`1.0.0`

## API Details

### Class: `OnceMainApplicationPreparedEventListener`

**Extends:** `OnceApplicationPreparedEventListener`

### Constants

| Constant | Type | Value | Description |
|---|---|---|---|
| `BOOTSTRAP_APPLICATION_LISTENER_CLASS_NAME` | `String` | `"org.springframework.cloud.bootstrap.BootstrapApplicationListener"` | Spring Cloud bootstrap listener class |
| `BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME` | `String` | `"spring.cloud.bootstrap.name"` | Property for custom bootstrap context ID |
| `DEFAULT_BOOTSTRAP_CONTEXT_ID` | `String` | `"bootstrap"` | Default bootstrap context ID |

### Protected Methods

```java
protected boolean isIgnored(ConfigurableApplicationContext context)
```
Returns `true` if the context is a bootstrap context or not the main application context.

```java
protected boolean isBootstrapContext(ConfigurableApplicationContext context)
```
Checks if the given context is a Spring Cloud bootstrap context.

```java
protected boolean isMainApplicationContext(ConfigurableApplicationContext context)
```
Checks if the given context is the main application context (not bootstrap).

## Configuration Properties

N/A

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

```java
public class MyMainAppListener extends OnceMainApplicationPreparedEventListener {

    @Override
    protected void onApplicationEvent(SpringApplication app, String[] args, ConfigurableApplicationContext ctx) {
        // Only called for the main application context, never for bootstrap
        System.out.println("Main application prepared: " + ctx.getId());
    }
}
```

## Related Components

- [OnceApplicationPreparedEventListener](OnceApplicationPreparedEventListener.md) — Parent class
- [LoggingApplicationPreparedEventListeners](LoggingApplicationPreparedEventListeners.md) — Concrete logging implementations
