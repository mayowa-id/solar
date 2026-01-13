package com.example.solar.quote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteComparisonDto {
    private Long jobId;
    private String jobTitle;
    private List<QuoteDto> quotes;
    private BigDecimal lowestAmount;
    private BigDecimal highestAmount;
    private BigDecimal averageAmount;
    private Integer totalQuotes;
}

