package rikser123.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.dto.request.UserFilterDto;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.security.dto.response.UpdateTokenResponseDto;
import rikser123.security.dto.response.UserDeactivateResponse;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.dto.response.UserFilterResponseDto;
import rikser123.security.dto.response.UserResponseDto;

import java.util.UUID;

/**
 * Интерфейс для работы с безопасностью и пользователями
 */
public interface SecurityService {
  /**
   * Регистрация пользователя
   *
   * @param requestDto {@link CreateUserRequestDto}
   * @return Ответ с зарегистрированным пользователем
   */
  RikserResponseItem<CreateUserResponseDto> register(CreateUserRequestDto requestDto);

  /**
   * Логиг пользователя
   *
   * @param requestDto {@link LoginRequestDto}
   * @return Токен и пользователь
   */
  RikserResponseItem<LoginResponseDto> login(LoginRequestDto requestDto);

  /**
   * Редактирование пользователя
   *
   * @param userDto  {@link EditUserDto}
   * @param oldToken Токен запроса
   * @return Ответ на редаиткрование пользователя
   */
  RikserResponseItem<UserResponseDto> editUser(EditUserDto userDto, String oldToken);

  /**
   * Деактивация пользователя
   *
   * @param requestDto {@link UserDeactivateRequestDto}
   * @return Ответ на деактивацию пользователя
   */
  RikserResponseItem<UserDeactivateResponse> deactivate(UserDeactivateRequestDto requestDto);

  /**
   * Подтвердение емейла пользователя
   *
   * @param requestDto {@link UserEmailRequestDto}
   * @return Ответ на подтверждение емейла
   */
  RikserResponseItem<UserEmailResponse> activateEmail(UserEmailRequestDto requestDto);

  /**
   * Получение пользователя по айди
   *
   * @param id Id пользователя
   * @return Ответ на получение пользователя
   */
  RikserResponseItem<UserResponseDto> getUser(UUID id);


  /**
   * Получение пользователя по токену
   *
   * @param authToken Токен авторизации
   * @return Ответ на получение пользователя
   */
  RikserResponseItem<JsonNode> getUserByToken(String authToken);

  /**
   * Обновляет access токен по refresh токену
   *
   * @param refreshToken refresh токен
   * @return ответ с новым access токеном
   */
  RikserResponseItem<UpdateTokenResponseDto> updateToken(String refreshToken);

  /**
   * Поиск списка пользователей по фильтру
   *
   * @param filterDto фильтры на поиск
   * @return Список пользователей
   */
  RikserResponseItem<UserFilterResponseDto> findUsers(UserFilterDto filterDto);
}
