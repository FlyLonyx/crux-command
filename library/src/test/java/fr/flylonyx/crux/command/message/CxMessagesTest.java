package fr.flylonyx.crux.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CxMessagesTest {

    private static final String SECTION = "\u00A7";

    private final CxMessages messages = new CxMessages();

    @Test
    void falls_back_to_the_english_default() {
        assertThat(messages.template(CxKey.TOO_MANY_ARGUMENTS))
                .isEqualTo(CxKey.TOO_MANY_ARGUMENTS.defaultMessage());
    }

    @Test
    void uses_an_override_once_one_is_set() {
        messages.set(CxKey.TOO_MANY_ARGUMENTS, "&cThat is too much.");

        assertThat(messages.template(CxKey.TOO_MANY_ARGUMENTS)).isEqualTo("&cThat is too much.");
    }

    @Test
    void reads_as_one_statement_per_override() {
        assertThat(messages.set(CxKey.INVALID_NUMBER, "&cnope")).isSameAs(messages);
    }

    @Test
    void translates_the_colour_codes_of_a_template() {
        messages.set(CxKey.TOO_MANY_ARGUMENTS, "&cToo much");

        assertThat(messages.render(CxKey.TOO_MANY_ARGUMENTS, Collections.emptyMap()))
                .isEqualTo(SECTION + "cToo much");
    }

    @Test
    void substitutes_the_values_it_is_given() {
        messages.set(CxKey.MISSING_ARGUMENT, "Missing {argument}. Usage: {usage}");

        Map<String, String> values = new LinkedHashMap<>();
        values.put("argument", "amount");
        values.put("usage", "/money give <target> <amount>");

        assertThat(messages.render(CxKey.MISSING_ARGUMENT, values))
                .isEqualTo("Missing amount. Usage: /money give <target> <amount>");
    }

    /** A player who types a coloured value must not be able to colour the message. */
    @Test
    void strips_the_colour_codes_out_of_a_value() {
        messages.set(CxKey.INVALID_NUMBER, "&c{value} is not a number.");

        assertThat(messages.render(CxKey.INVALID_NUMBER, Collections.singletonMap("value", "&aok")))
                .isEqualTo(SECTION + "cok is not a number.");
        assertThat(messages.render(CxKey.INVALID_NUMBER, Collections.singletonMap("value", SECTION + "aok")))
                .isEqualTo(SECTION + "cok is not a number.");
    }

    @Test
    void applies_the_overrides_a_configuration_holds() {
        Map<String, String> loaded = new LinkedHashMap<>();
        loaded.put("too-many-arguments", "&cToo much");
        loaded.put("invalid-number", "&cNot a number");

        assertThat(messages.setAll(loaded)).isEmpty();
        assertThat(messages.template(CxKey.TOO_MANY_ARGUMENTS)).isEqualTo("&cToo much");
        assertThat(messages.template(CxKey.INVALID_NUMBER)).isEqualTo("&cNot a number");
    }

    /** The library owns no logger, so a typo in a configuration comes back to the caller. */
    @Test
    void reports_a_configuration_key_that_matches_no_message() {
        Map<String, String> loaded = new LinkedHashMap<>();
        loaded.put("too-many-argument", "&cToo much");
        loaded.put("invalid-number", "&cNot a number");

        assertThat(messages.setAll(loaded)).containsExactly("too-many-argument");
        assertThat(messages.template(CxKey.TOO_MANY_ARGUMENTS))
                .isEqualTo(CxKey.TOO_MANY_ARGUMENTS.defaultMessage());
    }

    @Test
    void reports_unknown_keys_as_an_unmodifiable_list() {
        assertThatThrownBy(() -> messages.setAll(Collections.singletonMap("nope", "x")).add("other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /** One set of messages per plugin, so an override cannot leak into another. */
    @Test
    void instances_do_not_share_their_overrides() {
        messages.set(CxKey.TOO_MANY_ARGUMENTS, "&cToo much");

        assertThat(new CxMessages().template(CxKey.TOO_MANY_ARGUMENTS))
                .isEqualTo(CxKey.TOO_MANY_ARGUMENTS.defaultMessage());
    }

    @Test
    void every_default_renders_without_leaving_a_placeholder_unfilled() {
        Map<String, String> values = new HashMap<>();
        values.put("usage", "/money give <target> <amount>");
        values.put("argument", "amount");
        values.put("value", "lots");
        values.put("min", "1");
        values.put("max", "64");
        values.put("choices", "easy, hard");
        values.put("label", "money");

        for (CxKey key : CxKey.values()) {
            assertThat(messages.render(key, values)).as("rendered %s", key).doesNotContain("{");
        }
    }

    @Test
    void refuses_what_it_cannot_use() {
        assertThatThrownBy(() -> messages.set(null, "x")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> messages.set(CxKey.INVALID_NUMBER, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> messages.setAll(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> messages.template(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> messages.render(CxKey.INVALID_NUMBER, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> messages.render(CxKey.INVALID_NUMBER, Collections.singletonMap("value", null)))
                .isInstanceOf(NullPointerException.class);
    }
}
