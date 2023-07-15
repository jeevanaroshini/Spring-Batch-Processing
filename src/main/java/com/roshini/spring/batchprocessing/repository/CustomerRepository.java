package com.roshini.spring.batchprocessing.repository;

import com.roshini.spring.batchprocessing.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
