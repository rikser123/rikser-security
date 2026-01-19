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
import rikser123.security.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {
    private final UserRepository userRepository;
    public Mono<UserDetails> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(data -> data.getName())
                .flatMap(this::getByUsername);
    }

    public ReactiveUserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public Mono<UserDetails> getByUsername(String username) {
        return Mono.just(userRepository.findUserByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден")));

    }
}