package rikser123.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.security.repository.entity.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findRefreshTokenByUserIdAndTokenHash(UUID userId, String tokenHash);

  List<RefreshToken> findAllByUserId(UUID userId);
}
