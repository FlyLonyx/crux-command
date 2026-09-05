package fr.flylonyx.crux.command.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CxPlaceholdersTest {

    private static Map<String, String> values() {
        final Map<String, String> values = new HashMap<>();
        values.put("argument", "amount");
        values.put("usage", "/money give <target> <amount>");
        return values;
    }

    @Test
    void fills_a_placeholder_with_its_value() {
        assertThat(CxPlaceholders.substitute("Missing {argument}.", values())).isEqualTo("Missing amount.");
    }

    @Test
    void fills_every_placeholder_in_the_template() {
        assertThat(CxPlaceholders.substitute("Missing {argument}. Usage: {usage}", values()))
                .isEqualTo("Missing amount. Usage: /money give <target> <amount>");
    }

    @Test
    void fills_the_same_placeholder_more_than_once() {
        assertThat(CxPlaceholders.substitute("{argument} {argument}", values())).isEqualTo("amount amount");
    }

    @Test
    void leaves_a_template_holding_no_placeholder_alone() {
        assertThat(CxPlaceholders.substitute("Too many arguments.", values())).isEqualTo("Too many arguments.");
        assertThat(CxPlaceholders.substitute("", values())).isEmpty();
    }

    /** Showing the placeholder is how a missing value gets noticed instead of hidden. */
    @Test
    void leaves_a_placeholder_nothing_fills_as_it_was_written() {
        assertThat(CxPlaceholders.substitute("Usage: {command}", values())).isEqualTo("Usage: {command}");
    }

    @Test
    void leaves_a_brace_that_opens_nothing() {
        assertThat(CxPlaceholders.substitute("a { b", values())).isEqualTo("a { b");
        assertThat(CxPlaceholders.substitute("a } b", values())).isEqualTo("a } b");
    }

    /** Otherwise a sender could reach a placeholder by naming one in an argument. */
    @Test
    void never_looks_at_a_value_it_has_just_substituted() {
        Map<String, String> values = new HashMap<>();
        values.put("value", "{usage}");
        values.put("usage", "/money");

        assertThat(CxPlaceholders.substitute("{value}", values)).isEqualTo("{usage}");
    }

    @Test
    void fills_nothing_when_there_is_nothing_to_fill_with() {
        assertThat(CxPlaceholders.substitute("Missing {argument}.", Collections.emptyMap()))
                .isEqualTo("Missing {argument}.");
    }
}
