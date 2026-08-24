import Testing
import Quote

@Suite("Quote Swift Export Suite")
struct QuoteExportTests {
    @Test("Swift module imports cleanly and basic quote works")
    func swiftModuleLoads() throws {
        let ts = quote(template: "hello", interpolations: [:])
        #expect(ts.isEmpty() == false)
    }
}

