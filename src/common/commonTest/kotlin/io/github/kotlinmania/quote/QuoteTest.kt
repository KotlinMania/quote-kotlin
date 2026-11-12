// port-lint: tests tests/test.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.btree.BTreeSet
import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.Spacing
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.kotlinmania.procmacro.Span as ProcMacroSpan

private class X : ToTokens {
    override fun toTokens(tokens: TokenStream) {
        tokens.append(Ident.new("X", Span.callSite()))
    }
}

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
    fun testFormatIdentSimple() {
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
    fun testFormatIdentComposesRawAndNonRawIdentifiers() {
        val myIdent = formatIdent("My{}", "Ident")
        assertEquals("MyIdent", myIdent.toString())

        val raw = formatIdent("r#Raw")
        assertEquals("r#Raw", raw.toString())

        val myIdentRaw = formatIdent("{}Is{}", myIdent, raw)
        assertEquals("MyIdentIsRaw", myIdentRaw.toString())
    }

    @Test
    fun testFormatIdentIntegerFormattingOptions() {
        val num = 10.toUInt()

        assertEquals("Id_10", formatIdent("Id_{}", num).toString())
        assertEquals("Id_12", formatIdent("Id_{:o}", num).toString())
        assertEquals("Id_1010", formatIdent("Id_{:b}", num).toString())
        assertEquals("Id_a", formatIdent("Id_{:x}", num).toString())
        assertEquals("Id_A", formatIdent("Id_{:X}", num).toString())
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
        val idents =
            listOf(
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
        val idents =
            listOf(
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
        assertEquals("'static", tokens.toString())
    }

    @Test
    fun testQuoteImpl() {
        val tokens =
            quote(
                """
                impl<'a, T: ToTokens> ToTokens for &'a T {
                    fn to_tokens(&self, tokens: &mut TokenStream) {
                        (**self).to_tokens(tokens)
                    }
                }
                """,
            )
        val expected =
            "impl < 'a , T : ToTokens > ToTokens for & 'a T { " +
                "fn to_tokens (& self , tokens : & mut TokenStream) { " +
                "(* * self) . to_tokens (tokens) " +
                "} " +
                "}"
        assertEquals(expected, tokens.toString())
    }

    @Test
    fun testQuoteSpannedImpl() {
        val span = Span.callSite()
        val tokens =
            quoteSpanned(
                span,
                """
                impl<'a, T: ToTokens> ToTokens for &'a T {
                    fn to_tokens(&self, tokens: &mut TokenStream) {
                        (**self).to_tokens(tokens)
                    }
                }
                """,
            )
        val expected =
            "impl < 'a , T : ToTokens > ToTokens for & 'a T { " +
                "fn to_tokens (& self , tokens : & mut TokenStream) { " +
                "(* * self) . to_tokens (tokens) " +
                "} " +
                "}"
        assertEquals(expected, tokens.toString())
    }

    @Test
    fun testSubstitution() {
        val x = X()
        val tokens = quote("#x <#x> (#x) [#x] {#x}", mapOf("x" to x))
        assertEquals("X < X > (X) [X] { X }", tokens.toString())
    }

    @Test
    fun testIter() {
        val primes = listOf(X(), X(), X(), X())
        assertEquals("X X X X", quote("#(#primes)*", mapOf("primes" to primes)).toString())
        assertEquals("X , X , X , X ,", quote("#(#primes,)*", mapOf("primes" to primes)).toString())
        assertEquals("X , X , X , X", quote("#(#primes),*", mapOf("primes" to primes)).toString())
    }

    @Test
    fun testArray() {
        val expectedBytes = List(80) { "0u8" }.joinToString(" ")
        val array = Array(40) { 0.toUByte() }
        assertEquals(expectedBytes, quote("#(#array #array)*", "array" to array).toString())

        val refArray = Array(40) { 0.toUByte() }
        assertEquals(expectedBytes, quote("#(#refArray #refArray)*", "refArray" to refArray).toString())

        val refSlice: List<UByte> = Array(40) { 0.toUByte() }.asList()
        assertEquals(expectedBytes, quote("#(#refSlice #refSlice)*", "refSlice" to refSlice).toString())

        val xArray = arrayOf(X(), X()) // Non-copyable upstream; Kotlin does not enforce the Copy trait.
        assertEquals("X X X X", quote("#(#xArray #xArray)*", "xArray" to xArray).toString())

        val xRefArray = arrayOf(X(), X())
        assertEquals("X X X X", quote("#(#xRefArray #xRefArray)*", "xRefArray" to xRefArray).toString())

        val xRefSlice: List<X> = arrayOf(X(), X()).asList()
        assertEquals("X X X X", quote("#(#xRefSlice #xRefSlice)*", "xRefSlice" to xRefSlice).toString())

        val arrayOfArray = Array(2) { Array(2) { 0.toUByte() } }
        assertEquals("0u8 0u8 0u8 0u8", quote("#(#(#arrayOfArray)*)*", "arrayOfArray" to arrayOfArray).toString())
        // Rust's distinct owning, borrowed, and non-Copy array type checks have no Kotlin equivalent.
    }

    @Test
    fun testAdvanced() {
        val generics = quote("<'a, T>")
        val whereClause = quote("where T: Serialize")
        val fieldTy = quote("String")
        val itemTy = quote("Cow<'a, str>")
        val path = quote("SomeTrait::serialize_with")
        val value = quote("self.x")

        val tokens =
            quote(
                """
            struct SerializeWith #generics #whereClause {
                value: &'a #fieldTy,
                phantom: ::std::marker::PhantomData<#itemTy>,
            }

            impl #generics ::serde::Serialize for SerializeWith #generics #whereClause {
                fn serialize<S>(&self, s: &mut S) -> Result<(), S::Error>
                    where S: ::serde::Serializer
                {
                    #path(self.value, s)
                }
            }

            SerializeWith {
                value: #value,
                phantom: ::std::marker::PhantomData::<#itemTy>,
            }
            """,
                mapOf(
                    "generics" to generics,
                    "whereClause" to whereClause,
                    "fieldTy" to fieldTy,
                    "itemTy" to itemTy,
                    "path" to path,
                    "value" to value,
                ),
            )

        val expected =
            "struct SerializeWith < 'a , T > where T : Serialize { " +
                "value : & 'a String , " +
                "phantom : :: std :: marker :: PhantomData < Cow < 'a , str > > , " +
                "} " +
                "impl < 'a , T > :: serde :: Serialize for SerializeWith < 'a , T > where T : Serialize { " +
                "fn serialize < S > (& self , s : & mut S) -> Result < () , S :: Error > " +
                "where S : :: serde :: Serializer " +
                "{ " +
                "SomeTrait :: serialize_with (self . value , s) " +
                "} " +
                "} " +
                "SerializeWith { " +
                "value : self . x , " +
                "phantom : :: std :: marker :: PhantomData :: < Cow < 'a , str > > , " +
                "}"
        assertEquals(expected, tokens.toString())
    }

    @Test
    fun testTemplateLiteralsPreserveSpelling() {
        val tokens = quote("0 1usize 2u32 3.5f32")

        assertEquals("0 1usize 2u32 3.5f32", tokens.toString())
        assertEquals("1i32 2.5f64", quote("#integer #floating", "integer" to 1, "floating" to 2.5).toString())
    }

    @Test
    fun testInteger() {
        val ii8: Byte = -1
        val ii16: Short = -1
        val ii32 = -1
        val ii64 = -1L
        val ii128 =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) {
                    (-1L).i128ToTokens(tokens)
                }
            }
        val iisize =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) {
                    (-1L).isizeToTokens(tokens)
                }
            }
        val uu8: UByte = 1u
        val uu16: UShort = 1u
        val uu32 = 1u
        val uu64 = 1uL
        val uu128 =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) {
                    1uL.u128ToTokens(tokens)
                }
            }
        val uusize =
            object : ToTokens {
                override fun toTokens(tokens: TokenStream) {
                    1uL.usizeToTokens(tokens)
                }
            }
        // Kotlin lacks distinct 128-bit and pointer-sized primitives; these four adapters test their literal emission only.
        val tokens =
            quote(
                """
                1 1i32 1u256
                #ii8 #ii16 #ii32 #ii64 #ii128 #iisize
                #uu8 #uu16 #uu32 #uu64 #uu128 #uusize
                """,
                // Explicit Literal wrappers preserve sub-word integer and 32-bit float widths under Kotlin/JS Any erasure.
                mapOf(
                    "ii8" to Literal.i8Suffixed(ii8),
                    "ii16" to Literal.i16Suffixed(ii16),
                    "ii32" to ii32,
                    "ii64" to ii64,
                    "ii128" to ii128,
                    "iisize" to iisize,
                    "uu8" to uu8,
                    "uu16" to uu16,
                    "uu32" to uu32,
                    "uu64" to uu64,
                    "uu128" to uu128,
                    "uusize" to uusize,
                ),
            )
        val expected = "1 1i32 1u256 - 1i8 - 1i16 - 1i32 - 1i64 - 1i128 - 1isize 1u8 1u16 1u32 1u64 1u128 1usize"
        assertEquals(expected, tokens.toString())
    }

    @Test
    fun testFloating() {
        val e32 = 2.345f
        val e64 = 2.345
        val tokens = quote("#e32 #e64", "e32" to Literal.f32Suffixed(e32), "e64" to e64)
        assertEquals("2.345f32 2.345f64", tokens.toString())
    }

    @Test
    fun testChar() {
        val tokens =
            quote(
                "#zero #pound #q #apost #newline #heart",
                mapOf(
                    "zero" to '\u0001',
                    "pound" to '#',
                    "q" to '"',
                    "apost" to '\'',
                    "newline" to '\n',
                    "heart" to '\u2764',
                ),
            )
        assertEquals("'\\u{1}' '#' '\"' '\\'' '\\n' '❤'", tokens.toString())
    }

    @Test
    fun testStr() {
        val tokens = quote("#s", mapOf("s" to "\u0001 a 'b \" c"))
        assertEquals("\"\\u{1} a 'b \\\" c\"", tokens.toString())
    }

    @Test
    fun testString() {
        val tokens = quote("#s", mapOf("s" to "\u0001 a 'b \" c"))
        assertEquals("\"\\u{1} a 'b \\\" c\"", tokens.toString())
    }

    @Test
    fun testCStr() {
        val bytesWithNul = "\u0001 a 'b \" c\u0000".encodeToByteArray()
        // Kotlin has no borrowed C-string type; its byte view excludes the terminating NUL.
        val s = bytesWithNul.copyOf(bytesWithNul.size - 1)
        val tokens = quote("#s", "s" to s)
        assertEquals("c\"\\u{1} a 'b \\\" c\"", tokens.toString())
    }

    @Test
    fun testCString() {
        val s = "\u0001 a 'b \" c".encodeToByteArray()
        val tokens = quote("#s", "s" to s)
        assertEquals("c\"\\u{1} a 'b \\\" c\"", tokens.toString())
    }

    // testInterpolatedLiteral requires Rust macro literal-fragment forwarding, which a Kotlin string template cannot exercise.

    @Test
    fun testIdent() {
        val foo = Ident.new("Foo", Span.callSite())
        val bar = Ident.new("Bar${7}", Span.callSite())
        val tokens = quote("struct #foo; enum #bar {}", mapOf("foo" to foo, "bar" to bar))
        assertEquals("struct Foo ; enum Bar7 { }", tokens.toString())
    }

    @Test
    fun testUnderscore() {
        val tokens = quote("let _;")
        assertEquals("let _ ;", tokens.toString())
    }

    @Test
    fun testDuplicate() {
        val tokens = quote("#ch #ch", mapOf("ch" to 'x'))
        assertEquals("'x' 'x'", tokens.toString())
    }

    @Test
    fun testFancyRepetition() {
        val foo = listOf("a", "b")
        val bar = listOf(true, false)
        val tokens = quote("#(#foo : #bar),*", mapOf("foo" to foo, "bar" to bar))
        assertEquals("\"a\" : true , \"b\" : false", tokens.toString())
    }

    @Test
    fun testNestedFancyRepetition() {
        val nested = listOf(listOf('a', 'b', 'c'), listOf('x', 'y', 'z'))
        val tokens =
            quote(
                """
                #(
                    #(#nested)*
                ),*
                """,
                mapOf("nested" to nested),
            )
        assertEquals("'a' 'b' 'c' , 'x' 'y' 'z'", tokens.toString())
    }

    @Test
    fun testDuplicateNameRepetition() {
        val foo = listOf("a", "b")
        val tokens =
            quote(
                "#(#foo : #foo),* #(#foo : #foo),*",
                mapOf("foo" to foo),
            )
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\" \"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testDuplicateNameRepetitionNoCopy() {
        // Rust's non-Copy borrowing guarantee has no Kotlin equivalent; the repeated-value token output is retained.
        val foo = listOf("a", "b")
        val tokens = quote("#(#foo : #foo),*", mapOf("foo" to foo))
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testBtreesetRepetition() {
        val set = BTreeSet<String>()
        set.insert("a")
        set.insert("b")
        val tokens = quote("#(#set: #set),*", "set" to set)
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testSetRepetition() {
        val set = setOf("a", "b")
        val tokens = quote("#(#set : #set),*", mapOf("set" to set))
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testVariableNameConflict() {
        // The comma-separated repetition implementation uses the variable _i but it should be
        // fine, if a little confusing when debugging.
        val items = listOf('a', 'b')
        val tokens = quote("#(#_i),*", "_i" to items)
        assertEquals("'a' , 'b'", tokens.toString())
        // Rust macro hygiene is not reproduced by Kotlin template lookup; only the original variable-name input is exercised.
    }

    @Test
    fun testNonrepInRepetition() {
        val rep = listOf("a", "b")
        val nonrep = "c"
        val tokens =
            quote(
                "#(#rep #rep : #nonrep #nonrep),*",
                mapOf("rep" to rep, "nonrep" to nonrep),
            )
        assertEquals("\"a\" \"a\" : \"c\" \"c\" , \"b\" \"b\" : \"c\" \"c\"", tokens.toString())
    }

    @Test
    fun testEmptyQuote() {
        val tokens = quote("")
        assertEquals("", tokens.toString())
    }

    // testBoxStr needs the missing canonical Box port and its token dispatch; plain String interpolation is not equivalent.

    // testCow needs the missing canonical Cow/ToOwned port and its token dispatch; direct identifiers are not equivalent.

    @Test
    fun testClosure() {
        fun fieldI(i: ULong): Ident = formatIdent("__field{}", i)

        val fields =
            (0uL until 3uL)
                .asSequence()
                .map(::fieldI)
                .map { variable -> quote("#variable", "variable" to variable) }
        val tokens = quote("#(#fields)*", "fields" to fields)
        assertEquals("__field0 __field1 __field2", tokens.toString())
    }

    @Test
    fun testAppendTokens() {
        val a = quote("a")
        val b = quote("b")
        a.appendAll(b)
        assertEquals("a b", a.toString())
    }

    @Test
    fun testFormatIdent() {
        val id0 = formatIdent("Aa")
        val id1 = formatIdent("Hello{x}", "x" to id0)
        val id2 = formatIdent("Hello{x}", "x" to 5uL)
        val id3 = formatIdent("Hello{}_{x}", id0, "x" to 10uL)
        val id4 = formatIdent("Aa", "span" to Span.callSite())
        // The borrowed copy-on-write fragment case needs the missing canonical Cow/ToOwned implementation.

        assertEquals("Aa", id0.toString())
        assertEquals("HelloAa", id1.toString())
        assertEquals("Hello5", id2.toString())
        assertEquals("HelloAa_10", id3.toString())
        assertEquals("Aa", id4.toString())
    }

    @Test
    fun testFormatIdentFull() {
        val spanTokens = TokenStream.fromString("origin other").getOrThrow().toList()
        val originSpan = spanTokens[0].span()
        val overrideSpan = spanTokens[1].span()

        val id0 = formatIdent(originSpan, "Aa")
        val id1 = formatIdent("Hello{x}", "x" to id0)
        val id2 = formatIdent("Hello{x}", "x" to 5.toULong())
        val id3 = formatIdent("Hello{}_{x}", id0, "x" to 10.toULong())
        val id4 = formatIdent("Aa", "span" to overrideSpan)
        val id5 = formatIdent("Hello{}", "World")
        val id6 = formatIdent("Idx_{1}_{0:x}_{b:b}", 15.toUByte(), 7.toUShort(), "b" to 5.toUByte())

        assertEquals("Aa", id0.toString())
        assertEquals(originSpan.byteRange(), id0.span().byteRange())
        assertEquals("HelloAa", id1.toString())
        assertEquals(originSpan.byteRange(), id1.span().byteRange())
        assertEquals("Hello5", id2.toString())
        assertEquals("HelloAa_10", id3.toString())
        assertEquals(originSpan.byteRange(), id3.span().byteRange())
        assertEquals("Aa", id4.toString())
        assertEquals(overrideSpan.byteRange(), id4.span().byteRange())
        assertEquals("HelloWorld", id5.toString())
        assertEquals("Idx_7_f_101", id6.toString())
    }

    @Test
    fun testFormatIdentStripRaw() {
        val id = formatIdent("r#struct")
        val myId = formatIdent("MyId{}", id)
        val rawMyId = formatIdent("r#MyId{}", id)
        assertEquals("r#struct", id.toString())
        assertEquals("MyIdstruct", myId.toString())
        assertEquals("r#MyIdstruct", rawMyId.toString())
    }

    @Test
    fun testOuterLineComment() {
        val tokens = quote("/// doc")
        assertEquals("# [doc = r\" doc\"]", tokens.toString())
    }

    @Test
    fun testInnerLineComment() {
        val tokens = quote("//! doc")
        assertEquals("# ! [doc = r\" doc\"]", tokens.toString())
    }

    @Test
    fun testOuterBlockComment() {
        val tokens = quote("/** doc */")
        assertEquals("# [doc = r\" doc \"]", tokens.toString())
    }

    @Test
    fun testInnerBlockComment() {
        val tokens = quote("/*! doc */")
        assertEquals("# ! [doc = r\" doc \"]", tokens.toString())
    }

    @Test
    fun testOuterAttr() {
        val tokens = quote("#[inline]")
        assertEquals("# [inline]", tokens.toString())
    }

    @Test
    fun testInnerAttr() {
        val tokens = quote("#![no_std]")
        assertEquals("# ! [no_std]", tokens.toString())
    }

    // https://github.com/dtolnay/quote/issues/130
    @Test
    fun testStarAfterRepetition() {
        val c = listOf('0', '1')
        val tokens = quote("#(f(#c);)* *out = None;", mapOf("c" to c))
        assertEquals("f ('0') ; f ('1') ; * out = None ;", tokens.toString())
    }

    @Test
    fun testQuoteRawId() {
        val tokens = quote("r#raw_id")
        assertEquals("r#raw_id", tokens.toString())
    }

    @Test
    fun testQuoteRawLifetime() {
        val tokens = quote("'r#async")
        assertEquals("'r#async", tokens.toString())
    }

    @Test
    fun testTypeInferenceForSpan() {
        val span = Span.callSite()
        quoteSpanned(span, "...")

        val delimSpan = Group(Delimiter.Parenthesis, TokenStream.new()).delimSpan()
        quoteSpanned(delimSpan, "...")

        // Rust infers a trait factory's return type from a later macro call; Kotlin local inference does not work backwards.

        if (false) {
            val procMacroSpan = ProcMacroSpan.callSite()
            quoteSpanned(Span.fromProcMacro(procMacroSpan), "...")
        }
    }

    @Test
    fun testTemplateCharacterAndByteCharacterLiteralsVsLifetimes() {
        val tokens = quote("'a' '\\n' '\\'' '\\x7f' '\\u{2764}' '0' b'x' b'\\n' 'static 'r#async '_")
        val items = tokens.toList()
        assertEquals(14, items.size, "Unexpected token trees: $items")
        assertTrue(items[0] is TokenTree.Literal && items[0].toString() == "'a'")
        assertTrue(items[1] is TokenTree.Literal && items[1].toString() == "'\\n'")
        assertTrue(items[2] is TokenTree.Literal && items[2].toString() == "'\\''")
        assertTrue(items[3] is TokenTree.Literal && items[3].toString() == "'\\x7f'")
        assertTrue(items[4] is TokenTree.Literal && items[4].toString() == "'\\u{2764}'")
        assertTrue(items[5] is TokenTree.Literal && items[5].toString() == "'0'")
        assertTrue(items[6] is TokenTree.Literal && items[6].toString() == "b'x'")
        assertTrue(items[7] is TokenTree.Literal && items[7].toString() == "b'\\n'")
        assertEquals("'static 'r#async '_", quote("'static 'r#async '_").toString())
    }

    @Test
    fun testTemplateStringRawByteAndCStringLiteralsPreserveEscapes() {
        val tokens =
            quote(
                "\"a\\nb\\\"c\" r\"raw\\n\" r#\"raw \"quote\"\"# b\"bytes\\x00\" br#\"raw \"bytes\"\"# c\"cstr\\n\" cr#\"raw \"cstr\"\"#",
            )
        val items = tokens.toList()
        assertEquals(7, items.size)
        assertEquals("\"a\\nb\\\"c\"", items[0].toString())
        assertEquals("r\"raw\\n\"", items[1].toString())
        assertEquals("r#\"raw \"quote\"\"#", items[2].toString())
        assertEquals("b\"bytes\\x00\"", items[3].toString())
        assertEquals("br#\"raw \"bytes\"\"#", items[4].toString())
        assertEquals("c\"cstr\\n\"", items[5].toString())
        assertEquals("cr#\"raw \"cstr\"\"#", items[6].toString())
    }

    @Test
    fun testDotDotEqAndThreeCharPunctuationSpacing() {
        val tokens = quote("0..=10 a...b x<<=1 y>>=2")
        assertEquals("0 ..= 10 a ... b x <<= 1 y >>= 2", tokens.toString())
        val dotDotEq = quote("..=").toList()
        assertEquals(3, dotDotEq.size)
        assertEquals(Spacing.Joint, (dotDotEq[0] as TokenTree.Punct).value.spacing())
        assertEquals(Spacing.Joint, (dotDotEq[1] as TokenTree.Punct).value.spacing())
        assertEquals(Spacing.Alone, (dotDotEq[2] as TokenTree.Punct).value.spacing())
    }

    @Test
    fun testDelimitersInsideStringsCommentsAndCharsDoNotCloseGroupOrRepetition() {
        val grouped = quote("{ let s = \"}\"; let r = r#\"}\"#; let c = '}'; /* } */ }")
        val top = grouped.toList()
        assertEquals(1, top.size)
        assertTrue(top[0] is TokenTree.Group)

        val repeated = quote("#(f(\")\", #x))*", "x" to listOf(1, 2))
        assertEquals("f (\")\" , 1i32) f (\")\" , 2i32)", repeated.toString())

        val pathSep =
            quote(
                "#(#seg)::*",
                "seg" to listOf(formatIdent("alpha"), formatIdent("beta"), formatIdent("gamma")),
            )
        assertEquals("alpha :: beta :: gamma", pathSep.toString())

        val tupleIndex = quote("self.0.heap_size()")
        assertEquals("self . 0 . heap_size ()", tupleIndex.toString())
    }

    @Test
    fun testQuoteSpannedPropagatesToGroupsSeparatorsAndToTokensClones() {
        val parsed = TokenStream.fromString("alpha beta").getOrThrow().toList()
        val alphaSpan = parsed[0].span()
        val betaSpan = parsed[1].span()

        val spanned =
            quoteSpanned(
                alphaSpan,
                "{ #(#items),* }",
                "items" to listOf(Ident.new("x", betaSpan), Ident.new("y", betaSpan)),
            )
        val outerGroup = (spanned.toList().single() as TokenTree.Group).value
        assertEquals(alphaSpan.byteRange(), outerGroup.span().byteRange())
        val innerItems = outerGroup.stream().toList()
        assertEquals(3, innerItems.size)
        assertEquals(betaSpan.byteRange(), innerItems[0].span().byteRange())
        assertEquals(alphaSpan.byteRange(), innerItems[1].span().byteRange())
        assertEquals(betaSpan.byteRange(), innerItems[2].span().byteRange())

        val punct = Punct('+', Spacing.Alone, alphaSpan)
        val literal = Literal.i32Suffixed(7).also { it.setSpan(alphaSpan) }
        val group = Group(Delimiter.Bracket, TokenStream.new()).also { it.setSpan(alphaSpan) }
        val stream = TokenStream.new()
        punct.toTokens(stream)
        literal.toTokens(stream)
        group.toTokens(stream)

        punct.setSpan(betaSpan)
        literal.setSpan(betaSpan)
        group.setSpan(betaSpan)

        val emitted = stream.toList()
        assertEquals(alphaSpan.byteRange(), emitted[0].span().byteRange())
        assertEquals(alphaSpan.byteRange(), emitted[1].span().byteRange())
        assertEquals(alphaSpan.byteRange(), emitted[2].span().byteRange())
    }
}
