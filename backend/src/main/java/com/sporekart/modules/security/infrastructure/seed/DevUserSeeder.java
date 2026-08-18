package com.sporekart.modules.security.infrastructure.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DEV/QAT-only seed: ensures a valid admin and customer test account exist.
 *
 * Credentials:
 *   Admin:    admin@sporekart.com  / SporekartAdmin@123
 *   Customer: user@sporekart.com   / SporekartUser@123
 *
 * These accounts are ONLY created in dev and qat profiles.
 * They are NEVER present in production (profile guard prevents it).
 */
@Component
@Profile({"dev", "qat"})
public class DevUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder;

    public DevUserSeeder(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) {
        seedAdminIfMissing();
        seedCustomerIfMissing();
        seedGrowerIfMissing();
    }

    private void seedAdminIfMissing() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = 'admin@sporekart.com'", Integer.class);
        if (count != null && count > 0) {
            log.debug("Dev admin user already exists. Skipping.");
            return;
        }
        String hash = passwordEncoder.encode("SporekartAdmin@123");
        jdbc.update(
                "INSERT INTO users (id, email, password_hash, first_name, last_name, role, status) " +
                "VALUES ('dev-admin-1', 'admin@sporekart.com', ?, 'Dev', 'Admin', 'ROLE_ADMIN', 'ACTIVE')",
                hash
        );
        try {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES ('dev-admin-1', 'ROLE_ADMIN')");
        } catch (Exception e) {
            log.debug("user_roles insert skipped (may not exist or already set): {}", e.getMessage());
        }
        log.info("Dev admin user seeded: admin@sporekart.com");
    }

    private void seedCustomerIfMissing() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = 'user@sporekart.com'", Integer.class);
        if (count != null && count > 0) {
            log.debug("Dev customer user already exists. Skipping.");
            return;
        }
        String hash = passwordEncoder.encode("SporekartUser@123");
        jdbc.update(
                "INSERT INTO users (id, email, password_hash, first_name, last_name, role, status) " +
                "VALUES ('dev-user-1', 'user@sporekart.com', ?, 'Dev', 'User', 'ROLE_CUSTOMER', 'ACTIVE')",
                hash
        );
        try {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES ('dev-user-1', 'ROLE_CUSTOMER')");
        } catch (Exception e) {
            log.debug("user_roles insert skipped: {}", e.getMessage());
        }
        log.info("Dev customer user seeded: user@sporekart.com");
    }

    private void seedGrowerIfMissing() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = 'grower@sporekart.com'", Integer.class);
        if (count != null && count > 0) {
            log.debug("Dev grower user already exists. Skipping.");
            return;
        }
        String hash = passwordEncoder.encode("SporekartGrower@123");
        jdbc.update(
                "INSERT INTO users (id, email, password_hash, first_name, last_name, role, status) " +
                "VALUES ('grower-1', 'grower@sporekart.com', ?, 'Spore', 'Grower', 'ROLE_GROWER', 'ACTIVE')",
                hash
        );
        try {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES ('grower-1', 'ROLE_GROWER')");
            jdbc.update(
                    "INSERT INTO grower_profiles (id, user_id, business_name, contact_email, contact_phone, farm_address, status) " +
                    "VALUES ('grower-profile-1', 'grower-1', 'Apex Spore Farms', 'grower@sporekart.com', '+1-555-0199', '100 Mycology Way, Mushroom Valley, CA', 'ACTIVE')"
            );
        } catch (Exception e) {
            log.debug("grower user_roles / profiles insert skipped: {}", e.getMessage());
        }
        log.info("Dev grower user seeded: grower@sporekart.com");
    }
}
