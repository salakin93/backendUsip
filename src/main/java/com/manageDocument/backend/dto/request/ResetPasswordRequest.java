package com.manageDocument.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String newPassword;
}