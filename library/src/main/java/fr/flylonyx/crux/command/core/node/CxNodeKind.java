package fr.flylonyx.crux.command.core.node;

/**
 * What a node matches.
 */
public enum CxNodeKind {

    /** Matches one exact word, such as {@code give} in {@code /money give}. */
    LITERAL,

    /** Matches whatever the sender typed, to be read as a value. */
    ARGUMENT
}
