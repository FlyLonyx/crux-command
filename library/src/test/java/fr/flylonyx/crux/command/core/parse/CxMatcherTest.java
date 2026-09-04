package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.core.node.CxNodeBuilder;
import fr.flylonyx.crux.command.fixture.FakeArgumentType;
import fr.flylonyx.crux.command.message.CxKey;

class CxMatcherTest {

    private static CxNodeBuilder literal(String name) {
        return CxNodeBuilder.literal(name).executes(context -> { });
    }

    private static CxNodeBuilder word(String name) {
        return CxNodeBuilder.argument(name, new FakeArgumentType("word")).executes(context -> { });
    }

    /**
     * An argument that only leads somewhere: reaching it without going further is a
     * missing argument, not a runnable command.
     */
    private static CxNodeBuilder wordBranch(String name) {
        return CxNodeBuilder.argument(name, new FakeArgumentType("word"));
    }

    private static CxMatchResult route(CxNode root, String input) {
        return CxMatcher.match(root, CxTokenizer.tokenize(input));
    }

    @Nested
    class Routing {

        @Test
        void reaches_the_root_when_nothing_follows_it() {
            CxNode root = literal("money").build();

            assertThat(route(root, "").node()).isSameAs(root);
        }

        @Test
        void reaches_a_literal_child() {
            CxNode root = CxNodeBuilder.literal("money").then(literal("top")).build();

            assertThat(route(root, "top").node().name()).isEqualTo("top");
        }

        @Test
        void reaches_a_nested_literal() {
            CxNode root = CxNodeBuilder.literal("gang")
                    .then(CxNodeBuilder.literal("admin").then(literal("reload")))
                    .build();

            assertThat(route(root, "admin reload").node().name()).isEqualTo("reload");
        }

        @Test
        void reaches_a_literal_through_an_alias_ignoring_case() {
            CxNode root = CxNodeBuilder.literal("money")
                    .then(literal("give").aliases("pay"))
                    .build();

            assertThat(route(root, "PAY").node().name()).isEqualTo("give");
        }

        @Test
        void prefers_a_literal_over_an_argument_that_would_also_match() {
            CxNode root = CxNodeBuilder.literal("money")
                    .then(literal("top"))
                    .then(word("target"))
                    .build();

            assertThat(route(root, "top").node().name()).isEqualTo("top");
        }

        @Test
        void falls_back_to_an_argument_when_the_literal_branch_leads_nowhere() {
            CxNode root = CxNodeBuilder.literal("money")
                    .then(CxNodeBuilder.literal("give").then(literal("all")))
                    .then(word("target"))
                    .build();

            assertThat(route(root, "give").node().name()).isEqualTo("target");
        }
    }

    @Nested
    class Arguments {

        @Test
        void records_the_span_each_argument_claimed() {
            CxNode root = CxNodeBuilder.literal("money")
                    .then(CxNodeBuilder.literal("give").then(word("target").then(word("amount"))))
                    .build();

            CxMatchResult result = route(root, "give Notch 5");

            assertThat(result.arguments()).extracting(CxArgumentSpan::node).extracting(CxNode::name)
                    .containsExactly("target", "amount");
            assertThat(result.arguments()).extracting(CxArgumentSpan::first).containsExactly(1, 2);
        }

        @Test
        void lets_one_argument_claim_several_tokens() {
            CxNode root = CxNodeBuilder.literal("teleport")
                    .then(CxNodeBuilder.argument("destination", new FakeArgumentType("location", 3))
                            .executes(context -> { }))
                    .build();

            CxMatchResult result = route(root, "10 64 -20");

            assertThat(result.isMatched()).isTrue();
            assertThat(result.arguments()).singleElement()
                    .satisfies(span -> {
                        assertThat(span.first()).isZero();
                        assertThat(span.count()).isEqualTo(3);
                    });
        }

        @Test
        void lets_a_greedy_argument_claim_everything_that_remains() {
            CxNode root = CxNodeBuilder.literal("say")
                    .then(CxNodeBuilder.argument("message", FakeArgumentType.greedy("text"))
                            .executes(context -> { }))
                    .build();

            CxMatchResult result = route(root, "hello there world");

            assertThat(result.arguments()).singleElement()
                    .satisfies(span -> assertThat(span.count()).isEqualTo(3));
        }

        @Test
        void keeps_a_quoted_argument_as_one_token() {
            CxNode root = CxNodeBuilder.literal("warp")
                    .then(CxNodeBuilder.literal("set").then(word("name")))
                    .build();

            CxMatchResult result = route(root, "set \"spawn area\"");
            CxTokens tokens = CxTokenizer.tokenize("set \"spawn area\"");

            assertThat(result.arguments().get(0).extractFrom(tokens)).containsExactly("spawn area");
        }
    }

    @Nested
    class Failures {

        @Test
        void reports_an_unknown_subcommand() {
            CxNode root = CxNodeBuilder.literal("money").then(literal("top")).build();

            CxMatchResult result = route(root, "nope");

            assertThat(result.isMatched()).isFalse();
            assertThat(result.failure()).isEqualTo(CxKey.UNKNOWN_SUBCOMMAND);
            assertThat(result.detail()).isEqualTo("nope");
        }

        @Test
        void reports_surplus_input_after_a_complete_command() {
            CxNode root = literal("money").build();

            CxMatchResult result = route(root, "extra");

            assertThat(result.failure()).isEqualTo(CxKey.TOO_MANY_ARGUMENTS);
            assertThat(result.detail()).isEqualTo("extra");
        }

        @Test
        void reports_a_missing_argument_when_input_runs_out() {
            CxNode root = CxNodeBuilder.literal("money")
                    .then(CxNodeBuilder.literal("give").then(word("target")))
                    .build();

            CxMatchResult result = route(root, "give");

            assertThat(result.failure()).isEqualTo(CxKey.MISSING_ARGUMENT);
            assertThat(result.detail()).isEqualTo("target");
        }

        @Test
        void reports_a_missing_argument_when_too_few_tokens_remain_for_it() {
            CxNode root = CxNodeBuilder.literal("teleport")
                    .then(CxNodeBuilder.argument("destination", new FakeArgumentType("location", 3))
                            .executes(context -> { }))
                    .build();

            CxMatchResult result = route(root, "10 64");

            assertThat(result.failure()).isEqualTo(CxKey.MISSING_ARGUMENT);
            assertThat(result.detail()).isEqualTo("destination");
        }

        @Test
        void reports_a_branch_that_needs_a_subcommand() {
            CxNode root = CxNodeBuilder.literal("money").then(literal("top")).build();

            CxMatchResult result = route(root, "");

            assertThat(result.failure()).isEqualTo(CxKey.UNKNOWN_SUBCOMMAND);
            assertThat(result.detail()).isNull();
        }

        /**
         * Two branches fail; the sender's real mistake is the one further in. Reporting the
         * shallower failure would tell them {@code give} is unknown when it is not.
         */
        @Test
        void reports_the_failure_that_got_furthest() {
            CxNode root = CxNodeBuilder.literal("money")
                    .then(literal("top"))
                    .then(CxNodeBuilder.literal("give").then(wordBranch("target").then(word("amount"))))
                    .build();

            CxMatchResult result = route(root, "give Notch");

            assertThat(result.failure()).isEqualTo(CxKey.MISSING_ARGUMENT);
            assertThat(result.detail()).isEqualTo("amount");
            assertThat(result.depth()).isEqualTo(2);
        }
    }

    @Nested
    class Contract {

        @Test
        void rejects_a_null_root_or_tokens() {
            CxNode root = literal("money").build();

            assertThatThrownBy(() -> CxMatcher.match(null, CxTokenizer.tokenize("")))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CxMatcher.match(root, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
