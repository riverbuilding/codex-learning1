# Golden Test Format (v1)

Each test case file is JSON with deterministic fields:

- `name`: unique test identifier
- `script`: SQL script executed by both reference and candidate adapters
- `expected`: normalized expected output
  - `columns`: ordered column names
  - `rows`: ordered rows (stringified values)
  - `errorCategory`: nullable
  - `errorMessage`: nullable
  - `metadata`: optional map for deterministic tags

Example:
```json
{
  "name": "basic_select",
  "script": "SELECT 1 AS a;",
  "expected": {
    "columns": ["a"],
    "rows": [["1"]],
    "errorCategory": null,
    "errorMessage": null,
    "metadata": {"phase": "day4"}
  }
}
```
