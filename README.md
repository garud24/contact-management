# Spring Boot REST API Checklist

A step-by-step reference for building a REST API with Spring Boot, JPA, and PostgreSQL (Docker).
Built while creating this Contact Management project.

**The rule behind the order:** build from the database outward (entity → repository → service → controller), because each layer only depends on the ones beneath it. Harden what you built before adding features.

```
Client (Postman)
   → Controller   (HTTP in/out, URLs, status codes)
   → Service      (business rules, transactions)
   → Repository   (talks to DB)
   → PostgreSQL   (Docker)
```

---

## Phase 1: Setup

### 1. Generate the project
- [ ] Go to [start.spring.io](https://start.spring.io)
- [ ] Maven, Java, latest stable Spring Boot (no SNAPSHOT / M / RC)
- [ ] Group `com.yourname`, Artifact `project-name`
- [ ] **Fix the package name** (hyphens become underscores, e.g. `com.yourname.contacts`)
- [ ] Packaging: Jar, Java: current LTS
- [ ] Dependencies: **Spring Web**, **Spring Data JPA**, **PostgreSQL Driver**

> Every class must live under the main application's package. `@SpringBootApplication` only scans that package and its sub-packages.

### 2. Run the database
- [ ] Docker Desktop is running: `docker info`
- [ ] Container is up: `docker ps` (check `PORTS`, e.g. `0.0.0.0:5433->5432/tcp` means host 5433 → container 5432)
- [ ] User and DB exist:
  ```bash
  docker exec -it <container> psql -U <user> -d <db> -c '\conninfo'
  ```

### 3. Configure `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/contacts_db
spring.datasource.username=contacts_app
spring.datasource.password=dev_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```
- `ddl-auto=update`: Hibernate creates/alters tables on startup. Adds only, never drops or renames.
- `show-sql=true`: prints every SQL statement. Great for learning.
- `open-in-view=false`: keeps DB work inside the service layer.

### 4. Verify the connection
- [ ] Run: `./mvnw spring-boot:run`
- [ ] Log shows `HikariPool-1 - Start completed.`
- [ ] Log shows `Database dialect: PostgreSQLDialect`

---

## Phase 2: Data Layer

### 5. Entity (`entity/`)
- [ ] `@Entity` + `@Table(name = "plural_name")`
- [ ] `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` on a `Long id` (wrapper, so `null` = not saved yet)
- [ ] `@Column(nullable = false, unique = true, length = n)` for DB-level constraints
- [ ] `@PrePersist` / `@PreUpdate` callbacks for `createdAt` / `updatedAt` (`Instant`)
- [ ] `protected` no-arg constructor (required by JPA) + a public constructor
- [ ] No setters for `id` and timestamps
- [ ] Imports from `jakarta.persistence` (**not** `javax`)

**Verify:** log shows `create table ...`, then check from the DB:
```bash
docker exec -it <container> psql -U <user> -d <db> -c '\d <table>'
```

### 6. Repository (`repository/`)
```java
public interface ContactRepository extends JpaRepository<Contact, Long> {
    boolean existsByEmail(String email);   // derived query from the method name
}
```
- [ ] Interface only. Spring generates the implementation at startup.
- [ ] Free methods: `save`, `findById`, `findAll`, `existsById`, `deleteById`, `count`

**Verify:** log shows `Found 1 JPA repository interface.`

---

## Phase 3: Business and API Layers

### 7. DTOs (`dto/`)
- [ ] Use Java `record`s (immutable, no boilerplate)
- [ ] **Request DTO**: only fields the client may send (no `id`, no timestamps)
- [ ] **Response DTO**: what the client sees, plus a `static from(Entity)` mapper
- [ ] Never expose the entity directly

> Entities are classes (Hibernate needs a no-arg constructor and setters). DTOs are records.

### 8. Custom exceptions (`exception/`)
- [ ] One per failure type, e.g. `ContactNotFoundException`, `DuplicateEmailException`
- [ ] `extends RuntimeException`, message passed via `super(...)`
- [ ] No HTTP details here. Mapping to status codes happens in step 12.

### 9. Service (`service/`)
- [ ] `@Service`
- [ ] **Constructor injection** with `private final` fields (avoid `@Autowired` on fields)
- [ ] `@Transactional` on writes, `@Transactional(readOnly = true)` on reads
- [ ] All business rules live here (duplicate checks, not-found checks)
- [ ] Use `findById(id).orElseThrow(...)` for not-found handling
- [ ] On update, only check for duplicate email if the email actually changed
- [ ] **Dirty checking**: inside a transaction, modifying a loaded entity auto-runs `UPDATE`. No `save()` needed.

### 10. Controller (`controller/`)
- [ ] `@RestController` + `@RequestMapping("/api/<resources>")`
- [ ] Constructor injection of the service
- [ ] Keep each method thin: one service call, then return

| Action     | Method + URL                | Annotation       | Success status       |
|------------|-----------------------------|------------------|----------------------|
| Create     | `POST /api/contacts`        | `@PostMapping`   | **201** + `Location` |
| List all   | `GET /api/contacts`         | `@GetMapping`    | **200**              |
| Get one    | `GET /api/contacts/{id}`    | `@GetMapping`    | **200**              |
| Update     | `PUT /api/contacts/{id}`    | `@PutMapping`    | **200**              |
| Delete     | `DELETE /api/contacts/{id}` | `@DeleteMapping` | **204**              |

- `@RequestBody`: JSON body → DTO
- `@PathVariable`: `{id}` in URL → method parameter
- `ResponseEntity`: use only when you need a non-default status or headers

> URLs are nouns. The HTTP method carries the action. No `/createContact`.

---

## Phase 4: Hardening

### 11. Validation
- [ ] Add **Validation** starter (`spring-boot-starter-validation`)
- [ ] Annotations on the request DTO: `@NotBlank`, `@Email`, `@Size`
- [ ] `@Valid` on the controller's `@RequestBody`

### 12. Global exception handling
- [ ] `@RestControllerAdvice` class
- [ ] Map exceptions to status codes: validation → **400**, not found → **404**, duplicate → **409**
- [ ] Return a consistent error body (timestamp, status, message, path)

### 13. Test every endpoint
- [ ] Happy paths **and** failure cases (missing id, duplicate, invalid input)
- [ ] Save as a Postman collection and rerun after every change

**Postman tips:** for POST/PUT use Body → raw → **JSON** (sets `Content-Type` automatically; missing it causes **415**). Without step 12, unhandled exceptions return **500**.

---

## Phase 5: Production Readiness

- [ ] **14. Pagination and sorting** on list endpoints (`Pageable`)
- [ ] **15. Schema migrations** with Flyway instead of `ddl-auto=update`
- [ ] **16. Docker Compose** for DB + app (reproducible setup)
- [ ] **17. Secrets out of properties files** (env variables, profiles)
- [ ] **18. Automated tests**: unit tests for services, integration tests with Testcontainers
- [ ] **19. API docs** with OpenAPI / Swagger
- [ ] **20. Advanced**: Spring Security + JWT, Redis caching, Actuator + logging

---

## Common Pitfalls

| Symptom | Cause | Fix |
|---|---|---|
| DB properties underlined, no Hikari logs | JPA / driver dependency missing | Add `spring-boot-starter-data-jpa` + `postgresql` |
| Entity has no table | Class outside main package tree | Move under the base package |
| `javax.persistence` won't compile | Old tutorial | Use `jakarta.persistence` |
| Column change not applied | `ddl-auto=update` never alters existing columns | Drop table in dev, use Flyway in real projects |
| `WARN ... SQLState: 00000 ... does not exist, skipping` | Hibernate drops constraint before adding it | Harmless. `00000` = success |
| App won't start: `No property 'x' found` | Typo in derived query method name | Fix the method name |
| Port 8080 in use | Previous run still active | `Ctrl + C` the old process |
| 415 in Postman | Body not sent as JSON | Body → raw → JSON |
| 500 for not-found / duplicate | No global exception handler | Step 12 |

## Useful Commands

```bash
./mvnw spring-boot:run      # run the app
./mvnw compile              # compile only
docker ps                   # running containers
docker ps -a                # include stopped containers
lsof -i :5433               # what is using a port

# inside the Postgres container
docker exec -it <container> psql -U <user> -d <db> -c '\dt'            # list tables
docker exec -it <container> psql -U <user> -d <db> -c '\d <table>'     # describe table
docker exec -it <container> psql -U <user> -d <db> -c 'SELECT * FROM <table>;'
```
