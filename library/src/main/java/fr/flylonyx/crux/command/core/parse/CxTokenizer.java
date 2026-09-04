package fr.flylonyx.crux.command.core.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Splits a raw command line into tokens.
 *
 * <p>The server splits commands on spaces before handing them over, so grouping has to be
 * recovered here for {@code /warp set "spawn area"} to be one argument.
 *
 * <ul>
 *   <li>runs of spaces separate tokens and are collapsed;</li>
 *   <li>a double quote opens or closes a section in which spaces are literal;</li>
 *   <li>a backslash escapes the character that follows it, inside quotes or out;</li>
 *   <li>an unterminated quote yields a single token, since a sender still typing one has
 *       an unterminated quote by definition.</li>
 * </ul>
 */
public final class CxTokenizer {

    private static final char SEPARATOR = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';

    private CxTokenizer() {
    }

    /**
     * Splits a raw command line into tokens.
     *
     * @param raw the command line, without the leading slash or command label
     * @return the tokens, together with whether the input ended on a separator
     * @throws NullPointerException if {@code raw} is {@code null}
     */
    public static CxTokens tokenize(final String raw) {
        Objects.requireNonNull(raw, "raw");

        final List<String> values = new ArrayList<>();
        final StringBuilder current = new StringBuilder();

        boolean started = false;
        boolean quoted = false;
        boolean afterSeparator = false;
        int index = 0;

        while (index < raw.length()) {
            final char character = raw.charAt(index);

            if (character == ESCAPE && index + 1 < raw.length()) {
                current.append(raw.charAt(index + 1));
                started = true;
                afterSeparator = false;
                index += 2;
                continue;
            }

            if (character == QUOTE) {
                quoted = !quoted;
                started = true;
                afterSeparator = false;
            } else if (character == SEPARATOR && !quoted) {
                if (started) {
                    values.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
                afterSeparator = true;
            } else {
                current.append(character);
                started = true;
                afterSeparator = false;
            }

            index++;
        }

        if (started) {
            values.add(current.toString());
        }

        return CxTokens.of(values, afterSeparator);
    }
}
