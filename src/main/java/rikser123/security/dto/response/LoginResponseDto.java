package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Ответ на логин пользователя
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ пли логине пользователя")
public class LoginResponseDto {
    @Schema(description = "Токен авторизации", example = "dfhdfh8y8273")
    private String token;

    @Schema(description = "Возвращаемый пользователь")
    private UserResponseDto user;
}
