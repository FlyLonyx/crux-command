package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.fixture.FakeSender;
import fr.flylonyx.crux.command.message.CxKey;

class CxEnumArgumentTypeTest {

    private enum Difficulty {
        PEACEFUL, EASY, NORMAL, HARD
    }

    private enum Colour {
        BLACK, BLUE, BROWN, CYAN, GRAY, GREEN, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, WHITE, YELLOW
    }

    private enum Nothing {
    }

    private final CxArgumentType<Difficulty> type = CxArgumentTypes.enumOf(Difficulty.class);
    private final CxSender sender = FakeSender.player("Notch");

    @Test
    void an_enum_reads_a_constant_whatever_the_case() throws CxParseException {
        assertThat(type.parse(CxInput.of("difficulty", "hard"), sender)).isEqualTo(Difficulty.HARD);
        assertThat(type.parse(CxInput.of("difficulty", "PeaceFul"), sender)).isEqualTo(Difficulty.PEACEFUL);
    }

    @Test
    void an_enum_lists_its_constants_when_none_matches() {
        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> type.parse(CxInput.of("difficulty", "nightmare"), sender))
                .satisfies(failure -> {
                    assertThat(failure.key()).isEqualTo(CxKey.INVALID_CHOICE);
                    assertThat(failure.placeholders())
                            .containsEntry("choices", "peaceful, easy, normal, hard");
                });
    }

    /** A wide enum would otherwise fill the chat with a list nobody reads. */
    @Test
    void a_wide_enum_lists_only_the_first_choices() {
        CxArgumentType<Colour> colour = CxArgumentTypes.enumOf(Colour.class);

        assertThatExceptionOfType(CxParseException.class)
                .isThrownBy(() -> colour.parse(CxInput.of("colour", "beige"), sender))
                .satisfies(failure -> assertThat(failure.placeholders().get("choices"))
                        .isEqualTo("black, blue, brown, cyan, gray, green, lime, magenta, orange, pink, ..."));
    }

    @Test
    void an_enum_offers_its_constants_in_declaration_order() {
        assertThat(type.suggest("", sender)).containsExactly("peaceful", "easy", "normal", "hard");
    }

    @Test
    void an_enum_narrows_its_suggestions_to_what_was_typed() {
        assertThat(type.suggest("ha", sender)).containsExactly("hard");
        assertThat(type.suggest("E", sender)).containsExactly("easy");
        assertThat(type.suggest("z", sender)).isEmpty();
    }

    @Test
    void an_enum_is_named_after_itself() {
        assertThat(type.id()).isEqualTo("difficulty");
        assertThat(type.arity()).isEqualTo(1);
    }

    @Test
    void an_enum_with_no_constant_could_never_match_and_is_refused() {
        assertThatThrownBy(() -> CxArgumentTypes.enumOf(Nothing.class))
                .isInstanceOf(CxDefinitionException.class)
                .hasMessageContaining("Nothing");
    }

    @Test
    void an_enum_type_needs_an_enum() {
        Class<Difficulty> missing = null;

        assertThatThrownBy(() -> CxArgumentTypes.enumOf(missing)).isInstanceOf(NullPointerException.class);
    }
}
