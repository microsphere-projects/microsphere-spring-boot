# Test 模块

> [English](../test.md) · 文档索引：[中文](../../zh/README.md)

> **分支：** `release` · **版本线：** `0.2.x` · **Artifact：** `microsphere-spring-boot-test`
> **Spring Boot：** `3.0.13` – `4.1.1` · **Java：** 17+ · **作用域建议：** `test`

目录：
[1 概览](#1-模块概览) ·
[2 版本信息](#2-版本信息) ·
[3 基类层次](#3-基类层次) ·
[4 契约](#4-契约) ·
[5 断言语义](#5-断言语义) ·
[6 用法示例](#6-用法示例) ·
[7 依赖与常见问题](#7-依赖与常见问题)

---

## 1. 模块概览

这个模块把“自动配置类应当被正确装配、可被开关关闭、在缺失类时应被过滤”这三件事固化为**继承即得**的 JUnit 5 测试。写一个
自动配置类时，只要继承对应 Web 类型的基类、标注 `@SpringBootTest`、实现三个 `configure*` 方法，就能得到三个自动化测试，不必每次
手写 `ApplicationContextRunner` 断言。本模块**不含任何 `src/main/resources`**（没有 `spring.factories`，也没有默认属性文件），
纯粹是测试基类库。

---

## 2. 版本信息

| 项目 | 值 |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-spring-boot-test` |
| 最新发布 | `0.2.34` |
| 分支在研 | `0.2.35-SNAPSHOT` |
| Spring Boot profile 固定 | `3.0.13 / 3.1.12 / 3.2.12 / 3.3.13 / 3.4.13 / 3.5.16（默认）/ 4.0.8 / 4.1.1` |
| Java | `17`，CI 验证 JDK `17 / 21 / 25` |
| JUnit | Jupiter BOM `6.1.3`（parent POM） |
| 测试框架 | JUnit 5 `org.junit.jupiter.api.Test` + Spring Boot 的 `ApplicationContextRunner` 家族；**不使用** `@ExtendWith`/`SpringExtension` |
| 依赖性质 | 上游依赖均为 `<optional>`，由使用方提供（见 §7） |

---

## 3. 基类层次

包：`io.microsphere.spring.boot.test`

| Class | 泛型声明 | Runner | 适用 |
|---|---|---|---|
| `AbstractAutoConfigurationTest<A, R extends AbstractApplicationContextRunner>` | 抽象基类 | 由 `R` 决定 | — |
| `AutoConfigurationTest<A>` | `extends AbstractAutoConfigurationTest<A, ApplicationContextRunner>` | `ApplicationContextRunner` | 非 Web 自动配置 |
| `WebAutoConfigurationTest<A>` | `…<A, WebApplicationContextRunner>` | `WebApplicationContextRunner` | Servlet / MVC |
| `ReactiveWebAutoConfigurationTest<A>` | `…<A, ReactiveWebApplicationContextRunner>` | `ReactiveWebApplicationContextRunner` | WebFlux |

类型参数：`A` = 被测自动配置类（通过反射解析泛型得到）；`R` = runner 类型，同样从子类声明中解析。

构造函数里强制检查的关键约束：

* 具体测试类必须标注 `@SpringBootTest`，否则构造失败并报 `The @SpringBootTest must not be annotated on …`。
* 若 `webEnvironment == NONE`（非 Web），runner **必须**是 `ApplicationContextRunner`。

---

## 4. 契约

基类从 `@SpringBootTest` 注解上读取配置，这是理解本模块的关键：

| 来源 | 用途 |
|---|---|
| `@SpringBootTest(properties = {...})` | `runner.withPropertyValues(...)`，即测试上下文的属性基线 |
| `@SpringBootTest(classes = {...})` | `runner.withConfiguration(of(classes))`，额外参与装配的类 |
| 泛型参数 `A` | `runner.withConfiguration(of(autoConfigurationClass))`，被测自动配置类 |

子类**必须**实现下面三个方法（都是 `protected abstract`，各自收到一个待填充的集合）：

```java
protected abstract void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses);
protected abstract void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues);
protected abstract void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses);
```

| Method | 你应放入 | 生成的测试 |
|---|---|---|
| `configureAutoConfiguredClasses` | 除被测类之外还应存在的 Bean 类（可留空） | `testAutoConfiguredClasses()` |
| `configureGlobalDisabledPropertyValues` | 形如 `"key=false"` 的关闭开关，逐个验证 | `testOnGlobalDisabledProperty()` |
| `configureGlobalMissingClasses` | 缺失时应导致自动配置不生效的类 | `testOnGlobalMissingClass()` |

另外可用的成员：`newRunner()`（构造并初始化一个新 runner）、`initRunner(R)`、`getPropertyValues()`、`getClasses()`、
`getAutoConfiguredClasses()`（自动包含被测类自身）、常量 `SPRING_BOOT_TEST_CLASS`。

---

## 5. 断言语义

| Assertion | 实际检查 |
|---|---|
| `assertAutoConfiguredClass(Class<?>)` | `assertThat(context).hasSingleBean(clazz)` |
| `assertDisabledProperty(String propertyValue, Class<?>... beans)` | 追加该属性后 `runner.withPropertyValues(propertyValue)`，断言每个 Bean 类 `doesNotHaveBean` |
| `assertFilteredClass(String className, Class<?>... beans)` | 把 `runner.withPropertyValues` 换成 `withClassLoader(new FilteredClassLoader(className))`，断言 `doesNotHaveBean` |

三个实例方法与三个静态泛型方法同名并存（`assertAutoConfiguredClass` / `assertDisabledProperty` / `assertFilteredClass`），
静态版本的签名为：

```java
public static <R extends AbstractApplicationContextRunner<R, C, A>,
               C extends ConfigurableApplicationContext,
               A extends ApplicationContextAssertProvider<C>>
void assertAutoConfiguredClass(R runner, Class<?> autoConfiguredClass)
```

---

## 6. 用法示例

### 6.1 非 Web 自动配置

以仓库内的真实写法为模板：

```java
package com.example;

import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jmx.export.MBeanExporter;

import java.util.Set;

@SpringBootTest(
        classes = MyAutoConfigurationTest.class,   // 通常把测试类自身列入 classes
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jmx.enabled=true"     // 基线属性
)
public class JmxAutoConfigurationTest extends AutoConfigurationTest<JmxAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        // 无额外 Bean 类
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

`configureAutoConfiguredClasses` 留空也是有效的：基类总会把泛型参数 `A`（被测自动配置类）加入断言集合。

### 6.2 Servlet Web 自动配置

```java
@SpringBootTest(
        classes = WebMvcAutoConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,   // 非 NONE => 判定为 Web 应用
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

### 6.3 响应式

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

### 6.4 复用 runner 做额外断言

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
        // actuatorTaskScheduler 依赖 MeterRegistry，属性开关不适用
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> missing) {
        missing.add(io.micrometer.core.instrument.MeterRegistry.class);
    }
}
```

---

## 7. 依赖与常见问题

引入方式（BOM 已管理版本）：

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-spring-boot-test</artifactId>
    <scope>test</scope>
</dependency>
```

本模块上游依赖全部标记 `<optional>true</optional>`（JUnit Jupiter、`spring-boot-starter-test`、`spring-boot-starter-web`、
`spring-boot-starter-webflux`、`microsphere-spring-*`、servlet API、Jackson、H2），因此**你需要自己提供这些依赖**。

| 现象 | 处理 |
|---|---|
| 构造时报 `The @SpringBootTest must not be annotated on …` | 具体测试类缺 `@SpringBootTest`；补上（该信息即字面含义：必须标注）。 |
| 非 Web 测试报 runner 类型不匹配 | `webEnvironment = NONE` 时只能用 `AutoConfigurationTest`（`ApplicationContextRunner`）。 |
| `testOnGlobalMissingClass` 失败 | 你放进 `configureGlobalMissingClasses` 的类并非该自动配置的条件依赖；`FilteredClassLoader` 按**类名**过滤，传错类等于没过滤。 |
| `assertDisabledProperty` 失败 | 说明该自动配置没有对应的属性开关（例如 actuator 调度器只看 Bean 是否存在），把它移到 `configureGlobalMissingClasses`。 |
| 想要更细的定制 | 直接用 `newRunner()` 自行组装 `withPropertyValues` / `withBean` / `withConfiguration`。 |

---

**上一级：** [README.md](./README.md) · 相关：[core.md](./core.md)（条件与属性）·
[actuator.md](./actuator.md) · [webmvc.md](./webmvc.md) · [webflux.md](./webflux.md)
