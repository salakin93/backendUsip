package edu.usip.pdfdocumentmanager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatPdfQuestionLogResponse {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private String userPhone;
    private String userRole;
    private String question;
    private String answer;
    private String referencePages;
    private LocalDateTime createdAt;
}
