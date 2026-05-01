# Low-Level Design (LLD) - Job Portal Microservices Platform

## 1. Document Information
- **Project Name:** Job Portal Microservices
- **Version:** 1.0.0
- **Status:** Final
- **Date:** 2026-04-27
- **Author:** Antigravity AI

---

## 2. Overview
This Low-Level Design (LLD) document provides a detailed technical specification for the Job Portal Microservices Platform. It covers the internal class structures, data models, method signatures, and logic for each microservice. This document is intended for developers and architects who need to understand the granular details of the implementation.

---

## 3. User Service (LLD)

### 3.1. Data Model (Entities)
#### `User`
- `id` (Long, PK, Auto-increment)
- `name` (String, Not Null)
- `email` (String, Not Null, Unique)
- `password` (String, Not Null, BCrypt Encrypted)
- `role` (Enum: `ADMIN`, `RECRUITER`, `JOB_SEEKER`)
- `active` (boolean, Default: true)
- `mobile` (String)
- `skills` (String, Comma-separated)
- `headline` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### 3.2. Controllers
#### `UserController`
- `POST /api/users/register`: Registers a new user.
    - **Params:** `UserRequest` (name, email, password, role)
    - **Returns:** `UserResponse`
- `POST /api/users/login`: Authenticates user and returns JWT.
    - **Params:** `LoginRequest` (email, password)
    - **Returns:** `LoginResponse` (jwt, userDetails)
- `GET /api/users/profile/{id}`: Fetches user profile.
    - **Returns:** `UserResponse`
- `PUT /api/users/{id}`: Updates user profile.
    - **Params:** `UserUpdateRequest` (name, mobile, skills, headline)

### 3.3. Service Logic
#### `UserServiceImpl`
- `register(UserRequest)`: Validates if email already exists, hashes password using `BCryptPasswordEncoder`, sets default role, and saves to repository.
- `login(LoginRequest)`: Authenticates via `AuthenticationManager`, generates JWT using `JwtUtil`, and retrieves user details.
- `getUserById(Long)`: Fetches user or throws `ResourceNotFoundException`.

---

## 4. Job Service (LLD)

### 4.1. Data Model (Entities)
#### `Job`
- `jobId` (Long, PK, Auto-increment)
- `title` (String, Not Null)
- `company` (String, Not Null)
- `location` (String, Not Null)
- `salary` (Double)
- `jobType` (String: Full-time, Contract, etc.)
- `experienceLevel` (String: Junior, Mid, Senior)
- `description` (String, Length: 2000)
- `status` (String, Default: "OPEN")
- `createdBy` (String, Recruiter Email)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### 4.2. Controllers
#### `JobController`
- `POST /api/jobs`: Creates a new job listing.
    - **Params:** `JobRequest` (title, company, location, salary, description, jobType, experienceLevel)
    - **Headers:** `X-User-Email` (from Gateway)
- `GET /api/jobs/{id}`: Fetches job details.
- `PUT /api/jobs/{id}`: Updates job listing.
- `PUT /api/jobs/{id}/close`: Marks job as CLOSED.

### 4.3. Service Logic
#### `JobServiceImpl`
- `createJob(JobRequest, email)`: Saves job to DB, then publishes `JobEvent` to RabbitMQ (`job.exchange`, routing key `job.created`).
- `getJobById(Long)`: Fetches from DB, with optional Redis caching for performance.
- `updateJob(Long, JobRequest)`: Updates job and publishes `JobEvent` to sync with search-service.

---

## 5. Application Service (LLD)

### 5.1. Data Model (Entities)
#### `Application`
- `applicationId` (UUID, PK)
- `jobId` (Long, FK-like)
- `userEmail` (String)
- `status` (Enum: `APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `REJECTED`)
- `appliedAt` (LocalDateTime)

### 5.2. Controllers
#### `ApplicationController`
- `POST /api/applications`: Submit a new application.
    - **Params:** `ApplicationRequest` (jobId, resumeUrl)
- `GET /api/applications/me`: Get current user's applications.
- `GET /api/applications/job/{jobId}`: Get applicants for a specific job (Recruiter only).
- `PUT /api/applications/{id}/status`: Update application status.

### 5.3. Service Logic
#### `ApplicationServiceImpl`
- `apply(ApplicationRequesbt, email)`:
    - Checks if user has already applied (`existsByJobIdAndUserEmail`).
    - Validates if user role is `JOB_SEEKER`.
    - Saves application.
    - Publishes `ApplicationEvent` to RabbitMQ (`jobportal.exchange`, routing key `job.applied`).
- `updateStatus(UUID, status, role)`: Validates role, checks current status, updates DB, and triggers notification event.

---

## 6. Search Service (LLD)

### 6.1. Messaging Consumer
#### `JobConsumer`
- `@RabbitListener(queues = "job.created.queue")`: 
    - Consumes `JobEvent`.
    - Maps event to `Job` entity (search-specific table).
    - Persists to search database.
- `@RabbitListener(queues = "job.closed.queue")`:
    - Marks job as CLOSED in search index.

### 6.2. Search Logic
- **Filtering:** Implements Dynamic JPA Specifications for filtering by multiple criteria (salary range, location, job type).

---

## 7. AI Service (LLD)

### 7.1. Gemini Integration
#### `GeminiService`
- `analyzeResume(fileUrl)`:
    - Downloads PDF from `resume-service`.
    - Uses `PDFTextStripper` to extract raw text.
    - Constructs prompt for Gemini Pro.
    - Parses JSON response into `ScoreResponse` DTO.
- `chat(message, context, role, email)`:
    - Takes user query and system context.
    - Generates conversational response using Gemini.

### 7.2. Controllers
- `POST /api/ai/analyze-resume`: Trigger AI analysis.
- `POST /api/ai/chat`: Interactive job portal assistant.

---

## 8. API Gateway (LLD)

### 8.1. JWT Logic
#### `JwtUtil`
- `validateToken(token)`: Validates signature and expiration.
- `extractEmail(token)`: Extracts `sub` claim.
- `extractRole(token)`: Extracts `role` claim.

### 8.2. Global Filters
#### `JwtAuthFilter`
- **Logic:**
    1. Check if path is public (Login/Register/Swagger).
    2. Extract Bearer token from `Authorization` header.
    3. Validate token.
    4. Extract `email` and `role`.
    5. Perform RBAC (Role-Based Access Control) checks (e.g., only RECRUITER can POST to `/api/jobs`).
    6. Add `X-User-Email` and `X-User-Role` headers to the request before routing.

---

## 9. Messaging Protocol (Low Level)

### 9.1. RabbitMQ Configuration
- **Exchange:** `jobportal.exchange` (Topic)
- **Queues:**
    - `search.job.create.queue`: Bound with `job.created`
    - `notification.email.queue`: Bound with `job.applied` and `application.status.changed`
- **Serialization:** `Jackson2JsonMessageConverter` for JSON-to-DTO mapping.

---

## 10. Exception Handling Strategy

### 10.1. Global Exception Handler
- `@RestControllerAdvice`
- `@ExceptionHandler(ResourceNotFoundException.class)` -> returns 404.
- `@ExceptionHandler(UnauthorizedException.class)` -> returns 401.
- `@ExceptionHandler(AlreadyAppliedException.class)` -> returns 400.

---

## 11. Database Schema Details (Logical)

### 11.1. Constraints and Indexes
- **Users Table:**
    - Unique Index on `email`.
    - Index on `role`.
- **Jobs Table:**
    - Index on `title` (Full-text search enabled in search-service).
    - Index on `createdBy`.
- **Applications Table:**
    - Composite Unique Index on (`jobId`, `userEmail`).

---

## 12. Component Interactions (Sequence Diagrams)

### 12.1. Job Posting Flow
1. `Client` -> `Gateway`: POST `/api/jobs`
2. `Gateway` -> `Gateway`: Validate JWT
3. `Gateway` -> `JobService`: Proxy request with `X-User-Email`
4. `JobService` -> `Database`: Save Job
5. `JobService` -> `RabbitMQ`: Publish `JobCreatedEvent`
6. `SearchService` -> `RabbitMQ`: Consume `JobCreatedEvent`
7. `SearchService` -> `Database`: Index Job
8. `NotificationService` -> `RabbitMQ`: Consume `JobCreatedEvent`
9. `NotificationService` -> `SMTP`: Send Alerts

---

## 13. Infrastructure Details

### 13.1. Docker Compose Services
- `postgres`: Port 5432, Volumes for data persistence.
- `rabbitmq`: Port 5672 (AMQP), 15672 (Management).
- `redis`: Port 6379.
- `eureka-server`: Port 8761.
- `config-server`: Port 8888.

---

## 14. Performance & Caching (LLD)

### 14.1. Redis Implementation
- **Cache Names:** `jobs`, `userProfiles`.
- **TTL:** 10 minutes.
- **Eviction:** LRU (Least Recently Used).

---

## 15. Testing Details (LLD)

### 15.1. Unit Test Patterns
- Use `@Mock` for services and `@InjectMocks` for controllers.
- Use `MockMvc` for endpoint testing.

### 15.2. Integration Test Patterns
- Use `@SpringBootTest` with `@ActiveProfiles("test")`.
- Use `@Testcontainers` to spin up a real PostgreSQL container.

---

## 16. Detailed API Specification (JSON Examples)

### 16.1. Job Request
```json
{
  "title": "Senior Java Developer",
  "company": "TechCorp",
  "location": "Remote",
  "salary": 120000.0,
  "description": "Full stack java developer with experience in microservices...",
  "jobType": "Full-time",
  "experienceLevel": "Senior"
}
```

### 16.2. AI Analysis Response
```json
{
  "score": 85,
  "summary": "Strong candidate with relevant microservices experience.",
  "strengths": ["Java 17", "Spring Boot", "AWS"],
  "improvements": ["Needs more experience with Kubernetes"],
  "missingKeywords": ["Docker", "Terraform"],
  "recommendation": "Shortlist for technical interview."
}
```

---

## 17. Security Specifications

### 17.1. Password Policy
- Min 8 characters.
- Must contain at least one uppercase, one lowercase, one digit, and one special character.
- Hashed using BCrypt (Strength: 10).

### 17.2. JWT Payload
- `sub`: User Email
- `role`: User Role
- `iat`: Issued At
- `exp`: Expiration (3600 seconds)

---

## 18. Logging Context (MDC)
- Each request in the Gateway initiates a `correlationId` in the MDC (Mapped Diagnostic Context).
- Downstream services propagate this ID to ensure end-to-end log visibility.

---

## 19. Failure Mitigation (LLD)

### 19.1. Circuit Breaker for Feign
- `@FeignClient(name = "job-service", fallback = JobServiceFallback.class)`
- `JobServiceFallback` implements the client interface with "Service unavailable" messages.

---

## 20. Conclusion
This LLD provides the necessary level of detail to implement, test, and maintain the Job Portal Microservices Platform. Any changes to the core logic or data structures should be updated in this document to maintain technical consistency across the team.

---
*End of Document - LLD 1.0.0*

*(Expanding with more detailed class-level documentation...)*

---

## 21. Detailed Class Definitions (Service Layer)

### 21.1. User Service
- **`AuthService`**: Orchestrates login logic.
    - `login(String email, String password)`: Returns `TokenDTO`.
- **`ProfileService`**: Manages complex profile updates.
    - `updateSkills(Long userId, List<String> skills)`: Atomically updates skills list.

### 21.2. Job Service
- **`JobService`**:
    - `getAllJobs(Pageable)`: Implements paginated retrieval.
    - `findJobsByRecruiter(String email, Pageable)`: Filters by `createdBy`.

### 21.3. Notification Service
- **`EmailService`**:
    - `sendJobAlert(String to, String jobTitle)`: Templates email using Thymeleaf.
    - `sendApplicationUpdate(String to, String status)`: Notifies candidate of status change.

---

## 22. Detailed Repository Methods (Spring Data JPA)

### 22.1. UserRepository
- `Optional<User> findByEmail(String email);`
- `boolean existsByEmail(String email);`

### 22.2. JobRepository
- `Page<Job> findByStatus(String status, Pageable pageable);`
- `Page<Job> findByCreatedBy(String email, Pageable pageable);`

### 22.3. ApplicationRepository
- `boolean existsByJobIdAndUserEmail(Long jobId, String userEmail);`
- `Page<Application> findByUserEmail(String userEmail, Pageable pageable);`
- `Page<Application> findByJobId(Long jobId, Pageable pageable);`

---

## 23. Internal Data Transfer Objects (DTOs)

### 23.1. Common Response DTO
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

### 23.2. Error DTO
```java
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
}
```

---

## 24. Messaging Event Structures

### 24.1. JobEvent
```java
public class JobEvent {
    private Long jobId;
    private String title;
    private String company;
    private String action; // CREATED, UPDATED, CLOSED
}
```

### 24.2. ApplicationEvent
```java
public class ApplicationEvent {
    private String applicationId;
    private Long jobId;
    private String userEmail;
    private String status;
}
```

---

## 25. Configuration Details (YAML Snippets)

### 25.1. Gateway Route Configuration
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/users/**
```

### 25.2. RabbitMQ Configuration
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 2000ms
          max-attempts: 3
```

---

## 26. Resilience4j Settings (Detailed)

### 26.1. Circuit Breaker Properties
```yaml
resilience4j:
  circuitbreaker:
    instances:
      jobService:
        registerHealthIndicator: true
        slidingWindowSize: 100
        minimumNumberOfCalls: 10
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
```

---

## 27. UI Integration Details (Angular)

### 27.1. API Service Template
- `BaseUrl`: `http://localhost:8080` (Gateway)
- `Headers`: `Authorization: Bearer <token>`
- `Interceptors`: `AuthInterceptor` (attaches token to every request).

### 27.2. State Management (RxJS)
- `BehaviorSubject` for `CurrentUser` to track authentication state globally.

---

## 28. Future Proofing & Maintenance

### 28.1. API Versioning
- Strategy: URI Versioning (e.g., `/api/v1/users/`).
- Currently implied as `v1` by default.

### 28.2. Log Rotation
- Configured via `logback-spring.xml`.
- Max File Size: 10MB.
- Max History: 30 days.

---
*End of Detailed LLD*

## 29. Database Migration Strategy

### 29.1. Liquibase Implementation (Planned)
- **Standardization:** All microservices will eventually use Liquibase for version-controlled database schema changes.
- **Changelog Structure:**
    - `db.changelog-master.xml`: Root file.
    - `v1.0.0/`: Folder containing initial tables.
- **Benefits:** Ensures that all environments (Dev, QA, Prod) have identical schema structures.

### 29.2. Initial Data Seeding
- SQL script `init-db.sql` is used in Docker Compose for the first-time setup of Postgres.
- Services use `spring.jpa.hibernate.ddl-auto: update` during development to automatically adjust schemas.

---

## 30. Environment Setup Guide (Low Level)

### 30.1. Prerequisites
- **JDK:** OpenJDK 17.
- **Docker:** Desktop 4.x+.
- **Node.js:** v18+ (for Angular).
- **IDE:** IntelliJ IDEA (Recommended for Spring) and VS Code (Recommended for Angular).

### 30.2. Local Execution Order
1. Start `postgres`, `rabbitmq`, and `redis` via Docker.
2. Start `eureka-server` (Wait for port 8761).
3. Start `config-server` (Wait for port 8888).
4. Start core microservices (`user-service`, `job-service`, etc.).
5. Start `api-gateway`.
6. Run `npm start` in the `frontend` directory.

---

## 31. Detailed API Specification (Expanded)

### 31.1. User Registration JSON
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "Password@123",
  "role": "JOB_SEEKER",
  "mobile": "1234567890",
  "skills": "Java, Spring, SQL"
}
```

### 31.2. Application Status Update JSON
```json
{
  "status": "SHORTLISTED",
  "remarks": "Strong technical background in microservices."
}
```

---

## 32. UI Component Mapping (Low Level)

### 32.1. Shared Components
- **`AuthInterceptor`**: 
    - `intercept(req: HttpRequest<any>, next: HttpHandler)`: Adds Authorization header if token is present in `localStorage`.
- **`ErrorService`**:
    - `handleError(error: HttpErrorResponse)`: Maps HTTP status codes to user-friendly messages.

### 32.2. Feature Components (Job Seeker)
- **`JobListComponent`**: Uses `JobService.searchJobs()` with RxJS `debounceTime` for real-time search.
- **`ApplicationModalComponent`**: Handles file upload for resumes before calling the apply API.

---

## 33. Troubleshooting (Developer Level)

### 33.1. Common Errors and Fixes
- **401 Unauthorized:**
    - Check if JWT has expired.
    - Verify `JWT_SECRET` matches across Gateway and User Service.
- **503 Service Unavailable:**
    - Check Eureka dashboard to see if the service is registered.
    - Verify the service name in `application.yml` matches the Gateway route ID.
- **Connection Refused (DB):**
    - Ensure Postgres container is healthy.
    - Check if `SPRING_DATASOURCE_URL` is using the correct hostname (e.g., `postgres` inside Docker vs `localhost` outside).

---

## 34. Monitoring Setup (Low Level)

### 34.1. Micrometer & Prometheus
- Services include `micrometer-registry-prometheus` dependency.
- Scrape endpoint: `/actuator/prometheus`.
- Metrics: JVM memory, Request latency, Database connection pool status.

---

## 35. Conclusion
This document serves as the final technical blueprint for the Job Portal Microservices Platform. It bridges the gap between high-level architectural concepts and actual implementation details, ensuring that every developer on the team has a clear understanding of the "how" behind the "what".

---
*Document Finalized on 2026-04-27 - LLD Expanded Version*

## 36. Code Review Checklist for Developers

### 36.1. General
- [ ] Does the code follow the project's Java/Angular style guide?
- [ ] Are there any hardcoded values that should be in `application.yml`?
- [ ] Is the code properly documented with Javadocs/Comments?

### 36.2. Security
- [ ] Is input validation implemented for all controller endpoints?
- [ ] Are sensitive fields (like passwords) never logged or returned in APIs?
- [ ] Does the endpoint check for proper user roles via the Gateway?

### 36.3. Performance
- [ ] Are database queries optimized with proper indexing?
- [ ] Is pagination implemented for all list-returning endpoints?
- [ ] Are Redis cache keys structured properly (`prefix:id`)?

### 36.4. Reliability
- [ ] Are external service calls wrapped in Resilience4j circuit breakers?
- [ ] Is exception handling covering both success and failure paths?
- [ ] Are RabbitMQ messages idempotent?

---

## 37. API Versioning Strategy Details

### 37.1. URI-Based Versioning
- All new breaking changes must increment the version prefix (e.g., `/api/v2/...`).
- Non-breaking changes (new fields) should be added to existing versions.

### 37.2. Deprecation Policy
- Old API versions will be supported for 6 months after the release of a new version.
- Use the `@Deprecated` annotation in Java and add warning headers in the API responses.

---

## 38. External Integrations (Future)

### 38.1. Payment Gateway (Recruiter Credits)
- Integration with Stripe or Razorpay for job posting credits.
- Webhook handling for asynchronous payment confirmation.

### 38.2. LinkedIn/Indeed Job Sync
- Automated posting of jobs to external boards via their respective APIs.
- OAuth2 integration for user-authorized syncing.

---
*End of Complete LLD Document*


---
*(Lines: ~620)*
