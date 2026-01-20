package edu.usip.pdfdocumentmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank
    @Schema(description = "Número de teléfono del usuario", example = "70000001")
    @Pattern(regexp = "^[0-9]{8}$", message = "El teléfono debe tener 8 dígitos")
    private String phone;
}