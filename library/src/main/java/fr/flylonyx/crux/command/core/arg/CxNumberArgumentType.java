package fr.flylonyx.crux.command.core.arg;

import java.util.function.Function;
import java.util.regex.Pattern;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * Reads a number, optionally within bounds.
 *
 * <p>One class covers every numeric type: only how the token is read and which message
 * names the failure differ. Bounds are checked after reading, so a sender who typed a
 * number is told it is out of range rather than that it is not a number.
 *
 * @param <N> the number produced
 */
final class CxNumberArgumentType<N extends Comparable<N>> implements CxArgumentType<N> {

    /**
     * The decimal forms accepted, which are narrower than {@link Double#parseDouble}.
     *
     * <p>That method also reads hexadecimal literals, a trailing {@code d} or {@code f},
     * and the words {@code NaN} and {@code Infinity}. None of those are what a sender
     * typing an amount into chat meant.
     */
    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");

    private final String id;
    private final CxKey invalid;
    private final Function<String, N> reader;
    private final N min;
    private final N max;

    CxNumberArgumentType(final String id, final CxKey invalid, final Function<String, N> reader) {
        this(id, invalid, reader, null, null);
    }

    CxNumberArgumentType(final String id,
                         final CxKey invalid,
                         final Function<String, N> reader,
                         final N min,
                         final N max) {

        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new CxDefinitionException("The " + id + " argument type has a minimum of " + min
                    + " above its maximum of " + max + ", so no value could ever be valid.");
        }
        this.id = id;
        this.invalid = invalid;
        this.reader = reader;
        this.min = min;
        this.max = max;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public N parse(final CxInput input, final CxSender sender) throws CxParseException {
        final N value = this.read(input);

        if (this.min != null && value.compareTo(this.min) < 0) {
            throw CxParseException.of(CxKey.NUMBER_TOO_LOW, input).with("min", String.valueOf(this.min));
        }
        if (this.max != null && value.compareTo(this.max) > 0) {
            throw CxParseException.of(CxKey.NUMBER_TOO_HIGH, input).with("max", String.valueOf(this.max));
        }
        return value;
    }

    /** Reads a decimal, rejecting the forms the platform would otherwise accept. */
    static double readDouble(final String token) {
        return requireFinite(Double.parseDouble(requireDecimal(token)));
    }

    /** Reads a decimal of single precision, rejecting anything {@link #readDouble} would. */
    static float readFloat(final String token) {
        return (float) requireFinite(Float.parseFloat(requireDecimal(token)));
    }

    private N read(final CxInput input) throws CxParseException {
        try {
            return this.reader.apply(input.first());
        } catch (final NumberFormatException rejected) {
            throw CxParseException.of(this.invalid, input);
        }
    }

    private static String requireDecimal(final String token) {
        if (!DECIMAL.matcher(token).matches()) {
            throw new NumberFormatException("Not a decimal number: " + token);
        }
        return token;
    }

    private static double requireFinite(final double value) {
        if (!Double.isFinite(value)) {
            throw new NumberFormatException("Not a finite number: " + value);
        }
        return value;
    }
}
