package pl.alfateam.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.alfateam.portfoliomanager.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
