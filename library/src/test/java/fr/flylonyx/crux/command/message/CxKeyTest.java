package fr.flylonyx.crux.command.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CxKeyTest {

    @Test
    void every_key_has_a_default_message() {
        for (CxKey key : CxKey.values()) {
            assertThat(key.defaultMessage())
                    .as("default message of %s", key)
                    .isNotEmpty();
        }
    }

    @Test
    void every_configuration_key_is_kebab_case() {
        for (CxKey key : CxKey.values()) {
            assertThat(key.configKey())
                    .as("configuration key of %s", key)
                    .matches("[a-z]+(-[a-z]+)*");
        }
    }

    @Test
    void configuration_keys_are_unique() {
        Set<String> seen = new HashSet<>();

        for (CxKey key : CxKey.values()) {
            assertThat(seen.add(key.configKey()))
                    .as("configuration key of %s is not already used", key)
                    .isTrue();
        }
    }

    /**
     * A mismatch between the two would silently move an override onto the wrong message.
     */
    @Test
    void a_configuration_key_matches_the_constant_it_belongs_to() {
        for (CxKey key : CxKey.values()) {
            assertThat(key.configKey().replace('-', '_').toUpperCase(Locale.ROOT))
                    .isEqualTo(key.name());
        }
    }

    @Test
    void constants_resolve_by_name() {
        for (CxKey key : CxKey.values()) {
            assertThat(CxKey.valueOf(key.name())).isSameAs(key);
        }
    }

    @Test
    void placeholders_are_balanced() {
        for (CxKey key : CxKey.values()) {
            String message = key.defaultMessage();
            long opening = message.chars().filter(character -> character == '{').count();
            long closing = message.chars().filter(character -> character == '}').count();

            assertThat(opening)
                    .as("placeholder braces in %s", key)
                    .isEqualTo(closing);
        }
    }
}
