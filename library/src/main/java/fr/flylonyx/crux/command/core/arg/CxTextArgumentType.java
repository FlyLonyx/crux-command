package fr.flylonyx.crux.command.core.arg;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * Reads text, in one of three shapes.
 *
 * <p>One token of any content, one token that must be a single word, or every remaining
 * token joined by a space. Quoting is the tokeniser's business, so a quoted phrase arrives
 * here as one token. That is what the single-word shape refuses.
 */
final class CxTextArgumentType implements CxArgumentType<String> {

    private final String id;
    private final int arity;
    private final boolean singleWord;

    CxTextArgumentType(final String id, final int arity, final boolean singleWord) {
        this.id = id;
        this.arity = arity;
        this.singleWord = singleWord;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public int arity() {
        return this.arity;
    }

    @Override
    public String parse(final CxInput input, final CxSender sender) throws CxParseException {
        final String value = this.arity == ARITY_GREEDY ? input.joined() : input.first();
        if (this.singleWord && !isSingleWord(value)) {
            throw CxParseException.of(CxKey.NOT_A_SINGLE_WORD, input);
        }
        return value;
    }

    private static boolean isSingleWord(final String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            if (Character.isWhitespace(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }
}
