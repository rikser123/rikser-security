package rikser123.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import rikser123.bundle.utils.RikserResponseUtils;
import rikser123.security.component.Jwt;
import rikser123.security.repository.entity.User;
import rikser123.security.service.UserInfoService;

/** Фильтр аутентификации JWT для Spring WebFlux Security */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String HEADER_NAME = "Authorization";
  private final Jwt jwt;
  private final UserInfoService userService;
  private final ObjectMapper objectMapper;
  private final Map<String, Boolean> processedRequests = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var requestId = exchange.getRequest().getId();

    if (processedRequests.putIfAbsent(requestId, true) != null) {
      return chain.filter(exchange);
    }

    return authenticate(exchange, chain)
        .doFinally(
            signal -> {
              processedRequests.remove(requestId);
            });
  }

  /**
   * Обработка json web token
   *
   * @param exchange Запрос
   * @param chain Фильтры spring security
   */
  private Mono<Void> authenticate(ServerWebExchange exchange, WebFilterChain chain) {
    var authHeader = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);

    if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      return chain.filter(exchange);
    }

    var token = authHeader.substring(BEARER_PREFIX.length());
    var username = StringUtils.EMPTY;

    try {
      username = jwt.extractUserName(token);
    } catch (MalformedJwtException e) {
      return sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Некорректный формат JWT токена");
    } catch (ExpiredJwtException e) {
      return sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Срок действия токена истек");
    } catch (Exception e) {
      return sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Ошибка валидации токена");
    }

    if (StringUtils.isEmpty(username)) {
      log.warn("JWT token does not contain username");
      return chain.filter(exchange);
    }

    return userService
        .getByUsername(username)
        .flatMap(
            userDetails -> {
              var authentication = createAuthenticationToken(userDetails);
              log.warn("autu {}", authentication);

              return chain
                  .filter(exchange)
                  .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            })
        .switchIfEmpty(chain.filter(exchange))
        .onErrorResume(
            e -> {
              log.error("Error during authentication for user: {}", "111", e);
              return chain.filter(exchange);
            });
  }

  /**
   * Обработка ошибок токена
   *
   * @param exchange Запрос
   * @param status Статус запроса
   * @param message Сообщение об ошибке
   */
  private Mono<Void> sendErrorResponse(
      ServerWebExchange exchange, HttpStatus status, String message) {
    return Mono.fromCallable(
            () -> {
              exchange.getResponse().setStatusCode(status);
              exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

              var responseBody = RikserResponseUtils.createResponse(message, status);

              var jsonText = objectMapper.writer().writeValueAsString(responseBody);
              var bytes = jsonText.getBytes(StandardCharsets.UTF_8);
              return exchange.getResponse().bufferFactory().wrap(bytes);
            })
        .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
        .onErrorResume(
            e -> {
              log.error("Error writing authentication error response", e);

              // Fallback: простой текст в случае ошибки
              var errorMessage = "{\"error\":\"Authentication failed\"}";
              var buffer =
                  exchange
                      .getResponse()
                      .bufferFactory()
                      .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

              return exchange.getResponse().writeWith(Mono.just(buffer));
            });
  }

  /** Создает объект аутентификации на основе UserDetails */
  private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails) {
    // Проверяем, является ли userDetails экземпляром нашего User
    if (userDetails instanceof User) {
      var user = (User) userDetails;
      List<SimpleGrantedAuthority> authorities =
          user.getPrivileges().stream()
              .map(privilege -> new SimpleGrantedAuthority(privilege.name()))
              .toList();

      return new UsernamePasswordAuthenticationToken(
          user,
          null, // credentials - обычно null для JWT
          authorities);
    } else {
      // Для стандартных UserDetails
      return new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
    }
  }
}
