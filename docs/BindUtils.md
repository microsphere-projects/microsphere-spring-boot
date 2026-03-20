# BindUtils

## Overview

`BindUtils` provides utility methods for binding configuration properties with `BindListener` support. It includes methods to check binding context (whether at root level for `@ConfigurationProperties` beans or at nested property level) and methods to bind from various sources (environment, map) with listener support.

## Package

`io.microsphere.spring.boot.context.properties.bind.util`

## Since

`1.0.0`

## API Details

### Class: `BindUtils`

**Type:** Abstract utility class (not instantiable)

### Static Methods

```java
public static boolean isConfigurationPropertiesBean(Bindable<?> target, BindContext context)
```
Returns `true` if the target has `@ConfigurationProperties` and the binding context is at root level (depth 0).

```java
public static boolean isConfigurationPropertiesBean(BindContext context)
```
Returns `true` if the binding context depth is 0.

```java
public static boolean isBoundProperty(BindContext context)
```
Returns `true` if the binding context depth is greater than 0 (a nested property).

```java
public static <T> T bind(Environment environment, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners)
```
Binds properties from the Spring `Environment` to the target type.

```java
public static <T> T bind(Map<?, ?> properties, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners)
```
Binds properties from a `Map` to the target type.

```java
public static <T> T bind(Binder binder, String name, Class<T> targetType, BindListener... bindListeners)
```
Internal bind implementation using a `Binder` instance.

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
import io.microsphere.spring.boot.context.properties.bind.util.BindUtils;

// Bind from Environment
MyConfig config = BindUtils.bind(environment, "myapp", MyConfig.class);

// Bind from Map with listener
Map<String, Object> props = Map.of("myapp.name", "test", "myapp.port", "8080");
MyConfig config = BindUtils.bind(props, "myapp", MyConfig.class, new AuditBindListener());
```

## Related Components

- [BindHandlerUtils](BindHandlerUtils.md) — Complementary utility for BindHandler creation
- [BindListener](BindListener.md) — Listeners used with bind methods
- [ListenableBindHandlerAdapter](ListenableBindHandlerAdapter.md) — Used internally for listener support
- [ConfigurationPropertiesUtils](ConfigurationPropertiesUtils.md) — Finding @ConfigurationProperties annotations
