package rikser123.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import rikser123.security.repository.entity.UserStatus;

import java.time.LocalDate;

/**
 * Фильтры для поиска списка пользователей
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Параметры для поиска пользоваетелей")
public class UserFilterDto {
  @Size(max = 50, message = "Размер поля login не должен быть больше 50 символов")
  @Schema(description = "Login пользователя", example = "login")
  private String login;

  @Email(message = "Поле email должно иметь правильный формат")
  @Schema(description = "Email пользователя", example = "rar@rar.ru")
  private String email;

  @Schema(description = "Статус пользователя", example = "REGISTERED")
  private UserStatus status;

  @Schema(description = "Имя пользователя", example = "Иван")
  @Size(max = 100, message = "Размер поля firstName не должен быть больше 100 символов")
  private String firstName;

  @Schema(description = "Фамилия пользователя", example = "Иванов")
  @Size(max = 100, message = "Размер поля lastName не должен быть больше 100 символов")
  private String lastName;

  @Schema(description = "Отчестов пользователя", example = "Иванович")
  @Size(max = 100, message = "Размер поля middleName не должен быть больше 100 символов")
  private String middleName;

  @Schema(description = "Дата рождения начиная с", example = "09.09.1800")
  private LocalDate birthDateFrom;

  @Schema(description = "Дата рождения до", example = "09.09.1900")
  private LocalDate birthDateTo;

  @Schema(description = "Размер страницы", example = "25")
  private int pageSize = 25;

  @Schema(description = "Номер страницы", example = "1")
  private int pageNumber = 0;

  @Schema(description = "Сортировка по полю", example = "firstName")
  private String sortField;

  @Schema(description = "Направление сортировки", example = "firstName")
  private Sort.Direction direction;
}
