# sqlite1j

`sqlite1j` is a modernized Java rewrite target for SQLite 1.0 semantics, with full SQLite 1.0 parity targeted only after Phase 4 completion.

## Project goals

- Provide an in-memory SQL execution engine with SQLite 1.x-inspired behavior.
- Achieve SQLite 1.0 behavioral parity as the core project objective, explicitly gated on completing Phase 4.
- Preserve and validate historical semantics with parser, planner, executor, and storage tests.
- Support differential and golden-case testing to track behavior parity over time.

## Tech stack

- Java 15 (configured via Maven compiler release).
- Maven build.
- JUnit 5 for tests.

## Getting started

### Prerequisites

- JDK 15+
- Maven 3.8+

### Build

```bash
mvn clean package
```

### Run tests

```bash
mvn test
```

## Project structure

- `src/main/java/com/yourorg/sqlite1j/sql`: tokenizer, parser, and SQL statement models.
- `src/main/java/com/yourorg/sqlite1j/planner`: name binding and logical plan generation.
- `src/main/java/com/yourorg/sqlite1j/exec`: in-memory execution and expression evaluation.
- `src/main/java/com/yourorg/sqlite1j/storage`: pager, page cache, and B-Tree-related storage primitives.
- `src/main/java/com/yourorg/sqlite1j/testkit`: differential and golden test tooling.
- `src/test`: unit and integration tests, including golden corpora.

## Additional docs

- `FEATURE_SCOPE.md`
- `SEMANTICS.md`
- `SQL_SYNTAX.md`
- `PHASE1_RELEASE_CANDIDATE.md`
- `PHASE2_PLAN.md`
