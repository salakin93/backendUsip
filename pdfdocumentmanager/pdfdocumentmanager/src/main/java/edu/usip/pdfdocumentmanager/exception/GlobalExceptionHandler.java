package edu.usip.pdfdocumentmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ✅ Errores de validación @Valid (DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors);
    }

    // ✅ Parámetros requeridos ausentes (ej: @RequestParam faltante)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex,
                                                                  HttpServletRequest request) {
        Map<String, String> details = Map.of(
                "parameter", ex.getParameterName(),
                "message", ex.getMessage()
        );
        return build(HttpStatus.BAD_REQUEST, "Missing request parameter", request.getRequestURI(), details);
    }

    // ✅ Tipos inválidos (ej: id=abc cuando esperas Long, o fecha inválida)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                  HttpServletRequest request) {
        Map<String, String> details = Map.of(
                "parameter", ex.getName(),
                "value", ex.getValue() == null ? "null" : ex.getValue().toString(),
                "expectedType", ex.getRequiredType() == null ? "unknown" : ex.getRequiredType().getSimpleName()
        );
        return build(HttpStatus.BAD_REQUEST, "Invalid parameter type", request.getRequestURI(), details);
    }

    // ✅ Archivos demasiado grandes (si configuras multipart max)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "File too large",
                request.getRequestURI(),
                Map.of("message", "El archivo excede el tamaño permitido"));
    }

    // ✅ 403 cuando no tiene permisos
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN,
                "Access denied",
                request.getRequestURI(),
                Map.of("message", ex.getMessage()));
    }

    // ✅ 401 cuando no está autenticado (si llega aquí)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                request.getRequestURI(),
                Map.of("message", ex.getMessage()));
    }

    // ✅ Catch-all controlado (último recurso)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Bad request",
                request.getRequestURI(),
                Map.of("message", ex.getMessage()));
    }

    // ✅ Catch-all general (errores no previstos)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex,
                                                             HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                request.getRequestURI(),
                Map.of("message", "Ocurrió un error inesperado"));
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status,
                                                      String error,
                                                      String path,
                                                      Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("path", path);
        body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }
}
