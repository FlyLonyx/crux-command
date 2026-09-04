/**
 * Registration and removal of commands in the server command map.
 *
 * <p>Commands are injected at runtime, so consumers never declare them in
 * {@code plugin.yml}. Removal matters just as much: without it, a reload leaves commands
 * bound to a dead class loader.
 */
package fr.flylonyx.crux.command.bukkit.registry;
