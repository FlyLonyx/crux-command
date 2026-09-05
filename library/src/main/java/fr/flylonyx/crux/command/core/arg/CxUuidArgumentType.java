package fr.flylonyx.crux.command.core.arg;

import java.util.UUID;
import java.util.regex.Pattern;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/** Reads a unique id in the dashed form the platform prints. */
final class CxUuidArgumentType implements CxArgumentType<UUID> {

    /**
     * The canonical form, which is stricter than {@link UUID#fromString}.
     *
     * <p>That method also reads groups shorter than the ones it prints, so
     * {@code 1-2-3-4-5} would quietly become a different id than the sender meant.
     */
    private static final Pattern CANONICAL =
            Pattern.compile("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");

    @Override
    public String id() {
        return "uuid";
    }

    @Override
    public UUID parse(final CxInput input, final CxSender sender) throws CxParseException {
        final String value = input.first();
        if (!CANONICAL.matcher(value).matches()) {
            throw CxParseException.of(CxKey.INVALID_UUID, input);
        }
        return UUID.fromString(value);
    }
}
