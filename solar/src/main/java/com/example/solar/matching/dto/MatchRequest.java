package com.example.solar.matching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    // Optional: Override default matching criteria
    private Double distanceWeight;
    private Double expertiseWeight;
    private Double availabilityWeight;
    private Double ratingWeight;
    private Double priceWeight;
    private Double minimumMatchScore;
    private Integer maxMatches;
    private Boolean verifiedOnly;
}