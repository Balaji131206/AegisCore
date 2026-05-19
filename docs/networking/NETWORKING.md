# Networking Documentation

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Date:** 2026-05-18

---

## 1. Protocol Specification

| Property | Value |
|----------|-------|
| Transport | TCP |
| Server Port | 5000 |
| Encoding | UTF-8 |
| Message Delimiter | Newline (`\n`) |
| I/O Abstraction | `BufferedReader` / `PrintWriter` (blocking) |
| Connection Type | Persistent, stateful |

**Why TCP over UDP?** TCP guarantees ordered, reliable delivery — essential for authentication commands and session state. A dropped login packet cannot be silently ignored.

---

## 2. Connection Lifecycle

```
CLIENT                              SERVER
  │  ── TCP SYN ──────────────────►  │   1. Client initiates handshake
  │  ◄── TCP SYN-ACK ─────────────  │   2. Server acknowledges
  │  ── TCP ACK ──────────────────►  │   3. Connection established
  │                                   │   4. accept() returns → ClientHandler thread born
  │  ◄── "Connected to server!" ───  │   5. Welcome message sent
  │  ── "hello" ───────────────────►  │   6. Client sends message
  │  ◄── "Server received: hello" ─  │   7. Server echoes response
  │  ── "exit" ───────────────────►  │   8. Termination signal
  │  ◄── TCP FIN ──────────────────  │   9. Server closes socket → thread dies
```

---

## 3. Message Format

**Current (Level 1–2):** Raw UTF-8 text lines terminated by `\n`
```
hello world
exit
```

**Target (Level 5 — Command Engine):**
```
/command [arg1] [arg2]
/login alice password123
/msg bob hey there
/list
```

**Target (Level 8+ — Authenticated):**
```
[JWT_TOKEN] /command [args]
```

---

## 4. Port Configuration

| Stage | Port | Notes |
|-------|------|-------|
| Level 1–3 | 5000 (hardcoded) | Dev only |
| Level 4+ | Env var `SERVER_PORT` | Configurable |
| Level 8 | 5443 (TLS) | Encrypted |
| Level 15 | K8s LoadBalancer | Cloud-native |

---

## 5. Known Networking Issues

| Issue | Impact | Fix Level |
|-------|--------|-----------|
| No `SO_TIMEOUT` — `readLine()` blocks forever | Zombie threads | Level 4 |
| Default backlog of 50 | Dropped connections under burst | Level 6 |
| Hardcoded `localhost` in `Client.java` | Not deployable | Level 4 |
| No SSL/TLS — plaintext on wire | Trivially interceptable | Level 8 |

---

## 6. Future Networking Layers

| Level | Technology | Change |
|-------|-----------|--------|
| 8 | `SSLSocket` | Encrypted transport |
| 10 | WebSocket JSR-356 | Browser full-duplex |
| 11 | Java NIO `Selector`/`Channel` | Non-blocking event-driven |
| 12 | Apache Kafka | Distributed event streaming |
| 14 | gRPC | Type-safe inter-service RPC |
