package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro.bridge.BridgeClientState
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcMacroBridgeQuoteTest {
    @Test
    fun quoteInsideActiveBangMacroSessionRoundTripsThroughCompilerTokens() {
        val rawInput =
            io.github.kotlinmania.procmacro.TokenStream
                .fromString("Widget")
                .getOrThrow()

        val rawOutput =
            BridgeClientState.runBangMacro("quoteBridge", rawInput) { incoming ->
                assertTrue(BridgeClientState.isAvailable())
                val parsed = TokenStream.fromProcMacro(incoming)
                val targetIdent = formatIdent("Generated{}", parsed.toString())
                val body =
                    quoteSpanned(
                        Span.mixedSite(),
                        "impl `#`name { pub fn answer() -> i32 { 42 } }",
                        mapOf("name" to targetIdent),
                    )
                val full =
                    quote(
                        "pub struct `#`name ; `#`body",
                        mapOf("name" to targetIdent, "body" to body),
                    )
                full.intoProcMacro()
            }

        assertEquals(
            "pub struct GeneratedWidget ; impl GeneratedWidget { pub fn answer () -> i32 { 42 } }",
            rawOutput.toString(),
        )
    }
}
