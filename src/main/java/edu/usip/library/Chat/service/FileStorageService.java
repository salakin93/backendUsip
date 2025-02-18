package edu.usip.library.Chat.service;

import edu.usip.library.Chat.dto.model.FileUploadDTO;
import edu.usip.library.Chat.dto.response.ChatPdfResponseDTO;
import edu.usip.library.Chat.dto.response.SourceIdResponseDTO;
import edu.usip.library.Chat.exceptions.FileSizeExceededException;
import edu.usip.library.Chat.exceptions.FileStorageException;
import edu.usip.library.Chat.exceptions.InvalidFileTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final DocumentService documentService;
    private final ChatPdfService chatPdfService;
    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB

    @Autowired
    public FileStorageService(DocumentService documentService, ChatPdfService chatPdfService) {
        this.documentService = documentService;
        this.chatPdfService = chatPdfService;
        this.fileStorageLocation = Paths.get("src/main/resources/templates").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("No se pudo crear el directorio", ex);
        }
    }

    public FileUploadDTO storeFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("El archivo excede el tamaño máximo de 30MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new InvalidFileTypeException("Solo se permiten archivos PDF");
        }

        Path targetLocation = null;
        try {
            String fileName = Objects.requireNonNull(file.getOriginalFilename()).replaceAll("[^a-zA-Z0-9.]", "_");
            targetLocation = this.fileStorageLocation.resolve(fileName);

            // Guardar archivo localmente
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Subir a ChatPDF y obtener sourceId
            File pdfFile = targetLocation.toFile();
            SourceIdResponseDTO chatPdfResponse = chatPdfService.addFile(pdfFile);
            ChatPdfResponseDTO dataDocument = chatPdfService.sendMessage(chatPdfResponse.getSourceId(),"Nesecito los siguientes datos del libro en un formato Json 'degree, author, title, summary y defense date en el formato MM-YYYY' el summary debe ser de al menos 100 palabras");
            documentService.save(documentService.fillDocumentFromContent(dataDocument.getContent()));
            // Construir URL de descarga
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(fileName)
                    .toUriString();

            return FileUploadDTO.builder()
                    .fileName(fileName)
                    .downloadUrl(fileDownloadUri)
                    .size(file.getSize())
                    .sourceId(chatPdfResponse.getSourceId())
                    .message("Archivo subido exitosamente")
                    .build();

        } catch (IOException ex) {
            if (targetLocation != null) {
                try {
                    Files.deleteIfExists(targetLocation);
                } catch (IOException e) {
                    logger.error("Error limpiando archivo temporal", e);
                }
            }
            throw new FileStorageException("Error al almacenar el archivo " + file.getOriginalFilename(), ex);
        }
    }
}