package edu.usip.pdfdocumentmanager.security;

import edu.usip.pdfdocumentmanager.model.AppUser;
import edu.usip.pdfdocumentmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public String authenticate(String phone) {

        AppUser user = userService.getUserByPhoneOrThrow(phone);

        if (!user.isActive()) {
            throw new RuntimeException("Usuario desactivado");
        }

        return jwtUtil.generateToken(
                user.getPhone(),
                Set.of(user.getRole())
        );
    }
}
