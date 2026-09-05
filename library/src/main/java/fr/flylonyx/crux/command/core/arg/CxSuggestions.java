package fr.flylonyx.crux.command.core.arg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Narrows a set of candidate values to those a partial token could still become. */
final class CxSuggestions {

    private CxSuggestions() {
    }

    /**
     * Keeps the candidates starting with a partial token, ignoring case.
     *
     * <p>Case is folded in the root locale, so a client running under a Turkish locale
     * still has {@code I} match {@code idle}.
     *
     * @param candidates the values on offer
     * @param partial    what the sender has typed so far, possibly empty
     * @return an unmodifiable list of the candidates that still fit
     */
    static List<String> startingWith(final Collection<String> candidates, final String partial) {
        Objects.requireNonNull(partial, "partial");

        final String prefix = partial.toLowerCase(Locale.ROOT);
        final List<String> fitting = new ArrayList<>();
        for (final String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                fitting.add(candidate);
            }
        }
        return Collections.unmodifiableList(fitting);
    }
}
