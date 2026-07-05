// port-lint: source runtime.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.Spacing
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

/**
 * Runtime support functions for the quote system.
 *
 * These are the Kotlin analogues of the functions in Rust's
 * `quote::__private` (runtime.rs). They are used by [quote] and
 * [quoteSpanned] internally, and may be used directly by code that
 * needs fine-grained control over token emission.
 */

/**
 * Push a group with the given delimiter and inner stream onto [tokens].
 */
public fun pushGroup(tokens: TokenStream, delimiter: Delimiter, inner: TokenStream) {
    val group = Group(delimiter, inner)
    tokens.append(TokenTree.Group(group))
}

/**
 * Push a spanned group onto [tokens].
 */
public fun pushGroupSpanned(
    tokens: TokenStream,
    span: Span,
    delimiter: Delimiter,
    inner: TokenStream,
) {
    val group = Group(delimiter, inner)
    group.setSpan(span)
    tokens.append(TokenTree.Group(group))
}

/**
 * Parse a string as a token stream and extend [tokens] with the result.
 */
public fun pushParse(tokens: TokenStream, s: String) {
    val result = TokenStream.fromString(s)
    if (result.isSuccess()) {
        tokens.extendTokenStreams(listOf(result.getOrThrow()))
    } else {
        throw IllegalArgumentException("invalid token stream: $s")
    }
}

/**
 * Parse a string as a token stream, respan all tokens with [span],
 * and extend [tokens] with the result.
 */
public fun pushParseSpanned(tokens: TokenStream, span: Span, s: String) {
    val result = TokenStream.fromString(s)
    if (!result.isSuccess()) {
        throw IllegalArgumentException("invalid token stream: $s")
    }
    for (token in result.getOrThrow()) {
        tokens.append(respanTokenTree(token, span))
    }
}

/**
 * Replace every span in a token tree with the given span.
 */
public fun respanTokenTree(token: TokenTree, span: Span): TokenTree {
    return when (token) {
        is TokenTree.Group -> {
            val group = token.value
            val inner = TokenStream.new()
            for (innerToken in group.stream()) {
                inner.append(respanTokenTree(innerToken, span))
            }
            val newGroup = Group(group.delimiter(), inner)
            newGroup.setSpan(span)
            TokenTree.Group(newGroup)
        }
        else -> {
            token.setSpan(span)
            token
        }
    }
}

/**
 * Push an identifier onto [tokens] with [Span.callSite].
 */
public fun pushIdent(tokens: TokenStream, s: String) {
    pushIdentSpanned(tokens, Span.callSite(), s)
}

/**
 * Push a spanned identifier onto [tokens].
 */
public fun pushIdentSpanned(tokens: TokenStream, span: Span, s: String) {
    tokens.append(TokenTree.Ident(mkIdent(s, span)))
}

/**
 * Push a lifetime onto [tokens].
 */
public fun pushLifetime(tokens: TokenStream, lifetime: String) {
    val apostrophe = Punct('\'', Spacing.Joint, Span.callSite())
    tokens.append(TokenTree.Punct(apostrophe))
    tokens.append(TokenTree.Ident(mkIdent(lifetime.substring(1), Span.callSite())))
}

/**
 * Push a spanned lifetime onto [tokens].
 */
public fun pushLifetimeSpanned(tokens: TokenStream, span: Span, lifetime: String) {
    val apostrophe = Punct('\'', Spacing.Joint, span)
    tokens.append(TokenTree.Punct(apostrophe))
    tokens.append(TokenTree.Ident(mkIdent(lifetime.substring(1), span)))
}

/**
 * Push an underscore identifier onto [tokens].
 */
public fun pushUnderscore(tokens: TokenStream) {
    pushUnderscoreSpanned(tokens, Span.callSite())
}

/**
 * Push a spanned underscore identifier onto [tokens].
 */
public fun pushUnderscoreSpanned(tokens: TokenStream, span: Span) {
    tokens.append(TokenTree.Ident(Ident.new("_", span)))
}