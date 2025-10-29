package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.security.repository.entity.Privilege;
import rikser123.bundle.validation.IsEqual;
import rikser123.bundle.validation.IsStrongPassword;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@IsEqual(
        firstField = "password",
        secondField = "passwordConfirmation",
        message = "Подтверждение пароля не равно паролю"
)
@Schema(description = "Параметры для регистрации пользователя")
public class CreateUserRequestDto {
    @NotEmpty(message = "Логин не должен быть пустым")
    @Size(min = 8, message = "Логин должен состоять минимум из 8 симсолов")
    @Schema(description = "Логин пользователя",example = "hoor")
    private String login;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Size(min = 10, message = "Пароль должен состоять минимум из 10 символов")
    @IsStrongPassword(passwordMinLength = 10, message = "Пароль должен содержать латинские символы, цифры и специальные символы")
    @Schema(description = "Пароль пользователя", example = "ssd@@@SSDSflj12")
    private String password;

    @NotEmpty(message = "Подтверждение пароля не должно быть пустым")
    @Size(min = 10, message = "Подтверждение пароля должно состоять минимум из 10 символов")
    @Schema(description = "Подтверждение пароля пользователя", example = "ssd@@@SSDSflj12")
    private String passwordConfirmation;

    @NotEmpty(message = "Email не должен быть пустым")
    @Email(message = "Email должен быть корректного формата")
    @Schema(description = "Email пользователя", example = "erer@rar.ru")
    private String email;

    @NotEmpty
    @Schema(description = "Список привилегий пользователя", example = "[EDIT, CREATE]")
    private List<Privilege> privileges = new ArrayList<>();
}
