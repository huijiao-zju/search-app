package com.search.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.search.app.model.enums.AttachmentCategory;

@Entity
@Table(name = "resource_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private CourseResource resource;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AttachmentCategory category = AttachmentCategory.NOTE;
}
