package com.smartlib.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DatabaseUrlNormalizerTest {

    @Test
    void convertsRenderConnectionStringAndExtractsCredentials() {
        DatabaseUrlNormalizer.NormalizedDatabaseUrl result = DatabaseUrlNormalizer.normalize(
                "postgresql://smart%20user:p%40ss@internal.render.com:5432/smartlib?sslmode=require");

        assertEquals("jdbc:postgresql://internal.render.com:5432/smartlib?sslmode=require", result.jdbcUrl());
        assertEquals("smart user", result.username());
        assertEquals("p@ss", result.password());
    }

    @Test
    void rejectsCopiedPlaceholdersBeforeFlywayAttemptsAConnection() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> DatabaseUrlNormalizer.normalize("jdbc:postgresql://DATABASE_HOST:5432/smartlib"));

        assertEquals(true, exception.getMessage().contains("real Internal Database URL"));
    }
}
