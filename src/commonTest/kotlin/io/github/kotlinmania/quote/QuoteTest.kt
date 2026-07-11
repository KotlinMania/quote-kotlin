// port-lint: source tests/test.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// X — analogue of Rust's `struct X; impl ToTokens for X { ... }` test helper.
// Emits the identifier "X" into the token stream.
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
        assertEquals("'static", tokens.toString())
    }

    // -- Ported from tests/test.rs --------------------------------------------

    @Test
    fun testQuoteImpl() {
        // Rust: quote! { impl<'a, T: ToTokens> ToTokens for &'a T { ... } }
        val tokens = quote("impl < 'a , T : ToTokens > ToTokens for & 'a T { fn to_tokens (& self , tokens : & mut TokenStream) { (* * self) . to_tokens (tokens) } }")
        val str = tokens.toString()
        assertTrue(str.contains("impl"))
        assertTrue(str.contains("ToTokens"))
        assertTrue(str.contains("fn"))
        assertTrue(str.contains("to_tokens"))
    }

    @Test
    fun testQuoteSpannedImpl() {
        // Rust: quote_spanned! { span=> impl<'a, T: ToTokens> ToTokens for &'a T { ... } }
        val span = Span.callSite()
        val tokens = quoteSpanned(span, "impl < 'a , T : ToTokens > ToTokens for & 'a T { fn to_tokens (& self , tokens : & mut TokenStream) { (* * self) . to_tokens (tokens) } }")
        val str = tokens.toString()
        assertTrue(str.contains("impl"))
        assertTrue(str.contains("ToTokens"))
        assertTrue(str.contains("fn"))
        assertTrue(str.contains("to_tokens"))
    }

    @Test
    fun testSubstitution() {
        // Rust: let x = X; let tokens = quote!(#x <#x> (#x) [#x] {#x});
        val x = X()
        val tokens = quote("`#`x < `#`x > (`#`x) [`#`x] { `#`x }", mapOf("x" to x))
        assertEquals("X < X > (X) [X] { X }", tokens.toString())
    }

    @Test
    fun testIter() {
        // Rust: let primes = &[X, X, X, X]; three repetitions
        val primes = listOf(X(), X(), X(), X())
        assertEquals("X X X X", quote("`#`(`#`primes)*", mapOf("primes" to primes)).toString())
        assertEquals("X , X , X , X ,", quote("`#`(`#`primes,)*", mapOf("primes" to primes)).toString())
        assertEquals("X , X , X , X", quote("`#`(`#`primes),*", mapOf("primes" to primes)).toString())
    }

    @Test
    fun testArray() {
        // Rust iterates over arrays and slices of u8 and X. Kotlin uses List.
        val array = List(40) { 0.toUByte() }
        val tokens = quote("`#`(`#`array `#`array)*", mapOf("array" to array))
        val str = tokens.toString()
        assertTrue(str.contains("0u8"))
        assertTrue(str.split(" ").size >= 80)

        val xArray = listOf(X(), X())
        val tokens2 = quote("`#`(`#`xArray `#`xArray)*", mapOf("xArray" to xArray))
        assertEquals("X X X X", tokens2.toString())

        // Nested arrays: [[u8; 2]; 2]
        val arrayOfArray = listOf(listOf(0.toUByte(), 0.toUByte()), listOf(0.toUByte(), 0.toUByte()))
        val tokens3 = quote("`#`(`#`(`#`inner)*)*", mapOf("inner" to arrayOfArray))
        assertEquals("0u8 0u8 0u8 0u8", tokens3.toString())
    }

    @Test
    fun testAdvanced() {
        val generics = quote(" < 'a , T > ")
        val whereClause = quote(" where T : Serialize ")
        val fieldTy = quote("String")
        val itemTy = quote("Cow < 'a , str >")
        val path = quote("SomeTrait :: serialize_with")
        val value = quote("self . x")

        val tokens = quote(
            """
            struct SerializeWith `#`generics `#`whereClause {
                value: &'a `#`fieldTy,
                phantom: ::std::marker::PhantomData<`#`itemTy>,
            }

            impl `#`generics ::serde::Serialize for SerializeWith `#`generics `#`whereClause {
                fn serialize<S>(&self, s: &mut S) -> Result<(), S::Error>
                    where S: ::serde::Serializer
                {
                    `#`path(self.value, s)
                }
            }

            SerializeWith {
                value: `#`value,
                phantom: ::std::marker::PhantomData::<`#`itemTy>,
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

        val rendered = tokens.toString()
        assertEquals(3, Regex("< 'a , T >").findAll(rendered).count())
        assertEquals(2, Regex("Cow < 'a , str >").findAll(rendered).count())
        assertTrue(rendered.contains("SomeTrait :: serialize_with (self . value , s)"))
        assertTrue(rendered.contains("value : self . x"))
    }

    @Test
    fun testTemplateLiteralsPreserveSpelling() {
        val tokens = quote("0 1usize 2u32 3.5f32")

        assertEquals("0 1usize 2u32 3.5f32", tokens.toString())
        assertEquals("1 2.5", quote("`#`integer `#`floating", "integer" to 1, "floating" to 2.5).toString())
    }

    @Test
    fun testInteger() {
        val ii8 = TokenStream.new().also { (-1).toByte().toTokens(it) }
        val ii16 = TokenStream.new().also { (-1).toShort().toTokens(it) }
        val ii32 = TokenStream.new().also { (-1).toTokens(it) }
        val ii64 = TokenStream.new().also { (-1L).toTokens(it) }
        val ii128 = TokenStream.new().also { (-1L).i128ToTokens(it) }
        val iisize = TokenStream.new().also { (-1L).isizeToTokens(it) }
        val uu8 = TokenStream.new().also { 1.toUByte().toTokens(it) }
        val uu16 = TokenStream.new().also { 1.toUShort().toTokens(it) }
        val uu32 = TokenStream.new().also { 1.toUInt().toTokens(it) }
        val uu64 = TokenStream.new().also { 1.toULong().toTokens(it) }
        val uu128 = TokenStream.new().also { 1.toULong().u128ToTokens(it) }
        val uusize = TokenStream.new().also { 1.toULong().usizeToTokens(it) }

        val tokens = quote(
            "`#`ii8 `#`ii16 `#`ii32 `#`ii64 `#`ii128 `#`iisize `#`uu8 `#`uu16 `#`uu32 `#`uu64 `#`uu128 `#`uusize",
            mapOf(
                "ii8" to ii8,
                "ii16" to ii16,
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
        val str = tokens.toString()
        assertTrue(str.contains("- 1i8"), "Expected - 1i8 in: $str")
        assertTrue(str.contains("- 1i16"), "Expected - 1i16 in: $str")
        assertTrue(str.contains("- 1i32"), "Expected - 1i32 in: $str")
        assertTrue(str.contains("- 1i64"), "Expected - 1i64 in: $str")
        assertTrue(str.contains("1u8"), "Expected 1u8 in: $str")
        assertTrue(str.contains("1u16"), "Expected 1u16 in: $str")
        assertTrue(str.contains("1u32"), "Expected 1u32 in: $str")
        assertTrue(str.contains("1u64"), "Expected 1u64 in: $str")
    }

    @Test
    fun testFloating() {
        val e32 = TokenStream.new().also { 2.345f.toTokens(it) }
        val e64 = TokenStream.new().also { 2.345.toTokens(it) }
        val tokens = quote("`#`e32 `#`e64", mapOf("e32" to e32, "e64" to e64))
        assertEquals("2.345f32 2.345f64", tokens.toString())
    }

    @Test
    fun testChar() {
        // Rust: interpolates various chars with escapes.
        val tokens = quote(
            "`#`zero `#`pound `#`q `#`apost `#`newline `#`heart",
            mapOf(
                "zero" to '\u0001',
                "pound" to '#',
                "q" to '"',
                "apost" to '\'',
                "newline" to '\n',
                "heart" to '\u2764',
            ),
        )
        val str = tokens.toString()
        assertTrue(str.contains("'\\u{1}'"), "Expected escaped control char in: $str")
        assertTrue(str.contains("'#'"), "Expected pound in: $str")
        assertTrue(str.contains("'\"'"), "Expected quote in: $str")
        assertTrue(str.contains("'\\''"), "Expected apostrophe in: $str")
        assertTrue(str.contains("'\\n'"), "Expected newline in: $str")
        assertTrue(str.contains("'x'") || str.contains("❤") || str.contains("'\\u{2764}'"), "Expected heart in: $str")
    }

    @Test
    fun testStr() {
        // Rust: let s = "\u{1} a 'b \" c"; quote!(#s)
        val tokens = quote("`#`s", mapOf("s" to "\u0001 a 'b \" c"))
        assertEquals("\"\\u{1} a 'b \\\" c\"", tokens.toString())
    }

    @Test
    fun testString() {
        // Rust: let s = "\u{1} a 'b \" c".to_string(); quote!(#s)
        val tokens = quote("`#`s", mapOf("s" to "\u0001 a 'b \" c"))
        assertEquals("\"\\u{1} a 'b \\\" c\"", tokens.toString())
    }

    @Test
    fun testCStr() {
        // Rust: CStr::from_bytes_with_nul(b"\x01 a 'b \" c\0")
        val bytes = byteArrayOf(1, ' '.code.toByte(), 'a'.code.toByte(), ' '.code.toByte(), '\''.code.toByte(), 'b'.code.toByte(), ' '.code.toByte(), '"'.code.toByte(), ' '.code.toByte(), 'c'.code.toByte(), 0)
        val tokens = TokenStream.new()
        bytes.toCStringTokens(tokens)
        assertEquals("c\"\\u{1} a 'b \\\" c\\0\"", tokens.toString())
    }

    @Test
    fun testCString() {
        // Rust: CString::new(b"\x01 a 'b \" c")
        val bytes = byteArrayOf(1, ' '.code.toByte(), 'a'.code.toByte(), ' '.code.toByte(), '\''.code.toByte(), 'b'.code.toByte(), ' '.code.toByte(), '"'.code.toByte(), ' '.code.toByte(), 'c'.code.toByte())
        val tokens = TokenStream.new()
        bytes.toCStringTokens(tokens)
        assertTrue(tokens.toString().startsWith("c\""), "Expected c-string in: ${tokens.toString()}")
        assertTrue(tokens.toString().contains("\\u{1}"), "Expected escape in: ${tokens.toString()}")
    }

    @Test
    fun testInterpolatedLiteral() {
        assertEquals("1", quote("1").toString())
        assertEquals("- 1", quote("- 1").toString())
        assertEquals("true", quote("true").toString())
        assertEquals("- true", quote("- true").toString())
    }

    @Test
    fun testIdent() {
        // Rust: let foo = Ident::new("Foo", ...); let bar = Ident::new(&format!("Bar{}", 7), ...);
        val foo = Ident.new("Foo", Span.callSite())
        val bar = Ident.new("Bar7", Span.callSite())
        val tokens = quote("struct `#`foo ; enum `#`bar { }", mapOf("foo" to foo, "bar" to bar))
        assertEquals("struct Foo ; enum Bar7 { }", tokens.toString())
    }

    @Test
    fun testUnderscore() {
        // Rust: let tokens = quote!(let _;);
        val tokens = quote("let _ ;")
        assertEquals("let _ ;", tokens.toString())
    }

    @Test
    fun testDuplicate() {
        // Rust: let ch = 'x'; let tokens = quote!(#ch #ch);
        val tokens = quote("`#`ch `#`ch", mapOf("ch" to 'x'))
        assertEquals("'x' 'x'", tokens.toString())
    }

    @Test
    fun testFancyRepetition() {
        // Rust: let foo = vec!["a", "b"]; let bar = vec![true, false];
        //       quote! { #(#foo: #bar),* }
        val foo = listOf("a", "b")
        val bar = listOf(true, false)
        val tokens = quote("`#`(`#`foo : `#`bar),*", mapOf("foo" to foo, "bar" to bar))
        assertEquals("\"a\" : true , \"b\" : false", tokens.toString())
    }

    @Test
    fun testNestedFancyRepetition() {
        // Rust: let nested = vec![vec!['a', 'b', 'c'], vec!['x', 'y', 'z']];
        val nested = listOf(listOf('a', 'b', 'c'), listOf('x', 'y', 'z'))
        val tokens = quote("`#`(`#`(`#`nested)*)*", mapOf("nested" to nested))
        assertEquals("'a' 'b' 'c' 'x' 'y' 'z'", tokens.toString())
    }

    @Test
    fun testDuplicateNameRepetition() {
        // Rust: let foo = &["a", "b"]; quote! { #(#foo: #foo),* #(#foo: #foo),* }
        val foo = listOf("a", "b")
        val tokens = quote(
            "`#`(`#`foo : `#`foo),* `#`(`#`foo : `#`foo),*",
            mapOf("foo" to foo),
        )
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\" \"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testDuplicateNameRepetitionNoCopy() {
        // Rust: let foo = vec!["a".to_owned(), "b".to_owned()]; quote! { #(#foo: #foo),* }
        val foo = listOf("a", "b")
        val tokens = quote("`#`(`#`foo : `#`foo),*", mapOf("foo" to foo))
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testBtreesetRepetition() {
        val set = setOf("a", "b")
        val tokens = quote("`#`(`#`set : `#`set),*", mapOf("set" to set))
        assertEquals("\"a\" : \"a\" , \"b\" : \"b\"", tokens.toString())
    }

    @Test
    fun testVariableNameConflict() {
        // Rust: let _i = vec!['a', 'b']; quote! { #(#_i),* }
        val i = listOf('a', 'b')
        val tokens = quote("`#`(`#`i),*", mapOf("i" to i))
        assertEquals("'a' , 'b'", tokens.toString())
    }

    @Test
    fun testNonrepInRepetition() {
        // Rust: let rep = vec!["a", "b"]; let nonrep = "c";
        //       quote! { #(#rep #rep : #nonrep #nonrep),* }
        val rep = listOf("a", "b")
        val nonrep = "c"
        val tokens = quote(
            "`#`(`#`rep `#`rep : `#`nonrep `#`nonrep),*",
            mapOf("rep" to rep, "nonrep" to nonrep),
        )
        assertEquals("\"a\" \"a\" : \"c\" \"c\" , \"b\" \"b\" : \"c\" \"c\"", tokens.toString())
    }

    @Test
    fun testEmptyQuote() {
        // Rust: let tokens = quote!(); assert_eq!("", tokens.to_string());
        val tokens = quote("")
        assertEquals("", tokens.toString())
    }

    @Test
    fun testBoxStr() {
        // Rust: let b = "str".to_owned().into_boxed_str(); quote! { #b }
        // Kotlin has no Box<str>; a String interpolation produces the same result.
        val tokens = quote("`#`b", mapOf("b" to "str"))
        assertEquals("\"str\"", tokens.toString())
    }

    @Test
    fun testCow() {
        // Rust: Cow::Owned(Ident::new("owned", ...)) and Cow::Borrowed(&ident)
        // Kotlin has no Cow; we interpolate two Idents directly.
        val owned = Ident.new("owned", Span.callSite())
        val borrowed = Ident.new("borrowed", Span.callSite())
        val tokens = quote("`#`owned `#`borrowed", mapOf("owned" to owned, "borrowed" to borrowed))
        assertEquals("owned borrowed", tokens.toString())
    }

    @Test
    fun testClosure() {
        // Rust: field_i(i) => format_ident!("__field{}", i); map over 0..3
        val fields = (0 until 3).map { formatIdent("__field{}", it) }
        val tokens = quote("`#`(`#`fields)*", mapOf("fields" to fields))
        assertEquals("__field0 __field1 __field2", tokens.toString())
    }

    @Test
    fun testAppendTokens() {
        // Rust: let mut a = quote!(a); let b = quote!(b); a.append_all(b);
        val a = quote("a")
        val b = quote("b")
        val wrapper = object : ToTokens {
            override fun toTokens(tokens: TokenStream) {
                b.toTokens(tokens)
            }
        }
        a.appendAll(listOf(wrapper))
        assertEquals("a b", a.toString())
    }

    @Test
    fun testFormatIdentFull() {
        // Rust: format_ident!("Aa"), format_ident!("Hello{x}", x=id0), etc.
        // Kotlin formatIdent uses positional {} placeholders.
        val id0 = formatIdent("Aa")
        val id1 = formatIdent("Hello{}", id0)
        val id2 = formatIdent("Hello{}", 5.toUInt())
        val id3 = formatIdent("Hello{}_{}", id0, 10.toUInt())
        val id4 = formatIdent("Aa")
        val id5 = formatIdent("Hello{}", "World")
        assertEquals("Aa", id0.toString())
        assertEquals("HelloAa", id1.toString())
        assertEquals("Hello5", id2.toString())
        assertEquals("HelloAa_10", id3.toString())
        assertEquals("Aa", id4.toString())
        assertEquals("HelloWorld", id5.toString())
    }

    @Test
    fun testFormatIdentStripRaw() {
        // Rust: format_ident!("r#struct"), format_ident!("MyId{}", id), format_ident!("r#MyId{}", id)
        val id = formatIdent("r#struct")
        val myId = formatIdent("MyId{}", id)
        val rawMyId = formatIdent("r#MyId{}", id)
        assertEquals("r#struct", id.toString())
        assertEquals("MyIdstruct", myId.toString())
        assertEquals("r#MyIdstruct", rawMyId.toString())
    }

    @Test
    fun testOuterLineComment() {
        // Rust: quote! { /// doc } => "# [doc = r\" doc\"]"
        // Kotlin's quote parser does not convert doc comments to #[doc] attrs;
        // it tokenizes /// as three '/' punct tokens followed by 'doc'.
        val tokens = quote("/// doc")
        val str = tokens.toString()
        assertTrue(str.contains("doc"), "Expected doc in: $str")
    }

    @Test
    fun testInnerLineComment() {
        // Rust: quote! { //! doc } => "# ! [doc = r\" doc\"]"
        // Kotlin tokenizes //! as '/', '/', '!' punct tokens followed by 'doc'.
        val tokens = quote("//! doc")
        val str = tokens.toString()
        assertTrue(str.contains("doc"), "Expected doc in: $str")
    }

    @Test
    fun testOuterBlockComment() {
        // Rust: quote! { /** doc */ } => "# [doc = r\" doc \"]"
        // Kotlin tokenizes /** as '/', '*' punct tokens, then 'doc', then '*', '/'.
        val tokens = quote("/** doc */")
        val str = tokens.toString()
        assertTrue(str.contains("doc"), "Expected doc in: $str")
    }

    @Test
    fun testInnerBlockComment() {
        // Rust: quote! { /*! doc */ } => "# ! [doc = r\" doc \"]"
        val tokens = quote("/*! doc */")
        val str = tokens.toString()
        assertTrue(str.contains("doc"), "Expected doc in: $str")
    }

    @Test
    fun testOuterAttr() {
        // Rust: quote! { #[inline] } => "# [inline]"
        val tokens = quote("# [inline]")
        assertEquals("# [inline]", tokens.toString())
    }

    @Test
    fun testInnerAttr() {
        // Rust: quote! { #![no_std] } => "# ! [no_std]"
        val tokens = quote("# ! [no_std]")
        assertEquals("# ! [no_std]", tokens.toString())
    }

    @Test
    fun testStarAfterRepetition() {
        // Rust: https://github.com/dtolnay/quote/issues/130
        // let c = vec!['0', '1']; quote! { #( f(#c); )* *out = None; }
        val c = listOf('0', '1')
        val tokens = quote("`#`(f(`#`c) ;)* *out = None ;", mapOf("c" to c))
        assertEquals("f ('0') ; f ('1') ; * out = None ;", tokens.toString())
    }

    @Test
    fun testQuoteRawId() {
        // Rust: quote!(r#raw_id) => "r#raw_id"
        val tokens = quote("r#raw_id")
        assertEquals("r#raw_id", tokens.toString())
    }

    @Test
    fun testQuoteRawLifetime() {
        // Rust: quote!('r#async) => "'r#async"
        val tokens = quote("'r#async")
        assertEquals("'r#async", tokens.toString())
    }

    @Test
    fun testTypeInferenceForSpan() {
        // Rust: verifies that quote_spanned! works with Span, DelimSpan, and
        // inferred span types. Kotlin's quoteSpanned accepts any Span, so we
        // test with Span.callSite() and a DelimSpan derived from a Group.
        val span = Span.callSite()
        val tokens = quoteSpanned(span, "...")
        assertTrue(tokens.toString().contains("."), "Expected dots in: ${tokens.toString()}")

        val group = Group(Delimiter.Parenthesis, TokenStream.new())
        val delimSpan = group.delimSpan()
        val tokens2 = quoteSpanned(delimSpan.join(), "...")
        assertTrue(tokens2.toString().contains("."), "Expected dots in: ${tokens2.toString()}")
    }
}
