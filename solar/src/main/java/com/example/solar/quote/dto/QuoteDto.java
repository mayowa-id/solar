package com.example.solar.quote.dto;

import com.example.solar.quote.domain.QuoteStatus;
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
public class QuoteDto {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long professionalId;
    private String professionalName;
    private String professionalEmail;
    private String professionalPhone;
    private BigDecimal amount;
    private Integer estimatedHours;
    private BigDecimal materialsCost;
    private BigDecimal laborCost;
    private String details;
    private LocalDateTime validUntil;
    private QuoteStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}
