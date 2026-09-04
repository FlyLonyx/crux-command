package fr.flylonyx.crux.command.core.node;

import fr.flylonyx.crux.command.core.CxContext;

/**
 * What runs when a command reaches a node.
 *
 * <p>A node without a handler is a branch: {@code /money} is not runnable on its own if
 * only {@code /money give} and {@code /money top} do something.
 */
@FunctionalInterface
public interface CxHandler {

    /**
     * Runs the command.
     *
     * @param context who ran it, under which label, and what they typed
     */
    void execute(CxContext context);
}
