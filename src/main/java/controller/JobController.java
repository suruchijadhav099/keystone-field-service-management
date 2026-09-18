package com.zidio.keystone.controller;

import com.zidio.keystone.domain.Job;
import com.zidio.keystone.domain.JobStatus;
import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.service.JobService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    public JobController(
            JobService jobService,
            UserRepository userRepository) {

        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CREATE JOB
    // ==========================================

    @PostMapping
    public ResponseEntity<Job> createJob(
            @RequestBody Job job,
            Authentication authentication) {

        User customer = getLoggedInUser(authentication);

        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException(
                    "Only customers can create jobs"
            );
        }

        Job createdJob =
                jobService.createJob(job, customer);

        return ResponseEntity.ok(createdJob);
    }

    // ==========================================
    // GET ALL JOBS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs(
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can view all jobs"
            );
        }

        return ResponseEntity.ok(
                jobService.getAllJobs()
        );
    }

    // ==========================================
    // GET MY JOBS
    // ==========================================

    @GetMapping("/my")
    public ResponseEntity<List<Job>> getMyJobs(
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            return ResponseEntity.ok(
                    jobService.getCustomerJobs(loggedInUser)
            );
        }

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            return ResponseEntity.ok(
                    jobService.getTechnicianJobs(loggedInUser)
            );
        }

        throw new RuntimeException(
                "Only customers and technicians can access their jobs"
        );
    }

    // ==========================================
    // GET JOB BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        Job job = jobService.getJobById(id);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            if (job.getCustomer() == null ||
                    !job.getCustomer().getId()
                            .equals(loggedInUser.getId())) {

                throw new RuntimeException(
                        "Customer can only access their own jobs"
                );
            }
        }

        if (loggedInUser.getRole() == Role.TECHNICIAN) {

            if (job.getTechnician() == null ||
                    !job.getTechnician().getId()
                            .equals(loggedInUser.getId())) {

                throw new RuntimeException(
                        "Technician can only access assigned jobs"
                );
            }
        }

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER &&
                loggedInUser.getRole() != Role.CUSTOMER &&
                loggedInUser.getRole() != Role.TECHNICIAN) {

            throw new RuntimeException(
                    "You are not allowed to access this job"
            );
        }

        return ResponseEntity.ok(job);
    }

    // ==========================================
    // GET JOBS BY STATUS
    // ==========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(
            @PathVariable JobStatus status,
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can filter jobs by status"
            );
        }

        return ResponseEntity.ok(
                jobService.getJobsByStatus(status)
        );
    }

    // ==========================================
    // ASSIGN TECHNICIAN
    // ==========================================

    @PutMapping("/{jobId}/assign/{technicianId}")
    public ResponseEntity<Job> assignTechnician(
            @PathVariable Long jobId,
            @PathVariable Long technicianId,
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

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

        Job updatedJob =
                jobService.assignTechnician(
                        jobId,
                        technician
                );

        return ResponseEntity.ok(updatedJob);
    }

    // ==========================================
    // UPDATE JOB
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(
            @PathVariable Long id,
            @RequestBody Job job,
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can update jobs"
            );
        }

        Job existingJob =
                jobService.getJobById(id);

        existingJob.setTitle(job.getTitle());
        existingJob.setDescription(job.getDescription());
        existingJob.setStatus(job.getStatus());
        existingJob.setScheduledAt(job.getScheduledAt());

        Job updatedJob =
                jobService.updateJob(existingJob);

        return ResponseEntity.ok(updatedJob);
    }

    // ==========================================
    // DELETE JOB
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser =
                getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can delete jobs"
            );
        }

        jobService.deleteJob(id);

        return ResponseEntity.ok(
                "Job deleted successfully"
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
                        ));
    }
}