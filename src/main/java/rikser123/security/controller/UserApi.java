package rikser123.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.security.dto.response.UserDeactivateResponse;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.dto.response.UserResponseDto;

@Tag(name = "API для взаимодействия с пользователми")
@ApiResponses(
    value = {
      @ApiResponse(responseCode = "400", description = "Bad request"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    })
@RequestMapping("/api/v1/user")
public interface UserApi {

  @PostMapping("/register")
  @Operation(description = "Регистрация пользователя")
  Mono<RikserResponseItem<CreateUserResponseDto>> register(
      @RequestBody @Parameter(description = "Параметры для регистрации", required = true)
          RikserRequestItem<CreateUserRequestDto> registerDto);

  @PostMapping("/login")
  @Operation(description = "Авторизация пользователя")
  @ResponseStatus(HttpStatus.OK)
  Mono<RikserResponseItem<LoginResponseDto>> login(
      @RequestBody @Parameter(description = "Параметры для авторизации", required = true)
          RikserRequestItem<LoginRequestDto> loginDto);

  @PutMapping("/edit")
  @Operation(description = "Редактирование пользователя")
  @ResponseStatus(HttpStatus.OK)
  Mono<RikserResponseItem<UserResponseDto>> edit(
      @RequestBody
          @Parameter(description = "Параметры для редактирования пользователя", required = true)
          RikserRequestItem<EditUserDto> editDto,
      @RequestHeader("Authorization") String authToken);

  @PatchMapping("/deactivate")
  @Operation(description = "Деактивация пользователя")
  @PreAuthorize("hasAuthority('USER_DELETE')")
  @ResponseStatus(HttpStatus.OK)
  Mono<RikserResponseItem<UserDeactivateResponse>> deactivate(
      @RequestBody @Parameter(description = "Параметры для деактивации пользователя")
          RikserRequestItem<UserDeactivateRequestDto> deactivateDto);

  @PatchMapping("/activate-email")
  @Operation(description = "Подтверждение емейла пользователя")
  @ResponseStatus(HttpStatus.OK)
  Mono<RikserResponseItem<UserEmailResponse>> activateEmail(
      @RequestBody @Parameter(description = "Параметры для для подтверждения емейла пользователя")
          RikserRequestItem<UserEmailRequestDto> deactivateDto);

  @GetMapping("/get/{id}")
  @Operation(description = "Получпние пользователя по айди")
  @ResponseStatus(HttpStatus.OK)
  Mono<RikserResponseItem<UserResponseDto>> getUser(@PathVariable UUID id);
}
