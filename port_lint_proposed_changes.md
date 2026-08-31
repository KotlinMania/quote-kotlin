# port-lint Proposed Changes

**Generated:** 2026-08-31
**Source:** tmp/quote/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/quote

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/quote/ToTokens.kt` | `// port-lint: source quote/src/to_tokens.rs` | `// port-lint: source to_tokens.rs` | `to_tokens.rs` | `port-lint provenance header matched only after fallback normalization: 'quote/src/to_tokens.rs' vs expected 'to_tokens.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/quote/IdentFragment.kt` | `// port-lint: source quote/src/ident_fragment.rs` | `// port-lint: source ident_fragment.rs` | `ident_fragment.rs` | `port-lint provenance header matched only after fallback normalization: 'quote/src/ident_fragment.rs' vs expected 'ident_fragment.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/quote/Runtime.kt` | `// port-lint: source quote/src/runtime.rs` | `// port-lint: source runtime.rs` | `runtime.rs` | `port-lint provenance header matched only after fallback normalization: 'quote/src/runtime.rs' vs expected 'runtime.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/quote/Spanned.kt` | `// port-lint: source quote/src/spanned.rs` | `// port-lint: source spanned.rs` | `spanned.rs` | `port-lint provenance header matched only after fallback normalization: 'quote/src/spanned.rs' vs expected 'spanned.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/quote/Ext.kt` | `// port-lint: source quote/src/ext.rs` | `// port-lint: source ext.rs` | `ext.rs` | `port-lint provenance header matched only after fallback normalization: 'quote/src/ext.rs' vs expected 'ext.rs'` |
