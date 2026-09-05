package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.core.node.CxNodeBuilder;
import fr.flylonyx.crux.command.fixture.FakeArgumentType;
import fr.flylonyx.crux.command.message.CxKey;

class CxMatchResultTest {

    private static final CxNode NODE = CxNodeBuilder.literal("money").executes(context -> { }).build();

    private static final List<CxNode> PATH = Collections.singletonList(NODE);

    private static final CxNode ARGUMENT = CxNodeBuilder
            .argument("target", new FakeArgumentType("word"))
            .executes(context -> { })
            .build();

    @Nested
    class Success {

        @Test
        void carries_the_path_its_spans_and_the_node_to_run() {
            CxArgumentSpan span = new CxArgumentSpan(ARGUMENT, 1, 1);

            CxMatchResult result = CxMatchResult.matched(Arrays.asList(NODE, ARGUMENT),
                    Collections.singletonList(span));

            assertThat(result.isMatched()).isTrue();
            assertThat(result.path()).containsExactly(NODE, ARGUMENT);
            assertThat(result.node()).isSameAs(ARGUMENT);
            assertThat(result.arguments()).containsExactly(span);
            assertThat(result.depth()).isZero();
        }

        @Test
        void exposes_the_path_as_an_unmodifiable_list() {
            CxMatchResult result = CxMatchResult.matched(new ArrayList<>(PATH), Collections.emptyList());

            assertThatThrownBy(() -> result.path().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void copies_the_spans_defensively() {
            CxMatchResult result = CxMatchResult.matched(PATH,
                    new ArrayList<>(Collections.singletonList(new CxArgumentSpan(ARGUMENT, 0, 1))));

            assertThatThrownBy(() -> result.arguments().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void has_no_failure_to_report() {
            CxMatchResult result = CxMatchResult.matched(PATH, Collections.emptyList());

            assertThatThrownBy(result::failure)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no failure");
        }

        @Test
        void describes_itself() {
            CxMatchResult result = CxMatchResult.matched(PATH, Collections.emptyList());

            assertThat(result).hasToString("matched money with []");
        }

        @Test
        void rejects_missing_parts() {
            assertThatThrownBy(() -> CxMatchResult.matched(null, Collections.emptyList()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CxMatchResult.matched(PATH, null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CxMatchResult.matched(Collections.emptyList(), Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Failure {

        @Test
        void carries_the_reason_the_detail_and_the_depth() {
            CxMatchResult result = CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "nope", 2, PATH);

            assertThat(result.isMatched()).isFalse();
            assertThat(result.failure()).isEqualTo(CxKey.UNKNOWN_SUBCOMMAND);
            assertThat(result.detail()).isEqualTo("nope");
            assertThat(result.depth()).isEqualTo(2);
            assertThat(result.arguments()).isEmpty();
        }

        @Test
        void has_no_node_to_report() {
            CxMatchResult result = CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "amount", 1, PATH);

            assertThatThrownBy(result::node)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no matched node");
        }

        @Test
        void describes_itself_with_and_without_a_detail() {
            assertThat(CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "amount", 1, PATH))
                    .hasToString("failed MISSING_ARGUMENT at 1 on amount");
            assertThat(CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, null, 0, PATH))
                    .hasToString("failed UNKNOWN_SUBCOMMAND at 0");
        }

        @Test
        void carries_the_path_routing_gave_up_on() {
            CxMatchResult result = CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "amount", 1, PATH);

            assertThat(result.path()).containsExactly(NODE);
        }

        @Test
        void rejects_a_missing_reason_or_path() {
            assertThatThrownBy(() -> CxMatchResult.failed(null, "nope", 0, PATH))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "nope", 0, null))
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
        void stand_for_an_argument_the_sender_left_out() {
            CxArgumentSpan span = CxArgumentSpan.omitted(ARGUMENT, 2);

            assertThat(span.isOmitted()).isTrue();
            assertThat(span.count()).isZero();
            assertThat(span.first()).isEqualTo(2);
            assertThat(span.extractFrom(CxTokenizer.tokenize("give"))).isEmpty();
            assertThat(span).hasToString("target[omitted]");
        }

        @Test
        void reject_an_omission_that_could_not_have_happened() {
            assertThatThrownBy(() -> CxArgumentSpan.omitted(null, 0))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CxArgumentSpan.omitted(ARGUMENT, -1))
                    .isInstanceOf(IllegalArgumentException.class);
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
            CxMatchResult shallow = CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "a", 1, PATH);
            CxMatchResult deep = CxMatchResult.failed(CxKey.MISSING_ARGUMENT, "b", 3, PATH);

            assertThat(deep.isMoreInformativeThan(shallow)).isTrue();
            assertThat(shallow.isMoreInformativeThan(deep)).isFalse();
            assertThat(shallow.isMoreInformativeThan(null)).isTrue();
        }

        @Test
        void equally_deep_failures_keep_the_one_already_held() {
            CxMatchResult first = CxMatchResult.failed(CxKey.UNKNOWN_SUBCOMMAND, "a", 2, PATH);
            CxMatchResult second = CxMatchResult.failed(CxKey.TOO_MANY_ARGUMENTS, "b", 2, PATH);

            assertThat(second.isMoreInformativeThan(first)).isFalse();
        }
    }
}
