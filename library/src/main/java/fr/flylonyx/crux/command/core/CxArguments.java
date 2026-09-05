package fr.flylonyx.crux.command.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The values a command's arguments were read into, by name.
 *
 * <p>Every argument on the path the command took is here, whether the sender typed it or
 * left it out for its default. None of them is {@code null}: an argument that could not be
 * read stops the command before the handler runs.
 *
 * <pre>{@code
 * int amount = context.arguments().get("amount", Integer.class);
 * }</pre>
 */
public final class CxArguments {

    private static final CxArguments EMPTY = new CxArguments(Collections.emptyMap());

    private final Map<String, Object> values;

    private CxArguments(final Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Returns the arguments of a command that declares none.
     *
     * @return the empty arguments
     */
    public static CxArguments empty() {
        return EMPTY;
    }

    /**
     * Creates a set of arguments.
     *
     * @param values the value read for each argument name, copied defensively
     * @return the arguments
     * @throws NullPointerException if any name or value is {@code null}
     */
    public static CxArguments of(final Map<String, Object> values) {
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) {
            return EMPTY;
        }

        final Map<String, Object> copy = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            copy.put(Objects.requireNonNull(entry.getKey(), "name"),
                    Objects.requireNonNull(entry.getValue(), "value"));
        }
        return new CxArguments(copy);
    }

    /**
     * Returns the value read for an argument.
     *
     * @param name the argument name, as the command declared it
     * @param type the class the argument produces, boxed where the handler takes a primitive
     * @param <T>  the class the argument produces
     * @return the value, never {@code null}
     * @throws IllegalArgumentException if the command declares no such argument, or it holds
     *                                  a value of another class
     */
    public <T> T get(final String name, final Class<T> type) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");

        final Object value = this.values.get(name);
        if (value == null) {
            throw new IllegalArgumentException("This command declares no argument named '" + name
                    + "'; it declares " + this.values.keySet() + ".");
        }
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("The argument '" + name + "' holds a "
                    + value.getClass().getName() + ", not a " + type.getName() + ".");
        }
        return type.cast(value);
    }

    /**
     * Reports whether an argument was read.
     *
     * @param name the argument name
     * @return {@code true} if the command declares it
     */
    public boolean has(final String name) {
        return this.values.containsKey(name);
    }

    /**
     * Returns the names of the arguments that were read.
     *
     * @return an unmodifiable set of names, in the order the arguments appear
     */
    public Set<String> names() {
        return Collections.unmodifiableSet(this.values.keySet());
    }

    @Override
    public String toString() {
        return this.values.toString();
    }
}
