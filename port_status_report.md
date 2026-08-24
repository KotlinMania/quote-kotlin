# Code Port - Progress Report

**Generated:** 2026-08-24
**Source:** tmp/quote
**Target:** src

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 35/91 matched (target 205) | 38.5% |
| Class/type parity | 21/26 matched (target 24) | 80.8% |
| Combined symbol parity | 56/117 matched (target 229) | 47.9% |
| Average function body similarity | 0.43 | inline-code cosine |
| Average documentation similarity | 0.44 | doc text cosine |
| Missing source functions | 54 | 0% parity until ported |
| Missing source classes/types | 3 | 0% parity until ported |
| Missing source symbol files | 10 | 57 symbols |
| Cheat/scoring failures | 2 | forced to 0% |
| Total source files | 17 | 100% |
| Target units (paired) | 8 | - |
| Target files (total) | 8 | - |
| Porting progress | 7 | 41.2% (matched) |
| Missing files | 10 | 58.8% |

## Port Quality Analysis

**Average Function Similarity:** 0.43

Similarity in this report is the required function-by-function body/parameter score. Class/type parity and symbol deficits are reported beside it; whole-file shape is diagnostic only.

**Work Distribution:**
- Critical (<0.60): 5 files (71.4% of matched)
- Needs review (0.60-0.84): 1 files (14.3% of matched)

## Worst Function Scores First

Every matched file is listed from lowest function body/parameter similarity upward. Missing symbol names are not capped.

| Rank | Source | Target | Function similarity | Functions | Missing functions | Types | Missing types | Tests | Symbol deficit | Priority |
|------|--------|--------|---------------------|-----------|-------------------|-------|---------------|-------|----------------|----------|
| 1 | `format` | `quote.FormatIdent [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched (target 2) | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 2 | `lib` | `quote.Quote [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched (target 29) | _none_ | 0/0 matched (target 1) | _none_ | - | 0 | 10.0 |
| 3 | `ident_fragment` | `quote.IdentFragment [PROVENANCE-FALLBACK]` | 0.25 | 2/2 matched (target 11) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 1000307.4 |
| 4 | `runtime` | `quote.Runtime [PROVENANCE-FALLBACK]` | 0.42 | 21/23 matched (target 120) | `span`, `fmt` | 16/17 matched (target 16) | `IdentFragmentAdapter` | - | 3 | 34005.8 |
| 5 | `to_tokens` | `quote.ToTokens [PROVENANCE-FALLBACK]` | 0.58 | 3/3 matched (target 28) | _none_ | 1/1 matched | _none_ | - | 0 | 4000404.2 |
| 6 | `ext` | `quote.Ext [PROVENANCE-FALLBACK]` | 0.83 | 7/7 matched (target 11) | _none_ | 2/2 matched | _none_ | - | 0 | 901.7 |
| 7 | `spanned` | `quote.Spanned [PROVENANCE-FALLBACK]` | 0.89 | 2/2 matched (target 4) | _none_ | 1/2 matched (target 1) | `Sealed` | - | 1 | 10401.1 |

## Cheat Detection / Scoring Failures

- `format` -> `quote.FormatIdent [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `lib` -> `quote.Quote [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Quote.kt: Rust lifetime explanation in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `format` -> `quote.FormatIdent [ZERO] [PROVENANCE-FALLBACK]` (0.00, 1 deps)
- `lib` -> `quote.Quote [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `ident_fragment` -> `quote.IdentFragment [PROVENANCE-FALLBACK]` (0.25, 1 deps)
- `runtime` -> `quote.Runtime [PROVENANCE-FALLBACK]` (0.42)
- `to_tokens` -> `quote.ToTokens [PROVENANCE-FALLBACK]` (0.58, 4 deps)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| `runtime` | `quote.Runtime [PROVENANCE-FALLBACK]` | 1/17 | `IdentFragmentAdapter` |
| `spanned` | `quote.Spanned [PROVENANCE-FALLBACK]` | 1/2 | `Sealed` |

## High Priority Missing Files

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `tests.test` | `tests.Test` | 0 | 44 | 2 | 46 | `tests/test.rs` | `tests/Test.kt` |
| 2 | `build` | `Build` | 0 | 2 | 0 | 2 | `build.rs` | `Build.kt` |
| 3 | `ui.not-repeatable` | `tests.ui.Not-repeatable` | 0 | 1 | 1 | 2 | `tests/ui/not-repeatable.rs` | `tests/ui/Not-repeatable.kt` |
| 4 | `tests.compiletest` | `tests.Compiletest` | 0 | 1 | 0 | 1 | `tests/compiletest.rs` | `tests/Compiletest.kt` |
| 5 | `ui.does-not-have-iter` | `tests.ui.Does-not-have-iter` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter.rs` | `tests/ui/Does-not-have-iter.kt` |
| 6 | `ui.does-not-have-iter-interpolated` | `tests.ui.Does-not-have-iter-interpolated` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter-interpolated.rs` | `tests/ui/Does-not-have-iter-interpolated.kt` |
| 7 | `ui.does-not-have-iter-interpolated-dup` | `tests.ui.Does-not-have-iter-interpolated-dup` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter-interpolated-dup.rs` | `tests/ui/Does-not-have-iter-interpolated-dup.kt` |
| 8 | `ui.does-not-have-iter-separated` | `tests.ui.Does-not-have-iter-separated` | 0 | 1 | 0 | 1 | `tests/ui/does-not-have-iter-separated.rs` | `tests/ui/Does-not-have-iter-separated.kt` |
| 9 | `ui.not-quotable` | `tests.ui.Not-quotable` | 0 | 1 | 0 | 1 | `tests/ui/not-quotable.rs` | `tests/ui/Not-quotable.kt` |
| 10 | `ui.wrong-type-span` | `tests.ui.Wrong-type-span` | 0 | 1 | 0 | 1 | `tests/ui/wrong-type-span.rs` | `tests/ui/Wrong-type-span.kt` |

## Documentation Gaps

There is missing documentation that is hurting overall scoring.

**Documentation coverage:** 185 / 1490 lines (12%)

Documentation gaps (>20%), complete list:

- `lib` - 97% gap (1058 → 36 lines)
- `format` - 94% gap (218 → 14 lines)
- `to_tokens` - 74% gap (98 → 25 lines)
- `ext` - 58% gap (62 → 26 lines)

