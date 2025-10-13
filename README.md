# Microsphere Spring Boot

> Microsphere Projects for Spring Boot

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-spring-boot)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-spring-boot)
[![Maven Build](https://github.com/microsphere-projects/microsphere-spring-boot/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-spring-boot/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-spring-boot/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-spring-boot)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-spring-boot.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-spring-boot.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/microsphere-projects/microsphere-spring-boot.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-spring-boot "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/microsphere-projects/microsphere-spring-boot.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-spring-boot "Percentage of issues still open")

Microsphere Spring Boot is a collection of libraries that extends Spring Boot's capabilities with additional features
focused on configuration management, application diagnostics, and enhanced monitoring. The project is structured as a
multi-module Maven project that follows Spring Boot's conventions while providing value-added functionality.

## Purpose and Scope

The Microsphere Spring Boot project is a collection of Spring Boot extensions that enhance observability, configuration
management, and operational capabilities for Spring Boot applications. It provides:

- Enhanced Auto-Configuration: Sophisticated auto-configuration mechanisms with filtering and property management
- Custom Actuator Endpoints: Extended Spring Boot Actuator with additional endpoints for configuration metadata and
  application artifacts
- Monitoring Extensions: Enhanced task scheduling with metrics integration via Micrometer
- Configuration Management: Advanced configuration binding and property management features
- Multi-Version Support: Compatibility across Spring Boot versions 3.0 through 3.4

## Modules

| **Module**                               | **Purpose**                                                                        |
|------------------------------------------|------------------------------------------------------------------------------------|
| **microsphere-spring-parent**            | Defines the parent POM with dependency management and Spring Boot version profiles |
| **microsphere-spring-boot-dependencies** | Centralizes dependency management for all project modules                          |
| **microsphere-spring-boot-core**         | Provides core functionality for enhanced Spring Boot applications                  |
| **microsphere-spring-boot-actuator**     | Extends Spring Boot Actuator with additional endpoints and monitoring capabilities |

## Getting Started

The easiest way to get started is by adding the Microsphere Spring Boot BOM (Bill of Materials) to your project's
pom.xml:

```xml
<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Spring Boot Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-boot-dependencies</artifactId>
            <version>${microsphere-spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

`${microsphere-spring-boot.version}` has two branches:

| **Branches** | **Purpose**                               | **Latest Version** |
|--------------|-------------------------------------------|--------------------|
| **0.2.x**    | Compatible with Spring Boot 3.0.x - 3.2.x | 0.2.2              |
| **0.1.x**    | Compatible with Spring Boot 2.0.x - 2.7.x | 0.1.2              |

Then add the specific modules you need:

```xml
<dependencies>
    <!-- Microsphere Spring Boot Core -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-core</artifactId>
    </dependency>

    <!-- Microsphere Spring Boot Actuator -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-boot-actuator</artifactId>
    </dependency>
</dependencies>
```

### Example : Using the default properties to disable the Auto-Configuration of Spring Boot

The module `microsphere-spring-boot-core` offers the default properties, which can be used to disable the
auto-configuration:

1. To add the default properties resource(located classpath `config/default/test.properties`):

```properties
### Exclude Spring Boot Built-in Auto-Configuration Class
microsphere.autoconfigure.exclude=\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

> Alternatively, you can also use the `@EnableAutoConfiguration` annotation with `exclude` attribute or the Spring
> property `spring.autoconfigure.exclude` to exclude the auto-configuration, however, these two approaches can't merge
> the previous properties.

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-spring-boot.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw package
```

- Windows:

```powershell
mvnw.cmd package
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search the [issues](https://github.com/microsphere-projects/microsphere-spring-boot/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-spring-boot/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-spring-boot)

[ZRead Host](https://zread.ai/microsphere-projects/microsphere-spring-boot)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-spring-boot/wiki)

### JavaDoc

- [microsphere-spring-boot-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-boot-core)
- [microsphere-spring-boot-actuator](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-boot-actuator)

## License

The Microsphere Spring is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
