package com.example.solar.matching.domain;

import com.example.solar.job.domain.Job;
import com.example.solar.professional.domain.Professional;
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
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "professional_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Column(name = "match_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal matchScore;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "expertise_score", precision = 5, scale = 2)
    private BigDecimal expertiseScore;

    @Column(name = "availability_score", precision = 5, scale = 2)
    private BigDecimal availabilityScore;

    @Column(name = "rating_score", precision = 5, scale = 2)
    private BigDecimal ratingScore;

    @Column(name = "price_score", precision = 5, scale = 2)
    private BigDecimal priceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private MatchStatus status = MatchStatus.SUGGESTED;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
