package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxUuidArgumentTypeTest {

    private static final String NOTCH = "069a79f4-44e9-4726-a5be-fca90e38aaf5";

    private final CxArgumentType<UUID> type = CxArgumentTypes.uuid();
    private final CxSender sender = FakeSender.console();

    @Test
    void a_unique_id_reads_the_form_the_platform_prints() throws CxParseException {
        assertThat(type.parse(CxInput.of("target", NOTCH), sender)).isEqualTo(UUID.fromString(NOTCH));
    }

    @Test
    void a_unique_id_reads_the_same_value_whatever_the_case() throws CxParseException {
        assertThat(type.parse(CxInput.of("target", NOTCH.toUpperCase(Locale.ROOT)), sender))
                .isEqualTo(UUID.fromString(NOTCH));
    }

    /** The short forms would parse, into an id the sender never meant. */
    @ParameterizedTest
    @ValueSource(strings = {
        "1-2-3-4-5",
        "069a79f444e94726a5befca90e38aaf5",
        "069a79f4-44e9-4726-a5be-fca90e38aaf",
        "069a79f4-44e9-4726-a5be-fca90e38aaf5-",
        "Notch",
        ""
    })
    void a_unique_id_refuses_anything_but_the_canonical_form(String token) {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> type.parse(CxInput.of("target", token), sender))
                .extracting(CxParseException::key)
                .isEqualTo(CxKey.INVALID_UUID);
    }

    @Test
    void a_unique_id_names_itself() {
        assertThat(type.id()).isEqualTo("uuid");
        assertThat(type.arity()).isEqualTo(1);
    }
}
