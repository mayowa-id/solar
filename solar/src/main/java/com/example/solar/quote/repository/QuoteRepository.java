package com.example.solar.quote.repository;


import com.example.solar.quote.domain.Quote;
import com.example.solar.quote.domain.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByJobId(Long jobId);
    List<Quote> findByProfessionalId(Long professionalId);
    List<Quote> findByJobIdAndStatus(Long jobId, QuoteStatus status);
    Optional<Quote> findByJobIdAndProfessionalId(Long jobId, Long professionalId);
    boolean existsByJobIdAndProfessionalId(Long jobId, Long professionalId);

    @Query("SELECT q FROM Quote q WHERE q.job.id = :jobId ORDER BY q.amount ASC")
    List<Quote> findByJobIdOrderByAmountAsc(Long jobId);
}
