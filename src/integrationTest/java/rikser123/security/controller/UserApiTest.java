package rikser123.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import rikser123.security.BaseConfig;
import rikser123.security.IntegrationUtils;
import rikser123.security.TestData;
import rikser123.security.component.Jwt;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequestDto;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.repository.RefreshTokenRepository;
import rikser123.security.repository.UserRepository;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.User;
import rikser123.security.service.RefreshTokenService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserApiTest extends BaseConfig {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Jwt jwt;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

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

  @BeforeEach
  void cleanup() {
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void register() throws Exception {
    var dto = createValidUser();

    client.perform(post("/api/v1/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(dto))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.id").isNotEmpty())
      .andExpect(jsonPath("$.data.token").isNotEmpty())
      .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
  }

  @Test
  void registerWithMissingParams() throws Exception {
    var dto = new CreateUserRequestDto();
    dto.setPassword("password");
    dto.setPasswordConfirmation("password2");

    client.perform(post("/api/v1/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(dto))))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.result").value(false))
      .andExpect(jsonPath("$.errors.firstName[0]").value("FirstName не должен быть пустым"))
      .andExpect(jsonPath("$.errors.privileges[0]").value("Privileges не должно быть пустым"));
  }

  @Test
  void registerWithSameLogin() throws Exception {
    var dto = createValidUser();
    var user = TestData.createUser();
    user.setId(null);
    user.setLogin(dto.getLogin());
    userRepository.save(user);

    client.perform(post("/api/v1/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(dto))))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.result").value(false))
      .andExpect(jsonPath("$.message").value(
        "Пользователь с логином sys11111111111111111111a1aa1121121111121111111112 уже зарегистрирован"));
  }

  @Test
  void login() throws Exception {
    var user = TestData.createUser();
    var rawPassword = user.getPassword();
    user.setPassword(passwordEncoder.encode(rawPassword));
    userRepository.save(user);

    var loginDto = new LoginRequestDto();
    loginDto.setPassword(rawPassword);
    loginDto.setLogin(user.getLogin());

    client.perform(post("/api/v1/user/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(loginDto))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.token").isNotEmpty())
      .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
      .andExpect(jsonPath("$.data.user.login").value("login"))
      .andExpect(jsonPath("$.data.user.email").value("email"))
      .andExpect(jsonPath("$.data.user.firstName").value("firstName"));
  }

  @Test
  void loginWithUnexisted() throws Exception {
    var loginDto = new LoginRequestDto();
    loginDto.setPassword("password");
    loginDto.setLogin("login");

    client.perform(post("/api/v1/user/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(loginDto))))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.result").value(false))
      .andExpect(jsonPath("$.message").value("Пользователь с логином login не найден"));
  }

  @Test
  void loginWithWrongPassword() throws Exception {
    var user = TestData.createUser();
    var rawPassword = user.getPassword();
    user.setPassword(passwordEncoder.encode(rawPassword));
    userRepository.save(user);

    var loginDto = new LoginRequestDto();
    loginDto.setLogin(user.getLogin());
    loginDto.setPassword("password12345");

    client.perform(post("/api/v1/user/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(loginDto))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.result").value(false))
      .andExpect(jsonPath("$.message").value("Неверный пароль!"));
  }

  @Test
  void editUser() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);
    var editDto = TestData.createUserEditRequestDto();
    editDto.setEmail("uuu@rar.ru");
    editDto.setPassword("1111111111a!");
    editDto.setPasswordConfirmation("1111111111a!");
    editDto.setId(savedUser.getId());
    var token = generateAuthHeader(savedUser);

    client.perform(put("/api/v1/user/edit")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(editDto))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.login").value("loginNew"))
      .andExpect(jsonPath("$.data.email").value("uuu@rar.ru"));
  }

  @Test
  void editUserWithUpdatedLogin() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);
    var editDto = TestData.createUserEditRequestDto();
    editDto.setEmail("uuu@rar.ru");
    editDto.setPassword("1111111111a!");
    editDto.setPasswordConfirmation("1111111111a!");
    editDto.setLogin("my_new_login");
    editDto.setId(savedUser.getId());
    var token = generateAuthHeader(savedUser);

    client.perform(put("/api/v1/user/edit")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(editDto))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.login").value("my_new_login"))
      .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
      .andExpect(jsonPath("$.data.token").isNotEmpty());
  }

  @Test
  void editUserWithSameLogin() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);

    var user2 = TestData.createUser();
    user2.setLogin("login2222");
    user2.setEmail("email2");
    userRepository.save(user2);

    var editDto = TestData.createUserEditRequestDto();
    editDto.setLogin("login2222");
    editDto.setEmail("uuu@rar.ru");
    editDto.setPassword("1111111111a!");
    editDto.setPasswordConfirmation("1111111111a!");
    editDto.setId(savedUser.getId());
    var token = generateAuthHeader(savedUser);

    client.perform(put("/api/v1/user/edit")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(editDto))))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.result").value(false))
      .andExpect(jsonPath("$.message").value("Пользователь с логином login2222 уже зарегистрирован"));
  }

  @Test
  void deactivate() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);
    var dto = new UserDeactivateRequestDto();
    dto.setId(user.getId());
    var token = generateAuthHeader(savedUser);

    client.perform(patch("/api/v1/user/deactivate")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(dto))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.id").value(dto.getId()));
  }

  @Test
  void activateEmail() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);
    var dto = new UserEmailResponse();
    dto.setId(user.getId());
    var token = generateAuthHeader(savedUser);

    client.perform(patch("/api/v1/user/activate-email")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(dto))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.id").value(dto.getId()));
  }

  @Test
  void getUser() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);
    var token = generateAuthHeader(savedUser);

    client.perform(get("/api/v1/user/get/" + savedUser.getId())
        .header("Authorization", token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.id").value(savedUser.getId()));
  }

  @Test
  void canNotViewAnotherUser() throws Exception {
    var user = TestData.createUser();
    user.setUserPrivileges(Collections.emptySet());
    var savedUser1 = userRepository.save(user);

    var user2 = TestData.createUser();
    user2.setLogin("login123");
    user2.setEmail("email123");
    user2.setUserPrivileges(Collections.emptySet());
    var savedUser2 = userRepository.save(user2);

    var token = generateAuthHeader(savedUser1);

    client.perform(get("/api/v1/user/get/" + savedUser2.getId())
        .header("Authorization", token))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.result").value(false))
      .andExpect(jsonPath("$.message").value("Доступ к запрашиваемому ресурсу запрещен"));
  }

  @Test
  void updateToken() throws Exception {
    var user = TestData.createUser();
    userRepository.save(user);
    var refreshToken = refreshTokenService.create(user);

    client.perform(get("/api/v1/user/token/refresh")
        .header("X-Refresh-Token", refreshToken)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.token").isNotEmpty());
  }

  @Test
  void getUsersList() throws Exception {
    var user = TestData.createUser();
    var savedUser = userRepository.save(user);
    var token = generateAuthHeader(savedUser);

    client.perform(get("/api/v1/user")
        .queryParam("lastName", savedUser.getLastName())
        .header("Authorization", token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.users[0].lastName").value(savedUser.getLastName()));
  }

  private String generateAuthHeader(User user) {
    return "Bearer " + jwt.generateToken(user);
  }
}