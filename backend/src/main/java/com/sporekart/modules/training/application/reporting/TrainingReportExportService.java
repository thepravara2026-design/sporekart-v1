package com.sporekart.modules.training.application.reporting;

import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingReportExportService {

    private static final Logger log = LoggerFactory.getLogger(TrainingReportExportService.class);

    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingAuditService auditService;

    public TrainingReportExportService(
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingAuditService auditService) {
        this.enrollmentRepository = enrollmentRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public String exportEnrollmentsCsv() {
        log.info("Generating CSV export for training enrollments");
        StringBuilder csv = new StringBuilder();
        csv.append("EnrollmentID,EnrollmentCode,BatchID,TraineeID,Status,PaymentRef,Amount,Currency,CreatedAt\n");

        List<TrainingEnrollment> enrollments = enrollmentRepository.findAll();
        for (TrainingEnrollment e : enrollments) {
            csv.append(escapeCsv(e.getId())).append(",")
                    .append(escapeCsv(e.getEnrollmentCode())).append(",")
                    .append(escapeCsv(e.getBatchId())).append(",")
                    .append(escapeCsv(e.getTraineeId())).append(",")
                    .append(escapeCsv(e.getStatus().name())).append(",")
                    .append(escapeCsv(e.getPaymentReference())).append(",")
                    .append(e.getPriceAmount()).append(",")
                    .append(escapeCsv(e.getCurrency())).append(",")
                    .append(e.getCreatedAt()).append("\n");
        }

        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportAuditLogsCsv() {
        log.info("Generating CSV export for training audit logs");
        StringBuilder csv = new StringBuilder();
        csv.append("AuditID,EnrollmentID,FromStatus,ToStatus,Reason,Actor,Timestamp\n");

        List<TrainingAuditService.AuditLogItem> logs = auditService.getAuditLogs(null, null);
        for (TrainingAuditService.AuditLogItem logItem : logs) {
            csv.append(escapeCsv(logItem.id())).append(",")
                    .append(escapeCsv(logItem.enrollmentId())).append(",")
                    .append(escapeCsv(logItem.fromStatus())).append(",")
                    .append(escapeCsv(logItem.toStatus())).append(",")
                    .append(escapeCsv(logItem.reason())).append(",")
                    .append(escapeCsv(logItem.actor())).append(",")
                    .append(escapeCsv(logItem.timestamp())).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
