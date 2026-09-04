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
    TOO_MANY_ARGUMENTS("too-many-arguments", "&cToo many arguments. Usage: &f{usage}");

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
