# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 7/7 (100.0%)
- **Function parity:** 37/37 matched (target 208) — 100.0%
- **Class/type parity:** 23/23 matched (target 26) — 100.0%
- **Combined symbol parity:** 60/60 matched (target 234) — 100.0%
- **Average inline-code cosine:** 0.43 (function body across 7 matched files)
- **Average documentation cosine:** 0.44 (doc text across 7 matched files)
- **Cheat-zeroed Files:** 2
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

### 3. format

- **Target:** `quote.FormatIdent [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 4. runtime

- **Target:** `quote.Runtime`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 4005.4
- **Functions:** 23/23 matched (target 123)
- **Missing functions:** _none_
- **Types:** 17/17 matched
- **Missing types:** _none_

### 5. ext

- **Target:** `quote.Ext`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 901.7
- **Functions:** 7/7 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 6. spanned

- **Target:** `quote.Spanned`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 401.1
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 7. lib

- **Target:** `quote.Quote [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 29)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

