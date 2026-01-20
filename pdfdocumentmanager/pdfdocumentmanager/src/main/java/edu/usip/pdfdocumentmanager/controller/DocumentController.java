package edu.usip.pdfdocumentmanager.controller;

import edu.usip.pdfdocumentmanager.dto.records.DocumentDownload;
import edu.usip.pdfdocumentmanager.dto.request.DocumentUploadRequest;
import edu.usip.pdfdocumentmanager.dto.response.DocumentResponse;
import edu.usip.pdfdocumentmanager.model.Document;
import edu.usip.pdfdocumentmanager.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Subir documento PDF", description = "Permite a un ADMIN subir un documento PDF con metadata")
    public ResponseEntity<DocumentResponse> upload(
            @Parameter(description = "Datos del documento") @Valid @ModelAttribute DocumentUploadRequest request,
            @Parameter(description = "Archivo PDF") @RequestParam("file") MultipartFile file
    ) {
        Document document = documentService.upload(request, file);
        return ResponseEntity.ok(toResponse(document));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @Operation(summary = "Buscar documentos", description = "Busca documentos por título, autor o degree. Paginable y ordenable.")
    public ResponseEntity<Page<DocumentResponse>> search(
            @Parameter(description = "Título del documento") @RequestParam(required = false) String title,
            @Parameter(description = "Autor del documento") @RequestParam(required = false) String author,
            @Parameter(description = "Carrera o degree") @RequestParam(required = false) String degree,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DocumentResponse> response = documentService.search(title, author, degree, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @Operation(summary = "Obtener metadata", description = "Obtiene la metadata de un documento por ID")
    public ResponseEntity<DocumentResponse> getById(
            @Parameter(description = "ID del documento") @PathVariable Long id) {
        Document document = documentService.getById(id);
        return ResponseEntity.ok(toResponse(document));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @Operation(summary = "Descargar documento PDF", description = "Descarga el archivo PDF del documento por ID")
    public ResponseEntity<Resource> download(
            @Parameter(description = "ID del documento") @PathVariable Long id
    ) {
        DocumentDownload download = documentService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.fileName() + "\"")
                .body(download.resource());
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .author(document.getAuthor())
                .degree(document.getDegree())
                .defenseDate(document.getDefenseDate())
                .sourceId(document.getSourceId())
                .fileName(document.getFileName())
                .downloadUrl("/documents/" + document.getId() + "/download")
                .size(document.getSize())
                .build();
    }
}
