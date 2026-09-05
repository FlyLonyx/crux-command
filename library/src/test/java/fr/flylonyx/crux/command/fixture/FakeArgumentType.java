package fr.flylonyx.crux.command.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.flylonyx.crux.command.core.arg.CxArgumentType;
import fr.flylonyx.crux.command.core.arg.CxInput;
import fr.flylonyx.crux.command.core.arg.CxParseException;
import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * An argument type with behaviour a test can dictate.
 *
 * <p>Reading back what was typed keeps routing and resolution tests independent of the
 * built-in types, and therefore independent of whether reading a number happens to work.
 */
public final class FakeArgumentType implements CxArgumentType<String> {

    private final String id;
    private final int arity;
    private final CxKey rejection;
    private final List<String> suggestions;

    public FakeArgumentType(String id) {
        this(id, 1, null, Collections.emptyList());
    }

    public FakeArgumentType(String id, int arity) {
        this(id, arity, null, Collections.emptyList());
    }

    private FakeArgumentType(String id, int arity, CxKey rejection, List<String> suggestions) {
        this.id = id;
        this.arity = arity;
        this.rejection = rejection;
        this.suggestions = suggestions;
    }

    public static FakeArgumentType greedy(String id) {
        return new FakeArgumentType(id, CxArgumentType.ARITY_GREEDY);
    }

    /** Returns a type that refuses everything with the given key. */
    public static FakeArgumentType rejecting(String id, CxKey key) {
        return new FakeArgumentType(id, 1, key, Collections.emptyList());
    }

    /** Returns a type that offers the given suggestions whatever the partial token. */
    public static FakeArgumentType suggesting(String id, String... values) {
        return new FakeArgumentType(id, 1, null, Arrays.asList(values));
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public int arity() {
        return this.arity;
    }

    @Override
    public String parse(CxInput input, CxSender sender) throws CxParseException {
        if (this.rejection != null) {
            throw CxParseException.of(this.rejection, input);
        }
        return input.joined();
    }

    @Override
    public List<String> suggest(String partial, CxSender sender) {
        return this.suggestions;
    }
}
