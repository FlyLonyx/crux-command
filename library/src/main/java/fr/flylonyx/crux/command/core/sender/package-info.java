/**
 * Platform-independent view of whoever ran a command.
 *
 * <p>{@code CxSender} exposes only what the engine needs: a name, a permission check, and
 * a way to send a message. The Bukkit adapter is the only place allowed to unwrap it back
 * into a concrete server type.</p>
 */
package fr.flylonyx.crux.command.core.sender;
