package com.interviewiq.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
public class OllamaClient implements AiClient {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OllamaClient(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.ai.ollama.model:llama3:8b}") String model
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public ReportAiResult generateReport(ReportAiRequest request) {
        String text = callOllamaApi(buildReportPrompt(request), reportSystemPrompt(), true, 1800);
        JsonNode payload = normalizeReportPayload(parseJsonPayloadWithRepair(text));
        if (!hasReportContent(payload)) {
            throw new AiProviderException("Ollama completed but did not return usable report content. Please retry.");
        }
        return new ReportAiResult(
                payload.toString(),
                extractInt(payload, "atsMatchScore"),
                extractInt(payload, "interviewReadinessScore"),
                estimatePromptTokens(request),
                estimateCompletionTokens(text),
                model
        );
    }

    @Override
    public ChatAiResult generateChatReply(ChatAiRequest request) {
        String text = callOllamaApi(buildChatPrompt(request), chatSystemPrompt(), false, 700);
        return new ChatAiResult(text, estimateChatPromptTokens(request), estimateCompletionTokens(text), model);
    }

    @Override
    public String provider() {
        return "ollama";
    }

    private String callOllamaApi(String prompt, String systemPrompt, boolean structuredJson, int maxTokens) {
        try {
            String userPrompt = prompt;
            if (structuredJson) {
                userPrompt = prompt + """

                        Return only a valid JSON object matching this JSON schema:
                        %s
                        """.formatted(objectMapper.writeValueAsString(reportSchema()));
            }

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt + "\n\nMaximum output tokens: " + maxTokens)
                    .call()
                    .content();
            if (response == null || response.isBlank()) {
                throw new AiProviderException("Ollama returned an empty response");
            }
            return response;
        } catch (IOException ex) {
            throw new AiProviderException("Unable to build Ollama prompt", ex);
        } catch (RuntimeException ex) {
            if (ex instanceof AiProviderException) {
                throw (AiProviderException) ex;
            }
            throw new AiProviderException("Failed to call Ollama through Spring AI. Make sure Ollama is running and the model is pulled.", ex);
        }
    }

    private JsonNode parseJsonPayloadWithRepair(String text) {
        try {
            return parseJsonPayload(text);
        } catch (AiProviderException firstFailure) {
            String repaired = callOllamaApi(
                    "Convert this response into valid InterviewIQ report JSON. Return only the JSON object.\n\n" + safe(text),
                    reportSystemPrompt(),
                    true,
                    1800
            );
            try {
                return parseJsonPayload(repaired);
            } catch (AiProviderException secondFailure) {
                throw new AiProviderException("Ollama report response was not valid JSON after retry", secondFailure);
            }
        }
    }

    private JsonNode parseJsonPayload(String text) {
        try {
            return objectMapper.readTree(stripJson(text));
        } catch (IOException ex) {
            throw new AiProviderException("Ollama report response is not valid JSON", ex);
        }
    }

    private JsonNode normalizeReportPayload(JsonNode rawPayload) {
        JsonNode source = rawPayload;
        if (rawPayload.has("report") && rawPayload.path("report").isObject()) {
            source = rawPayload.path("report");
        } else if (rawPayload.has("analysis") && rawPayload.path("analysis").isObject()) {
            source = rawPayload.path("analysis");
        }

        ObjectNode normalized = objectMapper.createObjectNode();
        putText(normalized, "finalSummary", firstNode(source, "finalSummary", "final_summary", "summary", "overallSummary"));
        putInt(normalized, "atsMatchScore", firstNode(source, "atsMatchScore", "ats_match_score", "atsScore", "matchScore"));
        putInt(normalized, "interviewReadinessScore", firstNode(source, "interviewReadinessScore", "interview_readiness_score", "readinessScore"));
        putArray(normalized, "missingSkills", source, "missingSkills", "missing_skills");
        putArray(normalized, "skillGapAnalysis", source, "skillGapAnalysis", "skill_gap_analysis");
        putArray(normalized, "strengths", source, "strengths");
        putArray(normalized, "weaknesses", source, "weaknesses");
        putArray(normalized, "resumeImprovementSuggestions", source, "resumeImprovementSuggestions", "resume_improvement_suggestions");
        putArray(normalized, "missingKeywords", source, "missingKeywords", "missing_keywords");
        putArray(normalized, "resumeRewriteSuggestions", source, "resumeRewriteSuggestions", "resume_rewrite_suggestions");
        putArray(normalized, "technicalInterviewQuestions", source, "technicalInterviewQuestions", "technical_interview_questions");
        putArray(normalized, "behavioralQuestions", source, "behavioralQuestions", "behavioral_questions");
        putArray(normalized, "projectBasedQuestions", source, "projectBasedQuestions", "project_based_questions");
        putArray(normalized, "systemDesignQuestions", source, "systemDesignQuestions", "system_design_questions");
        putArray(normalized, "hrQuestions", source, "hrQuestions", "hr_questions");
        putArray(normalized, "salaryNegotiationTips", source, "salaryNegotiationTips", "salary_negotiation_tips");
        putArray(normalized, "twoWeekPreparationPlan", source, "twoWeekPreparationPlan", "two_week_preparation_plan");
        putArray(normalized, "learningResources", source, "learningResources", "learning_resources");

        if (!rawPayload.equals(source) || rawPayload.size() > normalized.size()) {
            normalized.set("rawModelOutput", rawPayload);
        }
        return normalized;
    }

    private boolean hasReportContent(JsonNode payload) {
        if (!safe(payload.path("finalSummary").asText()).isBlank()) {
            return true;
        }
        for (String field : List.of(
                "missingSkills",
                "skillGapAnalysis",
                "strengths",
                "weaknesses",
                "resumeImprovementSuggestions",
                "missingKeywords",
                "resumeRewriteSuggestions",
                "technicalInterviewQuestions",
                "behavioralQuestions",
                "projectBasedQuestions",
                "systemDesignQuestions",
                "hrQuestions",
                "salaryNegotiationTips",
                "twoWeekPreparationPlan",
                "learningResources"
        )) {
            if (payload.path(field).isArray() && !payload.path(field).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private JsonNode firstNode(JsonNode source, String... fields) {
        for (String field : fields) {
            JsonNode value = source.path(field);
            if (!value.isMissingNode() && !value.isNull()) {
                return value;
            }
        }
        return objectMapper.nullNode();
    }

    private void putText(ObjectNode target, String field, JsonNode value) {
        target.put(field, value.isTextual() ? value.asText() : value.isMissingNode() || value.isNull() ? "" : value.toString());
    }

    private void putInt(ObjectNode target, String field, JsonNode value) {
        target.put(field, value.canConvertToInt() ? value.asInt() : 0);
    }

    private void putArray(ObjectNode target, String field, JsonNode source, String... aliases) {
        JsonNode value = firstNode(source, aliases);
        ArrayNode array = objectMapper.createArrayNode();
        if (value.isArray()) {
            value.forEach((item) -> array.add(formatNode(item)));
        } else if (!value.isMissingNode() && !value.isNull()) {
            array.add(formatNode(value));
        }
        target.set(field, array);
    }

    private String formatNode(JsonNode value) {
        return value.isTextual() ? value.asText() : value.toString();
    }

    private Map<String, Object> reportSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("finalSummary", stringSchema());
        properties.put("atsMatchScore", integerSchema());
        properties.put("interviewReadinessScore", integerSchema());
        properties.put("missingSkills", stringArraySchema());
        properties.put("skillGapAnalysis", stringArraySchema());
        properties.put("strengths", stringArraySchema());
        properties.put("weaknesses", stringArraySchema());
        properties.put("resumeImprovementSuggestions", stringArraySchema());
        properties.put("missingKeywords", stringArraySchema());
        properties.put("resumeRewriteSuggestions", stringArraySchema());
        properties.put("technicalInterviewQuestions", stringArraySchema());
        properties.put("behavioralQuestions", stringArraySchema());
        properties.put("projectBasedQuestions", stringArraySchema());
        properties.put("systemDesignQuestions", stringArraySchema());
        properties.put("hrQuestions", stringArraySchema());
        properties.put("salaryNegotiationTips", stringArraySchema());
        properties.put("twoWeekPreparationPlan", stringArraySchema());
        properties.put("learningResources", stringArraySchema());

        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.copyOf(properties.keySet())
        );
    }

    private Map<String, Object> stringSchema() {
        return Map.of("type", "string");
    }

    private Map<String, Object> integerSchema() {
        return Map.of("type", "integer");
    }

    private Map<String, Object> stringArraySchema() {
        return Map.of("type", "array", "items", stringSchema());
    }

    private String reportSystemPrompt() {
        return """
                You are InterviewIQ, an interview preparation assistant.
                Return accurate report JSON only. Scores must be integers from 0 to 100.
                All list fields must be arrays of concise strings.
                Do not invent candidate experience that is not present in the resume.
                """;
    }

    private String buildReportPrompt(ReportAiRequest request) {
        return """
                Analyze this candidate resume against the target job description.

                Company: %s
                Role: %s

                Resume:
                %s

                Job description:
                %s
                """.formatted(
                safe(request.companyName()),
                safe(request.roleTitle()),
                truncate(request.resumeText(), 18000),
                truncate(request.jobDescriptionText(), 12000)
        );
    }

    private String chatSystemPrompt() {
        return """
                You are InterviewIQ's report-scoped interview coach.
                Answer using the supplied resume, job description, report JSON, and recent chat turns.
                Be specific, practical, and concise. Do not invent experience the candidate did not provide.
                """;
    }

    private String buildChatPrompt(ChatAiRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Report JSON:\n").append(truncate(request.reportPayloadJson(), 12000)).append("\n\n");
        builder.append("Resume context:\n").append(truncate(request.resumeText(), 8000)).append("\n\n");
        builder.append("Job description context:\n").append(truncate(request.jobDescriptionText(), 8000)).append("\n\n");
        if (request.recentTurns() != null && !request.recentTurns().isEmpty()) {
            builder.append("Recent conversation:\n");
            for (ChatAiRequest.ChatTurn turn : request.recentTurns()) {
                builder.append(turn.role()).append(": ").append(turn.content()).append("\n");
            }
            builder.append("\n");
        }
        builder.append("User message:\n").append(safe(request.userMessage()));
        return builder.toString();
    }

    private String stripJson(String text) {
        String trimmed = safe(text).trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        int first = trimmed.indexOf('{');
        int last = trimmed.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return trimmed.substring(first, last + 1);
        }
        return trimmed;
    }

    private int extractInt(JsonNode payload, String field) {
        JsonNode value = payload.path(field);
        return value.canConvertToInt() ? value.asInt() : 0;
    }

    private String truncate(String value, int maxChars) {
        String safe = safe(value);
        return safe.length() <= maxChars ? safe : safe.substring(0, maxChars);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int estimatePromptTokens(ReportAiRequest request) {
        return Math.max(1, (safe(request.resumeText()).length() + safe(request.jobDescriptionText()).length()) / 4);
    }

    private int estimateChatPromptTokens(ChatAiRequest request) {
        int length = safe(request.reportPayloadJson()).length()
                + safe(request.resumeText()).length()
                + safe(request.jobDescriptionText()).length()
                + safe(request.userMessage()).length();
        if (request.recentTurns() != null) {
            for (ChatAiRequest.ChatTurn turn : request.recentTurns()) {
                length += safe(turn.content()).length();
            }
        }
        return Math.max(1, length / 4);
    }

    private int estimateCompletionTokens(String text) {
        return Math.max(1, safe(text).length() / 4);
    }
}
