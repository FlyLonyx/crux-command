package fr.flylonyx.crux.command.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.arg.CxArgumentTypes;
import fr.flylonyx.crux.command.core.arg.CxInput;
import fr.flylonyx.crux.command.core.arg.CxParseException;
import fr.flylonyx.crux.command.core.node.CxNode;
import fr.flylonyx.crux.command.core.node.CxNodeBuilder;
import fr.flylonyx.crux.command.message.CxKey;
import fr.flylonyx.crux.command.message.CxMessages;

class CxFailuresTest {

    private static final String SECTION = "\u00A7";

    private static final CxNode MONEY = CxNodeBuilder.literal("money")
            .then(CxNodeBuilder.literal("top").executes(context -> { }))
            .then(CxNodeBuilder.literal("give")
                    .then(CxNodeBuilder.argument("target", CxArgumentTypes.word())
                            .then(CxNodeBuilder.argument("amount", CxArgumentTypes.integer(1, 64))
                                    .executes(context -> { }))))
            .build();

    private final CxMessages messages = new CxMessages();

    private CxMatchResult route(String line) {
        return CxMatcher.match(MONEY, CxTokenizer.tokenize(line));
    }

    @Test
    void describes_an_unknown_subcommand_with_a_usage_that_works() {
        messages.set(CxKey.UNKNOWN_SUBCOMMAND, "Unknown. Usage: {usage}");

        assertThat(CxFailures.describe(messages, "money", route("nope"))).isEqualTo("Unknown. Usage: /money top");
    }

    @Test
    void describes_a_missing_argument_by_name_and_shows_what_follows_it() {
        messages.set(CxKey.MISSING_ARGUMENT, "Missing {argument}. Usage: {usage}");

        assertThat(CxFailures.describe(messages, "money", route("give")))
                .isEqualTo("Missing target. Usage: /money give <target> <amount>");
    }

    @Test
    void describes_surplus_input() {
        messages.set(CxKey.TOO_MANY_ARGUMENTS, "Too many. Usage: {usage}");

        assertThat(CxFailures.describe(messages, "money", route("top extra"))).isEqualTo("Too many. Usage: /money top");
    }

    @Test
    void uses_the_label_the_sender_typed() {
        messages.set(CxKey.UNKNOWN_SUBCOMMAND, "Usage: {usage}");

        assertThat(CxFailures.describe(messages, "bal", route("nope"))).isEqualTo("Usage: /bal top");
    }

    @Test
    void offers_the_label_on_its_own_to_an_override() {
        messages.set(CxKey.UNKNOWN_SUBCOMMAND, "Try /{label} help");

        assertThat(CxFailures.describe(messages, "money", route("nope"))).isEqualTo("Try /money help");
    }

    @Test
    void translates_the_colour_codes_of_the_message() {
        assertThat(CxFailures.describe(messages, "money", route("nope"))).startsWith(SECTION + "c");
    }

    @Test
    void describes_a_value_that_could_not_be_read() {
        messages.set(CxKey.INVALID_NUMBER, "{value} is not a number for {argument}. Usage: {usage}");

        CxParseException failure = CxParseException.of(CxKey.INVALID_NUMBER, CxInput.of("amount", "lots"));

        assertThat(CxFailures.describe(messages, "money", route("give Notch 5"), failure))
                .isEqualTo("lots is not a number for amount. Usage: /money give <target> <amount>");
    }

    @Test
    void describes_a_value_that_was_out_of_range() {
        messages.set(CxKey.NUMBER_TOO_HIGH, "{argument} tops out at {max}. Usage: {usage}");

        CxParseException failure = CxParseException.of(CxKey.NUMBER_TOO_HIGH, CxInput.of("amount", "99"))
                .with("max", "64");

        assertThat(CxFailures.describe(messages, "money", route("give Notch 5"), failure))
                .isEqualTo("amount tops out at 64. Usage: /money give <target> <amount>");
    }

    /** The value comes from the sender, so it must not be able to colour the line around it. */
    @Test
    void strips_the_colour_codes_a_sender_typed_into_a_value() {
        messages.set(CxKey.INVALID_NUMBER, "&c{value} is not a number.");

        CxParseException failure = CxParseException.of(CxKey.INVALID_NUMBER, CxInput.of("amount", "&alots"));

        assertThat(CxFailures.describe(messages, "money", route("give Notch 5"), failure))
                .isEqualTo(SECTION + "clots is not a number.");
    }

    @Test
    void refuses_to_describe_a_command_line_that_worked() {
        assertThatThrownBy(() -> CxFailures.describe(messages, "money", route("top")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refuses_what_it_cannot_use() {
        CxMatchResult failed = route("nope");
        CxParseException parseFailure = CxParseException.of(CxKey.INVALID_NUMBER, CxInput.of("amount", "lots"));

        assertThatThrownBy(() -> CxFailures.describe(null, "money", failed))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxFailures.describe(messages, "money", (CxMatchResult) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxFailures.describe(messages, "money", null, parseFailure))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CxFailures.describe(messages, "money", failed, null))
                .isInstanceOf(NullPointerException.class);
    }
}
