package com.interviewiq.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.analysis.dto.ReportResponse;
import com.interviewiq.analysis.dto.ReportSummaryResponse;
import com.interviewiq.analysis.entity.Report;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {
    private final ObjectMapper objectMapper;

    public ReportMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getResume().getId(),
                report.getJobDescription().getId(),
                report.getStatus(),
                report.getAtsMatchScore(),
                report.getInterviewReadinessScore(),
                report.getFailureReason(),
                parsePayload(report.getPayloadJson()),
                report.getCreatedAt()
        );
    }

    public ReportSummaryResponse toSummary(Report report) {
        return new ReportSummaryResponse(
                report.getId(),
                report.getResume().getId(),
                report.getJobDescription().getId(),
                report.getStatus(),
                report.getAtsMatchScore(),
                report.getInterviewReadinessScore(),
                report.getCreatedAt()
        );
    }

    private JsonNode parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payloadJson);
        } catch (IOException ex) {
            return null;
        }
    }
}

