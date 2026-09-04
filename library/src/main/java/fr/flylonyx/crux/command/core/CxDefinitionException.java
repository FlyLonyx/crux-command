package fr.flylonyx.crux.command.core;

/**
 * Thrown when a command is declared in a way that cannot work.
 *
 * <p>Two literal branches sharing a name, an argument without a type, a branch that leads
 * nowhere: none of these can produce sensible behaviour, and none of them depend on what a
 * sender types. So they are rejected when the command is built, at server startup, rather
 * than surfacing as a confusing failure the first time somebody runs the command.
 *
 * <p>Messages name the offending node so the author knows where to look.
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
