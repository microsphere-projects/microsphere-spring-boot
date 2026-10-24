# Web MVC Module

> [中文](./zh/webmvc.md) · [Documentation index](../README.md)
>
> **Branch:** `release` · **Version line:** `0.2.x` · **Artifact:** `microsphere-spring-boot-webmvc`
> **Spring Boot:** `3.0.13` – `4.1.1` · **Java:** 17+ · Depends on `microsphere-spring-boot-core`

Contents:
[1 Overview](#1-module-overview) ·
[2 Version information](#2-version-information) ·
[3 Auto-configuration](#3-auto-configuration-and-its-ordering) ·
[4 Beans and switches](#4-beans-and-property-switches) ·
[5 Web MVC extension](#5-the-upstream-web-mvc-extension) ·
[6 Availability condition](#6-availability-condition) ·
[7 Content negotiation](#7-configurable-content-negotiation) ·
[8 Exclusive view resolver](#8-exclusive-view-resolver) ·
[9 Logging](#9-handler-method-logging) ·
[10 Examples](#10-examples-from-the-test-cases) ·
[11 Property reference](#11-property-reference) ·
[12 FAQ](#12-faq)

---

## 1. Module Overview

This module wires Microsphere's Spring MVC extensions into Spring Boot's auto-configuration system. It contributes a
content-caching filter, a property-driven content negotiation configurer, an exclusive view resolver listener, and
logging interceptors for handler methods, page-render contexts and argument resolvers.

Three characteristics define how it behaves in an application:

* **Everything is property-gated.** Each bean sits behind a `@ConditionalOnProperty`, and the whole module sits behind
  one module-level switch.
* **It ships no default-properties file.** Only `core` and `actuator` carry `core.properties` / `endpoints.properties`,
  so Spring Boot's own defaults apply here unless you explicitly opt in.
* **The extension itself lives upstream.** This module is a Boot-facing adapter; the actual interceptors, filters and
  resolvers come from `microsphere-spring-webmvc`. If a class in this module is not registered, check whether the
  upstream `@EnableWebMvcExtension` attribute is the one you need to turn on.

Base package: `io.microsphere.spring.boot.webmvc` → `.autoconfigure`, `.autoconfigure.condition`, `.constants`.

---

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webmvc` |
| Latest published | `0.2.34` |
| In development on `release` | `0.2.35-SNAPSHOT` |
| Pinned Spring Boot per profile | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16` (default)`, 4.0.8 / 4.1.1` |
| Java | `17` source level; CI verifies JDK `17 / 21 / 25` |
| Compile dependency | `microsphere-spring-boot-core` (`${revision}`), `microsphere-spring-webmvc` (BOM-managed) |
| Optional dependency | `spring-boot-starter-web` — your application must provide the web starter itself |
| Servlet namespace | `jakarta.servlet` |
| Registration | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webmvc</artifactId>
</dependency>
```

Import the BOM (see [dependencies.md](./dependencies.md)) or use `microsphere-spring-boot-parent` so no version is
needed on this declaration.

---

## 3. Auto-Configuration and Its Ordering

`io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` is registered in
`AutoConfiguration.imports` and carries four class-level annotations:

```java
@ConditionalOnWebMvcAvailable
@AutoConfigureAfter(name = {
        // Spring Boot API [2.0, 4.0)
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
        // Spring Boot API [4.0,)
        "org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration"
})
@EnableWebMvcExtension(
        registerHandlerInterceptors = true,
        reversedProxyHandlerMapping = true
)
@Import(value = {
        WebMvcAutoConfiguration.LoggingConfiguration.class
})
public class WebMvcAutoConfiguration {
}
```

The ordering is declared **by class name, not by class reference**. That single line is what makes one artifact serve
both Boot generations: Spring Boot 3.x and 4.x moved `WebMvcAutoConfiguration` to a different package, and an
annotation that references a class would fail to compile or to resolve on the other side. Listing both names keeps the
module buildable under every profile from `-Pspring-boot-3.0` to `-Pspring-boot-4.1` without the unpublished
`compatible` shim.

`@EnableWebMvcExtension` on the auto-configuration class activates the upstream extension with two attributes flipped
to `true` compared to the annotation defaults: `registerHandlerInterceptors` (so `HandlerInterceptor` beans are added
to the `InterceptorRegistry`) and `reversedProxyHandlerMapping` (so handler mapping respects the original host and
scheme behind a gateway or reverse proxy).

---

## 4. Beans and Property Switches

| Bean method | Return type | Gate property | Default |
|---|---|---|---|
| `contentCachingFilter()` | `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` | `microsphere.spring.boot.webmvc.filter.enabled` | on (`matchIfMissing = true`) |
| `contentNegotiationManagerWebMvcConfigurer()` | `io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer` | `microsphere.spring.webmvc.content-negotiation.enabled` | on (`matchIfMissing = true`) |
| `exclusiveViewResolverApplicationListener()` | `io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener` | `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | off (no `matchIfMissing`; the key must hold a value) |
| `LoggingConfiguration` (nested static class) | imports `LoggingMethodHandlerInterceptor`, `LoggingPageRenderContextHandlerInterceptor`, `LoggingHandlerMethodArgumentResolverAdvice` | `microsphere.spring.boot.webmvc.logging.enabled` | on (`matchIfMissing = true`) |

The module-level switch is `microsphere.spring.boot.webmvc.enabled`, evaluated by
`ConditionalOnWebMvcAvailable` with `matchIfMissing = true`. Setting it to `false` removes all four contributions at
once.

```properties
# application.properties
microsphere.spring.boot.webmvc.enabled=true
microsphere.spring.boot.webmvc.filter.enabled=false   # drop the ContentCachingFilter only
microsphere.spring.webmvc.content-negotiation.enabled=false
microsphere.spring.boot.webmvc.logging.enabled=true   # keep handler-method logging
```

> **Two namespaces, deliberately.** Keys under `microsphere.spring.boot.webmvc.` switch **this module's** beans; keys
> under `microsphere.spring.webmvc.` (without `boot`) configure the upstream library, including content negotiation and
> the exclusive view resolver. `grep`-ing for `microsphere.spring.webmvc.filter.enabled` will find it in this module's
> own tests, but that key does **not** gate the `ContentCachingFilter` bean — the gate is
> `microsphere.spring.boot.webmvc.filter.enabled`.

> **Constant-name typo.** Several Java constants are spelled `ENALBED`, for example
> `MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME`. The **property keys are correct**; only the constant names
> carry the typo, so import the exact names from `io.microsphere.spring.boot.webmvc.constants.PropertyConstants` when
> you reference them statically.

---

## 5. The Upstream Web MVC Extension

`@EnableWebMvcExtension` (from `io.microsphere.spring.webmvc.annotation`) is the annotation this auto-configuration
applies for you. Its attributes and defaults:

| Attribute | Default | Effect |
|---|---|---|
| `registerWebEndpointMappings` | `true` | Registers `WebEndpointMapping` instances resolved from Spring MVC and classical servlet endpoints |
| `interceptHandlerMethods` | `true` | Enables `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` around `HandlerMethod` execution |
| `publishEvents` | `true` | Publishes `HandlerMethodArgumentsResolvedEvent` and `WebEndpointMappingsReadyEvent` |
| `requestContextStrategy` | `DEFAULT` | Chooses where `RequestAttributes` are stored |
| `sources` | `BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER` | Where extension components are collected from |
| `registerHandlerInterceptors` | `false` → **`true` here** | Adds `HandlerInterceptor` beans to the `InterceptorRegistry`; makes `handlerInterceptors` ineffective |
| `handlerInterceptors` | `{}` | Explicit `HandlerInterceptor` types to register (ignored when the above is `true`) |
| `storeRequestBodyArgument` | `false` | Keeps the `@RequestBody` argument in `RequestAttributes` for later inspection |
| `storeResponseBodyReturnValue` | `false` | Keeps the `@ResponseBody` return value before it is written |
| `reversedProxyHandlerMapping` | `false` → **`true` here`** | Registers `ReversedProxyHandlerMapping` for proxied deployments |

Because `@EnableWebMvcExtension` is meta-annotated with `@OverrideAnnotationAttributes`, every one of those attribute
values can be overridden from configuration instead of by writing your own annotation. The default strategy
(`ConfigurationPropertyOverrideAnnotationAttributesStrategy`) reads the prefix
`microsphere.spring.@EnableWebMvcExtension.` and binds each attribute name onto it:

```properties
# override the annotation attributes the auto-configuration applied
microsphere.spring.@EnableWebMvcExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebMvcExtension.storeResponseBodyReturnValue=true
```

A custom prefix per annotation type is also supported through
`microsphere.spring.prefix.<annotation fully qualified class name>`.

---

## 6. Availability Condition

`io.microsphere.spring.boot.webmvc.autoconfigure.condition.ConditionalOnWebMvcAvailable` is a composite meta-annotation
that requires all three of:

```java
@ConditionalOnWebApplication(type = SERVLET)                       // a servlet web application
@ConditionalOnClass(name = {                                       // and every one of these classes
        "jakarta.servlet.Servlet",
        "org.springframework.context.ApplicationContext",
        "org.springframework.web.method.HandlerMethod",
        "org.springframework.web.servlet.DispatcherServlet",
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor",
        "io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension"
})
@ConditionalOnProperty(name = "microsphere.spring.boot.webmvc.enabled",
        matchIfMissing = true)                                     // enabled unless switched off
```

Adding the module to a reactive application is therefore harmless — the `SERVLET` requirement simply does not match, and
nothing is registered. `@ConditionalOnClass` is declared by **name**, so a missing Microsphere class degrades the
condition to "no match" instead of throwing `NoClassDefFoundError`.

```bash
# the conditions report should show WebMvcAutoConfiguration under matchSessions
curl -s http://localhost:8080/actuator/conditions | jq '.matchSessions | keys[]' | grep -i webmvc
```

This needs `management.endpoint.conditions.enabled=true`; the library's `endpoints.properties` turns it off by default,
as described in the actuator module guide.

---

## 7. Configurable Content Negotiation

`ConfigurableContentNegotiationManagerWebMvcConfigurer` implements `WebMvcConfigurer.configureContentNegotiation`. It
reaches into the `ContentNegotiationConfigurer`'s `ContentNegotiationManagerFactoryBean` field and binds every property
under `microsphere.spring.webmvc.content-negotiation.` onto that factory bean with a Spring `DataBinder`.

Consequences you can rely on:

* Any `ContentNegotiationManagerFactoryBean` setter is reachable by key, so no code change is needed to add an option.
* `mediaTypes` accepts a JSON-object literal, converted through a dedicated `PropertyEditor`.
* `contentNegotiationManager` and `servletContext` are disallowed binding targets, so a stray key cannot replace the
  manager or detach the factory bean from the servlet context.

```properties
microsphere.spring.webmvc.content-negotiation.enabled=true
microsphere.spring.webmvc.content-negotiation.favorParameter=true
microsphere.spring.webmvc.content-negotiation.parameterName=p
microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=true
microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=true
```

When the gate is off, Boot's own `ContentNegotiationManager` remains as configured by Boot, i.e. a single
`HeaderContentNegotiationStrategy` — which is exactly what this module's all-disabled test asserts.

---

## 8. Exclusive View Resolver

`ExclusiveViewResolverApplicationListener` is an `ApplicationListener<ContextRefreshedEvent>` and `EnvironmentAware`. It
is registered **only** when `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` has a value, because its
`@ConditionalOnProperty` has no `matchIfMissing`:

```properties
microsphere.spring.webmvc.view-resolver.exclusive-bean-name=viewResolver
```

On context refresh it keeps the named `ViewResolver` and demotes the others, which is the fix for the common "two
resolvers both claim the view" problem when a template engine and an `InternalResourceViewResolver` coexist. Without
the property the listener bean is absent and Boot's resolver ordering is untouched — the plain
`WebMvcAutoConfigurationTest` asserts `assertNull(listener)` for exactly that state.

---

## 9. Handler Method Logging

The nested `WebMvcAutoConfiguration.LoggingConfiguration` imports three upstream components:

| Imported class | Package | Observes |
|---|---|---|
| `LoggingMethodHandlerInterceptor` | `io.microsphere.spring.webmvc.interceptor` | Handler method invocation and its arguments |
| `LoggingPageRenderContextHandlerInterceptor` | `io.microsphere.spring.webmvc.interceptor` | Model/view state at render time |
| `LoggingHandlerMethodArgumentResolverAdvice` | `io.microsphere.spring.webmvc.method.support` | `HandlerMethodArgumentResolver` calls |

The gate is `microsphere.spring.boot.webmvc.logging.enabled` (`matchIfMissing = true`). The beans exist even when the
loggers are silent; to see output, raise the levels of the classes above:

```yaml
logging:
  level:
    io.microsphere.spring.webmvc.interceptor: DEBUG
    io.microsphere.spring.webmvc.method.support: DEBUG
    io.microsphere.spring.web.servlet.filter: DEBUG
```

---

## 10. Examples From the Test Cases

The module's own tests are the reference for both activation and assertion patterns. They are all built on
`AbstractWebMvcAutoConfigurationTest`, which is annotated `@Disabled` so it never runs on its own:

```java
@Disabled
@WebAppConfiguration
@SpringJUnitConfig(classes = {
        TestController.class,           // Test Controller
        RouterFunctionTestConfig.class  // Test RouterFunction
})
class AbstractWebMvcAutoConfigurationTest {

    @Autowired
    protected ConfigurableWebApplicationContext context;

    @Autowired
    protected TestController testController;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(this.context).build();
    }

    protected void testHelloWorld() throws Exception {
        this.mockMvc.perform(get("/test/helloworld"))
                .andExpect(status().isOk())
                .andExpect(content().string(this.testController.helloWorld()));
    }
}
```

**Defaults are active.** `WebMvcAutoConfigurationTest` adds no properties at all and asserts the out-of-the-box shape —
filter, configurer and logging configuration present, exclusive view resolver absent:

```java
@SpringBootTest(classes = { WebMvcAutoConfigurationTest.class })
@EnableAutoConfiguration
class WebMvcAutoConfigurationTest extends AbstractWebMvcAutoConfigurationTest {

    @Autowired
    private WebMvcExtensionConfiguration webMvcExtensionConfiguration;

    @Autowired
    private ContentCachingFilter contentCachingFilter;

    @Autowired
    private ConfigurableContentNegotiationManagerWebMvcConfigurer webMvcConfigurer;

    @Autowired
    private WebMvcAutoConfiguration.LoggingConfiguration loggingConfiguration;

    @Autowired(required = false)
    private ExclusiveViewResolverApplicationListener listener;

    @Test
    void test() throws Exception {
        super.testWebEndpoints();
        assertNotNull(this.webMvcExtensionConfiguration);
        assertNotNull(this.contentCachingFilter);
        assertNotNull(this.webMvcConfigurer);
        assertNotNull(this.loggingConfiguration);
        assertNull(this.listener);
    }
}
```

`WebMvcExtensionConfiguration` being injectable is the cheapest proof that `@EnableWebMvcExtension` took effect.

**Everything on.** `WebMvcAutoConfigurationAllEnabledTest` turns on the view resolver and a parameter-driven negotiation
strategy, then verifies the resulting strategy list:

```java
@SpringBootTest(classes = { WebMvcAutoConfigurationAllEnabledTest.class })
@TestPropertySource(properties = {
        "microsphere.spring.webmvc.view-resolver.exclusive-bean-name=viewResolver",
        "microsphere.spring.webmvc.content-negotiation.enabled=true",
        "microsphere.spring.webmvc.content-negotiation.favorParameter=true",
        "microsphere.spring.webmvc.content-negotiation.parameterName=p",
        "microsphere.spring.webmvc.content-negotiation.favorPathExtension=true",
        "microsphere.spring.webmvc.content-negotiation.ignoreUnknownPathExtensions=false",
        "microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=true",
        "microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=true",
        "microsphere.spring.webmvc.filter.enabled=true",
        "microsphere.spring.webmvc.logging.enabled=true"
})
@EnableAutoConfiguration
class WebMvcAutoConfigurationAllEnabledTest extends AbstractWebMvcAutoConfigurationTest {

    void assertContentNegotiationManager(ContentNegotiationManager manager) {
        List<ContentNegotiationStrategy> strategies = manager.getStrategies();
        assertFalse(strategies.isEmpty());
        for (ContentNegotiationStrategy strategy : strategies) {
            if (strategy instanceof ParameterContentNegotiationStrategy parameterStrategy) {
                assertEquals("p", parameterStrategy.getParameterName());
            }
        }
    }
}
```

**Everything off.** `WebMvcAutoConfigurationAllDisabledTest` is the counterpart; its assertion documents what "no
Microsphere contribution" looks like — exactly one strategy, and it is Boot's own:

```java
void assertContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
    List<ContentNegotiationStrategy> strategies = contentNegotiationManager.getStrategies();
    assertEquals(1, strategies.size());

    ContentNegotiationStrategy strategy = strategies.get(0);
    assertTrue(strategy instanceof HeaderContentNegotiationStrategy);
}
```

> The all-enabled and all-disabled tests set `microsphere.spring.webmvc.{filter,logging}.enabled`, while the bean gates
> in this module read `microsphere.spring.boot.webmvc.{filter,logging}.enabled`. Neither test asserts
> `assertNull(contentCachingFilter)`, so the mismatch does not fail them — use the `boot`-qualified keys in your own
> configuration.

---

## 11. Property Reference

Prefix chain: `microsphere.` + `spring.` + `boot.` + `webmvc.` for this module's switches, and `microsphere.` +
`spring.` + `webmvc.` for the upstream library. Constants live in
`io.microsphere.spring.boot.webmvc.constants.PropertyConstants`:

| Constant | Value |
|---|---|
| `MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webmvc.` |
| `MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME` | `microsphere.spring.boot.webmvc.enabled` |
| `FILTER_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webmvc.filter.` |
| `MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENABLED_PROPERTY_NAME` | `microsphere.spring.boot.webmvc.filter.enabled` |
| `LOGGING_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webmvc.logging.` |
| `MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME` | `microsphere.spring.boot.webmvc.logging.enabled` |
| `CONTENT_NEGOTIATION_PROPERTY_NAME_PREFIX` | `microsphere.spring.webmvc.content-negotiation.` |
| `MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME` | `microsphere.spring.webmvc.content-negotiation.enabled` |

`DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED`, `..._FILTER_ENABLED`, `..._LOGGING_ENABLED` and
`DEFAULT_MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED` are all the string `"true"`, and each key is annotated
`@ConfigurationProperty(type = boolean.class, source = APPLICATION_SOURCE)`, which is what publishes them into
`spring-configuration-metadata.json` for IDE completion.

| Property | Default | Effect |
|---|---|---|
| `microsphere.spring.boot.webmvc.enabled` | `true` (`matchIfMissing`) | Module switch; removes all contributions |
| `microsphere.spring.boot.webmvc.filter.enabled` | `true` (`matchIfMissing`) | `ContentCachingFilter` |
| `microsphere.spring.boot.webmvc.logging.enabled` | `true` (`matchIfMissing`) | Handler, render-context and resolver logging |
| `microsphere.spring.webmvc.content-negotiation.enabled` | `true` (`matchIfMissing`) | `ConfigurableContentNegotiationManagerWebMvcConfigurer` |
| `microsphere.spring.webmvc.content-negotiation.<setter>` | none | Bound onto `ContentNegotiationManagerFactoryBean` |
| `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | none | Registers `ExclusiveViewResolverApplicationListener` and names the resolver to keep |
| `microsphere.spring.@EnableWebMvcExtension.<attribute>` | none | Overrides an `@EnableWebMvcExtension` attribute value |

---

## 12. FAQ

| Symptom | Answer |
|---|---|
| No effect in a reactive application | By design — the condition requires `SERVLET`. Use [webflux.md](./webflux.md) instead. |
| I want to remove everything this module adds | `microsphere.spring.boot.webmvc.enabled=false`; the module switch wins over the per-bean gates. |
| `microsphere.spring.webmvc.content-negotiation.enabled` seems ignored | Wrong namespace is unlikely here — that key **is** correct (no `boot`). The key without `boot` for filter/logging, however, does nothing: use `microsphere.spring.boot.webmvc.filter.enabled`. |
| Content caching costs memory | `microsphere.spring.boot.webmvc.filter.enabled=false`. |
| No handler-method logs appear | Keep `microsphere.spring.boot.webmvc.logging.enabled=true` and raise `io.microsphere.spring.webmvc.interceptor` to `DEBUG`. |
| `@EnableWebMvcExtension` already on my own configuration class | Both imports coexist; the attribute values are resolved through the override strategy, so property keys under `microsphere.spring.@EnableWebMvcExtension.` win. |
| Missing `FilterRegistrationBean` ordering surprises | `ContentCachingFilter` is registered as a plain `OncePerRequestFilter` bean; use Boot's filter-order properties if you need it ahead of another filter. |
| `ClassNotFoundException` for `WebMvcAutoConfiguration` at runtime | Check you are on Boot 3/4 (`jakarta.servlet`). For Boot 2, use the `0.1.x` line documented under [../1.x/README.md](../1.x/README.md). |

---

**Related:** [core.md](./core.md) · [actuator.md](./actuator.md) · [webflux.md](./webflux.md)
