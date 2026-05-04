# SQLite 1.0 Parity Matrix (Candidate Freeze)

Freeze date: **2026-05-04**

| Clause ID | SQLite 1.0 feature/semantic clause | Test IDs |
|---|---|---|
| P-001 | `CREATE INDEX idx ON t(c)` parses and executes for existing column | `ParserMutationTest#parsesCreateIndex`, `InMemoryDatabaseTest#supportsDeterministicIndexedLookup` |
| P-002 | Equality predicate on indexed column returns deterministic rows | `InMemoryDatabaseTest#supportsDeterministicIndexedLookup` |
| P-003 | Planner may choose index path for covered equality predicate | `LogicalPlannerTest#prefersIndexScanForEqualityPredicateOnIndexedColumn` |
| P-004 | Durable persistence survives restart for scoped types | `FileBackedDatabaseTest#persistsDataAcrossRestart` |
| P-005 | Corrupt/unsupported persisted file normalizes to STORAGE_IO category | `FileBackedDatabaseTest#reportsCompatibilityDiagnosticsForCorruptFile` |
| P-006 | Concurrent statement execution on one DB instance is deterministic (serialized envelope) | `ConcurrencyDurabilityEnvelopeTest#serializedStatementExecutionIsDeterministicUnderWriteStress` |
| P-007 | Rollback visibility remains contract-consistent under concurrent mutation stress and restart | `ConcurrencyDurabilityEnvelopeTest#rollbackAndDurableRestartRemainConsistentAfterConcurrentMutations` |
| P-008 | Differential corpus includes coverage for index DDL/path and transaction error categories | `GoldenCorpusDay36Test#loadsParityExpansionCorpusForIndexedAndTxnCases` |
