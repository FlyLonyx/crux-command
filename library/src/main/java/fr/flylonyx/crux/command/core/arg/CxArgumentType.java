package fr.flylonyx.crux.command.core.arg;

/**
 * Turns tokens into a value of a given type.
 *
 * <p>Implementations describe one kind of argument: a number, a player, a duration.
 * Declaring how many tokens the type consumes lets a three-token location sit in the tree
 * beside a single-token number.
 *
 * @param <T> the type produced
 */
public interface CxArgumentType<T> {

    /** Arity of a type that consumes every remaining token. */
    int ARITY_GREEDY = -1;

    /**
     * Returns the identifier used in generated usage strings and error messages.
     *
     * <p>Short and lowercase: {@code player}, {@code int}, {@code duration}.
     *
     * @return the identifier, never {@code null} or empty
     */
    String id();

    /**
     * Returns how many tokens this type consumes.
     *
     * @return a positive token count, or {@link #ARITY_GREEDY} to consume the remainder
     */
    default int arity() {
        return 1;
    }
}
