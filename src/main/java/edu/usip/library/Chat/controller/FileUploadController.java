package edu.usip.library.Chat.controller;

import edu.usip.library.Chat.service.ChatPDFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${storage.directory}")
    private String storageDirectory;

    @Autowired
    private ChatPDFService chatPDFService;

    @PostMapping("/upload-pdf")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Iniciando carga del archivo: {}", file.getOriginalFilename());
        try {
            Object response = chatPDFService.processPDF(file);
            logger.info("Respuesta del sistema al cargar el archivo: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al cargar el archivo: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e);
        }
    }

    @PostMapping("/add-url")
    public ResponseEntity<Object> addPdfUrl(@RequestBody Map<String, String> requestData) {
        logger.info("Iniciando carga de la URL: {}", requestData.get("url"));

        try {
            // Llamamos al servicio para agregar la URL
            Object response = chatPDFService.addPdfUrl(requestData.get("url"));

            logger.info("Respuesta del sistema: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al cargar la URL del PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @GetMapping("/view")
    public ResponseEntity<Resource> getPdf(@RequestParam String fileName) {
        try {
            Path filePath = Paths.get(storageDirectory, fileName);
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            logger.error("Error al obtener el archivo PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/query")
    public ResponseEntity<Object> queryDocument(@RequestParam("question") String question, @RequestParam("sessionId") String sessionId) {
        logger.info("Realizando consulta: {}", question);
        try {
            Object response = chatPDFService.queryPDF(question, sessionId);
            logger.info("Respuesta del sistema para la consulta: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al realizar la consulta: {}", question, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteDocument(@RequestParam("documentId") String documentId) {
        logger.info("Solicitando eliminación del documento: {}", documentId);
        try {
            Object response = chatPDFService.deletePDF(documentId);
            logger.info("Respuesta del sistema al eliminar el documento: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al eliminar el documento: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e);
        }
    }
}
