package fr.flylonyx.crux.command.core.arg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * Reads one constant of an enum, by name and ignoring case.
 *
 * @param <E> the enum read
 */
final class CxEnumArgumentType<E extends Enum<E>> implements CxArgumentType<E> {

    /** How many choices a failure lists before trailing off, so a wide enum cannot flood the chat. */
    private static final int MAX_LISTED_CHOICES = 10;

    private final String id;
    private final Map<String, E> constants;

    CxEnumArgumentType(final Class<E> target) {
        Objects.requireNonNull(target, "target");
        this.id = target.getSimpleName().toLowerCase(Locale.ROOT);
        this.constants = index(target);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public E parse(final CxInput input, final CxSender sender) throws CxParseException {
        final E constant = this.constants.get(input.first().toLowerCase(Locale.ROOT));
        if (constant == null) {
            throw CxParseException.of(CxKey.INVALID_CHOICE, input).with("choices", this.listChoices());
        }
        return constant;
    }

    @Override
    public List<String> suggest(final String partial, final CxSender sender) {
        return CxSuggestions.startingWith(this.constants.keySet(), partial);
    }

    private String listChoices() {
        final List<String> names = new ArrayList<>(this.constants.keySet());
        if (names.size() <= MAX_LISTED_CHOICES) {
            return String.join(", ", names);
        }
        return String.join(", ", names.subList(0, MAX_LISTED_CHOICES)) + ", ...";
    }

    private static <E extends Enum<E>> Map<String, E> index(final Class<E> target) {
        final Map<String, E> indexed = new LinkedHashMap<>();
        for (final E constant : target.getEnumConstants()) {
            indexed.put(constant.name().toLowerCase(Locale.ROOT), constant);
        }
        if (indexed.isEmpty()) {
            throw new CxDefinitionException("The enum " + target.getName()
                    + " has no constants, so no value could ever be valid.");
        }
        return Collections.unmodifiableMap(indexed);
    }
}
