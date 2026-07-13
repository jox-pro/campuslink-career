package com.campuslink.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class AuditLogger {
    private static final Path LOG_DIR = Paths.get(System.getProperty("user.home"), ".campuslink-career", "logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("auth-audit.log");

    private AuditLogger() {}

    public static void log(String event, String username, String outcome, String details) {
        try {
            Files.createDirectories(LOG_DIR);
            String entry = String.format("%s event=%s username=%s outcome=%s details=%s%n",
                Instant.now(),
                safe(event),
                safe(username),
                safe(outcome),
                safe(details));
            Files.writeString(LOG_FILE, entry, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("Unable to write audit log: " + ex.getMessage());
        }
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.replace('\n', ' ').replace('\r', ' ');
    }
}
