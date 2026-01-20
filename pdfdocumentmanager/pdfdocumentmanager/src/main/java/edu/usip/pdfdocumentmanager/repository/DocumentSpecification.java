package edu.usip.pdfdocumentmanager.repository;

import edu.usip.pdfdocumentmanager.model.Document;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification {

    public static Specification<Document> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Document> titleContains(String title) {
        if (title == null || title.isBlank()) return null;
        String like = "%" + title.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), like);
    }

    public static Specification<Document> authorContains(String author) {
        if (author == null || author.isBlank()) return null;
        String like = "%" + author.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("author")), like);
    }

    public static Specification<Document> degreeEquals(String degree) {
        if (degree == null || degree.isBlank()) return null;
        String value = degree.trim();
        return (root, query, cb) -> cb.equal(root.get("degree"), value);
    }
}
