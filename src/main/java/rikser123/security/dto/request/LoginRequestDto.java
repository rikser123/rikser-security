package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.bundle.validation.CheckSqlInjection;

/**
 * Запрос на логин пользователя
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Входные параметры для логина пользователя")
public class LoginRequestDto {
    @NotEmpty(message = "Логин не должен быть пустым")
    @Schema(description = "Логин пользователя", example = "eere111")
    @CheckSqlInjection
    private String login;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Schema(description = "Пароль пользователя", example = "88@@@difidufIDU")
    @CheckSqlInjection
    private String password;
}
