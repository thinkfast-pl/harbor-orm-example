package io.harbor.example.shared.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory recorder for HarborORM post-callback invocations, used by integration tests
 * to observe which callbacks fired and in what order.
 */
public final class CallbackLog {

    private static final List<String> ENTRIES = new CopyOnWriteArrayList<>();

    private CallbackLog() {
    }

    public static void record(String entry) {
        ENTRIES.add(entry);
    }

    public static List<String> entries() {
        return List.copyOf(ENTRIES);
    }

    public static void clear() {
        ENTRIES.clear();
    }
}
