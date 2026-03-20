# ConditionalOnPropertyPrefix

## Overview

`@ConditionalOnPropertyPrefix` is a conditional annotation that checks whether properties with specified prefixes exist in the Spring environment. It provides a convenient way to conditionally enable beans or configurations based on the presence of entire property groups rather than individual properties.

The annotation delegates to `OnPropertyPrefixCondition`, which extends Spring Boot's `SpringBootCondition` to evaluate whether any properties matching the given prefixes are present in the environment.

## Package

`io.microsphere.spring.boot.condition`

## Since

`1.0.0`

## API Details

### Annotation: `@ConditionalOnPropertyPrefix`

| Attribute | Type | Description |
|---|---|---|
| `value` | `String[]` | Property prefix values to check. A dot (`.`) is automatically appended if not specified. |

**Target:** `TYPE`, `METHOD`
**Retention:** `RUNTIME`
**Meta-Annotation:** `@Conditional(OnPropertyPrefixCondition.class)`

### Class: `OnPropertyPrefixCondition`

**Extends:** `SpringBootCondition`

```java
public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata)
```
Evaluates whether any properties in the environment match the specified prefixes. Returns a match if at least one property is found for any of the specified prefixes.

## Configuration Properties

N/A — This annotation checks for the existence of arbitrary property prefixes.

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

### Single prefix

```java
@Configuration
@ConditionalOnPropertyPrefix("myapp.feature")
public class FeatureConfiguration {
    // This configuration is only loaded when properties
    // starting with "myapp.feature." exist in the environment
}
```

### Multiple prefixes

```java
@Configuration
@ConditionalOnPropertyPrefix({"feature.alpha", "feature.beta"})
public class MultiFeatureConfiguration {
    // Loaded when properties with either prefix exist
}
```

### On a bean method

```java
@Configuration
public class AppConfig {

    @Bean
    @ConditionalOnPropertyPrefix("myapp.cache")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
```

## Related Components

- [ConfigurableAutoConfigurationImportFilter](ConfigurableAutoConfigurationImportFilter.md) — Another conditional mechanism for auto-configuration
- [PropertyConstants (Core)](PropertyConstants-Core.md) — Microsphere property name conventions
