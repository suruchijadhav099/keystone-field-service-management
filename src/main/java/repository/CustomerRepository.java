package com.zidio.keystone.repository;

import com.zidio.keystone.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByContactEmail(String contactEmail);

    boolean existsByContactEmail(String contactEmail);
}
