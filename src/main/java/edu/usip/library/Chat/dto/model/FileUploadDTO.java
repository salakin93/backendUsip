package edu.usip.library.Chat.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Respuesta de subida de archivo")
public class FileUploadDTO {
    @Schema(description = "Nombre del archivo subido", example = "document.pdf")
    private String fileName;

    @Schema(description = "URL de descarga", example = "http://localhost:8080/api/files/download/document.pdf")
    private String downloadUrl;

    @Schema(description = "Tamaño del archivo en bytes", example = "2541234")
    private long size;

    @Schema(description = "Mensaje de estado", example = "Archivo subido exitosamente")
    private String message;

    @Schema(description = "Id del pdf que te asigna chatPdf", example = "src_XXXXXXXXXXXXX")
    private String sourceId;
}
