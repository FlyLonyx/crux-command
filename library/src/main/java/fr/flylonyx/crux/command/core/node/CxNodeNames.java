package fr.flylonyx.crux.command.core.node;

import java.util.Locale;

import fr.flylonyx.crux.command.core.CxDefinitionException;

/**
 * Naming rules shared by nodes and their builder.
 */
final class CxNodeNames {

    private CxNodeNames() {
    }

    /**
     * Normalises a word for case-insensitive lookup.
     *
     * <p>{@link Locale#ROOT} because a Turkish locale lowercases {@code I} to a dotless
     * {@code i}, which would stop {@code /INFO} matching {@code info}.
     */
    static String lookupKey(final String word) {
        return word.toLowerCase(Locale.ROOT);
    }

    /**
     * Rejects a name that could never be matched.
     */
    static void requireUsable(final String role, final String name) {
        if (name == null || name.isEmpty()) {
            throw new CxDefinitionException("A " + role + " needs a name.");
        }
        for (int index = 0; index < name.length(); index++) {
            if (Character.isWhitespace(name.charAt(index))) {
                throw new CxDefinitionException(
                        "The " + role + " '" + name + "' contains whitespace, so no single token can ever match it.");
            }
        }
    }
}
