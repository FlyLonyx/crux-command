package fr.flylonyx.crux.command.core.arg;

/**
 * The argument types the library ships with.
 *
 * <p>All of them are immutable and safe to share between commands, so the unbounded ones
 * are handed out as singletons.
 *
 * <pre>{@code
 * CxNodeBuilder.argument("amount", CxArgumentTypes.integer(1, 64))
 * }</pre>
 */
public final class CxArgumentTypes {

    private static final CxArgumentType<String> STRING = new CxTextArgumentType("string", 1, false);
    private static final CxArgumentType<String> WORD = new CxTextArgumentType("word", 1, true);
    private static final CxArgumentType<String> GREEDY_STRING =
            new CxTextArgumentType("greedy-string", CxArgumentType.ARITY_GREEDY, false);

    private CxArgumentTypes() {
    }

    /**
     * Returns a type reading one token, whatever it holds.
     *
     * <p>A quoted phrase counts as one token, so this accepts {@code "hello there"}.
     *
     * @return the type
     */
    public static CxArgumentType<String> string() {
        return STRING;
    }

    /**
     * Returns a type reading one token that holds no whitespace.
     *
     * <p>The right choice for names and identifiers, where a quoted phrase would be a
     * mistake rather than a longer value.
     *
     * @return the type
     */
    public static CxArgumentType<String> word() {
        return WORD;
    }

    /**
     * Returns a type reading every remaining token, joined by a single space.
     *
     * <p>Nothing can follow a greedy argument, so it is always the last one.
     *
     * @return the type
     */
    public static CxArgumentType<String> greedyString() {
        return GREEDY_STRING;
    }
}
