package rikser123.security.service;

import rikser123.security.repository.entity.BlackListToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс для работы с черным списком токенов
 *
 */
public interface BlackListService {
    /**
     * Добавление токена
     * @param token JwtToken
     * @param userId id пользователя
     *
     * @return Токен в черном списке     */
    BlackListToken addToken(String token, UUID userId);

    /**
     * Поиск токена в черном списке
     * @param token JwtToken
     *
     * @return Токен в черном списке     */
    Optional<BlackListToken> findByToken(String token);
}
