package edu.usip.library.Chat.exceptions;

import edu.usip.library.Chat.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Error capturado en GlobalExceptionHandler", ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setType(ex.getClass().getSimpleName());
        errorResponse.setTitle("Error inesperado");
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setDetail(request.getDescription(false));
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setError(ex.toString());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex, WebRequest request) {
        logger.error("Error capturado: {}", ex.getMessage(), ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setType("CustomException");
        errorResponse.setTitle("Error en la aplicación");
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setDetail(request.getDescription(false));
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setError(ex.toString());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Agrega más métodos si necesitas manejar otros tipos de excepciones específicas
}