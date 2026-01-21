package edu.usip.pdfdocumentmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatPdfQuestionRequest {

    @NotBlank
    private String question;

    /**
     * Si true, ChatPDF intentará devolver referencias de páginas.
     */
    private boolean referenceSources = true;

    /**
     * Historial opcional (máximo 6 mensajes incluyendo la pregunta actual).
     * Si lo envías vacío o null, se enviará solo la pregunta.
     */
    private List<ChatPdfMessage> messages;
}
