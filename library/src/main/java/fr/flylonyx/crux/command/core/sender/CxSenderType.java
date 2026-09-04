package fr.flylonyx.crux.command.core.sender;

/**
 * What kind of thing ran a command.
 *
 * <p>Restrictions compare against this rather than against a concrete server class.
 */
public enum CxSenderType {

    /** A player in the world. */
    PLAYER,

    /** The server console. */
    CONSOLE,

    /** A command block. */
    BLOCK,

    /** Anything else, including senders introduced by other plugins. */
    OTHER
}
