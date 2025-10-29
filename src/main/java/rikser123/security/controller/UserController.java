package rikser123.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.UserDeactivateRequest;
import rikser123.security.dto.request.UserEmailRequest;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.security.dto.response.UserDeactivateResponse;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.dto.response.UserResponseDto;
import rikser123.security.service.SecurityService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Сервис для работы с пользователями", description = "Сервис для работы с пользтвателями")
public class UserController {
    private final SecurityService securityService;

    @PostMapping("/register")
    @Operation(description = "Регистрация пользователя")
    public ResponseEntity<RikserResponseItem<CreateUserResponseDto>> register(
            @Valid
            @RequestBody
            @Parameter(description = "Параметры для регистрации", required = true)
            RikserRequestItem<CreateUserRequestDto> registerDto
    ) {
        return securityService.register(registerDto.getData());
    }

    @PostMapping("/login")
    @Operation(description = "Авторизация пользователя")
    public ResponseEntity<RikserResponseItem<LoginResponseDto>> login(
            @Valid
            @RequestBody
            @Parameter(description = "Параметры для авторизации", required = true)
            RikserRequestItem<LoginRequestDto> loginDto
    ) {
        return securityService.login(loginDto.getData());
    }

    @PutMapping("/edit")
    @Operation(description = "Редактирование пользователя")
    public ResponseEntity<RikserResponseItem<UserResponseDto>> edit(
            @Valid
            @RequestBody
            @Parameter(description = "Параметры для редактирования пользователя", required = true)
            RikserRequestItem<EditUserDto> editDto
    ) {
        return securityService.editUser(editDto.getData());
    }

    @PatchMapping("/deactivate")
    @Operation(description = "Деактивация пользователя")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<RikserResponseItem<UserDeactivateResponse>> deactivate(
            @Valid
            @RequestBody
            @Parameter(description = "Параметры для деактивации пользователя")
            RikserRequestItem<UserDeactivateRequest> deactivateDto
    ) {
        return securityService.deactivate(deactivateDto.getData());
    }

    @PatchMapping("/activate-email")
    @Operation(description = "Подтверждение емейла пользователя")
    public ResponseEntity<RikserResponseItem<UserEmailResponse>> activateEmail(
            @Valid
            @RequestBody
            @Parameter(description = "Параметры для для подтверждения емейла пользователя")
            RikserRequestItem<UserEmailRequest> deactivateDto
    ) {
        return securityService.activateEmail(deactivateDto.getData());
    }
}
