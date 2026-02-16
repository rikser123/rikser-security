package rikser123.security.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import rikser123.bundle.service.UserDetailService;
import rikser123.security.component.Jwt;
import rikser123.security.mapper.UserMapper;
import rikser123.security.service.BlackListService;
import rikser123.security.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {
  private final UserService userService;
  private final UserMapper userMapper;
  private final Jwt jwt;
  private final BlackListService blackListService;

  @Override
  public Mono<UserDetails> getCurrentUser() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(data -> (UserDetails) data.getPrincipal());
  }

  @Override
  public ReactiveUserDetailsService userDetailsService() {
    return this::getByUsername;
  }

  @Override
  public Mono<UserDetails> getByUsername(String token) {
    return Mono.fromCallable(() -> blackListService.findByToken(token))
        .flatMap(
            blackOpt -> {
              if (blackOpt.isPresent()) {
                return Mono.error(new EntityExistsException("Токен в блеклисте"));
              }
              return Mono.fromCallable(() -> jwt.extractUserName(token));
            })
        .map(userService::findUserByLogin)
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(userOpt -> userOpt.map(Mono::just).orElse(Mono.empty()))
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new EntityNotFoundException("Пользователь не найден"))))
        .map(userMapper::mapToSecurityUser);
  }
}
