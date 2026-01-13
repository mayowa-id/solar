package com.example.solar.matching.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCriteria {
    // Scoring weights (must sum to 100)
    @Builder.Default
    private double distanceWeight = 30.0;

    @Builder.Default
    private double expertiseWeight = 25.0;

    @Builder.Default
    private double availabilityWeight = 20.0;

    @Builder.Default
    private double ratingWeight = 15.0;

    @Builder.Default
    private double priceWeight = 10.0;

    // Minimum match score to be considered (0-100)
    @Builder.Default
    private double minimumMatchScore = 50.0;

    // Maximum number of matches to return
    @Builder.Default
    private int maxMatches = 10;

    // Only include verified professionals
    @Builder.Default
    private boolean verifiedOnly = true;
}