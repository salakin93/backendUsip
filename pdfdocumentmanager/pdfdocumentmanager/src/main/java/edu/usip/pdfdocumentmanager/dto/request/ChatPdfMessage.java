package edu.usip.pdfdocumentmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatPdfMessage {

    /**
     * Valores esperados por ChatPDF: "user" | "assistant"
     */
    @NotBlank
    private String role;

    @NotBlank
    private String content;
}
