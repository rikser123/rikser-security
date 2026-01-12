package rikser123.security.service;

import jakarta.persistence.EntityExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.request.UserDeactivateRequest;
import rikser123.security.dto.request.UserEmailRequest;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.security.dto.response.UserDeactivateResponse;
import rikser123.security.dto.response.UserEmailResponse;
import rikser123.security.dto.response.UserResponseDto;
import rikser123.security.mapper.UserMapper;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.utils.JwtUtils;
import rikser123.bundle.utils.RikserResponseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class SecurityService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserInfoService userInfoService;
    private final UserService userService;

    public ResponseEntity<RikserResponseItem<CreateUserResponseDto>> register(CreateUserRequestDto requestDto) {
        var existedWithSameLogin = userService.findUserByLogin(requestDto.getLogin());

        if (existedWithSameLogin.isPresent()) {
            throw new EntityExistsException(String.format("Пользователь с логином %s уже зарегистрирован", requestDto.getLogin()));
        }

        var existedWithSameEmail = userService.findUserByEmail(requestDto.getEmail());

        if (existedWithSameEmail.isPresent()) {
            throw new EntityExistsException(String.format("Пользователь с email %s уже зарегистрирован", requestDto.getEmail()));
        }

        var user = userMapper.mapUser(requestDto);

        userService.save(user);
        var token = jwtUtils.generateToken(user);

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                requestDto.getLogin(),
                requestDto.getPassword()
        ));

        var responseDto = new CreateUserResponseDto();
        responseDto.setId(user.getId());
        responseDto.setToken(token);
        var response = RikserResponseUtils.createResponse(responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<RikserResponseItem<LoginResponseDto>> login(LoginRequestDto requestDto) {
        var userLogin = requestDto.getLogin();
        var userOpt = userService.findUserByLogin(userLogin);

        if (userOpt.isEmpty()) {
            var errorValue =  List.of(String.format("Пользователь с логином %s не найден", userLogin));
            return createErrorResponse("login", errorValue);
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLogin,
                requestDto.getPassword()
        ));

        var user = userOpt.get();

        var token = jwtUtils.generateToken(user);
        var responseDto = new LoginResponseDto();
        responseDto.setToken(token);

        var userDto = userMapper.mapUserToDto(user);
        responseDto.setUser(userDto);

        var response = RikserResponseUtils.createResponse(responseDto);
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<RikserResponseItem<UserResponseDto>> editUser(EditUserDto userDto) {
        checkEditAccess(userDto.getId());

        var updatedUser = userService.findById(userDto.getId());
        userMapper.updateUser(userDto, updatedUser);

        var existedWithSameLogin = userService.findUserByLoginAndIdIsNot(updatedUser.getLogin(), updatedUser.getId());

        if (existedWithSameLogin.isPresent()) {
            throw new EntityExistsException(String.format("Пользователь с логином %s уже зарегистрирован", updatedUser.getLogin()));
        }

        var existedWithSameEmail = userService.findUserByEmailAndIdIsNot(updatedUser.getEmail(), updatedUser.getId());

        if (existedWithSameEmail.isPresent()) {
            throw new EntityExistsException(String.format("Пользователь с email %s уже зарегистрирован", updatedUser.getEmail()));
        }

        var savedUser = userService.save(updatedUser);
        var responseDto = userMapper.mapUserToDto(savedUser);

        var response = RikserResponseUtils.createResponse(responseDto);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<RikserResponseItem<UserDeactivateResponse>> deactivate(UserDeactivateRequest requestDto) {
        var user = userService.findById(requestDto.getId());
        userService.changeStatus(user, UserStatus.DEACTIVATED);

        var userIdDto = new UserDeactivateResponse();
        userIdDto.setId(user.getId());

        var response = RikserResponseUtils.createResponse(userIdDto);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<RikserResponseItem<UserEmailResponse>> activateEmail(UserEmailRequest requestDto) {
        checkEditAccess(requestDto.getId());

        var user = userService.findById(requestDto.getId());
        userService.changeStatus(user, UserStatus.EMAIL_ACTIVATED);

        var userIdDto = new UserEmailResponse();
        userIdDto.setId(user.getId());

        var response = RikserResponseUtils.createResponse(userIdDto);
        return ResponseEntity.ok(response);
    }

    private static <T> ResponseEntity<RikserResponseItem<T>> createErrorResponse(String errorKey, List<String> errorValue) {
        var errors = new HashMap<String, List<String>>();
        errors.put(errorKey, errorValue);
        var response = RikserResponseUtils.createResponse(errors, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private void checkEditAccess(UUID userId) {
        var currentUser = userInfoService.getCurrentUser();

        if (!userId.equals(currentUser.getId()) && !currentUser.getPrivileges().contains(Privilege.USER_EDIT)) {
            throw new AccessDeniedException("Доступ запрещен");
        }
    }
}
