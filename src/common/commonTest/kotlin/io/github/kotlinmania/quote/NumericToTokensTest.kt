package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals

class NumericToTokensTest {
    @Test
    fun signedIntegersRetainTheirDeclaredWidths() {
        val byte: Byte = -1
        val short: Short = -1
        val int = -1
        val long = -1L
        val tokens = TokenStream.new()
        byte.toTokens(tokens)
        short.toTokens(tokens)
        int.toTokens(tokens)
        long.toTokens(tokens)
        assertEquals("- 1i8 - 1i16 - 1i32 - 1i64", tokens.toString())
    }

    @Test
    fun floatingPointValuesRetainTheirDeclaredWidths() {
        val float = 2.345f
        val double = 2.345
        val tokens = TokenStream.new()
        float.toTokens(tokens)
        double.toTokens(tokens)
        assertEquals("2.345f32 2.345f64", tokens.toString())
    }

    @Test
    fun typedLiteralBindingsRetainTheirDeclaredWidths() {
        val byte: Byte = -1
        val short: Short = -1
        val int = -1
        val float = 2.345f
        val double = 2.345
        val tokens =
            quote(
                "#byte #short #int #float #double",
                "byte" to Literal.i8Suffixed(byte),
                "short" to Literal.i16Suffixed(short),
                "int" to Literal.i32Suffixed(int),
                "float" to Literal.f32Suffixed(float),
                "double" to Literal.f64Suffixed(double),
            )
        assertEquals("- 1i8 - 1i16 - 1i32 2.345f32 2.345f64", tokens.toString())
    }
}
