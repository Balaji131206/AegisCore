# Software Requirements Specification (SRS)

**Project:** Distributed Multithreaded Secure Server Platform
**Document Version:** 1.0
**System Version:** 0.2.0
**Date:** 2026-05-18
**Stage:** Level 2 — Multithreading

---

## 1. Introduction

### 1.1 Purpose

The purpose of this system is to develop a scalable, multithreaded TCP communication server capable of handling concurrent client sessions, processing structured commands, persisting user data, and eventually operating as a distributed, fault-tolerant backend infrastructure suitable for banking, trading, gaming, IoT, and cybersecurity applications.

This SRS defines the complete requirements — functional, non-functional, and constraint-based — across all 17 development levels of the project.

### 1.2 Scope

The system encompasses:
- A TCP socket server accepting concurrent client connections
- A command-processing engine interpreting structured client messages
- A user authentication and session management subsystem
- A database persistence layer
- An event-driven communication backbone
- A distributed clustering layer
- A security enforcement layer (encryption, hashing, authorization)

**Out of Scope (current level):**
- WebSocket support (Level 10)
- Distributed consensus (Level 12)
- AI/ML integration (Level 17)

### 1.3 Definitions

| Term | Definition |
|------|-----------|
| Client | A TCP socket connection established by an end-user process |
| ClientHandler | A server-side thread responsible for one client's lifecycle |
| Session | An authenticated, stateful association between a user and the server |
| Command | A structured message from client to server, prefixed with `/` |
| Thread Pool | A bounded set of reusable worker threads managed by `ExecutorService` |
| NIO | Java Non-Blocking I/O — `java.nio` package using `Selector`, `Channel`, `Buffer` |

---

## 2. Functional Requirements

### FR-1: Multi-Client Concurrency
The server SHALL accept and maintain multiple simultaneous client connections without serializing them.

**Acceptance Criteria:**
- 10 clients connected simultaneously, each sending messages independently
- No client blocks another client's communication

**Status:** ✅ Met (Level 2 — thread per client)

---

### FR-2: Bidirectional Communication
Each connected client SHALL be able to send messages to and receive responses from the server.

**Acceptance Criteria:**
- Client sends text → server echoes it back with prefix "Server received:"
- Response arrives on same connection

**Status:** ✅ Met

---

### FR-3: Clean Session Termination
The server SHALL gracefully close client connections when the client sends the `exit` command.

**Acceptance Criteria:**
- `exit` message → server closes socket → thread terminates
- Server continues accepting other clients after disconnect

**Status:** ✅ Met

---

### FR-4: Shared Client Registry
The server SHALL maintain a thread-safe registry of all currently connected clients.

**Acceptance Criteria:**
- Any thread can safely read the client list
- Concurrent additions/removals do not corrupt the list

**Status:** ⏳ Level 3

---

### FR-5: Global Broadcast
The server SHALL allow a message from one client to be delivered to all other connected clients.

**Acceptance Criteria:**
- Client A sends message → all other connected clients receive it
- No message loss under concurrent access

**Status:** ⏳ Level 3

---

### FR-6: Command Processing
The server SHALL parse and dispatch structured commands from clients.

| Command | Action |
|---------|--------|
| `/login <user> <pass>` | Authenticate user |
| `/register <user> <pass>` | Create new account |
| `/msg <target> <text>` | Send private message |
| `/list` | Return list of online users |
| `/quit` | Disconnect cleanly |

**Status:** ⏳ Level 5

---

### FR-7: User Authentication
The server SHALL authenticate users against a persistent store using hashed credentials.

**Acceptance Criteria:**
- Passwords stored as BCrypt hashes (never plaintext)
- Failed login returns `ERROR: Invalid credentials`
- Successful login returns session token

**Status:** ⏳ Level 8

---

### FR-8: Message Persistence
The server SHALL persist all messages to a relational database.

**Acceptance Criteria:**
- Messages survive server restart
- Messages queryable by sender, receiver, and timestamp

**Status:** ⏳ Level 7

---

### FR-9: Session Token Validation
The server SHALL validate JWT session tokens on every command that requires authentication.

**Acceptance Criteria:**
- Expired tokens rejected with `ERROR: Session expired`
- Tampered tokens rejected with `ERROR: Invalid token`

**Status:** ⏳ Level 8

---

## 3. Non-Functional Requirements

### NFR-1: Concurrency
The server SHOULD support a minimum of **1,000 concurrent clients** without crashing.

**Current state:** ~200–500 (limited by OS thread count)
**Target state:** 1,000+ via thread pool (Level 6), 10,000+ via NIO (Level 11)

---

### NFR-2: Response Time
Average server response time per request SHOULD be **< 100ms** under normal load.

**Baseline:** Sub-millisecond at current scale (no DB, no auth)
**Target:** < 100ms with DB + auth (Level 8)

---

### NFR-3: Availability
The server MUST remain operational after individual client connection failures.

**Current state:** ✅ `IOException` per client does not crash server
**Target state:** Server survives own internal errors with auto-restart (Level 13)

---

### NFR-4: Security
Passwords MUST NOT be stored in plaintext. Communications SHOULD be encrypted in transit.

**Current state:** ❌ No auth layer exists
**Target state:** BCrypt hashing (Level 8), SSL/TLS transport (Level 8)

---

### NFR-5: Scalability
The system architecture MUST support horizontal scaling across multiple server nodes.

**Current state:** Single-node only
**Target state:** Distributed cluster with Redis/Kafka (Level 12)

---

### NFR-6: Maintainability
The codebase MUST follow layered architecture principles. No business logic in socket handlers.

**Current state:** ⚠️ All logic in `ClientHandler` (acceptable at Level 2)
**Target state:** Layered architecture enforced at Level 4

---

### NFR-7: Observability
The server SHOULD emit structured logs for all connection events, command executions, and errors.

**Current state:** `System.out.println` only
**Target state:** SLF4J + Logback with structured JSON logs (Level 4)

---

## 4. Constraints

| Constraint | Detail |
|-----------|--------|
| Language | Java 21 LTS |
| Build | Standard `javac` (Maven/Gradle at Level 4) |
| Database | PostgreSQL (JDBC, no ORM until Level 7) |
| Protocol | TCP; no UDP, no HTTP at base layer |
| Port | 5000 (configurable via environment variable in Level 4) |
| Encoding | UTF-8 |
| Auth | No external OAuth; custom JWT implementation |
| OS | Cross-platform (Windows, Linux, macOS) |

---

## 5. Assumptions

- Clients communicate using line-terminated UTF-8 text (`\n` delimiter)
- Server runs in a trusted network environment at Level 1–7
- SSL/TLS added at Level 8 when external exposure is assumed
- Database is local PostgreSQL instance until Level 15 (cloud DB)

---

## 6. Risk Register

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|-----------|
| Thread explosion under load | HIGH | Server crash | Thread pool at Level 6 |
| Race condition in client registry | HIGH | Data corruption | Synchronized collections at Level 3 |
| SQL injection via command input | HIGH | DB breach | Prepared statements at Level 7 |
| Plaintext password storage | HIGH | Credential exposure | BCrypt at Level 8 |
| Single point of failure | MEDIUM | Full outage | Distributed cluster at Level 12 |
| Memory leak from unclosed sockets | MEDIUM | OOM crash | try-with-resources + Level 13 |
