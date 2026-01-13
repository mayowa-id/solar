package com.example.solar.professional.controller;

import com.example.solar.common.dto.ApiResponse;
import com.example.solar.professional.dto.*;
import com.example.solar.professional.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProfessionalDto>> createProfessional(
            @Valid @RequestBody CreateProfessionalRequest request) {
        ProfessionalDto professional = professionalService.createProfessional(request);
        return new ResponseEntity<>(
                ApiResponse.success("Professional created successfully", professional),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfessionalDto>> getProfessionalById(@PathVariable Long id) {
        ProfessionalDto professional = professionalService.getProfessionalById(id);
        return ResponseEntity.ok(ApiResponse.success(professional));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ProfessionalDto>> getProfessionalByUserId(@PathVariable UUID userId) {
        ProfessionalDto professional = professionalService.getProfessionalByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(professional));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProfessionalDto>>> getAllProfessionals() {
        List<ProfessionalDto> professionals = professionalService.getAllProfessionals();
        return ResponseEntity.ok(ApiResponse.success(professionals));
    }

    @GetMapping("/verified")
    public ResponseEntity<ApiResponse<List<ProfessionalDto>>> getVerifiedProfessionals() {
        List<ProfessionalDto> professionals = professionalService.getVerifiedProfessionals();
        return ResponseEntity.ok(ApiResponse.success(professionals));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfessionalDto>> updateProfessional(
            @PathVariable Long id,
            @Valid @RequestBody CreateProfessionalRequest request) {
        ProfessionalDto professional = professionalService.updateProfessional(id, request);
        return ResponseEntity.ok(ApiResponse.success("Professional updated successfully", professional));
    }

    @PostMapping("/{id}/expertise")
    public ResponseEntity<ApiResponse<ExpertiseDto>> addExpertise(
            @PathVariable Long id,
            @Valid @RequestBody AddExpertiseRequest request) {
        ExpertiseDto expertise = professionalService.addExpertise(id, request);
        return new ResponseEntity<>(
                ApiResponse.success("Expertise added successfully", expertise),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}/expertise")
    public ResponseEntity<ApiResponse<List<ExpertiseDto>>> getProfessionalExpertise(@PathVariable Long id) {
        List<ExpertiseDto> expertise = professionalService.getProfessionalExpertise(id);
        return ResponseEntity.ok(ApiResponse.success(expertise));
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<AvailabilityDto>> addAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AddAvailabilityRequest request) {
        AvailabilityDto availability = professionalService.addAvailability(id, request);
        return new ResponseEntity<>(
                ApiResponse.success("Availability added successfully", availability),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityDto>>> getProfessionalAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AvailabilityDto> availability = professionalService.getProfessionalAvailability(id, date);
        return ResponseEntity.ok(ApiResponse.success(availability));
    }

    @GetMapping("/{id}/availability/available")
    public ResponseEntity<ApiResponse<List<AvailabilityDto>>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AvailabilityDto> slots = professionalService.getAvailableSlots(id, date);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProfessional(@PathVariable Long id) {
        professionalService.deleteProfessional(id);
        return ResponseEntity.ok(ApiResponse.success("Professional deleted successfully", null));
    }
}