# BindHandlerUtils

## Overview

`BindHandlerUtils` is a utility class for creating `BindHandler` instances with specific error-handling behaviors. It provides a factory method that creates an appropriate chain of bind handlers based on whether unknown fields and invalid fields should be ignored.

## Package

`io.microsphere.spring.boot.context.properties.bind.util`

## Since

`1.0.0`

## API Details

### Class: `BindHandlerUtils`

**Type:** Abstract utility class (not instantiable)

### Static Methods

```java
public static BindHandler createBindHandler(boolean ignoreUnknownFields, boolean ignoreInvalidFields)
```

Creates a `BindHandler` with the specified behavior:

| `ignoreUnknownFields` | `ignoreInvalidFields` | Result |
|---|---|---|
| `true` | `true` | `IgnoreErrorsBindHandler` |
| `true` | `false` | `BindHandler.DEFAULT` |
| `false` | `true` | `IgnoreErrorsBindHandler` wrapped with `NoUnboundElementsBindHandler` |
| `false` | `false` | `NoUnboundElementsBindHandler` |

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
import io.microsphere.spring.boot.context.properties.bind.util.BindHandlerUtils;

// Strict binding — fail on unknown and invalid fields
BindHandler strict = BindHandlerUtils.createBindHandler(false, false);

// Lenient binding — ignore everything
BindHandler lenient = BindHandlerUtils.createBindHandler(true, true);

Binder binder = Binder.get(environment);
binder.bind("myapp", Bindable.of(MyConfig.class), strict);
```

## Related Components

- [BindableConfigurationBeanBinder](BindableConfigurationBeanBinder.md) — Uses this utility
- [BindUtils](BindUtils.md) — Additional binding utilities
- [ListenableBindHandlerAdapter](ListenableBindHandlerAdapter.md) — Another BindHandler implementation
