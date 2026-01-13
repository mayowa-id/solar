package com.example.solar.matching.dto;

import com.example.solar.matching.domain.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {
    private Long id;
    private Long jobId;
    private Long professionalId;
    private String professionalName;
    private String professionalEmail;
    private String professionalPhone;
    private BigDecimal professionalRating;
    private Integer professionalJobsCompleted;
    private BigDecimal matchScore;
    private MatchScoreBreakdown scoreBreakdown;
    private MatchStatus status;
    private LocalDateTime createdAt;
}