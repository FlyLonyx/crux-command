package fr.flylonyx.crux.command.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.flylonyx.crux.command.core.sender.CxSender;

/**
 * Everything a handler is given when its command runs.
 *
 * <p>Carries who ran the command, under which label, and the words they typed. Handlers
 * that only need the sender can ignore the rest; a handler that wants to echo the raw
 * input back has it without the engine having to guess.
 *
 * <p>Deliberately independent of the parsing package: the tokens reaching a handler are
 * plain strings, so nothing downstream of the engine has to understand how they were
 * produced.
 */
public final class CxContext {

    private final CxSender sender;
    private final String label;
    private final List<String> raw;

    /**
     * Creates a context.
     *
     * @param sender who ran the command
     * @param label  the alias the command was invoked under, without the leading slash
     * @param raw    the words that followed the label, copied defensively
     */
    public CxContext(final CxSender sender, final String label, final List<String> raw) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.label = Objects.requireNonNull(label, "label");
        this.raw = new ArrayList<String>(Objects.requireNonNull(raw, "raw"));
    }

    /**
     * Returns who ran the command.
     *
     * @return the sender
     */
    public CxSender sender() {
        return this.sender;
    }

    /**
     * Returns the alias the command was invoked under.
     *
     * <p>A command registered as {@code money} with the alias {@code bal} reports
     * {@code bal} here when the sender typed {@code /bal}, which is what a generated usage
     * string should echo back rather than the canonical name.
     *
     * @return the label, without the leading slash
     */
    public String label() {
        return this.label;
    }

    /**
     * Returns the words that followed the label.
     *
     * @return an unmodifiable list of the raw arguments
     */
    public List<String> raw() {
        return Collections.unmodifiableList(this.raw);
    }
}
