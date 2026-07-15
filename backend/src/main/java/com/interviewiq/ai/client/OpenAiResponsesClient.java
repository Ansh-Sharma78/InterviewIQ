package com.interviewiq.ai.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiResponsesClient implements AiClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public OpenAiResponsesClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.openai.api-key}") String apiKey,
            @Value("${app.ai.openai.base-url}") String baseUrl,
            @Value("${app.ai.openai.model}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
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
        String text = callResponsesApi(prompt, 1300);
        JsonNode payload = parseJsonPayload(text);
        int atsMatchScore = extractInt(payload, "atsMatchScore");
        int readinessScore = extractInt(payload, "interviewReadinessScore");
        return new ReportAiResult(payload.toString(), atsMatchScore, readinessScore,
                estimatePromptTokens(request), estimateCompletionTokens(text), model);
    }

    @Override
    public ChatAiResult generateChatReply(ChatAiRequest request) {
        ensureApiKey();
        String prompt = buildChatPrompt(request);
        String text = callResponsesApi(prompt, 500);
        return new ChatAiResult(text, estimateChatPromptTokens(request), estimateCompletionTokens(text), model);
    }

    @Override
    public String provider() {
        return "openai";
    }

    private void ensureApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiProviderException("OpenAI API key is missing");
        }
    }

    private String buildReportPrompt(ReportAiRequest request) {
        return "Generate a JSON-only report payload for interview preparation. " +
                "Do not include any explanation outside the JSON object. " +
                "Use the following fields exactly: finalSummary, atsMatchScore, interviewReadinessScore, " +
                "missingSkills, skillGapAnalysis, strengths, weaknesses, resumeImprovementSuggestions, " +
                "missingKeywords, resumeRewriteSuggestions, technicalInterviewQuestions, behavioralQuestions, " +
                "projectBasedQuestions, systemDesignQuestions, hrQuestions, salaryNegotiationTips, " +
                "twoWeekPreparationPlan, learningResources. " +
                "Provide arrays for all list fields and integer scores from 0 to 100. " +
                "Resume text: " + safe(request.resumeText()) + "\n" +
                "Job description text: " + safe(request.jobDescriptionText()) + "\n" +
                "Company name: " + safe(request.companyName()) + "\n" +
                "Role title: " + safe(request.roleTitle()) + "\n" +
                "Output must be valid JSON with no surrounding markdown or comments.";
    }

    private String buildChatPrompt(ChatAiRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a helpful interview coach. Reply using only plain text. ");
        builder.append("Use the report payload and recent conversation to answer the user request.\n");
        builder.append("Report payload JSON: ").append(safe(request.reportPayloadJson())).append("\n");
        builder.append("Resume text: ").append(safe(request.resumeText())).append("\n");
        builder.append("Job description text: ").append(safe(request.jobDescriptionText())).append("\n");
        if (request.recentTurns() != null && !request.recentTurns().isEmpty()) {
            builder.append("Conversation history:\n");
            for (ChatAiRequest.ChatTurn turn : request.recentTurns()) {
                builder.append(turn.role()).append(": ").append(turn.content()).append("\n");
            }
        }
        builder.append("User question: ").append(safe(request.userMessage()));
        return builder.toString();
    }

    private String callResponsesApi(String prompt, int maxOutputTokens) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "input", prompt,
                    "temperature", 0.2,
                    "max_output_tokens", maxOutputTokens
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/responses"))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 300) {
                throw new AiProviderException("OpenAI provider returned " + response.statusCode() + ": " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            return extractResponseText(root);
        } catch (IOException | InterruptedException ex) {
            throw new AiProviderException("Failed to call OpenAI provider", ex);
        }
    }

    private String extractResponseText(JsonNode root) {
        if (root.has("output") && root.get("output").isArray()) {
            for (JsonNode output : root.get("output")) {
                if (output.has("content") && output.get("content").isArray()) {
                    for (JsonNode content : output.get("content")) {
                        if (content.has("text")) {
                            return safe(content.get("text").asText());
                        }
                        if (content.has("markdown")) {
                            return safe(content.get("markdown").asText());
                        }
                    }
                }
            }
        }
        if (root.has("error")) {
            JsonNode error = root.get("error");
            throw new AiProviderException("OpenAI provider error: " + safe(error.toString()));
        }
        throw new AiProviderException("OpenAI response did not contain expected output");
    }

    private JsonNode parseJsonPayload(String text) {
        String candidate = stripJson(text);
        try {
            return objectMapper.readTree(candidate);
        } catch (IOException ex) {
            throw new AiProviderException("OpenAI report response is not valid JSON: " + text, ex);
        }
    }

    private String stripJson(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.lastIndexOf("```");
            if (end > start) {
                trimmed = trimmed.substring(start, end).trim();
            }
        }
        int first = trimmed.indexOf('{');
        int last = trimmed.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return trimmed.substring(first, last + 1);
        }
        return trimmed;
    }

    private int extractInt(JsonNode payload, String field) {
        if (payload.has(field) && payload.get(field).canConvertToInt()) {
            return payload.get(field).asInt();
        }
        return 0;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int estimatePromptTokens(ReportAiRequest request) {
        return Math.max(1, (safe(request.resumeText()).length() + safe(request.jobDescriptionText()).length()) / 4);
    }

    private int estimateChatPromptTokens(ChatAiRequest request) {
        int length = safe(request.reportPayloadJson()).length() + safe(request.resumeText()).length() + safe(request.jobDescriptionText()).length() + safe(request.userMessage()).length();
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
