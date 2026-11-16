package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span

/**
 * Formatting helper for constructing [Ident] values.
 *
 * ### Syntax
 *
 * Supports positional (`{}`, `{0}`) and named (`{name}` via `"name" to value`
 * pairs) arguments, as well as escaped braces (`{{` and `}}`).
 *
 * Only a limited set of formatting specifiers are supported:
 * - `{}` -> [IdentFragment]
 * - `{:o}` / `{:#o}` -> octal
 * - `{:x}` / `{:#x}` -> lowercase hexadecimal
 * - `{:X}` / `{:#X}` -> uppercase hexadecimal
 * - `{:b}` / `{:#b}` -> binary
 *
 * ### IdentFragment
 *
 * By default, arguments are formatted via [IdentFragment]:
 * - Only identifier-safe fragment types (such as integers, booleans, characters,
 *   strings, and identifiers) are accepted.
 * - [Ident] arguments have their `r#` raw prefix stripped, if present.
 *
 * ### Hygiene
 *
 * The [Span] of the first argument with a non-null [IdentFragment.span] (such as
 * an [Ident]) is inherited as the span of the resulting identifier, falling back
 * to [Span.callSite] when no spanned arguments are provided.
 *
 * Alternatively, the span can be overridden explicitly via the [formatIdent]
 * overload accepting a leading [Span], or by passing `"span" to mySpan`.
 *
 * ### Exceptions
 *
 * Throws [IllegalArgumentException] if the resulting formatted string is not a
 * valid identifier or if the format string or arguments are invalid.
 *
 * ### Examples
 *
 * Composing raw and non-raw identifiers:
 * ```kotlin
 * val myIdent = formatIdent("My{}", "Ident")
 * check(myIdent.toString() == "MyIdent")
 *
 * val raw = formatIdent("r#Raw")
 * check(raw.toString() == "r#Raw")
 *
 * val combined = formatIdent("{}Is{}", myIdent, raw)
 * check(combined.toString() == "MyIdentIsRaw")
 * ```
 *
 * Integer formatting options:
 * ```kotlin
 * val num: UInt = 10u
 * check(formatIdent("Id{}", num).toString() == "Id10")
 * check(formatIdent("Id{:o}", num).toString() == "Id12")
 * check(formatIdent("Id{:b}", num).toString() == "Id1010")
 * check(formatIdent("Id{:x}", num).toString() == "Ida")
 * check(formatIdent("Id{:X}", num).toString() == "IdA")
 * ```
 */
public fun formatIdent(fmt: String, vararg args: Any?): Ident =
    formatIdentImpl(explicitSpan = null, fmt = fmt, args = args)

/**
 * Constructs an [Ident] from a format string and arguments, overriding the
 * resulting identifier's span with [span].
 */
public fun formatIdent(span: Span, fmt: String, vararg args: Any?): Ident =
    formatIdentImpl(explicitSpan = span, fmt = fmt, args = args)

private fun formatIdentImpl(
    explicitSpan: Span?,
    fmt: String,
    args: Array<out Any?>,
): Ident {
    var spanOverride: Span? = explicitSpan
    var inheritedSpan: Span? = null
    val positional = mutableListOf<IdentFragmentAdapter>()
    val named = mutableMapOf<String, IdentFragmentAdapter>()

    for (rawArg in args) {
        if (rawArg is Pair<*, *> && rawArg.first is String) {
            val key = rawArg.first as String
            val value = rawArg.second
            if (key == "span" && value is Span) {
                spanOverride = value
            } else {
                val adapter = IdentFragmentAdapter(value)
                if (inheritedSpan == null) {
                    inheritedSpan = adapter.span()
                }
                named[key] = adapter
            }
        } else {
            val adapter = IdentFragmentAdapter(rawArg)
            if (inheritedSpan == null) {
                inheritedSpan = adapter.span()
            }
            positional.add(adapter)
        }
    }

    val builder = StringBuilder()
    var nextPositional = 0
    var i = 0
    while (i < fmt.length) {
        val ch = fmt[i]
        if (ch == '{') {
            if (i + 1 < fmt.length && fmt[i + 1] == '{') {
                builder.append('{')
                i += 2
                continue
            }
            val closeIdx = fmt.indexOf('}', i + 1)
            require(closeIdx != -1) {
                "formatIdent: unclosed '{' in format string \"$fmt\""
            }
            val directive = fmt.substring(i + 1, closeIdx)
            val colonIdx = directive.indexOf(':')
            val selector = if (colonIdx == -1) directive else directive.substring(0, colonIdx)
            val spec = if (colonIdx == -1) "" else directive.substring(colonIdx + 1)

            val adapter =
                when {
                    selector.isEmpty() -> {
                        require(nextPositional < positional.size) {
                            "formatIdent: not enough arguments for format string \"$fmt\""
                        }
                        positional[nextPositional++]
                    }
                    selector.all { it.isDigit() } -> {
                        val idx = selector.toInt()
                        require(idx in positional.indices) {
                            "formatIdent: positional index $idx out of bounds for \"$fmt\""
                        }
                        positional[idx]
                    }
                    else -> {
                        named[selector] ?: throw IllegalArgumentException(
                            "formatIdent: missing named argument '$selector' for format string \"$fmt\"",
                        )
                    }
                }

            when (spec) {
                "" -> adapter.fmt(builder)
                "o" -> adapter.fmtOctal(builder, alternate = false)
                "#o" -> adapter.fmtOctal(builder, alternate = true)
                "x" -> adapter.fmtLowerHex(builder, alternate = false)
                "#x" -> adapter.fmtLowerHex(builder, alternate = true)
                "X" -> adapter.fmtUpperHex(builder, alternate = false)
                "#X" -> adapter.fmtUpperHex(builder, alternate = true)
                "b" -> adapter.fmtBinary(builder, alternate = false)
                "#b" -> adapter.fmtBinary(builder, alternate = true)
                else -> throw IllegalArgumentException(
                    "formatIdent: unsupported format specifier ':$spec' in \"$fmt\"",
                )
            }
            i = closeIdx + 1
        } else if (ch == '}') {
            require(i + 1 < fmt.length && fmt[i + 1] == '}') {
                "formatIdent: unmatched '}' in format string \"$fmt\""
            }
            builder.append('}')
            i += 2
        } else {
            builder.append(ch)
            i++
        }
    }

    val resolvedSpan = spanOverride ?: inheritedSpan ?: Span.callSite()
    return mkIdent(builder.toString(), resolvedSpan)
}
