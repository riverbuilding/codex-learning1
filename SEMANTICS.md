# SQLite 1.0 to Java Rewrite — Semantics Contract v1

## Purpose
This document defines externally observable behavior that the Java rewrite must preserve for the Phase 1 SQL subset.

## 1) NULL Semantics
- `NULL` compared with any value using `=` or `!=` yields unknown/null-like predicate outcome.
- `IS NULL` and `IS NOT NULL` are the supported null-checking predicates.
- Arithmetic with `NULL` yields `NULL`.

## 2) Type Affinity and Coercion
- Runtime values are represented as: `NULL`, integer, real, text, blob.
- Numeric coercion is attempted for arithmetic operators when operands are text that can be parsed numerically.
- Non-coercible text in arithmetic operations results in a deterministic error for Phase 1.

## 3) Transaction Semantics
- `BEGIN` opens a transaction scope.
- `COMMIT` atomically makes all in-transaction changes visible.
- `ROLLBACK` discards all in-transaction changes.
- Statements outside explicit transactions execute in auto-commit mode.

## 4) Query Result Semantics
- For scoped `SELECT` without `ORDER BY`, row order is implementation-defined.
- Projection and filter semantics must match the reference baseline behavior for supported expressions.
- `ORDER BY` is applied before `LIMIT`.
- `ORDER BY` uses stable sorting; ties preserve original scan/insertion order.
- `NULL` values sort last for both ascending and descending ordering in this phase.

## 4.1) Row Mutation Semantics (Phase 3)
- `UPDATE <table> SET ... [WHERE ...]` mutates every row matching `WHERE`; without `WHERE`, all rows are candidates.
- `DELETE FROM <table> [WHERE ...]` removes every row matching `WHERE`; without `WHERE`, all rows are candidates.
- `WHERE` predicate evaluation and literal coercion in mutation paths are identical to `SELECT`.
- A deterministic affected-row count is recorded as the number of rows matched by the mutation predicate.

## 5) Error Categories
- Parse error: invalid syntax.
- Schema error: unknown table/column, invalid arity.
- Constraint error: future phase extension (reserved category).
- Transaction error: invalid state transitions (e.g., commit without active transaction).
- Storage/IO error: pager/file failures.

## 6) Day 2 Exit Criteria
- Semantics contract documented and versioned.
- Maven project scaffold added and build lifecycle validated (`validate` phase).
