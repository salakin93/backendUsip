package edu.usip.pdfdocumentmanager.controller;

import edu.usip.pdfdocumentmanager.dto.request.LoginRequest;
import edu.usip.pdfdocumentmanager.dto.response.LoginResponse;
import edu.usip.pdfdocumentmanager.security.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.authenticate(request.getPhone());
        return ResponseEntity.ok(LoginResponse.builder().token(token).build());
    }
}
