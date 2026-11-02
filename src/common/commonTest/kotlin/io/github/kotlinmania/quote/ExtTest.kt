// port-lint: tests quote/tests/test.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals

private class StreamWrapper(
    private val stream: TokenStream,
) : ToTokens {
    override fun toTokens(tokens: TokenStream) {
        stream.toTokens(tokens)
    }
}

class ExtTest {
    @Test
    fun testAppendSeparatedAndTerminated() {
        val stream1 = TokenStream.new()
        val id1 = StreamWrapper(quote("a"))
        val id2 = StreamWrapper(quote("b"))
        val comma = StreamWrapper(quote(","))

        stream1.appendSeparated(listOf(id1, id2), comma)
        assertEquals("a , b", stream1.toString())

        val stream2 = TokenStream.new()
        val semi = StreamWrapper(quote(";"))
        stream2.appendTerminated(listOf(id1, id2), semi)
        assertEquals("a ; b ;", stream2.toString())
    }
}
