# ConfigurationPropertiesBeanInfo

## Overview

`ConfigurationPropertiesBeanInfo` encapsulates introspection information about a `@ConfigurationProperties`-annotated bean. It resolves the bean class, its `@ConfigurationProperties` annotation, the property prefix, and all `PropertyDescriptor` instances for the bean's properties.

This class is used internally by the binding infrastructure to understand the structure of configuration properties beans and their mappings.

## Package

`io.microsphere.spring.boot.context.properties`

## Since

`1.0.0`

## API Details

### Class: `ConfigurationPropertiesBeanInfo`

### Constructors

```java
public ConfigurationPropertiesBeanInfo(Class<?> beanClass)
```
Creates an info object from the bean class, automatically discovering the `@ConfigurationProperties` annotation and prefix.

```java
public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation)
```
Creates an info object with an explicit annotation.

```java
public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation, String prefix)
```
Full constructor with explicit annotation and prefix.

### Methods

```java
public Class<?> getBeanClass()
public ConfigurationProperties getAnnotation()
public String getPrefix()
public List<PropertyDescriptor> getPropertyDescriptors()
public PropertyDescriptor getPropertyDescriptor(String name)
```

### Fields

| Field | Type | Annotation | Description |
|---|---|---|---|
| `beanClass` | `Class<?>` | `@Nonnull` | The bean class |
| `annotation` | `ConfigurationProperties` | `@Nonnull` | The @ConfigurationProperties annotation |
| `prefix` | `String` | `@Nonnull` | The property prefix |
| `propertyDescriptors` | `PropertyDescriptor[]` | `@Nonnull` | Bean property descriptors |

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
import io.microsphere.spring.boot.context.properties.ConfigurationPropertiesBeanInfo;

@ConfigurationProperties(prefix = "myapp.server")
public class ServerConfig {
    private int port;
    private String host;
    // getters and setters
}

ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(ServerConfig.class);
System.out.println(info.getPrefix());  // "myapp.server"
info.getPropertyDescriptors().forEach(pd ->
    System.out.println(pd.getName())   // "port", "host"
);
```

## Related Components

- [ConfigurationPropertiesBeanContext](ConfigurationPropertiesBeanContext.md) — Uses bean info for binding context
- [ConfigurationPropertiesUtils](ConfigurationPropertiesUtils.md) — Utility for finding @ConfigurationProperties annotations
- [BindableConfigurationBeanBinder](BindableConfigurationBeanBinder.md) — Binds properties to beans
