package com.example.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application-wide shared executors. Repositories used to each create their own
 * {@code Executors.newSingleThreadExecutor()} that was never shut down, so threads
 * accumulated every time a repository was instantiated (e.g. on navigation).
 * Sharing a single pool avoids that leak.
 */
public final class AppExecutors {

    private static final ExecutorService IO = Executors.newFixedThreadPool(3);

    private AppExecutors() {}

    /** Background executor for disk/DB/network-callback work. */
    public static ExecutorService io() {
        return IO;
    }
}
