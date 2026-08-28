# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 7/7 (100.0%)
- **Function parity:** 35/37 matched (target 174) — 94.6%
- **Class/type parity:** 21/23 matched (target 23) — 91.3%
- **Combined symbol parity:** 56/60 matched (target 197) — 93.3%
- **Average inline-code cosine:** 0.43 (function body across 7 matched files)
- **Average documentation cosine:** 0.44 (doc text across 7 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 5 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. to_tokens

- **Target:** `quote.ToTokens`
- **Similarity:** 0.58
- **Dependents:** 4
- **Priority Score:** 4000404.2
- **Functions:** 3/3 matched (target 28)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 2. ident_fragment

- **Target:** `quote.IdentFragment`
- **Similarity:** 0.25
- **Dependents:** 1
- **Priority Score:** 1000307.4
- **Functions:** 2/2 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 3. runtime

- **Target:** `quote.Runtime`
- **Similarity:** 0.42
- **Dependents:** 0
- **Priority Score:** 34005.8
- **Functions:** 21/23 matched (target 120)
- **Missing functions:** `span`, `fmt`
- **Types:** 16/17 matched (target 16)
- **Missing types:** `IdentFragmentAdapter`

### 4. spanned

- **Target:** `quote.Spanned`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 10401.1
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Sealed`

### 5. ext

- **Target:** `quote.Ext`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 901.7
- **Functions:** 7/7 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

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

