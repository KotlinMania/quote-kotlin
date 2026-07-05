// port-lint: source lib.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.Spacing
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

/**
 * Quasi-quote a token stream from a string template with interpolation.
 *
 * This is the Kotlin analogue of Rust's `quote! { ... }` macro. The template
 * string contains Rust-like source code. Interpolation is done with the
 * `` `#` `` marker followed by a variable name, which must be present in the
 * [interpolations] map. Repetition is done with `` `#`(...)* `` or
 * `` `#`(...),* `` syntax, where the separator is the character before `*`.
 *
 * Variables not found in [interpolations] are treated as literal identifiers
 * (the `` `#` `` is emitted as a `#` punctuation token followed by the
 * identifier).
 *
 * Tokens that originate within the quote invocation are spanned with
 * [Span.callSite].
 *
 * Example:
 * ```
 * val name = Ident.new("MyStruct", Span.callSite())
 * val tokens = quote("struct #name { }", mapOf("name" to name))
 * // produces: struct MyStruct { }
 * ```
 */
public fun quote(
    template: String,
    interpolations: Map<String, ToTokens> = emptyMap(),
): TokenStream {
    val out = TokenStream.new()
    val parser = QuoteParser(template, interpolations)
    parser.emitInto(out)
    return out
}

/**
 * Quasi-quote a token stream from a string template with interpolation,
 * using a spanned variant for hygienic macros.
 *
 * This is the Kotlin analogue of Rust's `quote_spanned! { span => ... }`
 * macro. All tokens originating within the quote invocation (i.e. not from
 * interpolated values) are spanned with [span] instead of [Span.callSite].
 */
public fun quoteSpanned(
    span: Span,
    template: String,
    interpolations: Map<String, ToTokens> = emptyMap(),
): TokenStream {
    val out = TokenStream.new()
    val parser = QuoteParser(template, interpolations, span)
    parser.emitInto(out)
    return out
}

/**
 * Convenience overload for a single interpolation pair.
 */
public fun quote(template: String, vararg pairs: Pair<String, ToTokens>): TokenStream {
    return quote(template, mapOf(*pairs))
}

/**
 * Convenience overload for quoteSpanned with pairs.
 */
public fun quoteSpanned(
    span: Span,
    template: String,
    vararg pairs: Pair<String, ToTokens>,
): TokenStream {
    return quoteSpanned(span, template, mapOf(*pairs))
}

// ---------------------------------------------------------------------------
// Parser — converts the template string into a TokenStream
// ---------------------------------------------------------------------------

private class QuoteParser(
    private val template: String,
    private val interpolations: Map<String, ToTokens>,
    private val span: Span = Span.callSite(),
) {
    private var pos = 0

    fun emitInto(out: TokenStream) {
        while (pos < template.length) {
            val ch = template[pos]

            // Skip whitespace — token streams don't store whitespace
            if (ch.isWhitespace()) {
                pos++
                continue
            }

            // Interpolation marker: `#`
            if (isInterpolationMarker()) {
                emitInterpolation(out)
                continue
            }

            // Punctuation
            if (isPunct(ch)) {
                emitPunct(out)
                continue
            }

            // String literal
            if (ch == '"') {
                emitStringLiteral(out)
                continue
            }

            // Raw string literal
            if (ch == 'r' && pos + 1 < template.length && template[pos + 1] == '"') {
                emitRawStringLiteral(out)
                continue
            }

            // Lifetime
            if (ch == '\'' && pos + 1 < template.length && isIdentStart(template[pos + 1])) {
                emitLifetime(out)
                continue
            }

            // Number literal
            if (ch.isDigit() || (ch == '-' && pos + 1 < template.length && template[pos + 1].isDigit())) {
                emitNumberLiteral(out)
                continue
            }

            // Identifier
            if (isIdentStart(ch)) {
                emitIdent(out)
                continue
            }

            // Unknown character — skip it
            pos++
        }
    }

    // -- Interpolation ------------------------------------------------------

    private fun isInterpolationMarker(): Boolean {
        // Check for `#` (backtick-hash-backtick) — the interpolation marker
        // In the template string, interpolation is written as `#`name
        // The actual bytes are: backtick, hash, backtick
        return pos + 2 < template.length &&
            template[pos] == '`' &&
            template[pos + 1] == '#' &&
            template[pos + 2] == '`'
    }

    private fun emitInterpolation(out: TokenStream) {
        // Skip the `#` marker (3 chars: backtick, hash, backtick)
        pos += 3

        // Check for repetition: `#`(...)
        if (pos < template.length && template[pos] == '(') {
            emitRepetition(out)
            return
        }

        // Read the variable name
        val name = readIdentName()
        if (name.isEmpty()) {
            // No name after `#` — emit # as punct
            out.append(TokenTree.Punct(Punct('#', Spacing.Alone, Span.callSite())))
            return
        }

        val value = interpolations[name]
        if (value != null) {
            value.toTokens(out)
        } else {
            // Variable not found — emit # as punct followed by the identifier
            out.append(TokenTree.Punct(Punct('#', Spacing.Alone, Span.callSite())))
            out.append(TokenTree.Ident(Ident.new(name, span)))
        }
    }

    private fun emitRepetition(out: TokenStream) {
        // We're at '(' — find the matching ')'
        pos++ // skip '('
        val bodyStart = pos
        var depth = 1
        while (pos < template.length && depth > 0) {
            when (template[pos]) {
                '(' -> depth++
                ')' -> depth--
            }
            if (depth > 0) pos++
        }
        val bodyEnd = pos
        pos++ // skip ')'

        // Check for separator and *
        var separator: Char? = null
        if (pos < template.length && template[pos] == '*') {
            pos++ // skip *
        } else if (pos + 1 < template.length && template[pos] != '*' && template[pos + 1] == '*') {
            separator = template[pos]
            pos += 2 // skip separator and *
        }

        // Collect all interpolation variable names in the body
        val varNames = collectVarNames(template.substring(bodyStart, bodyEnd))

        // Find the first variable that has an iterable interpolation value
        // For now, we support List<ToTokens> as the iterable type
        val iterVar = varNames.firstOrNull { interpolations[it] is Iterable<*> }

        if (iterVar != null) {
            val iterable = interpolations[iterVar] as Iterable<*>
            val bodyTemplate = template.substring(bodyStart, bodyEnd)
            var first = true
            for (item in iterable) {
                if (!first && separator != null) {
                    out.append(TokenTree.Punct(Punct(separator, Spacing.Alone, Span.callSite())))
                }
                first = false
                // Create a sub-parser with the item bound to the iterVar name
                val itemInterpolations = interpolations.toMutableMap()
                if (item is ToTokens) {
                    itemInterpolations[iterVar] = item
                }
                val subParser = QuoteParser(bodyTemplate, itemInterpolations, span)
                subParser.emitInto(out)
            }
        }
        // If no iterable variable, emit the body once (non-repeating)
    }

    private fun collectVarNames(body: String): List<String> {
        val names = mutableListOf<String>()
        var i = 0
        while (i < body.length) {
            if (i + 2 < body.length &&
                body[i] == '`' && body[i + 1] == '#' && body[i + 2] == '`'
            ) {
                i += 3
                val name = StringBuilder()
                while (i < body.length && isIdentPart(body[i])) {
                    name.append(body[i])
                    i++
                }
                if (name.isNotEmpty()) {
                    names.add(name.toString())
                }
            } else {
                i++
            }
        }
        return names
    }

    // -- Punctuation --------------------------------------------------------

    private fun isPunct(ch: Char): Boolean {
        return ch in "!#%&'*+,-./:;<=>?@^|~$"
    }

    private fun emitPunct(out: TokenStream) {
        val first = template[pos]
        pos++

        // Check for multi-char operators
        if (pos < template.length) {
            val second = template[pos]
            val two = "$first$second"
            when (two) {
                "::", "->", "=>", "==", "!=", "<=", ">=", "&&", "||",
                "<<", ">>", "+=", "-=", "*=", "/=", "&=", "|=", "^=",
                "%=", "..", "<-" -> {
                    // Check for three-char operators
                    if (pos + 1 < template.length) {
                        val third = template[pos + 1]
                        val three = "$two$third"
                        when (three) {
                            "...", "<<=", ">>=" -> {
                                emitPunct3(out, first, second, third)
                                pos += 2
                                return
                            }
                        }
                    }
                    emitPunct2(out, first, second)
                    pos++
                    return
                }
            }
        }

        emitPunct1(out, first)
    }

    private fun emitPunct1(out: TokenStream, ch: Char) {
        val punct = Punct(ch, Spacing.Alone, span)
        out.append(TokenTree.Punct(punct))
    }

    private fun emitPunct2(out: TokenStream, ch1: Char, ch2: Char) {
        val p1 = Punct(ch1, Spacing.Joint, span)
        out.append(TokenTree.Punct(p1))
        val p2 = Punct(ch2, Spacing.Alone, span)
        out.append(TokenTree.Punct(p2))
    }

    private fun emitPunct3(out: TokenStream, ch1: Char, ch2: Char, ch3: Char) {
        val p1 = Punct(ch1, Spacing.Joint, span)
        out.append(TokenTree.Punct(p1))
        val p2 = Punct(ch2, Spacing.Joint, span)
        out.append(TokenTree.Punct(p2))
        val p3 = Punct(ch3, Spacing.Alone, span)
        out.append(TokenTree.Punct(p3))
    }

    // -- String literal -----------------------------------------------------

    private fun emitStringLiteral(out: TokenStream) {
        val start = pos
        pos++ // skip opening "
        while (pos < template.length && template[pos] != '"') {
            if (template[pos] == '\\' && pos + 1 < template.length) {
                pos += 2 // skip escape sequence
            } else {
                pos++
            }
        }
        pos++ // skip closing "
        val text = template.substring(start, pos)
        val lit = Literal.string(text.removeSurrounding("\""))
        out.append(TokenTree.Literal(lit))
    }

    private fun emitRawStringLiteral(out: TokenStream) {
        pos++ // skip 'r'
        val start = pos
        pos++ // skip opening "
        while (pos < template.length && template[pos] != '"') {
            pos++
        }
        pos++ // skip closing "
        val text = template.substring(start, pos)
        val lit = Literal.string(text.removeSurrounding("\""))
        out.append(TokenTree.Literal(lit))
    }

    // -- Lifetime -----------------------------------------------------------

    private fun emitLifetime(out: TokenStream) {
        val apostrophe = Punct('\'', Spacing.Joint, span)
        out.append(TokenTree.Punct(apostrophe))

        val name = readIdentName()
        if (name.isNotEmpty()) {
            val ident = mkIdent(name, span)
            out.append(TokenTree.Ident(ident))
        }
    }

    // -- Number literal -----------------------------------------------------

    private fun emitNumberLiteral(out: TokenStream) {
        val start = pos
        if (template[pos] == '-') pos++
        while (pos < template.length && (template[pos].isDigit() || template[pos] in "._abcdefABCDEFxXouifl")) {
            pos++
        }
        val text = template.substring(start, pos)
        // Try to parse as integer or float, emit appropriate literal
        try {
            val longVal = text.replace("_", "").toLong()
            out.append(TokenTree.Literal(Literal.i64Suffixed(longVal)))
        } catch (e: NumberFormatException) {
            try {
                val doubleVal = text.replace("_", "").toDouble()
                out.append(TokenTree.Literal(Literal.f64Suffixed(doubleVal)))
            } catch (e2: NumberFormatException) {
                // Fallback: emit as string literal
                out.append(TokenTree.Literal(Literal.string(text)))
            }
        }
    }

    // -- Identifier ---------------------------------------------------------

    private fun emitIdent(out: TokenStream) {
        val name = readIdentName()
        if (name.isNotEmpty()) {
            val ident = mkIdent(name, span)
            out.append(TokenTree.Ident(ident))
        }
    }

    private fun readIdentName(): String {
        val start = pos
        while (pos < template.length && isIdentPart(template[pos])) {
            pos++
        }
        return template.substring(start, pos)
    }

    private fun isIdentStart(ch: Char): Boolean {
        return ch.isLetter() || ch == '_'
    }

    private fun isIdentPart(ch: Char): Boolean {
        return ch.isLetterOrDigit() || ch == '_'
    }
}