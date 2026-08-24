# High Priority Ports - Action Plan

## Files by Impact

Priority = deps * 1,000,000 + SymDeficit * 10,000 + SrcSymbols * 100 + (1 - function similarity) * 10

Dependency fanout is ranked first so the ladder favors ports that clear downstream compilation failures fastest.

This list is complete and includes function/type detail for every matched file. Function similarity is the required body/parameter comparison; file-level shape does not rescue a port.

| Rank | Source | Target | Function similarity | Deps | Functions | Missing functions | Types | Missing types | SymDeficit | SrcSymbols | Priority |
|------|--------|--------|------------|------|-----------|-------------------|-------|---------------|-----------|------------|----------|
| 1 | `to_tokens` | `quote.ToTokens [PROVENANCE-FALLBACK]` | 0.58 | 4 | 3/3 matched (target 28) | _none_ | 1/1 matched | _none_ | 0 | 4 | 4000404.2 |
| 2 | `ident_fragment` | `quote.IdentFragment [PROVENANCE-FALLBACK]` | 0.25 | 1 | 2/2 matched (target 11) | _none_ | 1/1 matched (target 3) | _none_ | 0 | 3 | 1000307.4 |
| 3 | `format` | `quote.FormatIdent [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 1 | 0/0 matched (target 2) | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000010.0 |
| 4 | `runtime` | `quote.Runtime [PROVENANCE-FALLBACK]` | 0.42 | 0 | 21/23 matched (target 120) | `span`, `fmt` | 16/17 matched (target 16) | `IdentFragmentAdapter` | 3 | 40 | 34005.8 |
| 5 | `spanned` | `quote.Spanned [PROVENANCE-FALLBACK]` | 0.89 | 0 | 2/2 matched (target 4) | _none_ | 1/2 matched (target 1) | `Sealed` | 1 | 4 | 10401.1 |
| 6 | `ext` | `quote.Ext [PROVENANCE-FALLBACK]` | 0.83 | 0 | 7/7 matched (target 11) | _none_ | 2/2 matched | _none_ | 0 | 9 | 901.7 |
| 7 | `lib` | `quote.Quote [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0 | 0/0 matched (target 29) | _none_ | 0/0 matched (target 1) | _none_ | 0 | 0 | 10.0 |

## Cheat Detection / Scoring Failures

- `format` -> `quote.FormatIdent [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `lib` -> `quote.Quote [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Quote.kt: Rust lifetime explanation in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only

## Critical Issues (Function Similarity < 0.60 with Dependencies)

These files need immediate attention:

- **to_tokens** → `quote.ToTokens [PROVENANCE-FALLBACK]`
  - Function similarity: 0.58
  - Dependencies: 4
  - Functions: 3/3 matched (target 28)
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Lint issues: 1

- **ident_fragment** → `quote.IdentFragment [PROVENANCE-FALLBACK]`
  - Function similarity: 0.25
  - Dependencies: 1
  - Functions: 2/2 matched (target 11)
  - Missing functions: _none_
  - Types: 1/1 matched (target 3)
  - Missing types: _none_
  - Lint issues: 1

- **format** → `quote.FormatIdent [ZERO] [PROVENANCE-FALLBACK]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 0/0 matched (target 2)
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: no source functions found; target defines functions; report scoring is function-by-function only
  - Lint issues: 1

## Missing Files (by Dependents)

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `build` | `Build` | 0 | 2 | 0 | 2 | `build.rs` | `Build.kt` |
| 2 | `tests.compiletest` | `tests.Compiletest` | 0 | 1 | 0 | 1 | `tests/compiletest.rs` | `tests/Compiletest.kt` |
| 3 | `tests.test` | `tests.Test` | 0 | 44 | 2 | 46 | `tests/test.rs` | `tests/Test.kt` |
| 4 | `ui.does-not-have-iter` | `tests.ui.Does-not-have-iter` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter.rs` | `tests/ui/Does-not-have-iter.kt` |
| 5 | `ui.does-not-have-iter-interpolated` | `tests.ui.Does-not-have-iter-interpolated` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter-interpolated.rs` | `tests/ui/Does-not-have-iter-interpolated.kt` |
| 6 | `ui.does-not-have-iter-interpolated-dup` | `tests.ui.Does-not-have-iter-interpolated-dup` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter-interpolated-dup.rs` | `tests/ui/Does-not-have-iter-interpolated-dup.kt` |
| 7 | `ui.does-not-have-iter-separated` | `tests.ui.Does-not-have-iter-separated` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter-separated.rs` | `tests/ui/Does-not-have-iter-separated.kt` |
| 8 | `ui.not-quotable` | `tests.ui.Not-quotable` | 0 | 1 | 0 | 1 | `tests/ui/not-quotable.rs` | `tests/ui/Not-quotable.kt` |
| 9 | `ui.not-repeatable` | `tests.ui.Not-repeatable` | 0 | 1 | 1 | 2 | `tests/ui/not-repeatable.rs` | `tests/ui/Not-repeatable.kt` |
| 10 | `ui.wrong-type-span` | `tests.ui.Wrong-type-span` | 0 | 1 | 0 | 1 | `tests/ui/wrong-type-span.rs` | `tests/ui/Wrong-type-span.kt` |

