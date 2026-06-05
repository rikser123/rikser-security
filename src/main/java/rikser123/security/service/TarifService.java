package rikser123.security.service;

import jakarta.persistence.EntityNotFoundException;
import rikser123.security.dto.response.UserTarifResponseDto;
import rikser123.security.repository.entity.User;

import java.util.UUID;

/**
 * Сервис для работы с тарифами пользователей.
 * Предоставляет методы для получения информации о текущем тарифе пользователя.
 */
public interface TarifService {

  /**
   * Получает информацию о текущем тарифе пользователя.
   *
   * @param user пользователь, чей тариф необходимо получить
   * @return DTO с деталями активного тарифа пользователя
   * @throws EntityNotFoundException если у пользователя нет активного тарифа
   *                                 или тариф с таким ID не найден в системе
   */
  UserTarifResponseDto getUserTarif(User user);

  /**
   * Обновление тарифов пользователя.
   *
   * @param user    пользователь, чей тариф необходимо обновить или создать
   * @param tarifId Id нового тарифа
   * @return Пользователь с обновленными тарифами
   */
  User updateTarif(User user, UUID tarifId);
}
