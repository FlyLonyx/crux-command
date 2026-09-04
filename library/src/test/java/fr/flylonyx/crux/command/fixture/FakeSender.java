package fr.flylonyx.crux.command.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import fr.flylonyx.crux.command.core.sender.CxSender;
import fr.flylonyx.crux.command.core.sender.CxSenderType;

/**
 * A sender that records what it was told, for tests.
 *
 * <p>The engine only ever sees {@link CxSender}, so this is enough to exercise routing,
 * permission filtering and message rendering with no server and no mocking framework.
 */
public final class FakeSender implements CxSender {

    private final String name;
    private final CxSenderType type;
    private final Set<String> permissions;
    private final List<String> received = new ArrayList<>();

    private FakeSender(String name, CxSenderType type, Set<String> permissions) {
        this.name = name;
        this.type = type;
        this.permissions = permissions;
    }

    public static FakeSender player(String name, String... permissions) {
        return new FakeSender(name, CxSenderType.PLAYER, new HashSet<>(Arrays.asList(permissions)));
    }

    public static FakeSender console(String... permissions) {
        return new FakeSender("CONSOLE", CxSenderType.CONSOLE, new HashSet<>(Arrays.asList(permissions)));
    }

    public static FakeSender block(String... permissions) {
        return new FakeSender("@", CxSenderType.BLOCK, new HashSet<>(Arrays.asList(permissions)));
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public CxSenderType type() {
        return this.type;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public void send(String message) {
        this.received.add(message);
    }

    /** Returns every message this sender was given, in order. */
    public List<String> received() {
        return Collections.unmodifiableList(this.received);
    }
}
