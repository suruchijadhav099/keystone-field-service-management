package com.zidio.keystone.repository;

import com.zidio.keystone.domain.Job;
import com.zidio.keystone.domain.JobStatus;
import com.zidio.keystone.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByCustomer(User customer);

    List<Job> findByTechnician(User technician);

    List<Job> findByStatus(JobStatus status);
}