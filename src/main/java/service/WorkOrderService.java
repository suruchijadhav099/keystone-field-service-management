package com.zidio.keystone.service;

import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.WorkOrderStatus;
import com.zidio.keystone.domain.WorkOrderStatusHistory;
import com.zidio.keystone.repository.WorkOrderRepository;
import com.zidio.keystone.repository.WorkOrderStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusHistoryRepository historyRepository;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            WorkOrderStatusHistoryRepository historyRepository) {

        this.workOrderRepository = workOrderRepository;
        this.historyRepository = historyRepository;
    }

    // ==========================================
    // CREATE WORK ORDER
    // ==========================================

    @Transactional
    public WorkOrder createWorkOrder(WorkOrder workOrder) {

        if (workOrder.getStatus() == null) {
            workOrder.setStatus(WorkOrderStatus.NEW);
        }

        if (workOrder.getCreatedAt() == null) {
            workOrder.setCreatedAt(LocalDateTime.now());
        }

        WorkOrder saved = workOrderRepository.save(workOrder);

        saveHistory(
                saved,
                null,
                saved.getStatus(),
                null,
                "Work order created"
        );

        return saved;
    }

    // ==========================================
    // GET ALL WORK ORDERS
    // ==========================================

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    // ==========================================
    // GET WORK ORDER BY ID
    // ==========================================

    public WorkOrder getWorkOrderById(Long id) {

        return workOrderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Work Order not found")
                );
    }

    // ==========================================
    // GET WORK ORDER BY CODE
    // ==========================================

    public WorkOrder getWorkOrderByCode(String code) {

        return workOrderRepository.findByCode(code)
                .orElseThrow(() ->
                        new RuntimeException("Work Order not found")
                );
    }

    // ==========================================
    // GET BY CUSTOMER
    // ==========================================

    public List<WorkOrder> getWorkOrdersByCustomer(
            com.zidio.keystone.domain.Customer customer) {

        return workOrderRepository.findByCustomer(customer);
    }

    // ==========================================
    // GET BY TECHNICIAN
    // ==========================================

    public List<WorkOrder> getWorkOrdersByTechnician(User technician) {
        return workOrderRepository.findByAssignedTo(technician);
    }

    // ==========================================
    // GET BY STATUS
    // ==========================================

    public List<WorkOrder> getWorkOrdersByStatus(
            WorkOrderStatus status) {

        return workOrderRepository.findByStatus(status);
    }

    // ==========================================
    // ASSIGN TECHNICIAN
    // ==========================================

    @Transactional
    public WorkOrder assignTechnician(
            Long id,
            User technician,
            User changedBy) {

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new RuntimeException(
                    "Selected user is not a technician"
            );
        }

        WorkOrder workOrder = getWorkOrderById(id);

        WorkOrderStatus oldStatus = workOrder.getStatus();

        workOrder.setAssignedTo(technician);
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);

        WorkOrder saved = workOrderRepository.save(workOrder);

        saveHistory(
                saved,
                oldStatus,
                WorkOrderStatus.ASSIGNED,
                changedBy,
                "Technician assigned"
        );

        return saved;
    }

    // ==========================================
    // UPDATE STATUS
    // ==========================================

    @Transactional
    public WorkOrder updateStatus(
            Long id,
            WorkOrderStatus newStatus,
            User changedBy) {

        WorkOrder workOrder = getWorkOrderById(id);

        WorkOrderStatus currentStatus = workOrder.getStatus();

        if (currentStatus == newStatus) {
            throw new RuntimeException(
                    "Work order is already in " + newStatus + " status"
            );
        }

        if (currentStatus == WorkOrderStatus.CLOSED ||
                currentStatus == WorkOrderStatus.CANCELLED) {

            throw new RuntimeException(
                    "Closed or cancelled work orders cannot be changed"
            );
        }

        if (!isAllowedTransition(currentStatus, newStatus)) {

            throw new RuntimeException(
                    "Invalid status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }

        // Technician security
        if (changedBy.getRole() == Role.TECHNICIAN) {

            if (workOrder.getAssignedTo() == null) {
                throw new RuntimeException(
                        "This work order has no technician assigned"
                );
            }

            if (!workOrder.getAssignedTo()
                    .getId()
                    .equals(changedBy.getId())) {

                throw new RuntimeException(
                        "Technician can only update assigned work orders"
                );
            }

            if (!(newStatus == WorkOrderStatus.IN_PROGRESS ||
                    newStatus == WorkOrderStatus.ON_HOLD ||
                    newStatus == WorkOrderStatus.COMPLETED)) {

                throw new RuntimeException(
                        "Technician is not allowed to set this status"
                );
            }
        }

        // Customer cannot change status
        if (changedBy.getRole() == Role.CUSTOMER) {

            throw new RuntimeException(
                    "Customer cannot change work order status"
            );
        }

        workOrder.setStatus(newStatus);

        WorkOrder saved =
                workOrderRepository.save(workOrder);

        saveHistory(
                saved,
                currentStatus,
                newStatus,
                changedBy,
                "Status changed"
        );

        return saved;
    }

    // ==========================================
    // ALLOWED STATUS TRANSITIONS
    // ==========================================

    private boolean isAllowedTransition(
            WorkOrderStatus from,
            WorkOrderStatus to) {

        return switch (from) {

            case NEW ->
                    to == WorkOrderStatus.ASSIGNED ||
                            to == WorkOrderStatus.CANCELLED;

            case ASSIGNED ->
                    to == WorkOrderStatus.IN_PROGRESS ||
                            to == WorkOrderStatus.CANCELLED;

            case IN_PROGRESS ->
                    to == WorkOrderStatus.ON_HOLD ||
                            to == WorkOrderStatus.COMPLETED ||
                            to == WorkOrderStatus.CANCELLED;

            case ON_HOLD ->
                    to == WorkOrderStatus.IN_PROGRESS ||
                            to == WorkOrderStatus.CANCELLED;

            case COMPLETED ->
                    to == WorkOrderStatus.CLOSED;

            case CLOSED, CANCELLED ->
                    false;
        };
    }

    // ==========================================
    // SAVE STATUS HISTORY
    // ==========================================

    private void saveHistory(
            WorkOrder workOrder,
            WorkOrderStatus fromStatus,
            WorkOrderStatus toStatus,
            User changedBy,
            String note) {

        WorkOrderStatusHistory history =
                new WorkOrderStatusHistory();

        history.setWorkOrder(workOrder);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);

        historyRepository.save(history);
    }

    // ==========================================
    // UPDATE WORK ORDER
    // ==========================================

    @Transactional
    public WorkOrder updateWorkOrder(
            Long id,
            WorkOrder workOrder) {

        WorkOrder existing =
                getWorkOrderById(id);

        if (existing.getStatus() == WorkOrderStatus.CLOSED ||
                existing.getStatus() == WorkOrderStatus.CANCELLED) {

            throw new RuntimeException(
                    "Closed or cancelled work orders cannot be edited"
            );
        }

        if (workOrder.getTitle() != null) {
            existing.setTitle(workOrder.getTitle());
        }

        if (workOrder.getDescription() != null) {
            existing.setDescription(
                    workOrder.getDescription()
            );
        }

        if (workOrder.getPriority() != null) {
            existing.setPriority(
                    workOrder.getPriority()
            );
        }

        if (workOrder.getSlaDueAt() != null) {
            existing.setSlaDueAt(
                    workOrder.getSlaDueAt()
            );
        }

        if (workOrder.getPhotoUrl() != null) {
            existing.setPhotoUrl(
                    workOrder.getPhotoUrl()
            );
        }

        return workOrderRepository.save(existing);
    }

    // ==========================================
    // UPDATE PHOTO URL
    // ==========================================

    @Transactional
    public WorkOrder updatePhotoUrl(
            Long id,
            String photoUrl) {

        WorkOrder existing =
                getWorkOrderById(id);

        if (existing.getStatus() == WorkOrderStatus.CLOSED ||
                existing.getStatus() == WorkOrderStatus.CANCELLED) {

            throw new RuntimeException(
                    "Closed or cancelled work orders cannot be changed"
            );
        }

        existing.setPhotoUrl(photoUrl);

        return workOrderRepository.save(existing);
    }

    // ==========================================
    // DELETE WORK ORDER
    // ==========================================

    @Transactional
    public void deleteWorkOrder(Long id) {

        if (!workOrderRepository.existsById(id)) {

            throw new RuntimeException(
                    "Work Order not found"
            );
        }

        workOrderRepository.deleteById(id);
    }
}