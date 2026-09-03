package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.TrainingProgramApplicationService;
import com.sporekart.modules.training.controller.dto.TrainingProgramResponse;
import com.sporekart.modules.training.domain.TrainingProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/training-programs")
public class PublicTrainingProgramController {

    private final TrainingProgramApplicationService programService;

    public PublicTrainingProgramController(TrainingProgramApplicationService programService) {
        this.programService = programService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TrainingProgramResponse>>> listActivePrograms(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TrainingProgram> programs = programService.listPublicActivePrograms(pageable);
        Page<TrainingProgramResponse> responsePage = programs.map(TrainingProgramResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<ApiResponse<TrainingProgramResponse>> getActiveProgram(@PathVariable("idOrSlug") String idOrSlug) {
        TrainingProgram program = programService.getPublicActiveProgram(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success(TrainingProgramResponse.fromDomain(program)));
    }
}
