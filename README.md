# Crux Command

**A command framework for PaperSpigot 1.8.8 plugins.**

[![Build](https://github.com/FlyLonyx/crux-command/actions/workflows/build.yml/badge.svg)](https://github.com/FlyLonyx/crux-command/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java 8](https://img.shields.io/badge/java-8-orange.svg)](https://adoptium.net/)
[![Minecraft 1.8.8](https://img.shields.io/badge/minecraft-1.8.8-brightgreen.svg)](https://www.spigotmc.org/)

You describe what a command does. The library derives the rest — routing, argument
parsing, permission checks, tab completion, usage strings and error messages.

## Status

In development, working towards 1.0.0. No release is published yet.

| | |
|---|---|
| Command tree, tokenising, routing | done |
| Argument types and validation | next |
| Messages and usage generation | planned |
| Bukkit adapter and registration | planned |
| Annotation layer | planned |
| Tab completion, help, execution guards | planned |

The API shown below is the target design. What is built today is the engine underneath
it: an immutable command tree, a tokenizer that handles quoting, and routing with
backtracking — none of which touch the server API.

## The problem

Writing a command for Spigot 1.8.8 means declaring it in `plugin.yml`, implementing
`CommandExecutor`, splitting `String[] args` by hand, validating every value, writing a
second `TabCompleter` that duplicates the first, and repeating the same permission and
sender checks in every branch.

## The target API

```java
@Command(name = "money", aliases = {"bal", "balance"})
@Permission("crux.money.use")
public final class MoneyCommand {

    private final EconomyService economy;

    public MoneyCommand(EconomyService economy) {
        this.economy = economy;
    }

    @Default
    public void balance(Player sender) {
        sender.sendMessage("Balance: " + economy.balanceOf(sender));
    }

    @Sub("give")
    @Permission("crux.money.admin")
    public void give(CommandSender sender, OfflinePlayer target, @Min(0) double amount) {
        economy.give(target, amount);
    }
}
```

```java
@Override
public void onEnable() {
    CxCommands.on(this).register(new MoneyCommand(economyService));
}
```

No `plugin.yml` entry, no `CommandExecutor`, no `TabCompleter`, no manual parsing. From
the class above the library derives the aliases and sub-commands, typed argument
resolution with a clear message when a value is invalid, tab completion filtered by
permission, a paginated `/money help`, and permission and sender checks applied before
anything is parsed.

For commands whose shape is only known at runtime, the same engine is reachable directly:

```java
CxNodeBuilder kits = CxNodeBuilder.literal("kit").permission("crux.kit.use");

for (Kit kit : kitService.loadAll()) {
    kits.then(CxNodeBuilder.literal(kit.name())
            .permission("crux.kit." + kit.name())
            .executes(context -> kit.giveTo(context.sender())));
}
```

## Design

- **No runtime dependencies.** Only `paperspigot-api`, at `provided` scope.
- **Commands are instances.** Register `new MoneyCommand(economy)` and inject dependencies
  through the constructor like any other class.
- **No static mutable state.** One manager per plugin.
- **The engine knows nothing about Bukkit.** Routing, parsing and usage generation work
  against a `CxSender` interface, so they are tested without a server. An ArchUnit test
  fails the build if that boundary is crossed.
- **Extensible.** Register your own argument types, conditions and suggestion providers.

## Requirements

| | |
|---|---|
| Java | 8 |
| Server | PaperSpigot 1.8.8 |
| Build | Maven |

## Part of Crux

Crux is a suite of libraries covering the recurring plumbing of Minecraft plugin
development — menus, commands, configuration, storage — so plugin code stays focused on
features. Each library lives in its own repository.

## License

[MIT](LICENSE)
