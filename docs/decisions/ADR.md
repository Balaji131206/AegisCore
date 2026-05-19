# Architecture Decision Records (ADRs)

**Project:** Distributed Multithreaded Secure Server Platform

This log records every significant architectural decision made during development. Each entry answers: **What** was decided, **Why**, and **What are the consequences.**

Engineers who read this document should understand not just what the code does, but *why it was built this way.*

---

## ADR-001 — TCP over UDP

**Date:** 2026-05-17
**Status:** Accepted
**Level:** 1

### Decision
Use TCP (Transmission Control Protocol) as the transport layer.

### Reason
- Client sessions require reliable, ordered message delivery
- Commands like `/login` or fund transfers cannot be retransmitted from a partially corrupted state
- TCP provides flow control and congestion control at no application-layer cost
- UDP's performance advantage is irrelevant at development-server scale

### Consequences
- Slightly higher overhead per packet (TCP headers, handshake)
- Connection state must be managed per-client
- Acceptable trade-off: correctness over raw throughput at this stage

---

## ADR-002 — One Thread Per Client (Level 2)

**Date:** 2026-05-18
**Status:** Accepted (temporary — will be superseded at Level 6)
**Level:** 2

### Decision
Spawn one OS thread per accepted client connection using `new Thread(handler).start()`.

### Reason
- Simplest correct model for concurrent client handling
- Allows full blocking I/O without callback complexity
- Appropriate for learning concurrency fundamentals at Level 2
- Avoids premature optimization before understanding the problem space

### Consequences
- **Positive:** Each client's logic is isolated, easy to reason about
- **Negative:** Thread explosion — 1,000 clients = 1,000 threads = ~1GB stack memory
- **Negative:** Not deployable beyond ~200–500 simultaneous clients
- **Superseded by:** `ExecutorService` at Level 6, Java NIO at Level 11

---

## ADR-003 — Blocking I/O Model (Level 1–2)

**Date:** 2026-05-17
**Status:** Accepted (temporary — superseded at Level 11)
**Level:** 1

### Decision
Use `BufferedReader.readLine()` and `PrintWriter.println()` for socket I/O (blocking model).

### Reason
- Mental model is synchronous and linear — easy to learn and debug
- Pairs naturally with the one-thread-per-client model
- No callback hell or event loop complexity at this stage
- Correct behavior trivially verifiable

### Consequences
- `readLine()` blocks the thread indefinitely until data arrives
- No timeout handling (zombie threads possible)
- Cannot handle 10,000+ clients on blocking model
- **Superseded by:** Java NIO `Selector`/`Channel`/`Buffer` at Level 11

---

## ADR-004 — Port 5000

**Date:** 2026-05-17
**Status:** Accepted
**Level:** 1

### Decision
Bind server to port 5000.

### Reason
- Above 1024: does not require root/administrator privileges on Linux/macOS
- Not a well-known service port (not HTTP/80, HTTPS/443, MySQL/3306, etc.)
- Conventional choice for Java development servers

### Consequences
- Potential conflict with other local development services (Python Flask also defaults to 5000)
- Will become configurable via environment variable `SERVER_PORT` at Level 4

---

## ADR-005 — Newline as Message Delimiter

**Date:** 2026-05-17
**Status:** Accepted (evolves at Level 5)
**Level:** 1

### Decision
Use newline character (`\n`) as the message delimiter. Rely on `readLine()` / `println()` contract.

### Reason
- Natural contract provided by `BufferedReader.readLine()`
- Simplest possible framing for text-based protocol
- Human-readable during debugging with `telnet` or `netcat`

### Consequences
- Messages cannot contain newlines (breaks framing)
- No length-prefix framing = no binary payload support
- **Evolves to:** Length-prefixed binary framing at Level 10 (WebSocket), then Protocol Buffers at Level 14 (gRPC)

---

## ADR-006 — Java 21 as Target Version

**Date:** 2026-05-16
**Status:** Accepted
**Level:** 0

### Decision
Target Java 21 LTS.

### Reason
- Long-Term Support release (supported until 2031)
- Includes Virtual Threads (Project Loom) — relevant at Level 11 as alternative to NIO
- Pattern matching, records, sealed classes available for clean data modeling
- Industry standard for new Java backend projects

### Consequences
- Cannot run on Java 8/11 environments without modification
- Enables future use of `Thread.ofVirtual()` as high-performance alternative to thread pools

---

## ADR-007 — No ORM at Database Layer (Level 7)

**Date:** Planned for Level 7
**Status:** Proposed
**Level:** 7

### Decision
Use raw JDBC with `PreparedStatement` at Level 7. Hibernate/JPA considered at Level 7+.

### Reason
- Understanding raw SQL and JDBC is mandatory before abstracting it away
- ORM frameworks hide critical behavior (N+1 queries, lazy loading, connection management)
- Premature ORM adoption is a common cause of production performance disasters

### Consequences
- More verbose data access code
- Explicit SQL gives full control over query performance
- **Upgrade path:** Introduce Hibernate/JPA once JDBC is mastered

---

*Every decision in this project is deliberate, documented, and traceable.*
*"We did it this way because it was fast" is not an engineering record.*
*"We did it this way because of X constraint, at cost Y, superseded at Level Z" is.*
