package com.interviewiq.dashboard.service;

import com.interviewiq.analysis.dto.ReportSummaryResponse;
import com.interviewiq.analysis.entity.Report;
import com.interviewiq.analysis.entity.ReportStatus;
import com.interviewiq.analysis.repository.ReportRepository;
import com.interviewiq.analysis.service.ReportMapper;
import com.interviewiq.chat.dto.ChatSessionResponse;
import com.interviewiq.chat.entity.ChatSession;
import com.interviewiq.chat.repository.ChatSessionRepository;
import com.interviewiq.dashboard.dto.DashboardSummaryResponse;
import com.interviewiq.jobdescription.dto.JobDescriptionSummaryResponse;
import com.interviewiq.jobdescription.entity.JobDescription;
import com.interviewiq.jobdescription.repository.JobDescriptionRepository;
import com.interviewiq.resume.dto.ResumeSummaryResponse;
import com.interviewiq.resume.entity.Resume;
import com.interviewiq.resume.repository.ResumeRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ReportRepository reportRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ReportMapper reportMapper;

    public DashboardService(ReportRepository reportRepository, ResumeRepository resumeRepository, JobDescriptionRepository jobDescriptionRepository, ChatSessionRepository chatSessionRepository, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.reportMapper = reportMapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(Long userId) {
        PageRequest recent = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRequest trendPage = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<Report> completedReports = reportRepository.findByUserIdAndDeletedAtIsNullAndStatus(userId, ReportStatus.COMPLETED, trendPage).getContent();
        return new DashboardSummaryResponse(
                reportRepository.countByUserIdAndDeletedAtIsNull(userId),
                round(reportRepository.averageAtsScore(userId, ReportStatus.COMPLETED)),
                round(reportRepository.averageReadinessScore(userId, ReportStatus.COMPLETED)),
                completedReports.stream().map(this::toTrendPoint).toList(),
                resumeRepository.findByUserIdAndDeletedAtIsNull(userId, recent).map(this::toResumeSummary).getContent(),
                jobDescriptionRepository.findByUserIdAndDeletedAtIsNull(userId, recent).map(this::toJobDescriptionSummary).getContent(),
                reportRepository.findByUserIdAndDeletedAtIsNull(userId, recent).map(reportMapper::toSummary).getContent(),
                chatSessionRepository.findByUserIdAndDeletedAtIsNull(userId, recent).map(this::toChatSessionResponse).getContent()
        );
    }

    private DashboardSummaryResponse.ScoreTrendPoint toTrendPoint(Report report) {
        return new DashboardSummaryResponse.ScoreTrendPoint(
                report.getId(),
                report.getAtsMatchScore(),
                report.getInterviewReadinessScore(),
                report.getCreatedAt().toString()
        );
    }

    private ResumeSummaryResponse toResumeSummary(Resume resume) {
        return new ResumeSummaryResponse(resume.getId(), resume.getOriginalFilename(), resume.getFileSizeBytes(), resume.getParseStatus(), preview(resume.getParsedText()), resume.getCreatedAt());
    }

    private JobDescriptionSummaryResponse toJobDescriptionSummary(JobDescription jd) {
        return new JobDescriptionSummaryResponse(jd.getId(), jd.getCompanyName(), jd.getRoleTitle(), jd.getSourceType(), jd.getParseStatus(), preview(jd.getRawText()), jd.getCreatedAt());
    }

    private ChatSessionResponse toChatSessionResponse(ChatSession session) {
        return new ChatSessionResponse(session.getId(), session.getReport().getId(), session.getTitle(), session.getCreatedAt());
    }

    private String preview(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= 180 ? text : text.substring(0, 180);
    }

    private Double round(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10.0) / 10.0;
    }
}
