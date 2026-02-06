package rikser123.security.service;

import reactor.core.publisher.Mono;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.security.dto.response.UserDeactivateResponse;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.dto.response.UserResponseDto;

import java.util.UUID;

/**
 * Интерфейс для работы с безопасностью и пользователями
 *
 */
public interface SecurityService {
    /**
     * Регистрация пользователя
     * @param requestDto {@link CreateUserRequestDto}
     *
     * @return Ответ с зарегистрированным пользователем     */
    Mono<RikserResponseItem<CreateUserResponseDto>> register(CreateUserRequestDto requestDto);

    /**
     * Логиг пользователя
     * @param requestDto {@link LoginRequestDto}
     *
     * @return Токен и пользователь     */
    Mono<RikserResponseItem<LoginResponseDto>> login(LoginRequestDto requestDto);
    /**
     * Редактирование пользователя
     * @param userDto {@link EditUserDto}
     * @param oldToken Токен запроса
     *
     * @return Ответ на редаиткрование пользователя     */
    Mono<RikserResponseItem<UserResponseDto>> editUser(EditUserDto userDto, String oldToken);
    /**
     * Деактивация пользователя
     * @param requestDto {@link UserDeactivateRequestDto}
     *
     * @return Ответ на деактивацию пользователя      */
    Mono<RikserResponseItem<UserDeactivateResponse>> deactivate(UserDeactivateRequestDto requestDto);
    /**
     * Подтвердение емейла пользователя
     * @param requestDto {@link UserEmailRequestDto}
     *
     * @return Ответ на подтверждение емейла      */
    Mono<RikserResponseItem<UserEmailResponse>> activateEmail(UserEmailRequestDto requestDto);

    /**
     * Получение пользователя по айди
     * @param id Id пользователя
     *
     * @return Ответ на получение пользователя     */
    Mono<RikserResponseItem<UserResponseDto>> getUser(UUID id);
}
