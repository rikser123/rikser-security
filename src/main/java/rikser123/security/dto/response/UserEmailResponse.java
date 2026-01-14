package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
/**
 * Ответ на подтверждение емейла пользователя
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Выходные параметры для подтверждения email")
public class UserEmailResponse {
    @NotNull(message = "id пользователя не должно быть пустым")
    @Schema(description = "Id пользователя")
    private UUID id;
}