package com.search.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String name;
    private String contentType;
    private long size;
    private String category; // NOTE / EXAM
}
