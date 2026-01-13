package com.example.solar.professional.repository;

import com.example.solar.professional.domain.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByProfessionalId(Long professionalId);

    List<AvailabilitySlot> findByProfessionalIdAndDate(Long professionalId, LocalDate date);

    @Query("SELECT a FROM AvailabilitySlot a WHERE a.professional.id = :professionalId " +
            "AND a.date = :date AND a.isBooked = false")
    List<AvailabilitySlot> findAvailableSlotsByProfessionalAndDate(
            @Param("professionalId") Long professionalId,
            @Param("date") LocalDate date
    );

    @Query("SELECT a FROM AvailabilitySlot a WHERE a.professional.id = :professionalId " +
            "AND a.date >= :startDate AND a.date <= :endDate AND a.isBooked = false")
    List<AvailabilitySlot> findAvailableSlotsByProfessionalAndDateRange(
            @Param("professionalId") Long professionalId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}