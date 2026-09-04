/**
 * Bukkit adapter.
 *
 * <p>The only layer aware of the server API. It adapts senders, registers commands into the
 * server command map, and provides the server-backed argument types. Kept deliberately thin
 * so that the behaviour worth testing lives in the platform-independent engine instead.</p>
 */
package fr.flylonyx.crux.command.bukkit;
