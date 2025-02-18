package edu.usip.library.Chat.service;

import edu.usip.library.Chat.model.Role;
import edu.usip.library.Chat.model.User;
import edu.usip.library.Chat.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveOrUpdateUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String googleId = oidcUser.getSubject();

        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setGoogleId(googleId);
            newUser.setApiKey(UUID.randomUUID().toString());
            newUser.setRole(Role.STUDENT);  // Asigna rol por defecto
            return userRepository.save(newUser);
        });
    }

    public void updateUserRole(String email, Role newRole) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> {
            user.setRole(newRole);
            userRepository.save(user);
        });
    }
}
