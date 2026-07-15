package com.interviewiq.storage;

public interface StorageService {
    StoredFile store(String folder, String originalFilename, byte[] content);

    record StoredFile(String storageKey, long sizeBytes) {
    }
}

