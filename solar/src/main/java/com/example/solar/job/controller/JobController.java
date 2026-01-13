package com.example.solar.job.controller;

import com.example.solar.common.dto.ApiResponse;
import com.example.solar.job.domain.JobStatus;
import com.example.solar.job.dto.*;
import com.example.solar.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobDetailDto>> createJob(
            @Valid @RequestBody CreateJobRequest request) {
        JobDetailDto job = jobService.createJob(request);
        return new ResponseEntity<>(
                ApiResponse.success("Job created successfully", job),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDetailDto>> getJobById(@PathVariable Long id) {
        JobDetailDto job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobDto>>> getAllJobs(
            @RequestParam(required = false) JobStatus status) {
        List<JobDto> jobs;
        if (status != null) {
            jobs = jobService.getJobsByStatus(status);
        } else {
            jobs = jobService.getAllJobs();
        }
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<JobDto>>> getJobsByCustomerId(@PathVariable Long customerId) {
        List<JobDto> jobs = jobService.getJobsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<JobDto>>> getOpenJobs() {
        List<JobDto> jobs = jobService.getOpenJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobDto>> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request) {
        JobDto job = jobService.updateJobStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Job status updated successfully", job));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }
}