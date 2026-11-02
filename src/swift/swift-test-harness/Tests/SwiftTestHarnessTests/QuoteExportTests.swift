import Testing
import Quote
import ProcMacro2

@Suite("Quote Swift Export Suite")
struct QuoteExportTests {
    @Test("Swift module imports cleanly and interpolates identifiers and spanned tokens")
    func quoteAndQuoteSpannedInterpolateTokens() throws {
        let span = Span.Companion.shared.callSite()
        let name = Ident.Companion.shared.new(string: "Widget", span: span)
        let ts = quote(template: "struct `#`name { }", interpolations: ["name": name])
        #expect(ts.isEmpty() == false)
        #expect(ts.toString() == "struct Widget { }")

        let spanned = quoteSpanned(
            span: Span.Companion.shared.mixedSite(),
            template: "impl `#`name { }",
            interpolations: ["name": name]
        )
        #expect(spanned.toString() == "impl Widget { }")
    }
}
