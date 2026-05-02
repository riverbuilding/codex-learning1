# Phase 3 Plan

## Purpose

Phase 3 expands beyond the incremental hardening work of Phase 2 and moves the project toward a practical **SQLite 1.0-style feature envelope** while preserving the project’s core principles:
- deterministic behavior,
- explicit error categories,
- and test-first compatibility.

This phase should still avoid modern SQLite-only features and remain intentionally constrained to functionality that can be validated against 1.0-era expectations.

## Inputs from Phase 1 and Phase 2

### Phase 1 baseline (frozen)
- Core subset: `CREATE TABLE`, `INSERT ... VALUES`, `SELECT ... FROM ... [WHERE ...]`, and `BEGIN|COMMIT|ROLLBACK`.
- Deterministic correctness over broad SQL surface.
- In-memory architecture with foundational pager/B-tree abstractions.

### Phase 2 baseline (expanded)
- Unified statement execution surface.
- Normalized error boundaries for parse/schema/transaction failures.
- Extended predicate operators (`!=`, `<=`, `>=`).
- Semantics contract coverage + stronger differential harness practices.

Phase 3 should **inherit all Phase 2 guarantees** and add carefully scoped SQL and storage/runtime capabilities.

## Phase 3 Feature Goals (SQLite 1.0-aligned expansion)

### 1) DML Completion: `UPDATE` and `DELETE`

**Goal:** complete essential row mutation operations expected in early SQL engines.

**Plan:**
- Add parser AST support for:
  - `UPDATE <table> SET <col>=<expr>[, ...] [WHERE ...]`
  - `DELETE FROM <table> [WHERE ...]`
- Add planner nodes and execution paths for row mutation.
- Reuse existing predicate evaluation and coercion behavior in `WHERE`.
- Ensure mutation count/affected-row behavior is deterministic and documented.

**Acceptance checks:**
- `UPDATE` applies deterministic column assignment order.
- `DELETE` supports full-table and filtered deletion.
- Unknown columns/tables and malformed set clauses yield normalized domain errors.

---

### 2) Query Expressiveness: `ORDER BY` + `LIMIT`

**Goal:** support basic result shaping frequently required by downstream users/tests.

**Plan:**
- Extend parser for `ORDER BY <col> [ASC|DESC] [, ...]` and `LIMIT <n>`.
- Add logical plan nodes (`SortNode`, `LimitNode`) layered after filtering/projection.
- Implement stable in-memory sort behavior with explicit NULL ordering policy.
- Document deterministic tie-breaking behavior (or explicitly mark as undefined if intentionally deferred).

**Acceptance checks:**
- Single and multi-column ordering pass deterministic tests.
- `LIMIT` applies after ordering.
- Invalid order columns and malformed limits produce normalized parse/schema errors.

---

### 3) Lightweight Aggregation: `COUNT`, `MIN`, `MAX`

**Goal:** provide high-value analytics primitives while avoiding full modern aggregation complexity.

**Plan:**
- Add aggregate function recognition in parser for scoped set: `COUNT(*)`, `COUNT(expr)`, `MIN(expr)`, `MAX(expr)`.
- Introduce aggregate execution node with simple whole-result aggregation (no `GROUP BY` in initial cut unless stretch goal).
- Define NULL-handling and coercion semantics explicitly in `SEMANTICS.md`.
- Guard unsupported mixes (aggregate + non-aggregate columns without grouping) with deterministic errors.

**Acceptance checks:**
- Aggregate results are deterministic and typed consistently.
- NULL handling matches documented contract.
- Unsupported aggregate patterns fail with normalized execution/planner errors.

---

### 4) Transaction Semantics Hardening: Savepoint-lite Strategy

**Goal:** deepen transactional realism without full concurrent engine complexity.

**Plan:**
- Introduce a scoped transactional enhancement:
  - either nested transaction rejection with stricter diagnostics,
  - or minimal savepoint-like checkpoints (`SAVEPOINT`/`RELEASE`/`ROLLBACK TO`) if feasible.
- Align behavior with documented SQLite 1.0 compatibility stance.
- Expand state-machine tests for all legal/illegal transitions.

**Acceptance checks:**
- Transaction lifecycle behavior is explicit and deterministic.
- Illegal state transitions always map to transaction-category errors.
- Rollback visibility guarantees are verified by contract tests.

---

### 5) Storage/Pager Reliability Milestone

**Goal:** make the existing in-memory pager/B-tree layer behavior robust enough for larger corpus and mutation-heavy workloads (without introducing new file/disk persistence).

**Plan:**
- Strengthen page lifecycle invariants (allocation, reuse, bounds checks, serialization checks).
- Add crash-safety simulation hooks for unit tests (logical, not OS-level durability claims).
- Expand B-tree/table storage tests for update/delete-heavy sequences.
- Keep SQLite on-disk file format compatibility and new file-backed persistence explicitly out of scope unless separately approved.

**Acceptance checks:**
- No data-loss/consistency regressions in mutation-heavy deterministic tests.
- Page codec and pager tests cover corruption/invalid header detection paths.
- Existing Phase 1/2 storage tests remain green.

---

### 6) Differential + Semantics Corpus v2

**Goal:** upgrade compatibility confidence as SQL surface expands.

**Plan:**
- Add golden corpus day folder for Phase 3 features (e.g., `day35`/`day40` batching convention).
- Include positive + negative cases for `UPDATE`, `DELETE`, `ORDER BY`, `LIMIT`, and aggregates.
- Add explicit expected error-category assertions for unsupported combinations.
- Extend `SEMANTICS.md` with section-to-test mapping for every new behavior.

**Acceptance checks:**
- Loader discovers new corpus folder without custom per-day wiring.
- Differential mismatches identify row/content/order/category deltas clearly.
- Every new semantics clause has at least one executable contract test.

## Suggested Implementation Order

1. `UPDATE` / `DELETE` parser + execution.
2. `ORDER BY` / `LIMIT` parser + execution.
3. Aggregate primitives (`COUNT`, `MIN`, `MAX`).
4. Transaction semantics hardening.
5. Storage/pager reliability expansion.
6. Differential corpus v2 + semantics finalization.

## Definition of Done (Phase 3)

Phase 3 is complete when all of the following are true:
- New SQL features (`UPDATE`, `DELETE`, `ORDER BY`, `LIMIT`, basic aggregates) are end-to-end implemented and documented.
- Error normalization remains consistent across new parser/planner/executor paths.
- Transaction and storage reliability tests cover newly introduced mutation/query behaviors.
- Differential corpus includes Phase 3 positive/negative compatibility cases.
- `SEMANTICS.md` is updated with executable contract traceability for all added behavior.

## Non-goals (still deferred)

- Joins and subqueries.
- Full `GROUP BY`/`HAVING` semantics beyond scoped aggregates.
- Secondary index selection/advanced optimizer rules.
- Full modern SQLite feature parity (post-1.0 additions).
- New file/disk-based storage implementation and SQLite file-format compatibility work.
- Multi-process concurrency and production durability guarantees.

## Risks and Mitigations

- **Scope creep toward modern SQLite parity:** enforce explicit Phase 3 non-goals and acceptance gates.
- **Semantics ambiguity (NULL/type coercion/order stability):** require docs + executable tests in same change set.
- **Mutation-path regressions:** prioritize update/delete property-style tests and differential corpus negatives.
- **Execution pipeline complexity growth:** keep planner nodes composable and statement dispatch centralized.
