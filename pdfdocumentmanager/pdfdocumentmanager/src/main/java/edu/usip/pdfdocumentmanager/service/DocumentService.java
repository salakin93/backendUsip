package edu.usip.pdfdocumentmanager.service;

import edu.usip.pdfdocumentmanager.dto.records.DocumentDownload;
import edu.usip.pdfdocumentmanager.dto.request.DocumentUploadRequest;
import edu.usip.pdfdocumentmanager.model.Document;
import edu.usip.pdfdocumentmanager.repository.DocumentRepository;
import edu.usip.pdfdocumentmanager.repository.DocumentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService storageService;
    private final UserService userService;

    @Transactional
    public Document upload(DocumentUploadRequest request, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Archivo PDF es requerido");
        }

        if (file.getSize() > 35L * 1024 * 1024) {
            throw new RuntimeException("El archivo supera los 35MB");
        }

        documentRepository.findBySourceId(request.getSourceId()).ifPresent(d -> {
            throw new RuntimeException("Ya existe un documento con sourceId: " + request.getSourceId());
        });

        String authPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        String createdBy = userService.getActiveUserByPhoneOrThrow(authPhone).getName();

        String storagePath;
        try {
            storagePath = storageService.storeFile(file, request.getSourceId());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }

        Document document = Document.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .degree(request.getDegree())
                .defenseDate(request.getDefenseDate())
                .sourceId(request.getSourceId())
                .fileName(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(file.getSize())
                .createdBy(createdBy)
                .active(true)
                .build();

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Page<Document> search(String title, String author, String degree, Pageable pageable) {

        Specification<Document> spec = Specification.where(DocumentSpecification.isActive());

        Specification<Document> titleSpec = DocumentSpecification.titleContains(title);
        if (titleSpec != null) spec = spec.and(titleSpec);

        Specification<Document> authorSpec = DocumentSpecification.authorContains(author);
        if (authorSpec != null) spec = spec.and(authorSpec);

        Specification<Document> degreeSpec = DocumentSpecification.degreeEquals(degree);
        if (degreeSpec != null) spec = spec.and(degreeSpec);

        return documentRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Document getById(Long id) {
        return documentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(Long id) {
        Document doc = getById(id);

        try {
            Resource resource = storageService.getFile(doc.getStoragePath());
            return new DocumentDownload(resource, doc.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo", e);
        }
    }

    @Transactional
    public void delete(Long id) {
        Document doc = getById(id);
        doc.setActive(false);
        documentRepository.save(doc);
    }
}
