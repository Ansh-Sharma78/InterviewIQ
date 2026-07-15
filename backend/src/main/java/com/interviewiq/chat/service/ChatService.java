package com.interviewiq.chat.service;

import com.interviewiq.ai.client.AiClient;
import com.interviewiq.ai.client.ChatAiRequest;
import com.interviewiq.ai.client.ChatAiResult;
import com.interviewiq.ai.usage.AiUsageLogService;
import com.interviewiq.analysis.entity.Report;
import com.interviewiq.analysis.entity.ReportStatus;
import com.interviewiq.analysis.repository.ReportRepository;
import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.repository.UserRepository;
import com.interviewiq.chat.dto.ChatMessageResponse;
import com.interviewiq.chat.dto.ChatSessionResponse;
import com.interviewiq.chat.dto.CreateChatSessionRequest;
import com.interviewiq.chat.dto.RenameChatSessionRequest;
import com.interviewiq.chat.dto.SendChatMessageRequest;
import com.interviewiq.chat.entity.ChatMessage;
import com.interviewiq.chat.entity.ChatMessageRole;
import com.interviewiq.chat.entity.ChatSession;
import com.interviewiq.chat.repository.ChatMessageRepository;
import com.interviewiq.chat.repository.ChatSessionRepository;
import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.common.exception.DomainException;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;
    private final AiUsageLogService usageLogService;

    public ChatService(ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository, ReportRepository reportRepository, UserRepository userRepository, AiClient aiClient, AiUsageLogService usageLogService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
        this.usageLogService = usageLogService;
    }

    @Transactional
    public ChatSessionResponse createSession(Long userId, CreateChatSessionRequest request) {
        Report report = reportRepository.findByIdAndUserIdAndDeletedAtIsNull(request.reportId(), userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found"));
        if (report.getStatus() != ReportStatus.COMPLETED) {
            throw new DomainException(HttpStatus.CONFLICT, "REPORT_NOT_COMPLETED", "Chat can only start after report generation completes");
        }
        User user = userRepository.getReferenceById(userId);
        ChatSession session = sessionRepository.save(new ChatSession(user, report, "Report #" + report.getId() + " coaching"));
        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChatSessionResponse> listSessions(Long userId, Pageable pageable) {
        Page<ChatSessionResponse> page = sessionRepository.findByUserIdAndDeletedAtIsNull(userId, pageable).map(this::toSessionResponse);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse getSession(Long userId, Long id) {
        return sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(this::toSessionResponse)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "CHAT_SESSION_NOT_FOUND", "Chat session not found"));
    }

    @Transactional
    public ChatSessionResponse rename(Long userId, Long id, RenameChatSessionRequest request) {
        ChatSession session = findSession(userId, id);
        session.rename(request.title().trim());
        return toSessionResponse(session);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        findSession(userId, id).softDelete();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChatMessageResponse> listMessages(Long userId, Long sessionId, Pageable pageable) {
        findSession(userId, sessionId);
        Page<ChatMessageResponse> page = messageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId, pageable).map(this::toMessageResponse);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long sessionId, SendChatMessageRequest request) {
        ChatSession session = findSession(userId, sessionId);
        long existingMessages = messageRepository.countByChatSessionId(sessionId);
        ChatMessage userMessage = messageRepository.save(new ChatMessage(session, ChatMessageRole.USER, request.content().trim()));
        List<ChatAiRequest.ChatTurn> turns = recentTurns(sessionId);
        try {
            Report report = session.getReport();
            boolean firstTurn = existingMessages == 0;
            ChatAiResult result = aiClient.generateChatReply(new ChatAiRequest(
                    report.getPayloadJson(),
                    firstTurn ? report.getResume().getParsedText() : summarize("Resume", report.getResume().getParsedText()),
                    firstTurn ? report.getJobDescription().getRawText() : summarize("Job description", report.getJobDescription().getRawText()),
                    turns,
                    userMessage.getContent()
            ));
            usageLogService.record(userId, "CHAT", aiClient.provider(), result.model(), result.promptTokens(), result.completionTokens(), true, null);
            ChatMessage assistant = messageRepository.save(new ChatMessage(session, ChatMessageRole.ASSISTANT, result.content()));
            return toMessageResponse(assistant);
        } catch (RuntimeException ex) {
            usageLogService.record(userId, "CHAT", aiClient.provider(), "unknown", 0, 0, false, ex.getClass().getSimpleName());
            throw new DomainException(HttpStatus.BAD_GATEWAY, "CHAT_GENERATION_FAILED", "Unable to generate chat reply");
        }
    }

    private ChatSession findSession(Long userId, Long id) {
        return sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "CHAT_SESSION_NOT_FOUND", "Chat session not found"));
    }

    private List<ChatAiRequest.ChatTurn> recentTurns(Long sessionId) {
        List<ChatMessage> messages = messageRepository.findTop8ByChatSessionIdOrderByCreatedAtDesc(sessionId);
        Collections.reverse(messages);
        return messages.stream().map(message -> new ChatAiRequest.ChatTurn(message.getRole().name(), message.getContent())).toList();
    }

    private String summarize(String label, String text) {
        if (text == null || text.isBlank()) {
            return label + " summary unavailable.";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return label + " condensed context: " + (compact.length() <= 1200 ? compact : compact.substring(0, 1200));
    }

    private ChatSessionResponse toSessionResponse(ChatSession session) {
        return new ChatSessionResponse(session.getId(), session.getReport().getId(), session.getTitle(), session.getCreatedAt());
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
