package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CxTokenizerTest {

    @Nested
    class Splitting {

        @Test
        void splits_on_single_spaces() {
            assertThat(CxTokenizer.tokenize("give Notch 5").values())
                    .containsExactly("give", "Notch", "5");
        }

        @Test
        void collapses_repeated_spaces() {
            assertThat(CxTokenizer.tokenize("give   Notch    5").values())
                    .containsExactly("give", "Notch", "5");
        }

        @Test
        void ignores_leading_spaces() {
            assertThat(CxTokenizer.tokenize("   give Notch").values())
                    .containsExactly("give", "Notch");
        }

        @Test
        void returns_no_tokens_for_empty_input() {
            assertThat(CxTokenizer.tokenize("").values()).isEmpty();
        }

        @Test
        void returns_no_tokens_for_blank_input() {
            assertThat(CxTokenizer.tokenize("     ").values()).isEmpty();
        }
    }

    @Nested
    class TrailingSeparator {

        @Test
        void is_reported_when_input_ends_with_a_space() {
            assertThat(CxTokenizer.tokenize("give ").trailingSeparator()).isTrue();
        }

        @Test
        void is_not_reported_when_input_ends_mid_token() {
            assertThat(CxTokenizer.tokenize("give").trailingSeparator()).isFalse();
        }

        @Test
        void is_reported_for_blank_input() {
            assertThat(CxTokenizer.tokenize("   ").trailingSeparator()).isTrue();
        }

        @Test
        void is_not_reported_for_empty_input() {
            assertThat(CxTokenizer.tokenize("").trailingSeparator()).isFalse();
        }

        @Test
        void is_not_reported_when_a_quoted_token_ends_the_input() {
            assertThat(CxTokenizer.tokenize("say \"hello world\"").trailingSeparator()).isFalse();
        }
    }

    @Nested
    class Quoting {

        @Test
        void keeps_a_quoted_section_as_one_token() {
            assertThat(CxTokenizer.tokenize("say \"hello world\"").values())
                    .containsExactly("say", "hello world");
        }

        @Test
        void allows_a_quoted_section_to_be_empty() {
            assertThat(CxTokenizer.tokenize("say \"\"").values())
                    .containsExactly("say", "");
        }

        @Test
        void allows_quotes_to_open_mid_token() {
            assertThat(CxTokenizer.tokenize("say pre\"fix suffix\"").values())
                    .containsExactly("say", "prefix suffix");
        }

        @Test
        void keeps_an_unterminated_quote_as_one_token() {
            // A player is still typing. Failing here would break tab completion.
            assertThat(CxTokenizer.tokenize("say \"hello wor").values())
                    .containsExactly("say", "hello wor");
        }
    }

    @Nested
    class Escaping {

        @Test
        void escapes_a_quote_inside_a_quoted_section() {
            assertThat(CxTokenizer.tokenize("say \"he said \\\"hi\\\"\"").values())
                    .containsExactly("say", "he said \"hi\"");
        }

        @Test
        void escapes_a_space_outside_a_quoted_section() {
            assertThat(CxTokenizer.tokenize("say hello\\ world").values())
                    .containsExactly("say", "hello world");
        }

        @Test
        void escapes_a_backslash() {
            assertThat(CxTokenizer.tokenize("say back\\\\slash").values())
                    .containsExactly("say", "back\\slash");
        }

        @Test
        void keeps_a_trailing_backslash_literal() {
            assertThat(CxTokenizer.tokenize("say trailing\\").values())
                    .containsExactly("say", "trailing\\");
        }
    }

    @Nested
    class Contract {

        @Test
        void rejects_null_input() {
            assertThatThrownBy(() -> CxTokenizer.tokenize(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void exposes_tokens_as_an_unmodifiable_list() {
            assertThatThrownBy(() -> CxTokenizer.tokenize("give").values().add("extra"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void reports_its_size_and_indexed_access() {
            CxTokens tokens = CxTokenizer.tokenize("give Notch");

            assertThat(tokens.size()).isEqualTo(2);
            assertThat(tokens.get(0)).isEqualTo("give");
            assertThat(tokens.get(1)).isEqualTo("Notch");
        }

        @Test
        void reports_emptiness() {
            assertThat(CxTokenizer.tokenize("").isEmpty()).isTrue();
            assertThat(CxTokenizer.tokenize("give").isEmpty()).isFalse();
        }

        @Test
        void rejects_an_index_outside_the_token_range() {
            CxTokens tokens = CxTokenizer.tokenize("give");

            assertThatThrownBy(() -> tokens.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }
}
