package com.roshini.spring.batchprocessing.importcsv.repository;

import com.roshini.spring.batchprocessing.importcsv.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
