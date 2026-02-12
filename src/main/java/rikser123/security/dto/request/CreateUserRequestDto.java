package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.bundle.validation.CheckSqlInjection;
import rikser123.bundle.validation.IsEqual;
import rikser123.bundle.validation.IsStrongPassword;
import rikser123.security.repository.entity.Privilege;

/** Запрос на создание пользователя */
@AllArgsConstructor
@NoArgsConstructor
@Data
@IsEqual(
    firstField = "password",
    secondField = "passwordConfirmation",
    message = "Подтверждение пароля не равно паролю")
@Schema(description = "Параметры для регистрации пользователя")
public class CreateUserRequestDto {
  @NotBlank(message = "Логин не должен быть пустым")
  @Size(min = 8, max = 50, message = "Логин должен состоять минимум из 8 символов и максимум из 50")
  @Schema(description = "Логин пользователя", example = "hoor")
  @CheckSqlInjection
  private String login;

  @NotBlank(message = "Пароль не должен быть пустым")
  @Size(
      min = 10,
      max = 100,
      message = "Пароль должен состоять минимум из 10 символов и максимум из 100")
  @IsStrongPassword(
      passwordMinLength = 10,
      message = "Пароль должен содержать латинские символы, цифры и специальные символы")
  @Schema(description = "Пароль пользователя", example = "ssd@@@SSDSflj12")
  @CheckSqlInjection
  private String password;

  @NotBlank(message = "Подтверждение пароля не должно быть пустым")
  @Size(
      min = 10,
      max = 100,
      message = "Подтверждение пароля должно состоять минимум из 10 символов и максимум из 100")
  @Schema(description = "Подтверждение пароля пользователя", example = "ssd@@@SSDSflj12")
  @CheckSqlInjection
  private String passwordConfirmation;

  @NotBlank(message = "Email не должен быть пустым")
  @Email(message = "Email должен быть корректного формата")
  @Size(max = 100, message = "Email не должен превышать 100 символов")
  @Schema(description = "Email пользователя", example = "erer@rar.ru")
  @CheckSqlInjection
  private String email;

  @NotBlank(message = "FirstName не должен быть пустым")
  @Size(max = 100, message = "Длина FirstName не должна превышать 100 символов")
  @Schema(description = "Имя пользователя", example = "Иван")
  @CheckSqlInjection
  private String firstName;

  @Schema(description = "Отчество пользователя", example = "Иванович")
  @Size(max = 100, message = "Длина MiddleName не должна превышать 100 символов")
  @CheckSqlInjection
  private String middleName;

  @NotBlank(message = "LastName не должен быть пустым")
  @Size(max = 100, message = "Длина LastName не должна превышать 100 символов")
  @Schema(description = "Фамилия пользователя", example = "Иванов")
  @CheckSqlInjection
  private String lastName;

  @Schema(description = "Дата рождения пользователя", example = "1990-01-01")
  @NotNull(message = "Значение BirthDate не должно быть пустым")
  @CheckSqlInjection
  private LocalDate birthDate;

  @NotEmpty(message = "Privileges не должно быть пустым")
  @Schema(description = "Список привилегий пользователя", example = "[EDIT, CREATE]")
  @CheckSqlInjection
  private List<Privilege> privileges = new ArrayList<>();
}
