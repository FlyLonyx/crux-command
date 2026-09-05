package fr.flylonyx.crux.command.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.flylonyx.crux.command.core.sender.CxSender;

/**
 * Everything a handler is given when its command runs.
 *
 * <p>Carries who ran the command, under which label, the values its arguments were read
 * into, and the words the sender typed. Independent of the parsing package on purpose.
 */
public final class CxContext {

    private final CxSender sender;
    private final String label;
    private final List<String> raw;
    private final CxArguments arguments;

    /**
     * Creates a context.
     *
     * @param sender    who ran the command
     * @param label     the alias the command was invoked under, without the leading slash
     * @param raw       the words that followed the label, copied defensively
     * @param arguments the value read for each argument the command declares
     */
    public CxContext(final CxSender sender,
                     final String label,
                     final List<String> raw,
                     final CxArguments arguments) {

        this.sender = Objects.requireNonNull(sender, "sender");
        this.label = Objects.requireNonNull(label, "label");
        this.raw = new ArrayList<>(Objects.requireNonNull(raw, "raw"));
        this.arguments = Objects.requireNonNull(arguments, "arguments");
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
     * {@code bal} when the sender typed {@code /bal}. Generated usage echoes this rather
     * than the canonical name.
     *
     * @return the label, without the leading slash
     */
    public String label() {
        return this.label;
    }

    /**
     * Returns the value read for each argument the command declares.
     *
     * @return the arguments
     */
    public CxArguments arguments() {
        return this.arguments;
    }

    /**
     * Returns the words that followed the label.
     *
     * <p>What the sender typed, before any argument was read. Handlers want
     * {@link #arguments()}; this is for the rare command that reads the line itself.
     *
     * @return an unmodifiable list of the raw arguments
     */
    public List<String> raw() {
        return Collections.unmodifiableList(this.raw);
    }
}
