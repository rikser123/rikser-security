package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ на поиск пользователей")
public class UserFilterResponseDto {
  @Schema(description = "Список пользователей")
  private List<UserResponseDto> users;

  @Schema(description = "Общее количество элементов")
  private long totalElements;
}
