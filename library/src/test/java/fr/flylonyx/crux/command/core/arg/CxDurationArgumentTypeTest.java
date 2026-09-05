package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxDurationArgumentTypeTest {

    private final CxArgumentType<Duration> type = CxArgumentTypes.duration();
    private final CxSender sender = FakeSender.player("Notch");

    @ParameterizedTest
    @CsvSource({
        "90s, 90",
        "15m, 900",
        "2h, 7200",
        "7d, 604800",
        "2h30m, 9000",
        "1d2h3m4s, 93784",
        "30m2h, 9000",
        "0s, 0"
    })
    void a_duration_adds_up_the_units_it_was_given(String token, long expectedSeconds) throws CxParseException {
        assertThat(type.parse(CxInput.of("time", token), sender))
                .isEqualTo(Duration.ofSeconds(expectedSeconds));
    }

    @Test
    void a_duration_reads_the_same_value_whatever_the_case() throws CxParseException {
        assertThat(type.parse(CxInput.of("time", "2H30M"), sender)).isEqualTo(Duration.ofSeconds(9000));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "90", "s", "2h30", "x2h", "-5m", "2h30x", "soon"})
    void a_duration_refuses_a_token_it_cannot_read_whole(String token) {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> type.parse(CxInput.of("time", token), sender))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.INVALID_DURATION);
    }

    /** Reading {@code 2h3h} as five hours would be a guess about what the sender meant. */
    @Test
    void a_duration_refuses_a_unit_given_twice() {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> type.parse(CxInput.of("time", "2h3h"), sender))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.INVALID_DURATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999999d", "9223372036854775807d", "9223372036854775807s1d"})
    void a_duration_refuses_a_value_it_could_not_hold(String token) {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> type.parse(CxInput.of("time", token), sender))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.INVALID_DURATION);
    }

    @Test
    void a_duration_names_itself() {
        assertThat(type.id()).isEqualTo("duration");
        assertThat(type.arity()).isEqualTo(1);
    }
}
