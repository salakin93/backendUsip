package edu.usip.pdfdocumentmanager.service;

import edu.usip.pdfdocumentmanager.model.AppUser;
import edu.usip.pdfdocumentmanager.model.Role;
import edu.usip.pdfdocumentmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Crear un nuevo usuario
     */
    @Transactional
    public AppUser createUser(String name, String phone, Role role) {
        userRepository.findByPhone(phone).ifPresent(u -> {
            throw new RuntimeException("Usuario con ese teléfono ya existe");
        });

        AppUser user = AppUser.builder()
                .name(name)
                .phone(phone)
                .role(role)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    /**
     * Actualizar usuario
     */
    @Transactional
    public AppUser updateUser(String phone, String newName, Role newRole) {
        AppUser user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(newName);
        user.setRole(newRole);

        return userRepository.save(user);
    }

    /**
     * Deshabilitar usuario (soft delete)
     */
    @Transactional
    public AppUser disableUser(String phone) {
        AppUser user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setActive(false);
        return userRepository.save(user);
    }

    /**
     * Eliminar usuario (hard delete)
     */
    @Transactional
    public void deleteUser(String phone) {
        AppUser user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        userRepository.delete(user);
    }

    /**
     * Listar todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Validar si un usuario es Admin
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(String phone) {
        AppUser user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getRole() == Role.ROLE_ADMIN;
    }

    /**
     * Validar si usuario existe
     */
    @Transactional(readOnly = true)
    public boolean exists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    public AppUser getUserByPhoneOrThrow(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public AppUser getActiveUserByPhoneOrThrow(String phone) {
        AppUser user = getUserByPhoneOrThrow(phone);
        if (!user.isActive()) {
            throw new RuntimeException("Usuario desactivado");
        }
        return user;
    }

}