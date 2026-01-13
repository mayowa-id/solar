package com.example.solar.job.service;

import com.example.solar.common.exception.ResourceNotFoundException;
import com.example.solar.common.exception.ValidationException;
import com.example.solar.common.util.GeoUtils;
import com.example.solar.common.util.ValidationUtils;
import com.example.solar.customer.domain.Customer;
import com.example.solar.customer.repository.CustomerRepository;
import com.example.solar.job.domain.*;
import com.example.solar.job.dto.*;
import com.example.solar.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public JobDetailDto createJob(CreateJobRequest request) {
        log.info("Creating job for customer ID: {}", request.getCustomerId());

        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Validate coordinates
        if (!GeoUtils.isValidLatitude(request.getLatitude())) {
            throw new ValidationException("Invalid latitude value");
        }
        if (!GeoUtils.isValidLongitude(request.getLongitude())) {
            throw new ValidationException("Invalid longitude value");
        }

        // Validate budget range
        if (request.getBudgetMin() != null && request.getBudgetMax() != null) {
            if (request.getBudgetMin().compareTo(request.getBudgetMax()) > 0) {
                throw new ValidationException("Minimum budget cannot be greater than maximum budget");
            }
        }

        // Create job entity
        Job job = Job.builder()
                .customer(customer)
                .jobType(request.getJobType())
                .title(ValidationUtils.sanitize(request.getTitle()))
                .description(ValidationUtils.sanitize(request.getDescription()))
                .status(JobStatus.PENDING)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .preferredDate(request.getPreferredDate())
                .urgencyLevel(request.getUrgencyLevel() != null ? request.getUrgencyLevel() : UrgencyLevel.NORMAL)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .build();

        // Create job requirement if details provided
        if (hasRequirementDetails(request)) {
            JobRequirement requirement = JobRequirement.builder()
                    .job(job)
                    .panelType(ValidationUtils.sanitize(request.getPanelType()))
                    .panelCapacityKw(request.getPanelCapacityKw())
                    .batteryRequired(request.getBatteryRequired() != null ? request.getBatteryRequired() : false)
                    .batteryCapacityKwh(request.getBatteryCapacityKwh())
                    .propertyType(request.getPropertyType())
                    .roofType(ValidationUtils.sanitize(request.getRoofType()))
                    .additionalNotes(ValidationUtils.sanitize(request.getAdditionalNotes()))
                    .build();
            job.setRequirement(requirement);
        }

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getId());

        return mapToDetailDto(savedJob);
    }

    @Transactional(readOnly = true)
    public JobDetailDto getJobById(Long id) {
        log.info("Fetching job with ID: {}", id);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
        return mapToDetailDto(job);
    }

    @Transactional(readOnly = true)
    public List<JobDto> getAllJobs() {
        log.info("Fetching all jobs");
        return jobRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobDto> getJobsByCustomerId(Long customerId) {
        log.info("Fetching jobs for customer ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return jobRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobDto> getJobsByStatus(JobStatus status) {
        log.info("Fetching jobs with status: {}", status);
        return jobRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobDto> getOpenJobs() {
        log.info("Fetching all open jobs");
        return jobRepository.findOpenJobs().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobDto updateJobStatus(Long id, UpdateJobStatusRequest request) {
        log.info("Updating job status for ID: {} to {}", id, request.getStatus());

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        job.setStatus(request.getStatus());
        Job updatedJob = jobRepository.save(job);

        log.info("Job status updated successfully for ID: {}", id);
        return mapToDto(updatedJob);
    }

    @Transactional
    public void deleteJob(Long id) {
        log.info("Deleting job with ID: {}", id);

        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job", "id", id);
        }

        jobRepository.deleteById(id);
        log.info("Job deleted successfully with ID: {}", id);
    }

    private boolean hasRequirementDetails(CreateJobRequest request) {
        return request.getPanelType() != null ||
                request.getPanelCapacityKw() != null ||
                request.getBatteryRequired() != null ||
                request.getBatteryCapacityKwh() != null ||
                request.getPropertyType() != null ||
                request.getRoofType() != null ||
                request.getAdditionalNotes() != null;
    }

    private JobDto mapToDto(Job job) {
        return JobDto.builder()
                .id(job.getId())
                .customerId(job.getCustomer().getId())
                .customerName(job.getCustomer().getName())
                .customerEmail(job.getCustomer().getEmail())
                .jobType(job.getJobType())
                .title(job.getTitle())
                .description(job.getDescription())
                .status(job.getStatus())
                .latitude(job.getLatitude())
                .longitude(job.getLongitude())
                .preferredDate(job.getPreferredDate())
                .urgencyLevel(job.getUrgencyLevel())
                .budgetMin(job.getBudgetMin())
                .budgetMax(job.getBudgetMax())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private JobDetailDto mapToDetailDto(Job job) {
        JobDto jobDto = mapToDto(job);

        JobRequirementDto requirementDto = null;
        if (job.getRequirement() != null) {
            requirementDto = JobRequirementDto.builder()
                    .id(job.getRequirement().getId())
                    .panelType(job.getRequirement().getPanelType())
                    .panelCapacityKw(job.getRequirement().getPanelCapacityKw())
                    .batteryRequired(job.getRequirement().getBatteryRequired())
                    .batteryCapacityKwh(job.getRequirement().getBatteryCapacityKwh())
                    .propertyType(job.getRequirement().getPropertyType())
                    .roofType(job.getRequirement().getRoofType())
                    .additionalNotes(job.getRequirement().getAdditionalNotes())
                    .build();
        }

        return JobDetailDto.builder()
                .job(jobDto)
                .requirement(requirementDto)
                .build();
    }
}
