package edu.usip.library.Chat.controller;

import edu.usip.library.Chat.dto.ErrorResponseDTO;
import edu.usip.library.Chat.model.Document;
import edu.usip.library.Chat.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document")
//@Tag(name = "Document Controller", description = "Controlador para la gestión de documentos y tesis")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/search")
    @Operation(
            tags = {"Document Management"},
            summary = "Buscar tesis",
            description = "Permite buscar documentos por grado académico, título o autor. Si no se proporcionan parámetros, se realiza una búsqueda combinada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Document.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            }),
            @ApiResponse(responseCode = "404", description = "No documents found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            })
    })
    public List<Document> buscarTesis(
            @Parameter(description = "Resumen del documento")
            @RequestParam(value = "summary", required = false) String summary,

            @Parameter(description = "Título del documento")
            @RequestParam(value = "title", required = false) String title) {

        if (summary == null && title == null) {
            throw new IllegalArgumentException("Debe proporcionar al menos uno de los parámetros: summary o title.");
        }
        if (summary != null && title != null) {
            throw new IllegalArgumentException("Solo puede proporcionar uno de los parámetros: summary o title.");
        }
        return summary != null ? documentService.findBySummary(summary) : documentService.findByTitle(title);
    }
}
