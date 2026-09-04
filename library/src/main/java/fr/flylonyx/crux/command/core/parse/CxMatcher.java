package fr.flylonyx.crux.command.core.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import fr.flylonyx.crux.command.core.arg.CxArgumentType;
import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * Walks a command tree to decide which handler a command line reaches.
 *
 * <p>Literal children are tried before argument children, so {@code /money top} runs the
 * leaderboard rather than looking up a player called {@code top}.
 *
 * <p>Routing backtracks: a branch that matches a token but fails further down is
 * abandoned and the next candidate tried.
 *
 * <p>No value is read here. Routing yields the reached node and the token spans each
 * argument claimed; reading those spans is a separate step.
 */
public final class CxMatcher {

    private CxMatcher() {
    }

    /**
     * Routes a command line through a tree.
     *
     * @param root   the node the command was registered under
     * @param tokens the tokens that followed the label
     * @return the reached node and its argument spans, or why nothing matched
     */
    public static CxMatchResult match(final CxNode root, final CxTokens tokens) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(tokens, "tokens");
        return walk(root, tokens, 0, Collections.emptyList());
    }

    private static CxMatchResult walk(final CxNode node,
                                      final CxTokens tokens,
                                      final int index,
                                      final List<CxArgumentSpan> claimed) {

        if (index >= tokens.size()) {
            return exhausted(node, index, claimed);
        }

        final String token = tokens.get(index);
        CxMatchResult best = null;

        final Optional<CxNode> literal = node.literal(token);
        if (literal.isPresent()) {
            final CxMatchResult result = walk(literal.get(), tokens, index + 1, claimed);
            if (result.isMatched()) {
                return result;
            }
            best = preferred(best, result);
        }

        for (final CxNode argument : node.arguments()) {
            final int span = spanOf(argument, tokens, index);
            if (span == 0) {
                continue;
            }
            final CxMatchResult result =
                    walk(argument, tokens, index + span, extend(claimed, new CxArgumentSpan(argument, index, span)));
            if (result.isMatched()) {
                return result;
            }
            best = preferred(best, result);
        }

        return best == null ? rejected(node, token, index) : best;
    }

    /**
     * Decides the outcome when the sender ran out of input.
     */
    private static CxMatchResult exhausted(final CxNode node, final int index, final List<CxArgumentSpan> claimed) {
        if (node.isExecutable()) {
            return CxMatchResult.matched(node, claimed);
        }
        if (!node.arguments().isEmpty()) {
            return CxMatchResult.failed(CxKey.MISSING_ARGUMENT, node.arguments().get(0).name(), index);
        }
        return CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, null, index);
    }

    /**
     * Decides the outcome when a token was left that no child could take.
     *
     * <p>A node with no children at all was already complete, so the leftover token is
     * surplus. A node offering only arguments reached here because none of them had enough
     * tokens left to claim, which is a missing argument rather than an unknown word.
     */
    private static CxMatchResult rejected(final CxNode node, final String token, final int index) {
        if (node.literals().isEmpty() && node.arguments().isEmpty()) {
            return CxMatchResult.failed(CxKey.TOO_MANY_ARGUMENTS, token, index);
        }
        if (node.literals().isEmpty()) {
            return CxMatchResult.failed(CxKey.MISSING_ARGUMENT, node.arguments().get(0).name(), index);
        }
        return CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, token, index);
    }

    /**
     * Returns how many tokens an argument can claim here, or zero if too few remain.
     */
    private static int spanOf(final CxNode argument, final CxTokens tokens, final int index) {
        final int available = tokens.size() - index;
        final int arity = argument.argumentType().arity();

        if (arity == CxArgumentType.ARITY_GREEDY) {
            return available;
        }
        return arity <= available ? arity : 0;
    }

    private static List<CxArgumentSpan> extend(final List<CxArgumentSpan> claimed, final CxArgumentSpan addition) {
        final List<CxArgumentSpan> extended = new ArrayList<>(claimed.size() + 1);
        extended.addAll(claimed);
        extended.add(addition);
        return extended;
    }

    private static CxMatchResult preferred(final CxMatchResult current, final CxMatchResult candidate) {
        return candidate.isMoreInformativeThan(current) ? candidate : current;
    }
}
