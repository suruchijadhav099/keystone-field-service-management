package com.zidio.keystone.service;

import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.WorkOrderStatus;
import com.zidio.keystone.repository.WorkOrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaService {

    private final WorkOrderRepository workOrderRepository;

    public SlaService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    // Check SLA every 5 minutes
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkSlaBreaches() {

        LocalDateTime now = LocalDateTime.now();

        List<WorkOrder> workOrders =
                workOrderRepository.findAll();

        for (WorkOrder workOrder : workOrders) {

            // Ignore completed, closed and cancelled work orders
            if (workOrder.getStatus() == WorkOrderStatus.COMPLETED ||
                    workOrder.getStatus() == WorkOrderStatus.CLOSED ||
                    workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
                continue;
            }

            // Check whether SLA is breached
            if (workOrder.getSlaDueAt() != null &&
                    workOrder.getSlaDueAt().isBefore(now)) {

                // SLA breach notification
                System.out.println(
                        "SLA BREACH NOTIFICATION: Work Order "
                                + workOrder.getCode()
                                + " has breached its SLA."
                );
            }
        }
    }
}