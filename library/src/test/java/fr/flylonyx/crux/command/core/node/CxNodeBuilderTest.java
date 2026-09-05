package fr.flylonyx.crux.command.core.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.fixture.FakeArgumentType;

class CxNodeBuilderTest {

    private static CxNodeBuilder runnable(String name) {
        return CxNodeBuilder.literal(name).executes(context -> { });
    }

    @Nested
    class BuiltShape {

        @Test
        void keeps_the_declared_name_and_kind() {
            CxNode node = runnable("money").build();

            assertThat(node.name()).isEqualTo("money");
            assertThat(node.kind()).isEqualTo(CxNodeKind.LITERAL);
            assertThat(node.isExecutable()).isTrue();
        }

        @Test
        void reports_a_branch_without_a_handler_as_not_executable() {
            CxNode node = CxNodeBuilder.literal("money").then(runnable("top")).build();

            assertThat(node.isExecutable()).isFalse();
            assertThat(node.handler()).isEmpty();
        }

        @Test
        void carries_permission_and_description() {
            CxNode node = runnable("money").permission("crux.money").description("Balance").build();

            assertThat(node.permission()).contains("crux.money");
            assertThat(node.description()).isEqualTo("Balance");
        }

        @Test
        void treats_a_null_description_as_empty() {
            assertThat(runnable("money").description(null).build().description()).isEmpty();
        }

        @Test
        void treats_a_null_permission_as_no_requirement() {
            assertThat(runnable("money").permission(null).build().permission()).isEmpty();
        }

        @Test
        void keeps_literal_children_in_declaration_order() {
            CxNode node = CxNodeBuilder.literal("money")
                    .then(runnable("top"))
                    .then(runnable("give"))
                    .build();

            assertThat(node.literals()).extracting(CxNode::name).containsExactly("top", "give");
        }

        @Test
        void orders_argument_children_by_priority_highest_first() {
            CxNode node = CxNodeBuilder.literal("money")
                    .then(CxNodeBuilder.argument("low", new FakeArgumentType("low")).priority(1)
                            .executes(context -> { }))
                    .then(CxNodeBuilder.argument("high", new FakeArgumentType("high")).priority(9)
                            .executes(context -> { }))
                    .build();

            assertThat(node.arguments()).extracting(CxNode::name).containsExactly("high", "low");
        }

        @Test
        void describes_itself_by_kind() {
            assertThat(runnable("money").build()).hasToString("money");
            assertThat(CxNodeBuilder.argument("amount", new FakeArgumentType("int"))
                    .executes(context -> { }).build()).hasToString("<amount>");
        }
    }

    @Nested
    class Lookup {

        @Test
        void finds_a_literal_child_ignoring_case() {
            CxNode node = CxNodeBuilder.literal("money").then(runnable("Give")).build();

            assertThat(node.literal("give")).isPresent();
            assertThat(node.literal("GIVE")).isPresent();
        }

        @Test
        void finds_a_literal_child_through_an_alias() {
            CxNode node = CxNodeBuilder.literal("money")
                    .then(runnable("give").aliases("grant", "pay"))
                    .build();

            assertThat(node.literal("pay")).contains(node.literals().get(0));
        }

        @Test
        void finds_nothing_for_an_unknown_or_null_token() {
            CxNode node = CxNodeBuilder.literal("money").then(runnable("give")).build();

            assertThat(node.literal("take")).isEmpty();
            assertThat(node.literal(null)).isEmpty();
        }

        @Test
        void exposes_aliases_of_a_literal() {
            CxNode node = runnable("give").aliases("grant").build();

            assertThat(node.aliases()).containsExactly("grant");
        }
    }

    @Nested
    class OptionalArguments {

        @Test
        void carries_the_text_read_when_the_argument_is_left_out() {
            CxNode node = CxNodeBuilder.argument("name", new FakeArgumentType("word"))
                    .optional("home")
                    .executes(context -> { })
                    .build();

            assertThat(node.isOptional()).isTrue();
            assertThat(node.defaultValue()).isEqualTo("home");
        }

        @Test
        void reports_an_argument_without_a_default_as_required() {
            CxNode node = CxNodeBuilder.argument("name", new FakeArgumentType("word"))
                    .executes(context -> { })
                    .build();

            assertThat(node.isOptional()).isFalse();
            assertThat(node.defaultValue()).isNull();
        }

        @Test
        void accepts_an_optional_argument_leading_to_another_one() {
            CxNode node = CxNodeBuilder.literal("warp")
                    .then(CxNodeBuilder.argument("name", new FakeArgumentType("word")).optional("spawn")
                            .then(CxNodeBuilder.argument("world", new FakeArgumentType("word")).optional("world")
                                    .executes(context -> { })))
                    .build();

            assertThat(node.arguments()).singleElement().satisfies(name -> {
                assertThat(name.isOptional()).isTrue();
                assertThat(name.arguments().get(0).isOptional()).isTrue();
            });
        }

        @Test
        void describes_itself_in_brackets_rather_than_angles() {
            assertThat(CxNodeBuilder.argument("name", new FakeArgumentType("word"))
                    .optional("home").executes(context -> { }).build()).hasToString("[name]");
        }
    }

    @Nested
    class RejectedDeclarations {

        @Test
        void a_node_without_a_name() {
            assertThatThrownBy(() -> runnable("").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("needs a name");
        }

        @Test
        void a_name_containing_whitespace() {
            assertThatThrownBy(() -> runnable("money give").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("whitespace");
        }

        @Test
        void a_node_that_neither_runs_nor_branches() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("money").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("could never do anything");
        }

        @Test
        void an_alias_repeating_the_name() {
            assertThatThrownBy(() -> runnable("give").aliases("GIVE").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("more than once");
        }

        @Test
        void a_repeated_alias() {
            assertThatThrownBy(() -> runnable("give").aliases("pay", "pay").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("more than once");
        }

        @Test
        void an_empty_alias() {
            assertThatThrownBy(() -> runnable("give").aliases("").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("needs a name");
        }

        @Test
        void an_argument_declaring_aliases() {
            assertThatThrownBy(() -> CxNodeBuilder.argument("amount", new FakeArgumentType("int"))
                    .aliases("value").executes(context -> { }).build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("only a literal can have them");
        }

        @Test
        void an_argument_without_a_type() {
            assertThatThrownBy(() -> CxNodeBuilder.argument("amount", null)
                    .executes(context -> { }).build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("has no type");
        }

        @Test
        void a_literal_declaring_a_default() {
            assertThatThrownBy(() -> runnable("give").optional("all").build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("only an argument can be left out");
        }

        @Test
        void a_default_that_is_null() {
            assertThatThrownBy(() -> CxNodeBuilder.argument("name", new FakeArgumentType("word")).optional(null))
                    .isInstanceOf(NullPointerException.class);
        }

        /** Leaving the argument out would then stop at a node that cannot run. */
        @Test
        void an_optional_argument_followed_by_a_required_one() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("warp")
                    .then(CxNodeBuilder.argument("name", new FakeArgumentType("word")).optional("spawn")
                            .then(CxNodeBuilder.argument("world", new FakeArgumentType("word"))
                                    .executes(context -> { })))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("could never be read");
        }

        @Test
        void an_argument_type_consuming_nothing() {
            assertThatThrownBy(() -> CxNodeBuilder.argument("amount", new FakeArgumentType("int", 0))
                    .executes(context -> { }).build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("cannot consume anything");
        }

        @Test
        void two_literals_claiming_the_same_word() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("money")
                    .then(runnable("give"))
                    .then(runnable("GIVE"))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("could never be reached");
        }

        @Test
        void a_literal_clashing_with_a_sibling_alias() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("money")
                    .then(runnable("give").aliases("pay"))
                    .then(runnable("pay"))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("could never be reached");
        }

        @Test
        void two_arguments_sharing_a_name() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("money")
                    .then(CxNodeBuilder.argument("value", new FakeArgumentType("int"))
                            .executes(context -> { }))
                    .then(CxNodeBuilder.argument("value", new FakeArgumentType("word"))
                            .executes(context -> { }))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("could not tell them apart");
        }

        @Test
        void an_argument_sitting_beside_a_greedy_one() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("say")
                    .then(CxNodeBuilder.argument("message", FakeArgumentType.greedy("text"))
                            .executes(context -> { }))
                    .then(CxNodeBuilder.argument("target", new FakeArgumentType("word"))
                            .executes(context -> { }))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("already consumes everything");
        }

        @Test
        void a_greedy_argument_declared_after_another_argument() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("say")
                    .then(CxNodeBuilder.argument("target", new FakeArgumentType("word"))
                            .executes(context -> { }))
                    .then(CxNodeBuilder.argument("message", FakeArgumentType.greedy("text"))
                            .executes(context -> { }))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("already consumes everything");
        }

        @Test
        void a_greedy_argument_with_children() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("say")
                    .then(CxNodeBuilder.argument("message", FakeArgumentType.greedy("text"))
                            .then(runnable("loud")))
                    .build())
                    .isInstanceOf(CxDefinitionException.class)
                    .hasMessageContaining("nothing can follow it");
        }

        @Test
        void a_null_child() {
            assertThatThrownBy(() -> CxNodeBuilder.literal("money").then(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void null_aliases() {
            assertThatThrownBy(() -> runnable("give").aliases((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
