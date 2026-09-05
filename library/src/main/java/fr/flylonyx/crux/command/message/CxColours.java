package fr.flylonyx.crux.command.message;

/**
 * Colour codes, in the two forms the client accepts.
 *
 * <p>Templates are written with {@code &} and translated on the way out. Anything a sender
 * typed is stripped of both forms first, so a player called {@code &c&lNotch} cannot dress
 * their name up as part of the message around it.
 */
final class CxColours {

    private static final char ALTERNATE = '&';

    /** The section sign, escaped so the encoding of this file cannot change what it means. */
    private static final char SECTION = '\u00A7';

    private static final String CODES = "0123456789abcdefklmnorABCDEFKLMNOR";

    private CxColours() {
    }

    /**
     * Turns {@code &c} into the form the client renders.
     *
     * @param text the template to translate
     * @return the translated text
     */
    static String translate(final String text) {
        final char[] characters = text.toCharArray();
        for (int index = 0; index < characters.length - 1; index++) {
            if (characters[index] == ALTERNATE && isCode(characters[index + 1])) {
                characters[index] = SECTION;
                characters[index + 1] = Character.toLowerCase(characters[index + 1]);
            }
        }
        return new String(characters);
    }

    /**
     * Removes the colour codes from text a sender supplied.
     *
     * <p>Every section sign goes, whether or not it opens a code. Removing only whole codes
     * would let a doubled sign come back together, and the section sign is the form the
     * client acts on. An ampersand is removed only when it opens a code, which keeps
     * {@code Tom & Jerry} intact.
     *
     * @param text the text to strip
     * @return the text with no code left in it
     */
    static String strip(final String text) {
        final StringBuilder stripped = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            if (!removable(text, index)) {
                stripped.append(text.charAt(index));
            } else if (opensCode(text, index)) {
                index++;
            }
        }
        return stripped.toString();
    }

    /** A section sign always goes; an ampersand only when a code character follows it. */
    private static boolean removable(final String text, final int index) {
        final char character = text.charAt(index);
        return character == SECTION || (character == ALTERNATE && opensCode(text, index));
    }

    private static boolean opensCode(final String text, final int index) {
        return index + 1 < text.length() && isCode(text.charAt(index + 1));
    }

    private static boolean isCode(final char character) {
        return CODES.indexOf(character) >= 0;
    }
}
