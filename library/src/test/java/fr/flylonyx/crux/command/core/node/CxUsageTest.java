package fr.flylonyx.crux.command.core.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.fixture.FakeArgumentType;

class CxUsageTest {

    private static CxNodeBuilder word(String name) {
        return CxNodeBuilder.argument(name, new FakeArgumentType("word"));
    }

    @Test
    void shows_a_command_that_takes_nothing() {
        CxNode money = CxNodeBuilder.literal("money").executes(context -> { }).build();

        assertThat(CxUsage.of("money", Collections.singletonList(money))).isEqualTo("/money");
    }

    @Test
    void shows_the_label_the_sender_typed_rather_than_the_registered_name() {
        CxNode money = CxNodeBuilder.literal("money").executes(context -> { }).build();

        assertThat(CxUsage.of("bal", Collections.singletonList(money))).isEqualTo("/bal");
    }

    @Test
    void shows_the_path_that_was_walked() {
        CxNode money = CxNodeBuilder.literal("money")
                .then(CxNodeBuilder.literal("give")
                        .then(word("target").then(word("amount").executes(context -> { }))))
                .build();
        CxNode give = money.literals().get(0);
        CxNode target = give.arguments().get(0);

        assertThat(CxUsage.of("money", Arrays.asList(money, give, target)))
                .isEqualTo("/money give <target> <amount>");
    }

    /** A missing argument is only useful to report alongside the ones still expected. */
    @Test
    void carries_on_to_whatever_still_has_to_be_typed() {
        CxNode money = CxNodeBuilder.literal("money")
                .then(CxNodeBuilder.literal("give")
                        .then(word("target").then(word("amount").executes(context -> { }))))
                .build();

        assertThat(CxUsage.of("money", Collections.singletonList(money)))
                .isEqualTo("/money give <target> <amount>");
    }

    @Test
    void marks_an_optional_argument_with_brackets() {
        CxNode home = CxNodeBuilder.literal("home")
                .then(word("name").optional("home").executes(context -> { }))
                .build();

        assertThat(CxUsage.of("home", Collections.singletonList(home))).isEqualTo("/home [name]");
    }

    @Test
    void stops_as_soon_as_the_command_would_run() {
        CxNode money = CxNodeBuilder.literal("money")
                .executes(context -> { })
                .then(CxNodeBuilder.literal("top").executes(context -> { }))
                .build();

        assertThat(CxUsage.of("money", Collections.singletonList(money))).isEqualTo("/money");
    }

    /** One correct form of the command, not the list of them. */
    @Test
    void follows_the_first_branch_when_a_node_offers_several() {
        CxNode money = CxNodeBuilder.literal("money")
                .then(CxNodeBuilder.literal("give").executes(context -> { }))
                .then(CxNodeBuilder.literal("top").executes(context -> { }))
                .build();

        assertThat(CxUsage.of("money", Collections.singletonList(money))).isEqualTo("/money give");
    }

    @Test
    void follows_an_argument_when_a_node_offers_no_literal() {
        CxNode pay = CxNodeBuilder.literal("pay")
                .then(word("target").executes(context -> { }))
                .build();

        assertThat(CxUsage.of("pay", Collections.singletonList(pay))).isEqualTo("/pay <target>");
    }

    @Test
    void refuses_what_it_cannot_use() {
        CxNode money = CxNodeBuilder.literal("money").executes(context -> { }).build();

        assertThatThrownBy(() -> CxUsage.of(null, Collections.singletonList(money)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxUsage.of("money", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxUsage.of("money", Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
