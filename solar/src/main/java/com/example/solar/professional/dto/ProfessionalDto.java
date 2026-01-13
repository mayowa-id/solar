package com.example.solar.professional.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalDto {
    private Long id;
    private UUID userId;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer serviceRadiusKm;
    private BigDecimal hourlyRate;
    private Integer yearsExperience;
    private BigDecimal rating;
    private Integer totalJobsCompleted;
    private Boolean isVerified;
    private List<ExpertiseDto> expertiseList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

