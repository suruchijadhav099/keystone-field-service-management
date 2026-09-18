package com.zidio.keystone.controller;

import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.TimeLog;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.repository.WorkOrderRepository;
import com.zidio.keystone.service.TimeLogService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-logs")
@SecurityRequirement(name = "bearerAuth")
public class TimeLogController {

    private final TimeLogService timeLogService;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;

    public TimeLogController(
            TimeLogService timeLogService,
            UserRepository userRepository,
            WorkOrderRepository workOrderRepository) {

        this.timeLogService = timeLogService;
        this.userRepository = userRepository;
        this.workOrderRepository = workOrderRepository;
    }

    // =========================================================
    // CREATE TIME LOG
    // =========================================================

    @PostMapping
    public ResponseEntity<TimeLog> createTimeLog(
            @RequestBody TimeLog timeLog,
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "Authentication is required");
        }

        User technician = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Logged-in user not found"));

        if (technician.getRole() != Role.TECHNICIAN) {

            throw new RuntimeException(
                    "Only technicians can create time logs");
        }

        if (timeLog == null ||
                timeLog.getWorkOrder() == null ||
                timeLog.getWorkOrder().getId() == null) {

            throw new RuntimeException(
                    "Work Order ID is required");
        }

        Long workOrderId =
                timeLog.getWorkOrder().getId();

        WorkOrder workOrder =
                workOrderRepository
                        .findById(workOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Work Order not found"));

        if (workOrder.getAssignedTo() == null) {

            throw new RuntimeException(
                    "This work order is not assigned to any technician");
        }

        if (!workOrder.getAssignedTo()
                .getId()
                .equals(technician.getId())) {

            throw new RuntimeException(
                    "Technician can only log time for assigned work orders");
        }

        timeLog.setTechnician(technician);
        timeLog.setWorkOrder(workOrder);

        if (timeLog.getMinutes() == null ||
                timeLog.getMinutes() <= 0) {

            throw new RuntimeException(
                    "Minutes must be greater than zero");
        }

        TimeLog savedTimeLog =
                timeLogService.createTimeLog(timeLog);

        return ResponseEntity.ok(savedTimeLog);
    }

    // =========================================================
    // GET ALL TIME LOGS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs(
            Authentication authentication) {

        User loggedInUser =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            throw new RuntimeException(
                    "Technicians can only access their own time logs");
        }

        return ResponseEntity.ok(
                timeLogService.getAllTimeLogs()
        );
    }

    // =========================================================
    // GET TIME LOG BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<TimeLog> getTimeLogById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                timeLogService.getTimeLogById(id)
        );
    }

    // =========================================================
    // GET MY TIME LOGS
    // =========================================================

    @GetMapping("/my")
    public ResponseEntity<List<TimeLog>> getMyTimeLogs(
            Authentication authentication) {

        User technician =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        return ResponseEntity.ok(
                timeLogService
                        .getTimeLogsByTechnician(technician)
        );
    }

    // =========================================================
    // GET TIME LOGS BY WORK ORDER
    // =========================================================

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<List<TimeLog>> getByWorkOrder(
            @PathVariable Long workOrderId) {

        WorkOrder workOrder =
                workOrderRepository
                        .findById(workOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Work Order not found"));

        return ResponseEntity.ok(
                timeLogService
                        .getTimeLogsByWorkOrder(workOrder)
        );
    }

    // =========================================================
    // UPDATE TIME LOG
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<TimeLog> updateTimeLog(
            @PathVariable Long id,
            @RequestBody TimeLog timeLog) {

        return ResponseEntity.ok(
                timeLogService.updateTimeLog(
                        id,
                        timeLog
                )
        );
    }

    // =========================================================
    // DELETE TIME LOG
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTimeLog(
            @PathVariable Long id) {

        timeLogService.deleteTimeLog(id);

        return ResponseEntity.ok(
                "Time log deleted successfully"
        );
    }
}