# Employee CRUD API

A Java 21 / Spring Boot 3.5.7 / Maven 3.6.3+ project using PostgreSQL 17,
Spring Data JPA, Jakarta Validation and Flyway. No Lombok or IDE plugins needed.

## Run locally

Install **JDK 21** (set JAVA_HOME and put its bin directory on PATH) and
**Maven 3.6.3+**, and **Docker Desktop with Docker Compose**. Start Docker Desktop first.
Install Maven separately and confirm that java -version and mvn -version both show Java 21. First build needs internet access to download dependencies.

Open a terminal in this folder.

### Windows PowerShell

```powershell
docker compose up -d --wait
mvn test
mvn spring-boot:run
```

### macOS / Linux

```sh
docker compose up -d --wait
mvn test
mvn spring-boot:run
```

The API listens at http://localhost:8080/api/employees.
Flyway creates the table on startup; Hibernate validates it rather than creating it.
Stop the app with Ctrl+C; stop PostgreSQL with `docker compose down`.
The named Docker volume preserves data between restarts.

If using an existing PostgreSQL server, create an empty database and configure the
variables below instead of running Compose. The database user must be able to
create tables in its schema. Port 5432 must be available for the supplied Compose setup.

## Configuration

| Environment variable | Default |
| --- | --- |
| DB_URL | jdbc:postgresql://localhost:5432/employee_db |
| DB_USERNAME | employee |
| DB_PASSWORD | employee_local |
| PORT | 8080 |

PowerShell example (set these before running Maven or the JAR):

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/employee_db"
$env:DB_USERNAME = "employee"
$env:DB_PASSWORD = "employee_local"
```

On macOS/Linux use `export DB_PASSWORD='your-password'`, etc.
The included credentials are for local development. This starter has no authentication.
Configure credentials, TLS and access controls before deploying it publicly.

## API contract

| Method | Path | Success |
| --- | --- | --- |
| POST | /api/employees | 201, employee JSON and Location header |
| GET | /api/employees?page=0&size=20 | 200, paginated employees ordered by empid |
| GET | /api/employees/{empid} | 200, employee JSON |
| PUT | /api/employees/{empid} | 200, fully replaced editable fields |
| DELETE | /api/employees/{empid} | 204, no body |

POST and PUT accept the same body; all three fields are required.
Do not send empid: PostgreSQL generates it. Unknown JSON fields are rejected.
Names and cities must be nonblank and at most 100 characters. Leading/trailing
whitespace is removed. yearsOfExperience must be an integer from 0 through 80.
IDs must be positive; page starts at 0; size must be between 1 and 100.
PUT is a full update and does not create a missing employee.

Request:

```json
{"name":"Ada Lovelace","yearsOfExperience":5,"city":"Boston"}
```

Response (the ID varies):

```json
{"empid":1,"name":"Ada Lovelace","yearsOfExperience":5,"city":"Boston"}
```

List response:

```json
{
  "content":[{"empid":1,"name":"Ada Lovelace","yearsOfExperience":5,"city":"Boston"}],
  "page":0,
  "size":20,
  "totalElements":1,
  "totalPages":1
}
```

### Try a complete CRUD sequence in PowerShell

Keep the server running and open a second terminal:

```powershell
$base = "http://localhost:8080/api/employees"
$body = @{ name = "Ada Lovelace"; yearsOfExperience = 5; city = "Boston" } | ConvertTo-Json
$employee = Invoke-RestMethod -Method Post -Uri $base -ContentType "application/json" -Body $body
$id = $employee.empid
Invoke-RestMethod -Uri "$base/$id"
Invoke-RestMethod -Uri "${base}?page=0&size=20"
$update = @{ name = "Ada Lovelace"; yearsOfExperience = 6; city = "Seattle" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "$base/$id" -ContentType "application/json" -Body $update
Invoke-RestMethod -Method Delete -Uri "$base/$id"
```

### curl examples (macOS / Linux)

```sh
curl -i -X POST http://localhost:8080/api/employees \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","yearsOfExperience":5,"city":"Boston"}'
# Replace 1 below with the empid from the POST response.
curl http://localhost:8080/api/employees/1
curl 'http://localhost:8080/api/employees?page=0&size=20'
curl -X PUT http://localhost:8080/api/employees/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","yearsOfExperience":6,"city":"Seattle"}'
curl -i -X DELETE http://localhost:8080/api/employees/1
```

## Errors

Errors use Spring ProblemDetail JSON (`application/problem+json`).
Invalid requests return 400; unknown employees return 404 (including repeated
deletion). Unsupported methods return 405 and unsupported content types return 415.
Unexpected failures return a generic 500; details are logged server-side.

Example field validation error (instance depends on the requested URL):

```json
{
  "type":"about:blank",
  "title":"Bad Request",
  "status":400,
  "detail":"Request validation failed",
  "instance":"/api/employees",
  "errors":{"name":"must not be blank"}
}
```

## Tests and packaging

```powershell
# Unit and MVC tests; no database or Docker required:
mvn test

# Full HTTP CRUD + Flyway + real PostgreSQL; Docker must be running:
mvn verify -Pintegration

# Run both suites and produce the executable JAR:
mvn clean verify -Pintegration

java -jar target/employee-api-0.0.1-SNAPSHOT.jar
```

The Maven commands are the same on Windows, macOS and Linux.
The integration suite starts a separate disposable PostgreSQL container on a random
port; it does not use or modify your local employee_db. Docker is required and
missing Docker causes a failure rather than silently skipping that suite.
Default mvn test and mvn package exclude integration tests; use mvn verify -Pintegration to run both suites. Use mvn clean package for an executable JAR without Docker.

Test results: target/surefire-reports/ and target/failsafe-reports/.

Included coverage:
- Service: trimming on creation, missing employee, updating, missing deletion.
- MVC: creation/Location header, field validation, 404, pagination bounds, malformed JSON.
- PostgreSQL integration: startup migration/schema validation and complete CRUD lifecycle.

Verification in the delivery environment was limited to source and Maven configuration inspection:
JDK 21 and Docker were unavailable, so the Java build and tests have not been run here.

## Structure

```text
src/main/java/com/example/employee/
  EmployeeApiApplication.java
  Employee.java                  JPA entity
  EmployeeRepository.java        persistence
  EmployeeService.java           transactional business operations
  EmployeeController.java        HTTP endpoints
  EmployeeRequest.java           validated input
  EmployeeResponse.java          output DTO
  EmployeePage.java              stable pagination response
  EmployeeNotFoundException.java
  ApiExceptionHandler.java       problem responses
src/main/resources/
  application.yml
  db/migration/V1__create_employees.sql
src/test/java/com/example/employee/
  EmployeeServiceTest.java
  EmployeeControllerTest.java
  EmployeeIntegrationTest.java
```

Add future database changes as new versioned Flyway migrations; do not edit a migration
already applied to a database.

Build-tool compatibility reference:
https://docs.spring.io/spring-boot/3.5/maven-plugin/
Flyway PostgreSQL module reference:
https://documentation.red-gate.com/fd/postgresql-database-277579325.html
