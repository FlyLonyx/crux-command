package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxTextArgumentTypeTest {

    private final CxSender sender = FakeSender.player("Notch");

    @Nested
    class Strings {

        @Test
        void a_string_reads_the_token_it_was_given() throws CxParseException {
            assertThat(CxArgumentTypes.string().parse(CxInput.of("reason", "griefing"), sender))
                    .isEqualTo("griefing");
        }

        @Test
        void a_string_accepts_a_quoted_phrase_the_tokeniser_already_joined() throws CxParseException {
            assertThat(CxArgumentTypes.string().parse(CxInput.of("reason", "repeated griefing"), sender))
                    .isEqualTo("repeated griefing");
        }

        @Test
        void a_string_accepts_an_empty_token() throws CxParseException {
            assertThat(CxArgumentTypes.string().parse(CxInput.of("reason", ""), sender)).isEmpty();
        }

        @Test
        void a_string_consumes_one_token() {
            assertThat(CxArgumentTypes.string().arity()).isEqualTo(1);
            assertThat(CxArgumentTypes.string().id()).isEqualTo("string");
        }
    }

    @Nested
    class Words {

        @Test
        void a_word_reads_a_token_holding_no_whitespace() throws CxParseException {
            assertThat(CxArgumentTypes.word().parse(CxInput.of("kit", "starter"), sender)).isEqualTo("starter");
        }

        @Test
        void a_word_refuses_a_quoted_phrase() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> CxArgumentTypes.word().parse(CxInput.of("kit", "not a kit"), sender))
                    .extracting(CxParseException::key)
                    .isEqualTo(CxKey.NOT_A_SINGLE_WORD);
        }

        @Test
        void a_word_refuses_an_empty_token() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> CxArgumentTypes.word().parse(CxInput.of("kit", ""), sender))
                    .extracting(CxParseException::key)
                    .isEqualTo(CxKey.NOT_A_SINGLE_WORD);
        }

        @Test
        void a_word_names_itself_in_its_failure() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> CxArgumentTypes.word().parse(CxInput.of("kit", "not a kit"), sender))
                    .extracting(failure -> failure.placeholders().get("argument"))
                    .isEqualTo("kit");
        }

        @Test
        void a_word_consumes_one_token() {
            assertThat(CxArgumentTypes.word().arity()).isEqualTo(1);
            assertThat(CxArgumentTypes.word().id()).isEqualTo("word");
        }
    }

    @Nested
    class GreedyStrings {

        @Test
        void a_greedy_string_joins_every_token_it_claimed() throws CxParseException {
            CxInput input = CxInput.of("message", Arrays.asList("see", "you", "tomorrow"));

            assertThat(CxArgumentTypes.greedyString().parse(input, sender)).isEqualTo("see you tomorrow");
        }

        @Test
        void a_greedy_string_reads_a_lone_token_unchanged() throws CxParseException {
            assertThat(CxArgumentTypes.greedyString().parse(CxInput.of("message", "hello"), sender))
                    .isEqualTo("hello");
        }

        @Test
        void a_greedy_string_consumes_the_remainder() {
            assertThat(CxArgumentTypes.greedyString().arity()).isEqualTo(CxArgumentType.ARITY_GREEDY);
            assertThat(CxArgumentTypes.greedyString().id()).isEqualTo("greedy-string");
        }
    }

    @Test
    void the_text_types_are_shared_rather_than_rebuilt() {
        assertThat(CxArgumentTypes.string()).isSameAs(CxArgumentTypes.string());
        assertThat(CxArgumentTypes.word()).isSameAs(CxArgumentTypes.word());
        assertThat(CxArgumentTypes.greedyString()).isSameAs(CxArgumentTypes.greedyString());
    }
}
