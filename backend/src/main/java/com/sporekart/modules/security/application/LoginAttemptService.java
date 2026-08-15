package com.sporekart.modules.security.application;

import com.sporekart.modules.security.domain.UserAccount;
import com.sporekart.modules.security.infrastructure.persistence.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final UserAccountRepository userAccountRepository;
    private final int maxAttempts;
    private final long lockDurationMinutes;

    public LoginAttemptService(
            UserAccountRepository userAccountRepository,
            @Value("${app.security.brute-force.max-attempts:5}") int maxAttempts,
            @Value("${app.security.brute-force.lock-duration-minutes:15}") long lockDurationMinutes
    ) {
        this.userAccountRepository = userAccountRepository;
        this.maxAttempts = maxAttempts;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    @Transactional
    public void recordFailedAttempt(UserAccount user) {
        user.incrementFailedAttempts(maxAttempts, lockDurationMinutes);
        userAccountRepository.save(user);
    }

    @Transactional
    public void recordSuccess(UserAccount user) {
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.resetFailedAttempts();
            userAccountRepository.save(user);
        }
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getLockDurationMinutes() {
        return lockDurationMinutes;
    }
}
