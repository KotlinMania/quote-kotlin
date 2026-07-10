// port-lint: source format.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span

/**
 * Constructs an [Ident] from a format string and arguments, analogous to
 * Rust's `format_ident!` macro.
 *
 * The format string uses `{}` placeholders that are replaced by the string
 * representation of the corresponding argument. Arguments implementing
 * [IdentFragment] have their `r#` prefix stripped (if present); other
 * arguments are converted via [toString].
 *
 * The span of the first [Ident] argument is inherited by the result.
 * If no [Ident] argument is provided, [Span.callSite] is used.
 *
 * Panics if the resulting string is not a valid identifier.
 */
public fun formatIdent(fmt: String, vararg args: Any?): Ident {
    var span: Span? = null
    val builder = StringBuilder()

    var argIndex = 0
    var i = 0
    while (i < fmt.length) {
        val ch = fmt[i]
        if (ch == '{' && i + 1 < fmt.length && fmt[i + 1] == '}') {
            require(argIndex < args.size) {
                "formatIdent: not enough arguments for format string \"$fmt\""
            }
            val arg = args[argIndex]
            val (text, argSpan) = fragmentOf(arg)
            if (span == null && argSpan != null) {
                span = argSpan
            }
            builder.append(text)
            argIndex++
            i += 2
        } else if (ch == '{' && i + 1 < fmt.length && fmt[i + 1] == ':') {
            require(i + 3 < fmt.length && fmt[i + 3] == '}') {
                "formatIdent: unsupported format specifier in \"$fmt\""
            }
            val spec = fmt[i + 2]
            require(spec == 'o' || spec == 'x' || spec == 'X' || spec == 'b') {
                "formatIdent: unsupported format specifier in \"$fmt\""
            }
            require(argIndex < args.size) {
                "formatIdent: not enough arguments for format string \"$fmt\""
            }
            val arg = args[argIndex]
            val (text, argSpan) = fragmentOf(arg, spec)
            if (span == null && argSpan != null) {
                span = argSpan
            }
            builder.append(text)
            argIndex++
            i += 4
        } else {
            builder.append(ch)
            i++
        }
    }

    val result = builder.toString()
    val resolvedSpan = span ?: Span.callSite()
    return mkIdent(result, resolvedSpan)
}

/**
 * Construct an [Ident] from a string, handling `r#` raw prefixes.
 */
internal fun mkIdent(id: String, span: Span): Ident {
    return if (id.startsWith("r#")) {
        Ident.newRaw(id.substring(2), span)
    } else {
        Ident.new(id, span)
    }
}

private fun fragmentOf(arg: Any?, spec: Char? = null): Pair<String, Span?> {
    return when (arg) {
        is IdentFragment -> {
            val text = arg.formatIdentFragment()
            Pair(text, arg.span())
        }
        is Ident -> {
            val text = arg.toString().removePrefix("r#")
            Pair(text, arg.span())
        }
        is UInt -> {
            val text = when (spec) {
                'o' -> arg.toString(8)
                'x' -> arg.toString(16)
                'X' -> arg.toString(16).uppercase()
                'b' -> arg.toString(2)
                else -> arg.toString()
            }
            Pair(text, null)
        }
        is ULong -> {
            val text = when (spec) {
                'o' -> arg.toString(8)
                'x' -> arg.toString(16)
                'X' -> arg.toString(16).uppercase()
                'b' -> arg.toString(2)
                else -> arg.toString()
            }
            Pair(text, null)
        }
        is Int -> {
            val text = when (spec) {
                'o' -> arg.toString(8)
                'x' -> arg.toString(16)
                'X' -> arg.toString(16).uppercase()
                'b' -> arg.toString(2)
                else -> arg.toString()
            }
            Pair(text, null)
        }
        is Long -> {
            val text = when (spec) {
                'o' -> arg.toString(8)
                'x' -> arg.toString(16)
                'X' -> arg.toString(16).uppercase()
                'b' -> arg.toString(2)
                else -> arg.toString()
            }
            Pair(text, null)
        }
        is String -> Pair(arg, null)
        is Char -> Pair(arg.toString(), null)
        is Boolean -> Pair(arg.toString(), null)
        else -> Pair(arg.toString(), null)
    }
}
