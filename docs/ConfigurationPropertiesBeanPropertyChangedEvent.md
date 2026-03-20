# ConfigurationPropertiesBeanPropertyChangedEvent

## Overview

`ConfigurationPropertiesBeanPropertyChangedEvent` is an application event published when a `@ConfigurationProperties` bean property value changes during binding. It extends `BeanPropertyChangedEvent` and adds access to the `ConfigurationProperty` that triggered the change.

This event enables reactive responses to configuration changes, such as refreshing caches, reconnecting to external services, or logging configuration updates.

## Package

`io.microsphere.spring.boot.context.properties.bind`

## Since

`1.0.0`

## API Details

### Class: `ConfigurationPropertiesBeanPropertyChangedEvent`

**Extends:** `BeanPropertyChangedEvent`

### Constructor

```java
public ConfigurationPropertiesBeanPropertyChangedEvent(Object bean, String propertyName,
    Object oldValue, Object newValue, ConfigurationProperty configurationProperty)
```

### Methods

```java
public ConfigurationProperty getConfigurationProperty()
```
Returns the `ConfigurationProperty` that caused this change event.

### Inherited Methods (from BeanPropertyChangedEvent)

- `getSource()` — Returns the bean instance
- `getPropertyName()` — Name of the changed property
- `getOldValue()` — Previous value
- `getNewValue()` — New value

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
@Component
public class ConfigChangeHandler {

    @EventListener
    public void onPropertyChanged(ConfigurationPropertiesBeanPropertyChangedEvent event) {
        System.out.printf("Property '%s' changed from '%s' to '%s'%n",
            event.getPropertyName(), event.getOldValue(), event.getNewValue());

        ConfigurationProperty cp = event.getConfigurationProperty();
        System.out.println("Configuration property: " + cp.getName());
    }
}
```

## Related Components

- [ConfigurationPropertiesBeanContext](ConfigurationPropertiesBeanContext.md) — Publishes these events
- [EventPublishingConfigurationPropertiesBeanPropertyChangedListener](EventPublishingConfigurationPropertiesBeanPropertyChangedListener.md) — Triggers event publishing
- [BindListener](BindListener.md) — Listener interface used to detect changes
