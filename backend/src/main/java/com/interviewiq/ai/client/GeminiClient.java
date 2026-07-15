package com.interviewiq.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiClient implements AiClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public GeminiClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ai.gemini.model:gemini-3.5-flash}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public ReportAiResult generateReport(ReportAiRequest request) {
        ensureApiKey();
        String prompt = buildReportPrompt(request);
        String text = callGeminiApi(prompt, reportSystemInstruction(), true, 1800);
        JsonNode payload = parseJsonPayloadWithRepair(text);
        int atsMatchScore = extractInt(payload, "atsMatchScore");
        int readinessScore = extractInt(payload, "interviewReadinessScore");
        return new ReportAiResult(
                payload.toString(),
                atsMatchScore,
                readinessScore,
                estimatePromptTokens(request),
                estimateCompletionTokens(text),
                model
        );
    }

    @Override
    public ChatAiResult generateChatReply(ChatAiRequest request) {
        ensureApiKey();
        String text = callGeminiApi(buildChatPrompt(request), chatSystemInstruction(), false, 700);
        return new ChatAiResult(text, estimateChatPromptTokens(request), estimateCompletionTokens(text), model);
    }

    @Override
    public String provider() {
        return "gemini";
    }

    private void ensureApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiProviderException("Gemini API key is missing. Set GEMINI_API_KEY in .env.");
        }
    }

    private String callGeminiApi(String prompt, String systemInstruction, boolean structuredJson, int maxOutputTokens) {
        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", systemInstruction))
            ));
            requestBody.put("contents", List.of(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", prompt))
            )));

            Map<String, Object> generationConfig = new LinkedHashMap<>();
            generationConfig.put("temperature", structuredJson ? 0.1 : 0.4);
            generationConfig.put("maxOutputTokens", maxOutputTokens);
            if (structuredJson) {
                generationConfig.put("responseMimeType", "application/json");
                generationConfig.put("responseSchema", reportSchema());
            }
            requestBody.put("generationConfig", generationConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(generateContentEndpoint()))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestBody),
                            StandardCharsets.UTF_8
                    ))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 300) {
                throw new AiProviderException(extractGeminiError(response.statusCode(), response.body()));
            }

            return extractResponseText(objectMapper.readTree(response.body()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiProviderException("Gemini request was interrupted", ex);
        } catch (IOException ex) {
            throw new AiProviderException("Failed to call Gemini provider", ex);
        }
    }

    private JsonNode parseJsonPayloadWithRepair(String text) {
        try {
            return parseJsonPayload(text);
        } catch (AiProviderException firstFailure) {
            String repairPrompt = """
                    Convert the following response into valid JSON for the InterviewIQ report schema.
                    Return only the corrected JSON object.

                    Response:
                    %s
                    """.formatted(safe(text));
            String repaired = callGeminiApi(repairPrompt, reportSystemInstruction(), true, 1800);
            try {
                return parseJsonPayload(repaired);
            } catch (AiProviderException secondFailure) {
                throw new AiProviderException("Gemini report response was not valid JSON after retry", secondFailure);
            }
        }
    }

    private JsonNode parseJsonPayload(String text) {
        String candidate = stripJson(text);
        try {
            return objectMapper.readTree(candidate);
        } catch (IOException ex) {
            throw new AiProviderException("Gemini report response is not valid JSON", ex);
        }
    }

    private String extractResponseText(JsonNode root) {
        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        String output = extractInteractionsOutput(root);
        if (!output.isBlank()) {
            return output;
        }

        String candidate = extractGenerateContentCandidate(root);
        if (!candidate.isBlank()) {
            return candidate;
        }

        throw new AiProviderException("Gemini response did not contain output text");
    }

    private String extractInteractionsOutput(JsonNode root) {
        StringBuilder builder = new StringBuilder();
        for (JsonNode item : root.path("output")) {
            for (JsonNode content : item.path("content")) {
                JsonNode text = content.path("text");
                if (text.isTextual()) {
                    builder.append(text.asText()).append('\n');
                }
            }
        }
        return builder.toString().trim();
    }

    private String extractGenerateContentCandidate(JsonNode root) {
        StringBuilder builder = new StringBuilder();
        for (JsonNode candidate : root.path("candidates")) {
            for (JsonNode part : candidate.path("content").path("parts")) {
                JsonNode text = part.path("text");
                if (text.isTextual()) {
                    builder.append(text.asText()).append('\n');
                }
            }
        }
        return builder.toString().trim();
    }

    private String extractGeminiError(int statusCode, String body) {
        try {
            JsonNode error = objectMapper.readTree(body).path("error");
            String status = error.path("status").asText("");
            String message = error.path("message").asText("Gemini request failed");
            if ("Gemini request failed".equals(message) && !body.isBlank()) {
                message = body.length() <= 500 ? body : body.substring(0, 500);
            }
            if (statusCode == 401 || statusCode == 403) {
                return status.isBlank()
                        ? "Gemini auth error: " + message
                        : "Gemini auth error (" + status + "): " + message;
            }
            if (statusCode == 429) {
                return "Gemini quota or rate limit was reached. Check your Google AI Studio quota, billing, or retry later.";
            }
            return status.isBlank() ? "Gemini error: " + message : "Gemini error (" + status + "): " + message;
        } catch (IOException ex) {
            return "Gemini provider returned HTTP " + statusCode;
        }
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

    private String reportSystemInstruction() {
        return """
                You are InterviewIQ, an interview preparation assistant.
                Return an accurate, practical report comparing the candidate resume with the job description.
                Scores must be integers from 0 to 100. All list fields must be arrays of concise strings.
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
                truncate(request.resumeText(), 24000),
                truncate(request.jobDescriptionText(), 18000)
        );
    }

    private String chatSystemInstruction() {
        return """
                You are InterviewIQ's report-scoped interview coach.
                Answer using the supplied resume, job description, report JSON, and recent chat turns.
                Be specific, practical, and concise. Do not invent experience the candidate did not provide.
                """;
    }

    private String buildChatPrompt(ChatAiRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Report JSON:\n").append(truncate(request.reportPayloadJson(), 16000)).append("\n\n");
        builder.append("Resume context:\n").append(truncate(request.resumeText(), 10000)).append("\n\n");
        builder.append("Job description context:\n").append(truncate(request.jobDescriptionText(), 10000)).append("\n\n");
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

    private String generateContentEndpoint() {
        String modelPath = model.startsWith("models/") ? model : "models/" + model;
        return baseUrl + "/" + modelPath + ":generateContent?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
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

    private String trimTrailingSlash(String value) {
        String safe = safe(value);
        return safe.endsWith("/") ? safe.substring(0, safe.length() - 1) : safe;
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
