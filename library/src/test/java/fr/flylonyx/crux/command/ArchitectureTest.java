package fr.flylonyx.crux.command;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Guards the layering the design rests on. A violation fails the build.
 */
@AnalyzeClasses(
        packages = "fr.flylonyx.crux.command",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule ENGINE_MUST_NOT_DEPEND_ON_THE_SERVER_API = noClasses()
            .that().resideInAnyPackage("..core..", "..message..")
            .should().dependOnClassesThat().resideInAnyPackage("org.bukkit..", "net.md_5..")
            .because("the engine has to be testable without a server");

    @ArchTest
    static final ArchRule ENGINE_MUST_NOT_DEPEND_ON_OUTER_LAYERS = noClasses()
            .that().resideInAPackage("..core..")
            .should().dependOnClassesThat().resideInAnyPackage("..reflect..", "..bukkit..")
            .because("dependencies point inwards");

    @ArchTest
    static final ArchRule PACKAGES_MUST_BE_FREE_OF_CYCLES = SlicesRuleDefinition.slices()
            .matching("fr.flylonyx.crux.command.(**)")
            .should().beFreeOfCycles()
            .because("a cycle makes two packages impossible to move or test apart");

    @ArchTest
    static final ArchRule NOTHING_MAY_WRITE_TO_THE_STANDARD_STREAMS =
            GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                    .because("a library logs through the plugin logger");
}
