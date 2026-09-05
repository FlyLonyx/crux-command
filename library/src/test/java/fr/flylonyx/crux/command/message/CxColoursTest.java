package fr.flylonyx.crux.command.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CxColoursTest {

    private static final String SECTION = "\u00A7";

    @Nested
    class Translating {

        @Test
        void turns_an_alternate_code_into_the_one_the_client_renders() {
            assertThat(CxColours.translate("&cred")).isEqualTo(SECTION + "cred");
        }

        @Test
        void folds_a_code_to_lower_case() {
            assertThat(CxColours.translate("&Cred")).isEqualTo(SECTION + "cred");
        }

        @Test
        void translates_every_code_in_the_text() {
            assertThat(CxColours.translate("&l&cbold red")).isEqualTo(SECTION + "l" + SECTION + "cbold red");
        }

        @Test
        void leaves_an_ampersand_that_opens_no_code() {
            assertThat(CxColours.translate("Tom & Jerry")).isEqualTo("Tom & Jerry");
            assertThat(CxColours.translate("ends with &")).isEqualTo("ends with &");
        }

        @Test
        void leaves_a_code_that_is_already_translated() {
            assertThat(CxColours.translate(SECTION + "cred")).isEqualTo(SECTION + "cred");
        }

        @Test
        void leaves_empty_text_alone() {
            assertThat(CxColours.translate("")).isEmpty();
        }
    }

    @Nested
    class Stripping {

        @Test
        void removes_an_ampersand_that_opens_a_code() {
            assertThat(CxColours.strip("&cred")).isEqualTo("red");
            assertThat(CxColours.strip("&l&cNotch&r")).isEqualTo("Notch");
        }

        @Test
        void removes_a_section_sign_and_the_code_it_opens() {
            assertThat(CxColours.strip(SECTION + "cred")).isEqualTo("red");
            assertThat(CxColours.strip("Notch" + SECTION + "r!")).isEqualTo("Notch!");
        }

        @Test
        void removes_a_section_sign_that_opens_no_code() {
            assertThat(CxColours.strip("ends with " + SECTION)).isEqualTo("ends with ");
            assertThat(CxColours.strip(SECTION + " space")).isEqualTo(" space");
        }

        /** Removing only whole codes would put the second sign back together with {@code c}. */
        @Test
        void leaves_no_section_sign_a_doubled_one_could_rebuild() {
            assertThat(CxColours.strip(SECTION + SECTION + "cred")).isEqualTo("red");
        }

        @Test
        void leaves_an_ampersand_that_opens_no_code() {
            assertThat(CxColours.strip("Tom & Jerry")).isEqualTo("Tom & Jerry");
            assertThat(CxColours.strip("ends with &")).isEqualTo("ends with &");
        }

        @Test
        void leaves_empty_text_alone() {
            assertThat(CxColours.strip("")).isEmpty();
        }
    }
}
