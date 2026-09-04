package fr.flylonyx.crux.command;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Guards the layering the whole design rests on.
 *
 * <p>These rules are not style preferences. Each one exists because breaking it would
 * quietly undo a property the library promises, so a violation is a build failure rather
 * than a review comment.
 */
@AnalyzeClasses(
        packages = "fr.flylonyx.crux.command",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule ENGINE_MUST_NOT_DEPEND_ON_THE_SERVER_API = noClasses()
            .that().resideInAnyPackage("..core..", "..message..")
            .should().dependOnClassesThat().resideInAnyPackage("org.bukkit..", "net.md_5..")
            .because("the engine is what carries the behaviour worth testing, and it can only be "
                    + "tested without a server if it never touches the server API");

    @ArchTest
    static final ArchRule ENGINE_MUST_NOT_DEPEND_ON_OUTER_LAYERS = noClasses()
            .that().resideInAPackage("..core..")
            .should().dependOnClassesThat().resideInAnyPackage("..reflect..", "..bukkit..")
            .because("dependencies point inwards; the engine knows nothing of how commands were "
                    + "declared or which platform runs them");

    @ArchTest
    static final ArchRule ANNOTATIONS_MUST_DEPEND_ON_NOTHING_BUT_THE_JDK = noClasses()
            .that().resideInAPackage("..annotation..")
            .should().dependOnClassesThat()
            .resideOutsideOfPackages("java..", "fr.flylonyx.crux.command.annotation..")
            .because("a command class must be writable and compilable without dragging in the "
                    + "server API or the engine");

    @ArchTest
    static final ArchRule PACKAGES_MUST_BE_FREE_OF_CYCLES = SlicesRuleDefinition.slices()
            .matching("fr.flylonyx.crux.command.(**)")
            .should().beFreeOfCycles()
            .because("a cycle means two packages can no longer be understood, moved or tested "
                    + "apart, which is how a layered design quietly stops being one");

    @ArchTest
    static final ArchRule NOTHING_MAY_WRITE_TO_THE_STANDARD_STREAMS =
            GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                    .because("a library writes through the plugin logger, so that server owners "
                            + "keep control of what is recorded and where");
}
