package edu.usip.pdfdocumentmanager.repository;

import edu.usip.pdfdocumentmanager.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByIdAndActiveTrue(Long id);

    Optional<Document> findBySourceId(String sourceId);
}