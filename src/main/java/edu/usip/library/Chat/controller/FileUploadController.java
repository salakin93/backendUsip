package edu.usip.library.Chat.controller;

import edu.usip.library.Chat.dto.ErrorResponseDTO;
import edu.usip.library.Chat.dto.model.FileUploadDTO;
import edu.usip.library.Chat.dto.response.ChatPdfResponseDTO;
import edu.usip.library.Chat.dto.response.DeleteResponseDTO;
import edu.usip.library.Chat.dto.response.SourceIdResponseDTO;
import edu.usip.library.Chat.service.ChatPdfService;
import edu.usip.library.Chat.service.FileStorageService;
import edu.usip.library.Chat.validator.ValidPdfFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${storage.directory}")
    private String storageDirectory;

    @Autowired
    private ChatPdfService chatPDFService;

    @Autowired
    private FileStorageService fileStorageService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            tags = {"PDF Management"},
            summary = "Subir archivo PDF",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "object"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF subido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileUploadDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "fileName": "document.pdf",
                                                "downloadUrl": "http://localhost:8080/api/files/download/document.pdf",
                                                "size": 2541234,
                                                "message": "Archivo subido exitosamente"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Tamaño excedido",
                                            value = """
                                                    {
                                                        "type": "FileSizeExceededException",
                                                        "title": "Tamaño de archivo excedido",
                                                        "status": 400,
                                                        "detail": "uri=/api/files/upload",
                                                        "message": "El archivo excede el tamaño máximo de 30MB",
                                                        "timestamp": "2023-12-20T16:45:30.123456"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Tipo inválido",
                                            value = """
                                                    {
                                                        "type": "InvalidFileTypeException",
                                                        "title": "Error en el archivo",
                                                        "status": 400,
                                                        "detail": "uri=/api/files/upload",
                                                        "message": "Solo se permiten archivos PDF",
                                                        "timestamp": "2023-12-20T16:47:15.654321"
                                                    }"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "type": "FileStorageException",
                                                "title": "Error interno del servidor",
                                                "status": 500,
                                                "detail": "uri=/api/files/upload",
                                                "message": "Error al almacenar el archivo",
                                                "timestamp": "2023-12-20T16:49:20.987654"
                                            }"""
                            )
                    )
            )
    })
    public ResponseEntity<FileUploadDTO> uploadPdfFile(
            @Parameter(
                    description = "Archivo PDF a subir (máx. 30MB)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") @ValidPdfFile MultipartFile file) {
        logger.info("Iniciando carga del archivo: {}", file.getOriginalFilename());
        FileUploadDTO response = fileStorageService.storeFile(file);
        logger.info("Respuesta del sistema al cargar el archivo: {}", response);
        return ResponseEntity.ok(response);
    }

    @Deprecated
    @PostMapping("/add-url")
    @Operation(tags = {"PDF Management"}, summary = "Agregar un PDF desde una URL", description = "Permite agregar un archivo PDF desde una URL y procesarlo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL procesada exitosamente", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadDTO.class))
            }),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            }),
    })
    public ResponseEntity<SourceIdResponseDTO> addPdfUrl(@RequestBody Map<String, String> requestData) {
        logger.info("Iniciando carga de la URL: {}", requestData.get("url"));
        SourceIdResponseDTO response = chatPDFService.addUrl(requestData.get("url"));
        logger.info("Respuesta del sistema: {}", response);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/query")
    @Operation(tags = {"PDF Management"}, summary = "Consultar un documento PDF", description = "Permite realizar consultas en un documento PDF ya procesado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada con éxito"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ChatPdfResponseDTO> queryDocument(@RequestParam("question") String question, @RequestParam("sessionId") String sessionId) {
        logger.info("Realizando consulta: {}", question);
        ChatPdfResponseDTO response = chatPDFService.sendMessage(sessionId, question);
        logger.info("Respuesta del sistema para la consulta: {}", response);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    @Operation(tags = {"PDF Management"}, summary = "Eliminar un documento PDF", description = "Permite eliminar un documento PDF del sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento eliminado exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<DeleteResponseDTO> deleteDocument(@RequestParam("documentId") String documentId) {
        logger.info("Solicitando eliminación del documento: {}", documentId);
        DeleteResponseDTO response = chatPDFService.deleteFile(documentId);
        logger.info("Respuesta del sistema al eliminar el documento: {}", response);
        return ResponseEntity.ok(response);
    }

}
