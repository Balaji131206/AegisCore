# CHANGELOG

All notable changes to this project are documented here.

This file follows the principles of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Format:
```
## [version] - YYYY-MM-DD
### Added / Changed / Fixed / Removed / Security
```

---

## [0.2.0] - 2026-05-18 — Level 2: Multithreading Entry

### Added
- `ClientHandler.java` — dedicated `Thread`-based handler for each connected client
- Per-client independent communication loop (`while readLine()`)
- Clean session termination on `exit` command
- Server-side log output for connect and disconnect events

### Changed
- Server now spawns a new `Thread` for each accepted connection instead of handling serially
- Architecture now supports N simultaneous clients (bounded by OS thread limits)

### Known Issues
- Thread explosion risk: one OS thread per client, no pool limit
- No shared state between client threads (addressed in Level 3)
- No authentication (addressed in Level 8)

### Architecture Notes
- Current threading model: `new Thread(clientHandler).start()` — intentional for Level 2 learning
- This will be replaced with `ExecutorService` in Level 6
- Blocking I/O will be replaced with Java NIO `Selector`/`Channel` in Level 11

---

## [0.1.0] - 2026-05-17 — Level 1: Raw Socket Networking

### Added
- `Server.java` — TCP server listening on port 5000
- `Client.java` — TCP client connecting to localhost:5000
- Bidirectional text-based communication over TCP
- `ServerSocket.accept()` loop for incoming connections
- `BufferedReader` / `PrintWriter` for stream-based I/O
- `.gitignore` — excludes compiled `.class` files and IDE metadata

### Architecture
- Single-threaded server (one client at a time)
- Blocking I/O: `accept()`, `readLine()` both block the main thread
- Port: 5000, Encoding: UTF-8, Delimiter: newline (`\n`)

### Decision
- Chose TCP over UDP: reliable delivery required for stateful sessions
- Chose blocking I/O for Level 1: simplest correct model before introducing threads

---

## [0.0.1] - 2026-05-16 — Level 0: Project Initialization

### Added
- Project directory structure created
- Java 21 development environment confirmed
- Initial README.md stub

### Status
- Level 0 (Java Core Foundation) confirmed complete
- Development roadmap established across 17 engineering levels

---

*Engineering record maintained from project Day 1.*
