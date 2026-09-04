package fr.flylonyx.crux.command.core.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * The outcome of routing a command line through a tree.
 *
 * <p>Either a node was reached that can run, or nothing matched. A failure records how far
 * routing got, so {@code /money give Notch} reports a missing amount rather than an
 * unknown sub-command.
 */
public final class CxMatchResult {

    private final CxNode node;
    private final List<CxArgumentSpan> arguments;
    private final CxKey failure;
    private final String detail;
    private final int depth;

    private CxMatchResult(final CxNode node,
                          final List<CxArgumentSpan> arguments,
                          final CxKey failure,
                          final String detail,
                          final int depth) {
        this.node = node;
        this.arguments = arguments;
        this.failure = failure;
        this.detail = detail;
        this.depth = depth;
    }

    /**
     * Records a successful match.
     *
     * @param node      the node whose handler should run
     * @param arguments the spans claimed along the way, in order
     * @return the result
     */
    public static CxMatchResult matched(final CxNode node, final List<CxArgumentSpan> arguments) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(arguments, "arguments");
        return new CxMatchResult(node, new ArrayList<CxArgumentSpan>(arguments), null, null, 0);
    }

    /**
     * Records a failure.
     *
     * @param failure what went wrong
     * @param detail  the offending token, or the name of the argument that was missing
     * @param depth   how many tokens routing consumed before giving up
     * @return the result
     */
    public static CxMatchResult failed(final CxKey failure, final String detail, final int depth) {
        Objects.requireNonNull(failure, "failure");
        return new CxMatchResult(null, Collections.<CxArgumentSpan>emptyList(), failure, detail, depth);
    }

    /**
     * Reports whether a runnable node was reached.
     *
     * @return {@code true} on success
     */
    public boolean isMatched() {
        return this.node != null;
    }

    /**
     * Returns the node whose handler should run.
     *
     * @return the matched node
     * @throws IllegalStateException if routing failed
     */
    public CxNode node() {
        if (this.node == null) {
            throw new IllegalStateException("Routing failed with " + this.failure + "; there is no matched node.");
        }
        return this.node;
    }

    /**
     * Returns the argument spans claimed along the matched path.
     *
     * @return an unmodifiable list of spans, in the order the arguments appear
     */
    public List<CxArgumentSpan> arguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    /**
     * Returns what went wrong.
     *
     * @return the failure key
     * @throws IllegalStateException if routing succeeded
     */
    public CxKey failure() {
        if (this.failure == null) {
            throw new IllegalStateException("Routing succeeded; there is no failure.");
        }
        return this.failure;
    }

    /**
     * Returns the offending token, or the name of the argument that was missing.
     *
     * @return the detail, or {@code null} if the failure needs none
     */
    public String detail() {
        return this.detail;
    }

    /**
     * Returns how many tokens routing consumed before giving up.
     *
     * @return the depth reached, zero on success
     */
    public int depth() {
        return this.depth;
    }

    /**
     * Reports whether this failure is more informative than another.
     *
     * <p>The failure that got furthest describes the sender's actual mistake; shallower
     * ones are usually just branches that were never going to match.
     *
     * @param other the failure to compare against, may be {@code null}
     * @return {@code true} if this one should be reported instead
     */
    boolean isMoreInformativeThan(final CxMatchResult other) {
        return other == null || this.depth > other.depth;
    }

    @Override
    public String toString() {
        return this.isMatched()
                ? "matched " + this.node + " with " + this.arguments
                : "failed " + this.failure + " at " + this.depth + (this.detail == null ? "" : " on " + this.detail);
    }
}
