package com.example.solar.matching.repository;

import com.example.solar.matching.domain.Match;
import com.example.solar.matching.domain.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByJobId(Long jobId);
    List<Match> findByProfessionalId(Long professionalId);
    List<Match> findByJobIdAndStatus(Long jobId, MatchStatus status);
    Optional<Match> findByJobIdAndProfessionalId(Long jobId, Long professionalId);

    @Query("SELECT m FROM Match m WHERE m.job.id = :jobId ORDER BY m.matchScore DESC")
    List<Match> findByJobIdOrderByMatchScoreDesc(Long jobId);

    boolean existsByJobIdAndProfessionalId(Long jobId, Long professionalId);
}