package com.zidio.keystone.controller;

import com.zidio.keystone.domain.Part;
import com.zidio.keystone.service.PartService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parts")
@SecurityRequirement(name = "bearerAuth")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    // ==========================================
    // CREATE PART
    // ==========================================

    @PreAuthorize("hasAnyRole('MANAGER','DISPATCHER')")
    @PostMapping
    public ResponseEntity<Part> createPart(
            @RequestBody Part part) {

        return ResponseEntity.ok(
                partService.createPart(part)
        );
    }

    // ==========================================
    // GET ALL PARTS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Part>> getAllParts() {

        return ResponseEntity.ok(
                partService.getAllParts()
        );
    }

    // ==========================================
    // GET PART BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Part> getPartById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                partService.getPartById(id)
        );
    }

    // ==========================================
    // GET PART BY SKU
    // ==========================================

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Part> getPartBySku(
            @PathVariable String sku) {

        return ResponseEntity.ok(
                partService.getPartBySku(sku)
        );
    }

    // ==========================================
    // UPDATE PART
    // ==========================================

    @PreAuthorize("hasAnyRole('MANAGER','DISPATCHER')")
    @PutMapping("/{id}")
    public ResponseEntity<Part> updatePart(
            @PathVariable Long id,
            @RequestBody Part part) {

        return ResponseEntity.ok(
                partService.updatePart(id, part)
        );
    }

    // ==========================================
    // ADD STOCK
    // ==========================================

    @PreAuthorize("hasAnyRole('MANAGER','DISPATCHER')")
    @PutMapping("/{id}/add-stock")
    public ResponseEntity<Part> addStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        Integer quantity = request.get("quantity");

        return ResponseEntity.ok(
                partService.addStock(id, quantity)
        );
    }

    // ==========================================
    // REMOVE STOCK
    // ==========================================

    @PreAuthorize("hasAnyRole('MANAGER','DISPATCHER')")
    @PutMapping("/{id}/remove-stock")
    public ResponseEntity<Part> removeStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        Integer quantity = request.get("quantity");

        return ResponseEntity.ok(
                partService.removeStock(id, quantity)
        );
    }

    // ==========================================
    // DELETE PART
    // ==========================================

    @PreAuthorize("hasAnyRole('MANAGER','DISPATCHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePart(
            @PathVariable Long id) {

        partService.deletePart(id);

        return ResponseEntity.ok(
                "Part deleted successfully"
        );
    }
}