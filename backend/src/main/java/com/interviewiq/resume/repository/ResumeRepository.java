package com.interviewiq.resume.repository;

import com.interviewiq.resume.entity.ParseStatus;
import com.interviewiq.resume.entity.Resume;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<Resume> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<Resume> findFirstByChecksumAndParseStatusAndParsedTextIsNotNullOrderByCreatedAtDesc(String checksum, ParseStatus parseStatus);
}

