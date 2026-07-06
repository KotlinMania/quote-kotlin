package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import kotlin.test.Test

class ProbeTest {

    @Test
    fun probeOutputs() {
        println("=== PROBE START ===")
        println("simple: [${quote("struct Foo { }")}]")
        println("impl: [${quote("impl<'a, T: ToTokens> ToTokens for &'a T { fn to_tokens(&self, tokens: &mut TokenStream) { (**self).to_tokens(tokens) } }")}]")

        val x = Ident.new("X", Span.callSite())
        println("subst: [${quote("`#`x <`#`x> (`#`x) [`#`x] {`#`x}", mapOf("x" to x))}]")

        // Integer types
        println("ii8: [${quote("`#`v", mapOf("v" to (-1).toByte()))}]")
        println("ii16: [${quote("`#`v", mapOf("v" to (-1).toShort()))}]")
        println("ii32: [${quote("`#`v", mapOf("v" to -1))}]")
        println("ii64: [${quote("`#`v", mapOf("v" to -1L))}]")
        println("uu8: [${quote("`#`v", mapOf("v" to 1u.toByte()))}]")
        println("uu16: [${quote("`#`v", mapOf("v" to 1u.toShort()))}]")
        println("uu32: [${quote("`#`v", mapOf("v" to 1u))}]")
        println("uu64: [${quote("`#`v", mapOf("v" to 1u.toLong()))}]")

        // Float
        println("f32: [${quote("`#`v", mapOf("v" to 2.345f))}]")
        println("f64: [${quote("`#`v", mapOf("v" to 2.345))}]")

        // Char
        println("char_x: [${quote("`#`v", mapOf("v" to 'x'))}]")
        println("char_heart: [${quote("`#`v", mapOf("v" to '\u2764'))}]")

        // String
        println("str: [${quote("`#`v", mapOf("v" to "hello"))}]")

        // Boolean
        println("bool_true: [${quote("`#`v", mapOf("v" to true))}]")
        println("bool_false: [${quote("`#`v", mapOf("v" to false))}]")

        // Underscore
        println("underscore: [${quote("let _ ;")}]")

        // Raw id
        println("raw_id: [${quote("r#raw_id")}]")

        // Raw lifetime
        println("raw_lifetime: [${quote("'r#async")}]")

        // Attributes
        println("attr_inline: [${quote("#[inline]")}]")
        println("inner_attr: [${quote("#![no_std]")}]")

        // Doc comments
        println("doc_outer: [${quote("/// doc")}]")
        println("doc_inner: [${quote("//! doc")}]")
        println("doc_outer_block: [${quote("/** doc */")}]")
        println("doc_inner_block: [${quote("/*! doc */")}]")

        // Ident substitution
        val foo = Ident.new("Foo", Span.callSite())
        val bar = Ident.new("Bar7", Span.callSite())
        println("ident_subst: [${quote("struct `#`foo ; enum `#`bar { }", mapOf("foo" to foo, "bar" to bar))}]")

        // Repetition with separator
        val idents = listOf(
            Ident.new("A", Span.callSite()),
            Ident.new("B", Span.callSite()),
            Ident.new("C", Span.callSite()),
        )
        println("rep_no_sep: [${quote("`#`(`#`item)*", mapOf("item" to idents))}]")
        println("rep_term_sep: [${quote("`#`(`#`item,)*", mapOf("item" to idents))}]")
        println("rep_comma_sep: [${quote("`#`(`#`item),*", mapOf("item" to idents))}]")

        // Fancy repetition
        val fooStrs = listOf("a", "b")
        val barBools = listOf(true, false)
        println("fancy_rep: [${quote("`#`(`#`foo: `#`bar),*", mapOf("foo" to fooStrs, "bar" to barBools))}]")

        // Nested repetition
        val nested = listOf(listOf('a', 'b', 'c'), listOf('x', 'y', 'z'))
        println("nested_rep: [${quote("`#`(`#`(`#`nested)*),*", mapOf("nested" to nested))}]")

        // Nonrep in repetition
        val rep = listOf("a", "b")
        val nonrep = "c"
        println("nonrep_rep: [${quote("`#`(`#`rep `#`rep : `#`nonrep `#`nonrep),*", mapOf("rep" to rep, "nonrep" to nonrep))}]")

        // Duplicate
        val ch = 'x'
        println("dup: [${quote("`#`ch `#`ch", mapOf("ch" to ch))}]")

        // Star after repetition
        val c = listOf('0', '1')
        println("star_after_rep: [${quote("`#`(f(`#`c) ;)* *out = None ;", mapOf("c" to c))}]")

        // formatIdent
        println("fmt_ident_simple: [${formatIdent("Aa")}]")
        println("fmt_ident_with_ident: [${formatIdent("Hello{}","Aa")}]")

        // TokenStream append
        val a = quote("a")
        val b = quote("b")
        a.extendTokenStreams(listOf(b))
        println("append: [$a]")

        // Group delim_span
        val g = Group(Delimiter.Parenthesis, TokenStream.new())
        println("group_delim: [${g.delimSpan().join()}]")

        println("=== PROBE END ===")
    }
}