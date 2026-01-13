package com.example.solar.professional.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "professionals")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Professional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "service_radius_km")
    @Builder.Default
    private Integer serviceRadiusKm = 50;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_jobs_completed")
    @Builder.Default
    private Integer totalJobsCompleted = 0;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfessionalExpertise> expertiseList = new ArrayList<>();

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvailabilitySlot> availabilitySlots = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addExpertise(ProfessionalExpertise expertise) {
        expertiseList.add(expertise);
        expertise.setProfessional(this);
    }

    public void removeExpertise(ProfessionalExpertise expertise) {
        expertiseList.remove(expertise);
        expertise.setProfessional(null);
    }

    public void addAvailabilitySlot(AvailabilitySlot slot) {
        availabilitySlots.add(slot);
        slot.setProfessional(this);
    }

    public void removeAvailabilitySlot(AvailabilitySlot slot) {
        availabilitySlots.remove(slot);
        slot.setProfessional(null);
    }
}