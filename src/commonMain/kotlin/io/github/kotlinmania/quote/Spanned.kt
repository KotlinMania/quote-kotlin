// port-lint: source src/spanned.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.DelimSpan
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream

// Not public API other than via the syn crate. Use syn.spanned.Spanned.
public interface Spanned {
    public fun __span(): Span
}

public fun Span.__span(): Span = this

public fun DelimSpan.__span(): Span = this.join()

public fun ToTokens.__span(): Span = joinSpans(this.intoTokenStream())

private fun joinSpans(tokens: TokenStream): Span {
    val iter = tokens.iterator()
    if (!iter.hasNext()) {
        return Span.callSite()
    }
    val first = iter.next().span()

    var last: Span? = null
    while (iter.hasNext()) {
        last = iter.next().span()
    }

    return last?.let { first.join(it) } ?: first
}
