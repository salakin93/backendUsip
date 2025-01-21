package edu.usip.library.Chat.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatPDFService {

    private static final Logger logger = LoggerFactory.getLogger(ChatPDFService.class);
    private final RestTemplate restTemplate;
    @Value("${chatpdf.api.key}")
    private String apiKey;
    @Value("${chatpdf.api.url}")
    private String apiUrl;
    @Value("${storage.directory}")
    private String storageDirectory;

    public ChatPDFService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object processPDF(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(storageDirectory, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

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

        String pdfUrl = "http://localhost:8082/api/pdf/view?fileName=" + fileName;

        String sourceId = extractSourceId(response.getBody());

        Map<String, Object> result = new HashMap<>();
        result.put("pdfUrl", pdfUrl);
        result.put("sourceId", sourceId);

        return result;
    }

    public Object addPdfUrl(String pdfUrl) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("url", pdfUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                apiUrl + "/sources/add-url", HttpMethod.POST, entity, Object.class);

        return response.getBody();
    }

    private String extractSourceId(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getString("sourceId");
        } catch (JSONException e) {
            logger.error("Error al procesar la respuesta del servicio externo: {}", responseBody, e);
            return null;
        }
    }

    public Object queryPDF(String question, String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", question);

        Map<String, Object> body = new HashMap<>();
        body.put("referenceSources", true);
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