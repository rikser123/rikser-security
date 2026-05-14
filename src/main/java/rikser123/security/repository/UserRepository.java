package rikser123.security.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rikser123.security.repository.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
  @Override
  @EntityGraph("privileges")
  Optional<User> findById(UUID id);

  Optional<User> findUserByEmail(String email);

  @EntityGraph("privileges")
  Optional<User> findUserByLogin(String login);

  Optional<User> findUserByLoginAndIdIsNot(String login, UUID id);

  Optional findUserByEmailAndIdIsNot(String email, UUID id);
}
