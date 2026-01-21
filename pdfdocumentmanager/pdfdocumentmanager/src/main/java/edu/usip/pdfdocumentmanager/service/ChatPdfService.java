package edu.usip.pdfdocumentmanager.service;

import edu.usip.pdfdocumentmanager.config.ChatPdfProperties;
import edu.usip.pdfdocumentmanager.dto.request.ChatPdfMessage;
import edu.usip.pdfdocumentmanager.dto.request.ChatPdfQuestionRequest;
import edu.usip.pdfdocumentmanager.dto.response.ChatPdfAnswerResponse;
import edu.usip.pdfdocumentmanager.dto.response.ChatPdfReference;
import edu.usip.pdfdocumentmanager.model.Document;
import edu.usip.pdfdocumentmanager.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatPdfService {

    private final ChatPdfProperties props;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final FileStorageService storageService;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-api-key", props.getApiKey())
                .build();
    }

    /**
     * Asegura que el documento tenga chatPdfSourceId (sube el PDF a ChatPDF si es necesario).
     */
    @Transactional
    public String ensureSourceId(Long documentId) {
        Document doc = documentService.getById(documentId);

        if (doc.getChatPdfSourceId() != null && !doc.getChatPdfSourceId().isBlank()) {
            return doc.getChatPdfSourceId();
        }

        String sourceId = uploadToChatPdf(doc);

        doc.setChatPdfSourceId(sourceId);
        documentRepository.save(doc);

        return sourceId;
    }

    @Transactional
    public String upload(Long documentId) {
        return ensureSourceId(documentId);
    }

    /**
     * Envía una pregunta (y opcional historial) a ChatPDF.
     */
    @Transactional(readOnly = true)
    public ChatPdfAnswerResponse ask(Long documentId, ChatPdfQuestionRequest request) {
        Document doc = documentService.getById(documentId);
        String sourceId = doc.getChatPdfSourceId();

        if (sourceId == null || sourceId.isBlank()) {
            sourceId = ensureSourceId(documentId);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("sourceId", sourceId);
        payload.put("referenceSources", request.isReferenceSources());

        List<Map<String, String>> messages = buildMessages(request);
        payload.put("messages", messages);

        ChatPdfChatApiResponse apiResponse = restClient()
                .post()
                .uri("/chats/message")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(ChatPdfChatApiResponse.class);

        if (apiResponse == null || apiResponse.getContent() == null) {
            throw new RuntimeException("Respuesta vacía de ChatPDF");
        }

        return ChatPdfAnswerResponse.builder()
                .answer(apiResponse.getContent())
                .references(apiResponse.getReferences() == null ? List.of() : apiResponse.getReferences())
                .build();
    }

    private List<Map<String, String>> buildMessages(ChatPdfQuestionRequest request) {
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            List<Map<String, String>> out = new ArrayList<>();
            for (ChatPdfMessage m : request.getMessages()) {
                out.add(Map.of("role", m.getRole(), "content", m.getContent()));
            }
            boolean hasUserQuestion = request.getMessages().stream()
                    .anyMatch(m -> "user".equalsIgnoreCase(m.getRole()) && Objects.equals(m.getContent(), request.getQuestion()));
            if (!hasUserQuestion) {
                out.add(Map.of("role", "user", "content", request.getQuestion()));
            }
            if (out.size() > 6) {
                out = out.subList(out.size() - 6, out.size());
            }
            return out;
        }

        return List.of(Map.of("role", "user", "content", request.getQuestion()));
    }

    private String uploadToChatPdf(Document doc) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new RuntimeException("chatpdf.api-key no configurado");
        }

        Resource pdfResource;
        try {
            pdfResource = storageService.getFile(doc.getStoragePath());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el PDF desde el storage", e);
        }

        byte[] bytes;
        try {
            bytes = pdfResource.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar bytes del PDF", e);
        }

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new NamedByteArrayResource(bytes, safeFileName(doc.getFileName())));

        ChatPdfUploadApiResponse response = restClient()
                .post()
                .uri("/sources/add-file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(ChatPdfUploadApiResponse.class);

        if (response == null || response.getSourceId() == null || response.getSourceId().isBlank()) {
            throw new RuntimeException("ChatPDF no devolvió sourceId");
        }

        return response.getSourceId();
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "document.pdf";
        return fileName.replaceAll("[\\\\/\\r\\n\\t\"]", "_");
    }

    /**
     * Helper para multipart con nombre de archivo.
     */
    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        public NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }

    /**
     * Modelos internos para deserializar la respuesta de ChatPDF.
     */
    @lombok.Getter
    @lombok.Setter
    private static class ChatPdfUploadApiResponse {
        private String sourceId;
    }

    @lombok.Getter
    @lombok.Setter
    private static class ChatPdfChatApiResponse {
        private String content;
        private List<ChatPdfReference> references;
    }
}
