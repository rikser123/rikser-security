package rikser123.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.security.repository.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByLogin(String login);

    Optional<User> findUserByLoginAndIdIsNot(String login, UUID id);

    Optional findUserByEmailAndIdIsNot(String email, UUID id);
}
