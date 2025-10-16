package rikser123.security.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailResponse {
    @NotNull(message = "id пользователя не должно быть пустым")
    private UUID id;
}