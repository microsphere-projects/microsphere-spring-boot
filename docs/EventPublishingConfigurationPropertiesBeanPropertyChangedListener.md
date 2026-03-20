# EventPublishingConfigurationPropertiesBeanPropertyChangedListener

## Overview

`EventPublishingConfigurationPropertiesBeanPropertyChangedListener` is a `BindListener` that detects changes in `@ConfigurationProperties` bean properties and publishes `ConfigurationPropertiesBeanPropertyChangedEvent` events through the Spring application context.

It implements `BeanFactoryPostProcessor` to initialize `ConfigurationPropertiesBeanContext` instances for all `@ConfigurationProperties` beans, and `SmartInitializingSingleton` to mark binding as complete after all singletons are initialized.

## Package

`io.microsphere.spring.boot.context.properties.bind`

## Since

`1.0.0`

## API Details

### Class: `EventPublishingConfigurationPropertiesBeanPropertyChangedListener`

**Implements:** `BindListener`, `BeanFactoryPostProcessor`, `ApplicationContextAware`, `SmartInitializingSingleton`

### BindListener Methods

```java
public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)
```
Initializes the `ConfigurationPropertiesBeanContext` when binding starts for a `@ConfigurationProperties` bean.

```java
public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
```
Sets the property value in the context, which triggers event publishing if the value changed.

### Lifecycle Methods

```java
public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
```
Initializes `ConfigurationPropertiesBeanContext` instances for all registered `@ConfigurationProperties` beans.

```java
public void afterSingletonsInstantiated()
```
Marks binding as complete.

```java
public boolean isBound()
```
Returns whether initial binding has completed.

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

Register as a Spring bean to enable automatic property change detection:

```java
@Configuration
public class PropertyChangeConfig {

    @Bean
    public EventPublishingConfigurationPropertiesBeanPropertyChangedListener propertyChangeListener() {
        return new EventPublishingConfigurationPropertiesBeanPropertyChangedListener();
    }
}
```

Then listen for changes:

```java
@Component
public class PropertyChangeHandler {

    @EventListener
    public void handle(ConfigurationPropertiesBeanPropertyChangedEvent event) {
        log.info("Config changed: {} = {}", event.getPropertyName(), event.getNewValue());
    }
}
```

## Related Components

- [BindListener](BindListener.md) — Interface this class implements
- [ConfigurationPropertiesBeanContext](ConfigurationPropertiesBeanContext.md) — Manages per-bean binding context
- [ConfigurationPropertiesBeanPropertyChangedEvent](ConfigurationPropertiesBeanPropertyChangedEvent.md) — Events published
- [ConfigurationPropertiesBeanInfo](ConfigurationPropertiesBeanInfo.md) — Bean introspection
