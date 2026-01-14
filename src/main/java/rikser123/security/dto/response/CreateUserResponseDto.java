package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
/**
 * Ответ на регистрацию пользователя
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ при регистрации пользователя")
public class CreateUserResponseDto {
    @Schema(description = "Id созданного пользователя", example = "2845f75e-bdeb-40da-8283-69622bba37b2")
    private UUID id;

    @Schema(description = "Токен авторизации", example = "fjghfjgh")
    private String token;
}
