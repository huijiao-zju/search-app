package com.search.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String title;
    private String college;
    private String uploaderName;
    private Instant createdAt;
    private List<AttachmentResponse> attachments;
}
