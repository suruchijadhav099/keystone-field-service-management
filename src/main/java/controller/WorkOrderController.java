package com.zidio.keystone.controller;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.WorkOrderStatus;
import com.zidio.keystone.domain.WorkOrderStatusHistory;
import com.zidio.keystone.dto.WorkOrderStatusHistoryResponse;
import com.zidio.keystone.repository.CustomerRepository;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.repository.WorkOrderStatusHistoryRepository;
import com.zidio.keystone.service.WorkOrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.zidio.keystone.repository.WorkOrderRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-orders")
@SecurityRequirement(name = "bearerAuth")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final WorkOrderStatusHistoryRepository historyRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderController(
            WorkOrderService workOrderService,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            WorkOrderStatusHistoryRepository historyRepository,
            WorkOrderRepository workOrderRepository) {

        this.workOrderService = workOrderService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.workOrderRepository = workOrderRepository;
    }

    // ==========================================
    // CREATE WORK ORDER
    // ==========================================

    @PostMapping
    public ResponseEntity<WorkOrder> createWorkOrder(
            @RequestBody WorkOrder workOrder,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            workOrder.setCustomer(customer);
        }

        if (workOrder.getCustomer() == null ||
                workOrder.getCustomer().getId() == null) {

            throw new RuntimeException(
                    "Customer ID is required"
            );
        }

        if (workOrder.getSite() == null ||
                workOrder.getSite().getId() == null) {

            throw new RuntimeException(
                    "Site ID is required"
            );
        }

        Customer customer =
                customerRepository
                        .findById(workOrder.getCustomer().getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found"
                                ));

        if (loggedInUser.getRole() == Role.CUSTOMER &&
                !customer.getId().equals(
                        workOrder.getCustomer().getId()
                )) {

            throw new RuntimeException(
                    "Customer can only create requests for their own account"
            );
        }

        workOrder.setCustomer(customer);

        return ResponseEntity.ok(
                workOrderService.createWorkOrder(workOrder)
        );
    }

    // ==========================================
    // GET ALL WORK ORDERS
    // ==========================================

    @GetMapping
    public ResponseEntity<?> getAllWorkOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (page < 0) {
            throw new RuntimeException("Page must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new RuntimeException("Size must be between 1 and 100");
        }

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        // Customer and technician continue to receive only their permitted data.
        // Search/status filtering is applied to the paginated manager/dispatcher list.
        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            List<WorkOrder> ownOrders =
                    workOrderService.getWorkOrdersByCustomer(customer);

            return ResponseEntity.ok(ownOrders);
        }
        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can access all work orders"
            );
        }
        Page<WorkOrder> result = workOrderRepository.findAll(pageable);

        if (search != null && !search.trim().isEmpty()) {
            String term = search.trim().toLowerCase();
            List<WorkOrder> filtered = result.getContent().stream()
                    .filter(w ->
                            (w.getCode() != null && w.getCode().toLowerCase().contains(term)) ||
                                    (w.getTitle() != null && w.getTitle().toLowerCase().contains(term)) ||
                                    (w.getCustomer() != null && w.getCustomer().getName() != null &&
                                            w.getCustomer().getName().toLowerCase().contains(term))
                    )
                    .filter(w -> status == null || w.getStatus() == status)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "content", filtered,
                    "page", page,
                    "size", size,
                    "totalElements", filtered.size(),
                    "totalPages", filtered.isEmpty() ? 0 : 1,
                    "first", page == 0,
                    "last", true
            ));
        }

        List<WorkOrder> content = result.getContent();
        if (status != null) {
            content = content.stream()
                    .filter(w -> w.getStatus() == status)
                    .toList();
        }

        return ResponseEntity.ok(Map.of(
                "content", content,
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "first", result.isFirst(),
                "last", result.isLast()
        ));
    }

    // ==========================================
    // GET MY WORK ORDERS
    // ==========================================

    @GetMapping("/my")
    public ResponseEntity<List<WorkOrder>> getMyWorkOrders(
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            return ResponseEntity.ok(
                    workOrderService.getWorkOrdersByCustomer(customer)
            );
        }

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            return ResponseEntity.ok(
                    workOrderService.getWorkOrdersByTechnician(
                            loggedInUser
                    )
            );
        }

        throw new RuntimeException(
                "Only customers and technicians can access this endpoint"
        );
    }

    // ==========================================
    // GET WORK ORDER BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        WorkOrder workOrder =
                workOrderService.getWorkOrderById(id);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            if (!workOrder.getCustomer().getId()
                    .equals(customer.getId())) {

                throw new RuntimeException(
                        "Customer can only access their own work orders"
                );
            }
        }

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            if (workOrder.getAssignedTo() == null ||
                    !workOrder.getAssignedTo().getId()
                            .equals(loggedInUser.getId())) {

                throw new RuntimeException(
                        "Technician can only access assigned work orders"
                );
            }
        }

        return ResponseEntity.ok(workOrder);
    }

    // ==========================================
    // GET WORK ORDER BY CODE
    // ==========================================

    @GetMapping("/code/{code}")
    public ResponseEntity<WorkOrder> getWorkOrderByCode(
            @PathVariable String code,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        WorkOrder workOrder =
                workOrderService.getWorkOrderByCode(code);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            if (!workOrder.getCustomer().getId()
                    .equals(customer.getId())) {

                throw new RuntimeException(
                        "Customer can only access their own work orders"
                );
            }
        }

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            if (workOrder.getAssignedTo() == null ||
                    !workOrder.getAssignedTo().getId()
                            .equals(loggedInUser.getId())) {

                throw new RuntimeException(
                        "Technician can only access assigned work orders"
                );
            }
        }

        return ResponseEntity.ok(workOrder);
    }

    // ==========================================
    // GET WORK ORDERS BY CUSTOMER
    // ==========================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<WorkOrder>> getByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer myCustomer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            if (!myCustomer.getId().equals(customerId)) {

                throw new RuntimeException(
                        "Customer can only access their own work orders"
                );
            }
        }

        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found"
                                ));

        return ResponseEntity.ok(
                workOrderService.getWorkOrdersByCustomer(customer)
        );
    }

    // ==========================================
    // GET WORK ORDERS BY TECHNICIAN
    // ==========================================

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<WorkOrder>> getByTechnician(
            @PathVariable Long technicianId,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.TECHNICIAN &&
                !loggedInUser.getId().equals(technicianId)) {

            throw new RuntimeException(
                    "Technician can only access their own work orders"
            );
        }

        User technician =
                userRepository
                        .findById(technicianId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Technician not found"
                                ));

        if (technician.getRole() != Role.TECHNICIAN) {

            throw new RuntimeException(
                    "Selected user is not a technician"
            );
        }

        return ResponseEntity.ok(
                workOrderService.getWorkOrdersByTechnician(
                        technician
                )
        );
    }

    // ==========================================
    // GET WORK ORDERS BY STATUS
    // ==========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<WorkOrder>> getByStatus(
            @PathVariable WorkOrderStatus status,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER ||
                loggedInUser.getRole() == Role.TECHNICIAN) {

            throw new RuntimeException(
                    "You are not allowed to use status filter"
            );
        }

        return ResponseEntity.ok(
                workOrderService.getWorkOrdersByStatus(status)
        );
    }

    // ==========================================
    // GET WORK ORDER STATUS HISTORY
    // ==========================================

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getStatusHistory(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        WorkOrder workOrder =
                workOrderService.getWorkOrderById(id);

        // CUSTOMER can see only own work order history
        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            if (!workOrder.getCustomer().getId()
                    .equals(customer.getId())) {

                throw new RuntimeException(
                        "Customer can only access their own work order history"
                );
            }
        }

        // TECHNICIAN can see only assigned work order history
        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            if (workOrder.getAssignedTo() == null ||
                    !workOrder.getAssignedTo().getId()
                            .equals(loggedInUser.getId())) {

                throw new RuntimeException(
                        "Technician can only access assigned work order history"
                );
            }
        }

        List<WorkOrderStatusHistory> history =
                historyRepository
                        .findByWorkOrderOrderByChangedAtAsc(workOrder);

        List<WorkOrderStatusHistoryResponse> response =
                history.stream()
                        .map(h -> new WorkOrderStatusHistoryResponse(
                                h.getId(),

                                h.getFromStatus() != null
                                        ? h.getFromStatus().name()
                                        : null,

                                h.getToStatus().name(),

                                h.getChangedBy() != null
                                        ? h.getChangedBy().getName()
                                        : null,

                                h.getChangedAt(),

                                h.getNote()
                        ))
                        .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ASSIGN TECHNICIAN
    // ==========================================

    @PutMapping("/{id}/assign/{technicianId}")
    public ResponseEntity<WorkOrder> assignTechnician(
            @PathVariable Long id,
            @PathVariable Long technicianId,
            Authentication authentication) {

        User changedBy = getLoggedInUser(authentication);

        if (changedBy.getRole() != Role.MANAGER &&
                changedBy.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can assign technicians"
            );
        }

        User technician =
                userRepository
                        .findById(technicianId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Technician not found"
                                ));

        if (technician.getRole() != Role.TECHNICIAN) {

            throw new RuntimeException(
                    "Selected user is not a technician"
            );
        }

        return ResponseEntity.ok(
                workOrderService.assignTechnician(
                        id,
                        technician,
                        changedBy
                )
        );
    }

    // ==========================================
    // UPDATE STATUS
    // ==========================================

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @PathVariable WorkOrderStatus status,
            Authentication authentication) {

        User changedBy = getLoggedInUser(authentication);

        WorkOrder updatedWorkOrder =
                workOrderService.updateStatus(
                        id,
                        status,
                        changedBy
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Work order status updated successfully",

                        "id",
                        updatedWorkOrder.getId(),

                        "code",
                        updatedWorkOrder.getCode(),

                        "status",
                        updatedWorkOrder.getStatus().name()
                )
        );
    }

    // ==========================================
    // UPDATE WORK ORDER
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<WorkOrder> updateWorkOrder(
            @PathVariable Long id,
            @RequestBody WorkOrder workOrder,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            WorkOrder existing =
                    workOrderService.getWorkOrderById(id);

            Customer customer =
                    customerRepository
                            .findByContactEmail(loggedInUser.getEmail())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Customer profile not found"
                                    ));

            if (!existing.getCustomer().getId()
                    .equals(customer.getId())) {

                throw new RuntimeException(
                        "Customer can only update their own work orders"
                );
            }
        }

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            WorkOrder existing =
                    workOrderService.getWorkOrderById(id);

            if (existing.getAssignedTo() == null ||
                    !existing.getAssignedTo().getId()
                            .equals(loggedInUser.getId())) {

                throw new RuntimeException(
                        "Technician can only update assigned work orders"
                );
            }
        }

        return ResponseEntity.ok(
                workOrderService.updateWorkOrder(
                        id,
                        workOrder
                )
        );
    }

    // ==========================================
    // PHOTO UPLOAD
    // ==========================================

    @PostMapping(
            value = "/{id}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long id,
            @RequestPart("photo") MultipartFile photo,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.TECHNICIAN) {

            throw new RuntimeException(
                    "Only technicians can upload photos"
            );
        }

        WorkOrder workOrder =
                workOrderService.getWorkOrderById(id);

        if (workOrder.getAssignedTo() == null ||
                !workOrder.getAssignedTo()
                        .getId()
                        .equals(loggedInUser.getId())) {

            throw new RuntimeException(
                    "You can upload photos only to your assigned work order"
            );
        }

        if (photo == null || photo.isEmpty()) {

            throw new RuntimeException(
                    "Photo is required"
            );
        }

        String contentType = photo.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new RuntimeException(
                    "Only image files are allowed"
            );
        }

        try {

            Path uploadDirectory =
                    Paths.get(
                            System.getProperty("user.dir"),
                            "uploads",
                            "work-orders"
                    );

            Files.createDirectories(uploadDirectory);

            String originalName =
                    photo.getOriginalFilename();

            String extension = ".jpg";

            if (originalName != null &&
                    originalName.contains(".")) {

                extension =
                        originalName.substring(
                                originalName.lastIndexOf(".")
                        );
            }

            String fileName =
                    UUID.randomUUID() + extension;

            Path filePath =
                    uploadDirectory.resolve(fileName);

            Files.copy(
                    photo.getInputStream(),
                    filePath
            );

            String photoUrl =
                    "/uploads/work-orders/" + fileName;

            WorkOrder savedWorkOrder =
                    workOrderService.updatePhotoUrl(
                            id,
                            photoUrl
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Photo uploaded successfully",

                            "photoUrl",
                            photoUrl,

                            "workOrderId",
                            savedWorkOrder.getId()
                    )
            );

        } catch (IOException e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to upload photo: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // DELETE WORK ORDER
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWorkOrder(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can delete work orders"
            );
        }

        workOrderService.deleteWorkOrder(id);

        return ResponseEntity.ok(
                "Work Order deleted successfully"
        );
    }

    // ==========================================
    // GET LOGGED-IN USER
    // ==========================================

    private User getLoggedInUser(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "Authentication is required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Logged-in user not found"
                        )
                );
    }
}