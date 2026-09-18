package com.zidio.keystone.repository;

import com.zidio.keystone.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findBySku(String sku);

    boolean existsBySku(String sku);
}
