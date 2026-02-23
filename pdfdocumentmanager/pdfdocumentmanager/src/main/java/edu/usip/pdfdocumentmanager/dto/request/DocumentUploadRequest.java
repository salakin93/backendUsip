package edu.usip.pdfdocumentmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotBlank
    @Schema(example = "Análisis de Sistemas Distribuidos")
    private String title;

    @NotBlank
    @Schema(example = "Juan Pérez")
    private String author;

    @NotBlank
    @Schema(example = "Ingeniería de Sistemas")
    private String degree;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(example = "2024-11-15")
    private LocalDate defenseDate;

    @Schema(example = "DOC-2024-0001", description = "Opcional. Si no se envía, el sistema genera uno.")
    private String sourceId;
}