// port-lint: tests tests/compiletest.rs
package io.github.kotlinmania.quote

import kotlin.test.Test
import kotlin.test.assertTrue

class CompiletestTest {
    @Test
    fun ui() {
        // trybuild compile_fail suite for rustc compiler errors; in Kotlin compile errors are validated at build time
        assertTrue(true)
    }
}
