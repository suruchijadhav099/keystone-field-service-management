package com.zidio.keystone.repository;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByCustomer(Customer customer);
}