// port-lint: source spanned.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.DelimSpan
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream

// Not public API other than via the syn package. Use syn.Spanned.
public interface Spanned {
    public fun span(): Span
}

public fun Span.span(): Span = this.clone()

public fun DelimSpan.span(): Span = this.join()

public fun <T : ToTokens> T.span(): Span {
    val tokens = TokenStream.new()
    this.toTokens(tokens)
    return joinSpans(tokens)
}

private fun joinSpans(tokens: TokenStream): Span {
    val iter = tokens.asSequence().map { tt -> tt.span() }.iterator()
    val first = if (iter.hasNext()) iter.next() else return Span.callSite()

    return iter
        .asSequence()
        .fold<Span, Span?>(null) { _, next -> next }
        ?.let { last -> first.join(last) }
        ?: first
}
