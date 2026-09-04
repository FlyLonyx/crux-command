package fr.flylonyx.crux.command.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.flylonyx.crux.command.core.sender.CxSenderType;
import fr.flylonyx.crux.command.fixture.FakeSender;

class CxContextTest {

    private static final List<String> ARGS = Arrays.asList("Notch", "5");

    @Test
    void exposes_the_sender_the_label_and_the_raw_arguments() {
        FakeSender sender = FakeSender.player("Steve");

        CxContext context = new CxContext(sender, "bal", ARGS);

        assertThat(context.sender()).isSameAs(sender);
        assertThat(context.label()).isEqualTo("bal");
        assertThat(context.raw()).containsExactly("Notch", "5");
    }

    @Test
    void copies_the_raw_arguments_defensively() {
        List<String> source = new ArrayList<>(ARGS);
        CxContext context = new CxContext(FakeSender.console(), "money", source);

        source.clear();

        assertThat(context.raw()).containsExactly("Notch", "5");
    }

    @Test
    void exposes_the_raw_arguments_as_an_unmodifiable_list() {
        CxContext context = new CxContext(FakeSender.console(), "money", ARGS);

        assertThatThrownBy(() -> context.raw().add("extra"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejects_missing_parts() {
        FakeSender sender = FakeSender.player("Steve");

        assertThatThrownBy(() -> new CxContext(null, "money", ARGS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CxContext(sender, null, ARGS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CxContext(sender, "money", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void carries_a_sender_that_reports_its_kind_and_permissions() {
        FakeSender player = FakeSender.player("Steve", "crux.money.use");

        assertThat(player.type()).isEqualTo(CxSenderType.PLAYER);
        assertThat(player.name()).isEqualTo("Steve");
        assertThat(player.hasPermission("crux.money.use")).isTrue();
        assertThat(player.hasPermission("crux.money.admin")).isFalse();
    }

    @Test
    void carries_a_sender_that_records_what_it_was_told() {
        FakeSender console = FakeSender.console();

        new CxContext(console, "money", Collections.emptyList()).sender().send("done");

        assertThat(console.received()).containsExactly("done");
    }

    @Test
    void distinguishes_the_kinds_of_sender() {
        assertThat(FakeSender.console().type()).isEqualTo(CxSenderType.CONSOLE);
        assertThat(FakeSender.block().type()).isEqualTo(CxSenderType.BLOCK);
        assertThat(CxSenderType.valueOf("OTHER")).isEqualTo(CxSenderType.OTHER);
        assertThat(CxSenderType.values()).hasSize(4);
    }
}
