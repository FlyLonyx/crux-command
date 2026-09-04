package fr.flylonyx.crux.command.core.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Splits a raw command line into tokens.
 *
 * <p>The server hands commands over already split on spaces, which loses any grouping the
 * sender intended. Joining the arguments back together and tokenising here is what makes
 * {@code /warp set "spawn area"} behave as one argument rather than two.
 *
 * <p>Rules:
 * <ul>
 *   <li>runs of spaces separate tokens and are collapsed;</li>
 *   <li>a double quote opens or closes a section in which spaces are literal;</li>
 *   <li>a backslash escapes the character that follows it, inside quotes or out;</li>
 *   <li>an unterminated quote yields a single token rather than an error.</li>
 * </ul>
 *
 * <p>That last rule is deliberate. A sender halfway through typing a quoted argument has
 * an unterminated quote by definition, and refusing to tokenise it would break tab
 * completion exactly when it is most useful.
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

        final List<String> values = new ArrayList<String>();
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
