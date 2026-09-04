package fr.flylonyx.crux.command.core.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import fr.flylonyx.crux.command.core.CxDefinitionException;
import fr.flylonyx.crux.command.core.arg.CxArgumentType;

/**
 * Assembles a command tree.
 *
 * <p>This is the API the annotation reader targets, and the one to use directly when the
 * shape of a command is only known at runtime: sub-commands coming from configuration, or
 * a branch per entry in a database.
 *
 * <pre>{@code
 * CxNodeBuilder kits = CxNodeBuilder.literal("kit").permission("crux.kit.use");
 * for (Kit kit : kitService.loadAll()) {
 *     kits.then(CxNodeBuilder.literal(kit.name())
 *             .permission("crux.kit." + kit.name())
 *             .executes(context -> kit.giveTo(context.sender())));
 * }
 * CxNode tree = kits.build();
 * }</pre>
 *
 * <p>Builders are mutable and not thread safe. The tree they produce is neither.
 *
 * <p>Everything that cannot depend on what a sender types is checked in {@link #build()}
 * and reported as a {@link CxDefinitionException}. A command that is wrong fails at
 * startup, named, rather than behaving strangely later.
 */
public final class CxNodeBuilder {

    private final CxNodeKind kind;
    private final String name;
    private final CxArgumentType<?> argumentType;
    private final List<String> aliases = new ArrayList<String>();
    private final List<CxNodeBuilder> children = new ArrayList<CxNodeBuilder>();

    private String permission;
    private String description = "";
    private int priority;
    private CxHandler handler;

    private CxNodeBuilder(final CxNodeKind kind, final String name, final CxArgumentType<?> argumentType) {
        this.kind = kind;
        this.name = name;
        this.argumentType = argumentType;
    }

    /**
     * Starts a node matching one exact word.
     *
     * @param name the word to match, compared without regard to case
     * @return a new builder
     */
    public static CxNodeBuilder literal(final String name) {
        return new CxNodeBuilder(CxNodeKind.LITERAL, name, null);
    }

    /**
     * Starts a node matching a value.
     *
     * @param name the name shown in usage strings and error messages
     * @param type the type used to read the value
     * @return a new builder
     */
    public static CxNodeBuilder argument(final String name, final CxArgumentType<?> type) {
        return new CxNodeBuilder(CxNodeKind.ARGUMENT, name, type);
    }

    /**
     * Adds alternative words this literal also matches.
     *
     * @param values the aliases
     * @return this builder
     */
    public CxNodeBuilder aliases(final String... values) {
        Objects.requireNonNull(values, "values");
        Collections.addAll(this.aliases, values);
        return this;
    }

    /**
     * Requires a permission to reach this node and everything below it.
     *
     * @param value the permission node, or {@code null} to add no requirement
     * @return this builder
     */
    public CxNodeBuilder permission(final String value) {
        this.permission = value;
        return this;
    }

    /**
     * Sets the description shown in generated help.
     *
     * @param value the description
     * @return this builder
     */
    public CxNodeBuilder description(final String value) {
        this.description = value == null ? "" : value;
        return this;
    }

    /**
     * Sets the ordering weight among sibling argument nodes, highest tried first.
     *
     * @param value the priority
     * @return this builder
     */
    public CxNodeBuilder priority(final int value) {
        this.priority = value;
        return this;
    }

    /**
     * Makes a command able to stop at this node, running the given handler.
     *
     * @param value what to run
     * @return this builder
     */
    public CxNodeBuilder executes(final CxHandler value) {
        this.handler = value;
        return this;
    }

    /**
     * Adds a child node.
     *
     * @param child the child builder; it is built when this builder is
     * @return this builder
     */
    public CxNodeBuilder then(final CxNodeBuilder child) {
        this.children.add(Objects.requireNonNull(child, "child"));
        return this;
    }

    /**
     * Validates the declaration and produces the immutable tree.
     *
     * @return the built node
     * @throws CxDefinitionException if the declaration could never work
     */
    public CxNode build() {
        CxNodeNames.requireUsable(this.role(), this.name);
        this.validateAliases();
        this.validateArgument();

        final CxNodeChildren built = this.buildChildren();
        this.requireReachable(built);

        return new CxNode(this, built);
    }

    CxNodeKind kind() {
        return this.kind;
    }

    String name() {
        return this.name;
    }

    List<String> aliases() {
        return this.aliases;
    }

    CxArgumentType<?> argumentType() {
        return this.argumentType;
    }

    String permission() {
        return this.permission;
    }

    String description() {
        return this.description;
    }

    int priority() {
        return this.priority;
    }

    CxHandler handler() {
        return this.handler;
    }

    private String role() {
        return this.kind == CxNodeKind.LITERAL ? "literal" : "argument";
    }

    private void validateAliases() {
        if (!this.aliases.isEmpty() && this.kind != CxNodeKind.LITERAL) {
            throw new CxDefinitionException(
                    "The argument '" + this.name + "' declares aliases, but only a literal can have them.");
        }

        final Set<String> seen = new LinkedHashSet<String>();
        seen.add(CxNodeNames.lookupKey(this.name));

        for (final String alias : this.aliases) {
            CxNodeNames.requireUsable("alias of '" + this.name + "'", alias);
            if (!seen.add(CxNodeNames.lookupKey(alias))) {
                throw new CxDefinitionException(
                        "The literal '" + this.name + "' declares '" + alias + "' more than once.");
            }
        }
    }

    private void validateArgument() {
        if (this.kind != CxNodeKind.ARGUMENT) {
            return;
        }
        if (this.argumentType == null) {
            throw new CxDefinitionException("The argument '" + this.name + "' has no type.");
        }
        final int arity = this.argumentType.arity();
        if (arity <= 0 && arity != CxArgumentType.ARITY_GREEDY) {
            throw new CxDefinitionException("The argument '" + this.name + "' declares an arity of " + arity
                    + ", which cannot consume anything.");
        }
    }

    /**
     * Builds and validates the children, then orders them for routing.
     *
     * <p>Argument children are sorted by priority, highest first. The sort is stable, so
     * arguments of equal priority keep the order they were declared in and routing stays
     * predictable across builds.
     */
    private CxNodeChildren buildChildren() {
        final Map<String, CxNode> lookup = new LinkedHashMap<String, CxNode>();
        final List<CxNode> literals = new ArrayList<CxNode>();
        final List<CxNode> arguments = new ArrayList<CxNode>();

        for (final CxNodeBuilder child : this.children) {
            final CxNode built = child.build();
            if (built.kind() == CxNodeKind.LITERAL) {
                this.registerLiteral(lookup, literals, built);
            } else {
                this.registerArgument(arguments, built);
            }
        }

        arguments.sort(Comparator.comparingInt(CxNode::priority).reversed());

        return new CxNodeChildren(Collections.unmodifiableMap(lookup),
                Collections.unmodifiableList(literals),
                Collections.unmodifiableList(arguments));
    }

    private void registerLiteral(final Map<String, CxNode> lookup, final List<CxNode> literals, final CxNode child) {
        final List<String> words = new ArrayList<String>();
        words.add(child.name());
        words.addAll(child.aliases());

        for (final String word : words) {
            final CxNode clash = lookup.put(CxNodeNames.lookupKey(word), child);
            if (clash != null) {
                throw new CxDefinitionException("Under '" + this.name + "', '" + word
                        + "' is claimed by both '" + clash.name() + "' and '" + child.name()
                        + "', so one of them could never be reached.");
            }
        }
        literals.add(child);
    }

    private void registerArgument(final List<CxNode> arguments, final CxNode child) {
        for (final CxNode existing : arguments) {
            if (existing.name().equals(child.name())) {
                throw new CxDefinitionException("Under '" + this.name + "', two arguments are both named '"
                        + child.name() + "', so a handler could not tell them apart.");
            }
        }

        if (!arguments.isEmpty() && (isGreedy(child) || isGreedy(arguments.get(0)))) {
            throw new CxDefinitionException("Under '" + this.name + "', the argument '" + child.name()
                    + "' sits beside a greedy argument, which already consumes everything that follows.");
        }

        if (isGreedy(child) && (!child.literals().isEmpty() || !child.arguments().isEmpty())) {
            throw new CxDefinitionException("The greedy argument '" + child.name()
                    + "' has children, but nothing can follow it.");
        }

        arguments.add(child);
    }

    private void requireReachable(final CxNodeChildren built) {
        if (this.handler == null && built.literals().isEmpty() && built.arguments().isEmpty()) {
            throw new CxDefinitionException("The " + this.role() + " '" + this.name
                    + "' has no handler and no children, so reaching it could never do anything.");
        }
    }

    private static boolean isGreedy(final CxNode node) {
        return node.argumentType() != null && node.argumentType().arity() == CxArgumentType.ARITY_GREEDY;
    }
}
