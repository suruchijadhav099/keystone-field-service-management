package com.zidio.keystone.repository;

import com.zidio.keystone.domain.TimeLog;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    List<TimeLog> findByTechnician(User technician);

    List<TimeLog> findByWorkOrder(WorkOrder workOrder);
}