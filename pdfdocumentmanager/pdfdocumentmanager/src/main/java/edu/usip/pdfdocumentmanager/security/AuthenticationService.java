package edu.usip.pdfdocumentmanager.security;

import edu.usip.pdfdocumentmanager.model.Role;
import edu.usip.pdfdocumentmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Autenticar usuario por phone
     */
    public String authenticate(String phone) {
        if (!userService.exists(phone)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!userService.getAllUsers().stream()
                .filter(u -> u.getPhone().equals(phone))
                .findFirst()
                .orElseThrow().isActive()) {
            throw new RuntimeException("Usuario desactivado");
        }

        boolean isAdmin = userService.isAdmin(phone);
        Set<Role> roles = isAdmin ? Set.of(Role.ROLE_ADMIN) : Set.of(Role.ROLE_STUDENT);

        return jwtUtil.generateToken(phone, roles);
    }
}