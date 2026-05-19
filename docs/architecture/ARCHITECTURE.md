# System Architecture Document

**Project:** Distributed Multithreaded Secure Server
**Version:** 0.2.0
**Stage:** Level 2 — Multithreading
**Last Updated:** 2026-05-18

---

## 1. Purpose

This document describes the system architecture of the server platform at its current development stage and projects forward to the target architecture at Level 4+. It is the authoritative reference for understanding how components interact, how data flows, and how the system is designed to scale.

---

## 2. High-Level Architecture

### Current (Level 2)

```
┌──────────────────────────────────────────────┐
│                   CLIENTS                    │
│  [Client.java]  [Client.java]  [Client.java] │
│       │               │               │      │
└───────┼───────────────┼───────────────┼──────┘
        │  TCP/IP       │  TCP/IP       │ TCP/IP
        │  Port 5000    │  Port 5000    │ Port 5000
┌───────▼───────────────▼───────────────▼──────┐
│              SERVER (Server.java)            │
│  ServerSocket(5000)                          │
│  accept() ← blocks until connection arrives  │
│       │               │               │      │
│  Thread-1         Thread-2         Thread-N  │
│  [ClientHandler]  [ClientHandler]  [ClientHandler] │
│       │               │               │      │
│  readLine()       readLine()       readLine() │
│  println()        println()        println()  │
└──────────────────────────────────────────────┘
```

### Target (Level 4+)

```
┌─────────────────────────────────────────────────────┐
│                     CLIENT TIER                     │
└──────────────────────┬──────────────────────────────┘
                       │ TCP / WebSocket / gRPC
┌──────────────────────▼──────────────────────────────┐
│                  NETWORK LAYER                      │
│  ConnectionManager  │  SocketAcceptor               │
│  ThreadPool (ExecutorService, Level 6)              │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  COMMAND LAYER                      │
│  CommandParser  │  CommandRouter  │  CommandRegistry │
│  /login  /msg  /list  /quit  (Level 5)              │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  SERVICE LAYER                      │
│  AuthService  │  MessageService  │  SessionService   │
└──────────┬───────────────────────────┬──────────────┘
           │                           │
┌──────────▼──────────┐  ┌────────────▼──────────────┐
│  DATABASE LAYER     │  │  CACHE LAYER               │
│  JDBC / PostgreSQL  │  │  Redis (Level 12)          │
│  HikariCP pool      │  └───────────────────────────┘
└─────────────────────┘
```

---

## 3. Component Descriptions

### Server.java
**Role:** Entry point. Binds to port 5000. Runs the accept loop.

**Responsibilities:**
- Create and bind `ServerSocket`
- Block on `accept()` awaiting client connections
- For each accepted connection, create and start a `ClientHandler` thread

**Current limitation:** The accept loop runs on the main thread. If it crashes, the entire server crashes. A supervisor/restart mechanism will be added at Level 13.

---

### ClientHandler.java
**Role:** Manages the full lifecycle of a single client connection.

**Responsibilities:**
- Wrap socket streams in `BufferedReader` / `PrintWriter`
- Send welcome acknowledgement to client
- Loop: read message → process → respond
- Detect `exit` command and terminate cleanly
- Close socket on completion or error

**Implements:** `Runnable` (via `extends Thread`)

**Current limitation:** Each `ClientHandler` knows nothing about other clients. There is no shared registry. Broadcast and global state are deferred to Level 3.

---

### Client.java
**Role:** Test client used for manual and automated testing.

**Responsibilities:**
- Establish TCP connection to `localhost:5000`
- Read server welcome message
- Enter interactive send/receive loop
- Terminate on `exit` command

---

## 4. Data Flow

### Message Flow (Current)

```
Client Terminal
    │
    │ user types: "hello"
    ▼
Client.java
    │ output.println("hello")
    ▼
TCP Socket Stream
    │
    ▼
ClientHandler.java (running on Thread-N)
    │ input.readLine() returns "hello"
    │ System.out.println("Client says: hello")
    │ output.println("Server received: hello")
    ▼
TCP Socket Stream (reverse direction)
    │
    ▼
Client.java
    │ input.readLine() returns "Server received: hello"
    │ System.out.println("Server received: hello")
    ▼
Client Terminal displays response
```

---

## 5. Threading Architecture

### Current Model: One Thread Per Client

| Property | Value |
|----------|-------|
| Model | `new Thread(clientHandler).start()` |
| Thread count | Equals number of connected clients |
| Max practical clients | ~200–500 (OS-dependent) |
| Memory per thread | ~1MB JVM stack |
| Thread lifecycle | Born on connect, dies on disconnect |

### Thread State Diagram

```
       Client connects
            │
            ▼
       [NEW thread created]
            │
            ▼
       [RUNNABLE — run() executing]
            │
    ┌───────┴────────────┐
    │                    │
    ▼                    ▼
[BLOCKED on        [Running — processing
 readLine()]        message]
    │
    ▼
[client sends "exit" or disconnects]
    │
    ▼
[TERMINATED — socket closed, thread ends]
```

### Future Model: ExecutorService (Level 6)

```java
ExecutorService pool = Executors.newFixedThreadPool(100);

while (true) {
    Socket client = serverSocket.accept();
    pool.submit(new ClientHandler(client));
    // No new thread created — reuses thread from pool
}
```

### Final Model: NIO Event Loop (Level 11)

```
Selector (single thread monitors ALL channels)
    │
    ├─ Channel 1 ready → process data → return
    ├─ Channel 2 ready → process data → return
    └─ Channel N ready → process data → return

No blocking. No thread per client. Handles 10,000+ clients.
```

---

## 6. Scalability Analysis

| Stage | Model | Max Clients | Memory Cost |
|-------|-------|-------------|-------------|
| Level 2 (current) | Thread per client | ~200–500 | 1MB × N threads |
| Level 6 | Thread pool (fixed) | ~1,000–5,000 | 1MB × pool size |
| Level 11 | NIO event loop | 10,000–100,000 | ~KB per connection |
| Level 12 | Distributed cluster | Millions | Horizontally scaled |

---

## 7. Error Handling Strategy

| Scenario | Current Behavior | Target Behavior |
|----------|-----------------|-----------------|
| Client crash mid-session | `IOException` printed, thread dies | Log + clean registry removal (Level 3) |
| Server `accept()` error | Server crashes | Retry loop with backoff (Level 13) |
| Invalid client message | Echo'd back as-is | Validated and rejected (Level 5) |
| OutOfMemory from thread explosion | JVM crash | Thread pool cap enforced (Level 6) |

---

## 8. Future Architecture Decision Points

| Decision | Options | Planned Choice |
|----------|---------|---------------|
| I/O model | Blocking, NIO, Netty | NIO at Level 11 |
| Thread management | new Thread(), pool, virtual threads (Loom) | Pool L6, Loom consideration L11 |
| Inter-service comms | REST, gRPC | gRPC at Level 14 |
| Message broker | Kafka, RabbitMQ | Kafka at Level 12 |
| Database ORM | Raw JDBC, Hibernate | Hibernate at Level 7 |
