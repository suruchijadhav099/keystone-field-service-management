package com.zidio.keystone.service;

import com.zidio.keystone.domain.Job;
import com.zidio.keystone.domain.JobStatus;
import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Create Job
    public Job createJob(Job job, User customer) {

        job.setCustomer(customer);
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }

    // Get All Jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Get Job By ID
    public Job getJobById(Long id) {

        return jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));
    }

    // Get Customer Jobs
    public List<Job> getCustomerJobs(User customer) {
        return jobRepository.findByCustomer(customer);
    }

    // Get Technician Jobs
    public List<Job> getTechnicianJobs(User technician) {
        return jobRepository.findByTechnician(technician);
    }

    // Get Jobs By Status
    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    // Assign Technician
    public Job assignTechnician(Long jobId, User technician) {

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new RuntimeException(
                    "Selected user is not a technician"
            );
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        job.setTechnician(technician);
        job.setStatus(JobStatus.ASSIGNED);

        return jobRepository.save(job);
    }

    // Update Job
    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    // Delete Job
    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found");
        }

        jobRepository.deleteById(id);
    }
}