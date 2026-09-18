package com.zidio.keystone.service;

import com.zidio.keystone.domain.Part;
import com.zidio.keystone.domain.PartUsage;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.repository.PartRepository;
import com.zidio.keystone.repository.PartUsageRepository;
import com.zidio.keystone.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartUsageService {

    private final PartUsageRepository partUsageRepository;
    private final PartRepository partRepository;
    private final WorkOrderRepository workOrderRepository;

    public PartUsageService(
            PartUsageRepository partUsageRepository,
            PartRepository partRepository,
            WorkOrderRepository workOrderRepository) {

        this.partUsageRepository = partUsageRepository;
        this.partRepository = partRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional
    public PartUsage addPartUsage(
            Long workOrderId,
            Long partId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }

        WorkOrder workOrder =
                workOrderRepository.findById(workOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Work Order not found"
                                )
                        );

        Part part =
                partRepository.findById(partId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Part not found"
                                )
                        );

        if (part.getStockQty() == null) {
            throw new RuntimeException(
                    "Part stock is not available"
            );
        }

        if (part.getStockQty() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock. Available stock: "
                            + part.getStockQty()
            );
        }

        // Decrease stock
        part.setStockQty(
                part.getStockQty() - quantity
        );

        partRepository.save(part);

        // Calculate total cost
        Double totalCost =
                part.getUnitCost() * quantity;

        // Create part usage
        PartUsage usage = new PartUsage();

        usage.setWorkOrder(workOrder);
        usage.setPart(part);

        // IMPORTANT:
        // Database has both qty_used and quantity columns.
        usage.setQtyUsed(quantity);
        usage.setQuantity(quantity);

        usage.setTotalCost(totalCost);

        return partUsageRepository.save(usage);
    }

    public List<PartUsage> getByWorkOrder(Long workOrderId) {

        WorkOrder workOrder =
                workOrderRepository.findById(workOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Work Order not found"
                                )
                        );

        return partUsageRepository.findByWorkOrder(workOrder);
    }

    public List<PartUsage> getAllPartUsage() {
        return partUsageRepository.findAll();
    }
}