package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.NotificationCategory;
import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationPreference;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationPreferenceService {

    private final SpringDataJpaNotificationPreferenceRepository preferenceRepository;

    public NotificationPreferenceService(SpringDataJpaNotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    public boolean isNotificationAllowed(String userId, NotificationCategory category, NotificationChannel channel) {
        if (category == null || category.isMandatory()) {
            return true;
        }
        if (userId == null || userId.isBlank()) {
            return true;
        }

        Optional<NotificationPreference> prefOpt = preferenceRepository.findByUserIdAndCategory(userId, category);
        if (prefOpt.isEmpty()) {
            return true; // Default opt-in
        }
        return prefOpt.get().isChannelEnabled(channel);
    }

    public List<NotificationPreference> getUserPreferences(String userId) {
        return preferenceRepository.findByUserId(userId);
    }

    @Transactional
    public NotificationPreference updatePreference(String userId, NotificationCategory category,
                                                  boolean emailEnabled, boolean smsEnabled,
                                                  boolean whatsappEnabled, boolean inAppEnabled) {
        Optional<NotificationPreference> prefOpt = preferenceRepository.findByUserIdAndCategory(userId, category);
        NotificationPreference pref;
        if (prefOpt.isPresent()) {
            pref = prefOpt.get();
            pref.updatePreferences(emailEnabled, smsEnabled, whatsappEnabled, inAppEnabled);
        } else {
            pref = new NotificationPreference(userId, category, emailEnabled, smsEnabled, whatsappEnabled, inAppEnabled);
        }
        return preferenceRepository.save(pref);
    }
}
