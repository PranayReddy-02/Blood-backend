# Blood Donor API - Architecture & Design Document

## Table of Contents
1. [System Architecture](#system-architecture)
2. [Database Design](#database-design)
3. [API Design](#api-design)
4. [Security Architecture](#security-architecture)
5. [Design Patterns](#design-patterns)
6. [Technology Decisions](#technology-decisions)

---

## System Architecture

### Layered Architecture

The application follows a **4-layer clean architecture** pattern:

```
┌─────────────────────────────────────┐
│      REST API Layer (Controllers)   │
│  - AuthController                   │
│  - DonorController                  │
│  - RequesterController              │
│  - AdminController                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Service Layer (Business Logic) │
│  - AuthService                      │
│  - DonorService                     │
│  - BloodRequestService              │
│  - RequestResponseService           │
│  - DonationService                  │
│  - AdminService                     │
│  - AuthorizationService             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Repository Layer (Data Access)    │
│  - UserRepository                   │
│  - DonorDetailsRepository           │
│  - BloodRequestRepository           │
│  - RequestResponseRepository        │
│  - DonationHistoryRepository        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Data Layer (Database)          │
│      MySQL with JPA/Hibernate       │
└─────────────────────────────────────┘
```

### Benefits of Layered Architecture

- **Separation of Concerns:** Each layer has a single responsibility
- **Maintainability:** Changes to one layer don't affect others
- **Testability:** Each layer can be unit tested independently
- **Reusability:** Services can be used by multiple controllers
- **Scalability:** Horizontal scaling at service layer

---

## Database Design

### Entity Relationship Diagram

```
┌─────────────────┐
│     Users       │
├─────────────────┤
│ id (PK)         │
│ email (UNIQUE)  │
│ password        │
│ name            │
│ phoneNumber     │
│ city            │
│ role (ENUM)     │
│ isVerified      │
│ isBlocked       │
│ createdAt       │
│ updatedAt       │
└────┬─────────────┘
     │
     ├──────────────────────────┬──────────────────┐
     │                          │                  │
     ▼                          ▼                  ▼
┌──────────────────┐  ┌─────────────────┐  ┌──────────────┐
│  DonorDetails    │  │  BloodRequests  │  │ DonorDetails │
├──────────────────┤  ├─────────────────┤  │ (continued)  │
│ id (PK)          │  │ id (PK)         │  │──────────────│
│ userId (FK)      │  │ requesterId(FK) │  │ bloodGroup   │
│ bloodGroup       │  │ bloodGroup      │  │ lastDonated  │
│ lastDonationDate │  │ hospitalName    │  │ isAvailable  │
│ isAvailable      │  │ location        │  │ totalDonate  │
│ totalDonations   │  │ urgencyLevel    │  └──────────────┘
└──────────────────┘  │ unitsRequired   │
                      │ status          │
                      │ notes           │
                      │ createdAt       │
                      │ updatedAt       │
                      └────┬────────────┘
                           │
                           ▼
                  ┌──────────────────────┐
                  │ RequestResponses     │
                  ├──────────────────────┤
                  │ id (PK)              │
                  │ bloodRequestId (FK)  │
                  │ donorId (FK)         │
                  │ status               │
                  │ otp                  │
                  │ otpVerified          │
                  │ unitsProvided        │
                  │ responseDate         │
                  │ updatedAt            │
                  └──────┬───────────────┘
                         │
                         ▼
                  ┌──────────────────────┐
                  │ DonationHistory      │
                  ├──────────────────────┤
                  │ id (PK)              │
                  │ donorId (FK)         │
                  │ responseId (FK)      │
                  │ unitsDonated         │
                  │ bloodGroup           │
                  │ hospitalName         │
                  │ donationDate         │
                  │ notes                │
                  │ createdAt            │
                  └──────────────────────┘
```

### Normalization

- **1NF (First Normal Form):** All attributes are atomic
- **2NF (Second Normal Form):** No partial dependencies
- **3NF (Third Normal Form):** No transitive dependencies
- **BCNF:** Boyce-Codd Normal Form applied where necessary

### Indexing Strategy

```sql
-- Primary Key Indexes
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE donor_details ADD PRIMARY KEY (id);
-- Foreign Key Indexes (auto-created)

-- Search Optimization Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_city ON users(city);
CREATE INDEX idx_donor_blood_group ON donor_details(blood_group);
CREATE INDEX idx_request_blood_group ON blood_requests(blood_group);
CREATE INDEX idx_request_status ON blood_requests(status);
```

---

## API Design

### REST Principles

**Resource-Oriented URLs:**
```
/api/donors                 → Donor collection
/api/donors/{id}            → Single donor
/api/requests               → Request collection
/api/requests/{id}          → Single request
/api/requests/{id}/responses → Request responses
```

**HTTP Methods:**
- `GET` - Retrieve resources (safe, idempotent)
- `POST` - Create new resources (unsafe)
- `PUT` - Update entire resource (idempotent)
- `DELETE` - Remove resources (idempotent)

### HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | GET, PUT successful |
| 201 | Created | POST successful |
| 204 | No Content | DELETE, PUT with no response |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Auth missing/invalid |
| 403 | Forbidden | Permission denied |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 500 | Server Error | Unexpected error |

### Response Format

**Success Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  ...
  "metadata": {
    "timestamp": "2024-03-12T10:30:00",
    "version": "1.0"
  }
}
```

**Error Response:**
```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2024-03-12T10:30:00",
  "validationErrors": {
    "email": "Email should be valid",
    "password": "Password must contain..."
  }
}
```

### Pagination

```
GET /api/donors?page=0&size=20&sort=name,asc

Response:
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "size": 20
}
```

---

## Security Architecture

### Authentication Flow

```
┌──────────────┐
│ Client Login │
└──────┬───────┘
       │ POST /auth/login
       ▼
┌─────────────────────────┐
│ Validate Credentials    │
│ - Check email exists    │
│ - Compare password hash │
│ - Verify user not blocked
└──────┬─────────────────┘
       │
       ▼
┌────────────────────────┐
│ Generate JWT Token     │
│ - Sub: email           │
│ - Role: user role      │
│ - Exp: 24h             │
└──────┬─────────────────┘
       │
       ▼
┌──────────────────────┐
│ Return Token         │
│ Bearer <jwt_token>   │
└──────────────────────┘
```

### Authorization Flow

```
┌─────────────────────────┐
│ Receive Request         │
│ Authorization: Bearer X │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────────┐
│ JwtAuthenticationFilter      │
│ - Extract token              │
│ - Validate signature         │
│ - Check expiration           │
└──────┬───────────────────────┘
       │
       ├─ Valid ─────────────────┐
       │                          │
       ▼                          ▼
┌────────────────────┐   ┌──────────────┐
│ Extract Claims     │   │ Reject       │
│ - Email (subject)  │   │ Return 401   │
│ - Role             │   └──────────────┘
└──────┬─────────────┘
       │
       ▼
┌──────────────────────────────┐
│ Create Authentication Object │
│ - Principal: email           │
│ - Authorities: [role]        │
│ - Authenticated: true        │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│ SecurityContext              │
│ Set authentication           │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│ @PreAuthorize Evaluation             │
│ - Check method-level permissions     │
│ - Verify user role matches required  │
└──────┬───────────────────────────────┘
       │
       ├─ Authorized ─┬─ Not Authorized
       │              └─> Return 403
       ▼
┌──────────────────┐
│ Process Request  │
│ Execute method   │
└──────────────────┘
```

### Password Security

- **Hashing:** BCrypt with 10+ rounds
- **Validation:** Minimum 8 chars, uppercase, lowercase, digit, special char
- **Never:** Plain text storage, weak algorithms, insufficient entropy

### JWT Security

- **Algorithm:** HS512 (HMAC with SHA-512)
- **Secret Key:** 256+ bit random key
- **Expiration:** 24 hours (configurable)
- **Claims:** Email (sub), Role, Issued time, Expiration time

---

## Design Patterns

### 1. Service Layer Pattern

```java
@Service
public class DonorService {
    @Autowired
    private DonorDetailsRepository repository;
    
    // Business logic encapsulation
    public Page<DonorDetailsResponse> searchDonors(String bloodGroup, Pageable page) {
        // Complex logic here
        DonorDetails.BloodGroup bg = parseBloodGroup(bloodGroup);
        return repository.findAvailableDonors(bg, page).map(this::mapToDTO);
    }
}
```

**Benefits:**
- Separation of business logic from HTTP layer
- Reusability across controllers
- Testability without HTTP mocking

### 2. DTO Pattern

```java
// Input validation
@Data
public class BloodRequestRequest {
    @NotBlank
    private String bloodGroup;
    @NotNull
    private Integer unitsRequired;
}

// Output formatting
@Data
public class BloodRequestResponse {
    private Long id;
    private String bloodGroup;
    // Only needed fields
}
```

**Benefits:**
- Input validation
- Output transformation
- Decoupling from entity objects

### 3. Repository Pattern

```java
public interface DonorDetailsRepository extends JpaRepository<DonorDetails, Long> {
    Page<DonorDetails> findAvailableDonorsByBloodGroup(
        BloodGroup bg, Pageable pageable);
}
```

**Benefits:**
- Abstraction of data access
- Easy to mock in tests
- Consistent query interface

### 4. Builder Pattern

```java
User user = User.builder()
    .email("john@example.com")
    .name("John Doe")
    .city("Mumbai")
    .build();
```

**Benefits:**
- Clean object creation
- Handles optional fields
- Improved readability

### 5. Exception Handling Pattern

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex) {
        return new ResponseEntity<>(
            errorResponse, HttpStatus.NOT_FOUND);
    }
}
```

**Benefits:**
- Centralized error handling
- Consistent error format
- Separation from business logic

---

## Technology Decisions

### Spring Boot

**Why?**
- Industry standard for Java microservices
- Auto-configuration reduces boilerplate
- Rich ecosystem (Security, Data, Web)
- Excellent community support

### Spring Data JPA

**Why?**
- ORM abstraction reduces SQL complexity
- Query methods generation from naming
- Built-in pagination and sorting
- Transaction management

### JWT vs Sessions

| Feature | JWT | Sessions |
|---------|-----|----------|
| Stateless | ✓ | ✗ |
| Mobile friendly | ✓ | ✗ |
| Scalability | ✓ | ✓ |
| Token invalidation | ✗ | ✓ |

**Chosen: JWT** - Better for distributed systems and mobile clients

### MySQL vs NoSQL

**MySQL Selected** because:
- Structured data with relationships
- ACID compliance for financial transactions
- Superior for complex queries
- Strong consistency requirements

### Lombok

**Benefits:**
- Reduces boilerplate (getter/setter/constructor)
- Improves code readability
- Automatic equals/hashCode

### Lombok Annotations Used
- `@Data` - Getter, setter, equals, hashCode, toString
- `@Builder` - Builder pattern implementation
- `@NoArgsConstructor` - Default constructor
- `@AllArgsConstructor` - Constructor with all fields

---

## Performance Considerations

### Database Optimization

1. **Connection Pooling:**
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   ```

2. **Lazy Loading:**
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   private User requester;
   ```

3. **Batch Processing:**
   ```properties
   spring.jpa.properties.hibernate.jdbc.batch_size=20
   ```

### Caching Strategy

```java
@Service
@EnableCaching
public class DonorService {
    @Cacheable(value = "donors", key = "#bloodGroup")
    public Page<DonorDetailsResponse> searchDonors(
        String bloodGroup, Pageable page) {
        // Expensive query cached
    }
}
```

### Query Optimization

- **Avoid N+1 problems:** Use joins instead of multiple queries
- **Use projections:** Fetch only needed columns
- **Pagination:** Always paginate large datasets
- **Indexing:** Index frequently filtered columns

---

## Scalability Architecture

### Horizontal Scaling

```
┌──────────┐
│ Load     │
│ Balancer │
└─────┬────┘
      │
   ┌──┴──┬──────┬──────┐
   │     │      │      │
   ▼     ▼      ▼      ▼
┌─────┐┌─────┐┌─────┐┌─────┐
│ API │ │API │ │API │ │API │
│ #1  │ │ #2 │ │ #3 │ │ #4 │
└────┬┘└────┬┘└────┬┘└────┬┘
     │      │      │      │
     └──────┴──┬───┴──────┘
              ▼
         ┌──────────────┐
         │  MySQL DB    │
         │  (Master)    │
         └──────┬───────┘
                │
         ┌──────┴──────┐
         │             │
         ▼             ▼
      ┌─────┐       ┌─────┐
      │Rep1 │       │Rep2 │
      └─────┘       └─────┘
```

### Caching Layer

```
API Request → Redis Cache → MySQL
              (if hit)
```

### Message Queue (Future)

```
Blood Request Create → Message Queue → 
  → Email Service
  → Notification Service
  → Analytics Service
```

---

## Security Best Practices Implemented

1. **Input Validation:** DTO-based validation with Hibernate Validator
2. **Password Security:** BCrypt hashing with 10+ rounds
3. **JWT Token:** HS512 algorithm with 256+ bit secrets
4. **Authorization:** Role-based access control (@PreAuthorize)
5. **SQL Injection Prevention:** JPA parameterized queries
6. **CORS:** Restrictive CORS policy
7. **Error Messages:** Generic error messages (no system details)
8. **Audit Trail:** Timestamp tracking (createdAt, updatedAt)

---

## Future Enhancements

### Short Term
- [ ] Add refresh token mechanism
- [ ] Implement request/response logging
- [ ] Add email notifications
- [ ] SMS alerts for critical requests

### Medium Term
- [ ] WebSocket for real-time notifications
- [ ] Redis caching layer
- [ ] Search indexing (Elasticsearch)
- [ ] File uploads for documents

### Long Term
- [ ] Microservices architecture
- [ ] Event-driven design
- [ ] Machine learning for donor matching
- [ ] Mobile native apps
- [ ] Admin dashboard UI

---

**Version:** 1.0.0  
**Last Updated:** March 12, 2024  
**Status:** Production Ready
