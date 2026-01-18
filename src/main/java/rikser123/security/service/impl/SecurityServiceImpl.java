package rikser123.security.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.utils.RikserResponseUtils;
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
import rikser123.security.mapper.UserMapper;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.service.SecurityService;
import rikser123.security.service.UserInfoService;
import rikser123.security.service.UserService;
import rikser123.security.component.Jwt;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {
    private final UserMapper userMapper;
    private final Jwt jwt;
    private final AuthenticationManager authenticationManager;
    private final UserInfoService userInfoService;
    private final UserService userService;

    @Override
    public Mono<RikserResponseItem<CreateUserResponseDto>> register(CreateUserRequestDto requestDto) {
        return Mono.fromCallable(() -> userService.findUserByLogin(requestDto.getLogin()))
            .flatMap(existedSameLoginOpt -> {
                if (existedSameLoginOpt.isPresent()) {
                    return Mono.error(new EntityExistsException(String.format("Пользователь с логином %s уже зарегистрирован", requestDto.getLogin())));
                }

                return Mono.fromCallable(() ->  userService.findUserByEmail(requestDto.getEmail()));
            })
            .flatMap(existedSameEmailOpt -> {
                if (existedSameEmailOpt.isPresent()) {
                    return Mono.error(new EntityExistsException(String.format("Пользователь с email %s уже зарегистрирован", requestDto.getEmail())));
                }

                return Mono.fromCallable(() -> userMapper.mapUser(requestDto));
            })
            .subscribeOn(Schedulers.boundedElastic())
            .map(userService::save)
            .map(user -> {
                var token = jwt.generateToken(user);

                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        requestDto.getLogin(),
                        requestDto.getPassword()
                ));
                var responseDto = new CreateUserResponseDto();
                responseDto.setId(user.getId());
                responseDto.setToken(token);
                return RikserResponseUtils.createResponse(responseDto);
            });

    }

    @Override
    public Mono<RikserResponseItem<LoginResponseDto>> login(LoginRequestDto requestDto) {
        var userLogin = requestDto.getLogin();

        return Mono.fromCallable(() -> userService.findUserByLogin(requestDto.getLogin()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(userOpt -> {
                if (userOpt.isEmpty()) {
                    return Mono.error(new EntityNotFoundException(String.format("Пользователь с логином %s не найден", userLogin)));
                }

                return Mono.just(userOpt.get());
            }).map(user -> {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        userLogin,
                        requestDto.getPassword()
                ));

                var token = jwt.generateToken(user);
                var responseDto = new LoginResponseDto();
                responseDto.setToken(token);

                var userDto = userMapper.mapUserToDto(user);
                responseDto.setUser(userDto);

                return RikserResponseUtils.createResponse(responseDto);
            });
    }

    @Override
    public Mono<RikserResponseItem<UserResponseDto>> editUser(EditUserDto userDto) {
        return Mono.fromCallable(() -> {
            checkEditAccess(userDto.getId());

            var updatedUser = userService.findById(userDto.getId());
            userMapper.updateUser(userDto, updatedUser);
            return updatedUser;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(user -> {
            var existedWithSameLogin = userService.findUserByLoginAndIdIsNot(user.getLogin(), user.getId());

            if (existedWithSameLogin.isPresent()) {
                Mono.error(new EntityExistsException(String.format("Пользователь с логином %s уже зарегистрирован", user.getLogin())));
            }

            return Mono.just(user);
        }).flatMap(user -> {
            var existedWithSameEmail = userService.findUserByEmailAndIdIsNot(user.getEmail(), user.getId());

            if (existedWithSameEmail.isPresent()) {
                Mono.error(new EntityExistsException(String.format("Пользователь с email %s уже зарегистрирован", user.getEmail())));
            }

            return Mono.just(user);
        })
        .map(userService::save)
        .map(user -> {
            var responseDto = userMapper.mapUserToDto(user);
            return RikserResponseUtils.createResponse(responseDto);
        });
    }

    @Override
    public Mono<RikserResponseItem<UserDeactivateResponse>> deactivate(UserDeactivateRequestDto requestDto) {
        return Mono.fromCallable(() -> userService.findById(requestDto.getId()))
            .map(user -> userService.changeStatus(user, UserStatus.DEACTIVATED))
            .map(user -> {
                var userIdDto = new UserDeactivateResponse();
                userIdDto.setId(user.getId());

                return RikserResponseUtils.createResponse(userIdDto);
            });
    }

    @Override
    public Mono<RikserResponseItem<UserEmailResponse>> activateEmail(UserEmailRequestDto requestDto) {
        return Mono.fromCallable(() -> {
            checkEditAccess(requestDto.getId());
            var user = userService.findById(requestDto.getId());
            userService.changeStatus(user, UserStatus.EMAIL_ACTIVATED);
            return user;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(user -> {
            var userIdDto = new UserEmailResponse();
            userIdDto.setId(user.getId());

            return RikserResponseUtils.createResponse(userIdDto);
        });
    }

    @Override
    public Mono<RikserResponseItem<UserResponseDto>> getUser(UUID id) {
        return Mono.fromCallable(() -> userService.findById(id))
          .map(user -> {
              var userDto = userMapper.mapUserToDto(user);
              return RikserResponseUtils.createResponse(userDto);
        });
    }

    /**
     * Проверка привилегий на редактирование пользователя
     * @param userId Id пользователя
     */
    private void checkEditAccess(UUID userId) {
        var currentUser = userInfoService.getCurrentUser();

        if (!userId.equals(currentUser.getId()) && !currentUser.getPrivileges().contains(Privilege.USER_EDIT)) {
            throw new AccessDeniedException("Доступ запрещен");
        }
    }
}