# High Priority Ports - Action Plan

## Files by Impact

Priority = deps * 1,000,000 + SymDeficit * 10,000 + SrcSymbols * 100 + (1 - function similarity) * 10

Dependency fanout is ranked first so the ladder favors ports that clear downstream compilation failures fastest.

This list is complete and includes function/type detail for every matched file. Function similarity is the required body/parameter comparison; file-level shape does not rescue a port.

| Rank | Source | Target | Function similarity | Deps | Functions | Missing functions | Types | Missing types | SymDeficit | SrcSymbols | Priority |
|------|--------|--------|------------|------|-----------|-------------------|-------|---------------|-----------|------------|----------|
| 1 | `to_tokens` | `quote.ToTokens` | 0.58 | 4 | 3/3 matched (target 28) | _none_ | 1/1 matched | _none_ | 0 | 4 | 4000404.2 |
| 2 | `ident_fragment` | `quote.IdentFragment` | 0.25 | 1 | 2/2 matched (target 11) | _none_ | 1/1 matched (target 3) | _none_ | 0 | 3 | 1000307.4 |
| 3 | `format` | `quote.FormatIdent [ZERO]` | 0.00 | 1 | 0/0 matched (target 2) | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000010.0 |
| 4 | `runtime` | `quote.Runtime` | 0.46 | 0 | 23/23 matched (target 123) | _none_ | 17/17 matched | _none_ | 0 | 40 | 4005.4 |
| 5 | `ext` | `quote.Ext` | 0.83 | 0 | 7/7 matched (target 11) | _none_ | 2/2 matched | _none_ | 0 | 9 | 901.7 |
| 6 | `spanned` | `quote.Spanned` | 0.89 | 0 | 2/2 matched (target 4) | _none_ | 2/2 matched | _none_ | 0 | 4 | 401.1 |
| 7 | `lib` | `quote.Quote [ZERO]` | 0.00 | 0 | 0/0 matched (target 29) | _none_ | 0/0 matched (target 1) | _none_ | 0 | 0 | 10.0 |

## Cheat Detection / Scoring Failures

- `format` -> `quote.FormatIdent [ZERO]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `lib` -> `quote.Quote [ZERO]`: function-by-function score forced to 0. Quote.kt: Rust lifetime explanation in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only

## Critical Issues (Function Similarity < 0.60 with Dependencies)

These files need immediate attention:

- **to_tokens** → `quote.ToTokens`
  - Function similarity: 0.58
  - Dependencies: 4
  - Functions: 3/3 matched (target 28)
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_

- **ident_fragment** → `quote.IdentFragment`
  - Function similarity: 0.25
  - Dependencies: 1
  - Functions: 2/2 matched (target 11)
  - Missing functions: _none_
  - Types: 1/1 matched (target 3)
  - Missing types: _none_

- **format** → `quote.FormatIdent [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 0/0 matched (target 2)
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: no source functions found; target defines functions; report scoring is function-by-function only

## Missing Files (by Dependents)

No missing files detected.

