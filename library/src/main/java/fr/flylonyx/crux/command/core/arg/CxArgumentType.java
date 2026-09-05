package fr.flylonyx.crux.command.core.arg;

import java.util.Collections;
import java.util.List;

import fr.flylonyx.crux.command.core.sender.CxSender;

/**
 * Turns tokens into a value of a given type.
 *
 * <p>Implementations describe one kind of argument: a number, a player, a duration.
 * Declaring how many tokens the type consumes lets a three-token location sit in the tree
 * beside a single-token number.
 *
 * <p>Built-in types come from {@link CxArgumentTypes}. Write your own when a command needs
 * a value the library knows nothing about; register it so the annotation layer can resolve
 * it from a parameter type.
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
     * Reads the value the sender typed.
     *
     * @param input  the tokens this argument claimed, never empty
     * @param sender who ran the command, for types whose result depends on them
     * @return the value, never {@code null}
     * @throws CxParseException if the tokens describe no valid value
     */
    T parse(CxInput input, CxSender sender) throws CxParseException;

    /**
     * Suggests the values that would complete a partial token.
     *
     * <p>Only worth overriding for a type with a listable set of values. Suggestions are
     * filtered again against what the sender may run, so returning a value here is not a
     * promise that it will be offered.
     *
     * @param partial what the sender has typed so far, possibly empty
     * @param sender  who is completing, so results can be narrowed to them
     * @return the candidates, never {@code null}
     */
    default List<String> suggest(String partial, CxSender sender) {
        return Collections.emptyList();
    }

    /**
     * Returns how many tokens this type consumes.
     *
     * @return a positive token count, or {@link #ARITY_GREEDY} to consume the remainder
     */
    default int arity() {
        return 1;
    }
}
