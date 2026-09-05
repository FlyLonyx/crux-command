package fr.flylonyx.crux.command.core.arg;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import fr.flylonyx.crux.command.core.CxDefinitionException;

/**
 * Which argument type reads which Java type.
 *
 * <p>The annotation layer looks a command method's parameter types up here, so registering
 * a type replaces how that class is read across every command at once.
 *
 * <pre>{@code
 * registry.register(OfflinePlayer.class, new VanishAwarePlayerType(server));
 * }</pre>
 *
 * <p>One registry per plugin, filled during startup and only read afterwards. It carries
 * no static state and is not safe to write to from more than one thread.
 */
public final class CxArgumentRegistry {

    private final Map<Class<?>, CxArgumentType<?>> types = new HashMap<>();

    /** Creates a registry holding the built-in types. */
    public CxArgumentRegistry() {
        this.register(String.class, CxArgumentTypes.string());
        this.register(boolean.class, CxArgumentTypes.booleanValue());
        this.register(Boolean.class, CxArgumentTypes.booleanValue());
        this.register(int.class, CxArgumentTypes.integer());
        this.register(Integer.class, CxArgumentTypes.integer());
        this.register(long.class, CxArgumentTypes.longNumber());
        this.register(Long.class, CxArgumentTypes.longNumber());
        this.register(float.class, CxArgumentTypes.floatNumber());
        this.register(Float.class, CxArgumentTypes.floatNumber());
        this.register(double.class, CxArgumentTypes.doubleNumber());
        this.register(Double.class, CxArgumentTypes.doubleNumber());
        this.register(UUID.class, CxArgumentTypes.uuid());
        this.register(Duration.class, CxArgumentTypes.duration());
    }

    /**
     * Registers a type, replacing whatever read that class before.
     *
     * @param target the class the type produces
     * @param type   how to read it
     * @param <T>    the class the type produces
     * @return this registry
     */
    public <T> CxArgumentRegistry register(final Class<T> target, final CxArgumentType<? extends T> type) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(type, "type");

        this.types.put(target, type);
        return this;
    }

    /**
     * Returns the type reading a class.
     *
     * <p>An enum is read by the name of its constants unless a type was registered for it.
     *
     * @param target the class to read
     * @return the type reading it
     * @throws CxDefinitionException if nothing reads that class
     */
    public CxArgumentType<?> resolve(final Class<?> target) {
        Objects.requireNonNull(target, "target");

        final CxArgumentType<?> registered = this.types.get(target);
        if (registered != null) {
            return registered;
        }
        if (target.isEnum()) {
            return enumType(target);
        }
        throw new CxDefinitionException("No argument type reads " + target.getName()
                + "; register one before declaring a command that takes it.");
    }

    /**
     * Reports whether a class can be read.
     *
     * @param target the class to read
     * @return {@code true} if {@link #resolve(Class)} would return a type
     */
    public boolean supports(final Class<?> target) {
        Objects.requireNonNull(target, "target");
        return this.types.containsKey(target) || target.isEnum();
    }

    /**
     * Builds the type reading an enum.
     *
     * <p>The cast holds because the caller checked {@link Class#isEnum()} first, which the
     * compiler cannot see.
     */
    private static <E extends Enum<E>> CxArgumentType<E> enumType(final Class<?> target) {
        @SuppressWarnings("unchecked")
        final Class<E> asEnum = (Class<E>) target;
        return CxArgumentTypes.enumOf(asEnum);
    }
}
