package fr.flylonyx.crux.command.core.arg;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.message.CxKey;

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
    private static final CxArgumentType<Integer> INTEGER =
            new CxNumberArgumentType<>("int", CxKey.INVALID_NUMBER, Integer::parseInt);
    private static final CxArgumentType<Long> LONG =
            new CxNumberArgumentType<>("long", CxKey.INVALID_NUMBER, Long::parseLong);
    private static final CxArgumentType<Float> FLOAT =
            new CxNumberArgumentType<>("float", CxKey.INVALID_DECIMAL, CxNumberArgumentType::readFloat);
    private static final CxArgumentType<Double> DOUBLE =
            new CxNumberArgumentType<>("double", CxKey.INVALID_DECIMAL, CxNumberArgumentType::readDouble);

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

    /**
     * Returns a type reading a whole number.
     *
     * @return the type
     */
    public static CxArgumentType<Integer> integer() {
        return INTEGER;
    }

    /**
     * Returns a type reading a whole number within bounds, both ends included.
     *
     * @param min the lowest value accepted
     * @param max the highest value accepted
     * @return the type
     * @throws CxDefinitionException if the bounds exclude every value
     */
    public static CxArgumentType<Integer> integer(final int min, final int max) {
        return new CxNumberArgumentType<>("int", CxKey.INVALID_NUMBER, Integer::parseInt, min, max);
    }

    /**
     * Returns a type reading a whole number of the wider range.
     *
     * @return the type
     */
    public static CxArgumentType<Long> longNumber() {
        return LONG;
    }

    /**
     * Returns a type reading a whole number of the wider range within bounds, both ends included.
     *
     * @param min the lowest value accepted
     * @param max the highest value accepted
     * @return the type
     * @throws CxDefinitionException if the bounds exclude every value
     */
    public static CxArgumentType<Long> longNumber(final long min, final long max) {
        return new CxNumberArgumentType<>("long", CxKey.INVALID_NUMBER, Long::parseLong, min, max);
    }

    /**
     * Returns a type reading a number of single precision.
     *
     * @return the type
     */
    public static CxArgumentType<Float> floatNumber() {
        return FLOAT;
    }

    /**
     * Returns a type reading a number of single precision within bounds, both ends included.
     *
     * @param min the lowest value accepted
     * @param max the highest value accepted
     * @return the type
     * @throws CxDefinitionException if the bounds exclude every value
     */
    public static CxArgumentType<Float> floatNumber(final float min, final float max) {
        return new CxNumberArgumentType<>("float", CxKey.INVALID_DECIMAL, CxNumberArgumentType::readFloat, min, max);
    }

    /**
     * Returns a type reading a number, with or without a decimal part.
     *
     * @return the type
     */
    public static CxArgumentType<Double> doubleNumber() {
        return DOUBLE;
    }

    /**
     * Returns a type reading a number within bounds, both ends included.
     *
     * @param min the lowest value accepted
     * @param max the highest value accepted
     * @return the type
     * @throws CxDefinitionException if the bounds exclude every value
     */
    public static CxArgumentType<Double> doubleNumber(final double min, final double max) {
        return new CxNumberArgumentType<>("double", CxKey.INVALID_DECIMAL, CxNumberArgumentType::readDouble, min, max);
    }
}
