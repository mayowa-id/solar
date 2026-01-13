package com.example.solar.professional.service;

import com.example.solar.common.exception.ResourceNotFoundException;
import com.example.solar.common.exception.ValidationException;
import com.example.solar.common.util.GeoUtils;
import com.example.solar.common.util.ValidationUtils;
import com.example.solar.professional.domain.AvailabilitySlot;
import com.example.solar.professional.domain.Professional;
import com.example.solar.professional.domain.ProfessionalExpertise;
import com.example.solar.professional.dto.*;
import com.example.solar.professional.repository.AvailabilitySlotRepository;
import com.example.solar.professional.repository.ProfessionalExpertiseRepository;
import com.example.solar.professional.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalExpertiseRepository expertiseRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    @Transactional
    public ProfessionalDto createProfessional(CreateProfessionalRequest request) {
        log.info("Creating professional with email: {}", request.getEmail());

        // Validate email
        if (!ValidationUtils.isValidEmail(request.getEmail())) {
            throw new ValidationException("Invalid email format");
        }

        // Check if email already exists
        if (professionalRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Professional with email " + request.getEmail() + " already exists");
        }

        // Check if userId already exists
        if (request.getUserId() != null && professionalRepository.existsByUserId(request.getUserId())) {
            throw new ValidationException("Professional with userId already exists");
        }

        // Validate coordinates if provided
        if (request.getLatitude() != null || request.getLongitude() != null) {
            if (!GeoUtils.isValidLatitude(request.getLatitude())) {
                throw new ValidationException("Invalid latitude value");
            }
            if (!GeoUtils.isValidLongitude(request.getLongitude())) {
                throw new ValidationException("Invalid longitude value");
            }
        }

        // Create professional entity
        Professional professional = Professional.builder()
                .userId(request.getUserId() != null ? request.getUserId() : UUID.randomUUID())
                .companyName(ValidationUtils.sanitize(request.getCompanyName()))
                .email(ValidationUtils.sanitize(request.getEmail().toLowerCase()))
                .phone(ValidationUtils.sanitize(request.getPhone()))
                .address(ValidationUtils.sanitize(request.getAddress()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .serviceRadiusKm(request.getServiceRadiusKm() != null ? request.getServiceRadiusKm() : 50)
                .hourlyRate(request.getHourlyRate())
                .yearsExperience(request.getYearsExperience())
                .build();

        Professional savedProfessional = professionalRepository.save(professional);
        log.info("Professional created successfully with ID: {}", savedProfessional.getId());

        return mapToDto(savedProfessional);
    }

    @Transactional(readOnly = true)
    public ProfessionalDto getProfessionalById(Long id) {
        log.info("Fetching professional with ID: {}", id);
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", "id", id));
        return mapToDto(professional);
    }

    @Transactional(readOnly = true)
    public ProfessionalDto getProfessionalByUserId(UUID userId) {
        log.info("Fetching professional with userId: {}", userId);
        Professional professional = professionalRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", "userId", userId));
        return mapToDto(professional);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalDto> getAllProfessionals() {
        log.info("Fetching all professionals");
        return professionalRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfessionalDto> getVerifiedProfessionals() {
        log.info("Fetching all verified professionals");
        return professionalRepository.findAllVerifiedProfessionals().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfessionalDto updateProfessional(Long id, CreateProfessionalRequest request) {
        log.info("Updating professional with ID: {}", id);

        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", "id", id));

        // Update fields
        if (ValidationUtils.isNotEmpty(request.getCompanyName())) {
            professional.setCompanyName(ValidationUtils.sanitize(request.getCompanyName()));
        }
        if (ValidationUtils.isNotEmpty(request.getPhone())) {
            professional.setPhone(ValidationUtils.sanitize(request.getPhone()));
        }
        if (ValidationUtils.isNotEmpty(request.getAddress())) {
            professional.setAddress(ValidationUtils.sanitize(request.getAddress()));
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            if (!GeoUtils.isValidLatitude(request.getLatitude())) {
                throw new ValidationException("Invalid latitude value");
            }
            if (!GeoUtils.isValidLongitude(request.getLongitude())) {
                throw new ValidationException("Invalid longitude value");
            }
            professional.setLatitude(request.getLatitude());
            professional.setLongitude(request.getLongitude());
        }
        if (request.getServiceRadiusKm() != null) {
            professional.setServiceRadiusKm(request.getServiceRadiusKm());
        }
        if (request.getHourlyRate() != null) {
            professional.setHourlyRate(request.getHourlyRate());
        }
        if (request.getYearsExperience() != null) {
            professional.setYearsExperience(request.getYearsExperience());
        }

        Professional updatedProfessional = professionalRepository.save(professional);
        log.info("Professional updated successfully with ID: {}", updatedProfessional.getId());

        return mapToDto(updatedProfessional);
    }

    @Transactional
    public ExpertiseDto addExpertise(Long professionalId, AddExpertiseRequest request) {
        log.info("Adding expertise to professional ID: {}", professionalId);

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", "id", professionalId));

        ProfessionalExpertise expertise = ProfessionalExpertise.builder()
                .professional(professional)
                .expertiseType(ValidationUtils.sanitize(request.getExpertiseType().toUpperCase()))
                .yearsExperience(request.getYearsExperience())
                .certificationName(ValidationUtils.sanitize(request.getCertificationName()))
                .build();

        ProfessionalExpertise savedExpertise = expertiseRepository.save(expertise);
        log.info("Expertise added successfully with ID: {}", savedExpertise.getId());

        return mapExpertiseToDto(savedExpertise);
    }

    @Transactional(readOnly = true)
    public List<ExpertiseDto> getProfessionalExpertise(Long professionalId) {
        log.info("Fetching expertise for professional ID: {}", professionalId);

        if (!professionalRepository.existsById(professionalId)) {
            throw new ResourceNotFoundException("Professional", "id", professionalId);
        }

        return expertiseRepository.findByProfessionalId(professionalId).stream()
                .map(this::mapExpertiseToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityDto addAvailability(Long professionalId, AddAvailabilityRequest request) {
        log.info("Adding availability for professional ID: {}", professionalId);

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", "id", professionalId));

        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new ValidationException("End time must be after start time");
        }

        AvailabilitySlot slot = AvailabilitySlot.builder()
                .professional(professional)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        AvailabilitySlot savedSlot = availabilitySlotRepository.save(slot);
        log.info("Availability slot added successfully with ID: {}", savedSlot.getId());

        return mapAvailabilityToDto(savedSlot);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> getProfessionalAvailability(Long professionalId, LocalDate date) {
        log.info("Fetching availability for professional ID: {} on date: {}", professionalId, date);

        if (!professionalRepository.existsById(professionalId)) {
            throw new ResourceNotFoundException("Professional", "id", professionalId);
        }

        List<AvailabilitySlot> slots;
        if (date != null) {
            slots = availabilitySlotRepository.findByProfessionalIdAndDate(professionalId, date);
        } else {
            slots = availabilitySlotRepository.findByProfessionalId(professionalId);
        }

        return slots.stream()
                .map(this::mapAvailabilityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> getAvailableSlots(Long professionalId, LocalDate date) {
        log.info("Fetching available slots for professional ID: {} on date: {}", professionalId, date);

        if (!professionalRepository.existsById(professionalId)) {
            throw new ResourceNotFoundException("Professional", "id", professionalId);
        }

        return availabilitySlotRepository.findAvailableSlotsByProfessionalAndDate(professionalId, date).stream()
                .map(this::mapAvailabilityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProfessional(Long id) {
        log.info("Deleting professional with ID: {}", id);

        if (!professionalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Professional", "id", id);
        }

        professionalRepository.deleteById(id);
        log.info("Professional deleted successfully with ID: {}", id);
    }

    private ProfessionalDto mapToDto(Professional professional) {
        return ProfessionalDto.builder()
                .id(professional.getId())
                .userId(professional.getUserId())
                .companyName(professional.getCompanyName())
                .email(professional.getEmail())
                .phone(professional.getPhone())
                .address(professional.getAddress())
                .latitude(professional.getLatitude())
                .longitude(professional.getLongitude())
                .serviceRadiusKm(professional.getServiceRadiusKm())
                .hourlyRate(professional.getHourlyRate())
                .yearsExperience(professional.getYearsExperience())
                .rating(professional.getRating())
                .totalJobsCompleted(professional.getTotalJobsCompleted())
                .isVerified(professional.getIsVerified())
                .expertiseList(professional.getExpertiseList().stream()
                        .map(this::mapExpertiseToDto)
                        .collect(Collectors.toList()))
                .createdAt(professional.getCreatedAt())
                .updatedAt(professional.getUpdatedAt())
                .build();
    }

    private ExpertiseDto mapExpertiseToDto(ProfessionalExpertise expertise) {
        return ExpertiseDto.builder()
                .id(expertise.getId())
                .expertiseType(expertise.getExpertiseType())
                .yearsExperience(expertise.getYearsExperience())
                .certificationName(expertise.getCertificationName())
                .build();
    }

    private AvailabilityDto mapAvailabilityToDto(AvailabilitySlot slot) {
        return AvailabilityDto.builder()
                .id(slot.getId())
                .date(slot.getDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.getIsBooked())
                .build();
    }
}