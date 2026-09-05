package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;

class CxArgumentTypeTest {

    /** A type stating nothing but how to read a value, to exercise the defaults. */
    private static final class Bare implements CxArgumentType<String> {

        @Override
        public String id() {
            return "bare";
        }

        @Override
        public String parse(CxInput input, CxSender sender) {
            return input.first();
        }
    }

    @Test
    void a_type_consumes_one_token_unless_it_says_otherwise() {
        assertThat(new Bare().arity()).isEqualTo(1);
    }

    @Test
    void a_type_suggests_nothing_unless_it_says_otherwise() {
        assertThat(new Bare().suggest("", FakeSender.console())).isEmpty();
    }

    @Test
    void a_greedy_arity_is_distinguishable_from_any_token_count() {
        assertThat(CxArgumentType.ARITY_GREEDY).isNegative();
    }
}
