// port-lint: source src/to_tokens.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

/**
 * Types that can be interpolated inside a `quote` invocation.
 */
public interface ToTokens {
    /**
     * Write this value to the given `TokenStream`.
     *
     * The token append methods provided by the `TokenStreamExt` extension
     * trait may be useful for implementing `ToTokens`.
     */
    public fun toTokens(tokens: TokenStream)

    /**
     * Convert this value directly into a `TokenStream` object.
     *
     * This method is implicitly implemented using `toTokens`, and acts as a
     * convenience method for consumers of the `ToTokens` trait.
     */
    public fun toTokenStream(): TokenStream {
        val tokens = TokenStream.new()
        toTokens(tokens)
        return tokens
    }

    /**
     * Convert this value directly into a `TokenStream` object.
     *
     * This method is implicitly implemented using `toTokens`, and acts as a
     * convenience method for consumers of the `ToTokens` trait.
     */
    public fun intoTokenStream(): TokenStream =
        toTokenStream()
}

public fun ToTokens?.toTokens(tokens: TokenStream) {
    this?.toTokens(tokens)
}

public fun String.toTokens(tokens: TokenStream) {
    tokens.append(Literal.string(this))
}

public fun Byte.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i8Suffixed(this))
}

public fun Short.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i16Suffixed(this))
}

public fun Int.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i32Suffixed(this))
}

public fun Long.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i64Suffixed(this))
}

public fun UByte.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u8Suffixed(this))
}

public fun UShort.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u16Suffixed(this))
}

public fun UInt.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u32Suffixed(this))
}

public fun ULong.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u64Suffixed(this))
}

public fun Float.toTokens(tokens: TokenStream) {
    tokens.append(Literal.f32Suffixed(this))
}

public fun Double.toTokens(tokens: TokenStream) {
    tokens.append(Literal.f64Suffixed(this))
}

public fun Char.toTokens(tokens: TokenStream) {
    tokens.append(Literal.character(this))
}

public fun Boolean.toTokens(tokens: TokenStream) {
    val word = if (this) "true" else "false"
    tokens.append(Ident.new(word, Span.callSite()))
}

public fun ByteArray.toCStringTokens(tokens: TokenStream) {
    tokens.append(Literal.cString(this))
}

public fun Group.toTokens(tokens: TokenStream) {
    tokens.append(this)
}

public fun Ident.toTokens(tokens: TokenStream) {
    tokens.append(this)
}

public fun Punct.toTokens(tokens: TokenStream) {
    tokens.append(this)
}

public fun Literal.toTokens(tokens: TokenStream) {
    tokens.append(this)
}

public fun TokenTree.toTokens(tokens: TokenStream) {
    tokens.append(this)
}

public fun TokenStream.toTokens(tokens: TokenStream) {
    tokens.extendTokenStreams(listOf(this))
}

public fun TokenStream.intoTokenStream(): TokenStream =
    this
