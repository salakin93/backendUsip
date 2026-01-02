package edu.usip.pdfdocumentmanager.controller;

import edu.usip.pdfdocumentmanager.dto.request.UserRequest;
import edu.usip.pdfdocumentmanager.dto.response.UserResponse;
import edu.usip.pdfdocumentmanager.model.AppUser;
import edu.usip.pdfdocumentmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        var user = userService.createUser(request.getName(), request.getPhone(), request.getRole());
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserRequest request) {
        var user = userService.updateUser(request.getPhone(), request.getName(), request.getRole());
        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{phone}")
    public ResponseEntity<Void> deleteUser(@PathVariable String phone) {
        userService.disableUser(phone);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        var users = userService.getAllUsers()
                .stream()
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