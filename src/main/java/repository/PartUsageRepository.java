package com.zidio.keystone.repository;

import com.zidio.keystone.domain.PartUsage;
import com.zidio.keystone.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartUsageRepository extends JpaRepository<PartUsage, Long> {

    List<PartUsage> findByWorkOrder(WorkOrder workOrder);
}