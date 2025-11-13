package com.search.app.repository;

import com.search.app.model.CourseResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {

    @Query(value = "select distinct r from CourseResource r left join r.attachments a " +
            "where lower(r.title) like lower(concat('%', :q, '%')) " +
            "or lower(a.originalName) like lower(concat('%', :q, '%'))",
           countQuery = "select count(distinct r) from CourseResource r left join r.attachments a " +
                   "where lower(r.title) like lower(concat('%', :q, '%')) " +
                   "or lower(a.originalName) like lower(concat('%', :q, '%'))")
    Page<CourseResource> search(@Param("q") String q, Pageable pageable);

    @Query(value = "select distinct r from CourseResource r left join r.attachments a " +
            "where lower(r.title) like lower(concat('%', :q, '%')) " +
            "or lower(a.originalName) like lower(concat('%', :q, '%')) " +
            "order by case when lower(r.title) like lower(concat('%', :q, '%')) then 0 else 1 end, r.createdAt desc",
           countQuery = "select count(distinct r) from CourseResource r left join r.attachments a " +
                   "where lower(r.title) like lower(concat('%', :q, '%')) " +
                   "or lower(a.originalName) like lower(concat('%', :q, '%'))")
    Page<CourseResource> searchRelevance(@Param("q") String q, Pageable pageable);
}
