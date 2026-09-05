package fr.flylonyx.crux.command.core.arg;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import fr.flylonyx.crux.command.message.CxKey;

/**
 * Thrown when the tokens an argument claimed do not describe a valid value.
 *
 * <p>Checked, unlike {@link fr.flylonyx.crux.command.core.CxDefinitionException}: a
 * malformed declaration is a bug to fix, while bad input is expected and every caller has
 * to answer for it.
 *
 * <p>Carries a message key and its placeholders rather than finished text, so the wording
 * stays configurable. No stack trace is recorded; this reports what a sender typed, not
 * where the library went wrong.
 */
public final class CxParseException extends Exception {

    private static final long serialVersionUID = 1L;

    private final CxKey key;
    private final Map<String, String> placeholders;

    private CxParseException(final CxKey key, final Map<String, String> placeholders) {
        super(key.configKey() + " " + placeholders, null, false, false);
        this.key = key;
        this.placeholders = placeholders;
    }

    /**
     * Creates a failure describing an input.
     *
     * <p>Fills the {@code argument} and {@code value} placeholders from the input; add any
     * others with {@link #with(String, String)}.
     *
     * @param key   which message describes the failure
     * @param input the tokens that were rejected
     * @return the failure
     */
    public static CxParseException of(final CxKey key, final CxInput input) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(input, "input");

        final Map<String, String> filled = new LinkedHashMap<>();
        filled.put("argument", input.argument());
        filled.put("value", input.joined());
        return new CxParseException(key, filled);
    }

    /**
     * Returns a copy of this failure carrying one more placeholder.
     *
     * @param name  the placeholder name, without braces
     * @param value what to substitute for it
     * @return a new failure; this one is left unchanged
     */
    public CxParseException with(final String name, final String value) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");

        final Map<String, String> extended = new LinkedHashMap<>(this.placeholders);
        extended.put(name, value);
        return new CxParseException(this.key, extended);
    }

    /**
     * Returns which message describes the failure.
     *
     * @return the message key
     */
    public CxKey key() {
        return this.key;
    }

    /**
     * Returns the values to substitute into the message.
     *
     * @return an unmodifiable map of placeholder names to values
     */
    public Map<String, String> placeholders() {
        return Collections.unmodifiableMap(this.placeholders);
    }
}
