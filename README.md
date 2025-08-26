# algebra-solver

A Spring Boot REST API to **store, retrieve, and evaluate algebraic equations** using a postfix (expression) tree and in-memory storage.

---

## Features

- **Java 17+ / Spring Boot** backend (no UI)
- **RESTful JSON APIs** (test by Postman/curl)
- **Store algebraic equations:** parses and stores as postfix expression tree
- **Retrieve all equations:** infix reconstruction from tree, list all equations
- **Evaluate equation:** substitute variable values and get numerical results
- **Validation:** robust syntax check, missing variable, division by zero, bad input
- **JUnit 5 tests:** unit and integration coverage for edge cases
- **No database** (in-memory only, per assignment)
- **Easy to run locally**—no extra dependencies

---

## Tech Stack

- Java 17+
- Spring Boot 3.x
- Maven
- JUnit 5

---
## Project Structure

- `src/main/java/com/algebra/algebra_solver/` : Main app, API, parser, service
- `src/test/java/com/algebra/algebra_solver/` : JUnit 5 tests

---

## How to Run Locally

1. **Clone the repository**
    ```
    git clone https://github.com/bhargavv1704/algebra-solver.git
    cd algebra-solver
    ```

2. **Build the project**
    ```
    mvn clean install
    ```

3. **Run the Spring Boot application**
    ```
    mvn spring-boot:run
    ```
    > API will be live at [http://localhost:8080](http://localhost:8080)

---
## Testing

- All logic and APIs are covered by **JUnit 5** tests (unit and integration)
- To run all tests:
    ```
    mvn test
    ```
- Edge cases (invalid syntax, division by zero, unsolvable, missing variables, etc.) are fully tested.

---
## API Usage

### 1. Store an Algebraic Equation

**POST** `/api/equations/store`  
_example request:_
{
"equation": "3x + 2y - z"
}
_success response:_
{
"message": "Equation stored successfully",
"equationId": "1"
}

---

### 2. Retrieve Stored Equations

**GET** `/api/equations`  
_example response:_
{
"equations": [
{ "equationId": "1", "equation": "3 * x + 2 * y - z" },
{ "equationId": "2", "equation": "x ^ 2 + y ^ 2 - 4" }
]
}

---

### 3. Evaluate an Equation

**POST** `/api/equations/{equationId}/evaluate`  
_example request:_
{
"variables": {
"x": 2,
"y": 3,
"z": 1
}
}
_success response:_
{
"equationId": "1",
"equation": "3 * x + 2 * y - z",
"variables": { "x": 2, "y": 3, "z": 1 },
"result": 11
}

---

## Error Handling

- **400 Bad Request:** invalid equation, missing variable, syntax error
- **404 Not Found:** equation ID does not exist
- **422 Unprocessable Entity:** unsolvable equation (e.g. no real roots)
- All errors return JSON with `status`, `error`, and `message`  
_example:_
{
"status": 400,
"error": "Bad Request",
"message": "Invalid equation syntax"
}

---

## Sample curl Commands

- Store:
curl -X POST -H "Content-Type: application/json" -d '{"equation": "3x + 2y - z"}' http://localhost:8080/api/equations/store
- Evaluate:
curl -X POST -H "Content-Type: application/json" -d '{"variables": {"x": 2, "y": 3, "z": 1}}' http://localhost:8080/api/equations/1/evaluate
- List equations:
curl http://localhost:8080/api/equations

---

## Assumptions

- Only basic arithmetic and power: `+`, `-`, `*`, `/`, `^` (with parentheses)
- Variable names: single words (e.g. `x`, `y`, `foo`)
- Equation and evaluation are stateless and stored in memory
- No persistent storage, no UI

---

## Contact

For questions or clarifications, contact: manishbhargav1014@gmail.com

---
