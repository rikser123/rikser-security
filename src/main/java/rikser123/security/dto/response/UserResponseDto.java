package rikser123.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Возвращаемое дто пользователя")
public class UserResponseDto {
    @Schema(description = "Id пользователя", example = "2845f75e-bdeb-40da-8283-69622bba37b2")
    private UUID id;

    @Schema(description = "Логин пользователя", example = "hook")
    private String login;

    @Schema(description = "Email пользователя", example = "rtrt@rar.ru")
    private String email;

    @Schema(description = "Статус пользователя", example = "REGISTERED")
    private UserStatus status;

    @Schema(description = "Привилегии пользователя", example = "ADMIN, USER")
    private Set<Privilege> privileges;

    @Schema(description = "Время создания")
    private LocalDateTime created;

    @Schema(description = "Время обновления")
    private LocalDateTime updated;
}
