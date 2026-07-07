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

// ---------------------------------------------------------------------------
// Punctuation push helpers — one pair per operator from the upstream
// push_punct macro. Each pair has an unsuffixed variant (call-site span)
// and a spanned variant.
// ---------------------------------------------------------------------------

private fun pushPunct1(tokens: TokenStream, ch: Char, span: Span) {
    tokens.append(TokenTree.Punct(Punct(ch, Spacing.Alone, span)))
}

private fun pushPunct2(tokens: TokenStream, ch1: Char, ch2: Char, span: Span) {
    tokens.append(TokenTree.Punct(Punct(ch1, Spacing.Joint, span)))
    tokens.append(TokenTree.Punct(Punct(ch2, Spacing.Alone, span)))
}

private fun pushPunct3(tokens: TokenStream, ch1: Char, ch2: Char, ch3: Char, span: Span) {
    tokens.append(TokenTree.Punct(Punct(ch1, Spacing.Joint, span)))
    tokens.append(TokenTree.Punct(Punct(ch2, Spacing.Joint, span)))
    tokens.append(TokenTree.Punct(Punct(ch3, Spacing.Alone, span)))
}

public fun pushAdd(tokens: TokenStream) { pushPunct1(tokens, '+', Span.callSite()) }
public fun pushAddSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '+', span) }
public fun pushAddEq(tokens: TokenStream) { pushPunct2(tokens, '+', '=', Span.callSite()) }
public fun pushAddEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '+', '=', span) }
public fun pushAnd(tokens: TokenStream) { pushPunct1(tokens, '&', Span.callSite()) }
public fun pushAndSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '&', span) }
public fun pushAndAnd(tokens: TokenStream) { pushPunct2(tokens, '&', '&', Span.callSite()) }
public fun pushAndAndSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '&', '&', span) }
public fun pushAndEq(tokens: TokenStream) { pushPunct2(tokens, '&', '=', Span.callSite()) }
public fun pushAndEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '&', '=', span) }
public fun pushAt(tokens: TokenStream) { pushPunct1(tokens, '@', Span.callSite()) }
public fun pushAtSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '@', span) }
public fun pushBang(tokens: TokenStream) { pushPunct1(tokens, '!', Span.callSite()) }
public fun pushBangSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '!', span) }
public fun pushCaret(tokens: TokenStream) { pushPunct1(tokens, '^', Span.callSite()) }
public fun pushCaretSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '^', span) }
public fun pushCaretEq(tokens: TokenStream) { pushPunct2(tokens, '^', '=', Span.callSite()) }
public fun pushCaretEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '^', '=', span) }
public fun pushColon(tokens: TokenStream) { pushPunct1(tokens, ':', Span.callSite()) }
public fun pushColonSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, ':', span) }
public fun pushColon2(tokens: TokenStream) { pushPunct2(tokens, ':', ':', Span.callSite()) }
public fun pushColon2Spanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, ':', ':', span) }
public fun pushComma(tokens: TokenStream) { pushPunct1(tokens, ',', Span.callSite()) }
public fun pushCommaSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, ',', span) }
public fun pushDiv(tokens: TokenStream) { pushPunct1(tokens, '/', Span.callSite()) }
public fun pushDivSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '/', span) }
public fun pushDivEq(tokens: TokenStream) { pushPunct2(tokens, '/', '=', Span.callSite()) }
public fun pushDivEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '/', '=', span) }
public fun pushDot(tokens: TokenStream) { pushPunct1(tokens, '.', Span.callSite()) }
public fun pushDotSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '.', span) }
public fun pushDot2(tokens: TokenStream) { pushPunct2(tokens, '.', '.', Span.callSite()) }
public fun pushDot2Spanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '.', '.', span) }
public fun pushDot3(tokens: TokenStream) { pushPunct3(tokens, '.', '.', '.', Span.callSite()) }
public fun pushDot3Spanned(tokens: TokenStream, span: Span) { pushPunct3(tokens, '.', '.', '.', span) }
public fun pushDotDotEq(tokens: TokenStream) { pushPunct3(tokens, '.', '.', '=', Span.callSite()) }
public fun pushDotDotEqSpanned(tokens: TokenStream, span: Span) { pushPunct3(tokens, '.', '.', '=', span) }
public fun pushEq(tokens: TokenStream) { pushPunct1(tokens, '=', Span.callSite()) }
public fun pushEqSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '=', span) }
public fun pushEqEq(tokens: TokenStream) { pushPunct2(tokens, '=', '=', Span.callSite()) }
public fun pushEqEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '=', '=', span) }
public fun pushGe(tokens: TokenStream) { pushPunct2(tokens, '>', '=', Span.callSite()) }
public fun pushGeSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '>', '=', span) }
public fun pushGt(tokens: TokenStream) { pushPunct1(tokens, '>', Span.callSite()) }
public fun pushGtSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '>', span) }
public fun pushLe(tokens: TokenStream) { pushPunct2(tokens, '<', '=', Span.callSite()) }
public fun pushLeSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '<', '=', span) }
public fun pushLt(tokens: TokenStream) { pushPunct1(tokens, '<', Span.callSite()) }
public fun pushLtSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '<', span) }
public fun pushMulEq(tokens: TokenStream) { pushPunct2(tokens, '*', '=', Span.callSite()) }
public fun pushMulEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '*', '=', span) }
public fun pushNe(tokens: TokenStream) { pushPunct2(tokens, '!', '=', Span.callSite()) }
public fun pushNeSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '!', '=', span) }
public fun pushOr(tokens: TokenStream) { pushPunct1(tokens, '|', Span.callSite()) }
public fun pushOrSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '|', span) }
public fun pushOrEq(tokens: TokenStream) { pushPunct2(tokens, '|', '=', Span.callSite()) }
public fun pushOrEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '|', '=', span) }
public fun pushOrOr(tokens: TokenStream) { pushPunct2(tokens, '|', '|', Span.callSite()) }
public fun pushOrOrSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '|', '|', span) }
public fun pushPound(tokens: TokenStream) { pushPunct1(tokens, '#', Span.callSite()) }
public fun pushPoundSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '#', span) }
public fun pushQuestion(tokens: TokenStream) { pushPunct1(tokens, '?', Span.callSite()) }
public fun pushQuestionSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '?', span) }
public fun pushRarrow(tokens: TokenStream) { pushPunct2(tokens, '-', '>', Span.callSite()) }
public fun pushRarrowSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '-', '>', span) }
public fun pushLarrow(tokens: TokenStream) { pushPunct2(tokens, '<', '-', Span.callSite()) }
public fun pushLarrowSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '<', '-', span) }
public fun pushRem(tokens: TokenStream) { pushPunct1(tokens, '%', Span.callSite()) }
public fun pushRemSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '%', span) }
public fun pushRemEq(tokens: TokenStream) { pushPunct2(tokens, '%', '=', Span.callSite()) }
public fun pushRemEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '%', '=', span) }
public fun pushFatArrow(tokens: TokenStream) { pushPunct2(tokens, '=', '>', Span.callSite()) }
public fun pushFatArrowSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '=', '>', span) }
public fun pushSemi(tokens: TokenStream) { pushPunct1(tokens, ';', Span.callSite()) }
public fun pushSemiSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, ';', span) }
public fun pushShl(tokens: TokenStream) { pushPunct2(tokens, '<', '<', Span.callSite()) }
public fun pushShlSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '<', '<', span) }
public fun pushShlEq(tokens: TokenStream) { pushPunct3(tokens, '<', '<', '=', Span.callSite()) }
public fun pushShlEqSpanned(tokens: TokenStream, span: Span) { pushPunct3(tokens, '<', '<', '=', span) }
public fun pushShr(tokens: TokenStream) { pushPunct2(tokens, '>', '>', Span.callSite()) }
public fun pushShrSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '>', '>', span) }
public fun pushShrEq(tokens: TokenStream) { pushPunct3(tokens, '>', '>', '=', Span.callSite()) }
public fun pushShrEqSpanned(tokens: TokenStream, span: Span) { pushPunct3(tokens, '>', '>', '=', span) }
public fun pushStar(tokens: TokenStream) { pushPunct1(tokens, '*', Span.callSite()) }
public fun pushStarSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '*', span) }
public fun pushSub(tokens: TokenStream) { pushPunct1(tokens, '-', Span.callSite()) }
public fun pushSubSpanned(tokens: TokenStream, span: Span) { pushPunct1(tokens, '-', span) }
public fun pushSubEq(tokens: TokenStream) { pushPunct2(tokens, '-', '=', Span.callSite()) }
public fun pushSubEqSpanned(tokens: TokenStream, span: Span) { pushPunct2(tokens, '-', '=', span) }

// ---------------------------------------------------------------------------
// RepInterp — helper wrapper for repeated interpolation bindings.
// ---------------------------------------------------------------------------

/**
 * Helper wrapper used within repetitions to allow repeated binding names.
 * Wraps a value so that it can be treated as both iterable and a single
 * value during repetition expansion.
 */
public class RepInterp<T>(public val value: T)

/**
 * Return the wrapped value, mirroring the single-element iterator pattern.
 */
public fun <T> RepInterp<T>.next(): T = value