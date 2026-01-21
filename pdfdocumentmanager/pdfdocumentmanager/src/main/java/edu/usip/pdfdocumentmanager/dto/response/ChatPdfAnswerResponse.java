package edu.usip.pdfdocumentmanager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatPdfAnswerResponse {
    private String answer;
    private List<ChatPdfReference> references;
}
