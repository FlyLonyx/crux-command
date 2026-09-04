package fr.flylonyx.crux.command.core.node;

import java.util.List;
import java.util.Map;

/**
 * The children of a node, already validated and ordered by the builder.
 *
 * <p>Exists so that {@link CxNode} can be constructed without exceeding a sane parameter
 * count, and so that the ordering and lookup rules live in one place rather than being
 * re-derived by every consumer.
 */
final class CxNodeChildren {

    private final Map<String, CxNode> lookup;
    private final List<CxNode> literals;
    private final List<CxNode> arguments;

    CxNodeChildren(final Map<String, CxNode> lookup,
                   final List<CxNode> literals,
                   final List<CxNode> arguments) {
        this.lookup = lookup;
        this.literals = literals;
        this.arguments = arguments;
    }

    Map<String, CxNode> lookup() {
        return this.lookup;
    }

    List<CxNode> literals() {
        return this.literals;
    }

    List<CxNode> arguments() {
        return this.arguments;
    }
}
