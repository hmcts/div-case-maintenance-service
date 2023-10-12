# Divorce Case Maintenance Service [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This is a case maintenance service. This service facilitates all the communication between Core Case Data and the
Divorce system.

## Setup

**Prerequisites**

- [JDK 17](https://openjdk.java.net/)
- [Docker](https://www.docker.com)

**Building**

The project uses [Gradle](https://gradle.org) as a build tool but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build project please execute the following command:

```bash
./gradlew build
```

If the build failed because of tests try running the following

```bash
./gradlew build -x test
```
This command will run the build task but it will exclude the test task

**Running**

When the distribution has been created in `build/install/div-case-maintenance-service` directory,
you can run the application by executing following command:

```bash
docker-compose up
```

(If `build/install/div-case-maintenance-service` does not exist try running `./gradlew build` again )
As a result the following container(s) will get created and started:
 - long living container for API application exposing port `4010`

## Testing

To run all unit tests please execute following command:

```bash
./gradlew test
```

**Coding style tests**

To run all checks (including unit tests) please execute following command:

```bash
./gradlew check
```
**Mutation tests**

To run all mutation tests execute the following command:

```bash
/gradlew pitest
```

**Integration tests**

To run all integration tests locally:

* Make a copy of `src/main/resources/example-application-aat.yml` as `src/main/resources/application-aat.yml`
* Make a copy of `src/integrationTest/resources/example-application-local.properties` as `src/integrationTest/resources/application-local.properties`
* Replace the `replace_me` secrets in the _newly created_ files. You can get the values from SCM and Azure secrets key vault (the new files are in .gitignore and should ***not*** be committed to git)
* Start the app with AAT config:
  * Using gradle: `./gradlew clean bootRunAat`
  * Using IntelliJ: edit Run Configuration and set Environment variables to `SPRING_PROFILES_ACTIVE=aat`
* Start the test with AAT config using `./gradlew clean functional`

### Running additional tests in the Jenkins PR Pipeline

1. Add one or more appropriate labels to your PR in GitHub. Valid labels are:

- ```enable_fortify_scan```
- ```enable_security_scan```
- ```enable_full_functional_tests```

2. Trigger a build of your PR in Jenkins.  Fortify scans will take place asynchronously as part of the Static Checks/Container Build step.
- Check the Blue Ocean view for live monitoring, and review the logs once complete for any issues.
- As Fortify scans execute during the Static Checks/Container Build step, you will need to ensure this is triggered by making a minor change to the PR, such as bumping the chart version.

##Developing
**API documentation**

API documentation is provided with Swagger:
 - `http://localhost:4010/swagger-ui.html` - UI to interact with the API resources

The `documentation.swagger.enabled` property should be 'true' to enable Swagger.

**Versioning**

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

**Standard API**

We follow [RESTful API standards](https://hmcts.github.io/restful-api-standards/).

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
