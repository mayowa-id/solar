package com.example.solar.professional.repository;

import com.example.solar.professional.domain.ProfessionalExpertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfessionalExpertiseRepository extends JpaRepository<ProfessionalExpertise, Long> {
    List<ProfessionalExpertise> findByProfessionalId(Long professionalId);
    List<ProfessionalExpertise> findByExpertiseType(String expertiseType);
}