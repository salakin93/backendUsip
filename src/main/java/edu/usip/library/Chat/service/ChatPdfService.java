package edu.usip.library.Chat.service;

import edu.usip.library.Chat.dto.model.ReferenceDTO;
import edu.usip.library.Chat.dto.response.ChatPdfResponseDTO;
import edu.usip.library.Chat.dto.response.DeleteResponseDTO;
import edu.usip.library.Chat.dto.response.SourceIdResponseDTO;
import edu.usip.library.Chat.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ChatPdfService {

    private static final Logger logger = LoggerFactory.getLogger(ChatPdfService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final QuestionService questionService;

    public ChatPdfService(RestTemplate restTemplate, @Value("${chatpdf.api.key}") String apiKey, @Value("${chatpdf.api.url}") String baseUrl, QuestionService questionService) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.questionService = questionService;
    }

    public SourceIdResponseDTO addFile(File file) {
        String url = baseUrl + "sources/add-file";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        logger.info("Enviando archivo a URL: {}", url);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        logger.info("Respuesta recibida: {}", response.getStatusCode());

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            logger.info("Archivo subido con éxito, sourceId: {}", response.getBody().get("sourceId"));
            return SourceIdResponseDTO.builder()
                    .sourceId((String) response.getBody().get("sourceId"))
                    .build();
        } else {
            logger.error("Error al subir archivo a ChatPDF, código de estado: {}", response.getStatusCode());
            throw new RuntimeException("Error al subir archivo a ChatPDF");
        }
    }

    public SourceIdResponseDTO addUrl(String url) {
        String urlEndpoint = baseUrl + "/sources/add-url";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"url\": \"" + url + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        logger.info("Enviando URL a ChatPDF: {}", urlEndpoint);

        ResponseEntity<SourceIdResponseDTO> response = restTemplate.exchange(
                urlEndpoint, HttpMethod.POST, requestEntity, SourceIdResponseDTO.class);

        logger.info("Respuesta recibida: {}", response.getStatusCode());

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("URL agregada con éxito, sourceId: {}", response.getBody().getSourceId());
            return response.getBody();
        } else {
            logger.error("Error al agregar URL a ChatPDF, código de estado: {}", response.getStatusCode());
            throw new RuntimeException("Error al agregar URL a ChatPDF");
        }
    }

    public ChatPdfResponseDTO sendMessage(String sourceId, String message) {
        String url = baseUrl + "chats/message";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "referenceSources", true,
                "sourceId", sourceId,
                "messages", List.of(
                        Map.of("role", "user", "content", message)
                )
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        logger.info("Enviando mensaje a ChatPDF para sourceId: {}", sourceId);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Question question = Question.builder()
                .request(message)
                .sourceId(sourceId)
                .response(response.getStatusCode().toString())
                .date(LocalDate.now())
                .build();
        questionService.saveQuestion(question);
        logger.info("Respuesta recibida: {}", response.getStatusCode());

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String content = (String) response.getBody().get("content");
            List<ReferenceDTO> references = (List<ReferenceDTO>) response.getBody().get("references");

            logger.info("Respuesta del mensaje: {}", content);

            return ChatPdfResponseDTO.builder()
                    .content(content)
                    .references(references)
                    .build();
        } else {
            logger.error("Error al enviar mensaje a ChatPDF, código de estado: {}", response.getStatusCode());
            throw new RuntimeException("Error al enviar mensaje a ChatPDF");
        }
    }

    public DeleteResponseDTO deleteFile(String sourceId) {
        String url = baseUrl + "/sources/delete";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of("sources", List.of(sourceId));
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        logger.info("Eliminando archivo con sourceId: {}", sourceId);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);

        logger.info("Respuesta recibida: {}", response.getStatusCode());

        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.error("Error al eliminar archivo de ChatPDF, código de estado: {}", response.getStatusCode());
            throw new RuntimeException("Error al eliminar archivo de ChatPDF");
        }

        logger.info("Archivo con sourceId {} eliminado con éxito.", sourceId);

        return DeleteResponseDTO.builder()
                .message("El archivo con sourceId " + sourceId + " ha sido eliminado.")
                .sourceId(sourceId)
                .build();
    }
}
