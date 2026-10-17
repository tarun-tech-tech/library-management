package com.smartlib.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public final class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String renderUrl = environment.getProperty("DATABASE_URL");
        String configuredUrl = renderUrl == null || renderUrl.isBlank()
                ? environment.getProperty("DB_URL") : renderUrl;
        if (configuredUrl == null || configuredUrl.isBlank()) return;

        DatabaseUrlNormalizer.NormalizedDatabaseUrl normalized = DatabaseUrlNormalizer.normalize(configuredUrl);
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("SMARTLIB_JDBC_DATABASE_URL", normalized.jdbcUrl());
        if (isBlank(environment.getProperty("DB_USERNAME")) && normalized.username() != null) {
            overrides.put("DB_USERNAME", normalized.username());
        }
        if (isBlank(environment.getProperty("DB_PASSWORD")) && normalized.password() != null) {
            overrides.put("DB_PASSWORD", normalized.password());
        }
        environment.getPropertySources().addFirst(new MapPropertySource("smartlibDatabaseUrl", overrides));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
