// port-lint: source ident_fragment.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span

/**
 * Specialized formatting interface used by [formatIdent].
 *
 * [Ident] arguments formatted using this interface have their raw prefix
 * stripped, if present.
 *
 * See [formatIdent] for more information.
 */
public interface IdentFragment {
    /** Format this value as an identifier fragment. */
    public fun formatIdentFragment(): String

    /** Format this value as an identifier fragment into [formatter]. */
    public fun fmt(formatter: Appendable): Appendable =
        formatter.append(formatIdentFragment())

    /** Format this value as an identifier fragment string. */
    public fun fmt(): String = formatIdentFragment()

    /**
     * Span associated with this [IdentFragment].
     *
     * If non-null, this may be inherited by formatted identifiers.
     */
    public fun span(): Span? = null
}

/** Format this [Ident] as an identifier fragment, stripping a raw prefix. */
public fun Ident.formatIdentFragment(): String =
    toString().removePrefix("r#")

/** Return the span associated with this [Ident] fragment. */
public fun Ident.identFragmentSpan(): Span = span()

/** Format this [Ident] into [formatter], stripping a raw prefix if present. */
public fun Ident.fmt(formatter: Appendable): Appendable {
    val id = toString()
    val stripped = id.removePrefix("r#")
    return formatter.append(stripped)
}

// Limited set of types for identifier fragment formatting, avoiding types
// whose string representations commonly include non-identifier characters.

public fun Boolean.fmt(formatter: Appendable): Appendable =
    formatter.append(toString())

public fun String.fmt(formatter: Appendable): Appendable =
    formatter.append(this)

public fun Char.fmtChar(formatter: Appendable): Appendable =
    formatter.append(this)

public fun UByte.fmt(formatter: Appendable): Appendable =
    formatter.append(toString())

public fun UShort.fmt(formatter: Appendable): Appendable =
    formatter.append(toString())

public fun UInt.fmt(formatter: Appendable): Appendable =
    formatter.append(toString())

public fun ULong.fmt(formatter: Appendable): Appendable =
    formatter.append(toString())

/** Format a supported primitive value as an identifier fragment. */
public fun formatIdentFragment(value: Any?): String =
    IdentFragmentAdapter(value).formatIdentFragment()

/** Return the span associated with a supported identifier fragment value. */
public fun identFragmentSpan(value: Any?): Span? =
    IdentFragmentAdapter(value).span()
