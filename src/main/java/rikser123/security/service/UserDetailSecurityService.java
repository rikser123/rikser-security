package rikser123.security.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import rikser123.bundle.service.UserDetailService;
import rikser123.security.component.Jwt;
import rikser123.security.mapper.UserMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class UserDetailSecurityService implements UserDetailService {
  private final UserService userService;
  private final UserMapper userMapper;
  private final Jwt jwt;
  private final BlackListService blackListService;

  public UserDetails getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("Пользователь не аутентифицирован");
    }
    return (UserDetails) authentication.getPrincipal();
  }

  @Override
  public UserDetailsService userDetailsService() {
    return this::getByLogin;
  }

  private UserDetails getByLogin(String login) {
    var user = userService.findUserByLogin(login)
      .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    return userMapper.mapToSecurityUser(user);
  }

  @Override
  public UserDetails getByUsername(String token) {
    var blackOptional = blackListService.findByToken(token);
    if (blackOptional.isPresent()) {
      throw new EntityExistsException("Токен в блеклисте");
    }

    var username = jwt.extractUserName(token);

    var user = userService.findUserByLogin(username)
      .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

    return userMapper.mapToSecurityUser(user);
  }
}
