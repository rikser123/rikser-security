package rikser123.security.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.security.repository.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph("userPrivileges")
    Optional<User> findUserByEmail(String email);

    @EntityGraph("userPrivileges")
    Optional<User> findUserByLogin(String login);

    @Override
    Optional<User> findById(UUID id);

    Optional<User> findUserByLoginAndIdIsNot(String login, UUID id);

    Optional findUserByEmailAndIdIsNot(String email, UUID id);
}
