package edu.usip.library.Chat.exceptions;

import edu.usip.library.Chat.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Manejo genérico para todas las excepciones no capturadas específicamente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String title = "Error interno del servidor";

        if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            title = "Recurso no encontrado";
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            title = "Solicitud inválida";
        }

        return buildResponse(ex, request, status, title);
    }

    // Manejo de excepciones de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponseDTO errorResponse = createErrorResponse(ex, request,
                HttpStatus.BAD_REQUEST, "Error de validación", errorMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Manejo de excepciones personalizadas
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST, "Error de negocio");
    }

    // Manejo de excepciones relacionadas con archivos
    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileSizeExceeded(FileSizeExceededException ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST, "Tamaño de archivo excedido");
    }

    @ExceptionHandler({InvalidFileTypeException.class, FileStorageException.class})
    public ResponseEntity<ErrorResponseDTO> handleFileErrors(RuntimeException ex, WebRequest request) {
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST, "Error en el archivo");
    }

    // Método helper para construir respuestas
    private ResponseEntity<ErrorResponseDTO> buildResponse(Exception ex,
                                                           WebRequest request,
                                                           HttpStatus status,
                                                           String title) {
        ErrorResponseDTO errorResponse = createErrorResponse(ex, request, status, title, ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }

    // Método centralizado para crear la respuesta de error
    private ErrorResponseDTO createErrorResponse(Exception ex,
                                                 WebRequest request,
                                                 HttpStatus status,
                                                 String title,
                                                 String detailMessage) {
        logger.error("Error [{}] - {}: {}",
                ex.getClass().getSimpleName(),
                title,
                detailMessage,
                ex);

        return ErrorResponseDTO.builder()
                .type(ex.getClass().getSimpleName())
                .title(title)
                .status(status.value())
                .detail(request.getDescription(false))
                .message(detailMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}