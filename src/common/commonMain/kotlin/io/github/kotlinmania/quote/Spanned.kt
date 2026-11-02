// port-lint: source spanned.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.DelimSpan
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream

// Not public API other than via the syn package. Use syn.Spanned.
public interface Spanned {
    public fun __span(): Span
}

public fun Span.__span(): Span = this

public fun DelimSpan.__span(): Span = this.join()

public fun ToTokens.__span(): Span = joinSpans(this.intoTokenStream())

private fun joinSpans(tokens: TokenStream): Span {
    val iter = tokens.asSequence().map { tt -> tt.span() }.iterator()
    val first = if (iter.hasNext()) iter.next() else return Span.callSite()

    return iter
        .asSequence()
        .fold<Span, Span?>(null) { _, next -> next }
        ?.let { last -> first.join(last) }
        ?: first
}

private object SpannedPrivate {
    interface Sealed
}
