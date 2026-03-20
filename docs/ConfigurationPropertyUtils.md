# ConfigurationPropertyUtils

## Overview

`ConfigurationPropertyUtils` provides utility methods for working with `ConfigurationPropertyName` objects. It includes methods for extracting property prefixes based on binding depth and converting Java bean property names to their dashed (kebab-case) form.

## Package

`io.microsphere.spring.boot.context.properties.source.util`

## Since

`1.0.0`

## API Details

### Class: `ConfigurationPropertyUtils`

**Type:** Abstract utility class (not instantiable)

### Static Methods

```java
public static String getPrefix(ConfigurationPropertyName name, BindContext context)
```
Extracts the property prefix based on the binding context depth. For a property like `spring.datasource.url` at depth 1, the prefix would be `spring.datasource`.

```java
public static String toDashedForm(String name)
```
Converts a Java bean property name to dashed (kebab-case) form.

**Examples:**
| Input | Output |
|---|---|
| `userName` | `user-name` |
| `maxPoolSize` | `max-pool-size` |
| `port` | `port` |

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
import io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils;

// Convert property names
String dashed = ConfigurationPropertyUtils.toDashedForm("maxPoolSize");
// Result: "max-pool-size"

String simple = ConfigurationPropertyUtils.toDashedForm("port");
// Result: "port"
```

## Related Components

- [ConfigurationPropertiesBeanContext](ConfigurationPropertiesBeanContext.md) — Uses dashed form conversion
- [ConfigurationPropertiesUtils](ConfigurationPropertiesUtils.md) — Related utility for @ConfigurationProperties
- [BindUtils](BindUtils.md) — Uses context depth checking
