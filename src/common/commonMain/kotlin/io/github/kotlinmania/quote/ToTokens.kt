// port-lint: source to_tokens.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

/**
 * Types that can be interpolated inside a [quote] invocation.
 */
public interface ToTokens {
    /**
     * Write this value to the given [TokenStream].
     *
     * The token append methods provided by [TokenStreamExt] may be useful for
     * implementing [ToTokens].
     *
     * ### Example
     *
     * Example implementation for a type representing segmented paths:
     *
     * ```kotlin
     * class Path(
     *     val global: Boolean,
     *     val segments: List<PathSegment>,
     * ) : ToTokens {
     *     override fun toTokens(tokens: TokenStream) {
     *         for ((i, segment) in segments.withIndex()) {
     *             if (i > 0 || global) {
     *                 tokens.append(Punct(':', Spacing.Joint, Span.callSite()))
     *                 tokens.append(Punct(':', Spacing.Alone, Span.callSite()))
     *             }
     *             segment.toTokens(tokens)
     *         }
     *     }
     * }
     * ```
     */
    public fun toTokens(tokens: TokenStream)

    /**
     * Convert this value directly into a [TokenStream] object.
     *
     * This method is implicitly implemented using [toTokens], and acts as a
     * convenience method for consumers of the [ToTokens] interface.
     */
    public fun toTokenStream(): TokenStream {
        val tokens = TokenStream.new()
        toTokens(tokens)
        return tokens
    }

    /**
     * Convert this value directly into a [TokenStream] object.
     *
     * This method is implicitly implemented using [toTokens], and acts as a
     * convenience method for consumers of the [ToTokens] interface.
     */
    public fun intoTokenStream(): TokenStream =
        toTokenStream()
}

/** Appends this optional [ToTokens] value to [tokens] when non-null. */
public fun ToTokens?.toTokens(tokens: TokenStream) {
    this?.toTokens(tokens)
}

/** Appends this [String] as a string [Literal] to [tokens]. */
public fun String.toTokens(tokens: TokenStream) {
    tokens.append(Literal.string(this))
}

/** Appends this [Byte] as a suffixed `i8` [Literal] to [tokens]. */
public fun Byte.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i8Suffixed(this))
}

/** Appends this [Short] as a suffixed `i16` [Literal] to [tokens]. */
public fun Short.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i16Suffixed(this))
}

/** Appends this [Int] as a suffixed `i32` [Literal] to [tokens]. */
public fun Int.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i32Suffixed(this))
}

/** Appends this [Long] as a suffixed `i64` [Literal] to [tokens]. */
public fun Long.toTokens(tokens: TokenStream) {
    tokens.append(Literal.i64Suffixed(this))
}

/** 128-bit signed integer interpolation. */
public fun Long.i128ToTokens(tokens: TokenStream) {
    tokens.append(Literal.i128Suffixed(this))
}

/** Pointer-sized signed integer interpolation. */
public fun Long.isizeToTokens(tokens: TokenStream) {
    tokens.append(Literal.isizeSuffixed(this))
}

/** Appends this [UByte] as a suffixed `u8` [Literal] to [tokens]. */
public fun UByte.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u8Suffixed(this))
}

/** Appends this [UShort] as a suffixed `u16` [Literal] to [tokens]. */
public fun UShort.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u16Suffixed(this))
}

/** Appends this [UInt] as a suffixed `u32` [Literal] to [tokens]. */
public fun UInt.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u32Suffixed(this))
}

/** Appends this [ULong] as a suffixed `u64` [Literal] to [tokens]. */
public fun ULong.toTokens(tokens: TokenStream) {
    tokens.append(Literal.u64Suffixed(this))
}

/** 128-bit unsigned integer interpolation. */
public fun ULong.u128ToTokens(tokens: TokenStream) {
    tokens.append(Literal.u128Suffixed(this))
}

/** Pointer-sized unsigned integer interpolation. */
public fun ULong.usizeToTokens(tokens: TokenStream) {
    tokens.append(Literal.usizeSuffixed(this))
}

/** Appends this [Float] as a suffixed `f32` [Literal] to [tokens]. */
public fun Float.toTokens(tokens: TokenStream) {
    tokens.append(Literal.f32Suffixed(this))
}

/** Appends this [Double] as a suffixed `f64` [Literal] to [tokens]. */
public fun Double.toTokens(tokens: TokenStream) {
    tokens.append(Literal.f64Suffixed(this))
}

/** Appends this [Char] as a character [Literal] to [tokens]. */
public fun Char.toCharTokens(tokens: TokenStream) {
    tokens.append(Literal.character(this))
}

/** Appends this [Boolean] as a `true` or `false` [Ident] to [tokens]. */
public fun Boolean.toTokens(tokens: TokenStream) {
    val word = if (this) "true" else "false"
    tokens.append(Ident.new(word, Span.callSite()))
}

/** Appends this [ByteArray] as a C-string [Literal] to [tokens]. */
public fun ByteArray.toTokens(tokens: TokenStream) {
    tokens.append(Literal.cString(this))
}

/** Appends this [ByteArray] as a C-string [Literal] to [tokens]. */
public fun ByteArray.toCStringTokens(tokens: TokenStream) {
    tokens.append(Literal.cString(this))
}

/** Appends a clone of this [Group] to [tokens]. */
public fun Group.toTokens(tokens: TokenStream) {
    tokens.append(cloneToken())
}

/** Appends a clone of this [Ident] to [tokens]. */
public fun Ident.toTokens(tokens: TokenStream) {
    tokens.append(cloneToken())
}

/** Appends a clone of this [Punct] to [tokens]. */
public fun Punct.toTokens(tokens: TokenStream) {
    tokens.append(cloneToken())
}

/** Appends a clone of this [Literal] to [tokens]. */
public fun Literal.toTokens(tokens: TokenStream) {
    tokens.append(cloneToken())
}

/** Appends a clone of this [TokenTree] to [tokens]. */
public fun TokenTree.toTokens(tokens: TokenStream) {
    tokens.append(cloneToken())
}

/** Extends [tokens] with a clone of this [TokenStream]. */
public fun TokenStream.toTokens(tokens: TokenStream) {
    tokens.extendTokenStreams(listOf(clone()))
}

/** Returns this [TokenStream] directly. */
public fun TokenStream.intoTokenStream(): TokenStream =
    this
