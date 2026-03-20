# ListenableConfigurationPropertiesBindHandlerAdvisor

## Overview

`ListenableConfigurationPropertiesBindHandlerAdvisor` is a `ConfigurationPropertiesBindHandlerAdvisor` that wraps the standard `BindHandler` with listener support. It retrieves all `BindListener` beans from the `BeanFactory` and wraps the existing handler with a `ListenableBindHandlerAdapter`, enabling all registered listeners to observe property binding events.

## Package

`io.microsphere.spring.boot.context.properties`

## Since

`1.0.0`

## API Details

### Class: `ListenableConfigurationPropertiesBindHandlerAdvisor`

**Implements:** `ConfigurationPropertiesBindHandlerAdvisor`, `BeanFactoryAware`

### Methods

```java
public BindHandler apply(BindHandler bindHandler)
```
Wraps the given `BindHandler` with a `ListenableBindHandlerAdapter` containing all `BindListener` beans from the application context.

```java
public void setBeanFactory(BeanFactory beanFactory)
```
Injects the `BeanFactory` used to look up `BindListener` beans.

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

The advisor is typically auto-configured. Register `BindListener` beans to participate:

```java
@Component
public class MyBindListener implements BindListener {

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Object result) {
        System.out.println("Bound: " + name + " = " + result);
    }
}
```

The `ListenableConfigurationPropertiesBindHandlerAdvisor` automatically picks up this listener and applies it to all `@ConfigurationProperties` binding operations.

## Related Components

- [BindListener](BindListener.md) — The listener interface
- [ListenableBindHandlerAdapter](ListenableBindHandlerAdapter.md) — The adapter wrapping the BindHandler
- [EventPublishingConfigurationPropertiesBeanPropertyChangedListener](EventPublishingConfigurationPropertiesBeanPropertyChangedListener.md) — A concrete BindListener implementation
