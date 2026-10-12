# WebFlux Module

> [中文](./zh/webflux.md) · [Documentation index](../README.md)
>
> **Branch:** `release-1.x` · **Version line:** `0.1.x` · **Artifact:** `microsphere-spring-boot-webflux`
> **Spring Boot:** `2.1.18.RELEASE` – `2.7.18` · **Java:** 8+ · Depends on `microsphere-spring-boot-core`

Same document for the `release` (0.2.x) branch: [../../current/webflux.md](../current/webflux.md)

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

The reactive counterpart of `microsphere-spring-boot-webmvc` on the `0.1.x` line, and — as on `release` — deliberately
the narrowest module in the reactor: it registers exactly one auto-configuration class that applies
`@EnableWebFluxExtension(reversedProxyHandlerMapping = true)` and orders itself after Spring Boot's own WebFlux
auto-configuration.

Two facts explain almost every observation:

* **It declares no `@Bean` methods.** Adding the dependency introduces no bean of this module's own; it activates the
  upstream extension, and every component that appears comes from `microsphere-spring-webflux`.
* **It ships no default-properties file.** Only `core` and `actuator` carry defaults.

Against `release`, the differences are: the `@AutoConfigureAfter` array holds **one** name instead of two, and
registration is duplicated across `spring.factories` and `AutoConfiguration.imports`. The condition itself is
identical — the reactive stack never moved to a new servlet namespace, so there is no `javax`/`jakarta` split here.

Base package: `io.microsphere.spring.boot.webflux` → `.autoconfigure`, `.autoconfigure.condition`, `.constants`.

---

## 2. Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webflux` |
| Latest published on the `0.1.x` line | `0.1.34` |
| In development on `release-1.x` | `0.1.35-SNAPSHOT` |
| Pinned Spring Boot per profile | `2.1.18.RELEASE / 2.2.13.RELEASE / 2.3.12.RELEASE / 2.4.13 / 2.5.15 / 2.6.15 / 2.7.18` (default) |
| Java | `8` source level; CI verifies JDK `8 / 11 / 17 / 21 / 25` × 7 Boot profiles |
| Compile dependency | `microsphere-spring-boot-core` (`${revision}`), `microsphere-spring-webflux` (`0.1.39`, BOM-managed) |
| Optional dependency | `spring-boot-starter-webflux` — your application must provide it |
| Registering classes | `reactor.core.publisher.Flux`, `org.springframework.web.reactive.DispatcherHandler` |
| Registration | `META-INF/spring.factories` (`EnableAutoConfiguration`) **and** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

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
        // Spring Boot API
        "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration"
})
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
public class WebFluxAutoConfiguration {
}
```

The empty body is the point: the class contributes nothing itself, it only carries annotations. The ordering is still
declared by **class name** rather than by class reference, so a renamed or absent target degrades to "no ordering"
instead of a `ClassNotFoundException`. `release` adds the second entry
(`org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration`) for Boot 4.x; that one array element is the
only source change in this file between the two branches.

```properties
# META-INF/spring.factories (Boot 2.1 – 2.6, still read by 2.7)
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration
```

```
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (Boot 2.7+)
io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration
```

`reversedProxyHandlerMapping = true` is the single attribute this module turns on for you. It makes handler mapping and
URL computation respect the original host and scheme behind a gateway or reverse proxy — which is why a Spring Cloud
Gateway deployment is the typical consumer of this artifact.

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

This annotation is byte-for-byte equivalent to the `release` version — the only `webmvc`/`webflux` asymmetry across the
two branches sits in the servlet module. Importing the module into a servlet application is harmless because the
`REACTIVE` requirement does not match, and declaring `@ConditionalOnClass` by **name** means a missing Microsphere class
degrades to "no match" rather than `NoClassDefFoundError`.

```properties
# the module's only switch
microsphere.spring.boot.webflux.enabled=false
```

```bash
# when switched off, expect the class under noneMatches rather than matchSessions
curl -s http://localhost:8080/actuator/conditions | jq '.noneMatches | keys[]' | grep -i WebFlux
```

This needs `management.endpoint.conditions.enabled=true`; the library's `endpoints.properties` disables it by default,
as described in the actuator module guide.

---

## 5. The Upstream WebFlux Extension

`@EnableWebFluxExtension` (from `io.microsphere.spring.webflux.annotation`, in `microsphere-spring-webflux` `0.1.39`)
is the annotation applied for you:

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

There is no `registerHandlerInterceptors` / `handlerInterceptors` pair: the reactive stack has no
`InterceptorRegistry`, so interception runs through `InterceptingHandlerMethodProcessor`. That is the main asymmetry
with the sibling `webmvc` module, and it holds on both branches.

`@OverrideAnnotationAttributes` is present on this line too, so attributes can be overridden from configuration through
the prefix `microsphere.spring.@EnableWebFluxExtension.`:

```properties
microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebFluxExtension.storeResponseBodyReturnValue=true
```

A per-annotation custom prefix is supported via
`microsphere.spring.prefix.<annotation fully qualified class name>`.

---

## 6. Components It Activates

The components come from `microsphere-spring-webflux` `0.1.39`, under the same fully qualified names as the `0.2.39`
artifact; the "Appears when" column is the detection rule taken from this module's own tests:

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

All four test classes on this branch are identical to `release` — same base class, same expected component sets, same
property keys. `AbstractWebFluxAutoConfigurationTest` is `@Disabled`, so it is never executed on its own; it binds a
`WebTestClient` to the context and asserts both that the endpoints respond and that a subclass-supplied set of
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

    protected void testUser() {
        String pattern = "/test/user";
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);

        Mono<User> userMono = just(user);
        this.webTestClient.post()
                .uri(pattern)
                .accept(APPLICATION_JSON)
                .body(userMono, User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(this.testController.user(user));
    }

    protected WebTestClient buildWebTestClient(ConfigurableApplicationContext context) {
        return bindToApplicationContext(context).build();
    }
}
```

The `getRegisteredComponentClasses()` template method is the reusable idea: each subclass states exactly which extension
components it expects, so a regression that silently drops one fails the test.

**Defaults.** Four components from activation alone:

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

**Everything on.** The two storing flags, supplied through the `@OverrideAnnotationAttributes` mechanism, add two more:

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

**Everything off.** The module-switch test returns an empty set, so only the endpoint assertions run:

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

> `@Disabled` sits on the abstract base only and JUnit 5 does not inherit it, so the three concrete subclasses do run.
> Their fixtures (`TestController`, `RouterFunctionTestConfig`, `PersonHandler`, `User`) come from the upstream
> `microsphere-spring-test` artifact, reaching the test classpath transitively through `microsphere-spring-boot-test`.

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
upstream `ENALBED` typo; the property key itself is correct. Note also that this module has **no**
`PropertyConstantsTest`, so unlike `webmvc` these literals are not pinned by an assertion — verify against the source
when you add a key.

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
| Does importing it into a servlet application hurt? | No; the `REACTIVE` requirement does not match. Use [webmvc.md](./webmvc.md) there. |
| I want explicit control over the extension | Annotate your own configuration class with `@EnableWebFluxExtension(reversedProxyHandlerMapping = true)` from `io.microsphere.spring.webflux.annotation`. |
| Generated URLs are wrong behind a proxy | Keep the module enabled (that is what `reversedProxyHandlerMapping` fixes) and check forwarded-header configuration at the proxy. |
| `ReversedProxyHandlerMapping` is missing | Check `microsphere.spring.boot.webflux.enabled` and the conditions report; a non-reactive context also explains it. |
| `StoringRequestBodyArgumentInterceptor` is not registered | Enable `microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true`; it is off by default. |
| Boot 3/4 project | Use the `0.2.x` line, documented at [../../current/webflux.md](../current/webflux.md); its `@AutoConfigureAfter` lists both the Boot 3 and the Boot 4 package names. |
| Duplicate entries in the conditions report | `spring.factories` and `AutoConfiguration.imports` both register the class on Boot 2.7; Boot deduplicates it, so one configuration applies. |

---

**Related:** [webmvc.md](./webmvc.md) · [core.md](./core.md)
