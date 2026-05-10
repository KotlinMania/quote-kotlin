// port-lint: source src/ext.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

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
    for (token in iter) {
        token.toTokens(this)
    }
}

public fun <T : ToTokens, U : ToTokens> TokenStream.appendSeparated(iter: Iterable<T>, op: U) {
    var first = true
    for (token in iter) {
        if (!first) {
            op.toTokens(this)
        }
        first = false
        token.toTokens(this)
    }
}

public fun <T : ToTokens, U : ToTokens> TokenStream.appendTerminated(iter: Iterable<T>, term: U) {
    for (token in iter) {
        token.toTokens(this)
        term.toTokens(this)
    }
}
