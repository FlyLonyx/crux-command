package fr.flylonyx.crux.command.core.node;

import java.util.List;
import java.util.Objects;

/**
 * The line that shows a sender the shape of a command.
 *
 * <p>{@code /money give <target> [amount]}: the label the command was typed under, the path
 * taken through the tree, and whatever still has to follow before anything would run.
 */
public final class CxUsage {

    private static final char SEPARATOR = ' ';

    private CxUsage() {
    }

    /**
     * Builds the usage line for a path through the tree.
     *
     * <p>The label comes from the sender rather than the root node, so someone who typed
     * {@code /bal} is shown {@code /bal} and not the name the command was registered under.
     *
     * @param label the alias the command was typed under, without the leading slash
     * @param path  the nodes reached, root first
     * @return the usage line, starting with a slash
     * @throws IllegalArgumentException if the path is empty
     */
    public static String of(final String label, final List<CxNode> path) {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(path, "path");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("A usage line needs at least the node the command was registered "
                    + "under.");
        }

        final StringBuilder usage = new StringBuilder("/").append(label);
        for (int index = 1; index < path.size(); index++) {
            usage.append(SEPARATOR).append(path.get(index).token());
        }
        appendRemainder(usage, path.get(path.size() - 1));
        return usage.toString();
    }

    /**
     * Adds what would still have to be typed for the command to run.
     *
     * <p>Where a node offers several branches the first is taken: a usage line is one
     * correct form of the command, not the list of them.
     */
    private static void appendRemainder(final StringBuilder usage, final CxNode reached) {
        CxNode node = reached;
        while (!node.isExecutable()) {
            node = firstChild(node);
            usage.append(SEPARATOR).append(node.token());
        }
    }

    /**
     * Returns the branch a usage line follows out of a node.
     *
     * <p>Building the tree rejects a node with neither a handler nor children, so a node
     * that cannot run always has one.
     */
    private static CxNode firstChild(final CxNode node) {
        return node.literals().isEmpty() ? node.arguments().get(0) : node.literals().get(0);
    }
}
