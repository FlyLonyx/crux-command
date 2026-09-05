package fr.flylonyx.crux.command.core.arg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The tokens one argument claimed, under the name it was declared with.
 *
 * <p>Carrying the name lets a type name the argument in its own failures without reaching
 * back into the command tree.
 */
public final class CxInput {

    private final String argument;
    private final List<String> tokens;

    private CxInput(final String argument, final List<String> tokens) {
        this.argument = argument;
        this.tokens = tokens;
    }

    /**
     * Creates an input from the tokens an argument claimed.
     *
     * @param argument the name of the argument being read
     * @param tokens   the tokens it claimed, copied defensively
     * @return the input
     * @throws IllegalArgumentException if no token was claimed
     */
    public static CxInput of(final String argument, final List<String> tokens) {
        Objects.requireNonNull(argument, "argument");
        Objects.requireNonNull(tokens, "tokens");
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("The argument '" + argument + "' was given nothing to read.");
        }
        return new CxInput(argument, new ArrayList<>(tokens));
    }

    /**
     * Creates an input from a single token.
     *
     * @param argument the name of the argument being read
     * @param token    the token it claimed
     * @return the input
     */
    public static CxInput of(final String argument, final String token) {
        Objects.requireNonNull(token, "token");
        return of(argument, Collections.singletonList(token));
    }

    /**
     * Returns the name of the argument being read.
     *
     * @return the argument name
     */
    public String argument() {
        return this.argument;
    }

    /**
     * Returns the tokens the argument claimed.
     *
     * @return an unmodifiable list holding at least one token
     */
    public List<String> tokens() {
        return Collections.unmodifiableList(this.tokens);
    }

    /**
     * Returns the first token.
     *
     * @return the first token
     */
    public String first() {
        return this.tokens.get(0);
    }

    /**
     * Returns every token joined by a single space.
     *
     * <p>Runs of whitespace the sender typed between tokens are not preserved.
     *
     * @return the joined tokens
     */
    public String joined() {
        return String.join(" ", this.tokens);
    }

    @Override
    public String toString() {
        return this.argument + "=" + this.tokens;
    }
}
