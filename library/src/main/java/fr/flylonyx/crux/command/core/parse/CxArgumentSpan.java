package fr.flylonyx.crux.command.core.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.flylonyx.crux.command.core.node.CxNode;

/**
 * Which tokens an argument node claimed while routing.
 *
 * <p>Routing decides the shape of a command; reading the values comes after. Recording the
 * span rather than the value keeps those two concerns apart, so the tree can be walked and
 * tested without any argument type having to parse anything.
 */
public final class CxArgumentSpan {

    private final CxNode node;
    private final int first;
    private final int count;

    /**
     * Creates a span.
     *
     * @param node  the argument node that claimed the tokens
     * @param first the index of the first token claimed
     * @param count how many tokens were claimed
     */
    public CxArgumentSpan(final CxNode node, final int first, final int count) {
        this.node = Objects.requireNonNull(node, "node");
        if (first < 0) {
            throw new IllegalArgumentException("first must not be negative, was " + first);
        }
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least one, was " + count);
        }
        this.first = first;
        this.count = count;
    }

    /**
     * Returns the argument node that claimed the tokens.
     *
     * @return the node
     */
    public CxNode node() {
        return this.node;
    }

    /**
     * Returns the index of the first token claimed.
     *
     * @return the first index
     */
    public int first() {
        return this.first;
    }

    /**
     * Returns how many tokens were claimed.
     *
     * @return the token count
     */
    public int count() {
        return this.count;
    }

    /**
     * Extracts the claimed tokens.
     *
     * @param tokens the tokens the span refers to
     * @return an unmodifiable list of the claimed tokens
     * @throws IndexOutOfBoundsException if the span falls outside the given tokens
     */
    public List<String> extractFrom(final CxTokens tokens) {
        Objects.requireNonNull(tokens, "tokens");
        final List<String> claimed = new ArrayList<String>(this.count);
        for (int offset = 0; offset < this.count; offset++) {
            claimed.add(tokens.get(this.first + offset));
        }
        return Collections.unmodifiableList(claimed);
    }

    @Override
    public String toString() {
        return this.node.name() + "[" + this.first + ".." + (this.first + this.count - 1) + "]";
    }
}
