// port-lint: source spanned.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.DelimSpan
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream

// Not public API other than via the syn crate. Use syn.spanned.Spanned.
public interface Spanned {
    public fun span(): Span
}

public fun Span.span(): Span = this.clone()

public fun DelimSpan.span(): Span = this.join()

public fun <T : ToTokens> T.span(): Span = joinSpans(TokenStream.new().also { tokens -> this.toTokens(tokens) })

public fun joinSpans(tokens: TokenStream): Span {
    var iter = ArrayDeque((ArrayDeque((tokens).toList())).map({ tt -> tt.span() }))
    val first = iter.removeFirstOrNull() ?: return Span.callSite()
    return (iter.lastOrNull()?.let({ last -> first.join(last) }) ?: first)
}
