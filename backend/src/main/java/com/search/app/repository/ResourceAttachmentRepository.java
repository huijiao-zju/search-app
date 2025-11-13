package com.search.app.repository;

import com.search.app.model.ResourceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceAttachmentRepository extends JpaRepository<ResourceAttachment, Long> {
}

