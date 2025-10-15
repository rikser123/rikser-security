package rikser123.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.RikserRequestItem;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.security.dto.response.RikserResponseItem;
import rikser123.security.repository.entity.User;
import rikser123.security.service.SecurityService;
import rikser123.security.service.UserService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Сервис для работы с пользователями", description = "Сервис для работы с пользтвателями")
public class UserController {
    private final SecurityService securityService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Регистрация пользователя")
    public ResponseEntity<RikserResponseItem<CreateUserResponseDto>> register(
            @Valid
            @RequestBody
            @Parameter(name = "Параметры для регистрации", description = "Параметры для регистрации", required = true)
            RikserRequestItem<CreateUserRequestDto> registerDto
    ) {
        var result = securityService.register(registerDto.getData());
        return result;
    }

    @PutMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Авторизация пользователя")
    public ResponseEntity<RikserResponseItem<LoginResponseDto>> login(
            @Valid
            @RequestBody
            @Parameter(name = "Параметры для авторизации", description = "Параметры для авторизации", required = true)
            RikserRequestItem<LoginRequestDto> loginDto
    ) {
        var result = securityService.login(loginDto.getData());
        return result;
    }

    @PutMapping("/get")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<RikserResponseItem<LoginResponseDto>> get(
            @Valid @RequestBody RikserRequestItem<LoginRequestDto> loginDto
    ) {
        var result = securityService.login(loginDto.getData());
        var user = userService.getCurrentUser();
        log.info("user {} {}", user.getAuthorities(), user.getPrivileges());
        return result;
    }
}
