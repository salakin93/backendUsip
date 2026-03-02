package edu.usip.pdfdocumentmanager.service;

import edu.usip.pdfdocumentmanager.model.ChatPdfQuestionLog;
import edu.usip.pdfdocumentmanager.repository.ChatPdfQuestionLogRepository;
import edu.usip.pdfdocumentmanager.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatPdfLogService {

    private final ChatPdfQuestionLogRepository logRepository;
    private final DocumentRepository documentRepository;

    /**
     * ✅ Guarda auditoría en una transacción independiente.
     * Si esto falla, NO debe romper la operación de chat.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLogRequiresNew(Long documentId, String question, String answer, Collection<Integer> pages) {

        var document = documentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado para auditoría"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth != null ? auth.getName() : "unknown";

        // Authorities típicos: ROLE_ADMIN / ROLE_STUDENT
        String role = (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()
                : "unknown";

        String pagesStr = (pages == null || pages.isEmpty())
                ? null
                : pages.stream().map(String::valueOf).collect(Collectors.joining(","));

        ChatPdfQuestionLog log = ChatPdfQuestionLog.builder()
                .document(document)
                .userPhone(phone)
                .userRole(role)
                .question(question)
                .answer(answer)
                .referencePages(pagesStr)
                .build();

        logRepository.save(log);
    }

    /**
     * (Opcional) Si quieres mantener este método para otros usos.
     * Yo lo dejaría, pero evita llamarlo desde ask(). Usa saveLogRequiresNew().
     */
    @Transactional
    public void saveLog(edu.usip.pdfdocumentmanager.model.Document document, String question, String answer, Collection<Integer> pages) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth != null ? auth.getName() : "unknown";

        String role = (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()
                : "unknown";

        String pagesStr = (pages == null || pages.isEmpty())
                ? null
                : pages.stream().map(String::valueOf).collect(Collectors.joining(","));

        ChatPdfQuestionLog log = ChatPdfQuestionLog.builder()
                .document(document)
                .userPhone(phone)
                .userRole(role)
                .question(question)
                .answer(answer)
                .referencePages(pagesStr)
                .build();

        logRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ChatPdfQuestionLog> search(Long documentId, String userPhone, Pageable pageable) {

        if (documentId == null && (userPhone == null || userPhone.isBlank())) {
            return logRepository.findAll(pageable);
        }

        return logRepository.findAll((root, query, cb) -> {
            var p = cb.conjunction();

            if (documentId != null) {
                p = cb.and(p, cb.equal(root.get("document").get("id"), documentId));
            }
            if (userPhone != null && !userPhone.isBlank()) {
                p = cb.and(p, cb.equal(root.get("userPhone"), userPhone.trim()));
            }

            return p;
        }, pageable);
    }
}