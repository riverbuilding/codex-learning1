# Phase 4 Plan (Final) — SQLite 1.0 Parity Closure

## Purpose

Phase 4 is the final parity-closure phase after Phase 3. Its objective is to close the remaining feature and compatibility gaps so the project can claim practical SQLite 1.0 parity for the intended scope.

## Inputs

- Phase 1 established a deterministic core subset (`CREATE TABLE`, `INSERT`, `SELECT`, `BEGIN/COMMIT/ROLLBACK`).
- Phase 2 hardened execution boundary, error normalization, and predicate coverage.
- Phase 3 expands DML/query capabilities (`UPDATE`, `DELETE`, `ORDER BY`, `LIMIT`, scoped aggregates) and reliability/testing depth.
- Remaining parity gaps are explicitly documented in Phase 3 non-goals and earlier deferred lists.

## Final Remaining Gaps to Close

### 1) Relational Query Completeness

**Deliverables:**
- Implement scoped `JOIN` support (start with `INNER JOIN`, then compatibility-driven extensions).
- Implement scoped subquery support where required for SQLite 1.0 parity claims.

**Acceptance checks:**
- Joins/subqueries parse, bind, plan, and execute with deterministic outputs.
- Error paths (ambiguous names, unknown aliases/tables) map to normalized domain errors.

### 2) Aggregation Completion

**Deliverables:**
- Add `GROUP BY` and `HAVING` semantics.
- Validate interaction with Phase 3 aggregates (`COUNT`, `MIN`, `MAX`) and mixed expressions.

**Acceptance checks:**
- Grouping and post-aggregate filtering behave per documented contract.
- Unsupported combinations fail deterministically with normalized errors.

### 3) Index and Planner Parity Baseline

**Deliverables:**
- Add index DDL and secondary-index usage path for eligible predicates.
- Introduce planner heuristics sufficient to avoid full-table scans in basic indexed cases.

**Acceptance checks:**
- Indexed lookup behavior is correct and deterministic.
- Planner chooses valid index path for covered query patterns.

### 4) File/Disk Persistence Compatibility

**Deliverables:**
- Introduce file-backed persistence mode.
- Implement/validate SQLite on-disk format compatibility for the scoped feature surface.
- Add corruption/compatibility diagnostics with normalized error categories.

**Acceptance checks:**
- Data persists across process restarts.
- Scoped compatibility corpus validates read/write interoperability expectations.

### 5) Concurrency and Durability Envelope

**Deliverables:**
- Define and implement minimal concurrency semantics required for parity claim.
- Strengthen durability behavior within scoped guarantees.

**Acceptance checks:**
- Concurrency and durability behaviors are explicit, deterministic, and tested.
- Transaction visibility and rollback semantics remain contract-consistent under stress.

### 6) Parity Validation and Release Gate

**Deliverables:**
- Expand differential corpus for all newly added features and failure categories.
- Add explicit parity matrix mapping: “SQLite 1.0 feature/semantic clause → test IDs”.
- Freeze syntax/semantics docs at parity candidate.

**Acceptance checks (final gate):**
- All parity-matrix rows have executable passing coverage.
- No known category-A mismatches against scoped SQLite 1.0 behavior.
- CI runs full deterministic matrix cleanly in fresh environment.

## Suggested Implementation Order

1. Joins/subqueries foundation.
2. `GROUP BY` / `HAVING` completion.
3. Index + planner baseline.
4. File-backed persistence + scoped on-disk compatibility.
5. Concurrency/durability envelope.
6. Differential parity matrix + release freeze.

## Definition of Done (Final Phase)

Phase 4 is complete when:
- All remaining Phase 3 non-goal gaps required for SQLite 1.0 parity are implemented.
- Documentation (`SQL_SYNTAX.md`, `SEMANTICS.md`, scope docs) matches runtime behavior exactly.
- Differential and contract suites provide auditable parity traceability.
- The project can make a bounded, test-backed SQLite 1.0 parity claim for the declared scope.

## Explicit Non-goals (even at parity closure)

- Modern SQLite features beyond SQLite 1.0-era behavior.
- Non-deterministic optimization tactics that compromise reproducibility.
- Unbounded compatibility claims outside the documented parity matrix scope.
