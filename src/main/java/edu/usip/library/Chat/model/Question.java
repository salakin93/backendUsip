package edu.usip.library.Chat.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "Question")
@Data
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sourceId", nullable = false)
    private String sourceId;

    @Column(name = "request", nullable = false)
    private String request;

    @Column(name = "response", nullable = false)
    private String response;

    @Column(name = "date", nullable = false)
    private LocalDate date;
}
