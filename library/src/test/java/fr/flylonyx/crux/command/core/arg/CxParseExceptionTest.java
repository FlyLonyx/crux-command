package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.message.CxKey;

class CxParseExceptionTest {

    @Test
    void a_failure_describes_the_input_it_rejected() {
        CxParseException failure = CxParseException.of(CxKey.INVALID_NUMBER, CxInput.of("amount", "twelve"));

        assertThat(failure.key()).isEqualTo(CxKey.INVALID_NUMBER);
        assertThat(failure.placeholders())
                .containsExactly(entry("argument", "amount"), entry("value", "twelve"));
    }

    @Test
    void a_rejected_multi_token_value_is_reported_whole() {
        CxParseException failure =
                CxParseException.of(CxKey.INVALID_ARGUMENT, CxInput.of("where", Arrays.asList("12", "64", "12")));

        assertThat(failure.placeholders()).containsEntry("value", "12 64 12");
    }

    @Test
    void adding_a_placeholder_leaves_the_original_failure_alone() {
        CxParseException failure = CxParseException.of(CxKey.NUMBER_TOO_LOW, CxInput.of("amount", "0"));
        CxParseException bounded = failure.with("min", "1");

        assertThat(bounded.placeholders()).containsEntry("min", "1");
        assertThat(failure.placeholders()).doesNotContainKey("min");
        assertThat(bounded.key()).isEqualTo(failure.key());
    }

    @Test
    void the_placeholders_cannot_be_changed_through_the_getter() {
        CxParseException failure = CxParseException.of(CxKey.INVALID_UUID, CxInput.of("id", "nope"));

        assertThatThrownBy(() -> failure.placeholders().put("value", "other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /** Parsing failures are thrown per bad command line, so paying for a stack trace is waste. */
    @Test
    void a_failure_records_no_stack_trace() {
        CxParseException failure = CxParseException.of(CxKey.INVALID_BOOLEAN, CxInput.of("silent", "maybe"));

        assertThat(failure.getStackTrace()).isEmpty();
    }

    @Test
    void the_message_names_the_key_and_what_it_will_substitute() {
        CxParseException failure = CxParseException.of(CxKey.INVALID_DURATION, CxInput.of("time", "soon"));

        assertThat(failure).hasMessage("invalid-duration {argument=time, value=soon}");
    }

    @Test
    void a_failure_needs_both_a_key_and_an_input() {
        CxInput input = CxInput.of("amount", "1");

        assertThatThrownBy(() -> CxParseException.of(null, input)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxParseException.of(CxKey.INVALID_NUMBER, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void a_placeholder_needs_both_a_name_and_a_value() {
        CxParseException failure = CxParseException.of(CxKey.NUMBER_TOO_HIGH, CxInput.of("amount", "99"));

        assertThatThrownBy(() -> failure.with(null, "64")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> failure.with("max", null)).isInstanceOf(NullPointerException.class);
    }
}
