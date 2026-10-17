package com.smartlib.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final class DatabaseUrlNormalizer {

    private DatabaseUrlNormalizer() {
    }

    static NormalizedDatabaseUrl normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("A PostgreSQL database URL is required");
        }
        String raw = value.trim();
        if (raw.contains("DATABASE_HOST") || raw.contains("<internal-") || raw.contains("<database-")) {
            throw new IllegalStateException(
                    "The database URL still contains a placeholder. Copy Render's real Internal Database URL; do not use DATABASE_HOST or angle-bracket placeholders.");
        }
        if (raw.startsWith("jdbc:postgresql://")) {
            return new NormalizedDatabaseUrl(raw, null, null);
        }
        if (!raw.startsWith("postgresql://") && !raw.startsWith("postgres://")) {
            throw new IllegalStateException("Database URL must start with jdbc:postgresql:// or postgresql://");
        }

        URI uri = URI.create(raw);
        if (uri.getHost() == null || uri.getPath() == null || uri.getPath().length() < 2) {
            throw new IllegalStateException("The PostgreSQL connection URL is missing a host or database name");
        }
        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() > 0) jdbc.append(':').append(uri.getPort());
        jdbc.append(uri.getRawPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbc.append('?').append(uri.getRawQuery());
        } else {
            jdbc.append("?sslmode=require");
        }

        String username = null;
        String password = null;
        if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
            String[] credentials = uri.getUserInfo().split(":", 2);
            username = decode(credentials[0]);
            password = credentials.length == 2 ? decode(credentials[1]) : null;
        }
        return new NormalizedDatabaseUrl(jdbc.toString(), username, password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    record NormalizedDatabaseUrl(String jdbcUrl, String username, String password) {
    }
}
