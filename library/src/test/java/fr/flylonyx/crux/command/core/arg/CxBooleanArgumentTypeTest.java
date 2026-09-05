package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxBooleanArgumentTypeTest {

    private final CxArgumentType<Boolean> type = CxArgumentTypes.booleanValue();
    private final CxSender sender = FakeSender.player("Notch");

    @Test
    void a_boolean_reads_both_values_whatever_the_case() throws CxParseException {
        assertThat(type.parse(CxInput.of("silent", "true"), sender)).isTrue();
        assertThat(type.parse(CxInput.of("silent", "TRUE"), sender)).isTrue();
        assertThat(type.parse(CxInput.of("silent", "false"), sender)).isFalse();
        assertThat(type.parse(CxInput.of("silent", "False"), sender)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "no", "on", "off", "1", "0", ""})
    void a_boolean_refuses_any_other_spelling(String token) {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> type.parse(CxInput.of("silent", token), sender))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.INVALID_BOOLEAN);
    }

    @Test
    void a_boolean_offers_both_values() {
        assertThat(type.suggest("", sender)).containsExactly("true", "false");
    }

    @Test
    void a_boolean_narrows_its_suggestions_to_what_was_typed() {
        assertThat(type.suggest("t", sender)).containsExactly("true");
        assertThat(type.suggest("FA", sender)).containsExactly("false");
        assertThat(type.suggest("x", sender)).isEmpty();
    }

    @Test
    void suggestions_cannot_be_changed_by_the_caller() {
        assertThatThrownBy(() -> type.suggest("", sender).add("maybe"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void completing_needs_something_to_complete() {
        assertThatThrownBy(() -> type.suggest(null, sender)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void a_boolean_names_itself() {
        assertThat(type.id()).isEqualTo("boolean");
        assertThat(type.arity()).isEqualTo(1);
    }
}
