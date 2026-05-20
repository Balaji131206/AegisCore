# Concurrency Documentation

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Date:** 2026-05-18

---

## 1. Current Threading Model

**Model:** One OS thread per connected client.

```java
// Server.java — current implementation
Socket clientSocket = serverSocket.accept();
ClientHandler handler = new ClientHandler(clientSocket);
Thread thread = new Thread(handler);
thread.start();
```

Each client gets an independent thread. Threads do not share any data in the current implementation. This is intentionally simple at Level 2.

---

## 2. Thread Lifecycle

```
Client connects
      │
      ▼
[NEW] — Thread object created
      │
      ▼ thread.start()
[RUNNABLE] — run() executing
      │
      ├──► [BLOCKED] on input.readLine()  ← most of its life is here
      │         │
      │         ▼ data arrives
      │    [RUNNABLE] processes message, sends response
      │         │
      │         └──► back to BLOCKED (waiting for next message)
      │
      ▼ client sends "exit" or disconnects
[TERMINATED] — socket.close(), thread garbage-collected
```

---

## 3. Shared Resources (Level 3 - Active)

**Shared Registry:** `SharedClientRegistry` is a global singleton tracking all active connections in a thread-safe `ConcurrentHashMap`.

This enables high-performance real-time global broadcast messaging across all client threads without synchronization bottlenecks.

---

## 4. Concurrency Hazards — What's Coming at Level 3

### 4.1 Race Condition

**Scenario:** Two threads write to a shared `ArrayList<ClientHandler>` simultaneously.

```java
// Thread A                    // Thread B
clientList.add(handlerA);      clientList.add(handlerB);
// Internal array resize during simultaneous add → CORRUPTION
```

**Fix:** Use `CopyOnWriteArrayList` or `Collections.synchronizedList()`.

### 4.2 ConcurrentModificationException

**Scenario:** Thread A iterates the client list to broadcast. Thread B removes a disconnected client.

```java
// Thread A                    // Thread B
for (ClientHandler c : list)   list.remove(deadClient);
    c.send(message);           // ← throws ConcurrentModificationException
```

**Fix:** Use `CopyOnWriteArrayList` (iteration on snapshot) or synchronized iteration block.

### 4.3 Memory Visibility

**Scenario:** Thread A sets `boolean running = false`. Thread B never sees it due to CPU cache.

```java
// Thread A                    // Thread B
running = false;               while (running) { ... }
                               // ← may loop forever
```

**Fix:** Declare as `volatile boolean running`.

### 4.4 Lost Update (Banking-grade example)

```java
// Thread A and B both read balance = 1000
balance += 500;   // both write 1500 instead of 2000
// 500 units LOST
```

**Fix:** Use `synchronized` block or `AtomicInteger`/`AtomicLong`.

---

## 5. Synchronization Strategy (Level 3 - Active)

| Shared Resource | Strategy | Status | Reason |
|----------------|----------|--------|--------|
| Client registry | `ConcurrentHashMap` | ✅ Implemented | High-performance thread-safe concurrent reads, writes, and iterations |
| User session map | `ConcurrentHashMap` | ⏳ Level 8 | Frequent concurrent reads and writes |
| Message counter/stats | `AtomicLong` | ⏳ Level 6 | Lock-free increment |
| Balance/financial data | `synchronized` method | ⏳ Level 7 | Explicit lock semantics required |
| Server running flag | `volatile boolean` | ⏳ Level 13 | Single-writer visibility |

---

## 6. Thread Explosion Problem

| Clients | Threads | Memory (est.) |
|---------|---------|--------------|
| 100 | 100 | ~100 MB |
| 500 | 500 | ~500 MB |
| 1,000 | 1,000 | ~1 GB |
| 10,000 | 10,000 | ~10 GB — **server dies** |

**Resolution at Level 6:**
```java
ExecutorService pool = Executors.newFixedThreadPool(200);
pool.submit(new ClientHandler(socket));
// 10,000 clients queued, 200 handled concurrently
// Memory: 200MB instead of 10GB
```

---

## 7. Deadlock Prevention Rules

A deadlock occurs when Thread A holds Lock 1 and waits for Lock 2, while Thread B holds Lock 2 and waits for Lock 1.

**Rules to follow (Level 3+):**
1. Always acquire locks in the same consistent order
2. Never hold a lock while performing I/O (socket reads/writes)
3. Prefer `ConcurrentHashMap`/`CopyOnWriteArrayList` over manual `synchronized`
4. Use `tryLock()` with timeout instead of indefinite `lock()` for critical sections
5. Use thread dumps (`jstack`) to detect deadlocks in testing

---

## 8. Future Concurrency Evolution

| Level | Change | Why |
|-------|--------|-----|
| 3 | Shared registry with `ConcurrentHashMap` | ✅ Complete — Enables global real-time broadcast |
| 6 | `ExecutorService` fixed thread pool | Prevent thread explosion |
| 9 | Async event bus with `CompletableFuture` | Decouple components |
| 11 | Java NIO — 1 thread handles all connections | 10,000x scalability |
| 12 | Distributed coordination via ZooKeeper/Kafka | Multi-node state |

---

## 9. Shared State & Broadcast Implementation (Level 3)

### 9.1 The Shared Client Registry (`SharedClientRegistry.java`)
The server maintains a single instance of `SharedClientRegistry` using the thread-safe **Singleton Pattern**. 
This registry maps client identifiers (`clientId` based on IP and port) to their respective `ClientHandler` instances using a thread-safe `ConcurrentHashMap`.

```java
public class SharedClientRegistry {
    private static final ConcurrentHashMap<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private static final SharedClientRegistry instance = new SharedClientRegistry();

    private SharedClientRegistry() {}

    public static SharedClientRegistry getInstance() {
        return instance;
    }
    
    public void addClient(String clientId, ClientHandler handler) {
        connectedClients.put(clientId, handler);
    }

    public void removeClient(String clientId) {
        connectedClients.remove(clientId);
    }
    
    public void BroadcastMessage(String message) {
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(message);
        }
    }
}
```

### 9.2 Broadcast Execution Flow
When a client sends a message, the server distributes it using a non-blocking background broadcast:

```
[Client] 
   │ (TCP message: "hello world")
   ▼
[ClientHandler (Runnable Thread)]
   │ 1. input.readLine() reads message
   │ 2. registry.BroadcastMessage("[BROADCAST] ...")
   ▼
[SharedClientRegistry (Singleton)]
   │ 3. Iterates over connectedClients.values()
   ▼
[ClientHandler 1] ──► sendMessage() ──► socket write (client 1)
[ClientHandler 2] ──► sendMessage() ──► socket write (client 2)
[ClientHandler N] ──► sendMessage() ──► socket write (client N)
```

### 9.3 Client Asynchronous Socket Listener
To prevent blocking and visual latency on the client console when waiting for server broadcasts, `Client.java` is designed with an asynchronous dual-thread setup:
1. **Main Thread**: Exclusively handles blocking keyboard input (`System.in`) and writes user messages directly to the server.
2. **Listener Thread (`ServerListener` Daemon)**: Runs continuously in the background to read incoming messages from the socket and immediately print them on the terminal.

This decouples keyboard rendering from socket buffer reading, enabling instant, real-time message distribution.
