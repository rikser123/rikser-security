package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Выходные параметры для обновления токена")
public class UpdateTokenResponseDto {
  @Schema(description = "Токен")
  private String token;
}
