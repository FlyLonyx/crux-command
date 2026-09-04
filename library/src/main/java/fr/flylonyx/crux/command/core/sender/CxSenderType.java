package fr.flylonyx.crux.command.core.sender;

/**
 * What kind of thing ran a command.
 *
 * <p>Commands are restricted by comparing against this rather than by testing for a
 * concrete server class, which is what lets the engine enforce the restriction without
 * knowing anything about the platform.
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
