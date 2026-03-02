package edu.usip.pdfdocumentmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatpdf_question_logs",
        indexes = {
                @Index(name = "idx_chatlog_document_id", columnList = "document_id"),
                @Index(name = "idx_chatlog_user_phone", columnList = "user_phone"),
                @Index(name = "idx_chatlog_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatPdfQuestionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Documento al que se le preguntó
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Identidad del usuario (viene del JWT)
    @Column(name = "user_phone", nullable = false, length = 32)
    private String userPhone;

    @Column(name = "user_role", nullable = false, length = 32)
    private String userRole; // "ROLE_ADMIN" / "ROLE_STUDENT" (string por simplicidad)

    @Column(name = "question", nullable = false, columnDefinition = "text")
    private String question;

    @Column(name = "answer", columnDefinition = "text")
    private String answer;

    // páginas referenciadas (ej: "3,5,7")
    @Column(name = "reference_pages", length = 200)
    private String referencePages;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
