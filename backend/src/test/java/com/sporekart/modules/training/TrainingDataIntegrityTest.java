package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TRAINING 14 — Data Integrity Invariant Tests.
 *
 * Executable assertions against the live (H2 test) database.
 * These tests verify that the application, schema constraints, and migrations
 * collectively maintain all critical data integrity invariants.
 *
 * Every invariant here corresponds to a business rule from the Training Module specification.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Training Module — Data Integrity Invariants")
class TrainingDataIntegrityTest {

    @Autowired
    private JdbcTemplate jdbc;

    // ─── Capacity Invariants ─────────────────────────────────────────────────

    @Test
    @DisplayName("INV-01: occupied_seats must never exceed total_capacity for any batch")
    void inv01_occupiedSeatsNeverExceedTotalCapacity() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, batch_code, occupied_seats, total_capacity " +
                "FROM training_batches " +
                "WHERE occupied_seats > total_capacity"
        );
        assertThat(violations)
                .as("No batch should have occupied_seats > total_capacity. Violations: %s", violations)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-02: occupied_seats must be >= 0 for all batches")
    void inv02_occupiedSeatsNonNegative() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, batch_code, occupied_seats FROM training_batches WHERE occupied_seats < 0"
        );
        assertThat(violations)
                .as("No batch should have negative occupied_seats. Violations: %s", violations)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-03: total_capacity must be > 0 for all batches")
    void inv03_totalCapacityPositive() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, batch_code, total_capacity FROM training_batches WHERE total_capacity <= 0"
        );
        assertThat(violations)
                .as("No batch should have zero or negative total_capacity. Violations: %s", violations)
                .isEmpty();
    }

    // ─── Enrollment Integrity ─────────────────────────────────────────────────

    @Test
    @DisplayName("INV-04: CONFIRMED/ACTIVE/COMPLETED enrollments must reference a valid batch")
    void inv04_activeEnrollmentsHaveValidBatch() {
        List<Map<String, Object>> orphaned = jdbc.queryForList(
                "SELECT e.id, e.status, e.batch_id " +
                "FROM training_enrollments e " +
                "WHERE e.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED') " +
                "AND NOT EXISTS (SELECT 1 FROM training_batches b WHERE b.id = e.batch_id)"
        );
        assertThat(orphaned)
                .as("Capacity-consuming enrollments must not have orphaned batch references. Violations: %s", orphaned)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-05: No trainee should have two CONFIRMED/ACTIVE enrollments in the same batch")
    void inv05_noDuplicateActiveEnrollmentsPerBatch() {
        List<Map<String, Object>> duplicates = jdbc.queryForList(
                "SELECT batch_id, trainee_id, COUNT(*) AS cnt " +
                "FROM training_enrollments " +
                "WHERE status IN ('CONFIRMED', 'ACTIVE') " +
                "GROUP BY batch_id, trainee_id " +
                "HAVING COUNT(*) > 1"
        );
        assertThat(duplicates)
                .as("No trainee should have duplicate active enrollments in the same batch. Violations: %s", duplicates)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-06: CANCELLED enrollments must not be CONFIRMED, ACTIVE, or COMPLETED")
    void inv06_cancelledEnrollmentsNotActive() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, status FROM training_enrollments " +
                "WHERE status = 'CANCELLED' AND status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')"
        );
        assertThat(violations).as("CANCELLED is mutually exclusive with active statuses").isEmpty();
    }

    // ─── Payment Integrity ────────────────────────────────────────────────────

    @Test
    @DisplayName("INV-07: training_enrollment_payments amount must be >= 0")
    void inv07_paymentAmountNonNegative() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, amount FROM training_enrollment_payments WHERE amount < 0"
        );
        assertThat(violations)
                .as("No training payment should have a negative amount. Violations: %s", violations)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-08: ENROLLMENT_CONFIRMED payments must reference a valid enrollment")
    void inv08_confirmedPaymentsHaveValidEnrollment() {
        List<Map<String, Object>> orphaned = jdbc.queryForList(
                "SELECT p.id, p.status, p.enrollment_id " +
                "FROM training_enrollment_payments p " +
                "WHERE p.status = 'ENROLLMENT_CONFIRMED' " +
                "AND p.enrollment_id IS NOT NULL " +
                "AND NOT EXISTS (SELECT 1 FROM training_enrollments e WHERE e.id = p.enrollment_id)"
        );
        assertThat(orphaned)
                .as("ENROLLMENT_CONFIRMED payments must reference valid enrollments. Violations: %s", orphaned)
                .isEmpty();
    }

    // ─── Certificate Integrity ────────────────────────────────────────────────

    @Test
    @DisplayName("INV-09: certificates must reference a valid enrollment")
    void inv09_certificatesHaveValidEnrollment() {
        List<Map<String, Object>> orphaned = jdbc.queryForList(
                "SELECT c.id, c.enrollment_id " +
                "FROM training_certificates c " +
                "WHERE NOT EXISTS (SELECT 1 FROM training_enrollments e WHERE e.id = c.enrollment_id)"
        );
        assertThat(orphaned)
                .as("All certificates must reference a valid enrollment. Violations: %s", orphaned)
                .isEmpty();
    }

    // ─── Audit Integrity ─────────────────────────────────────────────────────

    @Test
    @DisplayName("INV-10: enrollment history actor must never be null")
    void inv10_auditActorNotNull() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, enrollment_id FROM training_enrollment_history WHERE actor IS NULL"
        );
        assertThat(violations)
                .as("Audit records must always have a non-null actor. Violations: %s", violations)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-11: enrollment history must reference valid enrollments")
    void inv11_auditHistoryHasValidEnrollment() {
        List<Map<String, Object>> orphaned = jdbc.queryForList(
                "SELECT h.id, h.enrollment_id " +
                "FROM training_enrollment_history h " +
                "WHERE NOT EXISTS (SELECT 1 FROM training_enrollments e WHERE e.id = h.enrollment_id)"
        );
        assertThat(orphaned)
                .as("Audit history must reference valid enrollments. Violations: %s", orphaned)
                .isEmpty();
    }

    // ─── Attendance Integrity ─────────────────────────────────────────────────

    @Test
    @DisplayName("INV-12: attendance records must reference valid enrollments")
    void inv12_attendanceHasValidEnrollment() {
        List<Map<String, Object>> orphaned = jdbc.queryForList(
                "SELECT a.id, a.enrollment_id " +
                "FROM training_attendance a " +
                "WHERE NOT EXISTS (SELECT 1 FROM training_enrollments e WHERE e.id = a.enrollment_id)"
        );
        assertThat(orphaned)
                .as("Attendance records must reference valid enrollments. Violations: %s", orphaned)
                .isEmpty();
    }

    // ─── Training Program Integrity ───────────────────────────────────────────

    @Test
    @DisplayName("INV-13: training program price must be >= 0")
    void inv13_programPriceNonNegative() {
        List<Map<String, Object>> violations = jdbc.queryForList(
                "SELECT id, title, price_amount FROM training_programs WHERE price_amount < 0"
        );
        assertThat(violations)
                .as("No training program should have a negative price. Violations: %s", violations)
                .isEmpty();
    }

    @Test
    @DisplayName("INV-14: training batches must reference valid programs")
    void inv14_batchesHaveValidProgram() {
        List<Map<String, Object>> orphaned = jdbc.queryForList(
                "SELECT b.id, b.batch_code, b.program_id " +
                "FROM training_batches b " +
                "WHERE NOT EXISTS (SELECT 1 FROM training_programs p WHERE p.id = b.program_id)"
        );
        assertThat(orphaned)
                .as("All batches must reference a valid training program. Violations: %s", orphaned)
                .isEmpty();
    }

    // ─── Capacity Domain Invariant ────────────────────────────────────────────

    @Test
    @DisplayName("INV-15: Capacity.allocateSeat() throws when batch is full")
    void inv15_capacityAllocateThrowsWhenFull() {
        Capacity full = new Capacity(5, 5);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.sporekart.modules.training.domain.exception.CapacityExceededException.class,
                full::allocateSeat,
                "Capacity.allocateSeat() must throw CapacityExceededException when batch is full"
        );
    }

    @Test
    @DisplayName("INV-16: Capacity constructor rejects occupied > total")
    void inv16_capacityRejectsOccupiedExceedingTotal() {
        org.junit.jupiter.api.Assertions.assertThrows(
                com.sporekart.modules.training.domain.exception.CapacityExceededException.class,
                () -> new Capacity(5, 6),
                "Capacity constructor must reject occupied > total"
        );
    }

    @Test
    @DisplayName("INV-17: Capacity constructor rejects negative total")
    void inv17_capacityRejectsNegativeTotal() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Capacity(-1, 0),
                "Capacity constructor must reject negative total"
        );
    }

    @Test
    @DisplayName("INV-18: Capacity.withTotalCapacity() rejects shrinking below occupied")
    void inv18_capacityCannotShrinkBelowOccupied() {
        Capacity c = new Capacity(10, 8);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.sporekart.modules.training.domain.exception.CapacityExceededException.class,
                () -> c.withTotalCapacity(7),
                "Cannot shrink capacity below occupied seats"
        );
    }
}
