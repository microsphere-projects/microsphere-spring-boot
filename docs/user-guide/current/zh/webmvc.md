# Web MVC 模块

> [English](../webmvc.md) · 文档索引：[中文](../../zh/README.md)
>
> **分支：** `release` · **版本线：** `0.2.x` · **Artifact：** `microsphere-spring-boot-webmvc`
> **Spring Boot：** `3.0.13` – `4.1.1` · **Java：** 17+ · 依赖 `microsphere-spring-boot-core`

目录：
[1 概览](#1-模块概览) ·
[2 版本信息](#2-版本信息) ·
[3 自动配置与排序](#3-自动配置与排序) ·
[4 Bean 与开关](#4-bean-与属性开关) ·
[5 上游扩展](#5-上游-web-mvc-扩展) ·
[6 可用性条件](#6-可用性条件) ·
[7 内容协商](#7-可配置内容协商) ·
[8 互斥视图解析器](#8-互斥视图解析器) ·
[9 日志拦截](#9-处理方法日志) ·
[10 测试用例示例](#10-来自测试用例的示例) ·
[11 属性清单](#11-配置属性清单) ·
[12 常见问题](#12-常见问题)

---

## 1. 模块概览

本模块把 Microsphere 的 Spring MVC 扩展接入 Spring Boot 的自动配置体系，贡献四类组件：内容缓存过滤器、由属性驱动的
内容协商配置器、互斥视图解析器监听器，以及针对处理方法、渲染上下文与参数解析器的日志组件。

决定它在应用里如何表现的特征有三点：

* **一切皆由属性控制。** 每个 Bean 都躲在一条 `@ConditionalOnProperty` 之后，而整个模块躲在同一个模块级开关之后。
* **模块不带默认属性文件。** 只有 `core` 与 `actuator` 携带 `core.properties` / `endpoints.properties`，因此除非你显式
  打开，这里一律沿用 Spring Boot 自己的默认值。
* **扩展能力在上游。** 本模块只是面向 Boot 的适配层；真正的拦截器、过滤器与解析器都来自 `microsphere-spring-webmvc`。
  若某个期望的组件没注册，先确认需要打开的是不是上游 `@EnableWebMvcExtension` 的某个属性。

基础包：`io.microsphere.spring.boot.webmvc` → `.autoconfigure`、`.autoconfigure.condition`、`.constants`。

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webmvc` |
| 最新发布 | `0.2.34` |
| `release` 分支在研 | `0.2.35-SNAPSHOT` |
| 各 profile 固定的 Spring Boot | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16`（默认）`/ 4.0.8 / 4.1.1` |
| Java | 源码级别 `17`；CI 验证 JDK `17 / 21 / 25` |
| 编译依赖 | `microsphere-spring-boot-core`（`${revision}`）、`microsphere-spring-webmvc`（由 BOM 管理） |
| 可选依赖 | `spring-boot-starter-web` —— web starter 需要由你的应用自己提供 |
| Servlet 命名空间 | `jakarta.servlet` |
| 注册方式 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webmvc</artifactId>
</dependency>
```

导入 BOM（见 [dependencies.md](./dependencies.md)）或直接以 `microsphere-spring-boot-parent` 为父工程，这条声明就无需
写版本号。

---

## 3. 自动配置与排序

`io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` 注册在 `AutoConfiguration.imports` 中，
类上有四个注解：

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

排序是**按类名声明而非按类引用**。正是这一行让同一个 artifact 同时服务两代 Boot：Spring Boot 3.x 与 4.x 把
`WebMvcAutoConfiguration` 放在不同包下，若注解里直接引用类，另一代就编译或解析不过。按名字列出两者，本模块在
`-Pspring-boot-3.0` 到 `-Pspring-boot-4.1` 的每个 profile 下都能构建，也不需要未发布的 `compatible` shim。

自动配置类上的 `@EnableWebMvcExtension` 会激活上游扩展，并把两个属性相对注解默认值翻成 `true`：
`registerHandlerInterceptors`（把 `HandlerInterceptor` Bean 注册进 `InterceptorRegistry`）与
`reversedProxyHandlerMapping`（让处理器映射尊重网关/反向代理后的原始 host 与 scheme）。

---

## 4. Bean 与属性开关

| Bean 方法 | 返回类型 | 条件属性 | 缺省 |
|---|---|---|---|
| `contentCachingFilter()` | `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` | `microsphere.spring.boot.webmvc.filter.enabled` | 开（`matchIfMissing = true`） |
| `contentNegotiationManagerWebMvcConfigurer()` | `io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer` | `microsphere.spring.webmvc.content-negotiation.enabled` | 开（`matchIfMissing = true`） |
| `exclusiveViewResolverApplicationListener()` | `io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener` | `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | 关（没有 `matchIfMissing`，该键必须有值） |
| `LoggingConfiguration`（内嵌静态类） | 导入 `LoggingMethodHandlerInterceptor`、`LoggingPageRenderContextHandlerInterceptor`、`LoggingHandlerMethodArgumentResolverAdvice` | `microsphere.spring.boot.webmvc.logging.enabled` | 开（`matchIfMissing = true`） |

模块级总开关是 `microsphere.spring.boot.webmvc.enabled`，由 `ConditionalOnWebMvcAvailable` 以
`matchIfMissing = true` 评估。置为 `false` 会一次性移除上述四项。

```properties
# application.properties
microsphere.spring.boot.webmvc.enabled=true
microsphere.spring.boot.webmvc.filter.enabled=false   # 只关内容缓存过滤器
microsphere.spring.webmvc.content-negotiation.enabled=false
microsphere.spring.boot.webmvc.logging.enabled=true   # 保留处理方法日志
```

> **两套命名空间，且是刻意的。** `microsphere.spring.boot.webmvc.` 下的键控制**本模块**的 Bean；
> `microsphere.spring.webmvc.`（不带 `boot`）下的键配置上游库，包括内容协商与互斥视图解析器。在本模块自己的测试里能
> `grep` 到 `microsphere.spring.webmvc.filter.enabled`，但那个键**并不会**决定 `ContentCachingFilter` 是否注册 ——
> 真正的闸门是 `microsphere.spring.boot.webmvc.filter.enabled`。

> **常量名拼写错误。** 若干 Java 常量名拼成了 `ENALBED`，例如
> `MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME`。**属性键本身是正确的**，错的只是常量名，因此静态引用时请按
> `io.microsphere.spring.boot.webmvc.constants.PropertyConstants` 里的实际名字。

---

## 5. 上游 Web MVC 扩展

`@EnableWebMvcExtension`（`io.microsphere.spring.webmvc.annotation`）就是本自动配置替你加上的注解。它的属性与默认值：

| 属性 | 默认值 | 作用 |
|---|---|---|
| `registerWebEndpointMappings` | `true` | 注册由 Spring MVC 与传统 Servlet 端点解析出的 `WebEndpointMapping` |
| `interceptHandlerMethods` | `true` | 在 `HandlerMethod` 执行前后调用 `HandlerMethodInterceptor` / `HandlerMethodArgumentInterceptor` |
| `publishEvents` | `true` | 发布 `HandlerMethodArgumentsResolvedEvent` 与 `WebEndpointMappingsReadyEvent` |
| `requestContextStrategy` | `DEFAULT` | 决定 `RequestAttributes` 存放位置 |
| `sources` | `BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER` | 收集扩展组件的来源 |
| `registerHandlerInterceptors` | `false` → **本模块为 `true`** | 把 `HandlerInterceptor` Bean 加入 `InterceptorRegistry`，此时 `handlerInterceptors` 失效 |
| `handlerInterceptors` | `{}` | 显式指定要注册的 `HandlerInterceptor` 类型（上一项为 `true` 时被忽略） |
| `storeRequestBodyArgument` | `false` | 把 `@RequestBody` 参数保留在 `RequestAttributes` 里供后续读取 |
| `storeResponseBodyReturnValue` | `false` | 在写出 `@ResponseBody` 之前保留其返回值 |
| `reversedProxyHandlerMapping` | `false` → **本模块为 `true`** | 为代理部署注册 `ReversedProxyHandlerMapping` |

由于 `@EnableWebMvcExtension` 被元标注了 `@OverrideAnnotationAttributes`，上面每一个属性值都可以改用配置项覆盖，而不必
自己写注解。默认策略 `ConfigurationPropertyOverrideAnnotationAttributesStrategy` 使用前缀
`microsphere.spring.@EnableWebMvcExtension.`，把属性名拼到前缀之后：

```properties
# 覆盖自动配置所应用的注解属性
microsphere.spring.@EnableWebMvcExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebMvcExtension.storeResponseBodyReturnValue=true
```

若要为某个注解类型换用自定义前缀，可设置
`microsphere.spring.prefix.<注解全限定类名>`。

---

## 6. 可用性条件

`io.microsphere.spring.boot.webmvc.autoconfigure.condition.ConditionalOnWebMvcAvailable` 是组合型元注解，三条要求必须
同时成立：

```java
@ConditionalOnWebApplication(type = SERVLET)                       // 必须是 Servlet 应用
@ConditionalOnClass(name = {                                       // 且以下类全部存在
        "jakarta.servlet.Servlet",
        "org.springframework.context.ApplicationContext",
        "org.springframework.web.method.HandlerMethod",
        "org.springframework.web.servlet.DispatcherServlet",
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor",
        "io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension"
})
@ConditionalOnProperty(name = "microsphere.spring.boot.webmvc.enabled",
        matchIfMissing = true)                                     // 未显式关闭即启用
```

因此在响应式应用里引入本模块没有副作用：`SERVLET` 这一条直接不匹配，什么都不会注册。`@ConditionalOnClass` 用的是
`name`，所以 Microsphere 的类缺失时条件只是"不成立"，不会抛 `NoClassDefFoundError`。

```bash
# 条件评估报告里应能在 matchSessions 中看到 WebMvcAutoConfiguration
curl -s http://localhost:8080/actuator/conditions | jq '.matchSessions | keys[]' | grep -i webmvc
```

这需要先打开 `management.endpoint.conditions.enabled=true`；本库的 `endpoints.properties` 默认把它关掉了，细节见
actuator 模块文档。

---

## 7. 可配置内容协商

`ConfigurableContentNegotiationManagerWebMvcConfigurer` 实现 `WebMvcConfigurer.configureContentNegotiation`。它反射拿到
`ContentNegotiationConfigurer` 内部的 `ContentNegotiationManagerFactoryBean` 字段，再用 Spring `DataBinder` 把
`microsphere.spring.webmvc.content-negotiation.` 前缀下的所有属性绑定到该工厂 Bean。

可以依赖的结论有三点：

* 任何 `ContentNegotiationManagerFactoryBean` 的 setter 都能按键触达，新增一个选项不需要改代码。
* `mediaTypes` 接受 JSON 对象字面量，由专门的 `PropertyEditor` 完成转换。
* `contentNegotiationManager` 与 `servletContext` 是禁止绑定的字段，因此一个写错的键不可能替换掉管理器或让工厂 Bean
  脱离 Servlet 上下文。

```properties
microsphere.spring.webmvc.content-negotiation.enabled=true
microsphere.spring.webmvc.content-negotiation.favorParameter=true
microsphere.spring.webmvc.content-negotiation.parameterName=p
microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=true
microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=true
```

闸门关闭时，Boot 自己的 `ContentNegotiationManager` 原样保留，即只有一条 `HeaderContentNegotiationStrategy` ——
这正是本模块"全部关闭"测试所断言的状态。

---

## 8. 互斥视图解析器

`ExclusiveViewResolverApplicationListener` 同时是 `ApplicationListener<ContextRefreshedEvent>` 与 `EnvironmentAware`。
由于它的 `@ConditionalOnProperty` 没有 `matchIfMissing`，只有当
`microsphere.spring.webmvc.view-resolver.exclusive-bean-name` 有值时该 Bean 才会注册：

```properties
microsphere.spring.webmvc.view-resolver.exclusive-bean-name=viewResolver
```

容器刷新时它保留被命名的那个 `ViewResolver`、把其余的降级，这正是"两个解析器都认领同一个视图"这一常见问题（模板引擎与
`InternalResourceViewResolver` 共存）的解法。不设置该属性时监听器根本不存在，Boot 的解析器顺序完全不受影响 ——
普通的 `WebMvcAutoConfigurationTest` 对这一状态断言的就是 `assertNull(listener)`。

---

## 9. 处理方法日志

内嵌的 `WebMvcAutoConfiguration.LoggingConfiguration` 导入三个上游组件：

| 导入的类 | 包 | 观察对象 |
|---|---|---|
| `LoggingMethodHandlerInterceptor` | `io.microsphere.spring.webmvc.interceptor` | 处理器方法调用及其入参 |
| `LoggingPageRenderContextHandlerInterceptor` | `io.microsphere.spring.webmvc.interceptor` | 渲染时的 model/view 状态 |
| `LoggingHandlerMethodArgumentResolverAdvice` | `io.microsphere.spring.webmvc.method.support` | `HandlerMethodArgumentResolver` 调用 |

闸门是 `microsphere.spring.boot.webmvc.logging.enabled`（`matchIfMissing = true`）。日志级别安静时这些 Bean 依然存在；
要看到输出，把上面几个类的级别调高：

```yaml
logging:
  level:
    io.microsphere.spring.webmvc.interceptor: DEBUG
    io.microsphere.spring.webmvc.method.support: DEBUG
    io.microsphere.spring.web.servlet.filter: DEBUG
```

---

## 10. 来自测试用例的示例

本模块的测试就是"如何激活、如何断言"的参考。它们都继承
`AbstractWebMvcAutoConfigurationTest`，而后者标注了 `@Disabled`，因此自身不会被执行：

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

**默认即生效。** `WebMvcAutoConfigurationTest` 一个属性都不加，用来断言开箱形态 —— 过滤器、配置器、日志配置都在，
互斥视图解析器不在：

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

`WebMvcExtensionConfiguration` 可注入，是证明 `@EnableWebMvcExtension` 生效的最廉价方式。

**全部打开。** `WebMvcAutoConfigurationAllEnabledTest` 打开视图解析器与基于参数的协商策略，然后校验得到的策略列表：

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

**全部关闭。** `WebMvcAutoConfigurationAllDisabledTest` 是它的对偶，其断言写清了"没有 Microsphere 贡献"长什么样 ——
只剩一条策略，且是 Boot 自己的：

```java
void assertContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
    List<ContentNegotiationStrategy> strategies = contentNegotiationManager.getStrategies();
    assertEquals(1, strategies.size());

    ContentNegotiationStrategy strategy = strategies.get(0);
    assertTrue(strategy instanceof HeaderContentNegotiationStrategy);
}
```

> 全部打开/全部关闭两个测试设置的是 `microsphere.spring.webmvc.{filter,logging}.enabled`，而本模块的 Bean 闸门读取的是
> `microsphere.spring.boot.webmvc.{filter,logging}.enabled`。这两个测试都没有断言 `assertNull(contentCachingFilter)`，
> 所以这处差异不会让它们失败 —— 但在你自己的配置里请使用带 `boot` 的那组键。

---

## 11. 配置属性清单

前缀链条：本模块的开关走 `microsphere.` + `spring.` + `boot.` + `webmvc.`，上游库走 `microsphere.` + `spring.` +
`webmvc.`。常量位于 `io.microsphere.spring.boot.webmvc.constants.PropertyConstants`：

| 常量 | 值 |
|---|---|
| `MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webmvc.` |
| `MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME` | `microsphere.spring.boot.webmvc.enabled` |
| `FILTER_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webmvc.filter.` |
| `MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENABLED_PROPERTY_NAME` | `microsphere.spring.boot.webmvc.filter.enabled` |
| `LOGGING_PROPERTY_NAME_PREFIX` | `microsphere.spring.boot.webmvc.logging.` |
| `MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME` | `microsphere.spring.boot.webmvc.logging.enabled` |
| `CONTENT_NEGOTIATION_PROPERTY_NAME_PREFIX` | `microsphere.spring.webmvc.content-negotiation.` |
| `MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME` | `microsphere.spring.webmvc.content-negotiation.enabled` |

`DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED`、`..._FILTER_ENABLED`、`..._LOGGING_ENABLED` 与
`DEFAULT_MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED` 都是字符串 `"true"`，且每个键都带
`@ConfigurationProperty(type = boolean.class, source = APPLICATION_SOURCE)`，正是这个注解把它们写进
`spring-configuration-metadata.json` 供 IDE 补全。

| 属性 | 默认 | 作用 |
|---|---|---|
| `microsphere.spring.boot.webmvc.enabled` | `true`（`matchIfMissing`） | 模块总开关，移除全部贡献 |
| `microsphere.spring.boot.webmvc.filter.enabled` | `true`（`matchIfMissing`） | `ContentCachingFilter` |
| `microsphere.spring.boot.webmvc.logging.enabled` | `true`（`matchIfMissing`） | 处理器/渲染上下文/参数解析日志 |
| `microsphere.spring.webmvc.content-negotiation.enabled` | `true`（`matchIfMissing`） | `ConfigurableContentNegotiationManagerWebMvcConfigurer` |
| `microsphere.spring.webmvc.content-negotiation.<setter>` | 无 | 绑定到 `ContentNegotiationManagerFactoryBean` |
| `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | 无 | 注册 `ExclusiveViewResolverApplicationListener` 并指定要保留的解析器 |
| `microsphere.spring.@EnableWebMvcExtension.<attribute>` | 无 | 覆盖 `@EnableWebMvcExtension` 的某个属性值 |

---

## 12. 常见问题

| 现象 | 处理 |
|---|---|
| 响应式应用引入后无效果 | 符合预期：条件要求 `SERVLET`。请改用 [webflux.md](./webflux.md)。 |
| 想彻底移除本模块的一切 | `microsphere.spring.boot.webmvc.enabled=false`，总开关优先于单个 Bean 的闸门。 |
| `microsphere.spring.webmvc.content-negotiation.enabled` 像是不生效 | 这个键没有写错，它确实不带 `boot`。但 filter/logging 若不带 `boot` 就毫无作用，请用 `microsphere.spring.boot.webmvc.filter.enabled`。 |
| 内容缓存带来内存开销 | `microsphere.spring.boot.webmvc.filter.enabled=false`。 |
| 看不到处理方法日志 | 保持 `microsphere.spring.boot.webmvc.logging.enabled=true`，并把 `io.microsphere.spring.webmvc.interceptor` 调到 `DEBUG`。 |
| 我自己的配置类上也写了 `@EnableWebMvcExtension` | 两处共存；属性值经由覆盖策略解析，因此 `microsphere.spring.@EnableWebMvcExtension.` 下的配置项优先。 |
| 过滤器顺序带来意外 | `ContentCachingFilter` 是一个普通 `OncePerRequestFilter` Bean 被注册的；需要排在前面的话请使用 Boot 的过滤器顺序属性。 |
| 运行时 `ClassNotFoundException`：`WebMvcAutoConfiguration` | 确认你在 Boot 3/4（`jakarta.servlet`）。Boot 2 请改用 `0.1.x` 线，文档见 [../1.x/README.md](../../1.x/README.md)。 |

---

**相关文档：** [core.md](./core.md) · [actuator.md](./actuator.md) · [webflux.md](./webflux.md)
