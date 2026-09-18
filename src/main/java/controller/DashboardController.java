package com.zidio.keystone.controller;

import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.WorkOrderStatus;
import com.zidio.keystone.repository.CustomerRepository;
import com.zidio.keystone.repository.PartRepository;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.repository.WorkOrderRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PartRepository partRepository;
    private final WorkOrderRepository workOrderRepository;

    public DashboardController(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PartRepository partRepository,
            WorkOrderRepository workOrderRepository) {

        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.partRepository = partRepository;
        this.workOrderRepository = workOrderRepository;
    }

    // ==========================================
    // DASHBOARD SUMMARY
    // ==========================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {

        List<WorkOrder> workOrders =
                workOrderRepository.findAll();

        Map<String, Object> dashboard =
                new LinkedHashMap<>();

        // ==========================================
        // TOTAL COUNTS
        // ==========================================

        dashboard.put(
                "totalUsers",
                userRepository.count()
        );

        dashboard.put(
                "totalCustomers",
                customerRepository.count()
        );

        dashboard.put(
                "totalParts",
                partRepository.count()
        );

        dashboard.put(
                "totalWorkOrders",
                workOrderRepository.count()
        );

        // ==========================================
        // WORK ORDER STATUS COUNTS
        // ==========================================

        dashboard.put(
                "new",
                countByStatus(workOrders, WorkOrderStatus.NEW)
        );

        dashboard.put(
                "assigned",
                countByStatus(workOrders, WorkOrderStatus.ASSIGNED)
        );

        dashboard.put(
                "inProgress",
                countByStatus(workOrders, WorkOrderStatus.IN_PROGRESS)
        );

        dashboard.put(
                "onHold",
                countByStatus(workOrders, WorkOrderStatus.ON_HOLD)
        );

        dashboard.put(
                "completed",
                countByStatus(workOrders, WorkOrderStatus.COMPLETED)
        );

        dashboard.put(
                "closed",
                countByStatus(workOrders, WorkOrderStatus.CLOSED)
        );

        dashboard.put(
                "cancelled",
                countByStatus(workOrders, WorkOrderStatus.CANCELLED)
        );

        // ==========================================
        // SLA
        // ==========================================

        int slaTracked = 0;
        int overdue = 0;
        int slaCompliant = 0;

        LocalDateTime now = LocalDateTime.now();

        for (WorkOrder workOrder : workOrders) {

            if (workOrder.getSlaDueAt() != null) {

                slaTracked++;

                if (workOrder.getStatus() != WorkOrderStatus.COMPLETED
                        && workOrder.getStatus() != WorkOrderStatus.CLOSED
                        && workOrder.getStatus() != WorkOrderStatus.CANCELLED
                        && workOrder.getSlaDueAt().isBefore(now)) {

                    overdue++;

                } else if (
                        workOrder.getStatus() == WorkOrderStatus.COMPLETED
                                || workOrder.getStatus() == WorkOrderStatus.CLOSED) {

                    slaCompliant++;
                }
            }
        }

        double slaCompliancePercentage =
                slaTracked == 0
                        ? 0
                        : (slaCompliant * 100.0) / slaTracked;

        dashboard.put(
                "slaTrackedWorkOrders",
                slaTracked
        );

        dashboard.put(
                "overdueWorkOrders",
                overdue
        );

        dashboard.put(
                "slaCompliantWorkOrders",
                slaCompliant
        );

        dashboard.put(
                "slaCompliancePercentage",
                Math.round(slaCompliancePercentage * 100.0) / 100.0
        );

        // ==========================================
        // TECHNICIAN BREAKDOWN
        // ==========================================

        Map<String, Integer> technicianBreakdown =
                new LinkedHashMap<>();

        for (WorkOrder workOrder : workOrders) {

            if (workOrder.getAssignedTo() != null) {

                String technicianName =
                        workOrder.getAssignedTo().getName();

                technicianBreakdown.put(
                        technicianName,
                        technicianBreakdown.getOrDefault(
                                technicianName,
                                0
                        ) + 1
                );
            }
        }

        dashboard.put(
                "technicianBreakdown",
                technicianBreakdown
        );

        // ==========================================
        // SITE BREAKDOWN
        // ==========================================

        Map<String, Integer> siteBreakdown =
                new LinkedHashMap<>();

        for (WorkOrder workOrder : workOrders) {

            if (workOrder.getSite() != null) {

                String siteName =
                        workOrder.getSite().getName();

                siteBreakdown.put(
                        siteName,
                        siteBreakdown.getOrDefault(
                                siteName,
                                0
                        ) + 1
                );
            }
        }

        dashboard.put(
                "siteBreakdown",
                siteBreakdown
        );

        return ResponseEntity.ok(dashboard);
    }

    // ==========================================
    // COUNT BY STATUS
    // ==========================================

    private long countByStatus(
            List<WorkOrder> workOrders,
            WorkOrderStatus status) {

        return workOrders.stream()
                .filter(workOrder ->
                        workOrder.getStatus() == status)
                .count();
    }
}