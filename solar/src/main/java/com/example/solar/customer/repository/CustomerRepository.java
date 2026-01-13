package com.example.solar.customer.repository;

import com.example.solar.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(UUID userId);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUserId(UUID userId);
}