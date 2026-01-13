package com.example.solar.professional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfessionalRequest {
    private UUID userId;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @Min(value = 1, message = "Service radius must be at least 1 km")
    private Integer serviceRadiusKm;

    private BigDecimal hourlyRate;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsExperience;
}