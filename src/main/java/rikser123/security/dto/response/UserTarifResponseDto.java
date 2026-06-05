package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Возвращаемое дто тарифа пользователя")
public class UserTarifResponseDto {
  @Schema(description = "Id тарифа")
  private UUID id;

  @Schema(description = "Название тарифа")
  private String name;

  @Schema(description = "Описание тарифа")
  private String description;

  @Schema(description = "Количество запросов за день в тарифе")
  private Integer requestPerDay;

  @Schema(description = "Время обновления тарифа")
  private Instant updated;

  @Schema(description = "Время создания тарифа")
  private Instant created;
}
