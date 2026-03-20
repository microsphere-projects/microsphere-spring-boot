# ConfigurationPropertiesUtils

## Overview

`ConfigurationPropertiesUtils` provides utility methods for discovering and working with `@ConfigurationProperties` annotations on beans and bindable targets.

## Package

`io.microsphere.spring.boot.context.properties.util`

## Since

`1.0.0`

## API Details

### Class: `ConfigurationPropertiesUtils`

**Type:** Abstract utility class (not instantiable)

### Constants

| Constant | Type | Value | Description |
|---|---|---|---|
| `CONFIGURATION_PROPERTIES_CLASS` | `Class<ConfigurationProperties>` | `ConfigurationProperties.class` | The annotation class |

### Static Methods

```java
public static ConfigurationProperties findConfigurationProperties(Bindable bindable)
```
Finds the `@ConfigurationProperties` annotation from the `Bindable` instance. First checks the bindable's annotations, then falls back to checking the resolved type's annotations.

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
import io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils;

Bindable<MyConfig> bindable = Bindable.of(MyConfig.class);
ConfigurationProperties annotation = ConfigurationPropertiesUtils.findConfigurationProperties(bindable);
if (annotation != null) {
    System.out.println("Prefix: " + annotation.prefix());
}
```

## Related Components

- [ConfigurationPropertiesBeanInfo](ConfigurationPropertiesBeanInfo.md) — Uses annotation discovery
- [BindUtils](BindUtils.md) — Checks for @ConfigurationProperties during binding
- [ConfigurationPropertyUtils](ConfigurationPropertyUtils.md) — Related property utilities
