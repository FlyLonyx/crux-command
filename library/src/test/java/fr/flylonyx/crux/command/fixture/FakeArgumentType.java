package fr.flylonyx.crux.command.fixture;

import fr.flylonyx.crux.command.core.arg.CxArgumentType;

/**
 * An argument type that only declares how much it consumes.
 *
 * <p>Routing never reads values, so this is all a matcher test needs. Using it keeps those
 * tests independent of the built-in types, and therefore independent of whether reading a
 * player or a number happens to work.
 */
public final class FakeArgumentType implements CxArgumentType<String> {

    private final String id;
    private final int arity;

    public FakeArgumentType(String id) {
        this(id, 1);
    }

    public FakeArgumentType(String id, int arity) {
        this.id = id;
        this.arity = arity;
    }

    public static FakeArgumentType greedy(String id) {
        return new FakeArgumentType(id, CxArgumentType.ARITY_GREEDY);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public int arity() {
        return this.arity;
    }
}
