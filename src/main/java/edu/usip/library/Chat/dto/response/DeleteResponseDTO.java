package edu.usip.library.Chat.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteResponseDTO {
    private String sourceId;
    private String message;
}