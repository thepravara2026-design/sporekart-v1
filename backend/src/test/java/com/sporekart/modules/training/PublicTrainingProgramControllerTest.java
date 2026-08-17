package com.sporekart.modules.training;

import com.sporekart.application.exception.GlobalExceptionHandler;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.training.application.TrainingProgramApplicationService;
import com.sporekart.modules.training.controller.PublicTrainingProgramController;
import com.sporekart.modules.training.domain.TrainingProgram;
import com.sporekart.modules.training.domain.exception.TrainingNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicTrainingProgramController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicTrainingProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingProgramApplicationService programService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/v1/training-programs should return active programs paginated")
    void testListActivePrograms() throws Exception {
        TrainingProgram program = TrainingProgram.create("Public Fungi Course", "Desc", "GENERAL", 10, new BigDecimal("499.00"), "INR", "ADMIN");
        program.activate();

        when(programService.listPublicActivePrograms(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(program)));

        mockMvc.perform(get("/api/v1/training-programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Public Fungi Course"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/training-programs/{idOrSlug} for draft program should return 404 Not Found")
    void testGetInactiveProgram() throws Exception {
        when(programService.getPublicActiveProgram("draft-course"))
                .thenThrow(new TrainingNotFoundException("Active TrainingProgram not found for id or slug: draft-course"));

        mockMvc.perform(get("/api/v1/training-programs/draft-course"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TRAINING_NOT_FOUND"));
    }
}
