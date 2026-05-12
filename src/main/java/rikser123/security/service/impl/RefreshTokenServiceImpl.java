package rikser123.security.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.security.component.Jwt;
import rikser123.security.repository.RefreshTokenRepository;
import rikser123.security.repository.entity.RefreshToken;
import rikser123.security.repository.entity.User;
import rikser123.security.service.RefreshTokenService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final Jwt jwt;

  @Value("${jwt.refreshExpirationTime}")
  private long refreshExpirationTime;

  @Transactional
  @Override
  public String create(User user) {
    var rawToken = jwt.generateRefreshToken(user);

    var refreshToken = new RefreshToken();
    refreshToken.setUserId(user.getId());
    refreshToken.setRevoked(false);
    refreshToken.setTokenHash(hashToken(rawToken));
    refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationTime, ChronoUnit.MILLIS));
    refreshTokenRepository.save(refreshToken);

    return rawToken;
  }

  @Override
  public String updateAccessToken(User user, String token) {
    try {
      jwt.extractUserName(token);
    } catch (Exception e) {
      log.warn("Некорректный токен", e);
      throw new IllegalStateException("Некорректный токен");
    }

    var existedToken = refreshTokenRepository.findRefreshTokenByUserIdAndTokenHash(user.getId(), hashToken(token))
      .orElseThrow(() -> new EntityNotFoundException("Не удалось найти refresh token!"));

    if (existedToken.getRevoked().equals(Boolean.TRUE)) {
      throw new IllegalStateException("Недействительный токен");
    }

    if (Instant.now().isAfter(existedToken.getExpiresAt())) {
      throw new IllegalStateException("Токен просрочен");
    }

    return jwt.generateToken(user);
  }

  @Transactional
  @Override
  public List<RefreshToken> revoke(User user) {
    var tokens = refreshTokenRepository.findAllByUserId(user.getId());
    tokens.forEach(token -> {
      token.setRevoked(true);
    });

    return refreshTokenRepository.saveAll(tokens);
  }

  private String hashToken(String token) {
    return DigestUtils.sha256Hex(token);
  }
}
