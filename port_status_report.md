# Code Port - Progress Report

**Generated:** 2026-08-31
**Source:** tmp
**Target:** src/commonMain/kotlin/io/github/kotlinmania/quote

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 35/91 matched (target 174) | 38.5% |
| Class/type parity | 21/26 matched (target 23) | 80.8% |
| Combined symbol parity | 56/117 matched (target 197) | 47.9% |
| Average function body similarity | 0.50 | inline-code cosine |
| Average documentation similarity | 0.43 | doc text cosine |
| Missing source functions | 54 | 0% parity until ported |
| Missing source classes/types | 3 | 0% parity until ported |
| Missing source symbol files | 10 | 57 symbols |
| Cheat/scoring failures | 0 | forced to 0% |
| Total source files | 17 | 100% |
| Target units (paired) | 10 | - |
| Target files (total) | 10 | - |
| Porting progress | 7 | 41.2% (matched) |
| Missing files | 10 | 58.8% |

## Port Quality Analysis

**Average Function Similarity:** 0.50

Similarity in this report is the required function-by-function body/parameter score. Class/type parity and symbol deficits are reported beside it; whole-file shape is diagnostic only.

**Work Distribution:**
- Critical (<0.60): 5 files (71.4% of matched)
- Needs review (0.60-0.84): 1 files (14.3% of matched)

## Worst Function Scores First

Every matched file is listed from lowest function body/parameter similarity upward. Missing symbol names are not capped.

| Rank | Source | Target | Function similarity | Functions | Missing functions | Types | Missing types | Tests | Symbol deficit | Priority |
|------|--------|--------|---------------------|-----------|-------------------|-------|---------------|-------|----------------|----------|
| 1 | `quote.ident_fragment` | `quote.IdentFragment` | 0.25 | 2/2 matched (target 11) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 1000307.4 |
| 2 | `quote.runtime` | `quote.Runtime` | 0.42 | 21/23 matched (target 120) | `span`, `fmt` | 16/17 matched (target 16) | `IdentFragmentAdapter` | - | 3 | 34005.8 |
| 3 | `quote.to_tokens` | `quote.ToTokens` | 0.58 | 3/3 matched (target 28) | _none_ | 1/1 matched | _none_ | - | 0 | 4000404.2 |
| 4 | `quote.ext` | `quote.Ext` | 0.83 | 7/7 matched (target 11) | _none_ | 2/2 matched | _none_ | - | 0 | 901.7 |
| 5 | `quote.spanned` | `quote.Spanned` | 0.89 | 2/2 matched (target 4) | _none_ | 1/2 matched (target 1) | `Sealed` | - | 1 | 10401.1 |

## Cheat Detection / Scoring Failures

_None detected._

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `quote.ident_fragment` -> `quote.IdentFragment` (0.25, 1 deps)
- `quote.runtime` -> `quote.Runtime` (0.42)
- `quote.to_tokens` -> `quote.ToTokens` (0.58, 4 deps)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| `quote.runtime` | `quote.Runtime` | 1/17 | `IdentFragmentAdapter` |
| `quote.spanned` | `quote.Spanned` | 1/2 | `Sealed` |

## High Priority Missing Files

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `tests.test` | `quote.tests.Test` | 0 | 44 | 2 | 46 | `quote/tests/test.rs` | `quote/tests/Test.kt` |
| 2 | `quote.build` | `quote.Build` | 0 | 2 | 0 | 2 | `quote/build.rs` | `quote/Build.kt` |
| 3 | `ui.not-repeatable` | `quote.tests.ui.Not-repeatable` | 0 | 1 | 1 | 2 | `quote/tests/ui/not-repeatable.rs` | `quote/tests/ui/Not-repeatable.kt` |
| 4 | `tests.compiletest` | `quote.tests.Compiletest` | 0 | 1 | 0 | 1 | `quote/tests/compiletest.rs` | `quote/tests/Compiletest.kt` |
| 5 | `ui.does-not-have-iter` | `quote.tests.ui.Does-not-have-iter` | 0 | 1 | 0 | 1 | `quote/tests/ui/does-not-have-iter.rs` | `quote/tests/ui/Does-not-have-iter.kt` |
| 6 | `ui.does-not-have-iter-interpolated` | `quote.tests.ui.Does-not-have-iter-interpolated` | 0 | 1 | 0 | 1 | `quote/tests/ui/does-not-have-iter-interpolated.rs` | `quote/tests/ui/Does-not-have-iter-interpolated.kt` |
| 7 | `ui.does-not-have-iter-interpolated-dup` | `quote.tests.ui.Does-not-have-iter-interpolated-dup` | 0 | 1 | 0 | 1 | `quote/tests/ui/does-not-have-iter-interpolated-dup.rs` | `quote/tests/ui/Does-not-have-iter-interpolated-dup.kt` |
| 8 | `ui.does-not-have-iter-separated` | `quote.tests.ui.Does-not-have-iter-separated` | 0 | 1 | 0 | 1 | `quote/tests/ui/does-not-have-iter-separated.rs` | `quote/tests/ui/Does-not-have-iter-separated.kt` |
| 9 | `ui.not-quotable` | `quote.tests.ui.Not-quotable` | 0 | 1 | 0 | 1 | `quote/tests/ui/not-quotable.rs` | `quote/tests/ui/Not-quotable.kt` |
| 10 | `ui.wrong-type-span` | `quote.tests.ui.Wrong-type-span` | 0 | 1 | 0 | 1 | `quote/tests/ui/wrong-type-span.rs` | `quote/tests/ui/Wrong-type-span.kt` |

## Documentation Gaps

There is missing documentation that is hurting overall scoring.

**Documentation coverage:** 185 / 1490 lines (12%)

Documentation gaps (>20%), complete list:

- `quote.lib` - 97% gap (1058 → 36 lines)
- `quote.format` - 94% gap (218 → 14 lines)
- `quote.to_tokens` - 74% gap (98 → 25 lines)
- `quote.ext` - 58% gap (62 → 26 lines)

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Matched

| Source | Target | Path |
|--------|--------|------|
| `quote.format` | `quote.FormatIdent` | `quote/src/format` |
| `quote.lib` | `quote.Quote` | `quote/src/lib` |

