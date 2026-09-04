/**
 * Translation of annotated classes into command trees.
 *
 * <p>Runs once, at registration. Annotations are read, validated and compiled into method
 * handles, so dispatch performs no reflection. A malformed declaration fails here, at
 * server startup, naming the class and method responsible.</p>
 */
package fr.flylonyx.crux.command.reflect;
