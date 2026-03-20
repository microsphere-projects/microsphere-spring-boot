# ConfigurableAutoConfigurationImportFilter

## Overview

`ConfigurableAutoConfigurationImportFilter` is an implementation of Spring Boot's `AutoConfigurationImportFilter` that allows you to exclude specific auto-configuration classes from being loaded. Unlike Spring Boot's built-in `spring.autoconfigure.exclude` property, this filter uses the `microsphere.autoconfigure.exclude` property and supports both declarative (property-based) and programmatic exclusion of auto-configuration classes.

The filter runs at the earliest possible stage during auto-configuration import processing (order `HIGHEST_PRECEDENCE + 99`), making it more efficient than post-processing exclusions. It resolves property placeholders and supports both comma-delimited strings and indexed array syntax for specifying excluded classes.

Internally, the filter provides a custom `PropertySource` (`ExcludedAutoConfigurationClassPropertySource`) that is added to the environment with highest priority when classes are excluded programmatically, ensuring programmatic exclusions take precedence over other property sources.

## Package

`io.microsphere.spring.boot.autoconfigure`

## Since

`1.0.0`

## API Details

### Class: `ConfigurableAutoConfigurationImportFilter`

**Implements:** `AutoConfigurationImportFilter`, `EnvironmentAware`, `Ordered`

### Constants

| Constant | Type | Value | Description |
|---|---|---|---|
| `AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME` | `String` | `"microsphere.autoconfigure.exclude"` | Property name for specifying excluded auto-configuration classes |

### Key Methods

```java
public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata)
```
Filters auto-configuration classes based on the exclusion list. Returns a boolean array where `false` means the class at that index is excluded.

```java
public static Set<String> getExcludedAutoConfigurationClasses(Environment environment)
```
Gets the current set of excluded auto-configuration class names from the environment.

```java
public static void addExcludedAutoConfigurationClass(Environment environment, String className)
```
Programmatically adds a single auto-configuration class to the exclusion list.

```java
public static void addExcludedAutoConfigurationClasses(Environment environment, String className, String... otherClassNames)
```
Programmatically adds multiple auto-configuration classes to the exclusion list.

```java
public static void addExcludedAutoConfigurationClasses(Environment environment, Iterable<String> classNames)
```
Programmatically adds auto-configuration classes from an iterable to the exclusion list.

```java
public int getOrder()
```
Returns `Ordered.HIGHEST_PRECEDENCE + 99`.

### Inner Class: `ExcludedAutoConfigurationClassPropertySource`

A custom `PropertySource` that manages the set of excluded auto-configuration class names, ensuring programmatic exclusions are reflected in the environment.

## Configuration Properties

| Property | Type | Default | Description |
|---|---|---|---|
| `microsphere.autoconfigure.exclude` | `String[]` | — | Comma-separated or indexed list of auto-configuration class names to exclude |

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

### Property-based exclusion

```properties
# application.properties
microsphere.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

### Programmatic exclusion

```java
import io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter;
import org.springframework.core.env.ConfigurableEnvironment;

public class MyCustomizer {
    public void customize(ConfigurableEnvironment environment) {
        ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(
            environment,
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
        );
    }
}
```

### Registration

The filter is automatically loaded via `META-INF/spring.factories`:

```properties
org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
  io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter
```

## Related Components

- [PropertyConstants (Core)](PropertyConstants-Core.md) — Defines the property name prefix for Microsphere Spring Boot
- [SpringBootPropertyConstants](SpringBootPropertyConstants.md) — References the standard `spring.autoconfigure.exclude` property
