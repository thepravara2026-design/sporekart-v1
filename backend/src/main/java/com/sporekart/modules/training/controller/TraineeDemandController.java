package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.security.domain.exception.AuthenticationFailedException;
import com.sporekart.modules.training.application.DemandApplicationService;
import com.sporekart.modules.training.controller.dto.DemandResponse;
import com.sporekart.modules.training.domain.TrainingDemandRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TraineeDemandController {

    private final DemandApplicationService demandService;

    public TraineeDemandController(DemandApplicationService demandService) {
        this.demandService = demandService;
    }

    private String getAuthenticatedTraineeId(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        throw new AuthenticationFailedException("Authentication required to access trainee demand resources");
    }

    @PostMapping("/batches/{batchId}/demand")
    public ResponseEntity<ApiResponse<DemandResponse>> createDemand(@PathVariable String batchId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String traineeId = getAuthenticatedTraineeId(auth);

        TrainingDemandRequest demand = demandService.createDemand(batchId, traineeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(DemandResponse.fromDomain(demand)));
    }

    @GetMapping("/me/demands")
    public ResponseEntity<ApiResponse<Page<DemandResponse>>> getMyDemands(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String traineeId = getAuthenticatedTraineeId(auth);

        Page<DemandResponse> responsePage = demandService.getTraineeDemands(traineeId, pageable)
                .map(DemandResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @DeleteMapping("/demands/{id}")
    public ResponseEntity<ApiResponse<DemandResponse>> withdrawDemand(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterId = getAuthenticatedTraineeId(auth);
        boolean isAdmin = auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        TrainingDemandRequest withdrawn = demandService.withdrawDemand(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(DemandResponse.fromDomain(withdrawn)));
    }
}
