package com.manageDocument.backend.controller;

import com.manageDocument.backend.model.Document;
import com.manageDocument.backend.model.Student;
import com.manageDocument.backend.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ASSIST')")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.saveStudent(student));
    }

    @GetMapping
    @PreAuthorize("hasRole('ASSIST')")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @PostMapping("/{studentId}/documents")
    @PreAuthorize("hasRole('ASSIST')")
    public ResponseEntity<Student> uploadDocument(@PathVariable Long studentId, @RequestBody Document document) {
        return ResponseEntity.ok(studentService.addDocument(studentId, document));
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        studentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}