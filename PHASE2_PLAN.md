# Phase 2 Plan

## Purpose

Phase 2 builds on the frozen Phase 1 release candidate by improving execution API cohesion, error consistency, operator coverage, and semantics verification while keeping scope intentionally incremental.

## Baseline (from Phase 1 RC)

- Implemented SQL subset: `CREATE TABLE`, `INSERT ... VALUES`, `SELECT ... FROM ... [WHERE ...]`, `BEGIN|COMMIT|ROLLBACK`.
- Deterministic-correctness milestone is prioritized over broad SQL feature breadth.
- Parser/tokenizer support is intentionally constrained.

## Phase 2 Deliverables

### 1) Unified Statement Execution Surface

**Goal:** all statement kinds execute through one top-level dispatcher.

**Plan:**
- Ensure all SQL statement types implement a common `Statement` abstraction.
- Provide `InMemoryDatabase.executeStatement(Statement)` as the boundary API.
- Route transaction control statements through transaction lifecycle methods.
- Keep typed `execute(...)` methods for compatibility during transition.

**Acceptance checks:**
- Dispatcher executes create/insert/select/transaction statements.
- Unsupported statement subtype fails with deterministic domain error.

### 2) Error Boundary Normalization

**Goal:** remove raw exception leakage from user-facing boundaries.

**Plan:**
- Normalize parse, schema, and transaction-state failures into stable domain categories.
- Standardize short, machine-comparable error message formats.
- Apply normalization consistently at parse+execute boundaries.

**Acceptance checks:**
- Nested `BEGIN` yields transaction-category error.
- Unknown table yields schema-category error.
- Malformed SQL yields parse-category error.
- Insert arity mismatch yields normalized execution error.

### 3) Operator & Predicate Expansion

**Goal:** add `!=`, `<=`, `>=` end-to-end support.

**Plan:**
- Extend tokenizer symbol handling for `!`, `<=`, `>=`, `!=`.
- Extend parser comparison-operator handling for new operators.
- Extend expression evaluator semantics for numeric/text comparisons using existing coercion behavior.
- Update `SQL_SYNTAX.md` to reflect supported operators.

**Acceptance checks:**
- Parser accepts all new operators in `WHERE`.
- Execution filtering works for numeric and text columns.
- Malformed operators fail with normalized parse errors.

### 4) Semantics Contract Testization

**Goal:** turn documented semantics into executable contract tests.

**Plan:**
- Add/expand a semantics contract suite mapped to `SEMANTICS.md`.
- Cover currently claimed behavior for NULL handling, coercion, and transaction visibility/rollback.
- Mark deferred behavior explicitly in tests to prevent contract drift.

**Acceptance checks:**
- Every currently claimed Phase-1 semantic clause has at least one executable assertion.
- Contract test names map clearly to semantics sections.

### 5) Differential Harness Expansion

**Goal:** expand golden corpus for new operators + normalized errors.

**Plan:**
- Add corpus cases for `!=`, `<=`, `>=` behavior.
- Add corpus cases for transaction-state and parse/schema error parity.
- Extend loader/runner coverage for new corpus folders.

**Acceptance checks:**
- New corpus cases are discovered and executed by loader/runner.
- Differential mismatch output remains clear and actionable.

### 6) Build/CI Reliability Hardening

**Goal:** improve reproducibility in environments with intermittent Maven/plugin resolution issues.

**Plan:**
- Pin and/or adjust plugin/repository configuration in `pom.xml`.
- Add CI staging: fast parser/executor smoke tests first, full suite second.
- Document any approved mirror/fallback strategy.

**Acceptance checks:**
- Fresh-environment build path succeeds with pinned configuration.
- CI runs fast checks before full matrix.

## Suggested Implementation Order

1. Unified statement execution API foundation.
2. Error normalization boundary hardening.
3. Operator expansion end-to-end.
4. Semantics contract test coverage.
5. Differential corpus expansion.
6. CI/build reliability hardening (parallelizable if separate owner).

## Risks and Mitigations

- **Contract drift (docs vs runtime):** tie semantics claims directly to executable tests.
- **API mismatch (parser outputs vs runtime entrypoint):** enforce single statement dispatcher.
- **Build instability:** prioritize plugin/repository determinism early.

## Definition of Done (Phase 2 kickoff)

- Unified statement dispatch exists and is used in tests.
- `!=`, `<=`, `>=` work tokenize → parse → evaluate.
- Execution boundary emits normalized error categories.
- Semantics docs are covered by executable tests for all currently claimed behavior.
- Differential corpus includes positive and negative cases for new behavior.
