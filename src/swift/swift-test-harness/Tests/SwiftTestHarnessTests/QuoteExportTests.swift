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

        let group = Group(delimiter: Delimiter.Parenthesis, stream: TokenStream.Companion.shared.new())
        let delimSpanned = quoteSpanned(
            span: group.delimSpan(),
            template: "0..=10 'a' 'static",
            interpolations: [:]
        )
        #expect(delimSpanned.toString() == "0 ..= 10 'a' 'static")
    }

    @Test("Swift consumer exercises formatIdent, appendAll, and primitive toTokens")
    func formatIdentAndStreamExtensionsWorkFromSwift() throws {
        let raw = formatIdent(fmt: "r#async")
        #expect(raw.toString() == "r#async")

        let combined = formatIdent(
            span: Span.Companion.shared.callSite(),
            fmt: "handle_{}",
            args: raw
        )
        #expect(combined.toString() == "handle_async")

        let lhs = quote(template: "alpha", interpolations: [:])
        let rhs = quote(template: "beta", interpolations: [:])
        appendAll(lhs, iter: rhs)
        toTokens(Int32(42), tokens: lhs)
        #expect(lhs.toString() == "alpha beta 42i32")
    }
}
