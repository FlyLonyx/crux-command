package fr.flylonyx.crux.command.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CxArgumentsTest {

    private static Map<String, Object> values() {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("target", "Notch");
        values.put("amount", 5);
        return values;
    }

    @Test
    void reads_a_value_back_under_the_name_it_was_stored_with() {
        CxArguments arguments = CxArguments.of(values());

        assertThat(arguments.get("target", String.class)).isEqualTo("Notch");
        assertThat(arguments.get("amount", Integer.class)).isEqualTo(5);
    }

    @Test
    void lists_the_arguments_it_holds_in_order() {
        CxArguments arguments = CxArguments.of(values());

        assertThat(arguments.names()).containsExactly("target", "amount");
        assertThat(arguments.has("target")).isTrue();
        assertThat(arguments.has("reason")).isFalse();
    }

    @Test
    void reports_an_argument_the_command_does_not_declare() {
        CxArguments arguments = CxArguments.of(values());

        assertThatThrownBy(() -> arguments.get("reason", String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason")
                .hasMessageContaining("target");
    }

    @Test
    void reports_an_argument_read_as_another_class() {
        CxArguments arguments = CxArguments.of(values());

        assertThatThrownBy(() -> arguments.get("amount", String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java.lang.Integer")
                .hasMessageContaining("java.lang.String");
    }

    @Test
    void a_command_declaring_no_argument_shares_one_empty_instance() {
        assertThat(CxArguments.of(Collections.emptyMap())).isSameAs(CxArguments.empty());
        assertThat(CxArguments.empty().names()).isEmpty();
    }

    @Test
    void copies_the_values_away_from_the_caller() {
        Map<String, Object> source = values();
        CxArguments arguments = CxArguments.of(source);

        source.clear();

        assertThat(arguments.names()).containsExactly("target", "amount");
    }

    @Test
    void exposes_the_names_as_an_unmodifiable_set() {
        CxArguments arguments = CxArguments.of(values());

        assertThatThrownBy(() -> arguments.names().add("reason"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /** A value read as nothing would reach a handler as a missing one. */
    @Test
    void refuses_a_name_or_a_value_that_is_missing() {
        Map<String, Object> missingValue = new HashMap<>();
        missingValue.put("target", null);

        Map<String, Object> missingName = new HashMap<>();
        missingName.put(null, "Notch");

        assertThatThrownBy(() -> CxArguments.of(missingValue)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxArguments.of(missingName)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxArguments.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void needs_both_a_name_and_a_class_to_read_a_value() {
        CxArguments arguments = CxArguments.of(values());

        assertThatThrownBy(() -> arguments.get(null, String.class)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> arguments.get("target", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void describes_itself_by_what_it_holds() {
        assertThat(CxArguments.of(values())).hasToString("{target=Notch, amount=5}");
    }
}
