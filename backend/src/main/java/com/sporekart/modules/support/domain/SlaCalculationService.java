package com.sporekart.modules.support.domain;

import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;

@Service
public class SlaCalculationService {

    public SlaStatus evaluateSlaStatus(SupportTicket ticket) {
        if (ticket.getStatus().isTerminal()) {
            return ticket.getSlaStatus();
        }

        OffsetDateTime now = OffsetDateTime.now();

        // Evaluate First Response SLA if not responded yet
        if (ticket.getFirstResponseAt() == null) {
            if (now.isAfter(ticket.getFirstResponseDueAt())) {
                return SlaStatus.BREACHED;
            }
            long totalSeconds = ticket.getFirstResponseDueAt().toEpochSecond() - ticket.getCreatedAt().toEpochSecond();
            long elapsedSeconds = now.toEpochSecond() - ticket.getCreatedAt().toEpochSecond();
            if (totalSeconds > 0 && ((double) elapsedSeconds / totalSeconds) >= 0.75) {
                return SlaStatus.AT_RISK;
            }
        }

        // Evaluate Resolution SLA
        if (now.isAfter(ticket.getResolutionDueAt())) {
            return SlaStatus.BREACHED;
        }
        long totalResSeconds = ticket.getResolutionDueAt().toEpochSecond() - ticket.getCreatedAt().toEpochSecond();
        long elapsedResSeconds = now.toEpochSecond() - ticket.getCreatedAt().toEpochSecond();
        if (totalResSeconds > 0 && ((double) elapsedResSeconds / totalResSeconds) >= 0.75) {
            return SlaStatus.AT_RISK;
        }

        return SlaStatus.MET;
    }
}
