package com.sporekart.modules.training.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sporekart.training")
public class TrainingPolicyProperties {

    private int cancellationAdminDays = 7;
    private int cancellationTraineeDays = 2;

    public int getCancellationAdminDays() {
        return cancellationAdminDays;
    }

    public void setCancellationAdminDays(int cancellationAdminDays) {
        this.cancellationAdminDays = cancellationAdminDays;
    }

    public int getCancellationTraineeDays() {
        return cancellationTraineeDays;
    }

    public void setCancellationTraineeDays(int cancellationTraineeDays) {
        this.cancellationTraineeDays = cancellationTraineeDays;
    }
}
