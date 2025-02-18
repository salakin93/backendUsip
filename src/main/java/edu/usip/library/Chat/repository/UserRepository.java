package edu.usip.library.Chat.repository;

import edu.usip.library.Chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByApiKey(String apiKey);
}
