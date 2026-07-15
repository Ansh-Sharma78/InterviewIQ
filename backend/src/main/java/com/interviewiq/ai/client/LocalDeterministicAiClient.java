package com.interviewiq.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "local", matchIfMissing = true)
public class LocalDeterministicAiClient implements AiClient {
    private final ObjectMapper objectMapper;

    public LocalDeterministicAiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportAiResult generateReport(ReportAiRequest request) {
        int resumeLength = safe(request.resumeText()).length();
        int jdLength = safe(request.jobDescriptionText()).length();
        int ats = Math.max(52, Math.min(88, 58 + resumeLength % 21));
        int readiness = Math.max(48, Math.min(86, 54 + jdLength % 23));
        Map<String, Object> payload = Map.ofEntries(
                Map.entry("atsMatchScore", ats),
                Map.entry("missingSkills", List.of("Prioritize role-specific keywords", "Add measurable project impact", "Strengthen system design examples")),
                Map.entry("skillGapAnalysis", List.of("Compare the resume language with the target JD and close gaps in tools, frameworks, and impact metrics.")),
                Map.entry("strengths", List.of("Clear technical background", "Reusable project experience", "Relevant backend preparation path")),
                Map.entry("weaknesses", List.of("Some achievements need quantified outcomes", "JD keywords should appear more naturally in the resume")),
                Map.entry("resumeImprovementSuggestions", List.of("Rewrite bullets with action, scope, and measurable result", "Mirror important JD terminology where truthful")),
                Map.entry("missingKeywords", List.of("Spring Boot", "REST APIs", "MySQL", "Docker", "system design")),
                Map.entry("resumeRewriteSuggestions", List.of("Built and optimized REST APIs with Spring Boot, improving reliability and maintainability for core workflows.")),
                Map.entry("technicalInterviewQuestions", List.of("Explain Spring Boot auto-configuration.", "How would you design a secure JWT refresh-token flow?", "How do JPA transactions work?")),
                Map.entry("behavioralQuestions", List.of("Tell me about a time you debugged a production-like issue.", "How do you prioritize when learning for a role quickly?")),
                Map.entry("projectBasedQuestions", List.of("Walk through the architecture of your strongest backend project.", "What tradeoffs did you make in your database design?")),
                Map.entry("systemDesignQuestions", List.of("Design an async report-generation system with retries and polling.", "Design a document upload and parsing service.")),
                Map.entry("hrQuestions", List.of("Why are you interested in this role?", "What kind of team environment helps you do your best work?")),
                Map.entry("salaryNegotiationTips", List.of("Anchor on role scope and market data", "Emphasize backend ownership and AI integration experience")),
                Map.entry("twoWeekPreparationPlan", List.of("Days 1-3: revise resume/JD gaps", "Days 4-8: Spring Boot, JPA, security", "Days 9-12: system design and projects", "Days 13-14: mock interviews")),
                Map.entry("learningResources", List.of("Spring Security reference", "Docker getting started", "Database indexing practice", "System design interview notes")),
                Map.entry("interviewReadinessScore", readiness),
                Map.entry("finalSummary", "This locally generated report exercises the complete async report workflow. Replace the local AI provider with OpenAI or Gemini in a later hardening step.")
        );
        try {
            String json = objectMapper.writeValueAsString(payload);
            return new ReportAiResult(json, ats, readiness, estimateTokens(request), 850, "local-deterministic-v1");
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize local AI report", ex);
        }
    }

    @Override
    public ChatAiResult generateChatReply(ChatAiRequest request) {
        String message = safe(request.userMessage()).toLowerCase();
        String reply;
        if (message.contains("spring")) {
            reply = "Focus on Spring Boot auto-configuration, dependency injection, transaction boundaries, security filters, and how your own project uses layered architecture. Turn each concept into a short story from your code.";
        } else if (message.contains("resume")) {
            reply = "Rewrite resume bullets around action, technical scope, and measurable outcome. Use the job description's language only where it truthfully matches your experience.";
        } else if (message.contains("question")) {
            reply = "Here are five targeted prompts: explain your JWT flow, design async report generation, compare JPA lazy/eager loading, debug a failed PDF parse, and discuss how you would add rate limiting.";
        } else {
            reply = "Based on this report, prioritize the highest-signal gaps first: strengthen role keywords, practice backend fundamentals aloud, and prepare one project walkthrough that connects architecture, tradeoffs, and measurable impact.";
        }
        int promptTokens = Math.max(1, (safe(request.reportPayloadJson()).length() + safe(request.resumeText()).length() + safe(request.jobDescriptionText()).length()) / 5);
        return new ChatAiResult(reply, promptTokens, Math.max(40, reply.length() / 4), "local-deterministic-v1");
    }

    @Override
    public String provider() {
        return "local";
    }

    private int estimateTokens(ReportAiRequest request) {
        return Math.max(1, (safe(request.resumeText()).length() + safe(request.jobDescriptionText()).length()) / 4);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
