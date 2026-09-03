package com.sporekart.modules.training;

import com.sporekart.modules.training.application.EnrollmentApplicationService;
import com.sporekart.modules.training.application.EnrollmentLifecycleService;
import com.sporekart.modules.training.controller.TraineeEnrollmentLifecycleController;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingEnrollmentHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeEnrollmentLifecycleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TrainingEnrollmentLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentApplicationService enrollmentService;

    @MockBean
    private EnrollmentLifecycleService lifecycleService;

    @Test
    @WithMockUser(username = "trainee-1")
    @DisplayName("GET /api/v1/training/my-enrollments should return paginated enrollments with lifecycle status")
    void testGetMyEnrollments() throws Exception {
        TrainingEnrollment enrollment = TrainingEnrollment.create("batch-101", "trainee-1", new BigDecimal("5000.00"), "INR", null, "trainee-1");
        enrollment.markPaymentPending();
        enrollment.markPaymentVerified("TRN-PAY-101");
        enrollment.confirm("TRN-PAY-101");

        when(enrollmentService.getTraineeEnrollments(eq("trainee-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(enrollment)));

        mockMvc.perform(get("/api/v1/training/my-enrollments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(enrollment.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.content[0].priceAmount").value(5000.00));
    }

    @Test
    @WithMockUser(username = "trainee-1")
    @DisplayName("GET /api/v1/training/my-enrollments/{id}/history should return transition audit history")
    void testGetMyEnrollmentHistory() throws Exception {
        String enrollmentId = UUID.randomUUID().toString();
        TrainingEnrollmentHistory history1 = TrainingEnrollmentHistory.record(enrollmentId, null, EnrollmentStatus.PENDING, "CREATED", "trainee-1");
        TrainingEnrollmentHistory history2 = TrainingEnrollmentHistory.record(enrollmentId, EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED, "PAYMENT_CONFIRMED", "SYSTEM");

        when(lifecycleService.getEnrollmentHistory(eq(enrollmentId), eq("trainee-1"), anyBoolean()))
                .thenReturn(List.of(history1, history2));

        mockMvc.perform(get("/api/v1/training/my-enrollments/" + enrollmentId + "/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].toStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[1].toStatus").value("CONFIRMED"));
    }
}
