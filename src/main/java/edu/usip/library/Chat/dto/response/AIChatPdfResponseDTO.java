package edu.usip.library.Chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AIChatPdfResponseDTO {
    private String content;
    private List<ReferenceDTO> references;
}
