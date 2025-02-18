package edu.usip.library.Chat.dto.response;

import edu.usip.library.Chat.dto.model.ReferenceDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatPdfResponseDTO {
    private String content;
    private List<ReferenceDTO> references;
}
