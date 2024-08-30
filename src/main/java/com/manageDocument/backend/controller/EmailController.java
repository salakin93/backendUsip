package com.manageDocument.backend.controller;

import com.manageDocument.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/sendEmail")
    public String sendEmail() {
        emailService.sendSimpleEmail("destinatario@ejemplo.com", "Asunto del correo", "Cuerpo del correo");
        return "Correo enviado!";
    }
}