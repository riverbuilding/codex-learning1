# SQLite 1.0 to Java Rewrite — Phase 1 Feature Scope

## Objective
Establish the Day 1 scope freeze for a modernized Java rewrite that preserves SQLite 1.0-era semantics for a limited, testable SQL subset.

## In Scope (Phase 1)
- Basic schema creation:
  - `CREATE TABLE <name>(<col defs...>)`
- Basic data writes:
  - `INSERT INTO <table> VALUES (...)`
- Basic reads:
  - `SELECT <projection> FROM <table> [WHERE <predicate>]`
- Transaction controls:
  - `BEGIN`, `COMMIT`, `ROLLBACK`
- Storage fundamentals:
  - Fixed page abstraction
  - Minimal pager read/write
  - Minimal B-tree search/insert needed by the SQL subset

## Out of Scope (Phase 1)
- Index DDL and secondary indexes
- Joins and subqueries
- Views, triggers, and virtual tables
- Advanced SQL expressions and aggregate/window functions
- Full compatibility with modern SQLite features introduced after 1.0-era design
- Query optimizer beyond simple scan/filter/project planning

## Compatibility Target
- Preserve external behavior for the scoped subset:
  - Equivalent query results
  - Equivalent transaction visibility semantics
  - Equivalent failure classes for invalid statements within scope
- Internal implementation is intentionally modernized and idiomatic Java.

## Exit Criteria for Day 1
- Scope documented and frozen.
- Java package skeleton created for all planned subsystems.
- Non-goals explicitly listed to avoid scope creep.
