// port-lint: source ext.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

internal interface Sealed

/**
 * TokenStream extension trait with methods for appending tokens.
 *
 * This trait is sealed and cannot be implemented outside of the `quote`
 * package.
 */
public interface TokenStreamExt {
    /**
     * For use by `ToTokens` implementations.
     *
     * Appends the token specified to this list of tokens.
     */
    public fun append(token: TokenTree)

    /**
     * For use by `ToTokens` implementations.
     */
    public fun <T : ToTokens> appendAll(iter: Iterable<T>)

    /**
     * For use by `ToTokens` implementations.
     *
     * Appends all of the items in the iterator `I`, separated by the tokens
     * `U`.
     */
    public fun <T : ToTokens, U : ToTokens> appendSeparated(iter: Iterable<T>, op: U)

    /**
     * For use by `ToTokens` implementations.
     *
     * Appends all tokens in the iterator `I`, appending `U` after each
     * element, including after the last element of the iterator.
     */
    public fun <T : ToTokens, U : ToTokens> appendTerminated(iter: Iterable<T>, term: U)
}

public fun TokenStream.append(token: TokenTree) {
    extendTokenTrees(listOf(token))
}

public fun TokenStream.append(token: Group) {
    append(TokenTree.Group(token))
}

public fun TokenStream.append(token: Ident) {
    append(TokenTree.Ident(token))
}

public fun TokenStream.append(token: Punct) {
    append(TokenTree.Punct(token))
}

public fun TokenStream.append(token: Literal) {
    append(TokenTree.Literal(token))
}

public fun <T : ToTokens> TokenStream.appendAll(iter: Iterable<T>) {
    doAppendAll(this, iter.iterator())
}

internal fun <T : ToTokens> doAppendAll(stream: TokenStream, iter: Iterator<T>) {
    for (token in iter) {
        token.toTokens(stream)
    }
}

public fun <T : ToTokens, U : ToTokens> TokenStream.appendSeparated(iter: Iterable<T>, op: U) {
    doAppendSeparated(this, iter.iterator(), op)
}

internal fun <T : ToTokens, U : ToTokens> doAppendSeparated(
    stream: TokenStream,
    iter: Iterator<T>,
    op: U,
) {
    var first = true
    for (token in iter) {
        if (!first) {
            op.toTokens(stream)
        }
        first = false
        token.toTokens(stream)
    }
}

public fun <T : ToTokens, U : ToTokens> TokenStream.appendTerminated(iter: Iterable<T>, term: U) {
    doAppendTerminated(this, iter.iterator(), term)
}

internal fun <T : ToTokens, U : ToTokens> doAppendTerminated(
    stream: TokenStream,
    iter: Iterator<T>,
    term: U,
) {
    for (token in iter) {
        token.toTokens(stream)
        term.toTokens(stream)
    }
}
