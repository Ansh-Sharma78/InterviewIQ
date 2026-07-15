package com.interviewiq.chat.controller;

import com.interviewiq.chat.dto.ChatMessageResponse;
import com.interviewiq.chat.dto.ChatSessionResponse;
import com.interviewiq.chat.dto.CreateChatSessionRequest;
import com.interviewiq.chat.dto.RenameChatSessionRequest;
import com.interviewiq.chat.dto.SendChatMessageRequest;
import com.interviewiq.chat.service.ChatService;
import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.security.userdetails.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createSession(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateChatSessionRequest request) {
        return ResponseEntity.status(201).body(chatService.createSession(user.id(), request));
    }

    @GetMapping("/sessions")
    public PagedResponse<ChatSessionResponse> listSessions(@AuthenticationPrincipal AuthenticatedUser user, @PageableDefault(size = 20) Pageable pageable) {
        return chatService.listSessions(user.id(), pageable);
    }

    @GetMapping("/sessions/{id}")
    public ChatSessionResponse getSession(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return chatService.getSession(user.id(), id);
    }

    @PatchMapping("/sessions/{id}")
    public ChatSessionResponse rename(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id, @Valid @RequestBody RenameChatSessionRequest request) {
        return chatService.rename(user.id(), id, request);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        chatService.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{id}/messages")
    public PagedResponse<ChatMessageResponse> messages(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id, @PageableDefault(size = 50) Pageable pageable) {
        return chatService.listMessages(user.id(), id, pageable);
    }

    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ChatMessageResponse> send(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id, @Valid @RequestBody SendChatMessageRequest request) {
        return ResponseEntity.status(201).body(chatService.sendMessage(user.id(), id, request));
    }
}

