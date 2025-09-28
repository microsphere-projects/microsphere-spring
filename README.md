# Microsphere Spring

[![Maven Build](https://github.com/microsphere-projects/microsphere-spring/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-spring/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-spring/branch/dev/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-spring)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-spring.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-spring.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/microsphere-projects/microsphere-spring.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-spring "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/microsphere-projects/microsphere-spring.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-spring "Percentage of issues still open")


> Microsphere Projects for Spring Framework

The Microsphere Spring framework is built upon a sophisticated multi-layered architecture that enhances standard Spring Framework capabilities through modular extensions and advanced integration patterns. This architecture focuses on providing enhanced dependency injection, event processing, web endpoint management, and caching capabilities while maintaining seamless compatibility with existing Spring applications.

## Project Purpose and Scope
Microsphere Spring aims to address common challenges and limitations in Spring applications by offering:

1. Enhanced bean lifecycle management with dependency analysis and parallel instantiation
2. Extended configuration system with listenable environment and advanced property resolution
3. Improved web framework capabilities for both Spring MVC and WebFlux
4. Flexible event processing with interception mechanisms
5. Integration with additional technologies like Google Guice and P6Spy for JDBC monitoring

## Project Structure
### Module Organization
| **Module** | **Purpose** | **Key Features** |
| --- | --- | --- |
| **microsphere-spring-parent** | Parent POM with dependency management | Centralized version control, dependency BOMs |
| **microsphere-spring-dependencies** | External dependency versions | Framework compatibility matrix |
| **microsphere-spring-context** | Core Spring Context enhancements | Bean utilities, configuration management |
| **microsphere-spring-web** | Web framework extensions | Endpoint mapping, request handling |
| **microsphere-spring-webmvc** | Spring MVC specific extensions | Controller enhancements, method support |
| **microsphere-spring-webflux** | Reactive web extensions | WebFlux utilities, reactive patterns |
| **microsphere-spring-jdbc** | Database access enhancements | Connection utilities, query support |
| **microsphere-spring-guice** | Google Guice integration | Dependency injection bridge |
| **microsphere-spring-test** | Testing framework integration | Test utilities, mocking support |


## Documentation
### Wiki
![](https://deepwiki.com/badge.svg)(AI Generated)

![](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff) (AI Generated)

[Github Host](https://github.com/microsphere-projects/microsphere-spring/wiki)

### JavaDoc
[**microsphere-spring-context**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-context)

[**microsphere-spring-web**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-web)

[**microsphere-spring-webmvc**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-webmvc)

[**microsphere-spring-webflux**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-webflux)

[**microsphere-spring-jdbc**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-jdbc)

[**microsphere-spring-guice**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-guice)

[**microsphere-spring-test**](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-spring-test)

## License
The Microsphere Spring is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).

