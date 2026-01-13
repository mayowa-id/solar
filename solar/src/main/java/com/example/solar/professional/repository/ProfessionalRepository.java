package com.example.solar.professional.repository;

import com.example.solar.professional.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    Optional<Professional> findByUserId(UUID userId);
    Optional<Professional> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserId(UUID userId);
    List<Professional> findByIsVerified(Boolean isVerified);

    @Query("SELECT p FROM Professional p WHERE p.isVerified = true")
    List<Professional> findAllVerifiedProfessionals();
}
