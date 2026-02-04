package rikser123.security.service;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {
    private final UserService userService;

    public Mono<UserDetails> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(data -> (UserDetails) data.getPrincipal());
    }

    public ReactiveUserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public Mono<UserDetails> getByUsername(String username) {
       return Mono.fromCallable(() -> userService.findUserByLogin(username))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(userOpt -> {
                if (userOpt.isPresent()) {
                    return Mono.just(userOpt.get());
                }

                return Mono.error(new EntityNotFoundException("Пользователь не найден"));
            });
    }
}