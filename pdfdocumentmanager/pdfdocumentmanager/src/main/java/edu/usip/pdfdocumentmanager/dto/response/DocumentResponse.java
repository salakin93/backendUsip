package edu.usip.pdfdocumentmanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DocumentResponse {

    @Schema(example = "15")
    private Long id;

    @Schema(example = "Ingeniería de Sistemas")
    private String degree;

    @Schema(example = "Análisis de Sistemas Distribuidos")
    private String title;

    @Schema(example = "Juan Pérez")
    private String author;

    @Schema(example = "2024-11-15", description = "Fecha de defensa (ISO-8601)")
    private LocalDate defenseDate;

    @Schema(example = "DOC-2024-0001", description = "Identificador de origen / código del documento")
    private String sourceId;

    @Schema(example = "DOC-2024-0001_tesis.pdf")
    private String fileName;

    @Schema(example = "/documents/15/download", description = "Endpoint para descargar el PDF")
    private String downloadUrl;

    @Schema(example = "1048576", description = "Tamaño del archivo en bytes")
    private long size;
}