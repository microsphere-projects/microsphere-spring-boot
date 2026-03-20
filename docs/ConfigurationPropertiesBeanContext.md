# ConfigurationPropertiesBeanContext

## Overview

`ConfigurationPropertiesBeanContext` manages the binding context for a `@ConfigurationProperties` bean. It tracks property values, detects changes during rebinding, and publishes `ConfigurationPropertiesBeanPropertyChangedEvent` events when property values change.

This class maintains a `BeanWrapperImpl` for the initialized bean and maps binding property names (dashed form) to Java bean property names for change detection.

## Package

`io.microsphere.spring.boot.context.properties.bind`

## Since

`1.0.0`

## API Details

### Class: `ConfigurationPropertiesBeanContext` (Package-private)

### Constructor

```java
ConfigurationPropertiesBeanContext(Class<?> beanClass, ConfigurationProperties annotation,
    String prefix, ConfigurableApplicationContext context)
```

### Methods

```java
public void setProperty(ConfigurationProperty property, Object newValue)
```
Sets a property value and publishes a `ConfigurationPropertiesBeanPropertyChangedEvent` if the value changed.

```java
public String getPrefix()
```
Gets the property prefix for this bean.

```java
public Class<?> getBeanClass()
```
Gets the bean class.

```java
public Object getPropertyValue(String name)
```
Gets the current value of a bean property.

```java
public Object getInitializedBean()
```
Gets the initialized bean instance.

```java
protected void initialize(Object bean)
```
Initializes the context with the actual bean instance and builds the binding property name map.

## Configuration Properties

N/A — Operates on arbitrary `@ConfigurationProperties` beans.

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

This class is used internally by the binding infrastructure. It is typically not used directly by application code. See [EventPublishingConfigurationPropertiesBeanPropertyChangedListener](EventPublishingConfigurationPropertiesBeanPropertyChangedListener.md) for how it's used.

## Related Components

- [ConfigurationPropertiesBeanPropertyChangedEvent](ConfigurationPropertiesBeanPropertyChangedEvent.md) — Events published by this context
- [ConfigurationPropertiesBeanInfo](ConfigurationPropertiesBeanInfo.md) — Bean introspection information
- [EventPublishingConfigurationPropertiesBeanPropertyChangedListener](EventPublishingConfigurationPropertiesBeanPropertyChangedListener.md) — Manages contexts
- [ConfigurationPropertyUtils](ConfigurationPropertyUtils.md) — Utility for dashed-form conversion
