package com.interviewiq.resume.service;

import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.repository.UserRepository;
import com.interviewiq.common.dto.PagedResponse;
import com.interviewiq.common.exception.DomainException;
import com.interviewiq.common.file.FileChecks;
import com.interviewiq.common.pdf.PdfTextExtractor;
import com.interviewiq.resume.dto.ResumeResponse;
import com.interviewiq.resume.dto.ResumeSummaryResponse;
import com.interviewiq.resume.entity.ParseStatus;
import com.interviewiq.resume.entity.Resume;
import com.interviewiq.resume.repository.ResumeRepository;
import com.interviewiq.storage.StorageService;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PdfTextExtractor pdfTextExtractor;

    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository, StorageService storageService, PdfTextExtractor pdfTextExtractor) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    @Transactional
    public ResumeResponse upload(Long userId, MultipartFile file) {
        byte[] content = read(file);
        FileChecks.requirePdf(content);
        String checksum = FileChecks.sha256(content);
        StorageService.StoredFile stored = storageService.store("resumes", file.getOriginalFilename(), content);
        ParsedPdf parsed = parseOrReuse(checksum, content);
        User user = userRepository.getReferenceById(userId);
        Resume resume = resumeRepository.save(new Resume(
                user,
                file.getOriginalFilename(),
                stored.storageKey(),
                stored.sizeBytes(),
                safeMime(file.getContentType()),
                checksum,
                parsed.text(),
                parsed.status()
        ));
        return toResponse(resume);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ResumeSummaryResponse> list(Long userId, Pageable pageable) {
        Page<ResumeSummaryResponse> page = resumeRepository.findByUserIdAndDeletedAtIsNull(userId, pageable).map(this::toSummary);
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional(readOnly = true)
    public ResumeResponse get(Long userId, Long id) {
        return resumeRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "Resume not found"));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Resume resume = resumeRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "Resume not found"));
        resume.softDelete();
    }

    private ParsedPdf parseOrReuse(String checksum, byte[] content) {
        return resumeRepository.findFirstByChecksumAndParseStatusAndParsedTextIsNotNullOrderByCreatedAtDesc(checksum, ParseStatus.SUCCESS)
                .map(existing -> new ParsedPdf(existing.getParsedText(), ParseStatus.SUCCESS))
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

    private ResumeResponse toResponse(Resume resume) {
        return new ResumeResponse(resume.getId(), resume.getOriginalFilename(), resume.getFileSizeBytes(), resume.getMimeType(), resume.getChecksum(), resume.getParseStatus(), resume.getParsedText(), resume.getCreatedAt());
    }

    private ResumeSummaryResponse toSummary(Resume resume) {
        return new ResumeSummaryResponse(resume.getId(), resume.getOriginalFilename(), resume.getFileSizeBytes(), resume.getParseStatus(), preview(resume.getParsedText()), resume.getCreatedAt());
    }

    private String preview(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= 400 ? text : text.substring(0, 400);
    }

    private String safeMime(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/pdf" : contentType;
    }

    private record ParsedPdf(String text, ParseStatus status) {
    }
}

