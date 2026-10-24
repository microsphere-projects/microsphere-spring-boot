# Web MVC 模块

> [English](../webmvc.md) · 文档索引：[中文](../../zh/README.md)
>
> **分支：** `release-1.x` · **版本线：** `0.1.x` · **Artifact：** `microsphere-spring-boot-webmvc`
> **Spring Boot：** `2.1.18.RELEASE` – `2.7.18` · **Java：** 8+ · 依赖 `microsphere-spring-boot-core`

`release`（0.2.x）分支的同名文档：[../../current/zh/webmvc.md](../../current/zh/webmvc.md)

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

在 `0.1.x` 线上，本模块贡献的能力与 `release` 完全一致 —— 内容缓存过滤器、由属性驱动的内容协商配置器、互斥视图解析器监听器，
以及处理方法/渲染上下文/参数解析日志 —— 而且每一项都由同一批属性键控制。不同的地方在于**交付方式**而非能力：

* 条件判定的是 `javax.servlet.Servlet`，而不是 `jakarta.servlet.Servlet`。
* `@AutoConfigureAfter` 只列出**一个**类名，因为这一线只有一个 Boot 2 包路径需要排序。
* 自动配置**注册了两次**：`spring.factories` 与 `AutoConfiguration.imports` 各一份，从而 Boot 2.1–2.6 与 Boot 2.7 都能
  装载。
* 上游库是 `microsphere-spring-webmvc` `0.1.39`，其类名与属性命名空间与 `0.2.39` 完全一致 —— 同样的全限定名，底层是
  `javax.servlet` 命名空间。

基础包：`io.microsphere.spring.boot.webmvc` → `.autoconfigure`、`.autoconfigure.condition`、`.constants`。

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-webmvc` |
| `0.1.x` 线最新发布 | `0.1.34` |
| `release-1.x` 分支在研 | `0.1.35-SNAPSHOT` |
| 各 profile 固定的 Spring Boot | `2.1.18.RELEASE / 2.2.13.RELEASE / 2.3.12.RELEASE / 2.4.13 / 2.5.15 / 2.6.15 / 2.7.18`（默认） |
| Java | 源码级别 `8`；CI 验证 JDK `8 / 11 / 17 / 21 / 25` × 7 个 Boot profile |
| 编译依赖 | `microsphere-spring-boot-core`（`${revision}`）、`microsphere-spring-webmvc`（`0.1.39`，由 BOM 管理） |
| 可选依赖 | `spring-boot-starter-web` —— web starter 需要由你的应用自己提供 |
| Servlet 命名空间 | `javax.servlet` |
| 注册方式 | `META-INF/spring.factories`（`EnableAutoConfiguration`）**以及** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-webmvc</artifactId>
</dependency>
```

请通过 `0.1.x` 的 BOM 引入，或以 `microsphere-spring-boot-parent` 作为父 POM。不要把本 artifact 放进 Boot 3/4 的
classpath；那条线请看 [../../current/zh/webmvc.md](../../current/zh/webmvc.md)。

---

## 3. 自动配置与排序

`io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` 类上的注解：

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

依然使用 `name` 属性而不是类引用，因此排序是惰性解析的：目标类改名或不存在只会退化为"不排序"，不会抛
`ClassNotFoundException`。这一线只列出一个名字；`release` 分支额外加了
`org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` 以适配 Boot 4.x。数组里多出的这一项，加上条件里
`javax` → `jakarta` 的替换，就是这个模块在两分支之间的**全部**源码差异。

双份注册是刻意为之：

```properties
# META-INF/spring.factories（Boot 2.1 – 2.6 读取，2.7 仍兼容）
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
```

```
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports（Boot 2.7+）
io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
```

Boot 2.7 会同时读到两份并去重，因此写两遍是安全的；Boot 2.1–2.6 只认 `spring.factories`，Boot 3+ 只认 `imports`。

---

## 4. Bean 与属性开关

| Bean 方法 | 返回类型 | 条件属性 | 缺省 |
|---|---|---|---|
| `contentCachingFilter()` | `io.microsphere.spring.web.servlet.filter.ContentCachingFilter` | `microsphere.spring.boot.webmvc.filter.enabled` | 开（`matchIfMissing = true`） |
| `contentNegotiationManagerWebMvcConfigurer()` | `io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer` | `microsphere.spring.webmvc.content-negotiation.enabled` | 开（`matchIfMissing = true`） |
| `exclusiveViewResolverApplicationListener()` | `io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener` | `microsphere.spring.webmvc.view-resolver.exclusive-bean-name` | 关（没有 `matchIfMissing`，该键必须有值） |
| `LoggingConfiguration`（内嵌静态类） | 导入 `LoggingMethodHandlerInterceptor`、`LoggingPageRenderContextHandlerInterceptor`、`LoggingHandlerMethodArgumentResolverAdvice` | `microsphere.spring.boot.webmvc.logging.enabled` | 开（`matchIfMissing = true`） |

模块级总开关是 `microsphere.spring.boot.webmvc.enabled`，由 `ConditionalOnWebMvcAvailable` 以 `matchIfMissing = true`
评估。

```properties
# application.properties
microsphere.spring.boot.webmvc.enabled=true
microsphere.spring.boot.webmvc.filter.enabled=false   # 只关内容缓存过滤器
microsphere.spring.webmvc.content-negotiation.enabled=false
microsphere.spring.boot.webmvc.logging.enabled=true   # 保留处理方法日志
```

> **两套命名空间，且是刻意的。** `microsphere.spring.boot.webmvc.` 下的键控制**本模块**的 Bean；
> `microsphere.spring.webmvc.`（不带 `boot`）下的键配置上游库，包括内容协商与互斥视图解析器。本模块自己的测试里设置了
> `microsphere.spring.webmvc.filter.enabled`，但那个键**并不会**决定 `ContentCachingFilter` 是否注册 —— 真正的闸门是
> `microsphere.spring.boot.webmvc.filter.enabled`。

> **常量名拼写错误。** 若干 Java 常量名拼成了 `ENALBED`，例如
> `MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME`。**属性键本身是正确的**，错的只是常量名，因此静态引用时请按
> `io.microsphere.spring.boot.webmvc.constants.PropertyConstants` 里的实际名字。

---

## 5. 上游 Web MVC 扩展

`@EnableWebMvcExtension`（`io.microsphere.spring.webmvc.annotation`，位于 `microsphere-spring-webmvc` `0.1.39`）就是本
自动配置替你应用的注解。其属性与默认值与 `0.2.x` 线一致：

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

这一线的该注解同样被元标注了 `@OverrideAnnotationAttributes`，因此每个属性都可以改用配置项覆盖，前缀为
`microsphere.spring.@EnableWebMvcExtension.`：

```properties
microsphere.spring.@EnableWebMvcExtension.storeRequestBodyArgument=true
microsphere.spring.@EnableWebMvcExtension.storeResponseBodyReturnValue=true
```

也支持为注解类型指定自定义前缀：`microsphere.spring.prefix.<注解全限定类名>`。

---

## 6. 可用性条件

`io.microsphere.spring.boot.webmvc.autoconfigure.condition.ConditionalOnWebMvcAvailable` 要求三条同时成立：

```java
@ConditionalOnWebApplication(type = SERVLET)                       // 必须是 Servlet 应用
@ConditionalOnClass(name = {                                       // 且以下类全部存在
        "javax.servlet.Servlet",                                   // Servlet API
        "org.springframework.context.ApplicationContext",
        "org.springframework.web.method.HandlerMethod",
        "org.springframework.web.servlet.DispatcherServlet",
        "io.microsphere.spring.web.method.support.HandlerMethodInterceptor",
        "io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension"
})
@ConditionalOnProperty(name = "microsphere.spring.boot.webmvc.enabled",
        matchIfMissing = true)                                     // 未显式关闭即启用
```

与 `release` 唯一有实质意义的差别就是 `javax.servlet.Servlet`。实际后果：

* 在 Boot 3/4 应用里条件不会命中，因为 `javax.servlet.Servlet` 已不存在 —— 这段自动配置会直接消失。这是保险丝，不是迁移
  方案：那条线请用 `0.2.x`。
* 非 Web 应用里 `@ConditionalOnWebApplication(SERVLET)` 不匹配。
* 由于 `@ConditionalOnClass` 按 `name` 声明，类缺失只是"条件不成立"，不会抛 `NoClassDefFoundError`。

```bash
# 条件评估报告里应能在 matchSessions 中看到 WebMvcAutoConfiguration
curl -s http://localhost:8080/actuator/conditions | jq '.matchSessions | keys[]' | grep -i webmvc
```

Boot 2.x 上，只有 `management.endpoint.conditions.enabled=true` 时 `/actuator/conditions` 才存在；本库的
`endpoints.properties` 默认把它关掉了，细节见 actuator 模块文档。

---

## 7. 可配置内容协商

`ConfigurableContentNegotiationManagerWebMvcConfigurer` 实现 `WebMvcConfigurer.configureContentNegotiation`。它反射拿到
`ContentNegotiationConfigurer` 内部的 `ContentNegotiationManagerFactoryBean` 字段，再用 Spring `DataBinder` 把
`microsphere.spring.webmvc.content-negotiation.` 前缀下的所有属性绑定到该工厂 Bean。

* 任何 `ContentNegotiationManagerFactoryBean` 的 setter 都能按键触达，新增选项不需要改代码。
* `mediaTypes` 接受 JSON 对象字面量，由专门的 `PropertyEditor` 转换。
* `contentNegotiationManager` 与 `servletContext` 是禁止绑定的字段。

```properties
microsphere.spring.webmvc.content-negotiation.enabled=true
microsphere.spring.webmvc.content-negotiation.favorParameter=true
microsphere.spring.webmvc.content-negotiation.parameterName=p
microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=true
microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=true
```

> Boot 2.1–2.3 上 `favorPathExtension` 仍然有效；从 Boot 2.6 起 Spring MVC 默认不再偏爱路径扩展名，除非你显式改回，
> 因此在这一线上基于参数的协商策略才是可移植的选择。

闸门关闭时，Boot 自己的管理器原样保留，只剩一条 `HeaderContentNegotiationStrategy` —— 这正是本模块"全部关闭"测试断言的
状态。

---

## 8. 互斥视图解析器

`ExclusiveViewResolverApplicationListener` 同时是 `ApplicationListener<ContextRefreshedEvent>` 与 `EnvironmentAware`，
而它的 `@ConditionalOnProperty` 没有 `matchIfMissing`，所以只有该键有值时 Bean 才存在：

```properties
microsphere.spring.webmvc.view-resolver.exclusive-bean-name=viewResolver
```

容器刷新时它保留被命名的那个 `ViewResolver`、把其余的降级，这正是"两个解析器都认领同一个视图"（模板引擎与
`InternalResourceViewResolver` 共存）这一常见问题的解法。不设置该属性时监听器根本不存在，Boot 的解析器顺序完全不受影响 ——
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
要看到输出，请提高级别：

```yaml
logging:
  level:
    io.microsphere.spring.webmvc.interceptor: DEBUG
    io.microsphere.spring.webmvc.method.support: DEBUG
    io.microsphere.spring.web.servlet.filter: DEBUG
```

---

## 10. 来自测试用例的示例

测试类与 `release` 是同样四个，但有两处差异值得在复制之前了解。

这一线的 `AbstractWebMvcAutoConfigurationTest` 只装载 **一个** `TestController`，并且导入
`javax.servlet.ServletException`：

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

这里没有 `RouterFunctionTestConfig.class`，也没有 `testUpdatePerson()` 这一步 —— 两者都是 `release` 分支后加的。若要把
`0.2.x` 的测试往回移植，需要先去掉 RouterFunction 相关夹具。

**默认状态。** `WebMvcAutoConfigurationTest` 不加任何属性，断言开箱形态：

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

**全部打开。** `WebMvcAutoConfigurationAllEnabledTest` 使用与 `release` 相同的十个属性，但断言改写成 Java 8 兼容的强制
转型，并且钉死了策略数量：

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

两条策略、且依赖顺序 —— 这比 `release` 版本的循环断言严格得多，也正是升级到 `0.2.x` 时最容易先炸出来的差异。

**全部关闭。** `WebMvcAutoConfigurationAllDisabledTest` 与 `release` 完全相同：

```java
void assertContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
    List<ContentNegotiationStrategy> strategies = contentNegotiationManager.getStrategies();
    assertEquals(1, strategies.size());

    ContentNegotiationStrategy strategy = strategies.get(0);
    assertTrue(strategy instanceof HeaderContentNegotiationStrategy);
}
```

> 全部打开/全部关闭两个测试设置的是 `microsphere.spring.webmvc.{filter,logging}.enabled`，而 Bean 闸门读取的是
> `microsphere.spring.boot.webmvc.{filter,logging}.enabled`。两个测试都没有断言 `assertNull(contentCachingFilter)`，
> 所以这处差异不会让它们失败 —— 但在你自己的配置里请使用带 `boot` 的那组键。

---

## 11. 配置属性清单

前缀链条：本模块的开关走 `microsphere.` + `spring.` + `boot.` + `webmvc.`，上游库走 `microsphere.` + `spring.` +
`webmvc.`。常量位于 `io.microsphere.spring.boot.webmvc.constants.PropertyConstants`，且每个字面量都被
`PropertyConstantsTest` 钉住：

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
`@ConfigurationProperty(type = boolean.class, source = APPLICATION_SOURCE)`，因此会被写进
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
| Boot 3 应用里什么都没注册 | 符合预期：条件要求 `javax.servlet.Servlet`。请改用 `0.2.x` 线，见 [../../current/zh/webmvc.md](../../current/zh/webmvc.md)。 |
| 想彻底移除本模块的一切 | `microsphere.spring.boot.webmvc.enabled=false`，总开关优先于单个 Bean 的闸门。 |
| `microsphere.spring.webmvc.filter.enabled=false` 没效果 | 这个键的命名空间不对：Bean 闸门是 `microsphere.spring.boot.webmvc.filter.enabled`。只有内容协商与视图解析器这两项确实不带 `boot`。 |
| 内容缓存带来内存开销 | `microsphere.spring.boot.webmvc.filter.enabled=false`。 |
| 看不到处理方法日志 | 保持 `microsphere.spring.boot.webmvc.logging.enabled=true`，并把 `io.microsphere.spring.webmvc.interceptor` 调到 `DEBUG`。 |
| 路径扩展名被忽略 | Boot 2.6+ 默认不再偏爱路径扩展名，请改用 `favorParameter=true` 并配置 `parameterName`。 |
| 我自己的配置类上也写了 `@EnableWebMvcExtension` | 两处共存；`microsphere.spring.@EnableWebMvcExtension.` 下的配置项优先于注解默认值。 |
| 从 `0.2.x` 移植的测试在 `RouterFunctionTestConfig` 上失败 | `0.1.x` 的基类测试上下文不含该夹具，请去掉它以及 `testUpdatePerson()` 调用。 |
| 自动配置在报告里出现两次 | Boot 2.7 上 `spring.factories` 与 `AutoConfiguration.imports` 都注册了它；Boot 会对同一个类去重，最终只生效一份。 |

---

**相关文档：** [core.md](./core.md) · [actuator.md](./actuator.md) · [webflux.md](./webflux.md)
