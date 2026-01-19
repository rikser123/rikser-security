package rikser123.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import rikser123.security.service.UserInfoService;
import rikser123.security.component.Jwt;
import rikser123.security.repository.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Фильтр аутентификации JWT для Spring WebFlux Security
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final Jwt jwt;
    private final UserInfoService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var authHeader = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);

        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        var token = authHeader.substring(BEARER_PREFIX.length());

        var username = jwt.extractUserName(token);

        if (StringUtils.isEmpty(username)) {
            log.warn("JWT token does not contain username");
            return chain.filter(exchange);
        }

        if (!jwt.isTokenValid(token)) {
            log.warn("Invalid JWT token for user: {}", username);
            return chain.filter(exchange);
        }

        return userService.getByUsername(username)
                .flatMap(userDetails -> {
                    var authentication = createAuthenticationToken(userDetails);

                    log.debug("User authenticated successfully: {}", username);

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("User not found: {}", username);
                    return chain.filter(exchange);
                }))
                .onErrorResume(e -> {
                    log.error("Error during authentication for user: {}", username, e);
                    return chain.filter(exchange);
                });
    }

    /**
     * Создает объект аутентификации на основе UserDetails
     */
    private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
        // Проверяем, является ли userDetails экземпляром нашего User
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            List<SimpleGrantedAuthority> authorities = user.getPrivileges().stream()
                    .map(privilege -> new SimpleGrantedAuthority(privilege.name()))
                    .collect(Collectors.toList());

            return new UsernamePasswordAuthenticationToken(
                    user,
                    null, // credentials - обычно null для JWT
                    authorities
            );
        } else {
            // Для стандартных UserDetails
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        }
    }
}