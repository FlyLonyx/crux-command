package fr.flylonyx.crux.command.core;

/**
 * Thrown when a command is declared in a way that cannot work.
 *
 * <p>Clashing literals, an argument without a type, a branch leading nowhere: none of
 * these depend on what a sender types, so all are rejected at build time rather than at
 * dispatch. Messages name the offending node.
 */
public class CxDefinitionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what is wrong with the declaration, naming the node responsible
     */
    public CxDefinitionException(final String message) {
        super(message);
    }
}
