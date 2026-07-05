// port-lint: source tests/test.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuoteTest {

    @Test
    fun testQuoteSimple() {
        val tokens = quote("struct Foo { }")
        val str = tokens.toString()
        assertTrue(str.contains("struct"))
        assertTrue(str.contains("Foo"))
    }

    @Test
    fun testQuoteInterpolation() {
        val name = Ident.new("MyStruct", Span.callSite())
        val tokens = quote("struct `#`name { }", mapOf("name" to name))
        val str = tokens.toString()
        assertTrue(str.contains("MyStruct"), "Expected MyStruct in: $str")
    }

    @Test
    fun testQuoteSpanned() {
        val span = Span.callSite()
        val tokens = quoteSpanned(span, "impl Trait for Type { }")
        assertTrue(tokens.toString().contains("impl"))
    }

    @Test
    fun testFormatIdent() {
        val ident = formatIdent("MyIdent")
        assertEquals("MyIdent", ident.toString())
    }

    @Test
    fun testFormatIdentWithArg() {
        val ident = formatIdent("My{}", "Ident")
        assertEquals("MyIdent", ident.toString())
    }

    @Test
    fun testFormatIdentWithIdent() {
        val base = Ident.new("Base", Span.callSite())
        val ident = formatIdent("_{}", base)
        assertEquals("_Base", ident.toString())
    }

    @Test
    fun testFormatIdentWithInt() {
        val ident = formatIdent("Id_{}", 42)
        assertEquals("Id_42", ident.toString())
    }

    @Test
    fun testQuotePunctuation() {
        val tokens = quote("a :: b -> c => d")
        val str = tokens.toString()
        assertTrue(str.contains("a"))
        assertTrue(str.contains("b"))
        assertTrue(str.contains("c"))
        assertTrue(str.contains("d"))
    }

    @Test
    fun testQuoteToTokens() {
        val x = Ident.new("X", Span.callSite())
        val tokens = quote("`#`x < `#`x > (`#`x) [`#`x] { `#`x }", mapOf("x" to x))
        val str = tokens.toString()
        assertTrue(str.contains("X"))
    }

    @Test
    fun testQuoteRepetition() {
        val idents = listOf(
            Ident.new("A", Span.callSite()),
            Ident.new("B", Span.callSite()),
            Ident.new("C", Span.callSite()),
        )
        val tokens = quote("`#`(`#`item)*", mapOf("item" to idents))
        val str = tokens.toString()
        assertTrue(str.contains("A"), "Expected A in: $str")
        assertTrue(str.contains("B"), "Expected B in: $str")
        assertTrue(str.contains("C"), "Expected C in: $str")
    }

    @Test
    fun testQuoteRepetitionWithSeparator() {
        val idents = listOf(
            Ident.new("A", Span.callSite()),
            Ident.new("B", Span.callSite()),
            Ident.new("C", Span.callSite()),
        )
        val tokens = quote("`#`(`#`item),*", mapOf("item" to idents))
        val str = tokens.toString()
        assertTrue(str.contains("A"))
        assertTrue(str.contains("B"))
        assertTrue(str.contains("C"))
    }

    @Test
    fun testQuoteEmpty() {
        val tokens = quote("")
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun testQuoteIdent() {
        val tokens = quote("hello")
        assertTrue(tokens.toString().contains("hello"))
    }

    @Test
    fun testQuoteLifetime() {
        val tokens = quote("'static")
        val str = tokens.toString()
        assertTrue(str.contains("static") || str.contains("'"))
    }
}