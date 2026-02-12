package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Запрос на деактивацию пользователя */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Входные параметры для деактивации пользователя")
public class UserDeactivateRequestDto {
  @NotNull(message = "id пользователя не должно быть пустым")
  @Schema(description = "Id пользователя", example = "2b4deb54-d343-4d8c-b41b-0b6b60505b70")
  private UUID id;
}
