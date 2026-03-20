# BindListener

## Overview

`BindListener` is an interface for observing configuration property binding lifecycle events. It defines callback methods for each phase of the binding process: start, success, create, failure, and finish. All methods have default (no-op) implementations, allowing listeners to override only the events they care about.

`BindListeners` is a package-private composite implementation that delegates to multiple `BindListener` instances, following the composite pattern.

## Package

`io.microsphere.spring.boot.context.properties.bind`

## Since

`1.0.0`

## API Details

### Interface: `BindListener`

```java
default <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)
```
Called when binding starts for a property.

```java
default void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
```
Called when a property is successfully bound.

```java
default void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
```
Called when a new instance is created during binding.

```java
default void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)
```
Called when binding fails for a property.

```java
default void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
```
Called when binding finishes (regardless of success or failure).

### Class: `BindListeners` (Package-private)

**Implements:** `BindListener`

Composite implementation that iterates over an `Iterable<BindListener>` and delegates each callback to all registered listeners.

```java
BindListeners(Iterable<BindListener> listeners)
```

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
import io.microsphere.spring.boot.context.properties.bind.BindListener;

@Component
public class AuditBindListener implements BindListener {

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Object result) {
        log.info("Property bound: {} = {}", name, result);
    }

    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target,
                          BindContext context, Exception error) {
        log.warn("Property binding failed: {}", name, error);
    }
}
```

## Related Components

- [ListenableBindHandlerAdapter](ListenableBindHandlerAdapter.md) — Adapts BindListeners to Spring Boot's BindHandler
- [ListenableConfigurationPropertiesBindHandlerAdvisor](ListenableConfigurationPropertiesBindHandlerAdvisor.md) — Advisor that applies BindListeners
- [EventPublishingConfigurationPropertiesBeanPropertyChangedListener](EventPublishingConfigurationPropertiesBeanPropertyChangedListener.md) — A concrete BindListener
- [BindUtils](BindUtils.md) — Utility methods accepting BindListeners
