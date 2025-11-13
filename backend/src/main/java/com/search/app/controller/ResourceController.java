package com.search.app.controller;

import com.search.app.dto.AttachmentResponse;
import com.search.app.dto.PageResponse;
import com.search.app.dto.ResourceResponse;
import com.search.app.model.CourseResource;
import com.search.app.model.ResourceAttachment;
import com.search.app.model.enums.AttachmentCategory;
import com.search.app.repository.CourseResourceRepository;
import com.search.app.repository.ResourceAttachmentRepository;
import com.search.app.repository.UserRepository;
import com.search.app.service.FileStorageService;
import com.search.app.service.ResourceSearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB per file
    private static final String SUPER_DELETER = "testqwq";

    @Autowired
    private CourseResourceRepository resourceRepository;

    @Autowired
    private ResourceAttachmentRepository attachmentRepository;

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private ResourceSearchService searchService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> upload(
            @RequestPart("title") @NotBlank String title,
            @RequestPart(value = "college", required = false) String college,
            @RequestPart(value = "type", required = false) String type,
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
        if (college != null) {
            resource.setCollege(college.trim());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByUsername(authentication.getName()).ifPresent(resource::setUploader);
        }

        resource = resourceRepository.save(resource);

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
            if (cat == null) {
                cat = resolveType(type);
            }
            att.setCategory(cat);
            attachmentRepository.save(att);
            resource.getAttachments().add(att);
        }

        ResourceResponse resp = toResponse(resource);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public List<ResourceResponse> list() {
        return resourceRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

        List<ResourceResponse> content = resultPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, pageIndex + 1, size, resultPage.getTotalElements(), resultPage.getTotalPages());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        String username = authentication.getName();
        if (!SUPER_DELETER.equalsIgnoreCase(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权限删除该资源");
        }

        CourseResource resource = resourceRepository.findById(id).orElse(null);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        if (resource.getAttachments() != null) {
            for (ResourceAttachment attachment : resource.getAttachments()) {
                storageService.delete(attachment.getStoredName());
            }
        }
        resourceRepository.delete(resource);
        return ResponseEntity.noContent().build();
    }

    private AttachmentCategory resolveCategory(List<String> categories, int index) {
        if (categories == null || categories.isEmpty() || index >= categories.size()) return null;
        String raw = categories.get(index);
        if (raw == null) return null;
        String v = raw.trim().toUpperCase();
        // Accept English keys and common Chinese labels
        if (v.equals("NOTE") || v.equals("STUDY_NOTE") || v.contains("笔记")) return AttachmentCategory.NOTE;
        if (v.equals("EXAM") || v.equals("PAST_PAPER") || v.contains("历年") || v.contains("试卷") || v.contains("回忆")) return AttachmentCategory.EXAM;
        return null;
    }

    private AttachmentCategory resolveType(String type) {
        if (type == null) return AttachmentCategory.NOTE;
        String v = type.trim().toUpperCase();
        if (v.equals("NOTE") || v.contains("笔记")) return AttachmentCategory.NOTE;
        if (v.equals("EXAM") || v.contains("回忆") || v.contains("历年") || v.contains("试卷")) return AttachmentCategory.EXAM;
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

    private ResourceResponse toResponse(CourseResource resource) {
        List<AttachmentResponse> attachmentResponses = resource.getAttachments().stream()
                .map(a -> new AttachmentResponse(
                        a.getId(),
                        a.getOriginalName(),
                        a.getContentType(),
                        a.getSize(),
                        (a.getCategory() != null ? a.getCategory().name() : AttachmentCategory.NOTE.name())
                ))
                .collect(Collectors.toList());
        String uploaderName = resource.getUploader() != null ? resource.getUploader().getUsername() : null;
        return new ResourceResponse(resource.getId(), resource.getTitle(), resource.getCollege(), uploaderName, resource.getCreatedAt(), attachmentResponses);
    }
}
