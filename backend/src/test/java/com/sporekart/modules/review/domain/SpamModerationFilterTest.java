package com.sporekart.modules.review.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpamModerationFilterTest {

    private final SpamModerationFilter spamModerationFilter = new SpamModerationFilter();

    @Test
    @DisplayName("Should pass clean review text")
    void shouldPassCleanText() {
        ModerationOutcome outcome = spamModerationFilter.evaluateReview("High Quality Product", "Loved this item!");
        assertThat(outcome).isEqualTo(ModerationOutcome.PASS);
    }

    @Test
    @DisplayName("Should flag review with spam keywords")
    void shouldFlagSpamKeywords() {
        ModerationOutcome outcome = spamModerationFilter.evaluateReview("Free Money Offer", "Visit http://spam.com for free money");
        assertThat(outcome).isEqualTo(ModerationOutcome.FLAG_SPAM);
    }
}
