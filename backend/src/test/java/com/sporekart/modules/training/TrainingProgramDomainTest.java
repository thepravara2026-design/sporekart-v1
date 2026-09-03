package com.sporekart.modules.training;

import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.InvalidTrainingProgramDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TrainingProgramDomainTest {

    @Test
    @DisplayName("Should create TrainingProgram with generated slug and default DRAFT status")
    void testCreateProgram() {
        TrainingProgram program = TrainingProgram.create(
                "  Mushroom Cultivation 101  ",
                "Introductory course for beginners",
                "CULTIVATION",
                12,
                new BigDecimal("1499.00"),
                "INR",
                "ADMIN_01"
        );

        assertNotNull(program.getId());
        assertEquals("Mushroom Cultivation 101", program.getTitle());
        assertEquals("mushroom-cultivation-101", program.getSlug());
        assertEquals("Introductory course for beginners", program.getDescription());
        assertEquals("CULTIVATION", program.getCategory());
        assertEquals(12, program.getDurationHours());
        assertEquals(ProgramStatus.DRAFT, program.getStatus());
        assertEquals(new BigDecimal("1499.00"), program.getPriceAmount());
        assertEquals("INR", program.getCurrency());
        assertEquals("ADMIN_01", program.getCreatedBy());
    }

    @Test
    @DisplayName("Should reject blank or null title during creation")
    void testInvalidTitle() {
        assertThrows(InvalidTrainingProgramDataException.class, () ->
                TrainingProgram.create("   ", "Desc", "GEN", 4, new BigDecimal("100"), "INR", "ADMIN")
        );
        assertThrows(InvalidTrainingProgramDataException.class, () ->
                TrainingProgram.create(null, "Desc", "GEN", 4, new BigDecimal("100"), "INR", "ADMIN")
        );
    }

    @Test
    @DisplayName("Should reject negative duration or negative price")
    void testInvalidDurationAndPrice() {
        assertThrows(InvalidTrainingProgramDataException.class, () ->
                TrainingProgram.create("Valid Title", "Desc", "GEN", -5, new BigDecimal("100"), "INR", "ADMIN")
        );
        assertThrows(InvalidTrainingProgramDataException.class, () ->
                TrainingProgram.create("Valid Title", "Desc", "GEN", 5, new BigDecimal("-10.00"), "INR", "ADMIN")
        );
    }

    @Test
    @DisplayName("Should enforce state transitions correctly")
    void testStateTransitions() {
        TrainingProgram program = TrainingProgram.create("Gourmet Fungi", "Desc", "GEN", 8, new BigDecimal("500"), "INR", "ADMIN");
        assertEquals(ProgramStatus.DRAFT, program.getStatus());

        program.activate();
        assertEquals(ProgramStatus.ACTIVE, program.getStatus());

        program.deactivate();
        assertEquals(ProgramStatus.INACTIVE, program.getStatus());

        program.activate();
        assertEquals(ProgramStatus.ACTIVE, program.getStatus());

        program.archive();
        assertEquals(ProgramStatus.ARCHIVED, program.getStatus());

        assertThrows(InvalidTrainingProgramDataException.class, program::activate);
        assertThrows(InvalidTrainingProgramDataException.class, program::deactivate);
    }
}
