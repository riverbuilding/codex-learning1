# Supported SQL Syntax (Current Scaffold)

This document lists SQL syntax currently recognized by the tokenizer/parser scaffold.

## Tokenizer-supported keywords
- `SELECT`
- `FROM`
- `WHERE`
- `INSERT`
- `INTO`
- `VALUES`
- `CREATE`
- `TABLE`
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
- `=`
- `<`
- `>`

## Tokenizer-supported literals
- Number literals (integer and decimal): e.g. `1`, `12.5`
- Single-quoted strings: e.g. `'abc'`

## Tokenizer-supported identifiers
- Starts with letter or underscore.
- May contain letters, digits, underscore.

## Example supported statements (tokenization-level)
- `SELECT a, b FROM t WHERE a = 1;`
- `INSERT INTO t VALUES ('abc', 12.5);`
- `CREATE TABLE users(id, name);`
- `BEGIN; COMMIT;`

## Not yet supported (current scaffold limitations)
- Quoted identifiers (`"name"`, `[name]`, `` `name` ``)
- Escape handling inside string literals
- Operators/symbols beyond the list above (e.g. `+`, `-`, `/`, `!=`, `<=`, `>=`)
- Full parser/AST coverage for all SQL grammar forms

This list should be updated as parser/planner milestones are completed.
