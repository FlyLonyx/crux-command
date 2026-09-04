# Crux Command

**A command framework for PaperSpigot 1.8.8 plugins.**

[![Build](https://github.com/FlyLonyx/crux-command/actions/workflows/build.yml/badge.svg)](https://github.com/FlyLonyx/crux-command/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java 8](https://img.shields.io/badge/java-8-orange.svg)](https://adoptium.net/)
[![Minecraft 1.8.8](https://img.shields.io/badge/minecraft-1.8.8-brightgreen.svg)](https://www.spigotmc.org/)

You describe what a command does. The library derives the rest — routing,
argument parsing, permission checks, tab completion, usage strings and error
messages.

> **Status: in development.** The public API is not frozen yet and no release is
> published. Installation instructions will land with `1.0.0`.

---

## What it removes

Writing a command for Spigot 1.8.8 normally means declaring it in `plugin.yml`,
implementing `CommandExecutor`, splitting `String[] args` by hand, validating
every value, writing a second `TabCompleter` that duplicates the first, and
repeating the same permission and sender checks in every branch.

Crux Command removes all of it.

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

That is the whole setup. No `plugin.yml` entry, no `CommandExecutor`, no
`TabCompleter`, no manual parsing.

Generated automatically from the class above:

- `/money`, `/bal` and `/balance`, plus the `/money give` sub-command
- typed argument resolution, with a clear message when a player or a number is
  invalid
- tab completion on every node, filtered by permission
- `/money help`, paginated and clickable, showing only what the sender may run
- permission and sender-type checks, applied before anything is parsed

## Design highlights

- **No runtime dependencies.** Only `paperspigot-api`, at `provided` scope.
- **No reflection at dispatch time.** Annotations are read once at startup and
  compiled into `MethodHandle` invokers.
- **Commands are instances.** Register `new MoneyCommand(economy)` and inject
  dependencies through the constructor like any other class.
- **No static mutable state.** One manager per plugin, fully isolated.
- **Clean unregistration.** No ghost commands or class loader leaks after a
  `/reload`.
- **Extensible.** Register your own argument types, conditions and suggestion
  providers.

## Requirements

| | |
|---|---|
| Java | 8 |
| Server | PaperSpigot 1.8.8 |
| Build | Maven |

## Part of Crux

Crux is a suite of libraries that handle the recurring plumbing of Minecraft
plugin development — menus, commands, configuration, storage — so plugin code
stays focused on features. Each library lives in its own repository.

## License

[MIT](LICENSE)
