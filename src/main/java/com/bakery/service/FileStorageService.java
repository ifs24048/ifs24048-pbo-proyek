package com.bakery.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService() {
        this.uploadDir = Paths.get("src/main/resources/static/uploads");
        try {
            createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    // Protected method for testability
    protected void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        return "/uploads/" + fileName;
    }

    public boolean deleteFile(String filePath) {
        try {
            if (filePath != null && filePath.startsWith("/uploads/")) {
                String fileName = filePath.substring("/uploads/".length());
                Path path = uploadDir.resolve(fileName);
                return deleteIfExists(path);
            }
            return false;
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            return false;
        }
    }

    // Protected method for testability
    protected boolean deleteIfExists(Path path) throws IOException {
        return Files.deleteIfExists(path);
    }

    public boolean isImageFile(MultipartFile file) {
        if (file == null)
            return false;

        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.startsWith("image/jpeg") ||
                        contentType.startsWith("image/png") ||
                        contentType.startsWith("image/gif"));
    }
}