# Production-Grade Digital Wallet Backend

A production-style, high-concurrency Digital Wallet backend built with **Java 21**, **Spring Boot 3**, **Spring Security (JWT)**, **Spring Data JPA / Hibernate**, and **MySQL**.

This project demonstrates core Java backend engineering principles including ACID transactional money transfers, optimistic locking for concurrency control, duplicate-request idempotency protection, role-based security, pagination, audit logging, unit/integration testing, and clean layered architecture.

---

## 1. Architecture Overview

The system follows a clean, modular monolith layered architecture:

```text
Client (Web / Mobile / Postman / Swagger)
  │
  ▼
REST Controllers (@RestController, DTO Validation, OpenAPI)
  │
  ▼
Spring Security Filter Chain (JwtAuthenticationFilter & SecurityContext)
  │
  ▼
Business Service Layer (@Service, @Transactional Boundaries, Idempotency, Logic)
  │
  ▼
Data Access Layer (Spring Data JPA Repositories)
  │
  ▼
MySQL Database (Relational Schema with Indexes, Foreign Keys, Versioning)
```

---

## 2. Database ER Diagram

```text
+-----------------------+         +-----------------------+
|         users         |         |        wallets        |
+-----------------------+         +-----------------------+
| id (PK, AUTO_INC)     | 1   1   | id (PK, AUTO_INC)     |
| full_name             |<------->| user_id (FK, UNIQUE)  |
| email (UNIQUE, INDEX) |         | balance (DECIMAL 19,4)|
| password (BCrypt)     |         | currency (VARCHAR)    |
| role (USER / ADMIN)   |         | status (ACTIVE/FROZEN)|
| created_at            |         | version (@Version)    |
| updated_at            |         | created_at, updated_at|
+-----------------------+         +-----------------------+
       |                                      
       | 1                                    
       |                                      
       | N                                    
+---------------------------------------+
|             transactions              |
+---------------------------------------+
| id (PK, AUTO_INC)                     |
| reference (UNIQUE, INDEX)             |
| sender_id (FK, INDEX)                 |
| receiver_id (FK, INDEX)               |
| amount (DECIMAL 19,4)                 |
| type (DEPOSIT/WITHDRAWAL/TRANSFER)    |
| status (PENDING/SUCCESS/FAILED, INDEX)|
| description                           |
| idempotency_key (UNIQUE, INDEX)       |
| created_at (INDEX)                    |
+---------------------------------------+
       |
       | 1
       |
       | N
+---------------------------------------+
|              audit_logs               |
+---------------------------------------+
| id (PK, AUTO_INC)                     |
| user_id (FK, INDEX)                   |
| action (LOGIN/REGISTER/TRANSFER/...)  |
| entity_type, entity_id                |
| ip_address, description               |
| timestamp (INDEX)                     |
+---------------------------------------+
```

---

## 3. Authentication & Security Flow

```text
Client                       Backend (Spring Security)
  │                                     │
  │─── POST /api/auth/register ────────>│ Validates payload -> Hashes password with BCrypt
  │                                     │ Creates User & Active Wallet -> Generates JWT
  │<─── 201 Created (JWT Token) ────────│
  │                                     │
  │─── POST /api/auth/login ───────────>│ Validates credentials via AuthenticationManager
  │<─── 200 OK (JWT Token) ─────────────│ Generates signed JJWT token
  │                                     │
  │─── GET /api/wallet/me ─────────────>│ JwtAuthenticationFilter extracts Bearer token
  │    (Authorization: Bearer <JWT>)    │ Validates signature -> Populates SecurityContext
  │<─── 200 OK (Wallet Details) ────────│ Executes controller within user principal context
```

### Security Highlights:
* **Stateless JWT Authentication**: No HTTP sessions stored on the server.
* **Role-Based Authorization**: `USER` and `ADMIN` roles enforced at filter chain & method level (`@PreAuthorize("hasRole('ADMIN')")`).
* **IDOR Prevention**: User identity is extracted strictly from the authenticated `SecurityContext`, preventing users from reading or modifying financial data belonging to other accounts.

---

## 4. Money Transfer & Transaction Integrity

Money transfer is executed atomically inside a declarative `@Transactional` boundary:

```text
Sender Request (Transfer ₹500 to receiver@example.com)
  │
  ├── 1. Validate Amount (> 0) & Self-transfer check
  ├── 2. Check Idempotency-Key header -> If duplicate key exists, return previous transaction
  ├── 3. Retrieve Sender & Receiver Users & Wallets
  ├── 4. Verify Sender Wallet is ACTIVE
  ├── 5. Verify Receiver Wallet is ACTIVE
  ├── 6. Check Sender Balance >= ₹500 (throws InsufficientBalanceException if invalid)
  ├── 7. Debit Sender Wallet Balance (senderWallet.balance - ₹500)
  ├── 8. Credit Receiver Wallet Balance (receiverWallet.balance + ₹500)
  ├── 9. Persist Wallet updates -> Optimistic Locking (@Version) verifies version count
  ├── 10. Save Transaction record (Reference: TXN-20260819-8F72A1, Status: SUCCESS)
  ├── 11. Create AuditLog record
  └── 12. Commit Transaction

IF ANY STEP FAILS OR THROWS AN EXCEPTION -> TRANSACTION ROLLS BACK COMPLETELY!
```

---

## 5. Concurrency Strategy: Optimistic Locking

To prevent race conditions during simultaneous deposit/withdrawal/transfer requests against the same wallet, the `Wallet` entity incorporates JPA optimistic locking:

```java
@Version
private Long version;
```

### How Optimistic Locking Works:
1. **Request A** and **Request B** read Wallet balance ₹10,000 simultaneously (`version = 1`).
2. **Request A** withdraws ₹8,000 -> Updates DB: `SET balance = 2000, version = 2 WHERE id = 10 AND version = 1`. Success!
3. **Request B** tries to withdraw ₹7,000 -> Tries: `SET balance = 3000, version = 2 WHERE id = 10 AND version = 1`.
4. Since `version` is now `2`, 0 rows are updated. Spring/Hibernate throws `ObjectOptimisticLockingFailureException`.
5. The `GlobalExceptionHandler` intercepts the exception and returns a clean `409 CONFLICT` response (`CONCURRENT_UPDATE_CONFLICT`), preventing negative balance or lost updates.

---

## 6. Idempotency Implementation

In payment systems, network retries or accidental double clicks can resend transfer requests. 

The backend supports an optional or required `Idempotency-Key` HTTP header:

```http
POST /api/transactions/transfer
Authorization: Bearer <JWT>
Idempotency-Key: idemp-987654321-abc
```

* **Database Level Constraint**: The `transactions` table has a `UNIQUE` constraint on `idempotency_key`.
* **Execution Flow**: Before processing money movement, `TransactionService` checks `findByIdempotencyKey(key)`. If found, it immediately returns the original `TransactionResponse` without re-debiting or re-crediting money.

---

## 7. Database Indexing Strategy

Indexes are added to optimize high-frequency query patterns:

| Index Name | Table | Column(s) | Primary Reason |
| :--- | :--- | :--- | :--- |
| `idx_user_email` | `users` | `email` | Instant lookup during login and receiver search. |
| `idx_txn_sender_id` | `transactions` | `sender_id` | High performance filtering for user's debit transactions. |
| `idx_txn_receiver_id` | `transactions` | `receiver_id` | High performance filtering for user's credit transactions. |
| `idx_txn_status` | `transactions` | `status` | Scheduled jobs querying `PENDING` transactions. |
| `idx_txn_created_at` | `transactions` | `created_at` | Efficient date-range filtering and sorting (`ORDER BY created_at DESC`). |
| `idx_txn_reference` | `transactions` | `reference` | Direct reference lookups (`TXN-YYYYMMDD-XXXXXX`). |
| `idx_txn_idempotency_key` | `transactions` | `idempotency_key` | Fast idempotency deduplication checks. |

---

## 8. API Documentation (Swagger / OpenAPI)

Interactive Swagger UI documentation is available at:
`http://localhost:8080/swagger-ui.html`

OpenAPI specification JSON:
`http://localhost:8080/v3/api-docs`

Swagger is configured with a **JWT Bearer Security Scheme**. You can log in via `/api/auth/login`, paste the token into Swagger's "Authorize" modal, and test all protected endpoints directly.

---

## 9. Running Locally

### Option 1: Docker Compose (Recommended)

1. Clone repository:
   ```bash
   git clone https://github.com/example/wallet.git
   cd wallet
   ```
2. Start MySQL and Backend containers:
   ```bash
   docker compose up --build
   ```
3. Access application:
   - Frontend UI: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### Option 2: Maven & Local Spring Boot

```bash
./mvnw clean spring-boot:run
```

---

## 10. Running Tests

Run full unit and integration test suite:

```bash
./mvnw clean test
```

### Test Coverage:
* `AuthServiceTest`: User registration, duplicate email handling, login authentication.
* `WalletServiceTest`: Deposits, withdrawals, frozen wallet protection, balance validation.
* `TransactionServiceTest`: Money transfers, self-transfer checks, idempotency deduplication.
* `MoneyTransferIntegrationTest`: Full Spring Context integration tests verifying ACID database rollbacks.

---

## 11. Core Technical Interview Q&A

### Q1: Why use `BigDecimal` instead of `double` or `float` for monetary amounts?
`double` and `float` use IEEE 754 floating-point representation, which suffers from binary rounding errors (e.g., `0.1 + 0.2 = 0.30000000000000004`). In financial systems, accumulating floating-point inaccuracies leads to monetary loss and accounting mismatches. `BigDecimal` provides exact arbitrary-precision decimal arithmetic.

### Q2: How does `@Transactional` guarantee transaction rollback on failure?
Spring creates a dynamic AOP proxy around `@Transactional` service methods. It starts a database transaction before method execution. If the method executes successfully, the proxy commits the transaction. If an unhandled `RuntimeException` or `Error` is thrown, the proxy catches it and invokes `connection.rollback()`, ensuring no partial financial state changes persist.

### Q3: How do you prevent double-spending in concurrent transfer requests?
We employ two defensive layers:
1. **JPA Optimistic Locking (`@Version`)**: Ensures that if two concurrent requests attempt to update the same wallet simultaneously, only one succeeds and the second fails with `ObjectOptimisticLockingFailureException`.
2. **Idempotency Keys**: Unique database index on `idempotency_key` guarantees duplicate client retries return the original transaction result without re-executing money movement.

### Q4: How do you prevent Insecure Direct Object References (IDOR)?
We do not trust client-supplied user IDs in API path variables or request bodies for authorization. The backend extracts the user's identity directly from the authenticated `SecurityContext` populated by the `JwtAuthenticationFilter`. When requesting transaction details (`/api/transactions/{id}`), the service verifies that the authenticated user is either the sender or receiver (or has `ADMIN` role).
