package com.example.solar.professional.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddExpertiseRequest {
    @NotBlank(message = "Expertise type is required")
    private String expertiseType;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsExperience;

    private String certificationName;
}