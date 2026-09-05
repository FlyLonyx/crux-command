package fr.flylonyx.crux.command.core.arg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * Reads {@code true} or {@code false}, ignoring case.
 *
 * <p>Nothing else is accepted. A command that reads better with {@code on} and {@code off}
 * is better served by two literal sub-commands than by a second spelling of a boolean.
 */
final class CxBooleanArgumentType implements CxArgumentType<Boolean> {

    private static final List<String> CHOICES = Collections.unmodifiableList(Arrays.asList("true", "false"));

    @Override
    public String id() {
        return "boolean";
    }

    @Override
    public Boolean parse(final CxInput input, final CxSender sender) throws CxParseException {
        final String value = input.first();
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        throw CxParseException.of(CxKey.INVALID_BOOLEAN, input);
    }

    @Override
    public List<String> suggest(final String partial, final CxSender sender) {
        return CxSuggestions.startingWith(CHOICES, partial);
    }
}
