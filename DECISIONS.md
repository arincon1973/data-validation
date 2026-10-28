# Decisions

## Design decisions

### One endpoint that always returns a full report

`POST /validate` runs every rule and returns one entry per check, with an overall `Status` entry first.
A request that fails validation still returns `200 OK`; the outcome is in the response body.
This way a caller sees every problem in one round trip instead of fixing them one at a time.

This also allows for and doing incremental improvements to this API and return customized info to the caller if the requirements call for it.

### Two layers of validation

- **Shape** (Bean Validation, `@NotNull` / `@Valid` on the request records): required fields are present
  and dates parse as `yyyy-MM-dd`. A request that fails this returns `400 Bad Request` and no rules run.
- **Business rules** (`Rule` implementations): the values make sense. Failures are reported in the
  response body as described above.

Rules can therefore assume required fields are present and typed correctly.

This higher severity of shape rules allow for future API improvements to stop processing of applications that are being filled out with meaningless data and to add other checks for patterns like cross site scripting.

### Rules are small, independent classes

Each check is a class that implements `Rule`:

```java
boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart);
float ruleOrder();
```

A rule appends its own entries and returns whether it passed. Adding a check means writing a new class
and registering it in `RulesConfig`; the controller doesn't change. Each rule has its own unit test.

### Rule order is explicit

`RulesConfig` builds the rule list once at startup, sorted by `ruleOrder()`. The order controls the
order of entries in the response and is used to derive severity.
`ruleOrder()` is a `float` so a rule can be inserted between two existing ones (for example, 0.1 and 0.2)
without renumbering the others.

### Severity belongs to the rule

The `Rule` interface decides severity, not the controller:

| Situation | `severity` |
| --- | --- |
| Check passed | `N/A` |
| Check failed, rule has `ruleOrder() < 1` | `reject` |
| Check failed, any other rule | `needs review` |
| Overall `Status`: every rule passed / at least one failed | `all valid` / `invalid` |

Rules build entries with two helpers on `Rule`: `entry(description, status)` for failures, which uses
the rule's `severity()`, and `valid(description)` for passes. A rule can override `severity()` if it
needs a different value.

The rules ordered below 1 (`AllowedValuesRule`, `ConflictingValuesRule`) check whether the
application's basic answers are usable at all, so their failures are rejections. The other rules catch
problems a person can look into and correct.

### Observability

- **Metrics:** each rule run is timed with Micrometer as `validation.rule`, tagged with the rule name and
  `passed`/`failed`. Exposed through Spring Boot Actuator at `/actuator/metrics/validation.rule`.
- **Request logging:** `RequestLoggingFilter` gives each request a UUID, logs its start and finish
  (method, path, status, duration), and puts the ID in the logging MDC as `requestId`.
  Request bodies are not logged.
- **Health:** `/actuator/health`. Only `health` and `metrics` are exposed.

### Sensitive data stays out of responses and logs

SSN error messages describe the expected format but never include the submitted value. Other messages
do echo the value (for example, `Invalid US state abbreviation (XX)`) because it helps the caller fix it.

## Trade-offs

| Decision | Benefit | Cost |
| --- | --- | --- |
| Run every rule, even after a `reject` | Caller sees every problem at once | Later rules run on data already known to be unusable, and may report follow-on errors |
| `200 OK` for failed validation | Validation outcome is data, not an error; easy for clients to display | Clients must read the body; HTTP status alone doesn't say whether data is valid |
| Severity derived from `ruleOrder()` | One place defines severity; no per-rule config | Ordering and severity are coupled: moving a rule across 1 changes its severity. Override `severity()` to decouple |
| Rules registered by hand in `RulesConfig` | Full list and order visible in one file | A new rule does nothing until someone adds it there |
| Rules and limits hard-coded | Simple, no config to manage | Changing a limit (age, state list, school start date) needs a code change and redeploy |
| One entry per failed check (some rules add several) | Each problem is reported separately (e.g. each missing spouse field) | The response length varies, so clients shouldn't rely on entry positions |
| Severity values are strings | Readable JSON, no client enum to share | Typos aren't caught by the compiler |
| Rules in code | Complex rules are easier to handle | Code change and deployment process are needed
| Rules order from simple to complex | Allows to quickly check simple rules that do not have dependencies on multiple field | This may not be the order needed by the client

## Assumptions

These are built into the rules. Some reflect business rules that should be confirmed.

**Dates and age**
- Age is calculated on the school start date, **2027-09-01**, which is hard-coded in the controller.
- A valid student is 14 to 120 years old on that date (inclusive).
- `dateOfBirth` must be `yyyy-MM-dd`.

**Allowed values** (case-sensitive)
- `dependencyStatus`: `dependent` or `independent`.
- `maritalStatus`: `single` or `married`.
- A student can't be both `dependent` and `married`.

**SSN**
- 9 digits (`123456789`) or dashed (`123-45-6789`). Only the format is checked, not whether the
  number was ever issued.

**State of residence**
- One of the 50 US state abbreviations, uppercase. DC and US territories are not accepted.

**Household**
- `numberInHousehold` is at least 1; at least 2 for a dependent student (the student plus a parent).
- `numberInCollege` is at least 1 (the student).
- The service doesn't check that `numberInCollege` is no more than `numberInHousehold`.

**Income**
- Amounts are whole numbers (`Long`); currency and cents aren't represented.
- `studentIncome` is required and can't be negative.
- `parentIncome` is required for dependent students and can't be negative; it's ignored for
  independent students.

**Spouse**
- `spouseInfo` is required for married students, with first name, last name and a valid SSN.
- It's ignored for single students, even if provided.

## Example API usage

The examples assume the service runs on `localhost:8080`. The responses below were produced by the
service; long responses are shortened to the entries that matter.

### Valid application

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

`200 OK`:

```json
{
  "results": [
    { "description": "Status", "status": "Validation passed", "severity": "all valid" },
    { "description": "Dependency Status", "status": "Valid", "severity": "N/A" },
    { "description": "Marital Status", "status": "Valid", "severity": "N/A" },
    { "description": "Conflicting Values", "status": "Valid", "severity": "N/A" },
    { "description": "Age", "status": "Valid", "severity": "N/A" },
    { "description": "SSN", "status": "Valid", "severity": "N/A" },
    { "description": "Student Income", "status": "Valid", "severity": "N/A" },
    { "description": "State of Residence", "status": "Valid", "severity": "N/A" },
    { "description": "Household", "status": "Valid", "severity": "N/A" },
    { "description": "Parent Income", "status": "Valid", "severity": "N/A" },
    { "description": "Spouse", "status": "Valid", "severity": "N/A" }
  ]
}
```

### Rejected application

A dependent, married student fails a rule ordered below 1:

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
    "dependencyStatus": "dependent",
    "maritalStatus": "married",
    "household": { "numberInHousehold": 4, "numberInCollege": 1 },
    "income": { "studentIncome": 5000, "parentIncome": 65000 },
    "stateOfResidence": "CA",
    "spouseInfo": { "firstName": "John", "lastName": "Smith", "ssn": "987-65-4321" }
  }'
```

`200 OK` (passing entries omitted):

```json
{
  "results": [
    { "description": "Status", "status": "Validation failed", "severity": "invalid" },
    { "description": "Conflicting Values", "status": "Student cannot be both dependent and married", "severity": "reject" },
    ...
  ]
}
```

### Application that needs review

Several later rules fail: the student is too young, the SSN is malformed and the state is unknown.

```sh
curl -X POST localhost:8080/validate \
  -H 'Content-Type: application/json' \
  -d '{
    "studentInfo": {
      "firstName": "Jane",
      "lastName": "Smith",
      "ssn": "12345",
      "dateOfBirth": "2013-09-02"
    },
    "dependencyStatus": "independent",
    "maritalStatus": "single",
    "household": { "numberInHousehold": 1, "numberInCollege": 1 },
    "income": { "studentIncome": 5000 },
    "stateOfResidence": "XX"
  }'
```

`200 OK` (passing entries omitted):

```json
{
  "results": [
    { "description": "Status", "status": "Validation failed", "severity": "invalid" },
    { "description": "Age", "status": "Student too young (13 years old, < 14)", "severity": "needs review" },
    { "description": "SSN", "status": "Invalid SSN format (expected 9 digits or xxx-xx-xxxx)", "severity": "needs review" },
    { "description": "State of Residence", "status": "Invalid US state abbreviation (XX)", "severity": "needs review" },
    ...
  ]
}
```

### Malformed request

Missing required fields (or a `dateOfBirth` not in `yyyy-MM-dd`) fail before any rule runs:

```sh
curl -X POST localhost:8080/validate \
  -H 'Content-Type: application/json' \
  -d '{ "studentInfo": { "firstName": "Jane" } }'
```

`400 Bad Request`:

```json
{ "timestamp": "2026-10-28T18:39:11.496Z", "status": 400, "error": "Bad Request", "path": "/validate" }
```

### Health and metrics

```sh
curl localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}

curl localhost:8080/actuator/metrics/validation.rule
# Timer totals across all rules. Filter by tag:
curl 'localhost:8080/actuator/metrics/validation.rule?tag=rule:AgeRule&tag=result:failed'
```
# Questions 

## Throughput Requirements
What are the throughput requirements (e.g., requests per second, peak season load, or others)? 
I added metrics to support testing for througput requirements

## Batch requests
Does the system accept a single application at a time or is it expected to support batch applications?
The rules loop in the configuration and the design to always due the full validation of the data in the request in intended to be extensible to handle batch application.

## Rules changes
Who makes the rules changes? Is a code change and deployment ok (developers)? Or is it policy staff?
If policy staff needs to make the change, an alternative storage and editing method need to be used.

## Age requirement
What is the reference date this age is calculated against? 
Instead of a hardcoded day, a reference date based on the year and the version of the rules could be used.
