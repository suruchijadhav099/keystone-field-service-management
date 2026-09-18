package com.zidio.keystone.controller;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.service.CustomerService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;

    public CustomerController(
            CustomerService customerService,
            UserRepository userRepository) {

        this.customerService = customerService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CREATE CUSTOMER
    // ==========================================

    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @RequestBody Customer customer,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can create customers"
            );
        }

        return ResponseEntity.ok(
                customerService.createCustomer(customer)
        );
    }

    // ==========================================
    // GET ALL CUSTOMERS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerService.getCustomerByEmail(
                            loggedInUser.getEmail()
                    );

            return ResponseEntity.ok(
                    List.of(customer)
            );
        }

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER &&
                loggedInUser.getRole() != Role.TECHNICIAN) {

            throw new RuntimeException(
                    "You are not allowed to view customers"
            );
        }

        return ResponseEntity.ok(
                customerService.getAllCustomers()
        );
    }

    // ==========================================
    // GET MY CUSTOMER PROFILE
    // ==========================================

    @GetMapping("/me")
    public ResponseEntity<Customer> getMyCustomer(
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.CUSTOMER) {

            throw new RuntimeException(
                    "Only customers can access this endpoint"
            );
        }

        return ResponseEntity.ok(
                customerService.getCustomerByEmail(
                        loggedInUser.getEmail()
                )
        );
    }

    // ==========================================
    // GET CUSTOMER BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer myCustomer =
                    customerService.getCustomerByEmail(
                            loggedInUser.getEmail()
                    );

            if (!myCustomer.getId().equals(id)) {

                throw new RuntimeException(
                        "Customer can only access their own data"
                );
            }
        }

        return ResponseEntity.ok(
                customerService.getCustomerById(id)
        );
    }

    // ==========================================
    // GET CUSTOMER BY EMAIL
    // ==========================================

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(
            @PathVariable String email,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER &&
                !loggedInUser.getEmail()
                        .equalsIgnoreCase(email)) {

            throw new RuntimeException(
                    "Customer can only access their own data"
            );
        }

        return ResponseEntity.ok(
                customerService.getCustomerByEmail(email)
        );
    }

    // ==========================================
    // UPDATE CUSTOMER
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() == Role.CUSTOMER) {

            Customer myCustomer =
                    customerService.getCustomerByEmail(
                            loggedInUser.getEmail()
                    );

            if (!myCustomer.getId().equals(id)) {

                throw new RuntimeException(
                        "Customer can only update their own data"
                );
            }
        }

        if (loggedInUser.getRole() != Role.CUSTOMER &&
                loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "You are not allowed to update customers"
            );
        }

        return ResponseEntity.ok(
                customerService.updateCustomer(id, customer)
        );
    }

    // ==========================================
    // DELETE CUSTOMER
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(
            @PathVariable Long id,
            Authentication authentication) {

        User loggedInUser = getLoggedInUser(authentication);

        if (loggedInUser.getRole() != Role.MANAGER &&
                loggedInUser.getRole() != Role.DISPATCHER) {

            throw new RuntimeException(
                    "Only manager or dispatcher can delete customers"
            );
        }

        customerService.deleteCustomer(id);

        return ResponseEntity.ok(
                "Customer deleted successfully"
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