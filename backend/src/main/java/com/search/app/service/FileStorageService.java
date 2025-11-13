package com.search.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDir;

    public FileStorageService(@Value("${app.storage.dir:uploads}") String storageDir) throws IOException {
        this.storageDir = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(this.storageDir);
    }

    public StoredFile store(MultipartFile file, long maxBytes) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("文件为空");
        }
        if (file.getSize() > maxBytes) {
            throw new IOException("文件超过大小限制");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > 0 && idx < original.length() - 1) {
            ext = original.substring(idx);
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + ext;

        Path target = storageDir.resolve(stored);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("保存文件失败", e);
        }
        return new StoredFile(original, stored, file.getContentType(), file.getSize());
    }

    public Resource loadAsResource(String storedName) throws IOException {
        Path file = storageDir.resolve(storedName).normalize();
        if (!file.startsWith(storageDir) || !Files.exists(file)) {
            throw new NoSuchFileException(storedName);
        }
        return new InputStreamResource(Files.newInputStream(file, StandardOpenOption.READ));
    }

    public void delete(String storedName) throws IOException {
        if (!StringUtils.hasText(storedName)) {
            return;
        }
        Path file = storageDir.resolve(storedName).normalize();
        if (!file.startsWith(storageDir)) {
            throw new IOException("非法文件路径");
        }
        Files.deleteIfExists(file);
    }

    public record StoredFile(String originalName, String storedName, String contentType, long size) {}
}
