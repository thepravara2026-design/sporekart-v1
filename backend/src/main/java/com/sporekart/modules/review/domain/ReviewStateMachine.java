package com.sporekart.modules.review.domain;

import java.util.*;

public class ReviewStateMachine {

    private static final Map<ReviewStatus, Set<ReviewStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ReviewStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ReviewStatus.PENDING_MODERATION, Set.of(ReviewStatus.APPROVED, ReviewStatus.REJECTED, ReviewStatus.FLAGGED));
        ALLOWED_TRANSITIONS.put(ReviewStatus.APPROVED, Set.of(ReviewStatus.FLAGGED, ReviewStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ReviewStatus.FLAGGED, Set.of(ReviewStatus.APPROVED, ReviewStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ReviewStatus.REJECTED, Collections.emptySet());
    }

    public static boolean isValidTransition(ReviewStatus current, ReviewStatus target) {
        if (current == target) {
            return true;
        }
        Set<ReviewStatus> allowed = ALLOWED_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }

    public static void validateTransition(ReviewStatus current, ReviewStatus target) {
        if (!isValidTransition(current, target)) {
            throw new IllegalStateException("Invalid review status transition from " + current + " to " + target);
        }
    }
}
