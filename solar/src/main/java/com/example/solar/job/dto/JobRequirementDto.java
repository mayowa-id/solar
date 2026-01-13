package com.example.solar.job.dto;

import com.example.solar.job.domain.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequirementDto {
    private Long id;
    private String panelType;
    private BigDecimal panelCapacityKw;
    private Boolean batteryRequired;
    private BigDecimal batteryCapacityKwh;
    private PropertyType propertyType;
    private String roofType;
    private String additionalNotes;
}
