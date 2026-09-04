package fr.flylonyx.crux.command.core.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The result of tokenising a command line.
 *
 * <p>Holds the tokens themselves and whether the raw input ended on a separator. That
 * second piece of information matters for tab completion: {@code /money give} and
 * {@code /money give } produce the same tokens but call for different suggestions, the
 * first completing {@code give} and the second offering the next argument.
 */
public final class CxTokens {

    private static final CxTokens EMPTY = new CxTokens(Collections.<String>emptyList(), false);

    private final List<String> values;
    private final boolean trailingSeparator;

    private CxTokens(final List<String> values, final boolean trailingSeparator) {
        this.values = values;
        this.trailingSeparator = trailingSeparator;
    }

    /**
     * Creates a token list.
     *
     * @param values            the tokens, copied defensively; must not be {@code null}
     * @param trailingSeparator whether the raw input ended on an unquoted separator
     * @return the token list
     */
    public static CxTokens of(final List<String> values, final boolean trailingSeparator) {
        Objects.requireNonNull(values, "values");
        if (values.isEmpty() && !trailingSeparator) {
            return EMPTY;
        }
        return new CxTokens(Collections.unmodifiableList(new ArrayList<String>(values)), trailingSeparator);
    }

    /**
     * Returns the tokens.
     *
     * @return an unmodifiable list of tokens, in the order they appeared
     */
    public List<String> values() {
        return this.values;
    }

    /**
     * Returns the token at the given position.
     *
     * @param index the zero-based position
     * @return the token
     * @throws IndexOutOfBoundsException if the index is outside the token range
     */
    public String get(final int index) {
        return this.values.get(index);
    }

    /**
     * Returns how many tokens were produced.
     *
     * @return the token count
     */
    public int size() {
        return this.values.size();
    }

    /**
     * Reports whether any token was produced.
     *
     * @return {@code true} if there are no tokens
     */
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    /**
     * Reports whether the raw input ended on an unquoted separator.
     *
     * @return {@code true} if the sender had already finished the last token
     */
    public boolean trailingSeparator() {
        return this.trailingSeparator;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CxTokens)) {
            return false;
        }
        final CxTokens that = (CxTokens) other;
        return this.trailingSeparator == that.trailingSeparator && this.values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return 31 * this.values.hashCode() + (this.trailingSeparator ? 1 : 0);
    }

    @Override
    public String toString() {
        return "CxTokens" + this.values + (this.trailingSeparator ? "+" : "");
    }
}
