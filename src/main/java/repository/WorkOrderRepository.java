package com.zidio.keystone.repository;

import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByCode(String code);

    List<WorkOrder> findByCustomer(Customer customer);

    List<WorkOrder> findByAssignedTo(User technician);

    List<WorkOrder> findByStatus(WorkOrderStatus status);

    boolean existsByCode(String code);

    Page<WorkOrder> findAll(Pageable pageable);

    @Query("""
        SELECT w FROM WorkOrder w
        LEFT JOIN w.customer c
        WHERE LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(w.title) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    Page<WorkOrder> searchWorkOrders(
            @Param("search") String search,
            Pageable pageable
    );
}