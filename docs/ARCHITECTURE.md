# Project Package Structure

This document describes the initial backend package structure for the **Co-App** Spring Boot project. 
Co-App backend follows a **layered architecture**.

---

## High-Level Structure

```
com.coapp.backend
├── CoAppApplication.java
│
├── config
├── controller
├── service
├── repository
├── model
│   ├── document
│   └── enumeration
├── dto
└── exception
```

---

## Package Breakdown

### 1. `config`

**Purpose**: Application-wide configuration.

This package contains Spring configuration classes that define cross-cutting concerns such as security, CORS, and web configuration.

---

### 2. `controller`

**Purpose**: REST API layer.

Controllers expose HTTP endpoints and act as the entry point for client requests.

**Responsibilities**:

* Handle HTTP methods (GET, POST, PUT, DELETE)
* Validate request input
* Convert request/response bodies to/from DTOs
* Delegate business logic to the service layer

Controllers **should not** contain business logic.

---

### 3. `service`

**Purpose**: Business logic layer.

This layer contains the core application logic and orchestration between repositories and other services.

**Responsibilities**:

* Implement business rules
* Coordinate multiple repositories

---

### 4. `repository`

**Purpose**: Data access layer.

Repositories abstract database interactions and are responsible for persisting and querying data from MongoDB.

**Responsibilities**:

* CRUD operations
* Query definitions

---

### 5. `model`

**Purpose**: Domain model definitions.

This package represents the core business entities of the application.

#### 5.1 `model.document`

**Purpose**: MongoDB document models.

These classes:

* Represent how data is stored in MongoDB
* Are annotated with `@Document`
* May contain fields that should **not** be exposed to clients (e.g., passwords)

#### 5.2 `model.enumeration`

**Purpose**: Enumerations for fixed sets of allowed values.

Enums help ensure data consistency and readability across the application.

**Example**:

```java
public enum ApplicationStatus {
    INTEND_TO_APPLY,
    APPLIED,
    SCHEDULED_FOR_INTERVIEW,
    OFFER_PENDING,
    ACCEPTED,
    REJECTED
}

...

Application newApplication = new Application();
newApplication.setStatus(ApplicationStatus.INTEND_TO_APPLY);
```

### 6. `dto`

**Purpose**: Data Transfer Objects.

DTOs are used to transfer data between layers and to/from the client.

**Key Ideas**:

* DTOs are **safe** to expose to the frontend
* They may omit sensitive fields (e.g., password, internal IDs)
* They may be tailored specifically to frontend needs

#### Data Flow Example

```
Client <---> Controller <---> Service <---> Repository <---> MongoDB
-------(DTO)------->(Domain Object)-------------------------------->
```

#### Layer Responsibilities

| Layer           | Works With          | Responsibility                                     |
| --------------- |---------------------| -------------------------------------------------- |
| Controller      | DTOs                | Handles HTTP, validation, request/response mapping |
| Service (Logic) | Domain Objects/DTOs | Business rules, orchestration                      |
| Repository      | Domain Objects      | Database persistence (MongoDB)                     |

---

### 7. `exception`

**Purpose**: Centralized error handling.

This package contains custom exceptions and global exception handling logic.
