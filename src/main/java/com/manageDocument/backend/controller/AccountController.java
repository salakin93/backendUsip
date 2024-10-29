package com.manageDocument.backend.controller;

import com.manageDocument.backend.model.Account;
import com.manageDocument.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AccountController {

    private final UserService userService;

    // Inyecta UserService usando el constructor
    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint para obtener todos los usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo los usuarios con el rol ADMIN pueden acceder
    public ResponseEntity<List<Account>> getAllUsers() {
        List<Account> users = userService.findAllUsers(); // Lógica para obtener todos los usuarios
        return ResponseEntity.ok(users);
    }
}
