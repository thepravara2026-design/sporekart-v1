package com.sporekart.modules.review.domain;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpamModerationFilter {

    private static final List<String> BLACKLISTED_WORDS = List.of(
            "scam", "fake", "spam", "fraud", "casino", "viagra", "free money", "http://", "https://"
    );

    public ModerationOutcome evaluateReview(String title, String comment) {
        String combined = (title + " " + comment).toLowerCase();
        for (String word : BLACKLISTED_WORDS) {
            if (combined.contains(word)) {
                return ModerationOutcome.FLAG_SPAM;
            }
        }
        return ModerationOutcome.PASS;
    }
}
