package com.interviewiq.jobdescription.repository;

import com.interviewiq.jobdescription.entity.JobDescription;
import com.interviewiq.resume.entity.ParseStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    Page<JobDescription> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<JobDescription> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<JobDescription> findFirstByChecksumAndParseStatusAndRawTextIsNotNullOrderByCreatedAtDesc(String checksum, ParseStatus parseStatus);
}

