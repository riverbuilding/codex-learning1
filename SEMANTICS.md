# SQLite 1.0 to Java Rewrite — Semantics Contract v1

## Purpose
This document defines externally observable behavior currently implemented for the phase-scoped SQL subset.

## 1) NULL Semantics
- NULL handling is deterministic in current evaluator paths.
- Current parser subset does not include `IS NULL` / `IS NOT NULL` syntax.

## 2) Type and Comparison Semantics
- Runtime values are represented as integer, real, text, and null-like values.
- Literal parsing supports integer/real/text.
- Comparison behavior is engine-local and deterministic for supported operators.

## 3) Transaction Semantics
- `BEGIN` starts an explicit transaction.
- `COMMIT` persists in-transaction changes.
- `ROLLBACK` restores pre-transaction state snapshot.
- Nested `BEGIN` is rejected as transaction-state error.

## 4) Query Result Semantics
- `GROUP BY` partitions filtered rows into deterministic groups by the group-key tuple.
- `HAVING` is evaluated after per-group aggregate/projection calculation.
- `ORDER BY` is applied before `LIMIT` for non-grouped queries.
- Sort is stable; ties preserve insertion/scan order.
- Aggregates supported in select projections: `COUNT(*)`, `COUNT(expr)`, `MIN(expr)`, `MAX(expr)`.
- Mixing aggregate and non-aggregate projections without grouping is rejected.
- With `GROUP BY`, non-aggregate projections must be present in the `GROUP BY` list; unsupported combinations fail deterministically.

## 4.1) Relational Scope Semantics (JOIN/Subquery/Alias)
- `JOIN` and `INNER JOIN` are treated as inner joins.
- Join execution is deterministic nested-loop over scoped row sets.
- Scoped `FROM` subqueries are supported as derived tables.
- Subquery `FROM` items must have an alias in current parser behavior.
- Table aliases are supported with `AS` (`FROM t AS x`) and without `AS` (`FROM t x`).
- Qualified names (e.g. `u.id`) are supported in projections, join predicates, where predicates, and order terms.
- Unqualified names that resolve to multiple scoped columns are rejected as ambiguous.
- Unknown scoped names (column/alias resolution failures) are rejected deterministically.

## 4.2) Mutation Semantics
- `UPDATE` and `DELETE` apply row-wise against optional single-clause WHERE predicates.
- A deterministic affected-row count is tracked for mutation statements.

## 5) Error Category Intent
- Parse errors: malformed syntax/tokens for supported grammar.
- Schema errors: unknown table/column, ambiguous column resolution, scoped-name resolution failures, arity mismatches.
- Transaction errors: invalid transaction-state transitions.

## 6) Executable Contract Mapping (current)
- Transaction behavior: `TransactionStateMachineTest.*`, `InMemoryDatabaseTransactionTest.*`.
- Query shaping and aggregates: `SemanticsContractTest.section4_*`, `InMemoryDatabaseTest.*`.
- Relational scope behavior: `RelationalQueryCompletenessTest.*`.
- Error normalization: `ErrorParityTest.*`, `ErrorNormalizationIntegrationTest.*`.
# Parity Candidate Freeze

This semantics contract is frozen for SQLite 1.0 scoped parity candidate as of **2026-05-04**.
