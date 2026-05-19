# System Diagrams

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Date:** 2026-05-18

All diagrams are written in Mermaid format. GitHub renders these automatically.
Use [mermaid.live](https://mermaid.live) to preview locally.

---

## 1. High-Level Architecture Diagram

```mermaid
graph TD
    C1[Client 1] -->|TCP Port 5000| S[Server.java\nServerSocket]
    C2[Client 2] -->|TCP Port 5000| S
    C3[Client N] -->|TCP Port 5000| S

    S -->|accept + new Thread| H1[ClientHandler\nThread-1]
    S -->|accept + new Thread| H2[ClientHandler\nThread-2]
    S -->|accept + new Thread| HN[ClientHandler\nThread-N]

    H1 --> IO1[readLine / println]
    H2 --> IO2[readLine / println]
    HN --> ION[readLine / println]
```

---

## 2. Thread Lifecycle Diagram

```mermaid
stateDiagram-v2
    [*] --> NEW : Client connects\nThread created
    NEW --> RUNNABLE : thread.start()
    RUNNABLE --> BLOCKED : readLine() waiting
    BLOCKED --> RUNNABLE : Data arrives
    RUNNABLE --> TERMINATED : exit command\nor IOException
    TERMINATED --> [*] : Socket closed\nThread GC'd
```

---

## 3. Connection Sequence Diagram

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server
    participant H as ClientHandler Thread

    C->>S: TCP Connect (SYN)
    S->>C: TCP Accept (SYN-ACK)
    C->>S: ACK — connection established

    S->>H: new Thread(ClientHandler).start()
    H->>C: "Connected to server!"

    loop Message Exchange
        C->>H: "hello world"
        H->>C: "Server received: hello world"
    end

    C->>H: "exit"
    H->>C: "Server received: exit"
    H->>H: socket.close()
    H->>H: Thread terminates
```

---

## 4. Class Diagram (Current)

```mermaid
classDiagram
    class Server {
        -int PORT
        +main(String[] args) void
    }

    class ClientHandler {
        -Socket socket
        +ClientHandler(Socket socket)
        +run() void
    }

    class Client {
        +main(String[] args) void
    }

    Server --> ClientHandler : creates
    ClientHandler ..|> Runnable : implements
    Client --> Server : connects via TCP
```

---

## 5. Target Architecture Diagram (Level 4+)

```mermaid
graph TD
    subgraph CLIENTS
        C1[Client 1]
        C2[Client 2]
        CN[Client N]
    end

    subgraph NETWORK LAYER
        SA[SocketAcceptor]
        CM[ConnectionManager]
        TP[ThreadPool\nExecutorService]
    end

    subgraph COMMAND LAYER
        CP[CommandParser]
        CR[CommandRouter]
        REG[CommandRegistry\n/login /msg /list /quit]
    end

    subgraph SERVICE LAYER
        AS[AuthService]
        MS[MessageService]
        SS[SessionService]
    end

    subgraph DATA LAYER
        DB[(PostgreSQL)]
        CACHE[(Redis)]
    end

    C1 & C2 & CN -->|TCP| SA
    SA --> CM --> TP
    TP --> CP --> CR --> REG
    REG --> AS & MS & SS
    AS & MS & SS --> DB
    AS & SS --> CACHE
```

---

## 6. Data Flow Diagram

```mermaid
flowchart LR
    A[Client types message] --> B[output.println]
    B --> C[TCP Stream]
    C --> D[input.readLine in ClientHandler]
    D --> E{Is exit?}
    E -- Yes --> F[socket.close\nThread terminates]
    E -- No --> G[Process message]
    G --> H[output.println response]
    H --> I[TCP Stream reverse]
    I --> J[Client receives response]
    J --> A
```

---

## 7. Future: Distributed Architecture (Level 12+)

```mermaid
graph TD
    subgraph Node A - Server 1
        S1[Server Instance 1]
        H1[ClientHandlers]
    end

    subgraph Node B - Server 2
        S2[Server Instance 2]
        H2[ClientHandlers]
    end

    subgraph Shared Infrastructure
        LB[Load Balancer]
        KAFKA[Apache Kafka\nEvent Bus]
        REDIS[Redis Cluster\nSession Cache]
        DB[(PostgreSQL\nPrimary + Replicas)]
    end

    CLIENT[Clients] --> LB
    LB --> S1 & S2
    S1 & S2 --> KAFKA
    S1 & S2 --> REDIS
    S1 & S2 --> DB
    KAFKA --> S1 & S2
```
