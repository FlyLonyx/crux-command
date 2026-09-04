package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CxArgumentTypeTest {

    @Test
    void a_type_consumes_one_token_unless_it_says_otherwise() {
        CxArgumentType<String> word = () -> "word";

        assertThat(word.id()).isEqualTo("word");
        assertThat(word.arity()).isEqualTo(1);
    }

    @Test
    void a_greedy_arity_is_distinguishable_from_any_token_count() {
        assertThat(CxArgumentType.ARITY_GREEDY).isNegative();
    }
}
