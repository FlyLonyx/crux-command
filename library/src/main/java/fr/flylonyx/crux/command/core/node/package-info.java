/**
 * The command tree: literal nodes, argument nodes and the builders that produce them.
 *
 * <p>Builders are mutable. The tree they produce is immutable and safe to read from any
 * thread, which is what allows dispatch and tab completion to run without locking.</p>
 */
package fr.flylonyx.crux.command.core.node;
