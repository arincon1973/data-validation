# data-validation

A Spring Boot service that validates student application data. It exposes a single endpoint,
`POST /validate`, which runs a sequence of rules against the request and returns one result per check.

## Prerequisites

- JDK 25
- No Maven install needed; the project includes the Maven wrapper (`./mvnw`, or `mvnw.cmd` on Windows).

## Build

```sh
./mvnw clean package
```

This compiles the code, runs the tests, and produces `target/data-validator-0.0.1-SNAPSHOT.jar`.
To build without running the tests, add `-DskipTests`.

## Run

Either run from source:

```sh
./mvnw spring-boot:run
```

or run the packaged jar:

```sh
java -jar target/data-validator-0.0.1-SNAPSHOT.jar
```

The service listens on port 8080. To use a different port, add `--server.port=9090` to the
`java -jar` command.

Check that it is up:

```sh
curl localhost:8080/actuator/health
```

## Use

Send a request to `/validate`:

```sh
curl -X POST localhost:8080/validate \
  -H 'Content-Type: application/json' \
  -d '{
    "studentInfo": {
      "firstName": "Jane",
      "lastName": "Smith",
      "ssn": "123-45-6789",
      "dateOfBirth": "2003-05-15"
    },
    "dependencyStatus": "independent",
    "maritalStatus": "married",
    "household": { "numberInHousehold": 2, "numberInCollege": 1 },
    "income": { "studentIncome": 5000 },
    "stateOfResidence": "CA",
    "spouseInfo": { "firstName": "John", "lastName": "Smith", "ssn": "987-65-4321" }
  }'
```

`spouseInfo` is optional, but required (first name, last name and SSN) when `maritalStatus` is `married`.

The response lists one entry per check. The first entry is the overall `Status`:

```json
{
  "results": [
    { "description": "Status", "status": "Validation passed", "severity": "all valid" },
    { "description": "Dependency Status", "status": "Valid", "severity": "N/A" },
    ...
    { "description": "Parent Income", "status": "Valid", "severity": "N/A" },
    { "description": "Spouse", "status": "Valid", "severity": "N/A" }
  ]
}
```

`severity` values:

| Value | Meaning |
| --- | --- |
| `all valid` / `invalid` | Overall `Status` entry: every rule passed / at least one rule failed. |
| `N/A` | The check passed. |
| `reject` | The check failed in a rule with `ruleOrder()` below 1 (allowed values, conflicting values). |
| `needs review` | The check failed in any other rule. |

A request that is missing required fields, or that has a `dateOfBirth` not in `yyyy-MM-dd` format,
returns `400 Bad Request`.

Rule timings are available at `/actuator/metrics/validation.rule`.

## Test

Run all tests:

```sh
./mvnw test
```

Run a single test class or method:

```sh
./mvnw test -Dtest=AgeRuleTests
./mvnw test -Dtest=DataValidatorControllerTests#reportsValidationPassed
```

Test reports are written to `target/surefire-reports/`.
