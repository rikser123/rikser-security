package rikser123.security.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.security.repository.entity.BlackListToken;

public interface BlackListRepository extends JpaRepository<BlackListToken, UUID> {
  Optional<BlackListToken> findByToken(String token);
}
