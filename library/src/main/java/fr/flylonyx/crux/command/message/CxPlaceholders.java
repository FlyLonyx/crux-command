package fr.flylonyx.crux.command.message;

import java.util.Map;

/**
 * Substitutes {@code {name}} placeholders into a template.
 *
 * <p>One pass, so a value that itself looks like a placeholder is never substituted again.
 * Without that, a sender could smuggle {@code {usage}} into a message through an argument.
 *
 * <p>A placeholder nothing fills is left as it was written, which shows a defect rather
 * than hiding one.
 */
final class CxPlaceholders {

    private static final char OPEN = '{';
    private static final char CLOSE = '}';

    private CxPlaceholders() {
    }

    /**
     * Fills the placeholders a template holds.
     *
     * @param template the text to fill
     * @param values   what to substitute, by placeholder name
     * @return the filled text
     */
    static String substitute(final String template, final Map<String, String> values) {
        final StringBuilder filled = new StringBuilder(template.length());
        int index = 0;

        while (index < template.length()) {
            final int open = template.indexOf(OPEN, index);
            final int close = open < 0 ? -1 : template.indexOf(CLOSE, open + 1);
            if (close < 0) {
                filled.append(template, index, template.length());
                break;
            }
            filled.append(template, index, open).append(valueOf(template.substring(open + 1, close), values));
            index = close + 1;
        }
        return filled.toString();
    }

    private static String valueOf(final String name, final Map<String, String> values) {
        final String value = values.get(name);
        return value == null ? OPEN + name + CLOSE : value;
    }
}
