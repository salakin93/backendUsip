package edu.usip.pdfdocumentmanager.controller;

import edu.usip.pdfdocumentmanager.dto.request.UserRequest;
import edu.usip.pdfdocumentmanager.dto.response.UserResponse;
import edu.usip.pdfdocumentmanager.model.AppUser;
import edu.usip.pdfdocumentmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un usuario ADMIN o STUDENT")
    public ResponseEntity<UserResponse> createUser(
            @Valid @Parameter(description = "Datos del usuario") @RequestBody UserRequest request) {
        AppUser user = userService.createUser(request.getName(), request.getPhone(), request.getRole());
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    @Operation(summary = "Actualizar usuario", description = "Actualiza nombre y rol de un usuario existente")
    public ResponseEntity<UserResponse> updateUser(
            @Valid @Parameter(description = "Datos del usuario") @RequestBody UserRequest request) {
        AppUser user = userService.updateUser(request.getPhone(), request.getName(), request.getRole());
        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{phone}")
    @Operation(summary = "Deshabilitar usuario", description = "Deshabilita (soft delete) un usuario por su número de teléfono")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Número de teléfono del usuario") @PathVariable String phone) {
        userService.disableUser(phone);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve la lista de todos los usuarios")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    private UserResponse toResponse(AppUser user) {
        return UserResponse.builder()
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}