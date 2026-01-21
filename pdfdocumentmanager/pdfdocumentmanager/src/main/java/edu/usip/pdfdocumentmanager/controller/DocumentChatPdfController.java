
package edu.usip.pdfdocumentmanager.controller;

import edu.usip.pdfdocumentmanager.dto.request.ChatPdfQuestionRequest;
import edu.usip.pdfdocumentmanager.dto.response.ChatPdfAnswerResponse;
import edu.usip.pdfdocumentmanager.dto.response.ChatPdfUploadResponse;
import edu.usip.pdfdocumentmanager.service.ChatPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documents/{id}/chatpdf")
public class DocumentChatPdfController {

    private final ChatPdfService chatPdfService;

    /**
     * Sube el documento a ChatPDF (si ya existe sourceId, lo reutiliza).
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<ChatPdfUploadResponse> upload(@PathVariable("id") Long documentId) {
        String sourceId = chatPdfService.upload(documentId);
        return ResponseEntity.ok(ChatPdfUploadResponse.builder().sourceId(sourceId).build());
    }

    /**
     * Realiza una pregunta al PDF en ChatPDF.
     */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<ChatPdfAnswerResponse> chat(
            @PathVariable("id") Long documentId,
            @Valid @RequestBody ChatPdfQuestionRequest request
    ) {
        return ResponseEntity.ok(chatPdfService.ask(documentId, request));
    }
}
