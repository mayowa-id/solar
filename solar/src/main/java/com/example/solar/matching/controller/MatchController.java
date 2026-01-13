package com.example.solar.matching.controller;

import com.example.solar.common.dto.ApiResponse;
import com.example.solar.matching.domain.MatchStatus;
import com.example.solar.matching.dto.MatchDto;
import com.example.solar.matching.dto.MatchRequest;
import com.example.solar.matching.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/find")
    public ResponseEntity<ApiResponse<List<MatchDto>>> findMatches(
            @Valid @RequestBody MatchRequest request) {
        List<MatchDto> matches = matchService.findMatches(request);
        return new ResponseEntity<>(
                ApiResponse.success("Matches found successfully", matches),
                HttpStatus.OK
        );
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<MatchDto>>> getMatchesByJobId(@PathVariable Long jobId) {
        List<MatchDto> matches = matchService.getMatchesByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<ApiResponse<List<MatchDto>>> getMatchesByProfessionalId(
            @PathVariable Long professionalId) {
        List<MatchDto> matches = matchService.getMatchesByProfessionalId(professionalId);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @PatchMapping("/{matchId}/status")
    public ResponseEntity<ApiResponse<MatchDto>> updateMatchStatus(
            @PathVariable Long matchId,
            @RequestParam MatchStatus status) {
        MatchDto match = matchService.updateMatchStatus(matchId, status);
        return ResponseEntity.ok(ApiResponse.success("Match status updated successfully", match));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<ApiResponse<Void>> deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatch(matchId);
        return ResponseEntity.ok(ApiResponse.success("Match deleted successfully", null));
    }
}