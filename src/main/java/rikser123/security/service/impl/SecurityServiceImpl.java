package rikser123.security.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.utils.RikserResponseUtils;
import rikser123.security.component.Jwt;
import rikser123.security.dto.TokenDto;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.dto.request.UserFilterDto;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.security.dto.response.UpdateTokenResponseDto;
import rikser123.security.dto.response.UserDeactivateResponse;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.dto.response.UserFilterResponseDto;
import rikser123.security.dto.response.UserResponseDto;
import rikser123.security.mapper.UserMapper;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.service.BlackListService;
import rikser123.security.service.RefreshTokenService;
import rikser123.security.service.SecurityService;
import rikser123.security.service.UserDetailSecurityService;
import rikser123.security.service.UserService;

import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {
  public static final String BEARER_PREFIX = "Bearer ";

  private final UserMapper userMapper;
  private final Jwt jwt;
  private final AuthenticationManager authenticationManager;
  private final UserDetailSecurityService userDetailSecurityService;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final BlackListService blackListService;
  private final ObjectMapper objectMapper;
  private final RefreshTokenService refreshTokenService;

  @Override
  @Transactional
  public RikserResponseItem<CreateUserResponseDto> register(CreateUserRequestDto requestDto) {
    var existedSameLoginOpt = userService.findUserByLogin(requestDto.getLogin());
    if (existedSameLoginOpt.isPresent()) {
      throw new EntityExistsException(
        String.format("Пользователь с логином %s уже зарегистрирован", requestDto.getLogin()));
    }

    var existedSameEmailOpt = userService.findUserByEmail(requestDto.getEmail());
    if (existedSameEmailOpt.isPresent()) {
      throw new EntityExistsException(
        String.format("Пользователь с email %s уже зарегистрирован", requestDto.getEmail()));
    }

    var user = userMapper.mapUser(requestDto);
    var savedUser = userService.save(user);
    var token = jwt.generateToken(savedUser);
    var refreshToken = refreshTokenService.create(user);

    var authParams = SetAuthenticationParams.builder()
      .login(requestDto.getLogin())
      .password(requestDto.getPassword())
      .token(token)
      .refreshToken(refreshToken)
      .privileges(savedUser.getPrivileges())
      .build();

    setAuthentication(authParams);

    var responseDto = new CreateUserResponseDto();
    responseDto.setId(savedUser.getId());
    responseDto.setToken(token);
    responseDto.setRefreshToken(refreshToken);

    return RikserResponseUtils.createResponse(responseDto);
  }

  @Override
  public RikserResponseItem<LoginResponseDto> login(LoginRequestDto requestDto) {
    var userLogin = requestDto.getLogin();

    try {
      var user = userService.findUserByLogin(requestDto.getLogin()).orElseThrow(() ->
        new EntityNotFoundException(String.format("Пользователь с логином %s не найден", userLogin)));

      var isMatches = passwordEncoder.matches(requestDto.getPassword(), user.getPassword());
      if (!isMatches) {
        throw new BadCredentialsException("Неверный пароль!");
      }

      var token = jwt.generateToken(user);
      var refreshToken = refreshTokenService.create(user);

      var authParams = SetAuthenticationParams.builder()
        .login(requestDto.getLogin())
        .password(requestDto.getPassword())
        .token(token)
        .refreshToken(refreshToken)
        .privileges(user.getPrivileges())
        .build();

      setAuthentication(authParams);

      var responseDto = new LoginResponseDto();
      responseDto.setToken(token);
      responseDto.setRefreshToken(refreshToken);

      var userDto = userMapper.mapUserToDto(user);
      responseDto.setUser(userDto);

      return RikserResponseUtils.createResponse(responseDto);

    } catch (BadCredentialsException e) {
      log.warn("Ошибка авторизации", e);
      var response = new RikserResponseItem<LoginResponseDto>();
      response.setMessage(e.getMessage());
      response.setHttpStatus(HttpStatus.UNAUTHORIZED);
      return response;
    }
  }

  @Override
  @Transactional
  public RikserResponseItem<UserResponseDto> editUser(EditUserDto userDto, String oldToken) {
    checkAccess(userDto.getId(), Privilege.USER_EDIT);

    var updatedUser = userService.findById(userDto.getId());
    var oldLogin = updatedUser.getLogin();
    userMapper.updateUser(userDto, updatedUser);

    var existedWithSameLogin = userService.findUserByLoginAndIdIsNot(updatedUser.getLogin(), updatedUser.getId());
    if (existedWithSameLogin.isPresent()) {
      throw new EntityExistsException(
        String.format("Пользователь с логином %s уже зарегистрирован", updatedUser.getLogin()));
    }

    var existedWithSameEmail = userService.findUserByEmailAndIdIsNot(updatedUser.getEmail(), updatedUser.getId());
    if (existedWithSameEmail.isPresent()) {
      throw new EntityExistsException(
        String.format("Пользователь с email %s уже зарегистрирован", updatedUser.getEmail()));
    }

    var savedUser = userService.save(updatedUser);

    var responseDto = userMapper.mapUserToDto(savedUser);
    var isLoginEqual = savedUser.getLogin().equals(oldLogin);

    if (!isLoginEqual) {
      var token = jwt.generateToken(savedUser);
      var refreshToken = refreshTokenService.create(savedUser);
      refreshTokenService.revoke(savedUser);
      responseDto.setToken(token);
      responseDto.setRefreshToken(refreshToken);

      if (oldToken.startsWith(BEARER_PREFIX)) {
        var oldTokenContent = extractToken(oldToken);
        blackListService.addToken(oldTokenContent, savedUser.getId());
      }

      var authParams = SetAuthenticationParams.builder()
        .login(userDto.getLogin())
        .password(userDto.getPassword())
        .token(token)
        .refreshToken(refreshToken)
        .privileges(savedUser.getPrivileges())
        .build();

      setAuthentication(authParams);
    }

    return RikserResponseUtils.createResponse(responseDto);
  }

  @Override
  public RikserResponseItem<UserDeactivateResponse> deactivate(UserDeactivateRequestDto requestDto) {
    var user = userService.findById(requestDto.getId());
    userService.changeStatus(user, UserStatus.DEACTIVATED);

    var userIdDto = new UserDeactivateResponse();
    userIdDto.setId(user.getId());

    return RikserResponseUtils.createResponse(userIdDto);
  }

  @Override
  public RikserResponseItem<UserEmailResponse> activateEmail(UserEmailRequestDto requestDto) {
    checkAccess(requestDto.getId(), Privilege.USER_EDIT);

    var user = userService.findById(requestDto.getId());
    userService.changeStatus(user, UserStatus.EMAIL_ACTIVATED);

    var userIdDto = new UserEmailResponse();
    userIdDto.setId(user.getId());

    return RikserResponseUtils.createResponse(userIdDto);
  }

  @Override
  public RikserResponseItem<UserResponseDto> getUser(UUID id) {
    checkAccess(id, Privilege.USER_VIEW);

    var user = userService.findById(id);
    var userDto = userMapper.mapUserToDto(user);

    return RikserResponseUtils.createResponse(userDto);
  }

  @Override
  public RikserResponseItem<JsonNode> getUserByToken(String authToken) {
    var token = extractToken(authToken);
    var details = userDetailSecurityService.getByUsername(token);
    var user = (rikser123.bundle.dto.User) details;

    ObjectNode node = objectMapper.valueToTree(user);
    node.remove("authorities");

    return RikserResponseUtils.createResponse(node);
  }

  @Override
  public RikserResponseItem<UpdateTokenResponseDto> updateToken(String refreshToken) {
    var token = userDetailSecurityService.updateToken(refreshToken);
    var tokenResponseDto = new UpdateTokenResponseDto();
    tokenResponseDto.setToken(token);

    return RikserResponseUtils.createResponse(tokenResponseDto);
  }

  @Override
  public RikserResponseItem<UserFilterResponseDto> findUsers(UserFilterDto filterDto) {
    var users = userService.findAll(filterDto);
    var responseDto = new UserFilterResponseDto();
    responseDto.setTotalElements(users.getTotalElements());
    responseDto.setUsers(users.get().map(userMapper::mapUserToDto).toList());

    return RikserResponseUtils.createResponse(responseDto);
  }


  /**
   * Установка аутентикации в контекст
   *
   * @param params {@link SetAuthenticationParams}
   */
  private void setAuthentication(SetAuthenticationParams params) {
    var authorities = params.getPrivileges().stream().map(privilege -> new SimpleGrantedAuthority(privilege.toString())).toList();

    var authToken = new UsernamePasswordAuthenticationToken(params.getLogin(), params.getPassword(), authorities);
    var tokenDto = new TokenDto();
    tokenDto.setRefreshToken(params.getRefreshToken());
    tokenDto.setAccessToken(params.getToken());
    authToken.setDetails(tokenDto);

    var auth = authenticationManager.authenticate(authToken);

    var context = SecurityContextHolder.getContext();
    context.setAuthentication(auth);
  }

  /**
   * Проверка привилегий на редактирование пользователя
   *
   * @param userId Id пользователя
   */
  private rikser123.bundle.dto.User checkAccess(UUID userId, Privilege privilege) {
    var userDetails = userDetailSecurityService.getCurrentUser();
    var user = (rikser123.bundle.dto.User) userDetails;

    if (!userId.equals(user.getId()) && !user.getPrivileges().contains(privilege)) {
      throw new AccessDeniedException("Доступ запрещен");
    }

    return user;
  }

  /**
   * Извлечение токена из заголовка
   *
   * @param authToken Заголовок с токеном
   */
  private String extractToken(String authToken) {
    return authToken.substring(BEARER_PREFIX.length());
  }


  @Builder
  @Getter
  private static class SetAuthenticationParams {
    private String login;
    private String password;
    private String token;
    private Set<Privilege> privileges;
    private String refreshToken;
  }
}
