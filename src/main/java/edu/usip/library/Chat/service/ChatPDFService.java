package edu.usip.library.Chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatPDFService {

    @Value("${chatpdf.api.key}")
    private String apiKey;

    @Value("${chatpdf.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ChatPDFService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object processPDF(MultipartFile file) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/sources/add-file", HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }

    public Object queryPDF(String question, String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", question);

        Map<String, Object> body = new HashMap<>();
        body.put("stream", true);
        body.put("sourceId", sessionId);
        body.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/chats/message", HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    public Object deletePDF(String documentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        List<String> books = new ArrayList<>();
        books.add(documentId);
        Map<String, Object> body = new HashMap<>();
        body.put("sources", books);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/sources/delete/", HttpMethod.POST, entity, String.class);

        return response.getBody();
    }
}