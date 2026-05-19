# Command API Reference

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Status:** Planned for Level 5

---

## Overview

This document defines the command protocol implemented at Level 5. Commands are UTF-8 text messages prefixed with `/`, followed by arguments separated by spaces.

**Format:**
```
/command [arg1] [arg2] [...]\n
```

---

## Command Reference

### `/register`
**Create a new user account.**

```
/register <username> <password>
```

| Argument | Type | Constraints |
|----------|------|-------------|
| username | String | 3–20 chars, alphanumeric + underscore |
| password | String | 8–64 chars, at least 1 digit |

**Responses:**
```
OK: Account created. Please /login to continue.
ERROR: Username already exists.
ERROR: Invalid username format.
ERROR: Password too weak.
```

---

### `/login`
**Authenticate with the server.**

```
/login <username> <password>
```

**Responses:**
```
OK: Welcome back, alice!
ERROR: Invalid credentials.
ERROR: Account locked. Try again in 14 minutes.
```

On success: server internally assigns a session. All subsequent commands are executed as this user.

---

### `/msg`
**Send a private message to another user.**

```
/msg <target_username> <message text>
```

**Responses:**
```
OK: Message delivered.
ERROR: User 'bob' is not online.
ERROR: User 'bob' does not exist.
ERROR: Not authenticated. Please /login first.
```

---

### `/broadcast`
**Send a message to ALL connected clients.**

```
/broadcast <message text>
```

**Responses:**
```
OK: Broadcast sent to 7 clients.
ERROR: Not authenticated.
```

---

### `/list`
**List all currently online users.**

```
/list
```

**Response:**
```
ONLINE USERS (3):
  alice
  bob
  charlie
```

---

### `/whoami`
**Display current authenticated user.**

```
/whoami
```

**Responses:**
```
Authenticated as: alice (role: USER)
Not authenticated.
```

---

### `/quit`
**Gracefully disconnect from the server.**

```
/quit
```

**Response:**
```
Goodbye, alice. Connection closed.
```

Server will close the socket after sending this response.

---

## Error Codes

| Code | Meaning |
|------|---------|
| `ERROR: Unknown command` | Command not recognized |
| `ERROR: Not authenticated` | Must `/login` first |
| `ERROR: Missing arguments` | Required args not provided |
| `ERROR: User not found` | Target username doesn't exist |
| `ERROR: Server error` | Internal exception (logged server-side) |

---

## Protocol State Machine

```
[UNAUTHENTICATED]
     │
     ├─ /register → creates account → stays UNAUTHENTICATED
     ├─ /login    → success → [AUTHENTICATED]
     └─ any other command → ERROR: Not authenticated

[AUTHENTICATED]
     ├─ /msg, /broadcast, /list, /whoami → OK
     └─ /quit → socket closed → thread terminated
```

---

## Implementation Design — Command Pattern

```java
interface Command {
    void execute(ClientContext ctx, String[] args) throws CommandException;
    String getName();
    boolean requiresAuth();
}

class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public void register(Command cmd) {
        commands.put("/" + cmd.getName(), cmd);
    }

    public void dispatch(String input, ClientContext ctx) {
        String[] tokens = input.split(" ", 2);
        Command cmd = commands.get(tokens[0]);
        if (cmd == null) { ctx.send("ERROR: Unknown command"); return; }
        if (cmd.requiresAuth() && !ctx.isAuthenticated()) {
            ctx.send("ERROR: Not authenticated"); return;
        }
        cmd.execute(ctx, tokens.length > 1 ? tokens[1].split(" ") : new String[0]);
    }
}
```
