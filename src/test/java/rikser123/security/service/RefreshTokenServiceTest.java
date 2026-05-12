package rikser123.security.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rikser123.security.component.Jwt;
import rikser123.security.repository.RefreshTokenRepository;
import rikser123.security.repository.entity.RefreshToken;
import rikser123.security.repository.entity.User;
import rikser123.security.service.impl.RefreshTokenServiceImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тестирование класса {@link RefreshTokenService} (синхронная версия)
 */
@ExtendWith(SpringExtension.class)

public class RefreshTokenServiceTest {
  private RefreshTokenService refreshTokenService;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private Jwt jwt;

  @BeforeEach
  void init() {
    refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository, jwt);
    ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationTime", 10000L);
  }

  @Test
  void shouldCreate() {
    var user = new User();
    user.setId(UUID.randomUUID());

    when(jwt.generateRefreshToken(user)).thenReturn("token");
    when(refreshTokenRepository.save(argThat(arg -> {
      assertThat(arg.getUserId()).isEqualTo(user.getId());
      assertThat(arg.getRevoked()).isEqualTo(Boolean.FALSE);
      assertThat(arg.getExpiresAt().minus(Instant.now().toEpochMilli(), ChronoUnit.MILLIS).toEpochMilli()).isLessThan(10000L);
      return true;
    }))).thenReturn(new RefreshToken());

    refreshTokenService.create(user);
  }

  @Test
  void shouldUpdate() {
    var user = new User();
    user.setId(UUID.randomUUID());

    var token = new RefreshToken();
    token.setExpiresAt(Instant.now().plus(10, ChronoUnit.DAYS));
    token.setRevoked(Boolean.FALSE);

    when(jwt.extractUserName(any())).thenReturn("token");
    when(refreshTokenRepository.findRefreshTokenByUserIdAndTokenHash(any(), any())).thenReturn(Optional.of(token));

    refreshTokenService.updateAccessToken(user, "token");

    verify(jwt, times(1)).generateToken(user);
  }

  @Test
  void shouldCatchErrorThenNoToken() {
    var user = new User();
    user.setId(UUID.randomUUID());

    var token = new RefreshToken();
    token.setExpiresAt(Instant.now().plus(10, ChronoUnit.DAYS));
    token.setRevoked(Boolean.FALSE);

    when(jwt.extractUserName(any())).thenReturn("token");
    when(refreshTokenRepository.findRefreshTokenByUserIdAndTokenHash(any(), any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenService.updateAccessToken(user, "token")).isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldCatchErrorWhenRevokedToken() {
    var user = new User();
    user.setId(UUID.randomUUID());

    var token = new RefreshToken();
    token.setRevoked(Boolean.TRUE);

    when(jwt.extractUserName(any())).thenReturn("token");
    when(refreshTokenRepository.findRefreshTokenByUserIdAndTokenHash(any(), any())).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> refreshTokenService.updateAccessToken(user, "token")).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldCatchErrorWhenExpiredToken() {
    var user = new User();
    user.setId(UUID.randomUUID());

    var token = new RefreshToken();
    token.setExpiresAt(Instant.now().minus(10, ChronoUnit.DAYS));
    token.setRevoked(Boolean.FALSE);

    when(jwt.extractUserName(any())).thenReturn("token");
    when(refreshTokenRepository.findRefreshTokenByUserIdAndTokenHash(any(), any())).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> refreshTokenService.updateAccessToken(user, "token")).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldRevoke() {
    var user = new User();
    user.setId(UUID.randomUUID());

    var token = new RefreshToken();
    token.setExpiresAt(Instant.now().plus(10, ChronoUnit.DAYS));
    token.setRevoked(Boolean.FALSE);

    when(refreshTokenRepository.findAllByUserId(any())).thenReturn(List.of(token));
    when(refreshTokenRepository.saveAll(argThat(arg -> {
      var argList = (List) arg;
      var tokenArg = (RefreshToken) argList.getFirst();
      assertThat(tokenArg.getRevoked()).isTrue();
      return true;
    }))).thenReturn(Collections.emptyList());

    refreshTokenService.revoke(user);
  }
}
