package com.interviewiq.storage;

import com.interviewiq.common.exception.DomainException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LocalStorageServiceImpl implements StorageService {
    private final Path root;

    public LocalStorageServiceImpl(@Value("${app.storage.local-root}") String localRoot) {
        this.root = Path.of(localRoot).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(String folder, String originalFilename, byte[] content) {
        try {
            Path directory = root.resolve(folder).normalize();
            Files.createDirectories(directory);
            String safeName = sanitize(originalFilename);
            String key = folder + "/" + UUID.randomUUID() + "-" + safeName;
            Path target = root.resolve(key).normalize();
            if (!target.startsWith(root)) {
                throw new DomainException(HttpStatus.BAD_REQUEST, "INVALID_FILE_NAME", "Invalid file name");
            }
            Files.write(target, content);
            return new StoredFile(key.replace('\\', '/'), content.length);
        } catch (IOException ex) {
            throw new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORAGE_FAILED", "Unable to store uploaded file");
        }
    }

    private String sanitize(String filename) {
        String fallback = filename == null || filename.isBlank() ? "upload.pdf" : filename;
        return fallback.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}

