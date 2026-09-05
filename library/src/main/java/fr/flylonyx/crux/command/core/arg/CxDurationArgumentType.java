package fr.flylonyx.crux.command.core.arg;

import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.message.CxKey;

/**
 * Reads a duration written as amounts and units: {@code 90s}, {@code 15m}, {@code 2h30m}.
 *
 * <p>Days, hours, minutes and seconds. Each unit may appear once, in any order, and the
 * whole token has to be covered: a stray character makes the value invalid rather than
 * silently reading the part of it that made sense.
 */
final class CxDurationArgumentType implements CxArgumentType<Duration> {

    private static final Pattern PART = Pattern.compile("(\\d+)([dhms])");

    private static final long SECONDS_PER_DAY = 86_400L;
    private static final long SECONDS_PER_HOUR = 3_600L;
    private static final long SECONDS_PER_MINUTE = 60L;

    @Override
    public String id() {
        return "duration";
    }

    @Override
    public Duration parse(final CxInput input, final CxSender sender) throws CxParseException {
        final String value = input.first().toLowerCase(Locale.ROOT);
        final Matcher part = PART.matcher(value);
        final Set<Character> units = new HashSet<>();
        long seconds = 0;
        int covered = 0;

        while (part.find() && part.start() == covered) {
            final char unit = part.group(2).charAt(0);
            if (!units.add(unit)) {
                throw CxParseException.of(CxKey.INVALID_DURATION, input);
            }
            seconds = add(seconds, part.group(1), unit, input);
            covered = part.end();
        }

        if (covered != value.length() || covered == 0) {
            throw CxParseException.of(CxKey.INVALID_DURATION, input);
        }
        return Duration.ofSeconds(seconds);
    }

    private static long add(final long seconds, final String amount, final char unit, final CxInput input)
            throws CxParseException {

        try {
            return Math.addExact(seconds, Math.multiplyExact(Long.parseLong(amount), secondsIn(unit)));
        } catch (final ArithmeticException | NumberFormatException tooLarge) {
            throw CxParseException.of(CxKey.INVALID_DURATION, input);
        }
    }

    private static long secondsIn(final char unit) {
        if (unit == 'd') {
            return SECONDS_PER_DAY;
        }
        if (unit == 'h') {
            return SECONDS_PER_HOUR;
        }
        if (unit == 'm') {
            return SECONDS_PER_MINUTE;
        }
        return 1;
    }
}
