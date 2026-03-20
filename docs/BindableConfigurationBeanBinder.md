# BindableConfigurationBeanBinder

## Overview

`BindableConfigurationBeanBinder` is an implementation of `ConfigurationBeanBinder` that uses Spring Boot's `Binder` and `Bindable` API to bind configuration properties to Java beans. It provides a bridge between the Microsphere configuration bean binding framework and Spring Boot's native property binding infrastructure.

The binder supports configurable behavior for handling unknown fields and invalid fields through its `bind` method parameters, and uses `BindHandlerUtils` to create appropriate `BindHandler` instances.

## Package

`io.microsphere.spring.boot.context.config`

## Since

`1.0.0`

## API Details

### Class: `BindableConfigurationBeanBinder`

**Implements:** `ConfigurationBeanBinder`

### Methods

```java
public void setConversionService(ConversionService conversionService)
```
Sets the `ConversionService` to use for type conversion during binding.

```java
public void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields, boolean ignoreInvalidFields, Object configurationBean)
```
Binds the given configuration properties map to the target bean. Creates a `MapConfigurationPropertySource` from the properties, builds a `Binder`, and uses `Bindable.ofInstance()` to bind values to the bean with appropriate error handling behavior.

## Configuration Properties

N/A — This component binds arbitrary properties to beans.

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
import io.microsphere.spring.boot.context.config.BindableConfigurationBeanBinder;

BindableConfigurationBeanBinder binder = new BindableConfigurationBeanBinder();
binder.setConversionService(DefaultConversionService.getSharedInstance());

Map<String, Object> properties = Map.of(
    "server.port", "8080",
    "server.address", "0.0.0.0"
);

ServerProperties bean = new ServerProperties();
binder.bind(properties, true, true, bean);
// bean.getPort() == 8080
```

## Related Components

- [BindHandlerUtils](BindHandlerUtils.md) — Creates BindHandler instances used by this binder
- [BindUtils](BindUtils.md) — Additional binding utilities
- [ConfigurationPropertiesBeanInfo](ConfigurationPropertiesBeanInfo.md) — Introspection for @ConfigurationProperties beans
