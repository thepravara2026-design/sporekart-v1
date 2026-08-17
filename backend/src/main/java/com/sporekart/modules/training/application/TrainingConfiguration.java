package com.sporekart.modules.training.application;

import com.sporekart.modules.training.domain.policy.CancellationPolicy;
import com.sporekart.modules.training.domain.policy.CapacityPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TrainingPolicyProperties.class)
public class TrainingConfiguration {

    @Bean
    public CancellationPolicy cancellationPolicy(TrainingPolicyProperties properties) {
        return new CancellationPolicy(
                properties.getCancellationAdminDays(),
                properties.getCancellationTraineeDays()
        );
    }

    @Bean
    public CapacityPolicy capacityPolicy() {
        return new CapacityPolicy();
    }
}
