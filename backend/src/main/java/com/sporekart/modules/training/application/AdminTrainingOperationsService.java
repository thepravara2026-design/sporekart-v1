package com.sporekart.modules.training.application;

import com.sporekart.modules.training.controller.dto.AdminTrainingDashboardResponse;
import com.sporekart.modules.training.domain.BatchStatus;
import com.sporekart.modules.training.domain.DemandStatus;
import com.sporekart.modules.training.domain.EnrollmentStatus;
import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingEnrollment;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import com.sporekart.modules.training.domain.TrainingEnrollmentPayment;
import com.sporekart.modules.training.domain.TrainingPaymentStatus;
import com.sporekart.modules.training.domain.port.TrainingBatchRepository;
import com.sporekart.modules.training.domain.port.TrainingDemandRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.domain.port.TrainingEnrollmentRepository;
import com.sporekart.modules.training.domain.port.TrainingProgramRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingBatchRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingEnrollmentPaymentRepository;
import com.sporekart.modules.training.infrastructure.persistence.SpringDataTrainingProgramRepository;
import com.sporekart.modules.training.infrastructure.persistence.TrainingBatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminTrainingOperationsService {

    private final TrainingProgramRepository programRepository;
    private final TrainingBatchRepository batchRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingDemandRepository demandRepository;
    private final TrainingEnrollmentPaymentRepository paymentRepository;
    private final SpringDataTrainingProgramRepository springDataProgramRepository;
    private final SpringDataTrainingBatchRepository springDataBatchRepository;
    private final SpringDataTrainingEnrollmentPaymentRepository springDataPaymentRepository;

    public AdminTrainingOperationsService(
            TrainingProgramRepository programRepository,
            TrainingBatchRepository batchRepository,
            TrainingEnrollmentRepository enrollmentRepository,
            TrainingDemandRepository demandRepository,
            TrainingEnrollmentPaymentRepository paymentRepository,
            SpringDataTrainingProgramRepository springDataProgramRepository,
            SpringDataTrainingBatchRepository springDataBatchRepository,
            SpringDataTrainingEnrollmentPaymentRepository springDataPaymentRepository) {
        this.programRepository = programRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.demandRepository = demandRepository;
        this.paymentRepository = paymentRepository;
        this.springDataProgramRepository = springDataProgramRepository;
        this.springDataBatchRepository = springDataBatchRepository;
        this.springDataPaymentRepository = springDataPaymentRepository;
    }

    public AdminTrainingDashboardResponse getDashboardMetrics() {
        long activePrograms = springDataProgramRepository.count();
        List<TrainingBatchEntity> allBatches = springDataBatchRepository.findAll();
        
        long upcomingBatches = allBatches.stream()
                .filter(b -> b.getStatus() == BatchStatus.PLANNED || b.getStatus() == BatchStatus.SCHEDULED || b.getStatus() == BatchStatus.ACTIVE)
                .count();

        long totalCapacity = allBatches.stream()
                .filter(b -> b.getStatus() != BatchStatus.CANCELLED)
                .mapToLong(TrainingBatchEntity::getTotalCapacity)
                .sum();

        long totalOccupied = allBatches.stream()
                .filter(b -> b.getStatus() != BatchStatus.CANCELLED)
                .mapToLong(TrainingBatchEntity::getOccupiedSeats)
                .sum();

        long remainingSeats = Math.max(0, totalCapacity - totalOccupied);

        long activeDemand = demandRepository.countByStatus(DemandStatus.ACTIVE);
        long totalEnrollments = enrollmentRepository.searchEnrollments(null, null, null, Pageable.unpaged()).getTotalElements();
        long paymentPending = enrollmentRepository.countByStatus(EnrollmentStatus.PAYMENT_PENDING);
        long paymentFailed = enrollmentRepository.countByStatus(EnrollmentStatus.REJECTED);

        // Payment verified but enrollment not confirmed exception count
        long paymentVerifiedExceptions = springDataPaymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == TrainingPaymentStatus.VERIFIED || p.getStatus() == TrainingPaymentStatus.ENROLLMENT_PENDING)
                .filter(p -> {
                    if (p.getEnrollmentId() == null) return true;
                    return enrollmentRepository.findById(p.getEnrollmentId())
                            .map(e -> e.getStatus() != EnrollmentStatus.CONFIRMED && e.getStatus() != EnrollmentStatus.ACTIVE && e.getStatus() != EnrollmentStatus.COMPLETED)
                            .orElse(true);
                })
                .count();

        long batchesApproachingFull = allBatches.stream()
                .filter(b -> b.getStatus() != BatchStatus.CANCELLED && b.getStatus() != BatchStatus.COMPLETED)
                .filter(b -> b.getTotalCapacity() > 0 && ((double) b.getOccupiedSeats() / b.getTotalCapacity()) >= 0.8)
                .count();

        return new AdminTrainingDashboardResponse(
                activePrograms,
                upcomingBatches,
                totalCapacity,
                totalOccupied,
                remainingSeats,
                activeDemand,
                totalEnrollments,
                paymentPending,
                paymentFailed,
                paymentVerifiedExceptions,
                batchesApproachingFull
        );
    }

    public Page<TrainingEnrollment> searchEnrollments(String batchId, EnrollmentStatus status, String search, Pageable pageable) {
        return enrollmentRepository.searchEnrollments(batchId, status, search, pageable);
    }

    public Page<TrainingDemandRequest> searchDemands(String batchId, DemandStatus status, String search, Pageable pageable) {
        return demandRepository.searchDemands(batchId, status, search, pageable);
    }

    public Page<TrainingEnrollmentPayment> getPaymentVerifiedExceptions(Pageable pageable) {
        List<TrainingEnrollmentPayment> exceptions = springDataPaymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == TrainingPaymentStatus.VERIFIED || p.getStatus() == TrainingPaymentStatus.ENROLLMENT_PENDING)
                .filter(p -> {
                    if (p.getEnrollmentId() == null) return true;
                    return enrollmentRepository.findById(p.getEnrollmentId())
                            .map(e -> e.getStatus() != EnrollmentStatus.CONFIRMED && e.getStatus() != EnrollmentStatus.ACTIVE && e.getStatus() != EnrollmentStatus.COMPLETED)
                            .orElse(true);
                })
                .map(p -> paymentRepository.findById(p.getPaymentId()).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), exceptions.size());
        if (start > exceptions.size()) {
            return new PageImpl<>(List.of(), pageable, exceptions.size());
        }
        return new PageImpl<>(exceptions.subList(start, end), pageable, exceptions.size());
    }
}
