# Tests Directory

Unit, integration, and stress tests live here. Populated starting at Level 4.

## Planned Structure

```
tests/
 ├── unit/
 │   ├── CommandParserTest.java       ← Level 5
 │   ├── AuthServiceTest.java         ← Level 8
 │   └── MessageServiceTest.java      ← Level 7
 ├── integration/
 │   ├── ServerClientTest.java        ← Level 4
 │   └── MultiClientTest.java         ← Level 6
 └── LoadTest.java                    ← Level 6 concurrent lifecycle stress test
```

See [docs/testing/TESTING.md](../docs/testing/TESTING.md) for full test case documentation.
