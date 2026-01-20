package edu.usip.pdfdocumentmanager.repository;

import edu.usip.pdfdocumentmanager.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByPhone(String phone);
}