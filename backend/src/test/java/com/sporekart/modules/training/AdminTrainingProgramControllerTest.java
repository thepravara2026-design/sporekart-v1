package com.sporekart.modules.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.TrainingProgramApplicationService;
import com.sporekart.modules.training.controller.AdminTrainingProgramController;
import com.sporekart.modules.training.controller.dto.CreateTrainingProgramRequest;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.TrainingNotFoundException;
import com.sporekart.modules.training.domain.exception.TrainingProgramAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTrainingProgramController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminTrainingProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingProgramApplicationService programService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/training-programs should create program and return 201 Created")
    void testCreateProgramSuccess() throws Exception {
        CreateTrainingProgramRequest request = new CreateTrainingProgramRequest(
                "Substrate Sterilization & Inoculation",
                "Complete guide to substrate prep",
                "CULTIVATION",
                8,
                new BigDecimal("1200.00"),
                "INR"
        );

        TrainingProgram program = TrainingProgram.create(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getDurationHours(),
                request.getPriceAmount(),
                request.getCurrency(),
                "admin-user"
        );

        when(programService.createProgram(any(), any(), any(), eq(8), any(), any(), any()))
                .thenReturn(program);

        mockMvc.perform(post("/api/v1/admin/training-programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Substrate Sterilization & Inoculation"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/training-programs with duplicate title should return 409 Conflict")
    void testCreateProgramDuplicate() throws Exception {
        CreateTrainingProgramRequest request = new CreateTrainingProgramRequest(
                "Existing Program", "Desc", "GEN", 4, new BigDecimal("100"), "INR"
        );

        when(programService.createProgram(any(), any(), any(), eq(4), any(), any(), any()))
                .thenThrow(new TrainingProgramAlreadyExistsException("A training program with title 'Existing Program' already exists"));

        mockMvc.perform(post("/api/v1/admin/training-programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TRAINING_PROGRAM_ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/training-programs/{id}/activate should return 200 OK")
    void testActivateProgram() throws Exception {
        TrainingProgram program = TrainingProgram.create("Mycology", "Desc", "GEN", 4, new BigDecimal("100"), "INR", "ADMIN_USER");
        program.activate();

        when(programService.activateProgram(eq("prog-123"), any())).thenReturn(program);

        mockMvc.perform(post("/api/v1/admin/training-programs/prog-123/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/training-programs/{id} not found should return 404 Not Found")
    void testGetProgramNotFound() throws Exception {
        when(programService.getProgramById("non-existent"))
                .thenThrow(new TrainingNotFoundException("TrainingProgram not found for id: non-existent"));

        mockMvc.perform(get("/api/v1/admin/training-programs/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TRAINING_NOT_FOUND"));
    }
}
