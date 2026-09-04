package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.core.node.CxNodeBuilder;
import fr.flylonyx.crux.command.fixture.FakeArgumentType;
import fr.flylonyx.crux.command.message.CxKey;

class CxMatchResultTest {

    private static final CxNode NODE = CxNodeBuilder.literal("money").executes(context -> { }).build();

    private static final CxNode ARGUMENT = CxNodeBuilder
            .argument("target", new FakeArgumentType("word"))
            .executes(context -> { })
            .build();

    @Nested
    class Success {

        @Test
        void carries_the_node_and_its_spans() {
            CxArgumentSpan span = new CxArgumentSpan(ARGUMENT, 1, 1);

            CxMatchResult result = CxMatchResult.matched(NODE, Collections.singletonList(span));

            assertThat(result.isMatched()).isTrue();
            assertThat(result.node()).isSameAs(NODE);
            assertThat(result.arguments()).containsExactly(span);
            assertThat(result.depth()).isZero();
        }

        @Test
        void copies_the_spans_defensively() {
            CxMatchResult result = CxMatchResult.matched(NODE,
                    new ArrayList<>(Collections.singletonList(new CxArgumentSpan(ARGUMENT, 0, 1))));

            assertThatThrownBy(() -> result.arguments().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void has_no_failure_to_report() {
            CxMatchResult result = CxMatchResult.matched(NODE, Collections.<CxArgumentSpan>emptyList());

            assertThatThrownBy(result::failure)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no failure");
        }

        @Test
        void describes_itself() {
            CxMatchResult result = CxMatchResult.matched(NODE, Collections.<CxArgumentSpan>emptyList());

            assertThat(result).hasToString("matched money with []");
        }

        @Test
        void rejects_missing_parts() {
            assertThatThrownBy(() -> CxMatchResult.matched(null, Collections.<CxArgumentSpan>emptyList()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CxMatchResult.matched(NODE, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Failure {

        @Test
        void carries_the_reason_the_detail_and_the_depth() {
            CxMatchResult result = CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "nope", 2);

            assertThat(result.isMatched()).isFalse();
            assertThat(result.failure()).isEqualTo(CxKey.UNKNOWN_SUBCOMMAND);
            assertThat(result.detail()).isEqualTo("nope");
            assertThat(result.depth()).isEqualTo(2);
            assertThat(result.arguments()).isEmpty();
        }

        @Test
        void has_no_node_to_report() {
            CxMatchResult result = CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "amount", 1);

            assertThatThrownBy(result::node)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no matched node");
        }

        @Test
        void describes_itself_with_and_without_a_detail() {
            assertThat(CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "amount", 1))
                    .hasToString("failed MISSING_ARGUMENT at 1 on amount");
            assertThat(CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, null, 0))
                    .hasToString("failed UNKNOWN_SUBCOMMAND at 0");
        }

        @Test
        void rejects_a_missing_reason() {
            assertThatThrownBy(() -> CxMatchResult.failed(null, "nope", 0))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Spans {

        @Test
        void extract_the_tokens_they_claimed() {
            CxTokens tokens = CxTokenizer.tokenize("give Notch 5");
            CxArgumentSpan span = new CxArgumentSpan(ARGUMENT, 1, 2);

            assertThat(span.extractFrom(tokens)).containsExactly("Notch", "5");
        }

        @Test
        void expose_their_node_and_bounds() {
            CxArgumentSpan span = new CxArgumentSpan(ARGUMENT, 3, 2);

            assertThat(span.node()).isSameAs(ARGUMENT);
            assertThat(span.first()).isEqualTo(3);
            assertThat(span.count()).isEqualTo(2);
            assertThat(span).hasToString("target[3..4]");
        }

        @Test
        void reject_bounds_that_could_not_describe_a_claim() {
            assertThatThrownBy(() -> new CxArgumentSpan(null, 0, 1))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new CxArgumentSpan(ARGUMENT, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new CxArgumentSpan(ARGUMENT, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void reject_extraction_from_tokens_they_do_not_fit() {
            CxArgumentSpan span = new CxArgumentSpan(ARGUMENT, 0, 2);

            assertThatThrownBy(() -> span.extractFrom(CxTokenizer.tokenize("give")))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> span.extractFrom(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void are_unmodifiable_once_extracted() {
            CxArgumentSpan span = new CxArgumentSpan(ARGUMENT, 0, 1);

            assertThatThrownBy(() -> span.extractFrom(CxTokenizer.tokenize("give")).add("x"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class Comparison {

        @Test
        void a_deeper_failure_is_preferred_over_a_shallower_one() {
            CxMatchResult shallow = CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "a", 1);
            CxMatchResult deep = CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "b", 3);

            assertThat(deep.isMoreInformativeThan(shallow)).isTrue();
            assertThat(shallow.isMoreInformativeThan(deep)).isFalse();
            assertThat(shallow.isMoreInformativeThan(null)).isTrue();
        }

        @Test
        void equally_deep_failures_keep_the_one_already_held() {
            CxMatchResult first = CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "a", 2);
            CxMatchResult second = CxMatchResult.failed(CxKey.TOO_MANY_ARGUMENTS, "b", 2);

            assertThat(second.isMoreInformativeThan(first)).isFalse();
        }
    }
}
