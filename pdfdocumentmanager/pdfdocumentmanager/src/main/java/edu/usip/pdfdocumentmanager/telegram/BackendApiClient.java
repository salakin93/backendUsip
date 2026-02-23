package edu.usip.pdfdocumentmanager.telegram;

import edu.usip.pdfdocumentmanager.dto.request.ChatPdfQuestionRequest;
import edu.usip.pdfdocumentmanager.dto.request.UserRequest;
import edu.usip.pdfdocumentmanager.dto.response.ChatPdfAnswerResponse;
import edu.usip.pdfdocumentmanager.dto.response.ChatPdfUploadResponse;
import edu.usip.pdfdocumentmanager.dto.response.DocumentResponse;
import edu.usip.pdfdocumentmanager.dto.response.UserResponse;
import edu.usip.pdfdocumentmanager.telegram.dto.PageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BackendApiClient {

    private final RestClient rest;

    public BackendApiClient(@Value("${backend.base-url:http://localhost:8080}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public DocumentResponse uploadDocument(
            String jwt,
            String title,
            String author,
            String degree,
            String defenseDate,
            byte[] pdfBytes,
            String fileName
    ) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("title", title);
            body.add("author", author);
            body.add("degree", degree);
            body.add("defenseDate", defenseDate);

            body.add("file", new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });

            return rest.post()
                    .uri("/documents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(DocumentResponse.class);

        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    public PageResponse<DocumentResponse> searchDocuments(String jwt, String title, String author, String degree, int page, int size) {
        try {
            return rest.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/documents")
                                .queryParam("page", page)
                                .queryParam("size", size);

                        if (title != null && !title.isBlank()) b.queryParam("title", title);
                        if (author != null && !author.isBlank()) b.queryParam("author", author);
                        if (degree != null && !degree.isBlank()) b.queryParam("degree", degree);

                        return b.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<DocumentResponse>>() {
                    });
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }


    public UserResponse createUser(String jwt, UserRequest req) {
        try {
            return rest.post()
                    .uri("/admin/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(UserResponse.class);
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    public void disableUser(String jwt, String phone) {
        try {
            rest.delete()
                    .uri("/admin/users/{phone}", phone)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    public byte[] downloadDocumentBytes(String jwt, Long documentId) {
        try {
            return rest.get()
                    .uri("/documents/{id}/download", documentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    public ChatPdfUploadResponse uploadToChatPdf(String jwt, Long documentId) {
        try {
            return rest.post()
                    .uri("/documents/{id}/chatpdf/upload", documentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .body(ChatPdfUploadResponse.class);
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    public ChatPdfAnswerResponse chatWithChatPdf(String jwt, Long documentId, String question, boolean referenceSources) {
        try {
            ChatPdfQuestionRequest req = new ChatPdfQuestionRequest();
            req.setQuestion(question);
            req.setReferenceSources(referenceSources);
            req.setMessages(null); // por ahora sin historial

            return rest.post()
                    .uri("/documents/{id}/chatpdf/chat", documentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(ChatPdfAnswerResponse.class);

        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }


    private RuntimeException translate(RestClientResponseException e) {
        int code = e.getStatusCode().value();
        if (code == 401 || code == 403) {
            return new BackendUnauthorizedException("Unauthorized");
        }
        // deja el mensaje del backend si existe
        String body = e.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            return new RuntimeException(body);
        }
        return new RuntimeException("Backend error HTTP " + code);
    }
}
