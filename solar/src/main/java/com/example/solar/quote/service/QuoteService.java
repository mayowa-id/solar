package com.example.solar.quote.service;



import com.example.solar.common.exception.ResourceNotFoundException;
import com.example.solar.common.exception.ValidationException;
import com.example.solar.common.util.ValidationUtils;
import com.example.solar.job.domain.Job;
import com.example.solar.job.repository.JobRepository;
import com.example.solar.matching.domain.Match;
import com.example.solar.matching.repository.MatchRepository;
import com.example.solar.professional.domain.Professional;
import com.example.solar.professional.repository.ProfessionalRepository;
import com.example.solar.quote.domain.Quote;
import com.example.solar.quote.domain.QuoteStatus;
import com.example.solar.quote.dto.CreateQuoteRequest;
import com.example.solar.quote.dto.QuoteComparisonDto;
import com.example.solar.quote.dto.QuoteDto;
import com.example.solar.quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final JobRepository jobRepository;
    private final ProfessionalRepository professionalRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public QuoteDto createQuote(CreateQuoteRequest request) {
        log.info("Creating quote for job {} by professional {}",
                request.getJobId(), request.getProfessionalId());

        // Validate job exists
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        // Validate professional exists
        Professional professional = professionalRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Professional", "id", request.getProfessionalId()));

        // Check if quote already exists for this job-professional pair
        if (quoteRepository.existsByJobIdAndProfessionalId(request.getJobId(), request.getProfessionalId())) {
            throw new ValidationException("Quote already exists for this job and professional");
        }

        // Validate match if provided
        Match match = null;
        if (request.getMatchId() != null) {
            match = matchRepository.findById(request.getMatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match", "id", request.getMatchId()));
        }

        // Validate cost breakdown if provided
        if (request.getMaterialsCost() != null && request.getLaborCost() != null) {
            BigDecimal total = request.getMaterialsCost().add(request.getLaborCost());
            if (total.compareTo(request.getAmount()) != 0) {
                log.warn("Materials + Labor ({}) doesn't match total amount ({})", total, request.getAmount());
            }
        }

        // Create quote
        Quote quote = Quote.builder()
                .job(job)
                .professional(professional)
                .match(match)
                .amount(request.getAmount())
                .estimatedHours(request.getEstimatedHours())
                .materialsCost(request.getMaterialsCost())
                .laborCost(request.getLaborCost())
                .details(ValidationUtils.sanitize(request.getDetails()))
                .validUntil(request.getValidUntil())
                .status(QuoteStatus.PENDING)
                .build();

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote created successfully with ID: {}", savedQuote.getId());

        return mapToDto(savedQuote);
    }

    @Transactional(readOnly = true)
    public QuoteDto getQuoteById(Long id) {
        log.info("Fetching quote with ID: {}", id);
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));
        return mapToDto(quote);
    }

    @Transactional(readOnly = true)
    public List<QuoteDto> getQuotesByJobId(Long jobId) {
        log.info("Fetching quotes for job ID: {}", jobId);

        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job", "id", jobId);
        }

        return quoteRepository.findByJobIdOrderByAmountAsc(jobId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuoteDto> getQuotesByProfessionalId(Long professionalId) {
        log.info("Fetching quotes for professional ID: {}", professionalId);

        if (!professionalRepository.existsById(professionalId)) {
            throw new ResourceNotFoundException("Professional", "id", professionalId);
        }

        return quoteRepository.findByProfessionalId(professionalId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuoteComparisonDto compareQuotes(Long jobId) {
        log.info("Comparing quotes for job ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        List<Quote> quotes = quoteRepository.findByJobId(jobId);

        if (quotes.isEmpty()) {
            throw new ResourceNotFoundException("No quotes found for job ID: " + jobId);
        }

        List<QuoteDto> quoteDtos = quotes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        BigDecimal lowestAmount = quotes.stream()
                .map(Quote::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal highestAmount = quotes.stream()
                .map(Quote::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal sum = quotes.stream()
                .map(Quote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAmount = sum.divide(
                BigDecimal.valueOf(quotes.size()), 2, RoundingMode.HALF_UP);

        return QuoteComparisonDto.builder()
                .jobId(jobId)
                .jobTitle(job.getTitle())
                .quotes(quoteDtos)
                .lowestAmount(lowestAmount)
                .highestAmount(highestAmount)
                .averageAmount(averageAmount)
                .totalQuotes(quotes.size())
                .build();
    }

    @Transactional
    public QuoteDto acceptQuote(Long quoteId) {
        log.info("Accepting quote ID: {}", quoteId);

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", quoteId));

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new ValidationException("Only pending quotes can be accepted");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        Quote acceptedQuote = quoteRepository.save(quote);

        // Reject all other quotes for this job
        List<Quote> otherQuotes = quoteRepository.findByJobIdAndStatus(
                quote.getJob().getId(), QuoteStatus.PENDING);

        for (Quote otherQuote : otherQuotes) {
            if (!otherQuote.getId().equals(quoteId)) {
                otherQuote.setStatus(QuoteStatus.REJECTED);
                quoteRepository.save(otherQuote);
            }
        }

        log.info("Quote accepted and other quotes rejected for job {}", quote.getJob().getId());
        return mapToDto(acceptedQuote);
    }

    @Transactional
    public QuoteDto rejectQuote(Long quoteId) {
        log.info("Rejecting quote ID: {}", quoteId);

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", quoteId));

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new ValidationException("Only pending quotes can be rejected");
        }

        quote.setStatus(QuoteStatus.REJECTED);
        Quote rejectedQuote = quoteRepository.save(quote);

        log.info("Quote rejected successfully");
        return mapToDto(rejectedQuote);
    }

    @Transactional
    public void deleteQuote(Long id) {
        log.info("Deleting quote with ID: {}", id);

        if (!quoteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quote", "id", id);
        }

        quoteRepository.deleteById(id);
        log.info("Quote deleted successfully");
    }

    private QuoteDto mapToDto(Quote quote) {
        return QuoteDto.builder()
                .id(quote.getId())
                .jobId(quote.getJob().getId())
                .jobTitle(quote.getJob().getTitle())
                .professionalId(quote.getProfessional().getId())
                .professionalName(quote.getProfessional().getCompanyName())
                .professionalEmail(quote.getProfessional().getEmail())
                .professionalPhone(quote.getProfessional().getPhone())
                .amount(quote.getAmount())
                .estimatedHours(quote.getEstimatedHours())
                .materialsCost(quote.getMaterialsCost())
                .laborCost(quote.getLaborCost())
                .details(quote.getDetails())
                .validUntil(quote.getValidUntil())
                .status(quote.getStatus())
                .submittedAt(quote.getSubmittedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }
}