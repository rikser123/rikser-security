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
public class UserEmailRequestDto {
    @NotNull(message = "Id пользователя не должно быть пустым")
    @Schema(description = "Id пользователя", example = "2b4deb54-d343-4d8c-b41b-0b6b60505b70")
    private UUID id;
}
