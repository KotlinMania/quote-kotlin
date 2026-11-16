package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals

class SpannedTest {
    @Test
    fun spanAndDelimiterSpanUseNormalizedOperations() {
        val span = Span.callSite()
        assertEquals(span.byteRange(), span.span().byteRange())

        val delimiter = Group(Delimiter.Parenthesis, TokenStream.new()).delimSpan()
        assertEquals(delimiter.join().byteRange(), delimiter.span().byteRange())
    }

    @Test
    fun emptyTokenSequenceUsesCallSiteSpan() {
        val value =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) = Unit
            }
        assertEquals(Span.callSite().byteRange(), value.span().byteRange())
    }

    @Test
    fun tokenSequenceJoinsItsFirstAndLastSpans() {
        val stream = TokenStream.fromString("alpha beta gamma").getOrThrow()
        val trees = stream.clone().toList()
        val value =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) {
                    stream.toTokens(tokens)
                }
            }
        val first = trees.first().span()
        val expected = first.join(trees.last().span()) ?: first
        assertEquals(expected.byteRange(), value.span().byteRange())
    }

    @Test
    fun spanRuntimeSpecializationsExtractTheirOriginalValues() {
        val span = Span.callSite()
        assertEquals(span.byteRange(), getSpan(span).intoSpan().byteRange())

        val delimiter = Group(Delimiter.Parenthesis, TokenStream.new()).delimSpan()
        assertEquals(delimiter.join().byteRange(), getSpan(delimiter).deref().intoSpan().byteRange())
    }

    @Test
    fun sharedSpanDispatchDoesNotInvokeOwnedIntoTokenStreamOverride() {
        val stream = TokenStream.fromString("alpha beta").getOrThrow()
        val trees = stream.clone().toList()
        var ownedCalled = false
        val value =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) {
                    stream.toTokens(tokens)
                }

                override fun intoTokenStream(): TokenStream {
                    ownedCalled = true
                    error("shared &self span() must dispatch through toTokenStream(), not owned intoTokenStream()")
                }
            }
        val first = trees.first().span()
        val expected = first.join(trees.last().span()) ?: first
        assertEquals(expected.byteRange(), value.span().byteRange())
        assertEquals(false, ownedCalled)
    }
}
