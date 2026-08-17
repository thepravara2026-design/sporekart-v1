package com.sporekart.modules.training.infrastructure;

import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Training Module Health Indicator.
 *
 * Registered as a Spring Boot Actuator HealthIndicator under the name "trainingModule".
 * Verifiable via: GET /actuator/health/trainingModule
 *
 * Checks:
 * - Training DB connectivity (lightweight read on training_programs)
 *
 * Design constraints:
 * - Does NOT ping external payment or notification providers (would cause liveness loop)
 * - Does NOT block application startup if the check degrades
 * - Uses read-only access
 */
@Component("trainingModule")
public class TrainingModuleHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(TrainingModuleHealthIndicator.class);

    private final TrainingProgramRepository programRepository;

    public TrainingModuleHealthIndicator(TrainingProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Override
    public Health health() {
        try {
            long programCount = programRepository.findAll(PageRequest.of(0, 1)).getTotalElements();
            return Health.up()
                    .withDetail("status", "Training Module operational")
                    .withDetail("trainingProgramsAccessible", true)
                    .withDetail("programCount", programCount)
                    .build();
        } catch (Exception ex) {
            log.error("Training Module health check failed: {}", ex.getMessage());
            return Health.down()
                    .withDetail("status", "Training Module DB unreachable")
                    .withDetail("error", ex.getClass().getSimpleName())
                    .build();
        }
    }
}
