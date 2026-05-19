# Security Documentation

**Project:** Distributed Multithreaded Secure Server Platform
**Version:** 0.2.0 | **Date:** 2026-05-18

---

## 1. Current Security Posture

**Threat Level: NONE (intentional at Level 2)**

The system currently has no authentication, no encryption, and no input validation. This is expected — security is systematically engineered at Level 8. However, the threat model is documented NOW so every future decision is made with full awareness of the attack surface.

---

## 2. Threat Model

### Actors

| Actor | Capability | Intent |
|-------|-----------|--------|
| External attacker | Network access to port 5000 | Data theft, disruption |
| Malicious client | Can send arbitrary byte sequences | Crash server, exfiltrate data |
| Insider threat | Legitimate credentials | Unauthorized access beyond role |

### Attack Surface (Current)

| Vector | Severity | Status |
|--------|----------|--------|
| Unauthenticated connection | CRITICAL | No auth exists |
| Plaintext communication | HIGH | No encryption |
| Unlimited connection rate | HIGH | No rate limiting |
| Arbitrary input accepted | HIGH | No input validation |
| Thread exhaustion (DoS) | HIGH | No connection limit |

---

## 3. Planned Security Controls

### Level 8 Security Implementation

#### 3.1 Password Hashing — BCrypt

**Never store plaintext passwords.**

```java
// Storing a password
String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));

// Verifying a password
boolean valid = BCrypt.checkpw(inputPassword, storedHash);
```

**Why BCrypt?**
- Deliberately slow (computational cost factor)
- Adaptive: cost factor increases as hardware gets faster
- Includes salt automatically — protects against rainbow tables
- Industry standard for password storage

---

#### 3.2 Session Tokens — JWT (JSON Web Tokens)

```
Header.Payload.Signature
eyJhbGci...  eyJ1c2Vy...  HMAC-SHA256-signature
```

**Token Claims:**
```json
{
  "sub": "user123",
  "role": "USER",
  "iat": 1716000000,
  "exp": 1716086400
}
```

**Validation flow:**
1. Client sends token in every request
2. Server verifies HMAC signature (prevents tampering)
3. Server checks `exp` claim (prevents replay with expired tokens)
4. Server checks `sub` exists in DB (prevents use of deleted user tokens)

---

#### 3.3 Transport Encryption — SSL/TLS

```java
SSLServerSocketFactory factory =
    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
SSLServerSocket serverSocket =
    (SSLServerSocket) factory.createServerSocket(5443);
```

**Why TLS?**
- Prevents man-in-the-middle (MITM) interception
- Encrypts all data in transit — credentials, messages, session tokens
- Required for any deployment beyond localhost

---

### 3.4 Input Validation (Level 5+)

All client input MUST be validated before processing:

```java
// Username: alphanumeric, 3-20 chars
if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
    return "ERROR: Invalid username format";
}

// Command injection prevention
// Use PreparedStatement, NEVER string concatenation in SQL
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM users WHERE username = ?"
);
stmt.setString(1, username); // safe — parameterized
```

---

#### 3.5 Rate Limiting (Level 8)

**Brute force login protection:**
```
After 5 failed logins from same IP → lock account for 15 minutes
After 3 lock periods → permanent IP block until admin review
```

---

## 4. Attack Scenarios & Mitigations

| Attack | Description | Mitigation | Level |
|--------|-------------|-----------|-------|
| Brute Force | Automated password guessing | Rate limit + account lockout | 8 |
| SQL Injection | Malicious SQL in input fields | `PreparedStatement` | 7 |
| Replay Attack | Reuse captured auth token | JWT expiry + nonce | 8 |
| MITM | Intercept plaintext traffic | SSL/TLS | 8 |
| DoS — Thread Flood | Connect thousands of times | Thread pool cap | 6 |
| DoS — Slow Loris | Hold connections idle forever | `SO_TIMEOUT` | 4 |
| Credential Exposure | DB breach reveals passwords | BCrypt hashing | 8 |

---

## 5. Security Principles to Enforce

1. **Zero Trust:** Every client is untrusted until authenticated. Every request requires token validation.
2. **Least Privilege:** Each user can only act within their role.
3. **Defense in Depth:** Multiple layers (TLS + JWT + BCrypt + rate limiting) — no single point of security failure.
4. **Fail Secure:** On any error, deny access. Never silently allow on exception.
5. **No Sensitive Logging:** NEVER log passwords, tokens, or PII — even in dev mode.
