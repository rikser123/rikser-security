package rikser123.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.security.TestData;
import rikser123.security.component.Jwt;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.dto.request.UserFilterDto;
import rikser123.security.mapper.UserMapper;
import rikser123.security.mapper.UserMapperImpl;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.service.impl.SecurityServiceImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Тестирование класса {@link SecurityService} (синхронная версия)
 */
@ExtendWith(SpringExtension.class)
public class SecurityServiceTest {
  @Mock
  RefreshTokenService refreshTokenService;
  private SecurityService securityService;
  private UserMapper userMapper;
  @Mock
  private Jwt jwt;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private UserDetailSecurityService userDetailService;
  @Mock
  private UserService userService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private BlackListService blackListService;

  @BeforeEach
  void init() {
    userMapper = new UserMapperImpl();
    userMapper.setPasswordEncoder(passwordEncoder);

    securityService = new SecurityServiceImpl(
      userMapper,
      jwt,
      authenticationManager,
      userDetailService,
      userService,
      passwordEncoder,
      blackListService,
      new ObjectMapper(),
      refreshTokenService
    );

    SecurityContextHolder.clearContext();
  }

  @Test
  void register() {
    var userDto = TestData.createUserRequestDto();
    var user = TestData.createUser();

    when(userService.findUserByLogin(userDto.getLogin())).thenReturn(Optional.empty());
    when(userService.findUserByEmail(userDto.getEmail())).thenReturn(Optional.empty());
    when(userService.save(any())).thenReturn(user);
    when(authenticationManager.authenticate(any()))
      .thenReturn(new AuthenticationMock());

    var result = securityService.register(userDto);

    assertThat(result.getData().getId()).isEqualTo(user.getId());
  }

  @Test
  void registerExisted() {
    var userDto = TestData.createUserRequestDto();
    var user = TestData.createUser();

    when(userService.findUserByLogin(userDto.getLogin())).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> securityService.register(userDto))
      .isInstanceOf(EntityExistsException.class);
  }

  @Test
  void login() {
    var loginDto = new LoginRequestDto();
    loginDto.setLogin("login");
    loginDto.setPassword("password");
    var user = TestData.createUser();

    when(userService.findUserByLogin(loginDto.getLogin())).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any()))
      .thenReturn(new AuthenticationMock());
    when(passwordEncoder.matches(any(), any())).thenReturn(true);

    var result = securityService.login(loginDto);

    assertThat(result.getData().getUser().getId()).isEqualTo(user.getId());
  }

  @Test
  void loginNotFound() {
    var loginDto = new LoginRequestDto();
    loginDto.setLogin("login");
    loginDto.setPassword("password");

    when(userService.findUserByLogin(loginDto.getLogin())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> securityService.login(loginDto))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void edit() {
    var editDto = TestData.createUserEditRequestDto();
    var user = TestData.createUser();
    editDto.setId(user.getId());
    userMapper.updateUser(editDto, user);

    when(userService.findById(editDto.getId())).thenReturn(user);
    when(userService.findUserByLogin(user.getLogin())).thenReturn(Optional.empty());
    when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());
    when(userService.save(any())).thenReturn(user);
    when(userDetailService.getCurrentUser()).thenReturn(user);
    when(authenticationManager.authenticate(any()))
      .thenReturn(new AuthenticationMock());

    var result = securityService.editUser(editDto, "Bearer 12345");

    assertThat(result.getData().getLogin()).isEqualTo(editDto.getLogin());
    assertThat(result.getData().getEmail()).isEqualTo(editDto.getEmail());
    assertThat(result.getData().getFirstName()).isEqualTo(editDto.getFirstName());
  }

  @Test
  void editWithNoAccess() {
    var editDto = TestData.createUserEditRequestDto();
    var user = TestData.createUser();
    editDto.setId(UUID.randomUUID());
    user.setUserPrivileges(Collections.emptySet());

    when(userDetailService.getCurrentUser()).thenReturn(user);

    assertThatThrownBy(() -> securityService.editUser(editDto, "Bearer 12345"))
      .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void deactivate() {
    var dto = new UserDeactivateRequestDto();
    dto.setId(UUID.randomUUID());
    var user = TestData.createUser();
    user.setId(dto.getId());

    when(userService.findById(dto.getId())).thenReturn(user);
    when(userService.changeStatus(user, UserStatus.DEACTIVATED)).thenReturn(user);

    var result = securityService.deactivate(dto);

    assertThat(result.getData().getId()).isEqualTo(user.getId());
  }

  @Test
  void activateEmail() {
    var dto = new UserEmailRequestDto();
    var user = TestData.createUser();
    dto.setId(user.getId());

    when(userDetailService.getCurrentUser()).thenReturn(user);
    when(userService.findById(user.getId())).thenReturn(user);
    when(userService.changeStatus(user, UserStatus.EMAIL_ACTIVATED)).thenReturn(user);

    var result = securityService.activateEmail(dto);

    assertThat(result.getData().getId()).isEqualTo(user.getId());
  }

  @Test
  void getUser() {
    var user = TestData.createUser();

    when(userService.findById(user.getId())).thenReturn(user);
    when(userDetailService.getCurrentUser()).thenReturn(user);

    var result = securityService.getUser(user.getId());

    assertThat(result.getData().getId()).isEqualTo(user.getId());
  }

  @Test
  void getUsers() {
    var users = List.of(TestData.createUser());
    var filter = new UserFilterDto();

    when(userService.findAll(any())).thenReturn(new PageImpl<>(users));

    var result = securityService.findUsers(filter);
    assertThat(result.getData().getUsers().size()).isEqualTo(1);
  }

  private static class AuthenticationMock implements Authentication {
    private boolean authenticated = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      return "user";
    }

    @Override
    public boolean isAuthenticated() {
      return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
      this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
      return "user";
    }
  }
}