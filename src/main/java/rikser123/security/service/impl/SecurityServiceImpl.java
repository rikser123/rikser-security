package rikser123.security.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.utils.RikserResponseUtils;
import rikser123.security.component.Jwt;
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
import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.service.BlackListService;
import rikser123.security.service.SecurityService;
import rikser123.security.service.UserInfoService;
import rikser123.security.service.UserService;

@Service
@AllArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {
  public static final String BEARER_PREFIX = "Bearer ";
  private final UserMapper userMapper;
  private final Jwt jwt;
  private final ReactiveAuthenticationManager authenticationManager;
  private final UserInfoService userInfoService;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final BlackListService blackListService;

  @Override
  public Mono<RikserResponseItem<CreateUserResponseDto>> register(CreateUserRequestDto requestDto) {
    return Mono.fromCallable(() -> userService.findUserByLogin(requestDto.getLogin()))
        .flatMap(
            existedSameLoginOpt -> {
              if (existedSameLoginOpt.isPresent()) {
                return Mono.error(
                    new EntityExistsException(
                        String.format(
                            "Пользователь с логином %s уже зарегистрирован",
                            requestDto.getLogin())));
              }

              return Mono.fromCallable(() -> userService.findUserByEmail(requestDto.getEmail()));
            })
        .flatMap(
            existedSameEmailOpt -> {
              if (existedSameEmailOpt.isPresent()) {
                return Mono.error(
                    new EntityExistsException(
                        String.format(
                            "Пользователь с email %s уже зарегистрирован", requestDto.getEmail())));
              }

              return Mono.fromCallable(() -> userMapper.mapUser(requestDto));
            })
        .subscribeOn(Schedulers.boundedElastic())
        .map(userService::save)
        .flatMap(
            user ->
                setAuthentication(requestDto.getLogin(), requestDto.getPassword()).thenReturn(user))
        .map(
            user -> {
              var token = jwt.generateToken(user);

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
        .flatMap(
            userOpt -> {
              if (userOpt.isEmpty()) {
                return Mono.error(
                    new EntityNotFoundException(
                        String.format("Пользователь с логином %s не найден", userLogin)));
              }

              var user = userOpt.get();
              var isMatches = passwordEncoder.matches(requestDto.getPassword(), user.getPassword());
              if (!isMatches) {
                return Mono.error(new BadCredentialsException("Неверный пароль!"));
              }

              return Mono.just(user);
            })
        .flatMap(
            user ->
                setAuthentication(requestDto.getLogin(), requestDto.getPassword()).thenReturn(user))
        .map(
            user -> {
              var token = jwt.generateToken(user);
              var responseDto = new LoginResponseDto();
              responseDto.setToken(token);

              var userDto = userMapper.mapUserToDto(user);
              responseDto.setUser(userDto);

              return RikserResponseUtils.createResponse(responseDto);
            })
        .onErrorResume(
            BadCredentialsException.class,
            exception -> {
              log.warn("Ошибка авторизации", exception);
              var response = new RikserResponseItem<LoginResponseDto>();
              response.setMessage(exception.getMessage());
              response.setHttpStatus(HttpStatus.UNAUTHORIZED);
              return Mono.just(response);
            });
  }

  @Override
  public Mono<RikserResponseItem<UserResponseDto>> editUser(EditUserDto userDto, String oldToken) {
    var oldLogin = new AtomicReference<>("");
    Objects.requireNonNull(oldToken);

    return checkAccess(userDto.getId(), Privilege.USER_EDIT)
        .map(
            data -> {
              var updatedUser = userService.findById(userDto.getId());
              oldLogin.set(updatedUser.getLogin());
              userMapper.updateUser(userDto, updatedUser);
              return updatedUser;
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            user -> {
              var existedWithSameLogin =
                  userService.findUserByLoginAndIdIsNot(user.getLogin(), user.getId());

              if (existedWithSameLogin.isPresent()) {
                return Mono.error(
                    new EntityExistsException(
                        String.format(
                            "Пользователь с логином %s уже зарегистрирован", user.getLogin())));
              }

              return Mono.just(user);
            })
        .flatMap(
            user -> {
              var existedWithSameEmail =
                  userService.findUserByEmailAndIdIsNot(user.getEmail(), user.getId());

              if (existedWithSameEmail.isPresent()) {
                return Mono.error(
                    new EntityExistsException(
                        String.format(
                            "Пользователь с email %s уже зарегистрирован", user.getEmail())));
              }

              return Mono.just(user);
            })
        .map(userService::save)
        .flatMap(
            user -> {
              var isLoginEqual = user.getLogin().equals(oldLogin.get());
              var responseDto = userMapper.mapUserToDto(user);

              if (isLoginEqual) {
                return Mono.just(responseDto);
              }

              var token = jwt.generateToken(user);
              responseDto.setToken(token);

              if (oldToken.startsWith(BEARER_PREFIX)) {
                var oldTokenContent = oldToken.substring(BEARER_PREFIX.length());
                blackListService.addToken(oldTokenContent, user.getId());
              }

              return setAuthentication(userDto.getLogin(), userDto.getPassword())
                  .thenReturn(responseDto);
            })
        .map(RikserResponseUtils::createResponse);
  }

  @Override
  public Mono<RikserResponseItem<UserDeactivateResponse>> deactivate(
      UserDeactivateRequestDto requestDto) {
    return Mono.fromCallable(() -> userService.findById(requestDto.getId()))
        .map(user -> userService.changeStatus(user, UserStatus.DEACTIVATED))
        .map(
            user -> {
              var userIdDto = new UserDeactivateResponse();
              userIdDto.setId(user.getId());

              return RikserResponseUtils.createResponse(userIdDto);
            });
  }

  @Override
  public Mono<RikserResponseItem<UserEmailResponse>> activateEmail(UserEmailRequestDto requestDto) {
    return checkAccess(requestDto.getId(), Privilege.USER_EDIT)
        .map(
            data -> {
              var user = userService.findById(requestDto.getId());
              userService.changeStatus(user, UserStatus.EMAIL_ACTIVATED);
              return user;
            })
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            user -> {
              var userIdDto = new UserEmailResponse();
              userIdDto.setId(user.getId());

              return RikserResponseUtils.createResponse(userIdDto);
            });
  }

  @Override
  public Mono<RikserResponseItem<UserResponseDto>> getUser(UUID id) {
    return checkAccess(id, Privilege.USER_VIEW)
        .publishOn(Schedulers.boundedElastic())
        .map(data -> userService.findById(id))
        .map(
            user -> {
              var userDto = userMapper.mapUserToDto(user);
              return RikserResponseUtils.createResponse(userDto);
            });
  }

  /**
   * Установка аутентикации в контекст
   *
   * @param login логин пользователя
   * @param password пароль пользователя
   */
  private Mono<Void> setAuthentication(String login, String password) {
    var auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(login, password));
    var context = ReactiveSecurityContextHolder.getContext();
    return Mono.zip(auth, context)
        .flatMap(
            tuple -> {
              tuple.getT2().setAuthentication(tuple.getT1());
              return Mono.empty();
            });
  }

  /**
   * Проверка привилегий на редактирование пользователя
   *
   * @param userId Id пользователя
   */
  private Mono<User> checkAccess(UUID userId, Privilege privilege) {
    return userInfoService
        .getCurrentUser()
        .flatMap(
            userDetails -> {
              User user = (User) userDetails;

              if (!userId.equals(user.getId()) && !user.getPrivileges().contains(privilege)) {
                return Mono.error(new AccessDeniedException("Доступ запрещен"));
              }

              return Mono.just(user);
            });
  }
}
