package com.search.app.controller;

import com.search.app.dto.AttachmentResponse;
import com.search.app.dto.PageResponse;
import com.search.app.dto.ResourceResponse;
import com.search.app.model.CourseResource;
import com.search.app.model.ResourceAttachment;
import com.search.app.model.enums.AttachmentCategory;
import com.search.app.repository.CourseResourceRepository;
import com.search.app.repository.ResourceAttachmentRepository;
import com.search.app.service.FileStorageService;
import com.search.app.service.ResourceSearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB per file

    @Autowired
    private CourseResourceRepository resourceRepository;

    @Autowired
    private ResourceAttachmentRepository attachmentRepository;

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private ResourceSearchService searchService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> upload(
            @RequestPart("title") @NotBlank String title,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "categories", required = false) List<String> categories
    ) throws IOException {
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("标题不能为空");
        }
        if (CollectionUtils.isEmpty(files)) {
            return ResponseEntity.badRequest().body("请至少上传一个附件");
        }

        CourseResource resource = new CourseResource();
        resource.setTitle(title.trim());
        resource = resourceRepository.save(resource);

        List<AttachmentResponse> attachmentResponses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileStorageService.StoredFile saved = storageService.store(file, MAX_FILE_SIZE);
            ResourceAttachment att = new ResourceAttachment();
            att.setResource(resource);
            att.setOriginalName(saved.originalName());
            att.setStoredName(saved.storedName());
            att.setContentType(saved.contentType());
            att.setSize(saved.size());
            AttachmentCategory cat = resolveCategory(categories, i);
            att.setCategory(cat);
            attachmentRepository.save(att);

            attachmentResponses.add(new AttachmentResponse(att.getId(), att.getOriginalName(), att.getContentType(), att.getSize(), cat.name()));
        }

        ResourceResponse resp = new ResourceResponse(resource.getId(), resource.getTitle(), resource.getCreatedAt(), attachmentResponses);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public List<ResourceResponse> list() {
        return resourceRepository.findAll().stream().map(r -> new ResourceResponse(
                r.getId(),
                r.getTitle(),
                r.getCreatedAt(),
                r.getAttachments().stream().map(a -> new AttachmentResponse(
                        a.getId(), a.getOriginalName(), a.getContentType(), a.getSize(),
                        (a.getCategory() != null ? a.getCategory().name() : AttachmentCategory.NOTE.name())
                )).collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public PageResponse<ResourceResponse> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "relevance") String sort,
            @RequestParam(value = "mode", defaultValue = "and") String mode
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = switch (sort) {
            case "date" -> PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            case "name" -> PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.ASC, "title"));
            default -> PageRequest.of(pageIndex, size);
        };

        Page<CourseResource> resultPage = searchService.search(q, mode, sort, pageable);

        List<ResourceResponse> content = resultPage.getContent().stream().map(r -> new ResourceResponse(
                r.getId(), r.getTitle(), r.getCreatedAt(),
                r.getAttachments().stream().map(a -> new AttachmentResponse(
                        a.getId(), a.getOriginalName(), a.getContentType(), a.getSize(),
                        (a.getCategory() != null ? a.getCategory().name() : AttachmentCategory.NOTE.name())
                )).collect(Collectors.toList())
        )).collect(Collectors.toList());

        return new PageResponse<>(content, pageIndex + 1, size, resultPage.getTotalElements(), resultPage.getTotalPages());
    }

    private AttachmentCategory resolveCategory(List<String> categories, int index) {
        if (categories == null || categories.isEmpty() || index >= categories.size()) return AttachmentCategory.NOTE;
        String raw = categories.get(index);
        if (raw == null) return AttachmentCategory.NOTE;
        String v = raw.trim().toUpperCase();
        // Accept English keys and common Chinese labels
        if (v.equals("NOTE") || v.equals("STUDY_NOTE") || v.contains("笔记")) return AttachmentCategory.NOTE;
        if (v.equals("EXAM") || v.equals("PAST_PAPER") || v.contains("历年") || v.contains("试卷")) return AttachmentCategory.EXAM;
        return AttachmentCategory.NOTE;
    }

    @GetMapping("/{id}/download/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long attachmentId) throws IOException {
        ResourceAttachment att = attachmentRepository.findById(attachmentId).orElseThrow();
        if (!att.getResource().getId().equals(id)) {
            return ResponseEntity.notFound().build();
        }
        Resource file = storageService.loadAsResource(att.getStoredName());

        String filename = URLEncoder.encode(att.getOriginalName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(att.getContentType() != null ? MediaType.parseMediaType(att.getContentType()) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentLength(att.getSize())
                .body(file);
    }
}
