# Phase-1 Release Candidate (Day 30)

## Objective
Day 30 marks a Phase-1 release candidate for the Java SQLite 1.x learning rewrite. The goal is to freeze the currently supported SQL subset and validate behavior through deterministic tests.

## Included capabilities
- SQL tokenization and parsing for:
  - `CREATE TABLE`
  - `INSERT INTO ... VALUES`
  - `SELECT ... FROM ... [WHERE <col> <op> <literal>]`
  - transaction commands: `BEGIN`, `COMMIT`, `ROLLBACK`
- In-memory execution engine with table creation, insert, projection, and simple filtering.
- Baseline type support (`INTEGER`, `REAL`, `TEXT`, `NULL`) and coercion helpers.
- Error normalization and category mapping.
- Differential-test harness and golden corpus loader.

## Release criteria
A Phase-1 RC is considered acceptable when:
1. Core parser/tokenizer/planner/storage/txn/type/error tests are green.
2. Golden corpus loaders and analyzer tests are green.
3. Phase-1 RC smoke test passes end-to-end core flow + transaction guardrail behavior.
4. Performance baseline tests execute and produce baseline metrics (not hard performance SLOs).

## Deferred (post-Phase-1)
- Disk-persistent storage compatibility with on-disk SQLite format.
- SQL joins, aggregation, ordering, index selection, and query optimization.
- Concurrency beyond single-process in-memory simulation.
- Broader SQLite compatibility and fuzz/differential parity with native sqlite3.

## Notes
This RC is intentionally narrow: correctness and deterministic behavior for the frozen subset are prioritized over feature breadth.
