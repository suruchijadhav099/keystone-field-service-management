package com.zidio.keystone.service;

import com.zidio.keystone.domain.Part;
import com.zidio.keystone.repository.PartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartService {

    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    // Create Part
    public Part createPart(Part part) {

        if (part.getStockQty() == null || part.getStockQty() < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }

        return partRepository.save(part);
    }

    // Get All Parts
    public List<Part> getAllParts() {
        return partRepository.findAll();
    }

    // Get Part By ID
    public Part getPartById(Long id) {

        return partRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Part not found"));
    }

    // Get Part By SKU
    public Part getPartBySku(String sku) {

        return partRepository.findBySku(sku)
                .orElseThrow(() ->
                        new RuntimeException("Part not found"));
    }

    // Update Part
    public Part updatePart(Long id, Part part) {

        Part existingPart = getPartById(id);

        existingPart.setName(part.getName());
        existingPart.setSku(part.getSku());
        existingPart.setUnitCost(part.getUnitCost());
        existingPart.setStockQty(part.getStockQty());

        return partRepository.save(existingPart);
    }

    // Add Stock
    public Part addStock(Long id, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        Part part = getPartById(id);

        part.setStockQty(
                part.getStockQty() + quantity
        );

        return partRepository.save(part);
    }

    // Remove Stock
    public Part removeStock(Long id, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        Part part = getPartById(id);

        if (part.getStockQty() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock");
        }

        part.setStockQty(
                part.getStockQty() - quantity
        );

        return partRepository.save(part);
    }

    // Delete Part
    public void deletePart(Long id) {

        if (!partRepository.existsById(id)) {
            throw new RuntimeException("Part not found");
        }

        partRepository.deleteById(id);
    }
}