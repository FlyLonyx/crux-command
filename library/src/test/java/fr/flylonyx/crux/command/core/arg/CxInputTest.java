package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class CxInputTest {

    @Test
    void an_input_reads_back_the_tokens_it_was_given() {
        CxInput input = CxInput.of("message", Arrays.asList("hello", "there"));

        assertThat(input.argument()).isEqualTo("message");
        assertThat(input.tokens()).containsExactly("hello", "there");
        assertThat(input.first()).isEqualTo("hello");
        assertThat(input.joined()).isEqualTo("hello there");
    }

    @Test
    void a_single_token_input_needs_no_list() {
        CxInput input = CxInput.of("amount", "12");

        assertThat(input.tokens()).containsExactly("12");
        assertThat(input.joined()).isEqualTo("12");
    }

    @Test
    void an_argument_with_nothing_to_read_is_rejected() {
        assertThatThrownBy(() -> CxInput.of("amount", Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void the_tokens_are_copied_away_from_the_caller() {
        List<String> given = new ArrayList<>(Arrays.asList("first"));
        CxInput input = CxInput.of("message", given);
        given.add("second");

        assertThat(input.tokens()).containsExactly("first");
    }

    @Test
    void the_tokens_cannot_be_changed_through_the_getter() {
        CxInput input = CxInput.of("message", Arrays.asList("first"));

        assertThatThrownBy(() -> input.tokens().add("second"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void a_null_token_is_rejected() {
        assertThatThrownBy(() -> CxInput.of("amount", (String) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void an_input_describes_itself_by_argument_and_tokens() {
        assertThat(CxInput.of("message", Arrays.asList("hello", "there")))
                .hasToString("message=[hello, there]");
    }
}
