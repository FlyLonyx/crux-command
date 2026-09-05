package fr.flylonyx.crux.command.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The text the library sends, with a plugin's overrides applied.
 *
 * <p>Every key ships with an English default, so nothing has to be configured. One set of
 * messages per plugin, filled during startup and only read afterwards; it holds no static
 * state and is not safe to write to from more than one thread.
 *
 * <p>Templates are written with {@code &} colour codes and come out translated. Values
 * substituted into them are stripped of colour codes first, so nothing a sender typed can
 * colour a message or pass itself off as part of one.
 */
public final class CxMessages {

    private static final Map<String, CxKey> BY_CONFIG_KEY = indexByConfigKey();

    private final Map<CxKey, String> overrides = new EnumMap<>(CxKey.class);

    /**
     * Overrides one message.
     *
     * @param key      the message to override
     * @param template the text to use instead, with {@code &} colour codes
     * @return these messages
     */
    public CxMessages set(final CxKey key, final String template) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(template, "template");

        this.overrides.put(key, template);
        return this;
    }

    /**
     * Overrides the messages a configuration holds, keyed by {@link CxKey#configKey()}.
     *
     * <p>A key matching no message is returned rather than dropped: the library owns no
     * logger, so reporting a typo in a configuration file is the caller's to do.
     *
     * @param templates the overrides, by configuration key
     * @return an unmodifiable list of the keys that matched no message
     */
    public List<String> setAll(final Map<String, String> templates) {
        Objects.requireNonNull(templates, "templates");

        final List<String> unknown = new ArrayList<>();
        for (final Map.Entry<String, String> entry : templates.entrySet()) {
            final CxKey key = BY_CONFIG_KEY.get(entry.getKey());
            if (key == null) {
                unknown.add(entry.getKey());
            } else {
                this.set(key, entry.getValue());
            }
        }
        return Collections.unmodifiableList(unknown);
    }

    /**
     * Returns the text a message is built from, before anything is substituted.
     *
     * @param key the message
     * @return the override if there is one, the English default otherwise
     */
    public String template(final CxKey key) {
        Objects.requireNonNull(key, "key");

        final String override = this.overrides.get(key);
        return override == null ? key.defaultMessage() : override;
    }

    /**
     * Builds a message, ready to send.
     *
     * <p>The colour codes are already translated. Running the result through a second
     * translation would colour whatever the sender typed into it.
     *
     * @param key    the message to build
     * @param values what to substitute, by placeholder name
     * @return the message, with colour codes translated
     */
    public String render(final CxKey key, final Map<String, String> values) {
        Objects.requireNonNull(values, "values");
        return CxPlaceholders.substitute(CxColours.translate(this.template(key)), stripped(values));
    }

    private static Map<String, String> stripped(final Map<String, String> values) {
        final Map<String, String> safe = new LinkedHashMap<>();
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            safe.put(Objects.requireNonNull(entry.getKey(), "placeholder name"),
                    CxColours.strip(Objects.requireNonNull(entry.getValue(), "placeholder value")));
        }
        return safe;
    }

    private static Map<String, CxKey> indexByConfigKey() {
        final Map<String, CxKey> indexed = new HashMap<>();
        for (final CxKey key : CxKey.values()) {
            indexed.put(key.configKey(), key);
        }
        return Collections.unmodifiableMap(indexed);
    }
}
