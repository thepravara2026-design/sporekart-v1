package com.sporekart.modules.notification.application;

import com.sporekart.modules.notification.domain.*;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationTemplateRepository;
import com.sporekart.modules.notification.infrastructure.persistence.SpringDataJpaNotificationTemplateVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationTemplateService {

    private final SpringDataJpaNotificationTemplateRepository templateRepository;
    private final SpringDataJpaNotificationTemplateVersionRepository versionRepository;
    private final TemplatePlaceholderSubstitutor placeholderSubstitutor;

    public NotificationTemplateService(SpringDataJpaNotificationTemplateRepository templateRepository,
                                       SpringDataJpaNotificationTemplateVersionRepository versionRepository,
                                       TemplatePlaceholderSubstitutor placeholderSubstitutor) {
        this.templateRepository = templateRepository;
        this.versionRepository = versionRepository;
        this.placeholderSubstitutor = placeholderSubstitutor;
    }

    @Transactional
    public NotificationTemplate createTemplate(String templateCode, String name, String description, NotificationCategory category) {
        String cleanCode = templateCode.toUpperCase().trim();
        Optional<NotificationTemplate> existing = templateRepository.findByTemplateCode(cleanCode);
        if (existing.isPresent()) {
            return existing.get();
        }
        NotificationTemplate template = new NotificationTemplate(cleanCode, name, description, category);
        return templateRepository.save(template);
    }

    @Transactional
    public NotificationTemplateVersion createTemplateVersion(String templateCode, NotificationChannel channel,
                                                             int version, String locale, String subject, String body) {
        validateTemplateSyntax(subject);
        validateTemplateSyntax(body);

        NotificationTemplate template = templateRepository.findByTemplateCode(templateCode.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateCode));

        NotificationTemplateVersion templateVersion = new NotificationTemplateVersion(
                template.getId(), template.getTemplateCode(), channel, version, locale, subject, body);
        return versionRepository.save(templateVersion);
    }

    public void validateTemplateSyntax(String text) {
        if (text == null || text.isBlank()) return;
        int openBraces = 0;
        for (char c : text.toCharArray()) {
            if (c == '{') openBraces++;
            else if (c == '}') {
                openBraces--;
                if (openBraces < 0) {
                    throw new IllegalArgumentException("Malformed template syntax: unbalanced braces");
                }
            }
        }
        if (openBraces != 0) {
            throw new IllegalArgumentException("Malformed template syntax: unbalanced braces");
        }
    }

    @Transactional
    public NotificationTemplateVersion activateTemplateVersion(String versionId) {
        NotificationTemplateVersion target = versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Template version not found: " + versionId));

        validateTemplateSyntax(target.getSubject());
        validateTemplateSyntax(target.getBody());

        // Deactivate active versions for same code, channel, locale
        List<NotificationTemplateVersion> versions = versionRepository.findByTemplateCode(target.getTemplateCode());
        for (NotificationTemplateVersion v : versions) {
            if (v.getChannel() == target.getChannel() && v.getLocale().equalsIgnoreCase(target.getLocale())
                    && v.getStatus() == NotificationTemplateVersion.TemplateStatus.ACTIVE) {
                v.archive();
                versionRepository.save(v);
            }
        }

        target.activate();
        return versionRepository.save(target);
    }

    public RenderedTemplate renderActiveTemplate(String templateCode, NotificationChannel channel,
                                                String locale, Map<String, ?> variables) {
        String reqLocale = (locale != null && !locale.isBlank()) ? locale : "en-US";
        Optional<NotificationTemplateVersion> activeOpt = versionRepository
                .findFirstByTemplateCodeAndChannelAndStatusAndLocaleOrderByVersionDesc(
                        templateCode.toUpperCase().trim(), channel, NotificationTemplateVersion.TemplateStatus.ACTIVE, reqLocale);

        if (activeOpt.isEmpty() && !"en-US".equalsIgnoreCase(reqLocale)) {
            // Fallback locale en-US
            activeOpt = versionRepository.findFirstByTemplateCodeAndChannelAndStatusAndLocaleOrderByVersionDesc(
                    templateCode.toUpperCase().trim(), channel, NotificationTemplateVersion.TemplateStatus.ACTIVE, "en-US");
        }

        if (activeOpt.isEmpty()) {
            // Default dynamic render fallback
            String subject = "Notification: " + templateCode;
            String body = variables != null && variables.containsKey("message") ?
                    variables.get("message").toString() : "Notification message for event " + templateCode;
            return new RenderedTemplate(1, subject, body);
        }

        NotificationTemplateVersion version = activeOpt.get();
        String renderedSubject = placeholderSubstitutor.substitute(version.getSubject(), variables);
        String renderedBody = placeholderSubstitutor.substitute(version.getBody(), variables);

        return new RenderedTemplate(version.getVersion(), renderedSubject, renderedBody);
    }

    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<NotificationTemplate> getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode.toUpperCase().trim());
    }

    public List<NotificationTemplateVersion> getVersionsForTemplate(String templateCode) {
        return versionRepository.findByTemplateCode(templateCode.toUpperCase().trim());
    }

    public record RenderedTemplate(int version, String subject, String body) {}
}
