package com.interviewiq.analysis.repository;

import com.interviewiq.analysis.entity.Report;
import com.interviewiq.analysis.entity.ReportStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<Report> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    long countByUserIdAndDeletedAtIsNull(Long userId);

    Page<Report> findByUserIdAndDeletedAtIsNullAndStatus(Long userId, ReportStatus status, Pageable pageable);

    @Query("select avg(r.atsMatchScore) from Report r where r.user.id = :userId and r.deletedAt is null and r.status = :status and r.atsMatchScore is not null")
    Double averageAtsScore(Long userId, ReportStatus status);

    @Query("select avg(r.interviewReadinessScore) from Report r where r.user.id = :userId and r.deletedAt is null and r.status = :status and r.interviewReadinessScore is not null")
    Double averageReadinessScore(Long userId, ReportStatus status);
}
