#if canImport(Testing)
import Testing
import Quote

@Suite("Quote Swift Export Smoke Tests")
struct QuoteExportTests {
    @Test("Swift module imports cleanly")
    func swiftModuleLoads() throws {
        #expect(true)
    }
}
#elseif canImport(XCTest)
import XCTest
import Quote

final class QuoteExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Quote swift module imported cleanly")
    }
}
#endif

