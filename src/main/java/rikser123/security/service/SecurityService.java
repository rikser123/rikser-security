package rikser123.security.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.LoginRequestDto;
import rikser123.security.dto.response.CreateUserResponseDto;
import rikser123.security.dto.response.LoginResponseDto;
import rikser123.security.dto.response.RikserResponseItem;
import rikser123.security.mapper.UserMapper;
import rikser123.security.repository.UserRepository;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.UserPrivilege;
import rikser123.security.utils.JwtUtils;
import rikser123.security.utils.RikserResponseUtils;

import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SecurityService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<RikserResponseItem<CreateUserResponseDto>> register(CreateUserRequestDto requestDto) {
        var existedWithSameLogin = userRepository.findUserByLogin(requestDto.getLogin());

        if (existedWithSameLogin.isPresent()) {
           var errorValue = List.of(String.format("Пользователь с логином %s уже зарегистрирован", requestDto.getLogin()));
            return createErrorResponse("login", errorValue);
        }

        var existedWithSameEmail = userRepository.findUserByEmail(requestDto.getEmail());

        if (existedWithSameEmail.isPresent()) {
            var errorValue =  List.of(String.format("Пользователь с email %s уже зарегистрирован", requestDto.getEmail()));
            return createErrorResponse("email", errorValue);
        }

        var user = userMapper.mapUser(requestDto);

        var userPrivilege = new UserPrivilege();
        userPrivilege.setPrivilege(Privilege.USER);
        userPrivilege.setUser(user);
        user.getPrivileges().add(userPrivilege);

        userRepository.save(user);
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
        var userOpt = userRepository.findUserByLogin(userLogin);

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

    private static <T> ResponseEntity<RikserResponseItem<T>> createErrorResponse(String errorKey, List<String> errorValue) {
        var errors = new HashMap<String, List<String>>();
        errors.put(errorKey, errorValue);
        var response = RikserResponseUtils.createResponse(errors, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
