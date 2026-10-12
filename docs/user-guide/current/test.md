# Test Module

> [Chinese](./zh/test.md) · [Documentation index](../README.md)

> **Branch:** `release` · **Version line:** `0.2.x` · **Artifact:** `microsphere-spring-boot-test`
> **Spring Boot:** `3.0.13` – `4.1.1` · **Java:** 17+ · **Suggested scope:** `test`

Contents:
[1 Overview](#1-module-overview) ·
[2 Version Information](#2-version-information) ·
[3 Class Hierarchy](#3-class-hierarchy) ·
[4 The Contract](#4-the-contract) ·
[5 Assertion Semantics](#5-assertion-semantics) ·
[6 Examples](#6-examples) ·
[7 Dependencies and FAQ](#7-dependencies-and-faq)

---

## 1. Module Overview

The module fixes three expectations — an auto-configuration applies, can be switched off, and is filtered out when its
required classes are missing — as **inherited** JUnit 5 tests. When you write an auto-configuration class, extending the
base class matching your web type, annotating `@SpringBootTest` and implementing the three `configure*` methods already
gives you three automated tests, without hand writing `ApplicationContextRunner` assertions each time. The module has
**no `src/main/resources`** at all (no `spring.factories`, no default-properties file); it is purely a test-support
library.

---

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-test` |
| Latest published | `0.2.34` |
| In development | `0.2.35-SNAPSHOT` |
| Spring Boot profile pinned | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16 (default) / 4.0.8 / 4.1.1` |
| Java | `17`; CI validates JDK `17 / 21 / 25` |
| JUnit | Jupiter BOM `6.1.3` (parent POM) |
| Test stack | JUnit 5 `org.junit.jupiter.api.Test` + Spring Boot's `ApplicationContextRunner` family; **no** `@ExtendWith`/`SpringExtension` |
| Dependency scope | All upstream dependencies are `<optional>`, supplied by the consumer (see §7) |

---

## 3. Class Hierarchy

Package: `io.microsphere.spring.boot.test`

| Class | Declaration | Runner | Applies to |
|---|---|---|---|
| `AbstractAutoConfigurationTest<A, R extends AbstractApplicationContextRunner>` | abstract base | decided by `R` | — |
| `AutoConfigurationTest<A>` | `extends AbstractAutoConfigurationTest<A, ApplicationContextRunner>` | `ApplicationContextRunner` | non-web auto-configurations |
| `WebAutoConfigurationTest<A>` | `…<A, WebApplicationContextRunner>` | `WebApplicationContextRunner` | Servlet / MVC |
| `ReactiveWebAutoConfigurationTest<A>` | `…<A, ReactiveWebApplicationContextRunner>` | `ReactiveWebApplicationContextRunner` | WebFlux |

**Type parameters**: `A` = the auto-configuration class under test, obtained by resolving the generic declaration
through reflection; `R` = the runner type, likewise resolved from the subclass declaration.

**Hard constraints** enforced in the constructor:

* A concrete test class **must** carry `@SpringBootTest`, otherwise construction fails with
  `The @SpringBootTest must not be annotated on …`.
* When `webEnvironment == NONE` (non-web), the runner **must** be `ApplicationContextRunner`.

---

## 4. The Contract

The base class reads its configuration off the `@SpringBootTest` annotation — this is the key to the module:

| Source | Use |
|---|---|
| `@SpringBootTest(properties = {...})` | `runner.withPropertyValues(...)`, i.e. the baseline properties for the test context |
| `@SpringBootTest(classes = {...})` | `runner.withConfiguration(of(classes))`, extra configuration classes that take part in the assembly |
| The type parameter `A` | `runner.withConfiguration(of(autoConfigurationClass))`, the class under test |

A subclass **must** implement three `protected abstract` methods, each receiving a set to fill:

```java
protected abstract void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses);
protected abstract void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues);
protected abstract void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses);
```

| Method | What goes in | Generated test |
|---|---|---|
| `configureAutoConfiguredClasses` | the bean classes that should exist besides the class under test (may stay empty) | `testAutoConfiguredClasses()` |
| `configureGlobalDisabledPropertyValues` | disable switches of the form `"key=false"`, verified one by one | `testOnGlobalDisabledProperty()` |
| `configureGlobalMissingClasses` | classes whose absence must make the auto-configuration not apply | `testOnGlobalMissingClass()` |

Other usable members: `newRunner()` (builds and initializes a fresh runner), `initRunner(R)`, `getPropertyValues()`,
`getClasses()`, `getAutoConfiguredClasses()` (which always includes the class under test), and the constant
`SPRING_BOOT_TEST_CLASS`.

---

## 5. Assertion Semantics

| Assertion | What it really asserts |
|---|---|
| `assertAutoConfiguredClass(Class<?>)` | `assertThat(context).hasSingleBean(clazz)` |
| `assertDisabledProperty(String propertyValue, Class<?>... beans)` | appends the property via `runner.withPropertyValues(propertyValue)`, then asserts `doesNotHaveBean` for every bean class |
| `assertFilteredClass(String className, Class<?>... beans)` | replaces `runner.withPropertyValues` with `withClassLoader(new FilteredClassLoader(className))`, then asserts `doesNotHaveBean` |

Each of the three instance methods coexists with a static generic variant of the same name
(`assertAutoConfiguredClass` / `assertDisabledProperty` / `assertFilteredClass`); the static signature is:

```java
public static <R extends AbstractApplicationContextRunner<R, C, A>,
               C extends ConfigurableApplicationContext,
               A extends ApplicationContextAssertProvider<C>>
void assertAutoConfiguredClass(R runner, Class<?> autoConfiguredClass)
```

---

## 6. Examples

### 6.1 Non-web auto-configuration

In the repository's own style:

```java
package com.example;

import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jmx.export.MBeanExporter;

import java.util.Set;

@SpringBootTest(
        classes = MyAutoConfigurationTest.class,   // usually the test class itself is listed in classes
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jmx.enabled=true"     // baseline property
)
public class JmxAutoConfigurationTest extends AutoConfigurationTest<JmxAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        // no extra bean classes expected
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("spring.jmx.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(MBeanExporter.class);
    }
}
```

Leaving `configureAutoConfiguredClasses` empty is valid: the base class always adds the type parameter `A` (the
auto-configuration under test) to the asserted set.

### 6.2 Servlet web auto-configuration

```java
@SpringBootTest(
        classes = WebMvcAutoConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,   // not NONE => treated as a web application
        properties = "microsphere.spring.boot.webmvc.enabled=true"
)
class WebMvcAutoConfigurationTest extends WebAutoConfigurationTest<WebMvcAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> classes) {
        classes.add(ContentCachingFilter.class);   // io.microsphere.spring.web.servlet.filter
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> values) {
        values.add("microsphere.spring.boot.webmvc.enabled=false");
        values.add("microsphere.spring.boot.webmvc.filter.enabled=false");
        values.add("microsphere.spring.boot.webmvc.logging.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> missing) {
        missing.add(jakarta.servlet.Servlet.class);
        missing.add(EnableWebMvcExtension.class);  // io.microsphere.spring.webmvc.annotation
    }
}
```

### 6.3 Reactive

```java
@SpringBootTest(
        classes = WebFluxAutoConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "microsphere.spring.boot.webflux.enabled=true"
)
class WebFluxAutoConfigurationTest extends ReactiveWebAutoConfigurationTest<WebFluxAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> classes) {
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> values) {
        values.add("microsphere.spring.boot.webflux.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> missing) {
        missing.add(reactor.core.publisher.Flux.class);
        missing.add(org.springframework.web.reactive.DispatcherHandler.class);
    }
}
```

### 6.4 Reuse the runner for your own assertions

```java
@SpringBootTest(classes = ActuatorAutoConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "microsphere.spring.boot.actuator.task-scheduler.pool-size=3")
class ActuatorAutoConfigurationTest extends AutoConfigurationTest<ActuatorAutoConfiguration> {

    @Test
    void schedulerPoolSizeIsApplied() {
        newRunner().withBean(org.springframework.context.ApplicationContext.class, () -> null)
                .run(context -> assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class));
    }

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> classes) {
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> values) {
        // actuatorTaskScheduler depends on MeterRegistry, so no property switch applies
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> missing) {
        missing.add(io.micrometer.core.instrument.MeterRegistry.class);
    }
}
```

---

## 7. Dependencies and FAQ

**Declaration** (the version is managed by the BOM):

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-test</artifactId>
    <scope>test</scope>
</dependency>
```

All upstream dependencies of this module are marked `<optional>true</optional>` — JUnit Jupiter,
`spring-boot-starter-test`, `spring-boot-starter-web`, `spring-boot-starter-webflux`, the `microsphere-spring-*` modules,
the servlet API, Jackson and H2 — so **you must supply the ones you need**.

| Symptom | Answer |
|---|---|
| Construction fails with `The @SpringBootTest must not be annotated on …` | The concrete test class is missing `@SpringBootTest`; add it (the message means exactly that). |
| Non-web test reports a runner type mismatch | With `webEnvironment = NONE` only `AutoConfigurationTest` (i.e. `ApplicationContextRunner`) is allowed. |
| `testOnGlobalMissingClass` fails | A class you put into `configureGlobalMissingClasses` is not a conditional dependency of that auto-configuration; `FilteredClassLoader` filters by **class name**, so a wrong class means no filtering at all. |
| `assertDisabledProperty` fails | That auto-configuration has no matching property switch (the actuator scheduler, for example, only depends on whether a bean exists); move the entry to `configureGlobalMissingClasses`. |
| You want finer control | Use `newRunner()` and assemble `withPropertyValues` / `withBean` / `withConfiguration` yourself. |

---

**Up:** [README.md](./README.md) · Related: [core.md](./core.md) (conditions and properties) ·
[actuator.md](./actuator.md) · [webmvc.md](./webmvc.md) · [webflux.md](./webflux.md)
