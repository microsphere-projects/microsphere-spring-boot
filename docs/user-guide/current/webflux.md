# WebFlux Module

> [中文](./zh/webflux.md) · [Documentation index](../README.md)
>
> **Branch:** `release` · **Version line:** `0.2.x` · **Artifact:** `microsphere-spring-boot-webflux`
> **Spring Boot:** `3.0.13` – `4.1.1` · **Java:** 17+ · Depends on `microsphere-spring-boot-core`

Contents:
[1 Overview](#1-module-overview) ·
[2 Version information](#2-version-information) ·
[3 Auto-configuration](#3-auto-configuration-and-its-ordering) ·
[4 Availability condition](#4-availability-condition) ·
[5 WebFlux extension](#5-the-upstream-webflux-extension) ·
[6 Components it activates](#6-components-it-activates) ·
[7 Examples from the test cases](#7-examples-from-the-test-cases) ·
[8 Property reference](#8-property-reference) ·
[9 FAQ](#9-faq)

---

## 1. Module Overview

This is the reactive counterpart of `microsphere-spring-boot-webmvc`, and it is narrower on purpose: it registers
exactly one auto-configuration class that applies `@EnableWebFluxExtension(reversedProxyHandlerMapping = true)` and
orders itself after Spring Boot's own WebFlux auto-configuration.

Two facts explain almost every observation about this module:

* **It declares no `@Bean` methods.** Adding the dependency introduces no bean of this module's own — it activates the
  upstream extension, and every component that appears comes from `microsphere-spring-webflux`.
* **It ships no default-properties file.** Only `core` and `actuator` carry defaults, so nothing here changes Boot's
  behaviour unless you ask it to.

Base package: `io.microsphere.spring.boot.webflux` → `.autoconfigure`, `.autoconfigure.condition`, `.constants`.

---

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webflux` |
| Latest published | `0.2.34` |
| In development on `release` | `0.2.35-SNAPSHOT` |
| Pinned Spring Boot per profile | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16` (default)`, 4.0.8 / 4.1.1` |
| Java | `17` source level; CI verifies JDK `17 / 21 / 25` |
| Compile dependency | `microsphere-spring-boot-core` (`${revision}`), `microsphere-spring-webflux` (BOM-managed) |
| Optional dependency | `spring-boot-starter-webflux` — your application must provide it |
| Registering classes | `reactor.core.publisher.Flux`, `org.springframework.web.reactive.DispatcherHandler` |
| Registration | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webflux</artifactId>
</dependency>
```

---

## 3. Auto-Configuration and Its Ordering

`io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration`:

```java
@ConditionalOnWebFluxAvailable
@AutoConfigureAfter(name = {
        // Spring Boot API [2.0, 4.0)
        "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration",
        // Spring Boot API [4.0,)
        "org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration"
})
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
public class WebFluxAutoConfiguration {
}
```

The empty body is the point: the class contributes nothing itself, it only carries annotations. As in `webmvc`, the
ordering is declared **by class name strings**, because Spring Boot 3.x and 4.x place `WebFluxAutoConfiguration` in
different packages — that is what lets one artifact serve all eight profiles without the unpublished `compatible` shim.

`reversedProxyHandlerMapping = true` is the single attribute this module turns on by itself. It makes handler mapping
and URL computation respect the original host and scheme when the application sits behind a gateway or reverse proxy.

---

## 4. Availability Condition

`io.microsphere.spring.boot.webflux.autoconfigure.condition.ConditionalOnWebFluxAvailable` combines:

```java
@ConditionalOnWebApplication(type = REACTIVE)                      // a reactive web application
@ConditionalOnClass(name = {
        "reactor.core.publisher.Flux",                                        // Reactor API
        "org.springframework.context.ApplicationContext",                    // Spring Context API
        "org.springframework.web.method.HandlerMethod",                      // Spring Web API
        "org.springframework.web.reactive.DispatcherHandler",                // Spring WebFlux API
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor", // Microsphere Spring Web API
        "io.microsphere.spring.webflux.annotation.EnableWebFluxExtension"    // Microsphere Spring WebFlux API
})
@ConditionalOnProperty(name = "microsphere.spring.boot.webflux.enabled",
        matchIfMissing = true)                                     // enabled unless switched off
```

Adding the module to a servlet application is therefore harmless — the `REACTIVE` requirement does not match. Because
`@ConditionalOnClass` is declared by **name**, missing Microsphere classes degrade the condition to "no match" instead
of throwing `NoClassDefFoundError`.

```properties
# the module's only switch
microsphere.spring.boot.webflux.enabled=false
```

```bash
# when the module is switched off, expect it under noneMatches rather than matchSessions
curl -s http://localhost:8080/actuator/conditions | jq '.noneMatches | keys[]' | grep -i WebFlux
```

This needs `management.endpoint.conditions.enabled=true`; the library's `endpoints.properties` disables it by default,
as described in the actuator module guide.

---

## 5. The Upstream WebFlux Extension

`@EnableWebFluxExtension` (from `io.microsphere.spring.webflux.annotation`) is the annotation applied for you. Its
attributes:

| Attribute | Default | Effect |
|---|---|---|
| `registerWebEndpointMappings` | `true` | Registers `WebEndpointMapping` instances for reactive endpoints |
| `interceptHandlerMethods` | `true` | Enables `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` around `HandlerMethod` execution |
| `publishEvents` | `true` | Publishes `HandlerMethodArgumentsResolvedEvent` and `WebEndpointMappingsReadyEvent` |
| `sources` | `BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER` | Where extension components are collected from |
| `requestContextStrategy` | `DEFAULT` | Chooses where request-scoped attributes are stored |
| `storeRequestBodyArgument` | `false` | Keeps the `@RequestBody` argument for later inspection |
| `storeResponseBodyReturnValue` | `false` | Keeps the `@ResponseBody` return value before it is written |
| `reversedProxyHandlerMapping` | `false` → **`true` here** | Registers `ReversedProxyHandlerMapping` for proxied deployments |

There is no `registerHandlerInterceptors` / `handlerInterceptors` pair here: the reactive stack has no
`InterceptorRegistry`, so interception goes through `InterceptingHandlerMethodProcessor` instead. That difference is
the main asymmetry between this module and `webmvc`.

The annotation is also meta-annotated with `@OverrideAnnotationAttributes`, so each attribute can be overridden from
configuration using the prefix `microsphere.spring.@EnableWebFluxExtension.`:

```properties
microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebFluxExtension.storeResponseBodyReturnValue=true
```

A per-annotation custom prefix is supported through
`microsphere.spring.prefix.<annotation fully qualified class name>`.

---

## 6. Components It Activates

The components below come from `microsphere-spring-webflux`; the "Appears when" column is the practical detection rule,
taken from this module's own tests:

| Component | Package | Appears when |
|---|---|---|
| `HandlerMappingWebEndpointMappingResolver` | `io.microsphere.spring.webflux.metadata` | Always, once the module matches |
| `InterceptingHandlerMethodProcessor` | `io.microsphere.spring.webflux.method` | Always |
| `RequestHandledEventPublishingWebFilter` | `io.microsphere.spring.webflux.server.filter` | Always |
| `ReversedProxyHandlerMapping` | `io.microsphere.spring.webflux.handler` | Always (`reversedProxyHandlerMapping = true`) |
| `StoringRequestBodyArgumentInterceptor` | `io.microsphere.spring.webflux.method` | `storeRequestBodyArgument=true` |
| `StoringResponseBodyReturnValueInterceptor` | `io.microsphere.spring.webflux.method` | `storeResponseBodyReturnValue=true` |
| Everything above | — | `microsphere.spring.boot.webflux.enabled=false` removes all of it |

---

## 7. Examples From the Test Cases

`AbstractWebFluxAutoConfigurationTest` is the shared base, annotated `@Disabled` so it is never executed on its own. It
binds a `WebTestClient` to the context and asserts both that the endpoints respond and that a subclass-supplied set of
component classes is present:

```java
@Disabled
@WebAppConfiguration
@SpringJUnitConfig(classes = {
        TestController.class,           // Test Controller
        RouterFunctionTestConfig.class  // Test RouterFunction
})
abstract class AbstractWebFluxAutoConfigurationTest {

    @Autowired
    protected ConfigurableApplicationContext context;

    @Autowired
    protected TestController testController;

    protected WebTestClient webTestClient;

    @BeforeEach
    final void init() {
        this.webTestClient = buildWebTestClient(this.context);
    }

    @Test
    void test() {
        testWebEndpoints();

        Set<Class<?>> registeredComponentClasses = getRegisteredComponentClasses();
        for (Class<?> componentClass : registeredComponentClasses) {
            assertTrue(isBeanPresent(this.context, componentClass), () -> componentClass.getName() + " is not registered");
        }
    }

    abstract Set<Class<?>> getRegisteredComponentClasses();

    protected void testHelloWorld() {
        this.webTestClient.get()
                .uri("/test/helloworld")
                .header(ACCEPT_LANGUAGE, ENGLISH.getLanguage())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(this.testController.helloWorld());
    }

    protected WebTestClient buildWebTestClient(ConfigurableApplicationContext context) {
        return bindToApplicationContext(context).build();
    }
}
```

The `getRegisteredComponentClasses()` template method is the reusable idea to copy: a subclass states exactly which
extension components it expects, so a regression that silently drops a component fails the test.

**Defaults.** `WebFluxAutoConfigurationTest` supplies no properties and lists the four components that activation alone
produces:

```java
@SpringBootTest(classes = { WebFluxAutoConfigurationTest.class })
@EnableAutoConfiguration
class WebFluxAutoConfigurationTest extends AbstractWebFluxAutoConfigurationTest {

    @Override
    Set<Class<?>> getRegisteredComponentClasses() {
        return ofSet(HandlerMappingWebEndpointMappingResolver.class,
                InterceptingHandlerMethodProcessor.class,
                RequestHandledEventPublishingWebFilter.class,
                ReversedProxyHandlerMapping.class);
    }
}
```

**Everything on.** `WebFluxAutoConfigurationAllEnabledTest` adds the two storing flags — which is exactly the
`@OverrideAnnotationAttributes` property mechanism from §5 — and expects six components:

```java
@SpringBootTest(classes = { WebFluxAutoConfigurationAllEnabledTest.class })
@TestPropertySource(properties = {
        "microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true",
        "microsphere.spring.@EnableWebFluxExtension.storeResponseBodyReturnValue=true",
})
@EnableAutoConfiguration
class WebFluxAutoConfigurationAllEnabledTest extends AbstractWebFluxAutoConfigurationTest {

    @Override
    Set<Class<?>> getRegisteredComponentClasses() {
        return ofSet(
                HandlerMappingWebEndpointMappingResolver.class,
                InterceptingHandlerMethodProcessor.class,
                RequestHandledEventPublishingWebFilter.class,
                StoringRequestBodyArgumentInterceptor.class,
                StoringResponseBodyReturnValueInterceptor.class,
                ReversedProxyHandlerMapping.class
        );
    }
}
```

**Everything off.** `WebFluxAutoConfigurationAllDisabledTest` is the module-switch test; note it returns an empty set,
so only the endpoint assertions run:

```java
@SpringBootTest(classes = { WebFluxAutoConfigurationAllDisabledTest.class })
@TestPropertySource(properties = {
        "microsphere.spring.boot.webflux.enabled=false"
})
@EnableAutoConfiguration
class WebFluxAutoConfigurationAllDisabledTest extends AbstractWebFluxAutoConfigurationTest {

    @Override
    Set<Class<?>> getRegisteredComponentClasses() {
        return ofSet();
    }
}
```

> `@Disabled` is declared on the abstract base only, and JUnit 5 does not inherit it, so all three concrete subclasses do
> run. Their fixtures (`TestController`, `RouterFunctionTestConfig`, `PersonHandler`, `User`) live in the upstream
> `microsphere-spring-test` artifact and reach the test classpath transitively through `microsphere-spring-boot-test`,
> which is why `getRegisteredComponentClasses()` is the extension point each subclass supplies.

---

## 8. Property Reference

Prefix chain: `microsphere.` + `spring.` + `boot.` + `webflux.`. Constants live in
`io.microsphere.spring.boot.webflux.constants.PropertyConstants`:

| Constant | Value |
|---|---|
| `MICROSPHERE_SPRING_BOOT_WEBFLUX_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webflux.` |
| `DEFAULT_MICROSPHERE_SPRING_BOOT_WEBFLUX_ENABLED` | `true` |
| `MICROSPHERE_SPRING_BOOT_WEBFLUX_ENALBED_PROPERTY_NAME` | `microsphere.spring.boot.webflux.enabled` |

The key carries `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = APPLICATION_SOURCE)`, so
it is published into `spring-configuration-metadata.json` for IDE completion. The Java constant name repeats the
upstream `ENALBED` typo; the property key itself is correct.

| Property | Default | Effect |
|---|---|---|
| `microsphere.spring.boot.webflux.enabled` | `true` (`matchIfMissing`) | The module's only switch |
| `microsphere.spring.@EnableWebFluxExtension.<attribute>` | none | Overrides one `@EnableWebFluxExtension` attribute |
| `microsphere.spring.prefix.io.microsphere.spring.webflux.annotation.EnableWebFluxExtension` | `microsphere.spring.@EnableWebFluxExtension.` | Alternative prefix for the overrides above |

---

## 9. FAQ

| Symptom | Answer |
|---|---|
| I added the dependency and nothing changed | Expected — the module registers no beans of its own; it only enables `@EnableWebFluxExtension`. |
| Does importing it into a servlet application hurt? | No; the `REACTIVE` requirement does not match. Use [webmvc.md](./webmvc.md) for a servlet app. |
| I want explicit control over the extension | Annotate your own configuration class with `@EnableWebFluxExtension(reversedProxyHandlerMapping = true)` from `io.microsphere.spring.webflux.annotation`. |
| Generated URLs are wrong behind a proxy | Keep the module enabled (that is what `reversedProxyHandlerMapping` fixes) and check your forwarded-header configuration at the proxy. |
| `ReversedProxyHandlerMapping` is missing from the context | Check `microsphere.spring.boot.webflux.enabled` and the conditions report; a non-reactive context also explains it. |
| `StoringRequestBodyArgumentInterceptor` is not registered | Enable it with `microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true`; it is off by default. |
| Which module should I add for a Gateway application | `webflux`, since Spring Cloud Gateway is reactive; `webmvc` and `webflux` are mutually exclusive in practice because their conditions require different application types. |
| Boot 2 project | Use the `0.1.x` line, documented under [../1.x/README.md](../1.x/README.md); its `@AutoConfigureAfter` lists only the Boot 2 package name. |

---

**Related:** [webmvc.md](./webmvc.md) · [core.md](./core.md)
