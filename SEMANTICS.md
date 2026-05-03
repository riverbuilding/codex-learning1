# SQLite 1.0 to Java Rewrite — Semantics Contract v1

## Purpose
This document defines externally observable behavior that the Java rewrite must preserve for the Phase 1 SQL subset.

## 1) NULL Semantics
- `NULL` compared with any value using `=` or `!=` yields unknown/null-like predicate outcome.
- `IS NULL` and `IS NOT NULL` are not part of the currently implemented parser subset.
- Arithmetic with `NULL` yields `NULL` (reserved for expression-phase expansion).

## 2) Type Affinity and Coercion
- Runtime values are represented as: `NULL`, integer, real, text, blob.
- Literal parsing supports integer, real, text.
- Comparisons in current execution follow deterministic engine-local comparison rules.

## 3) Transaction Semantics
- `BEGIN` opens a transaction scope.
- `COMMIT` atomically makes all in-transaction changes visible.
- `ROLLBACK` discards all in-transaction changes.
- Statements outside explicit transactions execute in auto-commit mode.
- Nested transactions are rejected deterministically (`BEGIN` while active is a transaction-state error).
- Savepoint commands (`SAVEPOINT`, `RELEASE`, `ROLLBACK TO`) are intentionally not supported in this phase.

## 4) Query Result Semantics
- For scoped `SELECT` without `ORDER BY`, row order is implementation-defined.
- `ORDER BY` is applied before `LIMIT`.
- `ORDER BY` uses stable sorting; ties preserve original scan/insertion order.
- `NULL` values sort last for both ascending and descending ordering in this phase.
- Aggregates supported in this phase: `COUNT(*)`, `COUNT(expr)`, `MIN(expr)`, `MAX(expr)`.
- `COUNT(*)` counts all filtered rows; `COUNT(expr)` counts only non-NULL expression values.
- `MIN(expr)`/`MAX(expr)` ignore NULL values; when all values are NULL (or input is empty), result is `NULL`.
- Mixing aggregate and non-aggregate projections without grouping is rejected with a deterministic error.

## 4.1) Relational Scope Semantics (JOIN/Subquery)
- `INNER JOIN` and `JOIN` are supported as inner joins only.
- Join execution is deterministic nested-loop inner join over the scoped row sets.
- Subqueries are supported in `FROM` when aliased (derived table scope).
- Qualified column references (e.g. `u.id`) are supported in projections, predicates, ordering, and join predicates.
- Unqualified references that resolve to multiple scoped columns are rejected deterministically as ambiguous-column schema errors.
- Unknown columns/aliases in scoped resolution are rejected deterministically as schema errors.

## 4.2) Row Mutation Semantics (Phase 3)
- `UPDATE <table> SET ... [WHERE ...]` mutates every row matching `WHERE`; without `WHERE`, all rows are candidates.
- `DELETE FROM <table> [WHERE ...]` removes every row matching `WHERE`; without `WHERE`, all rows are candidates.
- A deterministic affected-row count is recorded as the number of rows matched by the mutation predicate.

## 5) Error Categories
- Parse error: invalid syntax.
- Schema error: unknown table/column, invalid arity, ambiguous column resolution in scoped queries.
- Constraint error: future phase extension (reserved category).
- Transaction error: invalid state transitions (e.g., commit without active transaction).
- Storage/IO error: pager/file failures.

## 6) Executable Contract Mapping
- Section 3 (transactions + rollback visibility): `SemanticsContractTest.section3_*`, `InMemoryDatabaseTransactionTest.*`, `TransactionStateMachineTest.*`.
- Section 4 (query shaping + aggregates): `SemanticsContractTest.section4_*`, `ParserSelectTest.parsesOrderByAndLimit`, `InMemoryDatabaseTest.supportsOrderByAndLimitWithDeterministicTieBreak`.
- Section 4.1 (joins/subqueries + scoped name resolution): `RelationalQueryCompletenessTest.*`.
- Section 4.2 (row mutation semantics): `SemanticsContractTest.section4_1_*`, `InMemoryDatabaseMutationTest.*`, `ParserMutationTest.*`.
- Section 5 (error categories): `ErrorNormalizationIntegrationTest.*`, `ErrorParityTest.*`.
