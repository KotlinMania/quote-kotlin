// port-lint: source src/ident_fragment.rs
package io.github.kotlinmania.quote

import io.github.kotlinmania.procmacro2.Ident
import io.github.kotlinmania.procmacro2.Span

/**
 * Specialized formatting interface used by `formatIdent`.
 *
 * [Ident] arguments formatted using this interface have their raw-prefix
 * stripped, if present.
 *
 * See `formatIdent` for more information.
 */
public interface IdentFragment {
    /** Format this value as an identifier fragment. */
    public fun formatIdentFragment(): String

    /**
     * Span associated with this [IdentFragment].
     *
     * If non-null, this may be inherited by formatted identifiers.
     */
    public fun span(): Span? = null
}

private class DisplayIdentFragment(
    private val text: String,
    private val span: Span? = null,
) : IdentFragment {
    override fun formatIdentFragment(): String = text

    override fun span(): Span? = span
}

/**
 * Adapter matching the upstream helper that lets formatting code inspect both
 * the rendered fragment and any span inherited from the original argument.
 */
public class IdentFragmentAdapter(
    value: Any?,
) : IdentFragment {
    private val fragment: IdentFragment =
        when (value) {
            is IdentFragment -> value
            is Ident -> DisplayIdentFragment(value.toString().removePrefix("r#"), value.span())
            is Boolean -> DisplayIdentFragment(value.toString())
            is String -> DisplayIdentFragment(value)
            is Char -> DisplayIdentFragment(value.toString())
            is UByte -> DisplayIdentFragment(value.toString())
            is UShort -> DisplayIdentFragment(value.toString())
            is UInt -> DisplayIdentFragment(value.toString())
            is ULong -> DisplayIdentFragment(value.toString())
            else -> throw IllegalArgumentException(
                "Unsupported identifier fragment value: $value",
            )
        }

    override fun formatIdentFragment(): String = fragment.formatIdentFragment()

    override fun span(): Span? = fragment.span()
}

/** Format this [Ident] as an identifier fragment, stripping a raw-prefix. */
public fun Ident.formatIdentFragment(): String =
    toString().removePrefix("r#")

/** Return the span associated with this [Ident] fragment. */
public fun Ident.identFragmentSpan(): Span = span()

/** Format a supported primitive value as an identifier fragment. */
public fun formatIdentFragment(value: Any?): String =
    IdentFragmentAdapter(value).formatIdentFragment()

/** Return the span associated with a supported identifier fragment value. */
public fun identFragmentSpan(value: Any?): Span? =
    IdentFragmentAdapter(value).span()
