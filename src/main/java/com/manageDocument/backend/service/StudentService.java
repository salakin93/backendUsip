package com.manageDocument.backend.service;

import com.manageDocument.backend.model.Document;
import com.manageDocument.backend.model.Student;
import com.manageDocument.backend.repository.DocumentRepository;
import com.manageDocument.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DocumentRepository documentRepository;

    public StudentService(StudentRepository studentRepository, DocumentRepository documentRepository) {
        this.studentRepository = studentRepository;
        this.documentRepository = documentRepository;
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student addDocument(Long studentId, Document document) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        document.setStudent(student);
        documentRepository.save(document);
        return student;
    }

    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }
}