# ListenableBindHandlerAdapter

## Overview

`ListenableBindHandlerAdapter` is a `BindHandler` implementation that wraps another `BindHandler` and delegates binding lifecycle events to a collection of `BindListener` instances via a `BindListeners` composite. This adapter bridges Spring Boot's `BindHandler` API with the Microsphere `BindListener` interface.

## Package

`io.microsphere.spring.boot.context.properties.bind`

## Since

`1.0.0`

## API Details

### Class: `ListenableBindHandlerAdapter`

**Extends:** `AbstractBindHandler`

### Constructors

```java
public ListenableBindHandlerAdapter(Iterable<BindListener> bindListeners)
```
Creates an adapter with default parent handler.

```java
public ListenableBindHandlerAdapter(BindHandler parent, Iterable<BindListener> bindListeners)
```
Creates an adapter wrapping the given parent handler.

### Methods

All `BindHandler` lifecycle methods are overridden to delegate to `BindListeners`:

```java
public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context)
public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
public Object onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error)
public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result)
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
List<BindListener> listeners = List.of(new AuditBindListener(), new MetricsBindListener());
BindHandler handler = new ListenableBindHandlerAdapter(
    BindHandler.DEFAULT, listeners
);

Binder binder = Binder.get(environment);
binder.bind("myapp", Bindable.of(MyConfig.class), handler);
```

## Related Components

- [BindListener](BindListener.md) — The listener interface
- [ListenableConfigurationPropertiesBindHandlerAdvisor](ListenableConfigurationPropertiesBindHandlerAdvisor.md) — Uses this adapter
- [BindHandlerUtils](BindHandlerUtils.md) — Utility for creating BindHandler instances
