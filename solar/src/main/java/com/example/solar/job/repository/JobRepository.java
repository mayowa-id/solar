package com.example.solar.job.repository;

import com.example.solar.job.domain.Job;
import com.example.solar.job.domain.JobStatus;
import com.example.solar.job.domain.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByCustomerId(Long customerId);
    List<Job> findByStatus(JobStatus status);
    List<Job> findByJobType(JobType jobType);
    List<Job> findByCustomerIdAndStatus(Long customerId, JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = 'PENDING' OR j.status = 'MATCHED'")
    List<Job> findOpenJobs();
}