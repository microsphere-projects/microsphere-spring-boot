# Web MVC Module

> [中文](./zh/webmvc.md) · [Documentation index](../README.md)
>
> **Branch:** `release-1.x` · **Version line:** `0.1.x` · **Artifact:** `microsphere-spring-boot-webmvc`
> **Spring Boot:** `2.1.18.RELEASE` – `2.7.18` · **Java:** 8+ · Depends on `microsphere-spring-boot-core`

Same document for the `release` (0.2.x) branch: [../../current/webmvc.md](../current/webmvc.md)

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
[10 Examples from the test cases](#10-examples-from-the-test-cases) ·
[11 Property reference](#11-property-reference) ·
[12 FAQ](#12-faq)

---

## 1. Module Overview

On the `0.1.x` line this module contributes the same four things as on `release` — a content-caching filter, a
property-driven content negotiation configurer, an exclusive view resolver listener, and handler/render/resolver logging
— and gates each of them behind the identical property keys. What differs is **delivery**, not capability:

* The condition tests `javax.servlet.Servlet` instead of `jakarta.servlet.Servlet`.
* `@AutoConfigureAfter` lists **one** class name, because on this line there is only the Boot 2 package to order
  against.
* Auto-configuration is registered **twice**, in `spring.factories` and in `AutoConfiguration.imports`, so Boot 2.1–2.6
  and Boot 2.7 both pick it up.
* The upstream library is `microsphere-spring-webmvc` `0.1.39`, whose class names and property namespaces match the
  `0.2.39` artifact exactly — the same FQCNs, the `javax.servlet` namespace underneath.

Base package: `io.microsphere.spring.boot.webmvc` → `.autoconfigure`, `.autoconfigure.condition`, `.constants`.

---

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webmvc` |
| Latest published on the `0.1.x` line | `0.1.34` |
| In development on `release-1.x` | `0.1.35-SNAPSHOT` |
| Pinned Spring Boot per profile | `2.1.18.RELEASE / 2.2.13.RELEASE / 2.3.12.RELEASE / 2.4.13 / 2.5.15 / 2.6.15 / 2.7.18` (default) |
| Java | `8` source level; CI verifies JDK `8 / 11 / 17 / 21 / 25` × 7 Boot profiles |
| Compile dependency | `microsphere-spring-boot-core` (`${revision}`), `microsphere-spring-webmvc` (`0.1.39`, BOM-managed) |
| Optional dependency | `spring-boot-starter-web` — your application must provide the web starter itself |
| Servlet namespace | `javax.servlet` |
| Registration | `META-INF/spring.factories` (`EnableAutoConfiguration`) **and** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webmvc</artifactId>
</dependency>
```

Take the dependency through the `0.1.x` BOM or with `microsphere-spring-boot-parent` as your parent POM. Do not mix
this artifact with a Boot 3 or Boot 4 classpath; see [../../current/webmvc.md](../current/webmvc.md) for that line.

---

## 3. Auto-Configuration and Its Ordering

`io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` carries:

```java
@ConditionalOnWebMvcAvailable
@AutoConfigureAfter(name = {
        // Spring Boot API
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
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

The `name` attribute is still used rather than a class reference, so the ordering is resolved lazily and a renamed or
absent target degrades to "no ordering" instead of a `ClassNotFoundException`. On this line only one name is listed;
the `release` branch adds `org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` for Boot 4.x. That
one extra array entry — plus the `javax` → `jakarta` swap in the condition — is the **entire** source difference
between the two branches for this module.

Registration is duplicated deliberately:

```properties
# META-INF/spring.factories (Boot 2.1 – 2.6, still read by 2.7)
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
```

```
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (Boot 2.7+)
io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
```

On Boot 2.7 both files are read and the auto-configuration is deduplicated, so declaring it twice is safe; on Boot
2.1–2.6 only `spring.factories` is understood, and on Boot 3+ only `imports` is.

---

## 4. Beans and Property Switches

| Bean method | Return type | Gate property | Default |
|---|---|---|---|
| `contentCachingFilter()` | `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` | `microsphere.spring.boot.webmvc.filter.enabled` | on (`matchIfMissing = true`) |
| `contentNegotiationManagerWebMvcConfigurer()` | `io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer` | `microsphere.spring.webmvc.content-negotiation.enabled` | on (`matchIfMissing = true`) |
| `exclusiveViewResolverApplicationListener()` | `io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener` | `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | off (no `matchIfMissing`; the key must hold a value) |
| `LoggingConfiguration` (nested static class) | imports `LoggingMethodHandlerInterceptor`, `LoggingPageRenderContextHandlerInterceptor`, `LoggingHandlerMethodArgumentResolverAdvice` | `microsphere.spring.boot.webmvc.logging.enabled` | on (`matchIfMissing = true`) |

The module-level switch is `microsphere.spring.boot.webmvc.enabled`, evaluated by `ConditionalOnWebMvcAvailable` with
`matchIfMissing = true`.

```properties
# application.properties
microsphere.spring.boot.webmvc.enabled=true
microsphere.spring.boot.webmvc.filter.enabled=false   # drop the ContentCachingFilter only
microsphere.spring.webmvc.content-negotiation.enabled=false
microsphere.spring.boot.webmvc.logging.enabled=true   # keep handler-method logging
```

> **Two namespaces, deliberately.** Keys under `microsphere.spring.boot.webmvc.` switch **this module's** beans; keys
> under `microsphere.spring.webmvc.` (without `boot`) configure the upstream library, including content negotiation and
> the exclusive view resolver. The module's own tests set `microsphere.spring.webmvc.filter.enabled`, but that key does
> **not** gate the `ContentCachingFilter` bean — the gate is `microsphere.spring.boot.webmvc.filter.enabled`.

> **Constant-name typo.** Several Java constants are spelled `ENALBED`, for example
> `MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME`. The **property keys are correct**; only the constant names
> carry the typo, so import the exact names from
> `io.microsphere.spring.boot.webmvc.constants.PropertyConstants` when you reference them statically.

---

## 5. The Upstream Web MVC Extension

`@EnableWebMvcExtension` (from `io.microsphere.spring.webmvc.annotation`, in `microsphere-spring-webmvc` `0.1.39`) is
the annotation this auto-configuration applies for you. Its attributes and defaults are the same as on the `0.2.x` line:

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
| `reversedProxyHandlerMapping` | `false` → **`true` here** | Registers `ReversedProxyHandlerMapping` for proxied deployments |

The annotation is meta-annotated with `@OverrideAnnotationAttributes` on this line too, so any attribute value can be
overridden from configuration through the prefix `microsphere.spring.@EnableWebMvcExtension.`:

```properties
microsphere.spring.@EnableWebMvcExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebMvcExtension.storeResponseBodyReturnValue=true
```

A per-annotation custom prefix works as well:
`microsphere.spring.prefix.<annotation fully qualified class name>`.

---

## 6. Availability Condition

`io.microsphere.spring.boot.webmvc.autoconfigure.condition.ConditionalOnWebMvcAvailable` requires all three of:

```java
@ConditionalOnWebApplication(type = SERVLET)                       // a servlet web application
@ConditionalOnClass(name = {                                       // and every one of these classes
        "javax.servlet.Servlet",                                   // Servlet API
        "org.springframework.context.ApplicationContext",
        "org.springframework.web.method.HandlerMethod",
        "org.springframework.web.servlet.DispatcherServlet",
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor",
        "io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension"
})
@ConditionalOnProperty(name = "microsphere.spring.boot.webmvc.enabled",
        matchIfMissing = true)                                     // enabled unless switched off
```

The single meaningful difference from `release` is `javax.servlet.Servlet`. Practical consequences:

* On a Boot 3/4 application the condition does not match, because `javax.servlet.Servlet` is gone — the module
  auto-configuration simply disappears. That is a safety net, not a migration path: use the `0.2.x` line there.
* On a non-web application, `@ConditionalOnWebApplication(SERVLET)` does not match.
* Because `@ConditionalOnClass` is declared by **name**, a missing class degrades to "no match" rather than
  `NoClassDefFoundError`.

```bash
# the conditions report should show WebMvcAutoConfiguration under matchSessions
curl -s http://localhost:8080/actuator/conditions | jq '.matchSessions | keys[]' | grep -i webmvc
```

On Boot 2.x the report is at `/actuator/conditions` only if `management.endpoint.conditions.enabled=true`; the
library's `endpoints.properties` turns it off by default, as described in the actuator module guide.

---

## 7. Configurable Content Negotiation

`ConfigurableContentNegotiationManagerWebMvcConfigurer` implements `WebMvcConfigurer.configureContentNegotiation`. It
reaches into the `ContentNegotiationConfigurer`'s `ContentNegotiationManagerFactoryBean` field and binds every property
under `microsphere.spring.webmvc.content-negotiation.` onto that factory bean with a Spring `DataBinder`.

* Any `ContentNegotiationManagerFactoryBean` setter is reachable by key, so a new option needs no code change.
* `mediaTypes` accepts a JSON-object literal, converted through a dedicated `PropertyEditor`.
* `contentNegotiationManager` and `servletContext` are disallowed binding targets.

```properties
microsphere.spring.webmvc.content-negotiation.enabled=true
microsphere.spring.webmvc.content-negotiation.favorParameter=true
microsphere.spring.webmvc.content-negotiation.parameterName=p
microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=true
microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=true
```

> On Boot 2.1–2.3, `favorPathExtension` still functions; from Boot 2.6 onward Spring MVC itself ignores path extensions
> unless you opt back in, so a parameter-based strategy is the portable choice on this line.

When the gate is off, Boot's own manager remains with a single `HeaderContentNegotiationStrategy` — which is what this
module's all-disabled test asserts.

---

## 8. Exclusive View Resolver

`ExclusiveViewResolverApplicationListener` is an `ApplicationListener<ContextRefreshedEvent>` and `EnvironmentAware`, and
its `@ConditionalOnProperty` has no `matchIfMissing`, so the bean exists only when the key has a value:

```properties
microsphere.spring.webmvc.view-resolver.exclusive-bean-name=viewResolver
```

On context refresh it keeps the named `ViewResolver` and demotes the others — the fix for "two resolvers both claim the
view" when a template engine and an `InternalResourceViewResolver` coexist. Without the property the listener is absent
and Boot's resolver ordering is untouched; the plain `WebMvcAutoConfigurationTest` asserts `assertNull(listener)` for
that state.

---

## 9. Handler Method Logging

The nested `WebMvcAutoConfiguration.LoggingConfiguration` imports three upstream components:

| Imported class | Package | Observes |
|---|---|---|
| `LoggingMethodHandlerInterceptor` | `io.microsphere.spring.webmvc.interceptor` | Handler method invocation and its arguments |
| `LoggingPageRenderContextHandlerInterceptor` | `io.microsphere.spring.webmvc.interceptor` | Model/view state at render time |
| `LoggingHandlerMethodArgumentResolverAdvice` | `io.microsphere.spring.webmvc.method.support` | `HandlerMethodArgumentResolver` calls |

The gate is `microsphere.spring.boot.webmvc.logging.enabled` (`matchIfMissing = true`). The beans exist even when the
loggers are silent; to see output, raise the levels:

```yaml
logging:
  level:
    io.microsphere.spring.webmvc.interceptor: DEBUG
    io.microsphere.spring.webmvc.method.support: DEBUG
    io.microsphere.spring.web.servlet.filter: DEBUG
```

---

## 10. Examples From the Test Cases

The tests are the same four classes as on `release`, with two differences worth knowing before you copy them.

`AbstractWebMvcAutoConfigurationTest` on this line loads **only** `TestController`, and imports
`javax.servlet.ServletException`:

```java
@Disabled
@WebAppConfiguration
@SpringJUnitConfig(classes = {
        TestController.class           // Test Controller
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

    protected void testUser() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);
        String json = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/test/user")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(json));
    }
}
```

There is no `RouterFunctionTestConfig.class` in the context and no `testUpdatePerson()` step, both of which the
`release` branch added. If you port a test from `0.2.x` down, drop the `RouterFunction` fixture.

**Defaults.** `WebMvcAutoConfigurationTest` asserts the out-of-the-box shape with no properties at all:

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

**Everything on.** `WebMvcAutoConfigurationAllEnabledTest` uses the same ten properties as `release`, but asserts with a
Java 8-compatible cast instead of pattern matching, and pins an exact strategy count:

```java
void assertContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
    List<ContentNegotiationStrategy> strategies = contentNegotiationManager.getStrategies();
    assertEquals(2, strategies.size());

    ContentNegotiationStrategy strategy1 = strategies.get(0);
    ContentNegotiationStrategy strategy2 = strategies.get(1);

    assertTrue(strategy2 instanceof ParameterContentNegotiationStrategy);

    ParameterContentNegotiationStrategy parameterContentNegotiationStrategy = (ParameterContentNegotiationStrategy) strategy2;

    assertEquals("p", parameterContentNegotiationStrategy.getParameterName());
}
```

Two strategies, order-sensitive — that is a stricter assertion than the `release` version's loop, and it is the
difference that shows up first when you upgrade to `0.2.x`.

**Everything off.** `WebMvcAutoConfigurationAllDisabledTest` is identical to `release`:

```java
void assertContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
    List<ContentNegotiationStrategy> strategies = contentNegotiationManager.getStrategies();
    assertEquals(1, strategies.size());

    ContentNegotiationStrategy strategy = strategies.get(0);
    assertTrue(strategy instanceof HeaderContentNegotiationStrategy);
}
```

> The all-enabled and all-disabled tests set `microsphere.spring.webmvc.{filter,logging}.enabled`, while the bean gates
> read `microsphere.spring.boot.webmvc.{filter,logging}.enabled`. Neither test asserts
> `assertNull(contentCachingFilter)`, so the mismatch does not fail them — use the `boot`-qualified keys in your own
> configuration.

---

## 11. Property Reference

Prefix chain: `microsphere.` + `spring.` + `boot.` + `webmvc.` for this module's switches, and `microsphere.` +
`spring.` + `webmvc.` for the upstream library. Constants live in
`io.microsphere.spring.boot.webmvc.constants.PropertyConstants`, and `PropertyConstantsTest` pins each literal:

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
`@ConfigurationProperty(type = boolean.class, source = APPLICATION_SOURCE)`, which publishes it into
`spring-configuration-metadata.json`.

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
| Nothing registers on my Boot 3 application | Expected — the condition requires `javax.servlet.Servlet`. Use the `0.2.x` line; see [../../current/webmvc.md](../current/webmvc.md). |
| I want to remove everything this module adds | `microsphere.spring.boot.webmvc.enabled=false`; the module switch wins over the per-bean gates. |
| `microsphere.spring.webmvc.filter.enabled=false` had no effect | Wrong namespace for that key: the bean gate is `microsphere.spring.boot.webmvc.filter.enabled`. Only content negotiation and the view resolver legitimately sit without `boot`. |
| Content caching costs memory | `microsphere.spring.boot.webmvc.filter.enabled=false`. |
| No handler-method logs appear | Keep `microsphere.spring.boot.webmvc.logging.enabled=true` and raise `io.microsphere.spring.webmvc.interceptor` to `DEBUG`. |
| Path extensions are ignored | Boot 2.6+ does not favour path extensions by default; use `favorParameter=true` with a `parameterName`. |
| `@EnableWebMvcExtension` already on my own configuration class | Both imports coexist; property keys under `microsphere.spring.@EnableWebMvcExtension.` win over the annotation defaults. |
| My ported `0.2.x` test fails on `RouterFunctionTestConfig` | The `0.1.x` base test context does not include it; remove that fixture and the `testUpdatePerson()` call. |
| Auto-configuration reported twice in the report | `spring.factories` and `AutoConfiguration.imports` both register it on Boot 2.7; Boot deduplicates the class, so only one configuration applies. |

---

**Related:** [core.md](./core.md) · [actuator.md](./actuator.md) · [webflux.md](./webflux.md)
