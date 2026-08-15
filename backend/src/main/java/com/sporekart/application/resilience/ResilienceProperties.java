package com.sporekart.application.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties mapping app.resilience for payment, shipping, and database settings.
 */
@Component
@ConfigurationProperties(prefix = "app.resilience")
public class ResilienceProperties {

    private Policy payment = new Policy(3000, 5000, 3, 200, 2.0, 0.25);
    private Policy shipping = new Policy(3000, 5000, 3, 200, 2.0, 0.25);
    private Database database = new Database(5000, 3);

    public Policy getPayment() {
        return payment;
    }

    public void setPayment(Policy payment) {
        this.payment = payment;
    }

    public Policy getShipping() {
        return shipping;
    }

    public void setShipping(Policy shipping) {
        this.shipping = shipping;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public static class Policy {
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;
        private int maxAttempts = 3;
        private long initialBackoffMs = 200;
        private double backoffMultiplier = 2.0;
        private double jitterFactor = 0.25;

        public Policy() {}

        public Policy(int connectTimeoutMs, int readTimeoutMs, int maxAttempts, long initialBackoffMs, double backoffMultiplier, double jitterFactor) {
            this.connectTimeoutMs = connectTimeoutMs;
            this.readTimeoutMs = readTimeoutMs;
            this.maxAttempts = maxAttempts;
            this.initialBackoffMs = initialBackoffMs;
            this.backoffMultiplier = backoffMultiplier;
            this.jitterFactor = jitterFactor;
        }

        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public long getInitialBackoffMs() { return initialBackoffMs; }
        public void setInitialBackoffMs(long initialBackoffMs) { this.initialBackoffMs = initialBackoffMs; }
        public double getBackoffMultiplier() { return backoffMultiplier; }
        public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }
        public double getJitterFactor() { return jitterFactor; }
        public void setJitterFactor(double jitterFactor) { this.jitterFactor = jitterFactor; }
    }

    public static class Database {
        private int lockTimeoutMs = 5000;
        private int maxRetryAttempts = 3;

        public Database() {}

        public Database(int lockTimeoutMs, int maxRetryAttempts) {
            this.lockTimeoutMs = lockTimeoutMs;
            this.maxRetryAttempts = maxRetryAttempts;
        }

        public int getLockTimeoutMs() { return lockTimeoutMs; }
        public void setLockTimeoutMs(int lockTimeoutMs) { this.lockTimeoutMs = lockTimeoutMs; }
        public int getMaxRetryAttempts() { return maxRetryAttempts; }
        public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    }
}
