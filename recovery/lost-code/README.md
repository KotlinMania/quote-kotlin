# quote-kotlin lost-code recovery

This directory records objects that were unreachable before the recovery pass on
2026-05-14.

## Inventory

- 8 unreachable commits are listed in `commit-map.tsv` and exported under
  `patches/`.
- 14 unreachable trees are listed in `unreachable-tree-map.tsv`.
- 3 loose blobs are listed in `blob-map.tsv` and exported under `blobs/`.

## Source review

The reachable `origin/main` state already contains the source work from the
port commits:

- `src/commonMain/kotlin/io/github/kotlinmania/quote/Ext.kt`
- `src/commonMain/kotlin/io/github/kotlinmania/quote/ToTokens.kt`
- `src/commonMain/kotlin/io/github/kotlinmania/quote/Spanned.kt`

Those files were byte-for-byte identical to the recovered commit
`74007579a23f38270781ece8d986e7146c848897`, so duplicate source rows were
removed from the tree map.

The remaining recovered differences are build, workflow, documentation, and
metadata variants. The current branch is based on `origin/main`, whose build and
CI files are newer than the recovered variants.
