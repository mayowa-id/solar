package com.example.solar.quote.controller;

import com.example.solar.common.dto.ApiResponse;
import com.example.solar.quote.dto.CreateQuoteRequest;
import com.example.solar.quote.dto.QuoteComparisonDto;
import com.example.solar.quote.dto.QuoteDto;
import com.example.solar.quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuoteDto>> createQuote(
            @Valid @RequestBody CreateQuoteRequest request) {
        QuoteDto quote = quoteService.createQuote(request);
        return new ResponseEntity<>(
                ApiResponse.success("Quote created successfully", quote),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuoteDto>> getQuoteById(@PathVariable Long id) {
        QuoteDto quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<QuoteDto>>> getQuotesByJobId(@PathVariable Long jobId) {
        List<QuoteDto> quotes = quoteService.getQuotesByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<ApiResponse<List<QuoteDto>>> getQuotesByProfessionalId(
            @PathVariable Long professionalId) {
        List<QuoteDto> quotes = quoteService.getQuotesByProfessionalId(professionalId);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/job/{jobId}/compare")
    public ResponseEntity<ApiResponse<QuoteComparisonDto>> compareQuotes(@PathVariable Long jobId) {
        QuoteComparisonDto comparison = quoteService.compareQuotes(jobId);
        return ResponseEntity.ok(ApiResponse.success(comparison));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<QuoteDto>> acceptQuote(@PathVariable Long id) {
        QuoteDto quote = quoteService.acceptQuote(id);
        return ResponseEntity.ok(ApiResponse.success("Quote accepted successfully", quote));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<QuoteDto>> rejectQuote(@PathVariable Long id) {
        QuoteDto quote = quoteService.rejectQuote(id);
        return ResponseEntity.ok(ApiResponse.success("Quote rejected successfully", quote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuote(@PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.ok(ApiResponse.success("Quote deleted successfully", null));
    }
}
