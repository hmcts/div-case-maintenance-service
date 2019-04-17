# Divorce Case Maintenance Service

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This is a case maintenance service. This service facilitates all the communication between Core Case Data and the
Divorce system.

## Getting started

### Prerequisites

- [JDK 8](https://www.oracle.com/java)
- [Docker](https://www.docker.com)

### Building

The project uses [Gradle](https://gradle.org) as a build tool but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build project please execute the following command:

```bash
    ./gradlew build
```

### Running

First you need to create distribution by executing following command:

```bash
    ./gradlew build
```

When the distribution has been created in `build/install/div-case-maintenance-service` directory,
you can run the application by executing following command:

```bash
    docker-compose up
```

As a result the following container(s) will get created and started:
 - long living container for API application exposing port `4010`

### API documentation

API documentation is provided with Swagger:
 - `http://localhost:4010/swagger-ui.html` - UI to interact with the API resources

## Developing

If using IntelliJ:
    - Download the SCM JSON file from the AAT environment
    - Save as `settings-aat.json` (be careful to call it this exactly)
    - Add the ENV plugin to IntelliJ (search plugin repo)
    - Add the settings-aat.json as a config to the Spring runner, under the ENV tab
    - Run the Spring runner with VM settings, for the proxy:
        `-Dhttp.proxyHost=proxyout.reform.hmcts.net -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxyout.reform.hmcts.net -Dhttps.proxyPort=8080`
    - Application should start with no errors
    
### Integration tests

To run the Integration Test suite:
    - Copy `example-application-local.properties` as `application-local.properties`
    - Replace the missing environment values (replace_me), found in your `settings-aat.json`
    - Run the tests
    
### Unit tests

To run all unit tests please execute following command:

```bash
    ./gradlew test
```

### Coding style tests

To run all checks (including unit tests) please execute following command:

```bash
    ./gradlew check
```
### Mutation tests

To run all mutation tests execute the following command:

```
/gradlew pitest

```

## Versioning

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

## Standard API

We follow [RESTful API standards](https://hmcts.github.io/restful-api-standards/).

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
