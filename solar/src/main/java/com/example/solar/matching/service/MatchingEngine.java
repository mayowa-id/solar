package com.example.solar.matching.service;

import com.example.solar.common.util.GeoUtils;
import com.example.solar.job.domain.Job;
import com.example.solar.job.domain.JobType;
import com.example.solar.matching.domain.MatchCriteria;
import com.example.solar.matching.dto.MatchScoreBreakdown;
import com.example.solar.professional.domain.AvailabilitySlot;
import com.example.solar.professional.domain.Professional;
import com.example.solar.professional.domain.ProfessionalExpertise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/*
  Core matching logic that scores professionals against jobs
  basically  implements a weighted scoring algorithm across multiple dimensions
 */
@Component
@Slf4j
public class MatchingEngine {

    /*
     overall match score for a professional-job pairing
     */
    public MatchScoreBreakdown calculateMatchScore(Professional professional, Job job, MatchCriteria criteria) {
        log.debug("Calculating match score for Professional {} and Job {}",
                professional.getId(), job.getId());

        // Calculate individual dimension scores
        double distanceKm = calculateDistance(professional, job);
        double distanceScore = calculateDistanceScore(distanceKm, professional.getServiceRadiusKm());

        double expertiseScore = calculateExpertiseScore(professional, job);
        double availabilityScore = calculateAvailabilityScore(professional, job);
        double ratingScore = calculateRatingScore(professional);
        double priceScore = calculatePriceScore(professional, job);

        // Calculate weighted total score
        double totalScore = (distanceScore * criteria.getDistanceWeight() / 100.0) +
                (expertiseScore * criteria.getExpertiseWeight() / 100.0) +
                (availabilityScore * criteria.getAvailabilityWeight() / 100.0) +
                (ratingScore * criteria.getRatingWeight() / 100.0) +
                (priceScore * criteria.getPriceWeight() / 100.0);

        // Build breakdown with reasons
        return MatchScoreBreakdown.builder()
                .distanceKm(BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP))
                .distanceScore(BigDecimal.valueOf(distanceScore).setScale(2, RoundingMode.HALF_UP))
                .expertiseScore(BigDecimal.valueOf(expertiseScore).setScale(2, RoundingMode.HALF_UP))
                .availabilityScore(BigDecimal.valueOf(availabilityScore).setScale(2, RoundingMode.HALF_UP))
                .ratingScore(BigDecimal.valueOf(ratingScore).setScale(2, RoundingMode.HALF_UP))
                .priceScore(BigDecimal.valueOf(priceScore).setScale(2, RoundingMode.HALF_UP))
                .totalScore(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP))
                .distanceReason(generateDistanceReason(distanceKm, professional.getServiceRadiusKm()))
                .expertiseReason(generateExpertiseReason(professional, job))
                .availabilityReason(generateAvailabilityReason(professional, job))
                .ratingReason(generateRatingReason(professional))
                .priceReason(generatePriceReason(professional, job))
                .build();
    }

    /*
      distance between professional and job location using Haversine formula
     */
    private double calculateDistance(Professional professional, Job job) {
        return GeoUtils.calculateDistance(
                professional.getLatitude(),
                professional.getLongitude(),
                job.getLatitude(),
                job.getLongitude()
        );
    }

    /*
     Score distance: 100 if <= 10km, linearly decreasing to 0 at service radius
      Returns 0 if outside service radius
     */
    private double calculateDistanceScore(double distanceKm, int serviceRadiusKm) {
        return GeoUtils.calculateDistanceScore(distanceKm, serviceRadiusKm);
    }

    /*
      Score expertise match based on:
      1. Exact job type match (50 points)
      2. Years of experience (30 points)
      3. Certifications (20 points)
     */
    private double calculateExpertiseScore(Professional professional, Job job) {
        double score = 0.0;

        List<ProfessionalExpertise> expertiseList = professional.getExpertiseList();
        if (expertiseList == null || expertiseList.isEmpty()) {
            return 0.0;
        }

        // Convert job type to expertise type format
        String requiredExpertise = convertJobTypeToExpertise(job.getJobType());

        // Check for exact expertise match
        boolean hasMatch = false;
        int maxExperienceYears = 0;
        boolean hasCertification = false;

        for (ProfessionalExpertise expertise : expertiseList) {
            if (expertise.getExpertiseType().equalsIgnoreCase(requiredExpertise)) {
                hasMatch = true;
                if (expertise.getYearsExperience() != null) {
                    maxExperienceYears = Math.max(maxExperienceYears, expertise.getYearsExperience());
                }
                if (expertise.getCertificationName() != null && !expertise.getCertificationName().isEmpty()) {
                    hasCertification = true;
                }
            }
        }

        // Exact match: 50 points
        if (hasMatch) {
            score += 50.0;
        } else {
            // Check for related expertise (partial credit)
            if (hasRelatedExpertise(expertiseList, requiredExpertise)) {
                score += 25.0; // Half credit for related expertise
            }
        }

        // Years of experience: up to 30 points (capped at 10+ years)
        if (maxExperienceYears > 0) {
            score += Math.min(30.0, maxExperienceYears * 3.0);
        }

        // Certification: 20 points
        if (hasCertification) {
            score += 20.0;
        }

        return Math.min(100.0, score);
    }

    /*
      Score availability based on:
     1. Has slots on preferred date (60 points)
     2. Number of available slots (30 points)
     3. Flexibility (slots within +/- 7 days) (10 points)
     */
    private double calculateAvailabilityScore(Professional professional, Job job) {
        List<AvailabilitySlot> slots = professional.getAvailabilitySlots();

        if (slots == null || slots.isEmpty()) {
            return 20.0; // Base score for being in the system
        }

        double score = 0.0;
        LocalDate preferredDate = job.getPreferredDate();

        if (preferredDate == null) {
            // No preferred date - just check if they have any available slots
            long availableCount = slots.stream()
                    .filter(slot -> !slot.getIsBooked())
                    .count();
            return availableCount > 0 ? 70.0 : 20.0;
        }

        // Check for slots on preferred date
        long preferredDateSlots = slots.stream()
                .filter(slot -> slot.getDate().equals(preferredDate) && !slot.getIsBooked())
                .count();

        if (preferredDateSlots > 0) {
            score += 60.0;
        }

        // Number of available slots (normalized to 30 points max)
        long totalAvailable = slots.stream()
                .filter(slot -> !slot.getIsBooked())
                .count();
        score += Math.min(30.0, totalAvailable * 5.0);

        // Flexibility: has slots within +/- 7 days of preferred date
        if (preferredDateSlots == 0) {
            LocalDate startDate = preferredDate.minusDays(7);
            LocalDate endDate = preferredDate.plusDays(7);

            long nearbySlots = slots.stream()
                    .filter(slot -> !slot.getIsBooked() &&
                            !slot.getDate().isBefore(startDate) &&
                            !slot.getDate().isAfter(endDate))
                    .count();

            if (nearbySlots > 0) {
                score += 10.0;
            }
        }

        return Math.min(100.0, score);
    }

    /*
      Score rating: Direct conversion from 0-5 rating to 0-100 score
     Also considers track record (number of completed jobs)
     */
    private double calculateRatingScore(Professional professional) {
        BigDecimal rating = professional.getRating();
        Integer jobsCompleted = professional.getTotalJobsCompleted();

        if (rating == null || rating.compareTo(BigDecimal.ZERO) == 0) {
            return 50.0; // Neutral score for new professionals
        }

        // Convert 0-5 rating to 0-100 score
        double baseScore = rating.doubleValue() * 20.0;

        // Bonus for track record (up to 10 points)
        double trackRecordBonus = 0.0;
        if (jobsCompleted != null && jobsCompleted > 0) {
            trackRecordBonus = Math.min(10.0, jobsCompleted * 0.5);
        }

        return Math.min(100.0, baseScore + trackRecordBonus);
    }

    /*
     Score price competitiveness based on hourly rate vs job budget
     Higher score for rates within customer's budget
     */
    private double calculatePriceScore(Professional professional, Job job) {
        BigDecimal hourlyRate = professional.getHourlyRate();

        if (hourlyRate == null) {
            return 50.0; // Neutral score if no rate specified
        }

        BigDecimal budgetMax = job.getBudgetMax();

        if (budgetMax == null) {
            // No budget specified - score based on market rate
            // Assume average job is 20 hours, market rate is $50-150/hr
            double totalCost = hourlyRate.doubleValue() * 20;
            if (totalCost < 1000) return 100.0;       // Very affordable
            if (totalCost < 2000) return 80.0;        // Affordable
            if (totalCost < 3000) return 60.0;        // Moderate
            return 40.0;                               // Expensive
        }

        // Estimate total cost (assume 20 hours for typical job)
        double estimatedCost = hourlyRate.doubleValue() * 20;
        double budget = budgetMax.doubleValue();

        if (estimatedCost <= budget * 0.7) {
            return 100.0; // Well within budget
        } else if (estimatedCost <= budget) {
            return 80.0;  // Within budget
        } else if (estimatedCost <= budget * 1.2) {
            return 60.0;  // Slightly over budget
        } else if (estimatedCost <= budget * 1.5) {
            return 40.0;  // Over budget
        } else {
            return 20.0;  // Significantly over budget
        }
    }

    private String convertJobTypeToExpertise(JobType jobType) {
        return switch (jobType) {
            case INSTALLATION -> "PANEL_INSTALLATION";
            case BATTERY_SETUP -> "BATTERY_SETUP";
            case MAINTENANCE -> "MAINTENANCE";
            case REPAIR -> "REPAIR";
            case INSPECTION -> "INSPECTION";
            case UPGRADE -> "UPGRADE";
        };
    }

    private boolean hasRelatedExpertise(List<ProfessionalExpertise> expertiseList, String requiredExpertise) {
        // Define related expertise relationships
        return expertiseList.stream().anyMatch(exp -> {
            String type = exp.getExpertiseType();
            // INSTALLATION relates to UPGRADE and INSPECTION
            if (requiredExpertise.equals("PANEL_INSTALLATION")) {
                return type.equals("UPGRADE") || type.equals("INSPECTION");
            }
            // MAINTENANCE relates to REPAIR and INSPECTION
            if (requiredExpertise.equals("MAINTENANCE")) {
                return type.equals("REPAIR") || type.equals("INSPECTION");
            }
            // REPAIR relates to MAINTENANCE
            if (requiredExpertise.equals("REPAIR")) {
                return type.equals("MAINTENANCE");
            }
            return false;
        });
    }

    private String generateDistanceReason(double distanceKm, int serviceRadiusKm) {
        if (distanceKm <= 10) {
            return "Very close - within 10km";
        } else if (distanceKm <= serviceRadiusKm * 0.5) {
            return String.format("Close - %.1fkm away", distanceKm);
        } else if (distanceKm <= serviceRadiusKm) {
            return String.format("Within service area - %.1fkm away", distanceKm);
        } else {
            return String.format("Outside service area - %.1fkm away", distanceKm);
        }
    }

    private String generateExpertiseReason(Professional professional, Job job) {
        String requiredExpertise = convertJobTypeToExpertise(job.getJobType());

        boolean hasMatch = professional.getExpertiseList().stream()
                .anyMatch(exp -> exp.getExpertiseType().equalsIgnoreCase(requiredExpertise));

        if (hasMatch) {
            return "Direct expertise match for " + job.getJobType();
        } else if (hasRelatedExpertise(professional.getExpertiseList(), requiredExpertise)) {
            return "Related expertise in solar systems";
        } else {
            return "General solar experience";
        }
    }

    private String generateAvailabilityReason(Professional professional, Job job) {
        if (job.getPreferredDate() == null) {
            return "Available for scheduling";
        }

        long matchingSlots = professional.getAvailabilitySlots().stream()
                .filter(slot -> slot.getDate().equals(job.getPreferredDate()) && !slot.getIsBooked())
                .count();

        if (matchingSlots > 0) {
            return "Available on preferred date (" + job.getPreferredDate() + ")";
        } else {
            return "Available for alternative dates";
        }
    }

    private String generateRatingReason(Professional professional) {
        BigDecimal rating = professional.getRating();
        Integer jobs = professional.getTotalJobsCompleted();

        if (rating == null || rating.compareTo(BigDecimal.ZERO) == 0) {
            return "New professional - no reviews yet";
        }

        return String.format("%.1f star rating based on %d completed jobs",
                rating.doubleValue(), jobs != null ? jobs : 0);
    }

    private String generatePriceReason(Professional professional, Job job) {
        if (professional.getHourlyRate() == null) {
            return "Rate to be negotiated";
        }

        if (job.getBudgetMax() == null) {
            return String.format("$%.2f per hour", professional.getHourlyRate().doubleValue());
        }

        double estimatedCost = professional.getHourlyRate().doubleValue() * 20;
        double budget = job.getBudgetMax().doubleValue();

        if (estimatedCost <= budget * 0.7) {
            return "Well within budget";
        } else if (estimatedCost <= budget) {
            return "Within budget";
        } else {
            return "Above budget estimate";
        }
    }
}