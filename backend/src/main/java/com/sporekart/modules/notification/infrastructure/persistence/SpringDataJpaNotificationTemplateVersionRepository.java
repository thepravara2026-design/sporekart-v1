package com.sporekart.modules.notification.infrastructure.persistence;

import com.sporekart.modules.notification.domain.NotificationChannel;
import com.sporekart.modules.notification.domain.NotificationTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaNotificationTemplateVersionRepository extends JpaRepository<NotificationTemplateVersion, String> {

    List<NotificationTemplateVersion> findByTemplateCode(String templateCode);

    Optional<NotificationTemplateVersion> findByTemplateCodeAndChannelAndVersionAndLocale(
            String templateCode, NotificationChannel channel, int version, String locale);

    Optional<NotificationTemplateVersion> findFirstByTemplateCodeAndChannelAndStatusAndLocaleOrderByVersionDesc(
            String templateCode, NotificationChannel channel, NotificationTemplateVersion.TemplateStatus status, String locale);
}
