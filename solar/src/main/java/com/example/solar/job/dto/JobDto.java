package com.example.solar.job.dto;

import com.example.solar.job.domain.JobStatus;
import com.example.solar.job.domain.JobType;
import com.example.solar.job.domain.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private JobType jobType;
    private String title;
    private String description;
    private JobStatus status;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate preferredDate;
    private UrgencyLevel urgencyLevel;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}