package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.security.repository.entity.Privilege;
import rikser123.bundle.validation.IsStrongPassword;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Запрос на редактирование пользователя
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditUserDto {
    @NotNull(message = "Id пользователя должно быть заполнено")
    @Schema(description = "Id пользователя")
    private UUID id;

    @NotBlank(message = "Логин не должен быть пустым")
    @Size(min = 8, max = 50, message = "Логин должен состоять минимум из 8 символов и максимум из 50")
    @Schema(description = "Логин пользователя",example = "hoor")
    private String login;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 10, max = 100, message = "Пароль должен состоять минимум из 10 символов и максимум из 100")
    @IsStrongPassword(passwordMinLength = 10, message = "Пароль должен содержать латинские символы, цифры и специальные символы")
    @Schema(description = "Пароль пользователя", example = "ssd@@@SSDSflj12")
    private String password;

    @NotBlank(message = "Подтверждение пароля не должно быть пустым")
    @Size(min = 10, max = 100, message = "Подтверждение пароля должно состоять минимум из 10 символов и максимум из 100")
    @Schema(description = "Подтверждение пароля пользователя", example = "ssd@@@SSDSflj12")
    private String passwordConfirmation;

    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email должен быть корректного формата")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    @Schema(description = "Email пользователя", example = "erer@rar.ru")
    private String email;

    @NotBlank(message = "FirstName не должен быть пустым")
    @Size(max = 100, message = "Длина FirstName не должна превышать 100 символов")
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @Schema(description = "Отчество пользователя", example = "Иванович")
    @Size(max = 100, message = "Длина MiddleName не должна превышать 100 символов")
    private String middleName;

    @NotBlank(message = "LastName не должен быть пустым")
    @Size(max = 100, message = "Длина LastName не должна превышать 100 символов")
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Schema(description = "Дата рождения пользователя", example = "1990-01-01")
    @NotNull(message = "Значение BirthDate не должно быть пустым")
    private LocalDate birthDate;

    @NotEmpty
    @Schema(description = "Список привилегий пользователя", example = "[EDIT, CREATE]")
    private List<Privilege> privileges = new ArrayList<>();
}
