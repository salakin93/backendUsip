package com.manageDocument.backend.service;

import com.manageDocument.backend.model.Role;
import com.manageDocument.backend.model.Account;
import com.manageDocument.backend.repository.RoleRepository;
import com.manageDocument.backend.repository.UserRepository;
import com.manageDocument.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Account> findAllUsers() {
        return userRepository.findAll(); // Implementación simple para obtener todos los usuarios
    }

    public Account registerUser(String username, String password, String email) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email);

        Role userRole = roleRepository.findByName("ASSIST");
        account.setRoles(Set.of(userRole));

        return userRepository.save(account);
    }

    public Optional<Account> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<Account> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void resetPassword(String email, String newPassword) {
        Optional<Account> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Account account = userOpt.get();
            account.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(account);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new CustomUserDetails(user.get());
    }
}

