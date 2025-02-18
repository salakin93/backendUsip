package edu.usip.library.Chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de error")
public class ErrorResponseDTO {
    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private String type;

    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private String title;

    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private Integer status;

    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private String detail;

    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private String message;

    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private LocalDateTime timestamp;
}