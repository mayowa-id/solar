package com.example.solar.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreBreakdown {
    private BigDecimal distanceKm;
    private BigDecimal distanceScore;
    private BigDecimal expertiseScore;
    private BigDecimal availabilityScore;
    private BigDecimal ratingScore;
    private BigDecimal priceScore;
    private BigDecimal totalScore;

    // Reasons for score
    private String distanceReason;
    private String expertiseReason;
    private String availabilityReason;
    private String ratingReason;
    private String priceReason;
}