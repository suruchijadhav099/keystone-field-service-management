package com.zidio.keystone.service;

import com.zidio.keystone.domain.TimeLog;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.repository.TimeLogRepository;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    public TimeLogService(
            TimeLogRepository timeLogRepository,
            WorkOrderRepository workOrderRepository,
            UserRepository userRepository) {

        this.timeLogRepository = timeLogRepository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
    }

    // Create Time Log
    @Transactional
    public TimeLog createTimeLog(TimeLog timeLog) {

        if (timeLog.getMinutes() == null ||
                timeLog.getMinutes() <= 0) {

            throw new RuntimeException(
                    "Minutes must be greater than zero"
            );
        }

        if (timeLog.getWorkOrder() == null ||
                timeLog.getWorkOrder().getId() == null) {

            throw new RuntimeException(
                    "Work Order ID is required"
            );
        }

        if (timeLog.getTechnician() == null ||
                timeLog.getTechnician().getId() == null) {

            throw new RuntimeException(
                    "Technician ID is required"
            );
        }

        WorkOrder workOrder =
                workOrderRepository.findById(
                        timeLog.getWorkOrder().getId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Work Order not found"
                        )
                );

        User technician =
                userRepository.findById(
                        timeLog.getTechnician().getId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Technician not found"
                        )
                );

        // Only the assigned technician can log time
        if (workOrder.getAssignedTo() == null ||
                !workOrder.getAssignedTo()
                        .getId()
                        .equals(technician.getId())) {

            throw new RuntimeException(
                    "Technician is not assigned to this work order"
            );
        }

        timeLog.setWorkOrder(workOrder);
        timeLog.setTechnician(technician);

        return timeLogRepository.save(timeLog);
    }

    // Get All Time Logs
    public List<TimeLog> getAllTimeLogs() {
        return timeLogRepository.findAll();
    }

    // Get Time Log By ID
    public TimeLog getTimeLogById(Long id) {

        return timeLogRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Time log not found"
                        )
                );
    }

    // Get Time Logs By Technician
    public List<TimeLog> getTimeLogsByTechnician(
            User technician) {

        return timeLogRepository.findByTechnician(technician);
    }

    // Get Time Logs By Work Order
    public List<TimeLog> getTimeLogsByWorkOrder(
            WorkOrder workOrder) {

        return timeLogRepository.findByWorkOrder(workOrder);
    }

    // Update Time Log
    @Transactional
    public TimeLog updateTimeLog(
            Long id,
            TimeLog timeLog) {

        if (timeLog.getMinutes() == null ||
                timeLog.getMinutes() <= 0) {

            throw new RuntimeException(
                    "Minutes must be greater than zero"
            );
        }

        TimeLog existingLog = getTimeLogById(id);

        existingLog.setMinutes(
                timeLog.getMinutes()
        );

        existingLog.setNote(
                timeLog.getNote()
        );

        return timeLogRepository.save(existingLog);
    }

    // Delete Time Log
    @Transactional
    public void deleteTimeLog(Long id) {

        if (!timeLogRepository.existsById(id)) {

            throw new RuntimeException(
                    "Time log not found"
            );
        }

        timeLogRepository.deleteById(id);
    }
}