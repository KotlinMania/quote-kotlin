// port-lint: tests tests/test.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals

private class TokenWrapper(private val text: String) : ToTokens {
    override fun toTokens(tokens: TokenStream) {
        tokens.append(Ident.new(text, Span.callSite()))
    }
}

class ExtTest {
    @Test
    fun testAppendSeparatedAndTerminated() {
        val stream1 = TokenStream.new()
        val id1 = TokenWrapper("a")
        val id2 = TokenWrapper("b")
        val comma = TokenWrapper(",")

        stream1.appendSeparated(listOf(id1, id2), comma)
        assertEquals("a , b", stream1.toString())

        val stream2 = TokenStream.new()
        val semi = TokenWrapper(";")
        stream2.appendTerminated(listOf(id1, id2), semi)
        assertEquals("a ; b ;", stream2.toString())
    }
}
