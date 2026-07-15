package com.interviewiq.jobdescription.service;

import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.repository.UserRepository;
import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.common.exception.DomainException;
import com.interviewiq.common.file.FileChecks;
import com.interviewiq.common.pdf.PdfTextExtractor;
import com.interviewiq.jobdescription.dto.CreateJobDescriptionTextRequest;
import com.interviewiq.jobdescription.dto.JobDescriptionResponse;
import com.interviewiq.jobdescription.dto.JobDescriptionSummaryResponse;
import com.interviewiq.jobdescription.entity.JobDescription;
import com.interviewiq.jobdescription.entity.JobDescriptionSourceType;
import com.interviewiq.jobdescription.repository.JobDescriptionRepository;
import com.interviewiq.resume.entity.ParseStatus;
import com.interviewiq.storage.StorageService;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class JobDescriptionService {
    private final JobDescriptionRepository repository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PdfTextExtractor pdfTextExtractor;

    public JobDescriptionService(JobDescriptionRepository repository, UserRepository userRepository, StorageService storageService, PdfTextExtractor pdfTextExtractor) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    @Transactional
    public JobDescriptionResponse createText(Long userId, CreateJobDescriptionTextRequest request) {
        User user = userRepository.getReferenceById(userId);
        JobDescription jd = repository.save(new JobDescription(
                user,
                blankToNull(request.companyName()),
                blankToNull(request.roleTitle()),
                JobDescriptionSourceType.TEXT,
                null,
                null,
                null,
                null,
                null,
                request.rawText().trim(),
                ParseStatus.SUCCESS
        ));
        return toResponse(jd);
    }

    @Transactional
    public JobDescriptionResponse createPdf(Long userId, MultipartFile file, String companyName, String roleTitle) {
        byte[] content = read(file);
        FileChecks.requirePdf(content);
        String checksum = FileChecks.sha256(content);
        StorageService.StoredFile stored = storageService.store("job-descriptions", file.getOriginalFilename(), content);
        ParsedPdf parsed = parseOrReuse(checksum, content);
        User user = userRepository.getReferenceById(userId);
        JobDescription jd = repository.save(new JobDescription(
                user,
                blankToNull(companyName),
                blankToNull(roleTitle),
                JobDescriptionSourceType.PDF,
                file.getOriginalFilename(),
                stored.storageKey(),
                stored.sizeBytes(),
                safeMime(file.getContentType()),
                checksum,
                parsed.text() == null ? "" : parsed.text(),
                parsed.status()
        ));
        return toResponse(jd);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobDescriptionSummaryResponse> list(Long userId, Pageable pageable) {
        Page<JobDescriptionSummaryResponse> page = repository.findByUserIdAndDeletedAtIsNull(userId, pageable).map(this::toSummary);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional(readOnly = true)
    public JobDescriptionResponse get(Long userId, Long id) {
        return repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "JOB_DESCRIPTION_NOT_FOUND", "Job description not found"));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        JobDescription jd = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "JOB_DESCRIPTION_NOT_FOUND", "Job description not found"));
        jd.softDelete();
    }

    private ParsedPdf parseOrReuse(String checksum, byte[] content) {
        return repository.findFirstByChecksumAndParseStatusAndRawTextIsNotNullOrderByCreatedAtDesc(checksum, ParseStatus.SUCCESS)
                .map(existing -> new ParsedPdf(existing.getRawText(), ParseStatus.SUCCESS))
                .orElseGet(() -> parse(content));
    }

    private ParsedPdf parse(byte[] content) {
        try {
            String text = pdfTextExtractor.extract(content);
            if (text == null || text.isBlank()) {
                return new ParsedPdf(null, ParseStatus.FAILED);
            }
            return new ParsedPdf(text, ParseStatus.SUCCESS);
        } catch (IOException ex) {
            return new ParsedPdf(null, ParseStatus.FAILED);
        }
    }

    private byte[] read(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "FILE_REQUIRED", "PDF file is required");
        }
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "FILE_READ_FAILED", "Unable to read uploaded file");
        }
    }

    private JobDescriptionResponse toResponse(JobDescription jd) {
        return new JobDescriptionResponse(jd.getId(), jd.getCompanyName(), jd.getRoleTitle(), jd.getSourceType(), jd.getOriginalFilename(), jd.getFileSizeBytes(), jd.getMimeType(), jd.getChecksum(), jd.getParseStatus(), jd.getRawText(), jd.getCreatedAt());
    }

    private JobDescriptionSummaryResponse toSummary(JobDescription jd) {
        return new JobDescriptionSummaryResponse(jd.getId(), jd.getCompanyName(), jd.getRoleTitle(), jd.getSourceType(), jd.getParseStatus(), preview(jd.getRawText()), jd.getCreatedAt());
    }

    private String preview(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= 400 ? text : text.substring(0, 400);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeMime(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/pdf" : contentType;
    }

    private record ParsedPdf(String text, ParseStatus status) {
    }
}

