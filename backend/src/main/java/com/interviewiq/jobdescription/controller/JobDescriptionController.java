package com.interviewiq.jobdescription.controller;

import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.jobdescription.dto.CreateJobDescriptionTextRequest;
import com.interviewiq.jobdescription.dto.JobDescriptionResponse;
import com.interviewiq.jobdescription.dto.JobDescriptionSummaryResponse;
import com.interviewiq.jobdescription.service.JobDescriptionService;
import com.interviewiq.security.userdetails.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/job-descriptions")
public class JobDescriptionController {
    private final JobDescriptionService service;

    public JobDescriptionController(JobDescriptionService service) {
        this.service = service;
    }

    @PostMapping("/text")
    public ResponseEntity<JobDescriptionResponse> createText(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateJobDescriptionTextRequest request) {
        return ResponseEntity.status(201).body(service.createText(user.id(), request));
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobDescriptionResponse> createPdf(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String roleTitle
    ) {
        return ResponseEntity.status(201).body(service.createPdf(user.id(), file, companyName, roleTitle));
    }

    @GetMapping
    public PagedResponse<JobDescriptionSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser user, @PageableDefault(size = 20) Pageable pageable) {
        return service.list(user.id(), pageable);
    }

    @GetMapping("/{id}")
    public JobDescriptionResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return service.get(user.id(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        service.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}

