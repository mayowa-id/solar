package com.example.solar.job.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_requirements")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "panel_type", length = 100)
    private String panelType;

    @Column(name = "panel_capacity_kw", precision = 5, scale = 2)
    private BigDecimal panelCapacityKw;

    @Column(name = "battery_required")
    @Builder.Default
    private Boolean batteryRequired = false;

    @Column(name = "battery_capacity_kwh", precision = 5, scale = 2)
    private BigDecimal batteryCapacityKwh;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", length = 50)
    private PropertyType propertyType;

    @Column(name = "roof_type", length = 50)
    private String roofType;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}