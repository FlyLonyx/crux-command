package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.CxArguments;
import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.arg.CxArgumentType;
import fr.flylonyx.crux.command.core.arg.CxArgumentTypes;
import fr.flylonyx.crux.command.core.arg.CxInput;
import fr.flylonyx.crux.command.core.arg.CxParseException;
import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.core.node.CxNodeBuilder;
import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxResolverTest {

    /** A type that breaks the contract, to check the resolver refuses to pass a value on. */
    private static final class Nothing implements CxArgumentType<String> {

        @Override
        public String id() {
            return "nothing";
        }

        @Override
        public String parse(CxInput input, CxSender sender) {
            return null;
        }
    }

    private static final CxNode MONEY = CxNodeBuilder.literal("money")
            .then(CxNodeBuilder.literal("give")
                    .then(CxNodeBuilder.argument("target", CxArgumentTypes.word())
                            .then(CxNodeBuilder.argument("amount", CxArgumentTypes.integer(1, 64))
                                    .executes(context -> { }))))
            .build();

    private final CxSender sender = FakeSender.player("Notch");

    private CxArguments resolve(CxNode root, String line) throws CxParseException {
        CxTokens tokens = CxTokenizer.tokenize(line);
        return CxResolver.resolve(CxMatcher.match(root, tokens), tokens, sender);
    }

    @Test
    void reads_every_argument_on_the_path_the_command_took() throws CxParseException {
        CxArguments arguments = resolve(MONEY, "give Steve 5");

        assertThat(arguments.names()).containsExactly("target", "amount");
        assertThat(arguments.get("target", String.class)).isEqualTo("Steve");
        assertThat(arguments.get("amount", Integer.class)).isEqualTo(5);
    }

    @Test
    void reads_a_greedy_argument_as_everything_that_followed() throws CxParseException {
        CxNode say = CxNodeBuilder.literal("say")
                .then(CxNodeBuilder.argument("message", CxArgumentTypes.greedyString())
                        .executes(context -> { }))
                .build();

        assertThat(resolve(say, "see you tomorrow").get("message", String.class)).isEqualTo("see you tomorrow");
    }

    @Test
    void reads_the_default_of_an_argument_the_sender_left_out() throws CxParseException {
        CxNode home = CxNodeBuilder.literal("home")
                .then(CxNodeBuilder.argument("name", CxArgumentTypes.word()).optional("home")
                        .executes(context -> { }))
                .build();

        assertThat(resolve(home, "").get("name", String.class)).isEqualTo("home");
        assertThat(resolve(home, "work").get("name", String.class)).isEqualTo("work");
    }

    @Test
    void reads_nothing_for_a_command_that_declares_no_argument() throws CxParseException {
        CxNode ping = CxNodeBuilder.literal("ping").executes(context -> { }).build();

        assertThat(resolve(ping, "")).isSameAs(CxArguments.empty());
    }

    /** Routing claims the token; only reading it can tell whether it was a number. */
    @Test
    void reports_a_value_that_could_not_be_read() {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> resolve(MONEY, "give Steve lots"))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.INVALID_NUMBER);
    }

    @Test
    void reports_a_value_that_was_read_but_out_of_range() {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> resolve(MONEY, "give Steve 99"))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.NUMBER_TOO_HIGH);
    }

    @Test
    void refuses_a_type_that_reads_a_value_as_nothing() {
        CxNode broken = CxNodeBuilder.literal("broken")
                .then(CxNodeBuilder.argument("value", new Nothing()).executes(context -> { }))
                .build();

        assertThatThrownBy(() -> resolve(broken, "anything"))
                .isInstanceOf(CxDefinitionException.class)
                .hasMessageContaining("nothing")
                .hasMessageContaining("value");
    }

    @Test
    void refuses_to_read_anything_from_a_match_that_failed() {
        CxTokens tokens = CxTokenizer.tokenize("take Steve 5");
        CxMatchResult failed = CxMatcher.match(MONEY, tokens);

        assertThat(failed.isMatched()).isFalse();
        assertThatThrownBy(() -> CxResolver.resolve(failed, tokens, sender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nothing to read");
    }

    @Test
    void needs_a_match_its_tokens_and_a_sender() {
        CxTokens tokens = CxTokenizer.tokenize("give Steve 5");
        CxMatchResult match = CxMatcher.match(MONEY, tokens);

        assertThatThrownBy(() -> CxResolver.resolve(null, tokens, sender))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxResolver.resolve(match, null, sender))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxResolver.resolve(match, tokens, null))
                .isInstanceOf(NullPointerException.class);
    }
}
