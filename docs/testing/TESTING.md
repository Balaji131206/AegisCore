# Testing Documentation

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Date:** 2026-05-18

---

## 1. Testing Philosophy

This project tests at three levels:
1. **Unit tests** — individual method and class behavior
2. **Integration tests** — server + client interaction over real sockets
3. **Stress tests** — concurrent load to verify thread safety and stability

**Rule:** If it's not tested, it's broken. Document every test case — not just that it passed, but what it was supposed to verify.

---

## 2. Current Test Coverage

| Component | Test Type | Status |
|-----------|-----------|--------|
| Server socket binding | Manual | ✅ Tested |
| Single client connect + disconnect | Manual | ✅ Tested |
| Multiple simultaneous clients | Manual (3 terminals) | ✅ Tested |
| `exit` command termination | Manual | ✅ Tested |
| Server stability after client crash | Manual | ✅ Tested |
| Automated unit tests | JUnit | ⏳ Level 4 |
| Stress test (100+ clients) | Automated | ⏳ Level 6 |

---

## 3. Manual Test Cases

### TC-001: Server Start

**Objective:** Verify server binds and listens on port 5000.

**Steps:**
1. Compile: `javac Server.java ClientHandler.java Client.java`
2. Run: `java Server`
3. Observe console output

**Expected:** `Server is listening on port 5000`

**Result:** ✅ PASS

---

### TC-002: Single Client Connect

**Objective:** Verify a client can connect and receive welcome message.

**Steps:**
1. Server running
2. `java Client` in new terminal

**Expected:**
```
Connected to server!
Enter message to send to server (type 'exit' to quit):
```

**Result:** ✅ PASS

---

### TC-003: Message Echo

**Objective:** Verify client message is received by server and echoed back.

**Steps:**
1. Client connected
2. Type: `hello world`
3. Press Enter

**Expected (client terminal):** `Server received: hello world`
**Expected (server terminal):** `Client says: hello world`

**Result:** ✅ PASS

---

### TC-004: Clean Disconnect

**Objective:** Verify `exit` terminates session cleanly.

**Steps:**
1. Client connected
2. Type: `exit`

**Expected (client):** `Server received: exit` → program ends
**Expected (server):** `Client disconnected.`

**Result:** ✅ PASS

---

### TC-005: Multi-Client Isolation

**Objective:** Verify multiple clients can communicate simultaneously without interference.

**Steps:**
1. Server running
2. Open 3 terminals, run `java Client` in each
3. Send messages from each independently
4. Verify each sees only its own responses

**Expected:** All 3 clients receive responses. No cross-contamination.
**Expected (server):** 3 separate "New client connected" lines, 3 threads active.

**Result:** ✅ PASS

---

### TC-006: Server Survives Client Crash

**Objective:** Verify server continues after a client abruptly closes.

**Steps:**
1. Client connected
2. Kill client terminal (close window, not `exit`)
3. Connect a new client

**Expected:** Server catches `IOException`, logs "Client disconnected.", continues accepting.

**Result:** ✅ PASS

---

## 4. Planned Test Cases (Future Levels)

### TC-100: Thread Pool Limit (Level 6)

**Objective:** Verify server rejects connections beyond pool size gracefully (no crash).

**Steps:** Automated — spawn 250 client threads, pool size = 200.
**Expected:** 200 handled, 50 queued or rejected with appropriate message.

---

### TC-101: Race Condition — Concurrent Broadcast (Level 3)

**Objective:** Verify no `ConcurrentModificationException` during simultaneous connect + broadcast.

**Steps:**
1. 50 clients connected
2. Thread A broadcasts to all clients
3. Thread B disconnects 10 clients simultaneously
4. Repeat 1000 times

**Expected:** No exceptions. All remaining clients receive broadcast.

---

### TC-102: Authentication — Brute Force Protection (Level 8)

**Objective:** Verify account lockout after 5 failed login attempts.

**Steps:** Send `/login alice wrongpassword` 6 times.
**Expected:** First 5 return `ERROR: Invalid credentials`. 6th returns `ERROR: Account locked.`

---

### TC-103: Stress Test — 500 Concurrent Clients (Level 6)

**Objective:** Verify server stability under sustained load.

**Steps:**
1. Spawn 500 client threads simultaneously
2. Each sends 100 messages
3. Each disconnects cleanly

**Expected:**
- No server crash
- No messages lost
- All clients receive all responses
- Server memory stable (no leak)

---

## 5. Test Infrastructure (Level 4+)

```
tests/
 ├── unit/
 │   ├── CommandParserTest.java
 │   ├── AuthServiceTest.java
 │   └── MessageServiceTest.java
 ├── integration/
 │   ├── ServerClientTest.java
 │   └── MultiClientTest.java
 └── stress/
     └── LoadTest.java          ← spawns N threads, measures throughput
```

**Framework:** JUnit 5 + Mockito
**Build integration:** Maven Surefire plugin
**CI:** GitHub Actions (Level 15)
