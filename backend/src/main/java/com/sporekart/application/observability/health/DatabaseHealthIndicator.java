package com.sporekart.application.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Custom health indicator for database readiness validation.
 */
@Component("databaseHealthIndicator")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(@org.springframework.beans.factory.annotation.Autowired(required = false) DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        if (dataSource == null) {
            return Health.up().withDetail("database", "MOCK_ENVIRONMENT").build();
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SELECT 1");
            return Health.up()
                    .withDetail("database", "PostgreSQL/H2 Engine")
                    .withDetail("status", "ACTIVE_CONNECTED")
                    .build();

        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("database", "PostgreSQL/H2 Engine")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
