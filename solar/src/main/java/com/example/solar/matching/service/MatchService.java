package com.example.solar.matching.service;

import com.example.solar.common.exception.ResourceNotFoundException;
import com.example.solar.common.exception.ValidationException;
import com.example.solar.job.domain.Job;
import com.example.solar.job.domain.JobStatus;
import com.example.solar.job.repository.JobRepository;
import com.example.solar.matching.domain.Match;
import com.example.solar.matching.domain.MatchCriteria;
import com.example.solar.matching.domain.MatchStatus;
import com.example.solar.matching.dto.MatchDto;
import com.example.solar.matching.dto.MatchRequest;
import com.example.solar.matching.dto.MatchScoreBreakdown;
import com.example.solar.matching.repository.MatchRepository;
import com.example.solar.professional.domain.Professional;
import com.example.solar.professional.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final JobRepository jobRepository;
    private final ProfessionalRepository professionalRepository;
    private final MatchingEngine matchingEngine;

    /**
     * Find and create matches for a job using the matching algorithm
     */
    @Transactional
    public List<MatchDto> findMatches(MatchRequest request) {
        log.info("Finding matches for job ID: {}", request.getJobId());

        // Validate job exists and is in correct status
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        if (job.getStatus() != JobStatus.PENDING && job.getStatus() != JobStatus.MATCHED) {
            throw new ValidationException("Cannot find matches for job with status: " + job.getStatus());
        }

        // Build matching criteria from request or use defaults
        MatchCriteria criteria = buildMatchCriteria(request);

        // Get all eligible professionals
        List<Professional> professionals;
        if (criteria.isVerifiedOnly()) {
            professionals = professionalRepository.findAllVerifiedProfessionals();
        } else {
            professionals = professionalRepository.findAll();
        }

        log.info("Evaluating {} professionals for job {}", professionals.size(), job.getId());

        // Calculate match scores for all professionals
        List<MatchWithScore> matchesWithScores = new ArrayList<>();

        for (Professional professional : professionals) {
            try {
                MatchScoreBreakdown scoreBreakdown = matchingEngine.calculateMatchScore(
                        professional, job, criteria);

                // Only include if meets minimum score threshold
                if (scoreBreakdown.getTotalScore().doubleValue() >= criteria.getMinimumMatchScore()) {
                    matchesWithScores.add(new MatchWithScore(professional, scoreBreakdown));
                }
            } catch (Exception e) {
                log.warn("Error calculating match score for professional {}: {}",
                        professional.getId(), e.getMessage());
            }
        }

        // Sort by match score (descending) and limit to maxMatches
        List<MatchWithScore> topMatches = matchesWithScores.stream()
                .sorted((a, b) -> b.scoreBreakdown.getTotalScore()
                        .compareTo(a.scoreBreakdown.getTotalScore()))
                .limit(criteria.getMaxMatches())
                .collect(Collectors.toList());

        log.info("Found {} matches above threshold for job {}", topMatches.size(), job.getId());

        // Save matches to database and return DTOs
        List<MatchDto> matchDtos = new ArrayList<>();

        for (MatchWithScore matchWithScore : topMatches) {
            try {
                // Check if match already exists
                if (matchRepository.existsByJobIdAndProfessionalId(
                        job.getId(), matchWithScore.professional.getId())) {
                    log.debug("Match already exists for job {} and professional {}",
                            job.getId(), matchWithScore.professional.getId());
                    continue;
                }

                // Create and save match
                Match match = Match.builder()
                        .job(job)
                        .professional(matchWithScore.professional)
                        .matchScore(matchWithScore.scoreBreakdown.getTotalScore())
                        .distanceKm(matchWithScore.scoreBreakdown.getDistanceKm())
                        .expertiseScore(matchWithScore.scoreBreakdown.getExpertiseScore())
                        .availabilityScore(matchWithScore.scoreBreakdown.getAvailabilityScore())
                        .ratingScore(matchWithScore.scoreBreakdown.getRatingScore())
                        .priceScore(matchWithScore.scoreBreakdown.getPriceScore())
                        .status(MatchStatus.SUGGESTED)
                        .build();

                Match savedMatch = matchRepository.save(match);
                matchDtos.add(mapToDto(savedMatch, matchWithScore.scoreBreakdown));

            } catch (Exception e) {
                log.error("Error saving match: {}", e.getMessage(), e);
            }
        }

        // Update job status to MATCHED if matches were found
        if (!matchDtos.isEmpty() && job.getStatus() == JobStatus.PENDING) {
            job.setStatus(JobStatus.MATCHED);
            jobRepository.save(job);
        }

        log.info("Created {} new matches for job {}", matchDtos.size(), job.getId());
        return matchDtos;
    }

    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByJobId(Long jobId) {
        log.info("Fetching matches for job ID: {}", jobId);

        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job", "id", jobId);
        }

        return matchRepository.findByJobIdOrderByMatchScoreDesc(jobId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByProfessionalId(Long professionalId) {
        log.info("Fetching matches for professional ID: {}", professionalId);

        if (!professionalRepository.existsById(professionalId)) {
            throw new ResourceNotFoundException("Professional", "id", professionalId);
        }

        return matchRepository.findByProfessionalId(professionalId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatchDto updateMatchStatus(Long matchId, MatchStatus newStatus) {
        log.info("Updating match {} status to {}", matchId, newStatus);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", matchId));

        match.setStatus(newStatus);
        Match updatedMatch = matchRepository.save(match);

        log.info("Match status updated successfully");
        return mapToDto(updatedMatch);
    }

    @Transactional
    public void deleteMatch(Long matchId) {
        log.info("Deleting match with ID: {}", matchId);

        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match", "id", matchId);
        }

        matchRepository.deleteById(matchId);
        log.info("Match deleted successfully");
    }

    // ==================== HELPER METHODS ====================

    private MatchCriteria buildMatchCriteria(MatchRequest request) {
        MatchCriteria.MatchCriteriaBuilder builder = MatchCriteria.builder();

        if (request.getDistanceWeight() != null) {
            builder.distanceWeight(request.getDistanceWeight());
        }
        if (request.getExpertiseWeight() != null) {
            builder.expertiseWeight(request.getExpertiseWeight());
        }
        if (request.getAvailabilityWeight() != null) {
            builder.availabilityWeight(request.getAvailabilityWeight());
        }
        if (request.getRatingWeight() != null) {
            builder.ratingWeight(request.getRatingWeight());
        }
        if (request.getPriceWeight() != null) {
            builder.priceWeight(request.getPriceWeight());
        }
        if (request.getMinimumMatchScore() != null) {
            builder.minimumMatchScore(request.getMinimumMatchScore());
        }
        if (request.getMaxMatches() != null) {
            builder.maxMatches(request.getMaxMatches());
        }
        if (request.getVerifiedOnly() != null) {
            builder.verifiedOnly(request.getVerifiedOnly());
        }

        return builder.build();
    }

    private MatchDto mapToDto(Match match) {
        return MatchDto.builder()
                .id(match.getId())
                .jobId(match.getJob().getId())
                .professionalId(match.getProfessional().getId())
                .professionalName(match.getProfessional().getCompanyName())
                .professionalEmail(match.getProfessional().getEmail())
                .professionalPhone(match.getProfessional().getPhone())
                .professionalRating(match.getProfessional().getRating())
                .professionalJobsCompleted(match.getProfessional().getTotalJobsCompleted())
                .matchScore(match.getMatchScore())
                .scoreBreakdown(MatchScoreBreakdown.builder()
                        .distanceKm(match.getDistanceKm())
                        .distanceScore(match.getDistanceKm() != null ?
                                BigDecimal.valueOf(100 - (match.getDistanceKm().doubleValue() * 2)) : null)
                        .expertiseScore(match.getExpertiseScore())
                        .availabilityScore(match.getAvailabilityScore())
                        .ratingScore(match.getRatingScore())
                        .priceScore(match.getPriceScore())
                        .totalScore(match.getMatchScore())
                        .build())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .build();
    }

    private MatchDto mapToDto(Match match, MatchScoreBreakdown scoreBreakdown) {
        return MatchDto.builder()
                .id(match.getId())
                .jobId(match.getJob().getId())
                .professionalId(match.getProfessional().getId())
                .professionalName(match.getProfessional().getCompanyName())
                .professionalEmail(match.getProfessional().getEmail())
                .professionalPhone(match.getProfessional().getPhone())
                .professionalRating(match.getProfessional().getRating())
                .professionalJobsCompleted(match.getProfessional().getTotalJobsCompleted())
                .matchScore(match.getMatchScore())
                .scoreBreakdown(scoreBreakdown)
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .build();
    }

    // Helper class to hold professional and score together during processing
    private static class MatchWithScore {
        Professional professional;
        MatchScoreBreakdown scoreBreakdown;

        MatchWithScore(Professional professional, MatchScoreBreakdown scoreBreakdown) {
            this.professional = professional;
            this.scoreBreakdown = scoreBreakdown;
        }
    }
}
