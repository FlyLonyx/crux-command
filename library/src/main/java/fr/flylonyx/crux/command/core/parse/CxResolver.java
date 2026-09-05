package fr.flylonyx.crux.command.core.parse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import fr.flylonyx.crux.command.core.CxArguments;
import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.arg.CxInput;
import fr.flylonyx.crux.command.core.arg.CxParseException;
import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.core.sender.CxSender;

/**
 * Reads the values behind the spans a match claimed.
 *
 * <p>Routing decides which handler a command line reaches; this decides what it is given.
 * Keeping the two apart is what stops an argument type from deciding which branch was
 * taken, and stops a branch that was never going to match from looking a player up.
 */
public final class CxResolver {

    private CxResolver() {
    }

    /**
     * Reads every argument along a matched path.
     *
     * @param match  a match that succeeded
     * @param tokens the tokens the match refers to
     * @param sender who ran the command
     * @return the value read for each argument name
     * @throws CxParseException      if a value could not be read
     * @throws IllegalStateException if routing did not succeed
     */
    public static CxArguments resolve(final CxMatchResult match, final CxTokens tokens, final CxSender sender)
            throws CxParseException {

        Objects.requireNonNull(match, "match");
        Objects.requireNonNull(tokens, "tokens");
        Objects.requireNonNull(sender, "sender");
        if (!match.isMatched()) {
            throw new IllegalStateException("Routing failed with " + match.failure() + "; there is nothing to read.");
        }

        final Map<String, Object> values = new LinkedHashMap<>();
        for (final CxArgumentSpan span : match.arguments()) {
            final CxNode node = span.node();
            values.put(node.name(), read(node, inputFor(node, span, tokens), sender));
        }
        return CxArguments.of(values);
    }

    private static CxInput inputFor(final CxNode node, final CxArgumentSpan span, final CxTokens tokens) {
        return span.isOmitted()
                ? CxInput.of(node.name(), node.defaultValue())
                : CxInput.of(node.name(), span.extractFrom(tokens));
    }

    /**
     * Reads one value, refusing a type that answers with nothing.
     *
     * <p>A {@code null} would reach the handler as a value that was never read, which is
     * the failure this library exists to make impossible.
     */
    private static Object read(final CxNode node, final CxInput input, final CxSender sender)
            throws CxParseException {

        final Object value = node.argumentType().parse(input, sender);
        if (value == null) {
            throw new CxDefinitionException("The argument type '" + node.argumentType().id() + "' read '"
                    + node.name() + "' as nothing; a type has to refuse a value rather than return none.");
        }
        return value;
    }
}
