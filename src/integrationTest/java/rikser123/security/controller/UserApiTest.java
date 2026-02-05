package rikser123.security.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import rikser123.security.BaseConfig;
import rikser123.security.IntegrationUtils;
import rikser123.security.TestData;
import rikser123.security.component.Jwt;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.repository.UserRepository;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


/**
 * Класс для для тестирования {@link UserApi}
 *
 */

public class UserApiTest extends BaseConfig {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Jwt jwt;

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void register() {
        var dto = createValidUser();

        client.post().uri(uriBuilder -> uriBuilder
            .path("/api/v1/user/register")
            .build())
            .bodyValue(IntegrationUtils.buildRequest(dto))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath("$.result").isEqualTo(true)
            .jsonPath("$.data.id").isNotEmpty()
            .jsonPath("$.data.token").isNotEmpty();
    }

    @Test
    void registerWithMissingParams() {
        var dto = new CreateUserRequestDto();
        dto.setPassword("password");
        dto.setPasswordConfirmation("password2");

        client.post().uri(uriBuilder -> uriBuilder
            .path("/api/v1/user/register")
            .build())
            .bodyValue(IntegrationUtils.buildRequest(dto))
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath("$.result").isEqualTo(false)
            .jsonPath("$.errors.firstName[0]").isEqualTo("FirstName не должен быть пустым")
            .jsonPath("$.errors.privileges[0]").isEqualTo("Privileges не должно быть пустым");
    }

    @Test
    void registerWithSameLogin() {
        var dto = createValidUser();
        var user = TestData.createUser();
        user.setId(null);
        user.setLogin(dto.getLogin());
        userRepository.save(user);

        client.post().uri(uriBuilder -> uriBuilder
            .path("/api/v1/user/register")
            .build())
            .bodyValue(IntegrationUtils.buildRequest(dto))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath("$.result").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("Пользователь с логином sys11111111111111111111a1aa1121121111121111111112 уже зарегистрирован");
    }

    @Test
    void login() {
        var user = TestData.createUser();
        var rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        var loginDto = new LoginRequestDto();
        loginDto.setPassword(rawPassword);
        loginDto.setLogin(user.getLogin());

        client.post().uri(uriBuilder -> uriBuilder
            .path("/api/v1/user/login")
            .build())
            .bodyValue(IntegrationUtils.buildRequest(loginDto))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath("$.result").isEqualTo(true)
            .jsonPath("$.data.token").isNotEmpty()
            .jsonPath("$.data.user.login").isEqualTo("login")
            .jsonPath("$.data.user.email").isEqualTo("email")
            .jsonPath("$.data.user.firstName").isEqualTo("firstName");
    }

    @Test
    void loginWithUnexisted() {
        var loginDto = new LoginRequestDto();
        loginDto.setPassword("password");
        loginDto.setLogin("login");

        client.post().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/login")
        .build())
        .bodyValue(IntegrationUtils.buildRequest(loginDto))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(false)
        .jsonPath("$.message").isEqualTo("Пользователь с логином login не найден");
    }

    @Test
    void loginWithWrongPassword() {
        var user = TestData.createUser();
        var rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        var loginDto = new LoginRequestDto();
        loginDto.setLogin(user.getLogin());
        loginDto.setPassword("password12345");

        client.post().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/login")
        .build())
        .bodyValue(IntegrationUtils.buildRequest(loginDto))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(false)
        .jsonPath("$.message").isEqualTo("Неверный пароль!");
    }

    @Test
    void editUser() {
        var user = TestData.createUser();
        var savedUser = userRepository.save(user);
        var editDto = TestData.createUserEditRequestDto();
        editDto.setEmail("uuu@rar.ru");
        editDto.setPassword("1111111111a!");
        editDto.setPasswordConfirmation("1111111111a!");
        editDto.setId(savedUser.getId());
        var token = generateAuthHeader(savedUser);

        client.put().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/edit")
        .build())
        .header("Authorization", token)
        .bodyValue(IntegrationUtils.buildRequest(editDto))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(true)
        .jsonPath("$.data.login").isEqualTo("loginNew")
        .jsonPath("$.data.email").isEqualTo("uuu@rar.ru");

    }

    @Test
    void editUserWithSameLogin() {
        var user = TestData.createUser();
        var savedUser = userRepository.save(user);

        var user2 = TestData.createUser();
        user2.setLogin("login2222");
        user2.setEmail("email2");
        var savedUser2 = userRepository.save(user2);

        var editDto = TestData.createUserEditRequestDto();
        editDto.setLogin(savedUser2.getLogin());
        editDto.setEmail("uuu@rar.ru");
        editDto.setPassword("1111111111a!");

        editDto.setPasswordConfirmation("1111111111a!");
        editDto.setId(savedUser.getId());
        var token = generateAuthHeader(savedUser);

        client.put().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/edit")
        .build())
        .header("Authorization", token)
        .bodyValue(IntegrationUtils.buildRequest(editDto))
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(false)
        .jsonPath("$.message").isEqualTo("Пользователь с логином login2222 уже зарегистрирован");
    }

    @Test
    void deactivate() {
        var user = TestData.createUser();
        var savedUser = userRepository.save(user);
        var dto = new UserDeactivateRequestDto();
        dto.setId(user.getId());

        var token = generateAuthHeader(savedUser);

        client.patch().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/deactivate")
        .build())
        .header("Authorization", token)
        .bodyValue(IntegrationUtils.buildRequest(dto))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(true)
        .jsonPath("$.data.id").isEqualTo(dto.getId());
    }

    @Test
    void activateEmail() {
        var user = TestData.createUser();
        var savedUser = userRepository.save(user);
        var dto = new UserEmailResponse();
        dto.setId(user.getId());

        var token = generateAuthHeader(savedUser);

        client.patch().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user//activate-email")
        .build())
        .header("Authorization", token)
        .bodyValue(IntegrationUtils.buildRequest(dto))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(true)
        .jsonPath("$.data.id").isEqualTo(dto.getId());
    }

    @Test
    void getUser() {
        var user = TestData.createUser();
        var savedUser = userRepository.save(user);

        var token = generateAuthHeader(savedUser);

        client.get().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/get/" + savedUser.getId())
        .build())
        .header("Authorization", token)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(true)
        .jsonPath("$.data.id").isEqualTo(savedUser.getId());
    }

    @Test
    void canNotViewAnotherUser() {
        var user = TestData.createUser();
        user.setUserPrivileges(Collections.emptySet());
        var savedUser1 = userRepository.save(user);

        var user2 = TestData.createUser();
        user2.setLogin("login123");
        user2.setEmail("email123");
        user2.setUserPrivileges(Collections.emptySet());
        var savedUser2 = userRepository.save(user2);

        var token = generateAuthHeader(savedUser1);

        client.get().uri(uriBuilder -> uriBuilder
        .path("/api/v1/user/get/" + savedUser2.getId())
        .build())
        .header("Authorization", token)
        .exchange()
        .expectStatus().isForbidden()
        .expectBody()
        .consumeWith(System.out::println)
        .jsonPath("$.result").isEqualTo(false)
        .jsonPath("$.message").isEqualTo("Доступ к запрашиваемому ресурсу запрещен");
    }

    private static CreateUserRequestDto createValidUser() {
        var dto = new CreateUserRequestDto();
        dto.setLogin("sys11111111111111111111a1aa1121121111121111111112");
        dto.setPassword("1111111111a!");
        dto.setPasswordConfirmation("1111111111a!");
        dto.setEmail("sys1111112111a111212121112112@11111111rar.ru");
        dto.setLastName("sys");
        dto.setFirstName("sys");
        dto.setMiddleName("sys");
        dto.setBirthDate(LocalDate.of(1900, 1, 1));
        dto.setPrivileges(List.of(Privilege.USER_EDIT, Privilege.USER_DELETE));
        return dto;
    }

    private String generateAuthHeader(User user) {
        var token = jwt.generateToken(user);
        return "Bearer " + token;
    }
}
