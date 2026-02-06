package rikser123.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.security.repository.entity.BlackListToken;

import java.util.Optional;
import java.util.UUID;

public interface BlackListRepository extends JpaRepository<BlackListToken, UUID> {
    Optional<BlackListToken> findByToken(String token);
}
