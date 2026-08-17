package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingProgramApplicationService;
import com.sporekart.modules.training.controller.dto.CreateTrainingProgramRequest;
import com.sporekart.modules.training.controller.dto.TrainingProgramResponse;
import com.sporekart.modules.training.controller.dto.UpdateTrainingProgramRequest;
import com.sporekart.modules.training.domain.ProgramStatus;
import com.sporekart.modules.training.domain.TrainingProgram;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/training-programs")
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminTrainingProgramController {

    private final TrainingProgramApplicationService programService;

    public AdminTrainingProgramController(TrainingProgramApplicationService programService) {
        this.programService = programService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrainingProgramResponse>> createProgram(@Valid @RequestBody CreateTrainingProgramRequest request) {
        String actorId = getAuthenticatedActor();
        TrainingProgram program = programService.createProgram(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getDurationHours(),
                request.getPriceAmount(),
                request.getCurrency(),
                actorId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(TrainingProgramResponse.fromDomain(program)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainingProgramResponse>> updateProgram(@PathVariable("id") String id,
                                                                               @Valid @RequestBody UpdateTrainingProgramRequest request) {
        String actorId = getAuthenticatedActor();
        TrainingProgram program = programService.updateProgram(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getDurationHours(),
                request.getPriceAmount(),
                request.getCurrency(),
                actorId
        );
        return ResponseEntity.ok(ApiResponse.success(TrainingProgramResponse.fromDomain(program)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<TrainingProgramResponse>> activateProgram(@PathVariable("id") String id) {
        String actorId = getAuthenticatedActor();
        TrainingProgram program = programService.activateProgram(id, actorId);
        return ResponseEntity.ok(ApiResponse.success(TrainingProgramResponse.fromDomain(program)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<TrainingProgramResponse>> deactivateProgram(@PathVariable("id") String id) {
        String actorId = getAuthenticatedActor();
        TrainingProgram program = programService.deactivateProgram(id, actorId);
        return ResponseEntity.ok(ApiResponse.success(TrainingProgramResponse.fromDomain(program)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainingProgramResponse>> getProgramById(@PathVariable("id") String id) {
        TrainingProgram program = programService.getProgramById(id);
        return ResponseEntity.ok(ApiResponse.success(TrainingProgramResponse.fromDomain(program)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TrainingProgramResponse>>> searchPrograms(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) ProgramStatus status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortParts[0]));

        Page<TrainingProgram> programs = programService.searchPrograms(search, status, category, pageable);
        Page<TrainingProgramResponse> responsePage = programs.map(TrainingProgramResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    private String getAuthenticatedActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ADMIN_USER";
    }
}
