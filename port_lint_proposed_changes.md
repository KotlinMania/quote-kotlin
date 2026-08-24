# port-lint Proposed Changes

**Generated:** 2026-08-24
**Source:** tmp/quote
**Target:** src

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `commonMain/kotlin/io/github/kotlinmania/quote/ToTokens.kt` | `// port-lint: source to_tokens.rs` | `// port-lint: source to_tokens.rs` | `to_tokens.rs` | `port-lint provenance header matched only after fallback normalization: 'to_tokens.rs' vs expected 'to_tokens.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/quote/IdentFragment.kt` | `// port-lint: source ident_fragment.rs` | `// port-lint: source ident_fragment.rs` | `ident_fragment.rs` | `port-lint provenance header matched only after fallback normalization: 'ident_fragment.rs' vs expected 'ident_fragment.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/quote/FormatIdent.kt` | `// port-lint: source format.rs` | `// port-lint: source format.rs` | `format.rs` | `port-lint provenance header matched only after fallback normalization: 'format.rs' vs expected 'format.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/quote/Runtime.kt` | `// port-lint: source runtime.rs` | `// port-lint: source runtime.rs` | `runtime.rs` | `port-lint provenance header matched only after fallback normalization: 'runtime.rs' vs expected 'runtime.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/quote/Spanned.kt` | `// port-lint: source spanned.rs` | `// port-lint: source spanned.rs` | `spanned.rs` | `port-lint provenance header matched only after fallback normalization: 'spanned.rs' vs expected 'spanned.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/quote/Ext.kt` | `// port-lint: source ext.rs` | `// port-lint: source ext.rs` | `ext.rs` | `port-lint provenance header matched only after fallback normalization: 'ext.rs' vs expected 'ext.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/quote/Quote.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
