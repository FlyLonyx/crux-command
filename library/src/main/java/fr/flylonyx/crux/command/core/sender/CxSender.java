package fr.flylonyx.crux.command.core.sender;

/**
 * Whoever ran a command, seen from the engine.
 *
 * <p>Exposes only the four things the engine actually needs. Keeping it this narrow is
 * what allows routing, permission checks, usage generation and tab completion to be
 * exercised in plain unit tests, with no server and no mocking framework.
 *
 * <p>The Bukkit adapter provides the implementation, and is the only layer permitted to
 * turn one of these back into a concrete server type.
 */
public interface CxSender {

    /**
     * Returns the display name of the sender.
     *
     * @return the sender name, never {@code null}
     */
    String name();

    /**
     * Returns what kind of sender this is.
     *
     * @return the sender type, never {@code null}
     */
    CxSenderType type();

    /**
     * Tests whether the sender holds a permission.
     *
     * @param permission the permission node to test
     * @return {@code true} if the sender holds it
     */
    boolean hasPermission(String permission);

    /**
     * Sends a message to the sender.
     *
     * @param message the message, already resolved and formatted
     */
    void send(String message);
}
