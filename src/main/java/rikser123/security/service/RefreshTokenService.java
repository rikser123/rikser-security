package rikser123.security.service;

import rikser123.security.repository.entity.RefreshToken;
import rikser123.security.repository.entity.User;

import java.util.List;

/**
 * Сервис для управления Refresh токенами
 */
public interface RefreshTokenService {

  /**
   * Создаёт refresh токен для пользователя
   *
   * @param user пользователь, для которого создаётся токен
   * @return сырой (не хэшированный) refresh токен
   */
  String create(User user);

  /**
   * Обновляет access токен по refresh токену
   *
   * @param user  пользователь
   * @param token refresh токен
   * @return новый access токен
   * @throws IllegalStateException                       если токен некорректный, отозван или просрочен
   * @throws jakarta.persistence.EntityNotFoundException если токен не найден
   */
  String updateAccessToken(User user, String token);

  /**
   * Отзывает все refresh токены пользователя (logout everywhere)
   *
   * @param user пользователь
   * @return список отозванных токенов
   */
  List<RefreshToken> revoke(User user);
}