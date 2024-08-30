package com.manageDocument.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "DOCUMENT")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String fileContent;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}