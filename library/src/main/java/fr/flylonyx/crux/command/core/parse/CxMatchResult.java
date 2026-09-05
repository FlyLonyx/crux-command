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
 *
 * <p>Both outcomes carry the path taken through the tree, which is what a usage line is
 * built from: a failure has to say what the sender should have typed.
 */
public final class CxMatchResult {

    private final List<CxNode> path;
    private final List<CxArgumentSpan> arguments;
    private final CxKey failure;
    private final String detail;
    private final int depth;

    private CxMatchResult(final List<CxNode> path,
                          final List<CxArgumentSpan> arguments,
                          final CxKey failure,
                          final String detail,
                          final int depth) {

        this.path = path;
        this.arguments = arguments;
        this.failure = failure;
        this.detail = detail;
        this.depth = depth;
    }

    /**
     * Records a successful match.
     *
     * @param path      the nodes walked through, root first, ending on the one to run
     * @param arguments the spans claimed along the way, in order
     * @return the result
     * @throws IllegalArgumentException if the path is empty
     */
    public static CxMatchResult matched(final List<CxNode> path, final List<CxArgumentSpan> arguments) {
        Objects.requireNonNull(arguments, "arguments");
        return new CxMatchResult(copyOfPath(path), new ArrayList<>(arguments), null, null, 0);
    }

    /**
     * Records a failure.
     *
     * @param failure what went wrong
     * @param detail  the offending token, or the name of the argument that was missing
     * @param depth   how many tokens routing consumed before giving up
     * @param path    the nodes walked through, root first, ending where routing gave up
     * @return the result
     * @throws IllegalArgumentException if the path is empty
     */
    public static CxMatchResult failed(final CxKey failure,
                                       final String detail,
                                       final int depth,
                                       final List<CxNode> path) {

        Objects.requireNonNull(failure, "failure");
        return new CxMatchResult(copyOfPath(path), Collections.emptyList(), failure, detail, depth);
    }

    /**
     * Reports whether a runnable node was reached.
     *
     * @return {@code true} on success
     */
    public boolean isMatched() {
        return this.failure == null;
    }

    /**
     * Returns the node whose handler should run.
     *
     * @return the matched node
     * @throws IllegalStateException if routing failed
     */
    public CxNode node() {
        if (this.failure != null) {
            throw new IllegalStateException("Routing failed with " + this.failure + "; there is no matched node.");
        }
        return this.path.get(this.path.size() - 1);
    }

    /**
     * Returns the nodes routing walked through, root first.
     *
     * <p>On success it ends on the node to run, on failure on the one routing gave up at.
     *
     * @return an unmodifiable list holding at least the root
     */
    public List<CxNode> path() {
        return Collections.unmodifiableList(this.path);
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

    private static List<CxNode> copyOfPath(final List<CxNode> path) {
        Objects.requireNonNull(path, "path");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("A path holds at least the node the command was registered under.");
        }
        return new ArrayList<>(path);
    }

    @Override
    public String toString() {
        return this.isMatched()
                ? "matched " + this.node() + " with " + this.arguments
                : "failed " + this.failure + " at " + this.depth + (this.detail == null ? "" : " on " + this.detail);
    }
}
