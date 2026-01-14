package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
/**
 * Запрос на подтверждеие емейла пользователя
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Входные параметры для подтверждения емейла пользователя")
public class UserEmailRequest {
    @NotNull(message = "Id пользователя не должно быть пустым")
    @Schema(description = "Id пользователя")
    private UUID id;
}
