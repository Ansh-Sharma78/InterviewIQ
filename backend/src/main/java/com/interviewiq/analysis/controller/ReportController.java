package com.interviewiq.analysis.controller;

import com.interviewiq.analysis.dto.GenerateReportRequest;
import com.interviewiq.analysis.dto.ReportResponse;
import com.interviewiq.analysis.dto.ReportSummaryResponse;
import com.interviewiq.analysis.service.ReportService;
import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.security.userdetails.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.accepted().body(reportService.create(user.id(), request));
    }

    @GetMapping("/{id}")
    public ReportResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return reportService.get(user.id(), id);
    }

    @GetMapping
    public PagedResponse<ReportSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser user, @PageableDefault(size = 20) Pageable pageable) {
        return reportService.list(user.id(), pageable);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<ReportResponse> retry(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ResponseEntity.accepted().body(reportService.retry(user.id(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        reportService.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}

