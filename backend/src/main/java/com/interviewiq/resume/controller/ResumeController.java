package com.interviewiq.resume.controller;

import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.resume.dto.ResumeResponse;
import com.interviewiq.resume.dto.ResumeSummaryResponse;
import com.interviewiq.resume.service.ResumeService;
import com.interviewiq.security.userdetails.AuthenticatedUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> upload(@AuthenticationPrincipal AuthenticatedUser user, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(201).body(resumeService.upload(user.id(), file));
    }

    @GetMapping
    public PagedResponse<ResumeSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser user, @PageableDefault(size = 20) Pageable pageable) {
        return resumeService.list(user.id(), pageable);
    }

    @GetMapping("/{id}")
    public ResumeResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return resumeService.get(user.id(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        resumeService.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}

