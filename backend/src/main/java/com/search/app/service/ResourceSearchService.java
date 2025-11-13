package com.search.app.service;

import com.search.app.model.CourseResource;
import com.search.app.model.ResourceAttachment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ResourceSearchService {

    @PersistenceContext
    private EntityManager em;

    public Page<CourseResource> search(String q, String mode, String sort, Pageable pageable) {
        List<String> tokens = tokenize(q);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Main query
        CriteriaQuery<CourseResource> cq = cb.createQuery(CourseResource.class);
        Root<CourseResource> root = cq.from(CourseResource.class);
        root.join("attachments", JoinType.LEFT);
        cq.distinct(true);

        Predicate where = buildPredicate(cb, root, tokens, mode);
        cq.where(where);

        // Order by
        List<Order> orders = new ArrayList<>();
        if ("name".equalsIgnoreCase(sort)) {
            orders.add(cb.asc(root.get("title")));
        } else if ("date".equalsIgnoreCase(sort)) {
            orders.add(cb.desc(root.get("createdAt")));
        } else { // relevance
            // CASE WHEN title matches any token THEN 0 ELSE 1 END, createdAt DESC
            Expression<Integer> caseExpr = cb.<Integer>selectCase()
            .when(titleAnyMatch(cb, root, tokens), 0)
            .otherwise(1);
            orders.add(cb.asc(caseExpr));
            orders.add(cb.desc(root.get("createdAt")));
        }
        cq.orderBy(orders);

        TypedQuery<CourseResource> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<CourseResource> content = query.getResultList();

        // Count query
        CriteriaQuery<Long> ccq = cb.createQuery(Long.class);
        Root<CourseResource> countRoot = ccq.from(CourseResource.class);
        countRoot.join("attachments", JoinType.LEFT);
        ccq.select(cb.countDistinct(countRoot));
        ccq.where(buildPredicate(cb, countRoot, tokens, mode));
        Long total = em.createQuery(ccq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private List<String> tokenize(String q) {
        if (!StringUtils.hasText(q)) return List.of();
        return Arrays.stream(q.trim().split("\\s+"))
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .toList();
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<CourseResource> root, List<String> tokens, String mode) {
        if (tokens.isEmpty()) {
            return cb.conjunction();
        }
        List<Predicate> tokenPreds = new ArrayList<>();
        Path<String> title = root.get("title");
        Join<CourseResource, ResourceAttachment> att = root.join("attachments", JoinType.LEFT);
        Path<String> originalName = att.get("originalName");

        for (String t : tokens) {
            String like = "%" + t + "%";
            Predicate titleLike = cb.like(cb.lower(title), like);
            Predicate nameLike = cb.like(cb.lower(originalName), like);
            tokenPreds.add(cb.or(titleLike, nameLike));
        }

        boolean and = !"or".equalsIgnoreCase(mode);
        return and ? cb.and(tokenPreds.toArray(new Predicate[0])) : cb.or(tokenPreds.toArray(new Predicate[0]));
    }

    private Expression<Boolean> titleAnyMatch(CriteriaBuilder cb, Root<CourseResource> root, List<String> tokens) {
        if (tokens.isEmpty()) return cb.literal(false);
        Path<String> title = root.get("title");
        List<Predicate> likes = new ArrayList<>();
        for (String t : tokens) {
            likes.add(cb.like(cb.lower(title), "%" + t + "%"));
        }
        return cb.or(likes.toArray(new Predicate[0]));
    }
}

