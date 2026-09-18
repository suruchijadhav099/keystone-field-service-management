package com.zidio.keystone.controller;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.Site;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.repository.CustomerRepository;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.service.SiteService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@SecurityRequirement(name = "bearerAuth")
public class SiteController {

    private final SiteService siteService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public SiteController(
            SiteService siteService,
            CustomerRepository customerRepository,
            UserRepository userRepository) {

        this.siteService = siteService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CREATE SITE
    // ==========================================

    @PostMapping
    public ResponseEntity<Site> createSite(
            @RequestBody Site site,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can create sites"
            );
        }

        if (site.getCustomer() == null ||
                site.getCustomer().getId() == null) {

            throw new RuntimeException(
                    "Customer ID is required"
            );
        }

        Customer customer = customerRepository
                .findById(site.getCustomer().getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found"
                        )
                );

        site.setCustomer(customer);

        return ResponseEntity.ok(
                siteService.createSite(site)
        );
    }

    // ==========================================
    // GET ALL SITES
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Site>> getAllSites(
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER &&
                loggedInUser.getRole() != Role.TECHNICIAN &&
                loggedInUser.getRole() != Role.CUSTOMER) {

            throw new RuntimeException(
                    "You are not allowed to view sites"
            );
        }

        return ResponseEntity.ok(
                siteService.getAllSites()
        );
    }

    // ==========================================
    // GET SITE BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Site> getSiteById(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER &&
                loggedInUser.getRole() != Role.TECHNICIAN &&
                loggedInUser.getRole() != Role.CUSTOMER) {

            throw new RuntimeException(
                    "You are not allowed to view sites"
            );
        }

        return ResponseEntity.ok(
                siteService.getSiteById(id)
        );
    }

    // ==========================================
    // GET SITES BY CUSTOMER
    // ==========================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Site>> getSitesByCustomer(
            @PathVariable Long customerId,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER &&
                loggedInUser.getRole() != Role.TECHNICIAN &&
                loggedInUser.getRole() != Role.CUSTOMER) {

            throw new RuntimeException(
                    "You are not allowed to view sites"
            );
        }

        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found"
                        )
                );

        return ResponseEntity.ok(
                siteService.getSitesByCustomer(customer)
        );
    }

    // ==========================================
    // UPDATE SITE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Site> updateSite(
            @PathVariable Long id,
            @RequestBody Site site,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can update sites"
            );
        }

        if (site.getCustomer() != null &&
                site.getCustomer().getId() != null) {

            Customer customer = customerRepository
                    .findById(site.getCustomer().getId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Customer not found"
                            )
                    );

            site.setCustomer(customer);
        }

        return ResponseEntity.ok(
                siteService.updateSite(id, site)
        );
    }

    // ==========================================
    // DELETE SITE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSite(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can delete sites"
            );
        }

        siteService.deleteSite(id);

        return ResponseEntity.ok(
                "Site deleted successfully"
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