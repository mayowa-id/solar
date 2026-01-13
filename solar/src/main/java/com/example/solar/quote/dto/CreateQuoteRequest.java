package com.example.solar.quote.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CreateQuoteRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Professional ID is required")
    private Long professionalId;

    private Long matchId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private BigDecimal amount;

    @Min(value = 0, message = "Estimated hours must be positive")
    private Integer estimatedHours;

    private BigDecimal materialsCost;
    private BigDecimal laborCost;
    private String details;
    private LocalDateTime validUntil;
}

