package com.zidio.keystone.service;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Create Customer
    public Customer createCustomer(Customer customer) {

        return customerRepository.save(customer);
    }

    // Get All Customers
    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    // Get Customer By ID
    public Customer getCustomerById(Long id) {

        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));
    }

    // Get Customer By Email
    public Customer getCustomerByEmail(String email) {

        return customerRepository.findByContactEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));
    }

    // Update Customer
    public Customer updateCustomer(Long id, Customer customer) {

        Customer existingCustomer = getCustomerById(id);

        existingCustomer.setName(customer.getName());
        existingCustomer.setContactEmail(customer.getContactEmail());

        return customerRepository.save(existingCustomer);
    }

    // Delete Customer
    public void deleteCustomer(Long id) {

        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }

        customerRepository.deleteById(id);
    }
}