# WebFlux 模块

> [English](../webflux.md) · 文档索引：[中文](../../zh/README.md)
>
> **分支：** `release` · **版本线：** `0.2.x` · **Artifact：** `microsphere-spring-boot-webflux`
> **Spring Boot：** `3.0.13` – `4.1.1` · **Java：** 17+ · 依赖 `microsphere-spring-boot-core`

目录：
[1 概览](#1-模块概览) ·
[2 版本信息](#2-版本信息) ·
[3 自动配置与排序](#3-自动配置与排序) ·
[4 可用性条件](#4-可用性条件) ·
[5 上游扩展](#5-上游-webflux-扩展) ·
[6 被激活的组件](#6-被激活的组件) ·
[7 测试用例示例](#7-来自测试用例的示例) ·
[8 属性清单](#8-配置属性清单) ·
[9 常见问题](#9-常见问题)

---

## 1. 模块概览

本模块是 `microsphere-spring-boot-webmvc` 的响应式对应物，而且范围有意更小：它只注册一个自动配置类，该类应用
`@EnableWebFluxExtension(reversedProxyHandlerMapping = true)`，并把自己排在 Spring Boot 自身的 WebFlux 自动配置之后。

两个事实足以解释关于本模块几乎所有的观察结果：

* **它没有 `@Bean` 方法。** 引入依赖不会新增任何属于本模块的 Bean —— 它做的是激活上游扩展，所有真正出现的组件都来自
  `microsphere-spring-webflux`。
* **它不带默认属性文件。** 只有 `core` 与 `actuator` 携带默认值，因此除非你主动要求，这里不会改变 Boot 的任何行为。

基础包：`io.microsphere.spring.boot.webflux` → `.autoconfigure`、`.autoconfigure.condition`、`.constants`。

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webflux` |
| 最新发布 | `0.2.34` |
| `release` 分支在研 | `0.2.35-SNAPSHOT` |
| 各 profile 固定的 Spring Boot | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16`（默认）`/ 4.0.8 / 4.1.1` |
| Java | 源码级别 `17`；CI 验证 JDK `17 / 21 / 25` |
| 编译依赖 | `microsphere-spring-boot-core`（`${revision}`）、`microsphere-spring-webflux`（由 BOM 管理） |
| 可选依赖 | `spring-boot-starter-webflux` —— 需要由你的应用自己提供 |
| 判定存在用的关键类 | `reactor.core.publisher.Flux`、`org.springframework.web.reactive.DispatcherHandler` |
| 注册方式 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webflux</artifactId>
</dependency>
```

---

## 3. 自动配置与排序

`io.microsphere.spring.boot.webflux.autoconfigure.WebFluxAutoConfiguration`：

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

空的类体正是重点：这个类自己不贡献任何东西，它只是承载注解。与 `webmvc` 一样，排序是**按类名字符串**声明的，因为
Spring Boot 3.x 与 4.x 把 `WebFluxAutoConfiguration` 放在不同包下 —— 这正是同一个 artifact 能覆盖全部 8 个 profile、
且不需要未发布的 `compatible` shim 的原因。

`reversedProxyHandlerMapping = true` 是本模块唯一替你自己打开的属性：它让处理器映射与 URL 计算在应用位于网关或反向代理
之后时，采用原始的 host 与 scheme。

---

## 4. 可用性条件

`io.microsphere.spring.boot.webflux.autoconfigure.condition.ConditionalOnWebFluxAvailable` 的组合要求：

```java
@ConditionalOnWebApplication(type = REACTIVE)                      // 必须是响应式应用
@ConditionalOnClass(name = {
        "reactor.core.publisher.Flux",                                        // Reactor API
        "org.springframework.context.ApplicationContext",                    // Spring Context API
        "org.springframework.web.method.HandlerMethod",                      // Spring Web API
        "org.springframework.web.reactive.DispatcherHandler",                // Spring WebFlux API
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor", // Microsphere Spring Web API
        "io.microsphere.spring.webflux.annotation.EnableWebFluxExtension"    // Microsphere Spring WebFlux API
})
@ConditionalOnProperty(name = "microsphere.spring.boot.webflux.enabled",
        matchIfMissing = true)                                     // 未显式关闭即启用
```

因此在 Servlet 应用里引入本模块是无害的 —— `REACTIVE` 这一条不匹配。由于 `@ConditionalOnClass` 用的是 `name`，
Microsphere 的类缺失时条件只是"不成立"，不会抛 `NoClassDefFoundError`。

```properties
# 本模块唯一的开关
microsphere.spring.boot.webflux.enabled=false
```

```bash
# 关掉模块后，应出现在 noneMatches 而不是 matchSessions 里
curl -s http://localhost:8080/actuator/conditions | jq '.noneMatches | keys[]' | grep -i WebFlux
```

这需要先打开 `management.endpoint.conditions.enabled=true`；本库的 `endpoints.properties` 默认把它关掉了，细节见
actuator 模块文档。

---

## 5. 上游 WebFlux 扩展

`@EnableWebFluxExtension`（`io.microsphere.spring.webflux.annotation`）就是替你应用的注解。其属性：

| 属性 | 默认值 | 作用 |
|---|---|---|
| `registerWebEndpointMappings` | `true` | 为响应式端点注册 `WebEndpointMapping` |
| `interceptHandlerMethods` | `true` | 在 `HandlerMethod` 执行前后调用 `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` |
| `publishEvents` | `true` | 发布 `HandlerMethodArgumentsResolvedEvent` 与 `WebEndpointMappingsReadyEvent` |
| `sources` | `BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER` | 收集扩展组件的来源 |
| `requestContextStrategy` | `DEFAULT` | 决定请求作用域属性的存放位置 |
| `storeRequestBodyArgument` | `false` | 保留 `@RequestBody` 参数供后续读取 |
| `storeResponseBodyReturnValue` | `false` | 在写出 `@ResponseBody` 之前保留其返回值 |
| `reversedProxyHandlerMapping` | `false` → **本模块为 `true`** | 为代理部署注册 `ReversedProxyHandlerMapping` |

这里没有 `registerHandlerInterceptors` / `handlerInterceptors` 这一对属性：响应式栈没有 `InterceptorRegistry`，拦截是通过
`InterceptingHandlerMethodProcessor` 完成的。这一点就是本模块与 `webmvc` 之间最主要的不对称。

该注解同样被元标注了 `@OverrideAnnotationAttributes`，因此每个属性都可以改用配置项覆盖，前缀为
`microsphere.spring.@EnableWebFluxExtension.`：

```properties
microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebFluxExtension.storeResponseBodyReturnValue=true
```

若要换用自定义前缀，设置
`microsphere.spring.prefix.<注解全限定类名>` 即可。

---

## 6. 被激活的组件

下表的组件都来自 `microsphere-spring-webflux`；"何时出现"一列是本模块自己测试给出的可检测规则：

| 组件 | 包 | 何时出现 |
|---|---|---|
| `HandlerMappingWebEndpointMappingResolver` | `io.microsphere.spring.webflux.metadata` | 条件命中后始终存在 |
| `InterceptingHandlerMethodProcessor` | `io.microsphere.spring.webflux.method` | 始终存在 |
| `RequestHandledEventPublishingWebFilter` | `io.microsphere.spring.webflux.server.filter` | 始终存在 |
| `ReversedProxyHandlerMapping` | `io.microsphere.spring.webflux.handler` | 始终存在（`reversedProxyHandlerMapping = true`） |
| `StoringRequestBodyArgumentInterceptor` | `io.microsphere.spring.webflux.method` | `storeRequestBodyArgument=true` |
| `StoringResponseBodyReturnValueInterceptor` | `io.microsphere.spring.webflux.method` | `storeResponseBodyReturnValue=true` |
| 以上全部 | — | `microsphere.spring.boot.webflux.enabled=false` 会一并移除 |

---

## 7. 来自测试用例的示例

`AbstractWebFluxAutoConfigurationTest` 是共享基类，标注 `@Disabled` 因此自身不会被执行。它把 `WebTestClient` 绑定到容器，
同时断言端点有响应、以及子类提供的一组组件类确实存在：

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

`getRegisteredComponentClasses()` 这个模板方法值得借鉴：子类声明自己期望哪些扩展组件，于是任何"悄悄少注册了一个组件"的
回归都会让测试失败。

**默认状态。** `WebFluxAutoConfigurationTest` 不加任何属性，列出仅靠激活就会产生这四个组件：

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

**全部打开。** `WebFluxAutoConfigurationAllEnabledTest` 加上两个存储开关 —— 也就是 §5 里的
`@OverrideAnnotationAttributes` 属性覆盖机制 —— 并期望六个组件：

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

**全部关闭。** `WebFluxAutoConfigurationAllDisabledTest` 验证的是模块总开关；注意它返回空集合，因此只跑端点断言：

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

> `@Disabled` 只声明在抽象基类上，而 JUnit 5 不会让它被子类继承，所以三个具体子类确实都会执行。它们的测试夹具
> （`TestController`、`RouterFunctionTestConfig`、`PersonHandler`、`User`）来自上游的 `microsphere-spring-test`，通过
> `microsphere-spring-boot-test` 间接进入测试类路径 —— 这正是每个子类需要提供
> `getRegisteredComponentClasses()` 的原因。

---

## 8. 配置属性清单

前缀链条：`microsphere.` + `spring.` + `boot.` + `webflux.`。常量位于
`io.microsphere.spring.boot.webflux.constants.PropertyConstants`：

| 常量 | 值 |
|---|---|
| `MICROSPHERE_SPRING_BOOT_WEBFLUX_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webflux.` |
| `DEFAULT_MICROSPHERE_SPRING_BOOT_WEBFLUX_ENABLED` | `true` |
| `MICROSPHERE_SPRING_BOOT_WEBFLUX_ENALBED_PROPERTY_NAME` | `microsphere.spring.boot.webflux.enabled` |

该键带有 `@ConfigurationProperty(type = boolean.class, defaultValue = "true", source = APPLICATION_SOURCE)`，因此会被
写进 `spring-configuration-metadata.json` 供 IDE 补全。Java 常量名沿用了上游的 `ENALBED` 拼写错误；属性键本身是正确的。

| 属性 | 默认 | 作用 |
|---|---|---|
| `microsphere.spring.boot.webflux.enabled` | `true`（`matchIfMissing`） | 本模块唯一的开关 |
| `microsphere.spring.@EnableWebFluxExtension.<attribute>` | 无 | 覆盖 `@EnableWebFluxExtension` 的某个属性 |
| `microsphere.spring.prefix.io.microsphere.spring.webflux.annotation.EnableWebFluxExtension` | `microsphere.spring.@EnableWebFluxExtension.` | 为上面的覆盖改用自定义前缀 |

---

## 9. 常见问题

| 现象 | 处理 |
|---|---|
| 加了依赖却什么都没变 | 符合预期：本模块不注册 Bean，只启用 `@EnableWebFluxExtension`。 |
| 在 Servlet 应用里引入它会有副作用吗 | 不会，`REACTIVE` 条件不匹配。Servlet 应用请使用 [webmvc.md](./webmvc.md)。 |
| 想手工控制扩展 | 在你自己的配置类上标注 `@EnableWebFluxExtension(reversedProxyHandlerMapping = true)`（来自 `io.microsphere.spring.webflux.annotation`）。 |
| 反向代理后生成的 URL 不对 | 保持模块启用（`reversedProxyHandlerMapping` 正是修这个问题的），并检查代理侧的转发头配置。 |
| 容器里没有 `ReversedProxyHandlerMapping` | 检查 `microsphere.spring.boot.webflux.enabled` 与条件评估报告；应用不是响应式也能解释这一现象。 |
| `StoringRequestBodyArgumentInterceptor` 没注册 | 用 `microsphere.spring.@EnableWebFluxExtension.storeRequestBodyArgument=true` 打开，它默认关闭。 |
| Gateway 应用该引入哪个模块 | `webflux`，因为 Spring Cloud Gateway 是响应式的；`webmvc` 与 `webflux` 在实际使用中互斥，二者的条件要求不同的应用类型。 |
| Boot 2 项目 | 使用 `0.1.x` 线，文档见 [../1.x/README.md](../../1.x/README.md)；该线的 `@AutoConfigureAfter` 只列出 Boot 2 的包名。 |

---

**相关文档：** [webmvc.md](./webmvc.md) · [core.md](./core.md)
