/**
 * Platform-independent command engine.
 *
 * <p>Nothing under this package may reference the Bukkit API. The engine works against
 * {@code CxSender} and {@code CxArgumentType}, which the Bukkit adapter implements. That
 * boundary is what makes the engine testable without a server, and it is enforced by an
 * architecture test rather than by convention.</p>
 */
package fr.flylonyx.crux.command.core;
