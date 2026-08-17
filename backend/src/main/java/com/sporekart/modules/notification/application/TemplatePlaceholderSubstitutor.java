package com.sporekart.modules.notification.application;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplatePlaceholderSubstitutor {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{?([a-zA-Z0-9_.-]+)\\}\\}?");

    public String substitute(String templateContent, Map<String, ?> variables) {
        if (templateContent == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return templateContent;
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateContent);

        while (matcher.find()) {
            String key = matcher.group(1);
            Object rawValue = variables.get(key);
            String replacement = rawValue != null ? sanitize(rawValue.toString()) : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String sanitize(String value) {
        if (value == null) return "";
        // Prevent basic script/html injection tags
        return value.replace("<", "&lt;").replace(">", "&gt;");
    }
}
