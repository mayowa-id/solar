package com.example.solar.job.dto;

import com.example.solar.job.domain.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobStatusRequest {
    @NotNull(message = "Status is required")
    private JobStatus status;
}