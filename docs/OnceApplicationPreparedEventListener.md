# OnceApplicationPreparedEventListener

## Overview

`OnceApplicationPreparedEventListener` is an abstract base class that ensures an `ApplicationPreparedEvent` listener executes only once per application context. It tracks processed context IDs using both a static class-level map and an instance-level set, preventing duplicate processing when multiple listeners of the same type are registered.

This is particularly useful in Spring Cloud environments where multiple application contexts may be created (e.g., bootstrap context and main application context).

## Package

`io.microsphere.spring.boot.context`

## Since

`1.0.0`

## API Details

### Class: `OnceApplicationPreparedEventListener`

**Implements:** `ApplicationListener<ApplicationPreparedEvent>`, `Ordered`

### Abstract Methods

```java
protected abstract boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context)
```
Determines whether this event should be ignored (skipped).

```java
protected abstract void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context)
```
Handles the `ApplicationPreparedEvent` — called only once per context.

### Public Methods

```java
public void setOrder(int order)
public int getOrder()
```
Manages the execution order. Default is `Ordered.LOWEST_PRECEDENCE`.

### Internal Tracking

- **Class-level map:** `Map<Class<? extends ApplicationListener>, Set<String>>` — tracks processed context IDs per listener class across all instances
- **Instance-level set:** `Set<String>` — tracks processed context IDs for the specific listener instance
- A context is considered processed if either the class-level or instance-level set contains its ID

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
public class MyOnceListener extends OnceApplicationPreparedEventListener {

    public MyOnceListener() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected boolean isIgnored(SpringApplication app, String[] args, ConfigurableApplicationContext ctx) {
        return false; // Process all contexts
    }

    @Override
    protected void onApplicationEvent(SpringApplication app, String[] args, ConfigurableApplicationContext ctx) {
        System.out.println("Application prepared (once): " + ctx.getId());
    }
}
```

### Registration

```properties
# META-INF/spring.factories
org.springframework.context.ApplicationListener=\
  com.example.MyOnceListener
```

## Related Components

- [OnceMainApplicationPreparedEventListener](OnceMainApplicationPreparedEventListener.md) — Subclass filtering for main application context only
- [LoggingApplicationPreparedEventListeners](LoggingApplicationPreparedEventListeners.md) — Concrete logging implementations
