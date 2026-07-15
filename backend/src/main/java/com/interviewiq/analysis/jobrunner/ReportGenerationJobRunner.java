package com.interviewiq.analysis.jobrunner;

import com.interviewiq.ai.client.AiClient;
import com.interviewiq.ai.client.ReportAiRequest;
import com.interviewiq.ai.client.ReportAiResult;
import com.interviewiq.ai.usage.AiUsageLogService;
import com.interviewiq.analysis.entity.Report;
import com.interviewiq.analysis.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReportGenerationJobRunner {
    private static final Logger log = LoggerFactory.getLogger(ReportGenerationJobRunner.class);
    private static final int MAX_FAILURE_REASON_LENGTH = 240;

    private final ReportRepository reportRepository;
    private final AiClient aiClient;
    private final AiUsageLogService usageLogService;

    public ReportGenerationJobRunner(ReportRepository reportRepository, AiClient aiClient, AiUsageLogService usageLogService) {
        this.reportRepository = reportRepository;
        this.aiClient = aiClient;
        this.usageLogService = usageLogService;
    }

    @Async
    @Transactional
    public void run(Long reportId, Long userId) {
        Report report = reportRepository.findByIdAndUserIdAndDeletedAtIsNull(reportId, userId).orElseThrow();
        report.markProcessing();
        try {
            ReportAiResult result = aiClient.generateReport(new ReportAiRequest(
                    report.getResume().getParsedText(),
                    report.getJobDescription().getRawText(),
                    report.getJobDescription().getCompanyName(),
                    report.getJobDescription().getRoleTitle()
            ));
            report.markCompleted(result.payloadJson(), result.atsMatchScore(), result.interviewReadinessScore());
            usageLogService.record(userId, "REPORT_GENERATION", aiClient.provider(), result.model(), result.promptTokens(), result.completionTokens(), true, null);
        } catch (RuntimeException ex) {
            log.warn("Report generation failed for report {} using provider {}", reportId, aiClient.provider(), ex);
            report.markFailed(toFailureReason(ex));
            usageLogService.record(userId, "REPORT_GENERATION", aiClient.provider(), "unknown", 0, 0, false, ex.getClass().getSimpleName());
        }
    }

    private String toFailureReason(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "Report generation failed. Please retry.";
        }
        return message.length() <= MAX_FAILURE_REASON_LENGTH ? message : message.substring(0, MAX_FAILURE_REASON_LENGTH);
    }
}
