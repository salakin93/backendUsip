package edu.usip.library.Chat.repository;

import edu.usip.library.Chat.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByDegreeContainingIgnoreCase(String degree);

    List<Document> findByTitleContainingIgnoreCase(String title);

    List<Document> findByAuthorContainingIgnoreCase(String author);

    List<Document> findByDegreeContainingIgnoreCaseAndTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
            String degree, String title, String author);
}