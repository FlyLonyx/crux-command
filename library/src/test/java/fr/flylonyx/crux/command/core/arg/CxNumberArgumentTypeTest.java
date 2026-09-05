package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxNumberArgumentTypeTest {

    private final CxSender sender = FakeSender.player("Notch");

    private Object parse(CxArgumentType<?> type, String token) throws CxParseException {
        return type.parse(CxInput.of("amount", token), sender);
    }

    @Nested
    class WholeNumbers {

        @Test
        void an_integer_reads_a_signed_value() throws CxParseException {
            assertThat(parse(CxArgumentTypes.integer(), "12")).isEqualTo(12);
            assertThat(parse(CxArgumentTypes.integer(), "-12")).isEqualTo(-12);
            assertThat(parse(CxArgumentTypes.integer(), "+12")).isEqualTo(12);
        }

        @ParameterizedTest
        @ValueSource(strings = {"twelve", "12.5", "0x10", "", " 12", "2147483648"})
        void an_integer_refuses_what_is_not_a_whole_number(String token) {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.integer(), token))
                    .extracting(CxParseException::key)
                    .isEqualTo(CxKey.INVALID_NUMBER);
        }

        @Test
        void a_long_reads_a_value_beyond_the_integer_range() throws CxParseException {
            assertThat(parse(CxArgumentTypes.longNumber(), "2147483648")).isEqualTo(2147483648L);
        }

        @Test
        void the_whole_number_types_name_themselves() {
            assertThat(CxArgumentTypes.integer().id()).isEqualTo("int");
            assertThat(CxArgumentTypes.longNumber().id()).isEqualTo("long");
        }
    }

    @Nested
    class Decimals {

        @Test
        void a_decimal_reads_the_forms_a_sender_would_type() throws CxParseException {
            assertThat(parse(CxArgumentTypes.doubleNumber(), "12")).isEqualTo(12.0);
            assertThat(parse(CxArgumentTypes.doubleNumber(), "12.5")).isEqualTo(12.5);
            assertThat(parse(CxArgumentTypes.doubleNumber(), "-.5")).isEqualTo(-0.5);
            assertThat(parse(CxArgumentTypes.doubleNumber(), "1e3")).isEqualTo(1000.0);
        }

        /** {@link Double#parseDouble} reads all of these; none is what a sender meant. */
        @ParameterizedTest
        @ValueSource(strings = {"NaN", "Infinity", "-Infinity", "0x1p3", "12d", "12f", "1e999", "", "."})
        void a_decimal_refuses_what_the_platform_would_have_accepted(String token) {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.doubleNumber(), token))
                    .extracting(CxParseException::key)
                    .isEqualTo(CxKey.INVALID_DECIMAL);
        }

        @Test
        void a_float_reads_the_same_forms_at_lower_precision() throws CxParseException {
            assertThat(parse(CxArgumentTypes.floatNumber(), "12.5")).isEqualTo(12.5f);
        }

        @Test
        void a_float_refuses_a_value_beyond_its_range() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.floatNumber(), "1e40"))
                    .extracting(CxParseException::key)
                    .isEqualTo(CxKey.INVALID_DECIMAL);
        }

        @Test
        void the_decimal_types_name_themselves() {
            assertThat(CxArgumentTypes.doubleNumber().id()).isEqualTo("double");
            assertThat(CxArgumentTypes.floatNumber().id()).isEqualTo("float");
        }
    }

    @Nested
    class Bounds {

        @Test
        void a_value_on_either_bound_is_accepted() throws CxParseException {
            assertThat(parse(CxArgumentTypes.integer(1, 64), "1")).isEqualTo(1);
            assertThat(parse(CxArgumentTypes.integer(1, 64), "64")).isEqualTo(64);
        }

        @Test
        void a_value_below_the_minimum_reports_the_minimum() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.integer(1, 64), "0"))
                    .satisfies(failure -> {
                        assertThat(failure.key()).isEqualTo(CxKey.NUMBER_TOO_LOW);
                        assertThat(failure.placeholders()).containsEntry("min", "1");
                    });
        }

        @Test
        void a_value_above_the_maximum_reports_the_maximum() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.integer(1, 64), "65"))
                    .satisfies(failure -> {
                        assertThat(failure.key()).isEqualTo(CxKey.NUMBER_TOO_HIGH);
                        assertThat(failure.placeholders()).containsEntry("max", "64");
                    });
        }

        /** Being told the value is out of range beats being told it is not a number. */
        @Test
        void a_value_that_is_not_a_number_is_reported_before_the_bounds_are_checked() {
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.integer(1, 64), "lots"))
                    .extracting(CxParseException::key)
                    .isEqualTo(CxKey.INVALID_NUMBER);
        }

        @Test
        void bounds_apply_to_every_numeric_type() throws CxParseException {
            assertThat(parse(CxArgumentTypes.longNumber(0L, 10L), "5")).isEqualTo(5L);
            assertThat(parse(CxArgumentTypes.doubleNumber(0.0, 1.0), "0.5")).isEqualTo(0.5);
            assertThat(parse(CxArgumentTypes.floatNumber(0.0f, 1.0f), "0.5")).isEqualTo(0.5f);

            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.longNumber(0L, 10L), "11"));
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.doubleNumber(0.0, 1.0), "1.5"));
            assertThatExceptionOfType(CxParseException.class)
                    .isThrownBy(() -> parse(CxArgumentTypes.floatNumber(0.0f, 1.0f), "-1"));
        }

        @Test
        void bounds_that_exclude_every_value_are_refused_when_the_type_is_built() {
            assertThatThrownBy(() -> CxArgumentTypes.integer(64, 1))
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("64")
                    .hasMessageContaining("1");
        }
    }
}
