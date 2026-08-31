# High Priority Ports - Action Plan

## Files by Impact

Priority = deps * 1,000,000 + SymDeficit * 10,000 + SrcSymbols * 100 + (1 - function similarity) * 10

Dependency fanout is ranked first so the ladder favors ports that clear downstream compilation failures fastest.

This list is complete and includes function/type detail for every matched file. Function similarity is the required body/parameter comparison; file-level shape does not rescue a port.

| Rank | Source | Target | Function similarity | Deps | Functions | Missing functions | Types | Missing types | SymDeficit | SrcSymbols | Priority |
|------|--------|--------|------------|------|-----------|-------------------|-------|---------------|-----------|------------|----------|
| 1 | `to_tokens` | `quote.ToTokens` | 0.58 | 4 | 3/3 matched (target 28) | _none_ | 1/1 matched | _none_ | 0 | 4 | 4000404.2 |
| 2 | `ident_fragment` | `quote.IdentFragment` | 0.25 | 1 | 2/2 matched (target 11) | _none_ | 1/1 matched (target 3) | _none_ | 0 | 3 | 1000307.4 |
| 3 | `runtime` | `quote.Runtime` | 0.42 | 0 | 21/23 matched (target 120) | `span`, `fmt` | 16/17 matched (target 16) | `IdentFragmentAdapter` | 3 | 40 | 34005.8 |
| 4 | `spanned` | `quote.Spanned` | 0.89 | 0 | 2/2 matched (target 4) | _none_ | 1/2 matched (target 1) | `Sealed` | 1 | 4 | 10401.1 |
| 5 | `ext` | `quote.Ext` | 0.83 | 0 | 7/7 matched (target 11) | _none_ | 2/2 matched | _none_ | 0 | 9 | 901.7 |

## Cheat Detection / Scoring Failures

_None detected._

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

## Missing Files (by Dependents)

No missing files detected.

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Matched

| Source | Target | Path |
|--------|--------|------|
| `format` | `quote.FormatIdent` | `format` |
| `lib` | `quote.Quote` | `lib` |

