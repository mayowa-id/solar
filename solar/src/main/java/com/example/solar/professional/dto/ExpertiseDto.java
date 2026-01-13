package com.example.solar.professional.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertiseDto {
    private Long id;
    private String expertiseType;
    private Integer yearsExperience;
    private String certificationName;
}