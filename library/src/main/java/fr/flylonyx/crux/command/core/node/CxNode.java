package fr.flylonyx.crux.command.core.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.flylonyx.crux.command.core.arg.CxArgumentType;

/**
 * One step in a command.
 *
 * <p>{@code /money give <target> <amount>} is a literal node {@code money} holding a
 * literal child {@code give}, holding an argument child {@code target}, holding an
 * argument child {@code amount} that carries the handler.
 *
 * <p>Immutable once built, so dispatch and tab completion read it without locking. Build
 * one with {@link CxNodeBuilder#literal(String)} or
 * {@link CxNodeBuilder#argument(String, CxArgumentType)}.
 */
public final class CxNode {

    private final CxNodeKind kind;
    private final String name;
    private final List<String> aliases;
    private final CxArgumentType<?> argumentType;
    private final String permission;
    private final String description;
    private final String defaultValue;
    private final int priority;
    private final CxHandler handler;
    private final Map<String, CxNode> literalLookup;
    private final List<CxNode> literals;
    private final List<CxNode> arguments;

    CxNode(final CxNodeBuilder declaration,
           final Map<String, CxNode> literalLookup,
           final List<CxNode> literals,
           final List<CxNode> arguments) {

        this.kind = declaration.kind();
        this.name = declaration.name();
        this.aliases = new ArrayList<>(declaration.aliases());
        this.argumentType = declaration.argumentType();
        this.permission = declaration.permission();
        this.description = declaration.description();
        this.defaultValue = declaration.defaultValue();
        this.priority = declaration.priority();
        this.handler = declaration.handler();
        this.literalLookup = literalLookup;
        this.literals = literals;
        this.arguments = arguments;
    }

    /**
     * Returns what this node matches.
     *
     * @return the node kind
     */
    public CxNodeKind kind() {
        return this.kind;
    }

    /**
     * Returns the canonical name of this node.
     *
     * <p>For a literal, the word it matches. For an argument, the name shown in usage
     * strings and error messages.
     *
     * @return the name
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the alternative words this literal also matches.
     *
     * @return an unmodifiable list of aliases, empty for an argument node
     */
    public List<String> aliases() {
        return Collections.unmodifiableList(this.aliases);
    }

    /**
     * Returns the type used to read this argument.
     *
     * @return the argument type, or {@code null} for a literal node
     */
    public CxArgumentType<?> argumentType() {
        return this.argumentType;
    }

    /**
     * Returns the permission required to reach this node.
     *
     * <p>A node inherits its parent's requirement in addition to this one; the permission
     * returned here is the one declared on this node alone.
     *
     * @return the permission, or empty if this node adds no requirement
     */
    public Optional<String> permission() {
        return Optional.ofNullable(this.permission);
    }

    /**
     * Returns the description shown in generated help.
     *
     * @return the description, empty if none was given
     */
    public String description() {
        return this.description;
    }

    /**
     * Returns the text read when the sender leaves this argument out.
     *
     * @return the default, or {@code null} if the argument is required
     */
    public String defaultValue() {
        return this.defaultValue;
    }

    /**
     * Reports whether the sender may leave this argument out.
     *
     * @return {@code true} if this node declares a default
     */
    public boolean isOptional() {
        return this.defaultValue != null;
    }

    /**
     * Returns the ordering weight among sibling argument nodes.
     *
     * <p>Higher values are tried first, which is how a narrower type is given the chance
     * to match before a broader one at the same position.
     *
     * @return the priority
     */
    public int priority() {
        return this.priority;
    }

    /**
     * Returns what runs when a command stops at this node.
     *
     * @return the handler, or empty if this node is only a branch
     */
    public Optional<CxHandler> handler() {
        return Optional.ofNullable(this.handler);
    }

    /**
     * Reports whether a command may stop at this node.
     *
     * @return {@code true} if this node has a handler
     */
    public boolean isExecutable() {
        return this.handler != null;
    }

    /**
     * Returns the literal children, in declaration order.
     *
     * @return an unmodifiable list of literal children
     */
    public List<CxNode> literals() {
        return Collections.unmodifiableList(this.literals);
    }

    /**
     * Returns the argument children, most specific first.
     *
     * @return an unmodifiable list of argument children
     */
    public List<CxNode> arguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    /**
     * Finds the literal child matching a token, ignoring case.
     *
     * <p>Aliases resolve to the same node as the name they stand for.
     *
     * @param token the token to match
     * @return the child, or empty if no literal child matches
     */
    public Optional<CxNode> literal(final String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.literalLookup.get(CxNodeNames.lookupKey(token)));
    }

    /**
     * The way this node appears in a usage line: a word, {@code <required>} or
     * {@code [optional]}.
     */
    String token() {
        if (this.kind == CxNodeKind.LITERAL) {
            return this.name;
        }
        return this.isOptional() ? "[" + this.name + "]" : "<" + this.name + ">";
    }

    @Override
    public String toString() {
        return this.token();
    }
}
