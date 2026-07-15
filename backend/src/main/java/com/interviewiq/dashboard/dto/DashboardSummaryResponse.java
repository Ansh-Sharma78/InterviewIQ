package com.interviewiq.dashboard.dto;

import com.interviewiq.analysis.dto.ReportSummaryResponse;
import com.interviewiq.chat.dto.ChatSessionResponse;
import com.interviewiq.jobdescription.dto.JobDescriptionSummaryResponse;
import com.interviewiq.resume.dto.ResumeSummaryResponse;
import java.util.List;

public record DashboardSummaryResponse(
        long totalReports,
        Double averageAtsScore,
        Double averageReadinessScore,
        List<ScoreTrendPoint> scoreTrend,
        List<ResumeSummaryResponse> recentResumes,
        List<JobDescriptionSummaryResponse> recentJobDescriptions,
        List<ReportSummaryResponse> recentReports,
        List<ChatSessionResponse> recentChatSessions
) {
    public record ScoreTrendPoint(Long reportId, Integer atsMatchScore, Integer interviewReadinessScore, String createdAt) {
    }
}
