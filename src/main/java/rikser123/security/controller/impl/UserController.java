package rikser123.security.controller.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rikser123.security.controller.UserApi;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.dto.request.UserGetRequestDto;
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
public class UserController implements UserApi {
    private final SecurityService securityService;

    @Override
    public Mono<RikserResponseItem<CreateUserResponseDto>> register(
            @Valid
            @RequestBody
            RikserRequestItem<CreateUserRequestDto> registerDto
    ) {
        return securityService.register(registerDto.getData());
    }

    @Override
    public Mono<RikserResponseItem<LoginResponseDto>> login(
            @Valid
            @RequestBody
            RikserRequestItem<LoginRequestDto> loginDto
    ) {
        return securityService.login(loginDto.getData());
    }

   @Override
    public Mono<RikserResponseItem<UserResponseDto>> edit(
            @Valid
            @RequestBody
            RikserRequestItem<EditUserDto> editDto
    ) {
        return securityService.editUser(editDto.getData());
    }

    @Override
    public Mono<RikserResponseItem<UserDeactivateResponse>> deactivate(
            @Valid
            @RequestBody
            RikserRequestItem<UserDeactivateRequestDto> deactivateDto
    ) {
        return securityService.deactivate(deactivateDto.getData());
    }

    @Override
    public Mono<RikserResponseItem<UserEmailResponse>> activateEmail(
            @Valid
            @RequestBody
            RikserRequestItem<UserEmailRequestDto> deactivateDto
    ) {
        return securityService.activateEmail(deactivateDto.getData());
    }

    @Override
    public Mono<RikserResponseItem<UserResponseDto>> getUser(
            @Valid
            @RequestBody
            RikserRequestItem<UserGetRequestDto> getDto
    ) {
      return securityService.getUser(getDto.getData().getId());
    }
}
