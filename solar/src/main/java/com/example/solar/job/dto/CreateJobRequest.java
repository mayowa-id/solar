package com.example.solar.job.dto;

import com.example.solar.job.domain.JobType;
import com.example.solar.job.domain.PropertyType;
import com.example.solar.job.domain.UrgencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private LocalDate preferredDate;
    private UrgencyLevel urgencyLevel;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;

    // Job requirements
    private String panelType;
    private BigDecimal panelCapacityKw;
    private Boolean batteryRequired;
    private BigDecimal batteryCapacityKwh;
    private PropertyType propertyType;
    private String roofType;
    private String additionalNotes;
}