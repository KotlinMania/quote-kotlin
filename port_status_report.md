# Code Port - Progress Report

**Generated:** 2026-08-28
**Source:** tmp/quote/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/quote

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 35/37 matched (target 205) | 94.6% |
| Class/type parity | 21/23 matched (target 24) | 91.3% |
| Combined symbol parity | 56/60 matched (target 229) | 93.3% |
| Average function body similarity | 0.43 | inline-code cosine |
| Average documentation similarity | 0.44 | doc text cosine |
| Missing source functions | 0 | 0% parity until ported |
| Missing source classes/types | 0 | 0% parity until ported |
| Missing source symbol files | 0 | 0 symbols |
| Cheat/scoring failures | 2 | forced to 0% |
| Total source files | 7 | 100% |
| Target units (paired) | 10 | - |
| Target files (total) | 10 | - |
| Porting progress | 7 | 100.0% (matched) |
| Missing files | 0 | 0.0% |

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
| 1 | `format` | `quote.FormatIdent [ZERO]` | 0.00 | 0/0 matched (target 2) | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 2 | `lib` | `quote.Quote [ZERO]` | 0.00 | 0/0 matched (target 29) | _none_ | 0/0 matched (target 1) | _none_ | - | 0 | 10.0 |
| 3 | `ident_fragment` | `quote.IdentFragment` | 0.25 | 2/2 matched (target 11) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 1000307.4 |
| 4 | `runtime` | `quote.Runtime` | 0.42 | 21/23 matched (target 120) | `span`, `fmt` | 16/17 matched (target 16) | `IdentFragmentAdapter` | - | 3 | 34005.8 |
| 5 | `to_tokens` | `quote.ToTokens` | 0.58 | 3/3 matched (target 28) | _none_ | 1/1 matched | _none_ | - | 0 | 4000404.2 |
| 6 | `ext` | `quote.Ext` | 0.83 | 7/7 matched (target 11) | _none_ | 2/2 matched | _none_ | - | 0 | 901.7 |
| 7 | `spanned` | `quote.Spanned` | 0.89 | 2/2 matched (target 4) | _none_ | 1/2 matched (target 1) | `Sealed` | - | 1 | 10401.1 |

## Cheat Detection / Scoring Failures

- `format` -> `quote.FormatIdent [ZERO]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `lib` -> `quote.Quote [ZERO]`: function-by-function score forced to 0. Quote.kt: Rust lifetime explanation in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `format` -> `quote.FormatIdent [ZERO]` (0.00, 1 deps)
- `lib` -> `quote.Quote [ZERO]` (0.00)
- `ident_fragment` -> `quote.IdentFragment` (0.25, 1 deps)
- `runtime` -> `quote.Runtime` (0.42)
- `to_tokens` -> `quote.ToTokens` (0.58, 4 deps)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| `runtime` | `quote.Runtime` | 1/17 | `IdentFragmentAdapter` |
| `spanned` | `quote.Spanned` | 1/2 | `Sealed` |

## High Priority Missing Files

No missing files detected.

## Documentation Gaps

There is missing documentation that is hurting overall scoring.

**Documentation coverage:** 185 / 1490 lines (12%)

Documentation gaps (>20%), complete list:

- `lib` - 97% gap (1058 → 36 lines)
- `format` - 94% gap (218 → 14 lines)
- `to_tokens` - 74% gap (98 → 25 lines)
- `ext` - 58% gap (62 → 26 lines)

