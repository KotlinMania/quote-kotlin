package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.DelimSpan
import io.github.kotlinmania.procmacro2.Delimiter
import io.github.kotlinmania.procmacro2.Group
import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Literal
import io.github.kotlinmania.procmacro2.Punct
import io.github.kotlinmania.procmacro2.Spacing
import io.github.kotlinmania.procmacro2.Span
import io.github.kotlinmania.procmacro2.TokenStream
import io.github.kotlinmania.procmacro2.TokenTree

/**
 * Quasi-quote a token stream from a string template with interpolation.
 *
 * The template string contains token syntax. Variable interpolation is done
 * with `#name` or `` `#`name `` where `name` is present in [interpolations].
 * Repetition is done with `#(...)*` or `#(...),*` (or `` `#`(...)* ``), where
 * an optional separator token appears before `*`.
 *
 * A `null` interpolation value emits no tokens, matching an absent optional
 * interpolation.
 *
 * Tokens that originate within the [quote] invocation are spanned with
 * [Span.callSite], while interpolated values preserve their own spans.
 *
 * Example:
 * ```kotlin
 * val name = Ident.new("Widget", Span.callSite())
 * val tokens = quote("Type #name ( )", mapOf("name" to name))
 * ```
 */
public fun quote(
    template: String,
    interpolations: Map<String, *> = emptyMap<String, Any?>(),
): TokenStream {
    val out = TokenStream.new()
    val parser = QuoteParser(template, interpolations)
    parser.emitInto(out)
    return out
}

/**
 * Quasi-quote a token stream from a string template with interpolation,
 * applying [span] to all tokens originating within this invocation.
 *
 * Interpolated values preserve the spans produced by their [ToTokens]
 * implementations.
 */
public fun quoteSpanned(
    span: Span,
    template: String,
    interpolations: Map<String, *> = emptyMap<String, Any?>(),
): TokenStream {
    val out = TokenStream.new()
    val parser = QuoteParser(template, interpolations, span)
    parser.emitInto(out)
    return out
}

/**
 * Quasi-quote a token stream using the joined span of [span] for all tokens
 * originating within this invocation.
 */
public fun quoteSpanned(
    span: DelimSpan,
    template: String,
    interpolations: Map<String, *> = emptyMap<String, Any?>(),
): TokenStream = quoteSpanned(span.join(), template, interpolations)

/**
 * Convenience overload of [quote] accepting interpolation pairs.
 */
public fun quote(template: String, vararg pairs: Pair<String, *>): TokenStream =
    quote(template, pairs.toMap())

/**
 * Convenience overload of [quoteSpanned] accepting interpolation pairs.
 */
public fun quoteSpanned(
    span: Span,
    template: String,
    vararg pairs: Pair<String, *>,
): TokenStream = quoteSpanned(span, template, pairs.toMap())

/**
 * Convenience overload of [quoteSpanned] with [DelimSpan] and interpolation pairs.
 */
public fun quoteSpanned(
    span: DelimSpan,
    template: String,
    vararg pairs: Pair<String, *>,
): TokenStream = quoteSpanned(span.join(), template, pairs.toMap())

// ---------------------------------------------------------------------------
// Parser — converts the template string into a TokenStream
// ---------------------------------------------------------------------------

private class QuoteParser(
    private val template: String,
    private val interpolations: Map<String, *>,
    private val span: Span = Span.callSite(),
) {
    private var pos = 0

    fun emitInto(out: TokenStream) {
        while (pos < template.length) {
            val ch = template[pos]

            if (ch.isWhitespace()) {
                pos++
                continue
            }

            if (ch == '/' &&
                pos + 1 < template.length &&
                (template[pos + 1] == '/' || template[pos + 1] == '*')
            ) {
                emitComment(out)
                continue
            }

            if (isInterpolationMarkerAt(pos)) {
                emitInterpolation(out)
                continue
            }

            if (ch == '(' || ch == '[' || ch == '{') {
                emitGroup(out)
                continue
            }

            val rawStringEnd = tryScanRawStringEnd(pos)
            if (rawStringEnd != null) {
                emitLiteralSlice(out, pos, rawStringEnd)
                pos = rawStringEnd
                continue
            }

            val cookedStringEnd = tryScanCookedStringEnd(pos)
            if (cookedStringEnd != null) {
                emitLiteralSlice(out, pos, cookedStringEnd)
                pos = cookedStringEnd
                continue
            }

            val byteCharEnd = tryScanByteCharLiteralEnd(pos)
            if (byteCharEnd != null) {
                emitLiteralSlice(out, pos, byteCharEnd)
                pos = byteCharEnd
                continue
            }

            if (ch == '\'') {
                val charEnd = tryScanCharLiteralEnd(pos)
                if (charEnd != null) {
                    emitLiteralSlice(out, pos, charEnd)
                    pos = charEnd
                    continue
                }
                if (pos + 1 < template.length && isIdentStart(template[pos + 1])) {
                    emitQuotedIdent(out)
                    continue
                }
            }

            if (ch == 'r' &&
                pos + 2 < template.length &&
                template[pos + 1] == '#' &&
                isIdentStart(template[pos + 2])
            ) {
                emitRawIdent(out)
                continue
            }

            if (ch.isDigit()) {
                emitNumberLiteral(out)
                continue
            }

            if (isPunct(ch)) {
                emitPunct(out)
                continue
            }

            if (isIdentStart(ch)) {
                emitIdent(out)
                continue
            }

            pos++
        }
    }

    // -- Interpolation ------------------------------------------------------

    private fun isInterpolationMarkerAt(index: Int): Boolean {
        if (index + 2 < template.length &&
            template[index] == '`' &&
            template[index + 1] == '#' &&
            template[index + 2] == '`'
        ) {
            return true
        }
        if (template[index] == '#') {
            val nextIdx = skipWhitespaceAndNonDocComments(index + 1)
            if (isRepetitionStartAt(nextIdx)) {
                return true
            }
            if (index + 1 < template.length && isIdentStart(template[index + 1])) {
                val start = index + 1
                var end = start
                while (end < template.length && isIdentPart(template[end])) end++
                val ident = template.substring(start, end)
                if (interpolations.containsKey(ident)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isRepetitionStartAt(parenIdx: Int): Boolean {
        if (parenIdx >= template.length || template[parenIdx] != '(') {
            return false
        }
        val closeParen = findMatchingDelimiter(parenIdx, '(', ')')
        if (closeParen >= template.length) {
            return false
        }
        val afterClose = skipWhitespaceAndNonDocComments(closeParen + 1)
        if (afterClose < template.length && template[afterClose] == '*') {
            return true
        }
        val sepEnd = tryScanSeparatorTokenEnd(afterClose) ?: return false
        val afterSep = skipWhitespaceAndNonDocComments(sepEnd)
        return afterSep < template.length && template[afterSep] == '*'
    }

    private fun emitInterpolation(out: TokenStream) {
        if (template[pos] == '`') {
            pos += 3
        } else {
            pos += 1
        }

        val afterMarker = skipWhitespaceAndNonDocComments(pos)
        if (isRepetitionStartAt(afterMarker)) {
            pos = afterMarker
            emitRepetition(out)
            return
        }

        val name = readIdentName()
        if (name.isEmpty()) {
            out.append(TokenTree.Punct(Punct('#', Spacing.Alone, span)))
            return
        }

        require(interpolations.containsKey(name)) {
            "missing quote interpolation: $name"
        }
        emitValue(out, interpolations[name])
    }

    private fun emitRepetition(out: TokenStream) {
        val closeParen = findMatchingDelimiter(pos, '(', ')')
        val bodyStart = pos + 1
        val bodyEnd = closeParen
        pos = closeParen + 1

        var separatorSlice: String? = null
        val afterClose = skipWhitespaceAndNonDocComments(pos)
        if (afterClose < template.length && template[afterClose] == '*') {
            pos = afterClose + 1
        } else {
            val sepEnd = tryScanSeparatorTokenEnd(afterClose)
            if (sepEnd != null) {
                val afterSep = skipWhitespaceAndNonDocComments(sepEnd)
                if (afterSep < template.length && template[afterSep] == '*') {
                    separatorSlice = template.substring(afterClose, sepEnd)
                    pos = afterSep + 1
                }
            }
        }

        val bodyTemplate = template.substring(bodyStart, bodyEnd)
        val varNames = collectVarNames(bodyTemplate).distinct()
        val iterVars = varNames.filter { isRepetitionIterable(interpolations[it]) }

        if (iterVars.isNotEmpty()) {
            val iterLists = iterVars.map { toRepetitionList(interpolations[it]) }
            val len = iterLists.minOf { it.size }

            var first = true
            for (i in 0 until len) {
                if (!first && separatorSlice != null) {
                    QuoteParser(separatorSlice, emptyMap<String, Any?>(), span).emitInto(out)
                }
                first = false
                val itemInterpolations = interpolations.toMutableMap()
                for ((idx, iterVar) in iterVars.withIndex()) {
                    itemInterpolations[iterVar] = iterLists[idx][i]
                }
                QuoteParser(bodyTemplate, itemInterpolations, span).emitInto(out)
            }
        }
    }

    private fun isRepetitionIterable(value: Any?): Boolean =
        when (value) {
            is ToTokens, is TokenStream -> false
            is Iterable<*>, is Sequence<*>, is Array<*> -> true
            else -> false
        }

    private fun toRepetitionList(value: Any?): List<Any?> =
        when (value) {
            is List<*> -> value
            is Iterable<*> -> value.toList()
            is Sequence<*> -> value.toList()
            is Array<*> -> value.toList()
            else -> emptyList()
        }

    private fun tryScanSeparatorTokenEnd(index: Int): Int? {
        if (index >= template.length) return null
        val ch = template[index]
        if (ch == '*' || ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}') {
            return null
        }
        val rawStrEnd = tryScanRawStringEnd(index)
        if (rawStrEnd != null) return rawStrEnd
        val cookedStrEnd = tryScanCookedStringEnd(index)
        if (cookedStrEnd != null) return cookedStrEnd
        val byteCharEnd = tryScanByteCharLiteralEnd(index)
        if (byteCharEnd != null) return byteCharEnd
        if (ch == '\'') {
            val charEnd = tryScanCharLiteralEnd(index)
            if (charEnd != null) return charEnd
        }
        if (isPunct(ch)) {
            return scanPunctEnd(index)
        }
        if (isIdentStart(ch)) {
            var end = index + 1
            while (end < template.length && isIdentPart(template[end])) end++
            return end
        }
        return null
    }

    private fun collectVarNames(body: String): List<String> {
        val names = mutableListOf<String>()
        val sub = QuoteParser(body, interpolations, span)
        var i = 0
        while (i < body.length) {
            val ch = body[i]
            if (ch == '/' && i + 1 < body.length && (body[i + 1] == '/' || body[i + 1] == '*')) {
                i = sub.skipCommentAt(i)
                continue
            }
            val rawEnd = sub.tryScanRawStringEnd(i)
            if (rawEnd != null) {
                i = rawEnd
                continue
            }
            val cookedEnd = sub.tryScanCookedStringEnd(i)
            if (cookedEnd != null) {
                i = cookedEnd
                continue
            }
            val byteCharEnd = sub.tryScanByteCharLiteralEnd(i)
            if (byteCharEnd != null) {
                i = byteCharEnd
                continue
            }
            if (ch == '\'') {
                val charEnd = sub.tryScanCharLiteralEnd(i)
                if (charEnd != null) {
                    i = charEnd
                    continue
                }
            }
            if (i + 2 < body.length &&
                body[i] == '`' &&
                body[i + 1] == '#' &&
                body[i + 2] == '`'
            ) {
                i += 3
                val start = i
                while (i < body.length && isIdentPart(body[i])) i++
                if (i > start) {
                    names.add(body.substring(start, i))
                }
            } else if (ch == '#' && i + 1 < body.length && isIdentStart(body[i + 1])) {
                val start = i + 1
                var end = start
                while (end < body.length && isIdentPart(body[end])) end++
                val ident = body.substring(start, end)
                if (interpolations.containsKey(ident)) {
                    names.add(ident)
                    i = end
                } else {
                    i++
                }
            } else {
                i++
            }
        }
        return names
    }

    // -- Value emission -----------------------------------------------------

    private fun emitValue(out: TokenStream, value: Any?) {
        when (value) {
            null -> Unit
            is ToTokens -> value.toTokens(out)
            is Ident -> value.toTokens(out)
            is TokenStream -> value.toTokens(out)
            is TokenTree -> value.toTokens(out)
            is Group -> value.toTokens(out)
            is Punct -> value.toTokens(out)
            is Literal -> value.toTokens(out)
            is String -> value.toTokens(out)
            is CharSequence -> value.toString().toTokens(out)
            is Long -> value.toTokens(out)
            is Number -> {
                val literal = Literal.fromString(value.toString()).getOrThrow()
                literal.setSpan(span)
                literal.toTokens(out)
            }
            is Boolean -> value.toTokens(out)
            is UByte -> value.toTokens(out)
            is UShort -> value.toTokens(out)
            is UInt -> value.toTokens(out)
            is ULong -> value.toTokens(out)
            is Char -> value.toCharTokens(out)
            is ByteArray -> value.toTokens(out)
            is Iterable<*> -> {
                for (item in value) {
                    emitValue(out, item)
                }
            }
            is Sequence<*> -> {
                for (item in value) {
                    emitValue(out, item)
                }
            }
            is Array<*> -> {
                for (item in value) {
                    emitValue(out, item)
                }
            }
            else -> error("Unsupported quote interpolation value")
        }
    }

    // -- Comments and doc attributes ----------------------------------------

    private fun skipWhitespaceAndNonDocComments(start: Int): Int {
        var i = start
        while (i < template.length) {
            if (template[i].isWhitespace()) {
                i++
                continue
            }
            if (template.startsWith("//", i) &&
                (!template.startsWith("///", i) || template.startsWith("////", i)) &&
                !template.startsWith("//!", i)
            ) {
                i = skipCommentAt(i)
                continue
            }
            if (template.startsWith("/*", i) &&
                (template.startsWith("/**/", i) || !template.startsWith("/**", i) || template.startsWith("/***", i)) &&
                !template.startsWith("/*!", i)
            ) {
                i = skipCommentAt(i)
                continue
            }
            break
        }
        return i
    }

    private fun skipCommentAt(start: Int): Int {
        if (template[start + 1] == '/') {
            val end = template.indexOf('\n', start)
            return if (end == -1) template.length else end
        }
        var cursor = start + 2
        var depth = 1
        while (cursor < template.length && depth > 0) {
            when {
                template.startsWith("/*", cursor) -> {
                    depth++
                    cursor += 2
                }
                template.startsWith("*/", cursor) -> {
                    depth--
                    cursor += 2
                }
                else -> cursor++
            }
        }
        return cursor
    }

    private fun emitComment(out: TokenStream) {
        val lineComment = template[pos + 1] == '/'
        if (lineComment) {
            val outerDoc = template.startsWith("///", pos) && !template.startsWith("////", pos)
            val innerDoc = template.startsWith("//!", pos)
            val contentStart = if (outerDoc || innerDoc) pos + 3 else pos + 2
            val end = template.indexOf('\n', pos).let { if (it == -1) template.length else it }
            if (outerDoc || innerDoc) {
                emitDocAttribute(out, template.substring(contentStart, end), innerDoc)
            }
            pos = end
            return
        }

        val outerDoc =
            template.startsWith("/**", pos) &&
                !template.startsWith("/***", pos) &&
                !template.startsWith("/**/", pos)
        val innerDoc = template.startsWith("/*!", pos)
        val contentStart = if (outerDoc || innerDoc) pos + 3 else pos + 2
        var cursor = pos + 2
        var depth = 1
        var contentEnd = cursor
        while (cursor < template.length && depth > 0) {
            when {
                template.startsWith("/*", cursor) -> {
                    depth++
                    cursor += 2
                }
                template.startsWith("*/", cursor) -> {
                    depth--
                    contentEnd = cursor
                    cursor += 2
                }
                else -> cursor++
            }
        }
        if (outerDoc || innerDoc) {
            emitDocAttribute(out, template.substring(contentStart, contentEnd), innerDoc)
        }
        pos = cursor
    }

    private fun emitDocAttribute(out: TokenStream, content: String, inner: Boolean) {
        out.append(TokenTree.Punct(Punct('#', Spacing.Alone, span)))
        if (inner) {
            out.append(TokenTree.Punct(Punct('!', Spacing.Alone, span)))
        }

        val attribute = TokenStream.new()
        attribute.append(TokenTree.Ident(Ident.new("doc", span)))
        attribute.append(TokenTree.Punct(Punct('=', Spacing.Alone, span)))
        val literal = Literal.fromString(rawStringLiteral(content)).getOrThrow()
        literal.setSpan(span)
        attribute.append(TokenTree.Literal(literal))
        val group = Group(Delimiter.Bracket, attribute)
        group.setSpan(span)
        out.append(TokenTree.Group(group))
    }

    private fun rawStringLiteral(content: String): String {
        var hashes = ""
        while (content.contains("\"$hashes")) {
            hashes += "#"
        }
        return "r$hashes\"$content\"$hashes"
    }

    // -- Punctuation --------------------------------------------------------

    private fun isPunct(ch: Char): Boolean = ch in "!#%&'*+,-./:;<=>?@^|~$"

    private fun scanPunctEnd(index: Int): Int {
        val first = template[index]
        if (index + 1 < template.length) {
            val second = template[index + 1]
            val two = "$first$second"
            when (two) {
                "::", "->", "=>", "==", "!=", "<=", ">=", "&&", "||",
                "<<", ">>", "+=", "-=", "*=", "/=", "&=", "|=", "^=",
                "%=", "..", "<-",
                -> {
                    if (index + 2 < template.length) {
                        val third = template[index + 2]
                        when ("$two$third") {
                            "...", "..=", "<<=", ">>=" -> return index + 3
                        }
                    }
                    return index + 2
                }
            }
        }
        return index + 1
    }

    private fun emitPunct(out: TokenStream) {
        val end = scanPunctEnd(pos)
        when (end - pos) {
            3 -> emitPunct3(out, template[pos], template[pos + 1], template[pos + 2])
            2 -> emitPunct2(out, template[pos], template[pos + 1])
            else -> emitPunct1(out, template[pos])
        }
        pos = end
    }

    private fun emitPunct1(out: TokenStream, ch: Char) {
        out.append(TokenTree.Punct(Punct(ch, Spacing.Alone, span)))
    }

    private fun emitPunct2(out: TokenStream, ch1: Char, ch2: Char) {
        out.append(TokenTree.Punct(Punct(ch1, Spacing.Joint, span)))
        out.append(TokenTree.Punct(Punct(ch2, Spacing.Alone, span)))
    }

    private fun emitPunct3(out: TokenStream, ch1: Char, ch2: Char, ch3: Char) {
        out.append(TokenTree.Punct(Punct(ch1, Spacing.Joint, span)))
        out.append(TokenTree.Punct(Punct(ch2, Spacing.Joint, span)))
        out.append(TokenTree.Punct(Punct(ch3, Spacing.Alone, span)))
    }

    // -- String and character literals --------------------------------------

    private fun emitLiteralSlice(out: TokenStream, start: Int, end: Int) {
        val text = template.substring(start, end)
        val literal = Literal.fromString(text).getOrThrow()
        literal.setSpan(span)
        out.append(TokenTree.Literal(literal))
    }

    private fun tryScanCookedStringEnd(start: Int): Int? {
        val quoteIdx =
            when {
                start < template.length && template[start] == '"' -> start
                start + 1 < template.length &&
                    (template[start] == 'b' || template[start] == 'c') &&
                    template[start + 1] == '"' -> start + 1
                else -> return null
            }
        var i = quoteIdx + 1
        while (i < template.length) {
            val ch = template[i]
            if (ch == '"') {
                i++
                while (i < template.length && isIdentPart(template[i])) i++
                return i
            }
            if (ch == '\\' && i + 1 < template.length) {
                i += 2
            } else {
                i++
            }
        }
        return null
    }

    private fun tryScanRawStringEnd(start: Int): Int? {
        val hashStart =
            when {
                start < template.length && template[start] == 'r' -> start + 1
                start + 1 < template.length &&
                    (template[start] == 'b' || template[start] == 'c') &&
                    template[start + 1] == 'r' -> start + 2
                else -> return null
            }
        var hashes = 0
        while (hashStart + hashes < template.length && template[hashStart + hashes] == '#') {
            hashes++
        }
        val openQuote = hashStart + hashes
        if (openQuote >= template.length || template[openQuote] != '"') {
            return null
        }
        var i = openQuote + 1
        while (i < template.length) {
            if (template[i] == '"' && hasRepeatedHash(i + 1, hashes)) {
                i += 1 + hashes
                while (i < template.length && isIdentPart(template[i])) i++
                return i
            }
            i++
        }
        return null
    }

    private fun hasRepeatedHash(start: Int, count: Int): Boolean {
        if (start + count > template.length) return false
        for (k in 0 until count) {
            if (template[start + k] != '#') return false
        }
        return true
    }

    private fun tryScanByteCharLiteralEnd(start: Int): Int? {
        if (start + 2 >= template.length || template[start] != 'b' || template[start + 1] != '\'') {
            return null
        }
        return scanSingleQuotedBodyEnd(start + 2)
    }

    private fun tryScanCharLiteralEnd(start: Int): Int? {
        if (start + 2 >= template.length || template[start] != '\'') {
            return null
        }
        return scanSingleQuotedBodyEnd(start + 1)
    }

    private fun scanSingleQuotedBodyEnd(bodyStart: Int): Int? {
        var i = bodyStart
        if (i >= template.length) return null
        val ch = template[i]
        if (ch == '\\') {
            i++
            if (i >= template.length) return null
            when (template[i]) {
                'u' -> {
                    if (i + 1 >= template.length || template[i + 1] != '{') return null
                    val closeBrace = template.indexOf('}', i + 2)
                    if (closeBrace == -1) return null
                    i = closeBrace + 1
                }
                'x' -> {
                    if (i + 2 >= template.length) return null
                    i += 3
                }
                'n', 'r', 't', '\\', '0', '\'', '"' -> i += 1
                else -> return null
            }
        } else {
            if (ch == '\'' || ch == '\n' || ch == '\r' || ch == '\t') return null
            i += if (ch.isHighSurrogate() && i + 1 < template.length && template[i + 1].isLowSurrogate()) 2 else 1
        }
        if (i >= template.length || template[i] != '\'') {
            return null
        }
        i++
        while (i < template.length && isIdentPart(template[i])) i++
        return i
    }

    // -- Group delimiters ---------------------------------------------------

    private fun findMatchingDelimiter(openPos: Int, open: Char, close: Char): Int {
        var i = openPos + 1
        var depth = 1
        while (i < template.length && depth > 0) {
            val ch = template[i]
            if (ch == '/' && i + 1 < template.length && (template[i + 1] == '/' || template[i + 1] == '*')) {
                i = skipCommentAt(i)
                continue
            }
            val rawEnd = tryScanRawStringEnd(i)
            if (rawEnd != null) {
                i = rawEnd
                continue
            }
            val cookedEnd = tryScanCookedStringEnd(i)
            if (cookedEnd != null) {
                i = cookedEnd
                continue
            }
            val byteCharEnd = tryScanByteCharLiteralEnd(i)
            if (byteCharEnd != null) {
                i = byteCharEnd
                continue
            }
            if (ch == '\'') {
                val charEnd = tryScanCharLiteralEnd(i)
                if (charEnd != null) {
                    i = charEnd
                    continue
                }
            }
            when (ch) {
                open -> depth++
                close -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return i
    }

    private fun emitGroup(out: TokenStream) {
        val open = template[pos]
        val (delimiter, close) =
            when (open) {
                '(' -> Delimiter.Parenthesis to ')'
                '[' -> Delimiter.Bracket to ']'
                '{' -> Delimiter.Brace to '}'
                else -> return
            }
        val closePos = findMatchingDelimiter(pos, open, close)
        val innerStart = pos + 1
        val innerEnd = closePos
        pos = if (closePos < template.length) closePos + 1 else template.length

        val innerStream = TokenStream.new()
        val innerTemplate = template.substring(innerStart, innerEnd)
        QuoteParser(innerTemplate, interpolations, span).emitInto(innerStream)

        val group = Group(delimiter, innerStream)
        group.setSpan(span)
        out.append(TokenTree.Group(group))
    }

    // -- Raw identifier -----------------------------------------------------

    private fun emitRawIdent(out: TokenStream) {
        pos += 2
        val name = readIdentName()
        if (name.isNotEmpty()) {
            val ident = Ident.newRaw(name, span)
            out.append(TokenTree.Ident(ident))
        }
    }

    // -- Quoted lifetime tokens ---------------------------------------------

    private fun emitQuotedIdent(out: TokenStream) {
        val apostrophe = Punct('\'', Spacing.Joint, span)
        out.append(TokenTree.Punct(apostrophe))
        pos++

        if (pos + 2 < template.length &&
            template[pos] == 'r' &&
            template[pos + 1] == '#' &&
            isIdentStart(template[pos + 2])
        ) {
            pos += 2
            val name = readIdentName()
            if (name.isNotEmpty()) {
                out.append(TokenTree.Ident(Ident.newRaw(name, span)))
            }
            return
        }

        val name = readIdentName()
        if (name.isNotEmpty()) {
            out.append(TokenTree.Ident(mkIdent(name, span)))
        }
    }

    // -- Number literal -----------------------------------------------------

    private fun emitNumberLiteral(out: TokenStream) {
        val start = pos
        val radixPrefix = pos + 1 < template.length && template[pos] == '0' && template[pos + 1] in "boxBOX"
        if (radixPrefix) {
            pos += 2
            while (pos < template.length && (template[pos].isLetterOrDigit() || template[pos] == '_')) {
                pos++
            }
        } else {
            while (pos < template.length && (template[pos].isDigit() || template[pos] == '_')) {
                pos++
            }
            val afterDigitsNext = template.getOrNull(pos + 1)
            if (pos < template.length &&
                template[pos] == '.' &&
                afterDigitsNext != '.' &&
                (afterDigitsNext == null || !isIdentStart(afterDigitsNext))
            ) {
                pos++
                while (pos < template.length && (template[pos].isDigit() || template[pos] == '_')) {
                    pos++
                }
            }
            if (pos < template.length && (template[pos] == 'e' || template[pos] == 'E')) {
                var expCursor = pos + 1
                if (expCursor < template.length && (template[expCursor] == '+' || template[expCursor] == '-')) {
                    expCursor++
                }
                var hasExpDigit = false
                while (expCursor < template.length && (template[expCursor].isDigit() || template[expCursor] == '_')) {
                    if (template[expCursor].isDigit()) {
                        hasExpDigit = true
                    }
                    expCursor++
                }
                if (hasExpDigit) {
                    pos = expCursor
                }
            }
            while (pos < template.length && isIdentPart(template[pos])) {
                pos++
            }
        }
        emitLiteralSlice(out, start, pos)
    }

    // -- Identifier ---------------------------------------------------------

    private fun emitIdent(out: TokenStream) {
        val name = readIdentName()
        if (name.isNotEmpty()) {
            out.append(TokenTree.Ident(mkIdent(name, span)))
        }
    }

    private fun readIdentName(): String {
        val start = pos
        while (pos < template.length && isIdentPart(template[pos])) {
            pos++
        }
        return template.substring(start, pos)
    }

    private fun isIdentStart(ch: Char): Boolean = ch.isLetter() || ch == '_'

    private fun isIdentPart(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_'
}
