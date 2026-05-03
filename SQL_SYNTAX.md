# Supported SQL Syntax (Current Scaffold)

This document lists SQL syntax currently recognized by the tokenizer/parser scaffold.

## Tokenizer-supported keywords
- `SELECT`
- `FROM`
- `WHERE`
- `ORDER`
- `BY`
- `LIMIT`
- `ASC`
- `DESC`
- `INNER`
- `JOIN`
- `ON`
- `INSERT`
- `INTO`
- `VALUES`
- `CREATE`
- `TABLE`
- `UPDATE`
- `SET`
- `DELETE`
- `BEGIN`
- `COMMIT`
- `ROLLBACK`
- `AS`

## Tokenizer-supported symbols
- `,`
- `;`
- `(`
- `)`
- `*`
- `.`
- `=`
- `<`
- `>`
- `<=`
- `>=`
- `!=`

## Tokenizer-supported literals
- Number literals (integer and decimal): e.g. `1`, `12.5`
- Single-quoted strings: e.g. `'abc'`

## Tokenizer-supported identifiers
- Starts with letter or underscore.
- May contain letters, digits, underscore.
- Qualified names are supported via `.` token composition in the parser (e.g. `u.id`).

## Parser-supported `SELECT` forms (current scope)
- Basic select:
  - `SELECT a, b FROM t WHERE a = 1;`
- Ordering/limit:
  - `SELECT a FROM t ORDER BY b DESC, a ASC LIMIT 10;`
- Aggregates:
  - `SELECT COUNT(*), COUNT(a), MIN(a), MAX(a) FROM t;`
- Inner join:
  - `SELECT u.name FROM users u INNER JOIN posts p ON u.id = p.user_id;`
  - `SELECT name FROM users JOIN posts ON id = user_id;`
- Scoped subquery in `FROM`:
  - `SELECT name FROM (SELECT name FROM users) u;`

## Parser-supported non-`SELECT` forms
- `INSERT INTO t VALUES ('abc', 12.5);`
- `CREATE TABLE users(id, name);`
- `UPDATE users SET name = 'alice' WHERE id = 1;`
- `DELETE FROM users WHERE id = 1;`
- `BEGIN; COMMIT; ROLLBACK;`

## Not yet supported (current scaffold limitations)
- Outer joins (`LEFT`, `RIGHT`, `FULL`)
- `GROUP BY`, `HAVING`, window functions
- Scalar subqueries in expressions (current subquery scope is `FROM` item only)
- Quoted identifiers (`"name"`, `[name]`, `` `name` ``)
- Escape handling inside string literals
- Operators/symbols beyond the list above (e.g. `+`, `-`, `/`)
- Full SQLite SQL grammar coverage

This list should be updated as parser/planner milestones are completed.
