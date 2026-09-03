package com.sporekart.modules.training.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.training.application.BatchApplicationService;
import com.sporekart.modules.training.controller.dto.BatchResponse;
import com.sporekart.modules.training.domain.TrainingBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batches")
public class PublicBatchController {

    private final BatchApplicationService batchService;

    public PublicBatchController(BatchApplicationService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BatchResponse>>> listPublicBatches(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.ASC, "startDate"));
        Page<TrainingBatch> batches = batchService.listPublicActiveBatches(pageable);
        Page<BatchResponse> responsePage = batches.map(BatchResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    @GetMapping("/{idOrCode}")
    public ResponseEntity<ApiResponse<BatchResponse>> getPublicBatch(@PathVariable("idOrCode") String idOrCode) {
        TrainingBatch batch = batchService.getPublicActiveBatch(idOrCode);
        return ResponseEntity.ok(ApiResponse.success(BatchResponse.fromDomain(batch)));
    }
}
