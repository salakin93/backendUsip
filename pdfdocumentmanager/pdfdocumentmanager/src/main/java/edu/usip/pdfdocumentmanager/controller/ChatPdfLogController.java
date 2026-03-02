package edu.usip.pdfdocumentmanager.controller;

import edu.usip.pdfdocumentmanager.dto.response.ChatPdfQuestionLogResponse;
import edu.usip.pdfdocumentmanager.model.ChatPdfQuestionLog;
import edu.usip.pdfdocumentmanager.service.ChatPdfLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports/chatpdf")
public class ChatPdfLogController {

    private final ChatPdfLogService logService;

    // ✅ 1 endpoint
    // ADMIN puede ver todo; STUDENT solo debería ver lo suyo (si quieres lo aplicamos después)
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ChatPdfQuestionLogResponse>> getLogs(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) String userPhone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ChatPdfQuestionLogResponse> resp = logService.search(documentId, userPhone, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(resp);
    }

    private ChatPdfQuestionLogResponse toResponse(ChatPdfQuestionLog log) {
        return ChatPdfQuestionLogResponse.builder()
                .id(log.getId())
                .documentId(log.getDocument().getId())
                .documentTitle(log.getDocument().getTitle())
                .userPhone(log.getUserPhone())
                .userRole(log.getUserRole())
                .question(log.getQuestion())
                .answer(log.getAnswer())
                .referencePages(log.getReferencePages())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
