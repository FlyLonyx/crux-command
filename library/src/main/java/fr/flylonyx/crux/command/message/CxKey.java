package fr.flylonyx.crux.command.message;

/**
 * Every message the library can produce, with its English default.
 *
 * <p>Each constant carries its configuration key and its default text, so a message
 * cannot be added without a default and the library works unconfigured.
 *
 * <p>Placeholders are written {@code {name}} and substituted literally; no expression is
 * evaluated.
 */
public enum CxKey {

    /** No branch of the command matched what the sender typed. */
    UNKNOWN_SUBCOMMAND("unknown-subcommand", "&cUnknown subcommand. Usage: &f{usage}"),

    /** The command matched, but ran out of input before every argument was supplied. */
    MISSING_ARGUMENT("missing-argument", "&cMissing argument &f{argument}&c. Usage: &f{usage}"),

    /** The command matched, but the sender supplied more arguments than it accepts. */
    TOO_MANY_ARGUMENTS("too-many-arguments", "&cToo many arguments. Usage: &f{usage}"),

    /** An argument type rejected the value without a more precise reason. */
    INVALID_ARGUMENT("invalid-argument", "&f{value}&c is not a valid &f{argument}&c."),

    /** A whole number was expected. */
    INVALID_NUMBER("invalid-number", "&f{value}&c is not a whole number."),

    /** A number was expected, with or without a decimal part. */
    INVALID_DECIMAL("invalid-decimal", "&f{value}&c is not a number."),

    /** A number was below the lowest value its argument accepts. */
    NUMBER_TOO_LOW("number-too-low", "&f{argument}&c must be at least &f{min}&c."),

    /** A number was above the highest value its argument accepts. */
    NUMBER_TOO_HIGH("number-too-high", "&f{argument}&c must be at most &f{max}&c."),

    /** Neither {@code true} nor {@code false} was given. */
    INVALID_BOOLEAN("invalid-boolean", "&f{value}&c is not &ftrue&c or &ffalse&c."),

    /** A unique id was expected, in its usual dashed form. */
    INVALID_UUID("invalid-uuid", "&f{value}&c is not a valid unique id."),

    /** The value was not one of the choices the argument accepts. */
    INVALID_CHOICE("invalid-choice", "&f{value}&c is not one of: &f{choices}"),

    /** A duration was expected, such as {@code 90s} or {@code 2h30m}. */
    INVALID_DURATION("invalid-duration", "&f{value}&c is not a duration. Try &f2h30m&c."),

    /** Several words were given where the argument takes exactly one. */
    NOT_A_SINGLE_WORD("not-a-single-word", "&f{argument}&c must be a single word.");

    private final String configKey;
    private final String defaultMessage;

    CxKey(final String configKey, final String defaultMessage) {
        this.configKey = configKey;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Returns the key used to override this message from configuration.
     *
     * @return the kebab-case configuration key
     */
    public String configKey() {
        return this.configKey;
    }

    /**
     * Returns the text used when nothing overrides this message.
     *
     * @return the English default
     */
    public String defaultMessage() {
        return this.defaultMessage;
    }
}
