package com.interviewiq.analysis.service;

import com.interviewiq.analysis.dto.GenerateReportRequest;
import com.interviewiq.analysis.dto.ReportResponse;
import com.interviewiq.analysis.dto.ReportSummaryResponse;
import com.interviewiq.analysis.entity.Report;
import com.interviewiq.analysis.entity.ReportStatus;
import com.interviewiq.analysis.jobrunner.ReportGenerationJobRunner;
import com.interviewiq.analysis.repository.ReportRepository;
import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.repository.UserRepository;
import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.common.exception.DomainException;
import com.interviewiq.jobdescription.entity.JobDescription;
import com.interviewiq.jobdescription.repository.JobDescriptionRepository;
import com.interviewiq.resume.entity.ParseStatus;
import com.interviewiq.resume.entity.Resume;
import com.interviewiq.resume.repository.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;
    private final ReportGenerationJobRunner jobRunner;
    private final ReportMapper mapper;

    public ReportService(ReportRepository reportRepository, ResumeRepository resumeRepository, JobDescriptionRepository jobDescriptionRepository, UserRepository userRepository, ReportGenerationJobRunner jobRunner, ReportMapper mapper) {
        this.reportRepository = reportRepository;
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.userRepository = userRepository;
        this.jobRunner = jobRunner;
        this.mapper = mapper;
    }

    @Transactional
    public ReportResponse create(Long userId, GenerateReportRequest request) {
        Resume resume = resumeRepository.findByIdAndUserIdAndDeletedAtIsNull(request.resumeId(), userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "Resume not found"));
        JobDescription jd = jobDescriptionRepository.findByIdAndUserIdAndDeletedAtIsNull(request.jobDescriptionId(), userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "JOB_DESCRIPTION_NOT_FOUND", "Job description not found"));
        if (resume.getParseStatus() != ParseStatus.SUCCESS || jd.getParseStatus() != ParseStatus.SUCCESS) {
            throw new DomainException(HttpStatus.CONFLICT, "INPUT_PARSE_FAILED", "Resume and job description must have parsed text before report generation");
        }
        User user = userRepository.getReferenceById(userId);
        Report report = reportRepository.save(new Report(user, resume, jd));
        runAfterCommit(report.getId(), userId);
        return mapper.toResponse(report);
    }

    @Transactional(readOnly = true)
    public ReportResponse get(Long userId, Long id) {
        return reportRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found"));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReportSummaryResponse> list(Long userId, Pageable pageable) {
        Page<ReportSummaryResponse> page = reportRepository.findByUserIdAndDeletedAtIsNull(userId, pageable).map(mapper::toSummary);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional
    public ReportResponse retry(Long userId, Long id) {
        Report failed = reportRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found"));
        if (failed.getStatus() != ReportStatus.FAILED) {
            throw new DomainException(HttpStatus.CONFLICT, "REPORT_NOT_FAILED", "Only failed reports can be retried");
        }
        Report retry = reportRepository.save(new Report(userRepository.getReferenceById(userId), failed.getResume(), failed.getJobDescription()));
        runAfterCommit(retry.getId(), userId);
        return mapper.toResponse(retry);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Report report = reportRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found"));
        report.softDelete();
    }

    private void runAfterCommit(Long reportId, Long userId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                jobRunner.run(reportId, userId);
            }
        });
    }
}
