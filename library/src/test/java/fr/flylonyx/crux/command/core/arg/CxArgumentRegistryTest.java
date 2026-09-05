package fr.flylonyx.crux.command.core.arg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.fixture.FakeArgumentType;
import fr.flylonyx.crux.command.fixture.FakeSender;

class CxArgumentRegistryTest {

    private enum Difficulty {
        PEACEFUL, HARD
    }

    private final CxArgumentRegistry registry = new CxArgumentRegistry();

    @ParameterizedTest
    @ValueSource(classes = {String.class, Integer.class, Long.class, Float.class, Double.class,
        Boolean.class, UUID.class, Duration.class})
    void every_built_in_type_is_registered_for_the_class_it_produces(Class<?> target) {
        assertThat(registry.supports(target)).isTrue();
        assertThat(registry.resolve(target)).isNotNull();
    }

    /** A command method declares {@code int amount}, not {@code Integer amount}. */
    @ParameterizedTest
    @ValueSource(classes = {int.class, long.class, float.class, double.class, boolean.class})
    void a_primitive_is_read_the_same_way_as_the_class_that_wraps_it(Class<?> primitive) {
        assertThat(registry.resolve(primitive)).isNotNull();
    }

    @Test
    void an_enum_is_read_by_name_without_being_registered() throws CxParseException {
        assertThat(registry.supports(Difficulty.class)).isTrue();
        assertThat(registry.resolve(Difficulty.class).parse(CxInput.of("difficulty", "hard"), FakeSender.console()))
                .isEqualTo(Difficulty.HARD);
    }

    @Test
    void a_registered_type_replaces_the_built_in_one() {
        CxArgumentType<String> replacement = new FakeArgumentType("nickname");

        registry.register(String.class, replacement);

        assertThat(registry.resolve(String.class)).isSameAs(replacement);
    }

    @Test
    void a_registered_type_replaces_how_an_enum_is_read() {
        CxArgumentType<Difficulty> replacement = CxArgumentTypes.enumOf(Difficulty.class);

        registry.register(Difficulty.class, replacement);

        assertThat(registry.resolve(Difficulty.class)).isSameAs(replacement);
    }

    @Test
    void registering_reads_as_one_statement_per_type() {
        assertThat(registry.register(String.class, new FakeArgumentType("nickname"))).isSameAs(registry);
    }

    @Test
    void a_class_nothing_reads_is_reported_by_name() {
        assertThat(registry.supports(Thread.class)).isFalse();
        assertThatThrownBy(() -> registry.resolve(Thread.class))
                .isInstanceOf(CxDefinitionException.class)
                .hasMessageContaining("java.lang.Thread");
    }

    /** One registry per plugin, so what one registers cannot leak into another. */
    @Test
    void registries_do_not_share_what_is_registered_into_them() {
        registry.register(String.class, new FakeArgumentType("nickname"));

        assertThat(new CxArgumentRegistry().resolve(String.class))
                .isSameAs(CxArgumentTypes.string())
                .isNotSameAs(registry.resolve(String.class));
    }

    @Test
    void a_registration_needs_both_a_class_and_a_type() {
        assertThatThrownBy(() -> registry.register(null, CxArgumentTypes.string()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> registry.register(String.class, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void looking_a_class_up_needs_a_class() {
        assertThatThrownBy(() -> registry.resolve(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> registry.supports(null)).isInstanceOf(NullPointerException.class);
    }
}
