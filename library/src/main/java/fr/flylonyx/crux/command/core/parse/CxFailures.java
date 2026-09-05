package fr.flylonyx.crux.command.core.parse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import fr.flylonyx.crux.command.core.arg.CxParseException;
import fr.flylonyx.crux.command.core.node.CxUsage;
import fr.flylonyx.crux.command.message.CxKey;
import fr.flylonyx.crux.command.message.CxMessages;

/**
 * Turns a failure into the line a sender is shown.
 *
 * <p>A command line that routed nowhere and a value that could not be read are described
 * the same way: the message the failure names, filled with what the sender typed and the
 * usage of the command they were reaching for.
 */
public final class CxFailures {

    private CxFailures() {
    }

    /**
     * Describes a command line that reached no handler.
     *
     * @param messages the text to draw on
     * @param label    the alias the command was typed under, without the leading slash
     * @param failure  the routing outcome to describe
     * @return the line to send
     * @throws IllegalStateException if routing succeeded
     */
    public static String describe(final CxMessages messages, final String label, final CxMatchResult failure) {
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(failure, "failure");

        final CxKey key = failure.failure();
        final Map<String, String> values = surroundings(label, failure);
        if (key == CxKey.MISSING_ARGUMENT) {
            values.put("argument", failure.detail());
        }
        return messages.render(key, values);
    }

    /**
     * Describes a value that could not be read.
     *
     * <p>The failure already names the argument and the value; what it cannot know is the
     * command they belong to.
     *
     * @param messages the text to draw on
     * @param label    the alias the command was typed under, without the leading slash
     * @param match    where routing got to before the value was read
     * @param failure  the value that was rejected
     * @return the line to send
     */
    public static String describe(final CxMessages messages,
                                  final String label,
                                  final CxMatchResult match,
                                  final CxParseException failure) {

        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(failure, "failure");

        final Map<String, String> values = surroundings(label, match);
        values.putAll(failure.placeholders());
        return messages.render(failure.key(), values);
    }

    private static Map<String, String> surroundings(final String label, final CxMatchResult result) {
        Objects.requireNonNull(result, "result");

        final Map<String, String> values = new LinkedHashMap<>();
        values.put("label", label);
        values.put("usage", CxUsage.of(label, result.path()));
        return values;
    }
}
