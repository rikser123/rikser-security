package rikser123.security.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import rikser123.security.TestData;
import rikser123.security.component.Jwt;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.request.UserEmailRequestDto;
import rikser123.security.mapper.UserMapper;
import rikser123.security.mapper.UserMapperImpl;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.service.impl.SecurityServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Тестирование класса {@link SecurityService}
 *
 */

@ExtendWith(SpringExtension.class)
public class SecurityServiceTest {
    private SecurityService securityService;
    private UserMapper userMapper;

    @Mock
    private Jwt jwt;

    @Mock
    private ReactiveAuthenticationManager authenticationManager;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        userMapper = new UserMapperImpl();
        userMapper.setPasswordEncoder(passwordEncoder);

        securityService = new SecurityServiceImpl(userMapper, jwt, authenticationManager, userInfoService, userService);
    }

    @Test
    void register() {
        var userDto = TestData.createUserRequestDto();
        var user = TestData.createUser();

        when(userService.findUserByLogin(userDto.getLogin())).thenReturn(Optional.empty());
        when(userService.findUserByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userService.save(any())).thenReturn(user);

        StepVerifier.create(securityService.register(userDto))
            .assertNext(result -> {
                assertThat(result.getData().getId()).isEqualTo(user.getId());
            })
           .verifyComplete();
    }

    @Test
    void registerExisted() {
        var userDto = TestData.createUserRequestDto();
        var user = TestData.createUser();

        when(userService.findUserByLogin(userDto.getLogin())).thenReturn(Optional.of(user));

        StepVerifier.create(securityService.register(userDto))
            .verifyError(EntityExistsException.class);
    }

    @Test
    void login() {
        var loginDto = new LoginRequestDto();
        loginDto.setLogin("login");
        loginDto.setPassword("password");
        var user = TestData.createUser();

        when(userService.findUserByLogin(loginDto.getLogin())).thenReturn(Optional.of(user));

        StepVerifier.create(securityService.login(loginDto))
            .assertNext(result -> {
                assertThat(result.getData().getUser().getId()).isEqualTo(user.getId());
            })
            .verifyComplete();
    }

    @Test
    void loginNotFound() {
        var loginDto = new LoginRequestDto();
        loginDto.setLogin("login");
        loginDto.setPassword("password");

        when(userService.findUserByLogin(loginDto.getLogin())).thenReturn(Optional.empty());

        StepVerifier.create(securityService.login(loginDto))
            .verifyError(EntityNotFoundException.class);
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
        when(userInfoService.getCurrentUser()).thenReturn(Mono.just(user));

        StepVerifier.create(securityService.editUser(editDto))
            .assertNext(result -> {
                assertThat(result.getData().getLogin()).isEqualTo(editDto.getLogin());
                assertThat(result.getData().getEmail()).isEqualTo(editDto.getEmail());
                assertThat(result.getData().getFirstName()).isEqualTo(editDto.getFirstName());
            }).verifyComplete();
    }

    @Test
    void editWithNoAccess() {
        var editDto = TestData.createUserEditRequestDto();
        var user = TestData.createUser();
        editDto.setId(UUID.randomUUID());
        user.setUserPrivileges(Collections.emptySet());

        when(userInfoService.getCurrentUser()).thenReturn(Mono.just(user));

        StepVerifier.create(securityService.editUser(editDto))
            .verifyError(AccessDeniedException.class);
    }

    @Test
    void deactivate() {
        var dto = new UserDeactivateRequestDto();
        dto.setId(UUID.randomUUID());
        var user = TestData.createUser();
        user.setId(dto.getId());

        when( userService.findById(dto.getId())).thenReturn(user);
        when(userService.changeStatus(user, UserStatus.DEACTIVATED)).thenReturn(user);

        StepVerifier.create(securityService.deactivate(dto))
            .assertNext(result -> {
                assertThat(result.getData().getId()).isEqualTo(user.getId());
            }).verifyComplete();
    }

    @Test
    void activateEmail() {
        var dto = new UserEmailRequestDto();
        var user = TestData.createUser();
        dto.setId(user.getId());

        when(userInfoService.getCurrentUser()).thenReturn(Mono.just(user));
        when(userService.findById(user.getId())).thenReturn(user);
        when(userService.changeStatus(user, UserStatus.EMAIL_ACTIVATED)).thenReturn(user);

        StepVerifier.create(securityService.activateEmail(dto))
            .assertNext(result -> {
                assertThat(result.getData().getId()).isEqualTo(user.getId());
            }).verifyComplete();
    }

    @Test
    void getUser() {
        var user = TestData.createUser();

        when(userService.findById(user.getId())).thenReturn(user);

        StepVerifier.create(securityService.getUser(user.getId()))
            .assertNext(result -> {
                assertThat(result.getData().getId()).isEqualTo(user.getId());
            }).verifyComplete();
    }
}
