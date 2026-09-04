package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CxTokensTest {

    @Nested
    class Construction {

        @Test
        void rejects_null_values() {
            assertThatThrownBy(() -> CxTokens.of(null, false))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void copies_the_source_list_defensively() {
            List<String> source = new ArrayList<>(Arrays.asList("give", "Notch"));
            CxTokens tokens = CxTokens.of(source, false);

            source.add("5");

            assertThat(tokens.values()).containsExactly("give", "Notch");
        }

        @Test
        void treats_no_tokens_and_no_separator_as_empty() {
            CxTokens tokens = CxTokens.of(Collections.<String>emptyList(), false);

            assertThat(tokens.isEmpty()).isTrue();
            assertThat(tokens.trailingSeparator()).isFalse();
        }

        @Test
        void keeps_a_trailing_separator_even_with_no_tokens() {
            CxTokens tokens = CxTokens.of(Collections.<String>emptyList(), true);

            assertThat(tokens.isEmpty()).isTrue();
            assertThat(tokens.trailingSeparator()).isTrue();
        }
    }

    @Nested
    class Equality {

        @Test
        void an_instance_equals_itself() {
            CxTokens tokens = CxTokens.of(Arrays.asList("give"), false);

            assertThat(tokens).isEqualTo(tokens);
        }

        @Test
        void instances_with_the_same_tokens_and_separator_are_equal() {
            CxTokens left = CxTokens.of(Arrays.asList("give", "Notch"), true);
            CxTokens right = CxTokens.of(Arrays.asList("give", "Notch"), true);

            assertThat(left).isEqualTo(right);
            assertThat(left.hashCode()).isEqualTo(right.hashCode());
        }

        @Test
        void instances_with_different_tokens_are_not_equal() {
            CxTokens left = CxTokens.of(Arrays.asList("give"), false);
            CxTokens right = CxTokens.of(Arrays.asList("take"), false);

            assertThat(left).isNotEqualTo(right);
        }

        @Test
        void instances_differing_only_by_separator_are_not_equal() {
            CxTokens left = CxTokens.of(Arrays.asList("give"), false);
            CxTokens right = CxTokens.of(Arrays.asList("give"), true);

            assertThat(left).isNotEqualTo(right);
        }

        @Test
        void an_instance_is_not_equal_to_null_or_another_type() {
            CxTokens tokens = CxTokens.of(Arrays.asList("give"), false);

            assertThat(tokens).isNotEqualTo(null);
            assertThat(tokens).isNotEqualTo("give");
        }
    }

    @Nested
    class Description {

        @Test
        void lists_the_tokens() {
            assertThat(CxTokens.of(Arrays.asList("give", "Notch"), false).toString())
                    .isEqualTo("CxTokens[give, Notch]");
        }

        @Test
        void marks_a_trailing_separator() {
            assertThat(CxTokens.of(Arrays.asList("give"), true).toString())
                    .isEqualTo("CxTokens[give]+");
        }
    }
}
