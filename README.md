# shop-manager
Automated Software Testing project that implements a shop sales management application.

[![Maven build](https://github.com/Riccardo-Giannuzzi/shop-manager/actions/workflows/maven.yml/badge.svg)](https://github.com/Riccardo-Giannuzzi/shop-manager/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/Riccardo-Giannuzzi/shop-manager/badge.svg?branch=main)](https://coveralls.io/github/Riccardo-Giannuzzi/shop-manager?branch=main)

[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Reliability issues](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=software_quality_reliability_issues)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Security issues](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=software_quality_security_issues)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Maintainability issues](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=software_quality_maintainability_issues)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Riccardo-Giannuzzi_shop-manager&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=Riccardo-Giannuzzi_shop-manager)

# Build and Run
As long as these requirements are met, the build and execution of the project should be very straightforward:
- `Java 17 or later`
- `Maven`
- `Docker and Docker Compose`
- `Make`


The `Makefile` provides simple one-command solutions for running the tests and coverage, running mutation testing and the final application with either MongoDB or MariaDB.

To run the complete test suite and generate the JaCoCo coverage report:
```
make run-coverage
```

To run mutation testing with PIT:
```
make run-pit
```

To build and run the application using MongoDB:
```
make run-app-mongo
```
To build and run the application using MariaDB:
```
make run-app-maria
```
These two run commands build the application, start the corresponding database container, wait for it to become available, and then launch the packaged Swing application.
The repository includes the required Docker Compose configurations for both MongoDB and MariaDB databases. 

When finished, the database containers can be stopped and removed with:
```
make stop-containers
```

Alternatively if the `Make` dependency can't be satisfied the Maven and Docker Compose commands used by the `Makefile` can be executed manually.
