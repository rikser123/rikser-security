package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "ДТО пользователя с информацией о тарифе")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponseDtoTarif extends UserResponseDto {
  @Schema(description = "Информация о тарифе пользователя")
  private UserTarifResponseDto tarif;
}
