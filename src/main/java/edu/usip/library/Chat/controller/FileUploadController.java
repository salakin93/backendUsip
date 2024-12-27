package edu.usip.library.Chat.controller;

import edu.usip.library.Chat.service.ChatPDFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

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
