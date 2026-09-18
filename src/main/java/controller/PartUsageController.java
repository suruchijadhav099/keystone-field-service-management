package com.zidio.keystone.controller;

import com.zidio.keystone.domain.PartUsage;
import com.zidio.keystone.service.PartUsageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/part-usage")
@SecurityRequirement(name = "bearerAuth")
public class PartUsageController {

    private final PartUsageService partUsageService;

    public PartUsageController(PartUsageService partUsageService) {
        this.partUsageService = partUsageService;
    }

    // Add part usage to a work order
    @PostMapping("/work-order/{workOrderId}/part/{partId}")
    public ResponseEntity<PartUsage> addPartUsage(
            @PathVariable Long workOrderId,
            @PathVariable Long partId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                partUsageService.addPartUsage(
                        workOrderId,
                        partId,
                        quantity
                )
        );
    }

    // Get parts used for one work order
    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<List<PartUsage>> getByWorkOrder(
            @PathVariable Long workOrderId) {

        return ResponseEntity.ok(
                partUsageService.getByWorkOrder(workOrderId)
        );
    }

    // Get all part usage records
    @GetMapping
    public ResponseEntity<List<PartUsage>> getAllPartUsage() {

        return ResponseEntity.ok(
                partUsageService.getAllPartUsage()
        );
    }
}