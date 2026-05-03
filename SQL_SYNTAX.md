# Supported SQL Syntax (Current Scaffold)

This document lists SQL syntax currently recognized by the tokenizer and parser implementation.

## Tokenizer-supported keywords
- `SELECT`, `FROM`, `WHERE`, `ORDER`, `BY`, `LIMIT`, `ASC`, `DESC`
- `INNER`, `JOIN`, `ON`
- `INSERT`, `INTO`, `VALUES`
- `CREATE`, `TABLE`
- `UPDATE`, `SET`
- `DELETE`
- `BEGIN`, `COMMIT`, `ROLLBACK`
- `AS`

## Tokenizer-supported symbols
- `,`, `;`, `(`, `)`, `*`, `.`
- `=`, `<`, `>`, `<=`, `>=`, `!=`

## Tokenizer-supported literals
- Number literals (integer and decimal): e.g. `1`, `12.5`
- Single-quoted strings: e.g. `'abc'`

## Identifier rules
- Identifiers start with letter or underscore.
- Identifiers may contain letters, digits, underscore.
- Qualified names are parsed via dot composition (e.g. `u.id`, `schema_like.token` style chains).

## Parser-supported statements

### `SELECT`
Supported shape:
- `SELECT <projection[, ...]> FROM <from_item> [join ...] [WHERE ...] [GROUP BY ...] [HAVING ...] [ORDER BY ...] [LIMIT n] [;]`

Supported projection forms:
- `*`
- identifier or dotted identifier (e.g. `id`, `u.id`)
- function-like projection with one argument: `NAME(*)` or `NAME(identifier)`
  - used by current tests for aggregates such as `COUNT(*)`, `COUNT(id)`, `MIN(age)`, `MAX(age)`

Supported `FROM` item forms:
- Table name: `FROM users`
- Table alias with `AS`: `FROM users AS u`
- Table alias without `AS`: `FROM users u`
- Derived table subquery: `FROM (SELECT ...) alias`
  - Note: current parser requires an alias token after `)`.

Supported join forms:
- `JOIN <from_item> ON <name> = <name>`
- `INNER JOIN <from_item> ON <name> = <name>`

Supported `WHERE` predicate form:
- Single comparison only: `<name> <op> <literal>`
- `<op>` in `{=, !=, <, <=, >, >=}`

Supported `GROUP BY` form:
- `GROUP BY <name> [, ...]`

Supported `HAVING` form:
- Single comparison only: `<projection_ref> <op> <literal>`
- In current execution, `<projection_ref>` must appear in the select projection list (e.g. `COUNT(*)`).

Supported `ORDER BY` form:
- `ORDER BY <name> [ASC|DESC] [, ...]`

Supported `LIMIT` form:
- `LIMIT <numeric-literal>` (non-negative integer expected)

### `INSERT`
- `INSERT INTO <table> VALUES (<literal[, ...]>) [;]`

### `UPDATE`
- `UPDATE <table> SET <name> = <literal> [, ...] [WHERE <name> <op> <literal>] [;]`

### `DELETE`
- `DELETE FROM <table> [WHERE <name> <op> <literal>] [;]`

### `CREATE TABLE`
- `CREATE TABLE <table> (<column> <type> [, ...]) [;]`

### Transaction control
- `BEGIN [;]`
- `COMMIT [;]`
- `ROLLBACK [;]`

## Not yet supported
- Outer joins (`LEFT`/`RIGHT`/`FULL`)
- Window functions
- Scalar subqueries in expressions (`WHERE col IN (SELECT ...)`, etc.)
- Join predicates other than equality
- Complex boolean predicates (`AND`, `OR`, parentheses)
- Quoted identifiers (`"name"`, `[name]`, `` `name` ``)
- Escaped quotes inside string literals
- Full SQLite grammar coverage
