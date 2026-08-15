package com.sporekart.modules.security.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserAccount() {}

    public UserAccount(String id, String email, String passwordHash, String firstName, String lastName, UserRole role) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.email = email.toLowerCase().trim();
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role != null ? role : UserRole.ROLE_CUSTOMER;
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static UserAccount createCustomer(String email, String passwordHash, String firstName, String lastName) {
        return new UserAccount(null, email, passwordHash, firstName, lastName, UserRole.ROLE_CUSTOMER);
    }

    public static UserAccount createAdmin(String email, String passwordHash, String firstName, String lastName) {
        return new UserAccount(null, email, passwordHash, firstName, lastName, UserRole.ROLE_ADMIN);
    }

    public boolean isAccountLocked() {
        if (status == UserStatus.LOCKED) {
            if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
                // Auto unlock expired lock
                return false;
            }
            return true;
        }
        return false;
    }

    public void incrementFailedAttempts(int maxAttempts, long lockDurationMinutes) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.status = UserStatus.LOCKED;
            this.lockedUntil = Instant.now().plusSeconds(lockDurationMinutes * 60);
        }
        this.updatedAt = Instant.now();
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
    }

    public void lockAccount(long lockDurationMinutes) {
        this.status = UserStatus.LOCKED;
        this.lockedUntil = Instant.now().plusSeconds(lockDurationMinutes * 60);
        this.updatedAt = Instant.now();
    }

    public void unlockAccount() {
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
